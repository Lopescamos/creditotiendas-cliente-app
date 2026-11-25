# Estado — payment

Cubre US-10 (pagar cuenta, UF-06). **Regla de negocio no negociable:
sin pagos parciales — el monto no es editable por el usuario, lo
calcula el backend.**

## Model (dejado por el agente model)

Cubierto en `feature/project-setup`:
- `GET /api/v1/credit-profiles/{id}/payment-due?installmentIds=` →
  `PaymentDueResponse`.
- `POST /api/v1/credit-profiles/{id}/payments` body
  `RegisterPaymentRequest(installmentIds: List<Long>)` — **sin campo
  de monto**, es la forma exigida por el contrato.

## ViewModel (dejado por el agente viewmodel)

### `viewmodel/PaymentViewModel.kt`
- `StateFlow` expuestos: `due` (desglose exigible), `result` (pago
  registrado), `loading`, `error`.
- `loadDue(profileId, installmentIds, context)` — pide el desglose
  exigible para los IDs seleccionados. Nunca recibe un monto del
  usuario.
- `confirmPayment(profileId, installmentIds, context)` — envía solo
  `installmentIds`. El monto íntegro lo calcula el backend; la app no
  puede proponer otro.
- Mapeo de errores: 422 → "El pago no cubre el monto íntegro
  exigible.", otros → genérico; excepción → "Sin conexión al
  servidor."
- `reset()` limpia `due`, `result`, `error`.
- Constructor expone seam `webService: WebService? = null` para
  tests.

## View (dejado por el agente view)

### `view/PayScreen.kt`
- Muestra el desglose exigible (`CtCard`): capital, interés
  ordinario, mora — tal cual lo devuelve el backend.
- **No existe campo de monto editable.** El botón "Confirmar pago
  íntegro" envía solo los `installmentIds`.
- Prelación de imputación (mora → interés → capital) se muestra en el
  orden en que viene del backend, sin recalcular.
- Tras `confirmPayment` exitoso, muestra confirmación con el número
  de comprobante.

## Tests (dejado por el agente tests)

### `app/src/test/.../viewmodel/PaymentViewModelTest.kt`

Cubre:
- Estado inicial vacío.
- `loadDue` exitoso → `due` poblado con el desglose, `loading=false`.
- **Regla de negocio no trivial (sin monto editable):** `confirmPayment`
  envía `RegisterPaymentRequest(installmentIds=[...])` — captura el
  argumento real vía `ArgumentCaptor` y verifica que **el único campo
  del DTO es `installmentIds`**, no existe un campo de monto. Doble
  check: inspección reflexiva sobre `RegisterPaymentRequest`.
- `confirmPayment` con 422 → `error = "El pago no cubre el monto
  íntegro exigible."`, `result=null`, `loading=false`.
- `confirmPayment` con excepción de red → `error = "Sin conexión al
  servidor."`, `loading=false`.

**No cubierto:**
- Test de UI Compose de `PayScreen` verificando que **no existe un
  `TextField` de monto** — habría cerrado la regla por ambos lados
  (dato + UI). Pendiente si el tiempo sobra; hoy la regla queda
  cubierta por el lado del VM/dato que es donde más fácil se rompe.
