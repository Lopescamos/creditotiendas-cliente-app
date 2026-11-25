# Estado — navigation

Grafo de navegación completo, único punto donde las features se
conectan entre sí. Se cierra al final porque necesita que todas las
pantallas ya existan en `develop`.

## Model (dejado por el agente model)

N/A — Navigation Compose no requiere DTOs.

## ViewModel (dejado por el agente viewmodel)

N/A — el wiring usa `viewModel()` de `lifecycle-viewmodel-compose`
para instanciar `AuthViewModel`, `AccountViewModel`,
`PaymentViewModel` y `PurchaseRequestViewModel` en el scope del
`NavHost`.

## View (dejado por el agente view)

### `view/nav/Navigate.kt`
- `Routes` — `object` con constantes `UPPER_SNAKE_CASE`: `LOGIN`,
  `MY_ACCOUNT`, `INSTALLMENTS`, `PAY`, `ELIGIBILITY`,
  `REQUEST_CATALOG`, `REVIEW_REQUEST`, `STATEMENTS`, `HELP`.
- `AppNavigation()` — `NavHost` con `startDestination = Routes.LOGIN`.
- Cada composable recibe los ViewModels compartidos (scope del nav)
  y los callbacks de navegación (`onBack`, `onContinue`, etc.)
  resueltos aquí.
- Rutas con parámetros usan `Routes.INSTALLMENTS + "/{saleId}"` y
  `navArgument`.

### `MainActivity.kt`
- Monta `CreditotiendasclienteappTheme { Surface { AppNavigation() } }`.
- Se retiró el `Greeting` demo del template.

## Tests (dejado por el agente tests)

**Sin tests dedicados.** Rationale:
- Navigation Compose se verifica con `createComposeRule` + un host
  de navegación de prueba — es significativamente más pesado que un
  test de VM.
- Cada pantalla ya tiene su VM probado; el grafo es una capa de
  conexión muy delgada.
- Un test útil aquí sería "el destino inicial es `LOGIN`" y
  "`signedIn=true` lleva a `MY_ACCOUNT`", pero ambos casos son thin
  wiring sobre `startDestination` y una condición booleana. Se deja
  anotado como posible mejora si la app crece.

## Observaciones

- Al cerrar esta rama queda cubierto el wiring completo — la app
  arranca, navega entre las 7 features y respeta UF-01 a UF-07.
- Pendientes del backlog (`state/pendientes.md`) siguen tal cual:
  push, pantalla dedicada US-09, retry de red, soporte offline.
