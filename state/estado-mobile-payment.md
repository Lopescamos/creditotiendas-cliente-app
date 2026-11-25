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
