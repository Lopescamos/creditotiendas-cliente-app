# Estado — auth

Cubre el flujo previo a toda US (UF-01, login) y la verificación de
rol `CLIENT`.

## Model (dejado por el agente model)

Cubierto completamente en `feature/project-setup`:
- `model/beans/Auth.kt` — `SignInRequest`, `SignInResponse`,
  `MeResponse`.
- `model/response/WebService.kt` — `POST .../sign-in`, `GET .../me`.
- `model/client/AuthInterceptor.kt` — inyecta `Authorization: Bearer`
  automáticamente.

## ViewModel (dejado por el agente viewmodel)

### `viewmodel/AuthViewModel.kt`
- Expone 4 `StateFlow` de solo lectura: `loading`, `error`,
  `signedIn`, `user`.
- `signIn(email, password, context)` — llama al backend, verifica que
  `body.role == "CLIENT"`, guarda `token / id / email / fullName` en
  `SharedPreferences` (`ct_prefs`).
- Si el rol no es `CLIENT`, rechaza con:
  "Esta app es solo para clientes. Usa el panel web si eres
  administrador."
- Mapeo de errores HTTP: 401/403 → "Correo o contraseña incorrectos",
  otros → genérico con código. Excepciones de red → "Sin conexión con
  el servidor."
- `hydrateFromStorage(context)` — reflota `signedIn` si ya hay token.
- `signOut(context)` — limpia `SharedPreferences`.
- Constructor expone un seam `webService: WebService? = null` para
  inyectar un fake en tests. Si no se pasa nada, usa
  `RetrofitClient.create(context)` — el flujo normal no cambia.

## View (dejado por el agente view)

### `view/LoginScreen.kt`
- Campos de email y contraseña usando `CtTextField` de
  `view/Components.kt`.
- Botón "Ingresar" deshabilitado durante `loading`.
- Muestra el `error` del ViewModel como banner rojo arriba del form.
- Al `signedIn=true`, navega a la ruta de cuenta (wiring real
  llegará con `feature/navigation`).
