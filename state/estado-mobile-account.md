# Estado — account

Cubre US-03 (consultar mi cuenta, UF-02). Multi-perfil: una relación
por tienda, nunca se mezclan saldos.

## Model (dejado por el agente model)

Cubierto completamente en `feature/project-setup`. Endpoints usados:
- `GET /api/v1/credit-profiles/me`
- `GET /api/v1/credit-profiles/{id}/credit-sales`
- `GET /api/v1/credit-profiles/{id}/payments`

DTOs: `CreditProfileResponse`, `CreditSaleResponse`, `PaymentResponse`.

## ViewModel (dejado por el agente viewmodel)

### `viewmodel/AccountViewModel.kt`
- 7 `StateFlow` expuestos: `profiles`, `selectedProfile`, `sales`,
  `installments`, `payments`, `statements`, `loading`, `error`.
- `loadProfiles(context)` — carga listado y auto-selecciona el
  primero si no hay seleccionado (los demás permanecen disponibles).
- `selectProfile(profile, context)` — cambia el seleccionado y recarga
  ventas y pagos de **ese** perfil, nunca agregados.
- `loadSales`, `loadInstallments`, `loadPayments`, `loadStatements` —
  por perfil o venta, nunca cross-tienda.
- Manejo de errores: HTTP → mensaje con código; excepción → "Sin
  conexión al servidor."; silenciado solo en cargas secundarias
  (`sales`, `payments`) para no bloquear la pantalla si una llamada
  colateral falla.
- Constructor expone seam `webService: WebService? = null` para
  inyección en tests, igual que `AuthViewModel`.

## View (dejado por el agente view)

### `view/MyAccountScreen.kt`
- Landing tras login (UF-02).
- Tarjeta por perfil (`CtCard`): saldo pendiente, próximo pago
  (monto, fecha, estado con `CtBadge`), movimientos recientes.
- Selector de tienda si el cliente tiene más de un perfil — cada
  tienda se muestra independiente, sin mezclar saldos.
- Botones de acción: "Ver cuotas" (→ `InstallmentsScreen`),
  "Pagar" (→ `PayScreen`). Navegación real llega con
  `feature/navigation`.
- Muestra `loading` como spinner y `error` como banner.
