# Contrato de API — qué consume la app mobile

Subconjunto del backend que usa el rol `CLIENT`. No dupliques el
contrato completo del backend — solo lo que esta app realmente llama,
vía `WebService.kt`.

## Autenticación

### `POST /api/v1/authentication/sign-in`
- **Request:** `{ email, password }`
- **Consumido por:** `AuthViewModel` → `LoginScreen`
- **Regla:** si el rol devuelto no es `CLIENT`, se rechaza con el
  mensaje "Esta app es solo para clientes. Usa el panel web si eres
  administrador."

### `GET /api/v1/users/me`
- **Consumido por:** `AuthViewModel` (hidratar sesión / verificar rol)

## Mi cuenta (US-03)

### `GET /api/v1/credit-profiles/me`
- **Consumido por:** `AccountViewModel` → `MyAccountScreen`

### `GET /api/v1/credit-profiles/{id}`
- **Consumido por:** `AccountViewModel` (detalle de un perfil puntual)

### `GET /api/v1/credit-profiles/{id}/credit-sales`
- **Consumido por:** `AccountViewModel` (movimientos recientes)

### `GET /api/v1/credit-profiles/{id}/payments`
- **Consumido por:** `AccountViewModel` (histórico de pagos)

## Plan de cuotas (US-07)

### `GET /api/v1/credit-sales/{id}`
- **Consumido por:** `AccountViewModel` / `InstallmentsScreen`

### `GET /api/v1/credit-sales/{id}/payment-plan`
- **Consumido por:** `InstallmentsScreen`
- **Regla:** los valores son el snapshot de la venta, no las
  condiciones actuales del perfil.

### `GET /api/v1/credit-sales/{id}/installments`
- **Consumido por:** `InstallmentsScreen`

## Solicitar compra a crédito (US-08)

### `GET /api/v1/credit-profiles/{id}/purchase-request-eligibility`
- **Consumido por:** `EligibilityScreen` (pre-chequeo, 4 condiciones)

### `GET /api/v1/stores/{storeId}/products`
- **Consumido por:** `RequestCatalogScreen` (solo productos activos)

### `POST /api/v1/credit-profiles/{id}/purchase-requests`
- **Request:** `{ items: [{ productoId, cantidad }] }`
- **Consumido por:** `PurchaseRequestViewModel` → `ReviewRequestScreen`
- **Regla:** si no cumple elegibilidad, el backend responde 422 — la
  app no debe dejar enviar si `EligibilityScreen` ya marcó que no
  cumple.

## Consultar estado de solicitud (US-09)

### `GET /api/v1/credit-profiles/{id}/purchase-requests`
- **Consumido por:** `ReviewRequestScreen` (confirmación inmediata tras
  enviar)
- **Estado:** ⚠️ **pendiente** — falta una pantalla dedicada para
  volver a consultar el estado de una solicitud ya enviada en otro
  momento (ver `state/pendientes.md`). Hoy solo se ve justo al enviar.

## Pagar cuenta (US-10)

### `GET /api/v1/credit-profiles/{id}/payment-due?installmentIds=`
- **Consumido por:** `PaymentViewModel` → `PayScreen` (desglose antes
  de confirmar)

### `POST /api/v1/credit-profiles/{id}/payments`
- **Request:** `{ installmentIds: [...] }` — **sin monto**, lo calcula
  el backend
- **Consumido por:** `PaymentViewModel` → `PayScreen` (confirmar)

## Estados de cuenta

### `GET /api/v1/credit-profiles/{id}/account-statements`
- **Consumido por:** `StatementsScreen`

## Ayuda financiera (US-15)

- Contenido estático en `HelpScreen.kt` — **no consume ningún
  endpoint.**

## Reglas transversales

- Todo 422 se muestra tal cual el mensaje del backend.
- Ningún endpoint de pago acepta un monto calculado en la app.
- El token JWT se inyecta automáticamente vía `AuthInterceptor` — los
  ViewModels nunca arman el header `Authorization` a mano.
