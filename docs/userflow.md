# User Flow — App Mobile (Cliente)

Flujos identificados con ID (`UF-XX`), para referenciarlos desde
`state/estado-mobile-<feature>.md` sin describirlos de nuevo.

## UF-01 — Login

- **US relacionada:** (previa a todas)
- **Entrada:** `LoginScreen`
- **Pasos:**
  1. Cliente ingresa correo + contraseña → `POST .../sign-in`.
  2. Si el rol devuelto no es `CLIENT` → se rechaza con mensaje
     explícito ("esta app es solo para clientes").
  3. Si OK → token se guarda en `SharedPreferences`, navega a
     `MyAccountScreen`.

## UF-02 — Mi cuenta (home)

- **US relacionada:** US-03
- **Entrada:** `MyAccountScreen` (landing tras login)
- **Pasos:**
  1. Ver saldo pendiente, próximo pago, movimientos recientes.
  2. Si tiene perfil en más de una tienda, cada una se ve por separado.
- **Alimenta a:** UF-03 (ver cuotas), UF-06 (pagar).

## UF-03 — Consultar plan de cuotas

- **US relacionada:** US-07
- **Entrada:** desde `MyAccountScreen`, selecciona una venta
- **Pasos:**
  1. Ver cronograma completo por cuota (capital, interés, estado).
  2. Ver ayuda contextual sobre el método francés (link a UF-07).

## UF-04 — Solicitar compra a crédito

- **US relacionada:** US-08
- **Entrada:** `EligibilityScreen`
- **Pasos:**
  1. Revisa las 4 condiciones de elegibilidad.
  2. Si cumple → `RequestCatalogScreen`: arma carrito del catálogo de
     su tienda.
  3. Envía → `ReviewRequestScreen`: confirmación inmediata.
- **Reglas:** no avanza del paso 1 si no cumple elegibilidad.

## UF-05 — Consultar estado de solicitud

- **US relacionada:** US-09
- **Entrada:** hoy solo accesible justo después de UF-04 (parte de
  `ReviewRequestScreen`)
- **Estado:** ⚠️ sin pantalla propia para revisar más tarde — ver
  `state/pendientes.md`.

## UF-06 — Pagar cuenta

- **US relacionada:** US-10
- **Entrada:** desde `MyAccountScreen` o `InstallmentsScreen`
- **Pasos:**
  1. Ver desglose exigible (capital + interés + mora).
  2. Confirmar pago íntegro (sin monto editable).

## UF-07 — Ayuda financiera

- **US relacionada:** US-15
- **Entrada:** `HelpScreen` (acceso libre, sin pre-condición)
- **Pasos:** contenido estático, sin llamadas a red.
