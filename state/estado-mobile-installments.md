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
