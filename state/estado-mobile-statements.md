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
