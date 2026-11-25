# Estado — statements

Pantalla de estados de cuenta mensuales del cliente.

## Model (dejado por el agente model)

Cubierto en `feature/project-setup`:
- `GET /api/v1/credit-profiles/{id}/account-statements` →
  `List<AccountStatementResponse>`.

## ViewModel (dejado por el agente viewmodel)

**N/A — no hay VM propio.** La pantalla reutiliza
`AccountViewModel.loadStatements(profileId, context)` y expone
`accountVm.statements`.

## View (dejado por el agente view)

### `view/StatementsScreen.kt`
- Recibe `accountVm: AccountViewModel`.
- Para el perfil seleccionado, lista los estados por período
  (`YYYY-MM`): saldo anterior, compras, pagos, interés, mora, saldo
  final.
- Pensada como listado histórico, sin acciones.

## Tests (dejado por el agente tests)

### `app/src/test/.../viewmodel/StatementsLoadingTest.kt`
Como la pantalla no tiene VM propio, el test ejercita
`AccountViewModel.loadStatements` — la ruta de red que la alimenta.

Cubre:
- Respuesta 200 con 2 períodos → `statements.value` tiene los
  elementos esperados, `loading=false`.
- Excepción de red → `statements` permanece vacío sin bloquear
  loading (el patrón del VM silencia el error de pagos/estados para
  no interrumpir la pantalla principal).

**No cubierto:**
- Test de UI Compose de `StatementsScreen`. La pantalla es un
  listado thin sobre el `StateFlow`; el valor agregado de tests de
  UI aquí es bajo.
