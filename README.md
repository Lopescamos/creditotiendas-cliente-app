# CreditoTiendas — App Cliente (Android nativa)

App móvil para **clientes finales** (rol `CLIENT`) del sistema CreditoTiendas (SI642 UPC 2026-20, Grupo 4). Replica la estructura y el estilo de `SpacePulse-AppMobile` sobre el backend Spring Boot 4 compartido.

## Stack

- Kotlin 2.0 + Jetpack Compose (BOM 2024.12)
- Material 3
- Retrofit 2.11 + Gson + OkHttp (con interceptor que inyecta JWT)
- Navigation Compose 2.8
- ViewModel + StateFlow (MVVM)

## Arquitectura

```
pe.edu.upc.creditotiendas.cliente/
├── CreditoTiendasApp.kt          Application
├── MainActivity.kt
├── model/
│   ├── beans/                    data classes (Auth, CreditProfile, CreditSale, Installment, Payment, Product, PurchaseRequest, AccountStatement)
│   ├── client/                   RetrofitClient + AuthInterceptor
│   └── response/                 WebService (interfaz Retrofit)
├── viewmodel/                    Auth, Account, Payment, PurchaseRequest
├── view/                         Pantallas Compose por US
│   └── nav/Navigate.kt           NavHost + rutas
└── ui/theme/                     Color, Type, Theme (misma paleta que el frontend web)
```

## Pantallas (US cubiertas)

| # | Pantalla | US |
|---|----------|----|
| 1 | Login                       | login para rol CLIENT, rechaza otros roles |
| 2 | Mi cuenta (home)            | **US-03** — saldo, próximo pago, movimientos, acciones rápidas |
| 3 | Plan de cuotas              | **US-07** — cronograma con estado real + ayuda método francés |
| 4 | Elegibilidad                | **US-08** pre-check — las 4 condiciones (perfil activo, 1ra compra pagada a tiempo, sin mora, dentro de límite) |
| 5 | Catálogo del comercio       | **US-08** — agrega productos activos al carrito |
| 6 | Revisar y enviar solicitud  | **US-08** submit + **US-09** confirmación |
| 7 | Pagar cuenta                | **US-10** — total exigible, desglose capital/interés/mora, confirmación |
| 8 | Estados de cuenta           | **US-06** — listado mensual del backend |
| 9 | Ayuda financiera            | **US-15** — contenido estático (tasa, método francés, gracia, corte/pago, mora, prelación, no parciales) |

## Reglas de negocio respetadas

- **Solo rol CLIENT** entra a la app. El ViewModel de login rechaza `SYSTEM_ADMIN` y `STORE_ADMIN` con mensaje explícito.
- **Elegibilidad en backend, no solo UI**: la pantalla llama `GET /api/v1/credit-profiles/{id}/purchase-request-eligibility` antes de dejar entrar al catálogo. El botón "Enviar" igual puede devolver 422 si algo cambió desde la verificación.
- **Pago sin monto**: la app solo envía `installmentIds`; el backend calcula el total y aplica prelación mora → interés → capital.
- **Sin pagos parciales**: se comunica explícitamente al usuario en un `InfoAlert` antes de confirmar.
- **Snapshot de tasas**: el plan de cuotas lee desde el snapshot de la venta, nunca desde las condiciones actuales del perfil (`GET /api/v1/credit-sales/{id}/installments`).
- **Multi-tenant**: si el cliente tiene perfil en más de un comercio, aparece un selector arriba del saldo.

## Endpoints consumidos

```
POST   /api/v1/authentication/sign-in                          (login)
GET    /api/v1/users/me                                        (perfil)
GET    /api/v1/credit-profiles/me                              (US-03 — perfiles del cliente)
GET    /api/v1/credit-profiles/{id}
GET    /api/v1/credit-profiles/{id}/credit-sales               (ventas por perfil)
GET    /api/v1/credit-sales/{id}
GET    /api/v1/credit-sales/{id}/payment-plan                  (US-07 — snapshot)
GET    /api/v1/credit-sales/{id}/installments                  (US-07 — real)
GET    /api/v1/credit-profiles/{id}/purchase-request-eligibility  (US-08 pre-check)
POST   /api/v1/credit-profiles/{id}/purchase-requests          (US-08 submit)
GET    /api/v1/credit-profiles/{id}/purchase-requests          (US-09)
GET    /api/v1/stores/{storeId}/products                       (catálogo activo)
GET    /api/v1/credit-profiles/{id}/payment-due?installmentIds=  (US-10 desglose)
POST   /api/v1/credit-profiles/{id}/payments                   (US-10 pagar)
GET    /api/v1/credit-profiles/{id}/payments                   (histórico)
GET    /api/v1/credit-profiles/{id}/account-statements         (US-06)
```

## Configuración

### Correr en el emulador Android

El backend se espera en `http://10.0.2.2:8080` (la IP de loopback del host, vista desde el emulador). Si corres en un dispositivo físico, cambia la constante `BASE_URL` en `RetrofitClient.kt` a la IP de tu PC en la LAN (ej. `http://192.168.1.50:8080/`).

Si usas la IP de LAN, además agrega la configuración `network_security_config` para permitir tráfico cleartext a esa IP, o habilita HTTPS en el backend.

### Requisitos
- Android Studio Ladybug o más reciente
- JDK 17
- minSdk 26 (Android 8.0+), targetSdk 35

### Compilar

```bash
./gradlew :app:assembleDebug
```

o abre el proyecto en Android Studio y pulsa Run.

## Decisiones

- **Sin Room**: el estado vive en los `StateFlow` de los ViewModels. No hay cache local más allá del token en `SharedPreferences` (clave `ct_prefs`).
- **Sin Hilt/DI**: cada ViewModel instancia su `WebService` vía `RetrofitClient.create(context)` — la app es pequeña y mantener el patrón de SpacePulse.
- **Sin dark mode**: el Figma solo tiene modo claro. El tema ignora `isSystemInDarkTheme()`.
- **Sesión persiste**: el token JWT y los datos básicos del usuario quedan en `SharedPreferences`; al abrir la app re-entra directo si hay sesión.

## Lo que queda abierto (próximos pasos)

- **Notificaciones push** para avisos de vencimiento y resultado de solicitud (FCM).
- **Pantalla separada para estado de solicitud** (US-09) — hoy la respuesta se ve en la pantalla de confirmación; falta una vista propia con todas las solicitudes del cliente.
- **Botón "Reintentar"** en pantallas con error de red, hoy solo se muestra el mensaje.
- **Soporte offline mínimo** con Room para ver la última cuenta/estado sin conexión.
