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

## View (dejado por el agente view)

### `view/EligibilityScreen.kt` (pantalla 1, UF-04 paso 1)
- Muestra las 4 condiciones como lista con check/x según
  `eligibility.value.requirements`.
- Botón "Continuar" habilitado **solo si las 4 se cumplen** —
  sombrea y deshabilita si alguna falla.
- Mensaje contextual cuando falta la condición 2 (primera compra).

### `view/RequestCatalogScreen.kt` (pantalla 2, UF-04 paso 2)
- Carga productos activos del catálogo de la tienda (filtro
  `active=true` ya aplicado en el VM).
- Cada producto: foto, nombre, precio al crédito, botón "Agregar".
- Mini-carrito flotante con total; botón "Revisar" navega a
  `ReviewRequestScreen`.

### `view/ReviewRequestScreen.kt` (pantalla 3, UF-04 paso 3 + UF-05 parcial)
- Resumen del carrito con subtotales y total.
- Botón "Enviar solicitud" → `submit` del VM.
- Tras éxito: muestra la solicitud en `PENDIENTE` con número
  trazable; tras rechazo por 422, muestra el mensaje del backend y
  mantiene el carrito para que el cliente pueda corregir.
- **US-09 parcial:** vista inmediata del estado — la pantalla
  dedicada para volver a consultar más tarde sigue pendiente (ver
  `state/pendientes.md` ítem 2).

## Tests (dejado por el agente tests)

### `app/src/test/.../viewmodel/PurchaseRequestViewModelTest.kt`

Cubre:
- Estado inicial vacío (eligibility=null, catálogo vacío, carrito
  vacío, total=0, loading=false).
- **Regla de negocio no trivial — las 4 condiciones de
  elegibilidad:** un test por cada condición que, al fallar,
  bloquea `eligible=false`:
  - Todas cumplidas → `eligible=true`.
  - Sin primera compra pagada a tiempo → bloquea.
  - Con cuotas vencidas impagas → bloquea.
  - Fuera del límite de crédito disponible → bloquea.
  (Perfil activo = false → implícito; mismo patrón, no agregamos un
  test adicional idéntico.)
- `loadCatalog` filtra productos con `active=false`.
- `submit` exitoso → carrito limpio, `lastCreated` con estado
  `PENDING`.
- `submit` con 422 → mensaje "No cumples los criterios de
  elegibilidad.", **carrito conservado** (el cliente puede corregir
  sin volver a armarlo).
- `submit` con excepción de red → error mostrable, no loading
  infinito.

**No cubierto:**
- Test de UI Compose de `EligibilityScreen` que verifique el botón
  "Continuar" deshabilitado cuando alguna condición falla. La regla
  queda cubierta por el VM (que es donde decide `eligible`), pero
  habría cerrado el bucle por el lado de la UI también. Pendiente si
  el tiempo sobra.
- Tests de `addToCart`/`updateQty`/`clearCart` como unidades puras —
  se ejercen indirectamente en el test de `submit` exitoso. No se
  agregan tests dedicados para mantener el set compacto.
