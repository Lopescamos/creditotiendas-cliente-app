# Estado — purchase-request

Cubre US-08 (solicitar compra a crédito, UF-04) y US-09 parcial (ver
estado, UF-05, solo justo tras enviar; la pantalla dedicada sigue
pendiente — ver `state/pendientes.md` ítem 2).

## Regla no negociable

**Elegibilidad — 4 condiciones todas obligatorias** (`EligibilityScreen`
las muestra, `PurchaseRequestViewModel.eligibility` las agrega):
1. Perfil crediticio activo en la tienda.
2. Al menos una compra previa pagada a tiempo.
3. Sin cuotas vencidas impagas.
4. Monto solicitado dentro del límite de crédito disponible.

Fuente de verdad: backend (422 si no cumple). La app respeta las 4
pero no confía solo en ellas — el envío también maneja 422.

## Model (dejado por el agente model)

Cubierto en `feature/project-setup`:
- `GET /api/v1/credit-profiles/{id}/purchase-request-eligibility`.
- `GET /api/v1/stores/{storeId}/products`.
- `POST /api/v1/credit-profiles/{id}/purchase-requests` body
  `CreatePurchaseRequest(items: List<SaleLineItem>)`.
- `GET /api/v1/credit-profiles/{id}/purchase-requests`.

## ViewModel (dejado por el agente viewmodel)

### `viewmodel/PurchaseRequestViewModel.kt`
- `StateFlow`: `eligibility`, `catalog`, `cart`, `myRequests`,
  `lastCreated`, `loading`, `error`.
- `checkEligibility(profileId, context)` — pre-chequeo antes de
  mostrar el catálogo.
- `loadCatalog(storeId, context)` — solo productos `active=true`.
- `loadMyRequests(profileId, context)` — US-09 (listado).
- Gestión de carrito en memoria: `addToCart`, `updateQty`,
  `clearCart`.
- `submit(profileId, context)` — mapea `cart` a `SaleLineItem`s y
  envía. Si el backend retorna 422 (`"No cumples los criterios de
  elegibilidad."`), el error se expone al usuario sin perder el
  carrito.
- `total` derivado de `cart`.
- Constructor expone seam `webService: WebService? = null` para
  tests.
