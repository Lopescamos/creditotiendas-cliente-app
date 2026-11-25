# Estado — project-setup

Scaffold compartido por todas las features del cliente. No cubre
ninguna US directamente; es base técnica.

## Model (dejado por el agente model)

### DTOs (`model/beans/`)
Calcados 1:1 de `docs/contrato-api.md`, uno o varios por archivo:

- `Auth.kt` — `SignInRequest`, `SignInResponse`, `MeResponse`.
- `CreditProfile.kt` — `CreditProfileResponse`.
- `CreditSale.kt` — `CreditSaleResponse`, `PaymentPlanResponse`.
- `Installment.kt` — `InstallmentResponse` + enum/estado.
- `Payment.kt` — `PaymentDueResponse`, `PaymentResponse`,
  `RegisterPaymentRequest`.
- `Product.kt` — `ProductResponse`.
- `PurchaseRequest.kt` — `PurchaseRequestEligibilityResponse`,
  `CreatePurchaseRequest`, `SaleLineItem`, `PurchaseRequestResponse`.
- `AccountStatement.kt` — `AccountStatementResponse`.

### Cliente de red (`model/client/`)
- `RetrofitClient.kt` — singleton, `BASE_URL = http://10.0.2.2:8080/`
  (loopback del emulador). Expone `create(context): WebService` y las
  claves de `SharedPreferences` (`ct_prefs`, `auth_token`, `user_id`,
  `user_email`, `user_name`).
- `AuthInterceptor.kt` — inyecta `Authorization: Bearer <token>` en
  cada request leyendo el token del `SharedPreferences`.

### Contrato Retrofit (`model/response/WebService.kt`)
Interfaz **completa** con todos los endpoints que la app consume
(decisión explícita: no se hace crecer por feature, igual que
`app.routes.ts` en el web). 16 métodos cubren IAM, Credit Management,
Store Management y Billing.

## View (dejado por el agente view — parcial, solo lo compartido)

### Componentes compartidos (`view/Components.kt`)
- `CtCard`, `CtBadge`, `CtPrimaryButton`, `CtSecondaryButton`,
  `CtTextField`, `CtSectionTitle`, `BottomTabBar`, `TopAppBarCt`.
- Se decidió mover `Components.kt` de `feature/navigation` a
  `feature/project-setup` para que las ramas intermedias
  (`feature/auth`, `feature/account`, …) compilen al introducir sus
  pantallas antes de que `feature/navigation` exista.

### Tema (`ui/theme/`)
- `Color.kt`, `Theme.kt`, `Type.kt` afinados — paleta y tipografía del
  producto ya definidas, no requieren más retoques por feature.

### Application class
- `CreditoTiendasApp.kt` — `android.app.Application` con singleton
  `instance`. Declarada en `AndroidManifest.xml` (`android:name`).

## Pendiente en la rama

Nada — todos los commits de scaffold entran en esta rama. Lo demás
(ViewModels, pantallas, Navigate, MainActivity wiring) corresponde a
sus features respectivas.
