# Estado — installments

Cubre US-07 (consultar plan de cuotas, UF-03). Los valores son el
snapshot de la venta — no se recalculan con cambios posteriores del
perfil.

## Model (dejado por el agente model)

Cubierto en `feature/project-setup`:
- `GET /api/v1/credit-sales/{id}/installments` → lista de
  `InstallmentResponse`.
- `GET /api/v1/credit-sales/{id}/payment-plan` → `PaymentPlanResponse`
  (snapshot).

## ViewModel (dejado por el agente viewmodel)

**N/A — no hay VM propio.** La pantalla reutiliza
`AccountViewModel.loadInstallments(saleId, context)` y expone
`accountVm.installments: StateFlow<List<InstallmentResponse>>`.

Rationale: el cronograma de cuotas es contexto de un perfil/venta y
pertenece conceptualmente al mismo VM que agrupa perfiles, ventas y
pagos del cliente. Duplicar el cliente Retrofit en un
`InstallmentsViewModel` separado no aportaría nada.

## View (dejado por el agente view)

### `view/InstallmentsScreen.kt`
- Recibe `accountVm: AccountViewModel` y `saleId: Long`.
- `LaunchedEffect(saleId)` dispara `accountVm.loadInstallments(saleId,
  context)` al entrar.
- Lista de cuotas con capital, interés, cuota fija y estado
  (`CtBadge`).
- Botón "¿Qué es el método francés?" → navega a `HelpScreen`
  (UF-07).

## Tests (dejado por el agente tests)

### `app/src/test/.../viewmodel/InstallmentsLoadingTest.kt`
Como la pantalla no tiene VM propio, los tests ejercitan el flujo de
`AccountViewModel.loadInstallments(saleId, context)` — la ruta de red
que alimenta a `InstallmentsScreen`. Se separa en un archivo propio
con scope `installments` para que la cobertura quede trazable al
feature.

Cubre:
- Respuesta 200 con snapshot de 3 cuotas (una `PAID_ON_TIME`, una
  `PENDING`, una `OVERDUE`) → `installments.value` idéntico al
  snapshot, `loading=false`.
- Error HTTP 404 → `error = "No pudimos cargar las cuotas."`,
  `loading=false`.
- Excepción de red → `error` mostrable, `loading=false`.

**No cubierto:**
- Test de UI Compose de `InstallmentsScreen`. La cobertura de VM
  cubre la lógica de red; el render del cronograma y la navegación a
  ayuda son thin UI. Se podría agregar con `createComposeRule` si el
  tiempo sobra.
