# Guía de arquitectura — Frontend Mobile (Cliente)

Única fuente de verdad de convenciones para la app Android
(Kotlin + Jetpack Compose). Los agentes de `.claude/agents/` la leen
antes de tocar código.

## 1. Stack y principios

- Kotlin + Jetpack Compose (Material 3), sin XML layouts.
- Retrofit 2 + Gson + OkHttp para la capa de red, con `AuthInterceptor`
  inyectando el JWT en cada request.
- Navigation Compose para el grafo de pantallas.
- MVVM con `StateFlow` como única forma de estado reactivo — nada de
  `LiveData` ni callbacks sueltos.
- `SharedPreferences` (`ct_prefs`) para persistir el token de sesión.
- **No es DDD por bounded context** como el frontend web. Es MVVM por
  feature/pantalla, organizado por capa técnica (no por dominio de
  negocio). No repliques la estructura `domain/infrastructure/
  application/presentation` acá, no aplica.

## 2. Estructura de carpetas

```
app/src/main/java/pe/edu/upc/creditotiendas/cliente/
  model/
    beans/              # DTOs (request/response), uno o varios por .kt
    client/              # RetrofitClient, AuthInterceptor
    response/
      WebService.kt      # interfaz Retrofit completa, un método por endpoint
  viewmodel/
    <Feature>ViewModel.kt
  view/
    nav/
      Navigate.kt         # Routes + AppNavigation (grafo completo)
    Components.kt         # composables compartidos (CtCard, CtBadge, etc.)
    <Pantalla>Screen.kt
  ui/theme/
    Color.kt, Type.kt, Theme.kt
```

## 3. Flujo para construir (o continuar) una feature

1. **Model** — agrega los DTOs que falten en `model/beans/` y el método
   nuevo en `model/response/WebService.kt`. Si la feature necesita algo
   especial de red (otro interceptor, otro timeout), va en
   `model/client/`.
2. **ViewModel** — un `StateFlow` por feature con el estado de
   loading/data/error, y los métodos que llaman a `WebService` vía
   `RetrofitClient`.
3. **View** — la(s) pantalla(s) Compose que observan el `StateFlow`
   (nunca llaman a Retrofit directo), más el wiring en
   `view/nav/Navigate.kt`.
4. **Tests** — tests del ViewModel (JUnit + coroutines-test) y, si
   aplica, un test de UI de Compose.
5. Al terminar cada capa, deja un resumen en
   `state/estado-mobile-<feature>.md`.

## 4. Model layer

- Un DTO por forma de request/response real del backend — calcado 1:1,
  revisa `docs/contrato-api.md` antes de inventar un campo.
- Un método por endpoint en `WebService.kt`, con la anotación Retrofit
  correcta (`@GET`, `@POST`, `@Query`, `@Path`, `@Body`).
- Nunca pongas lógica de negocio acá — solo forma de datos y contrato
  de red.

## 5. ViewModel layer

- Un `MutableStateFlow` privado + exposición como `StateFlow` de solo
  lectura (`.asStateFlow()`) — igual de estricto que el `.asReadonly()`
  del web.
- Las llamadas a red van en `viewModelScope.launch { ... }`, con manejo
  de error explícito (no dejar una excepción sin capturar que tumbe la
  pantalla).
- Reglas de negocio reflejadas en UI (elegibilidad, sin pagos
  parciales) se validan acá antes de llamar al endpoint, pero la
  fuente de verdad sigue siendo el backend.

## 6. View layer

- Composables standalone, sin estado propio salvo UI efímera (texto de
  un campo antes de enviarlo) — todo lo demás viene del ViewModel.
- Reutiliza los componentes de `Components.kt` (`CtCard`, `CtBadge`,
  `CtPrimaryButton`, `BottomTabBar`, etc.) antes de crear uno nuevo.
- Las rutas nuevas se agregan en `Routes` + `AppNavigation()` dentro de
  `Navigate.kt`.

## 7. Tabla de nomenclatura

| Elemento | Convención | Ejemplo |
|---|---|---|
| DTO | `PascalCase`, sustantivo | `PurchaseRequestResponse` |
| ViewModel | `<Feature>ViewModel` | `PaymentViewModel` |
| Pantalla | `<Nombre>Screen` | `EligibilityScreen` |
| Ruta | `UPPER_SNAKE_CASE` en `Routes` | `Routes.PAY` |
| Endpoint Retrofit | verbo + recurso | `fun registerPayment(...)` |

## 8. Checklist antes de dar una capa por terminada

- [ ] ¿El DTO calza 1:1 con lo que el backend expone?
- [ ] ¿El ViewModel expone solo `StateFlow` de solo lectura?
- [ ] ¿La pantalla reutiliza componentes de `Components.kt` antes de
      crear uno nuevo?
- [ ] ¿La ruta quedó agregada en `Navigate.kt`?
- [ ] ¿Quedó un resumen en `state/estado-mobile-<feature>.md`?
