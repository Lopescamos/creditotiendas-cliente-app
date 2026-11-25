# User Stories cubiertas por esta app (Cliente)

Las 6 user stories de `CLIENT` — las que el frontend web **excluye**
(ahí solo cubre `SYSTEM_ADMIN`/`STORE_ADMIN`). Autocontenido: no
depende de abrir el repo del backend para saber qué significa cada US.

## Resumen

| US | Historia | Pantalla | Estado |
|---|---|---|---|
| US-03 | Consultar mi cuenta | `MyAccountScreen` | ✅ |
| US-07 | Consultar plan de cuotas | `InstallmentsScreen` | ✅ |
| US-08 | Solicitar compra a crédito | `EligibilityScreen`, `RequestCatalogScreen`, `ReviewRequestScreen` | ✅ |
| US-09 | Consultar estado de solicitud | `ReviewRequestScreen` | ⚠️ parcial (ver nota) |
| US-10 | Pagar cuenta | `PayScreen` | ✅ |
| US-15 | Ayuda financiera | `HelpScreen` | ✅ |

---

### US-03 — Consultar mi cuenta
**Pantalla:** `MyAccountScreen`

- **Pre-condición:** cliente autenticado con perfil activo en al menos
  una tienda.
- **Flujo:** saldo pendiente, próximo pago (monto, fecha, estado),
  movimientos recientes.
- **Criterios de aceptación:**
  - Si el cliente tiene perfil en más de una tienda, cada relación se
    muestra de forma independiente (no se mezclan saldos de distintas
    tiendas en un solo número).

### US-07 — Consultar plan de cuotas
**Pantalla:** `InstallmentsScreen`

- **Pre-condición:** cliente con al menos una venta a crédito en
  cuotas.
- **Flujo:** cronograma detallado por cuota — capital, interés, cuota
  fija, estado; ayuda contextual sobre el método francés.
- **Criterios de aceptación:**
  - Los valores mostrados son el snapshot de la venta — no se
    recalculan si las condiciones del perfil cambiaron después.

### US-08 — Solicitar compra a crédito
**Pantallas:** `EligibilityScreen` → `RequestCatalogScreen` → `ReviewRequestScreen`

- **Pre-condición (elegibilidad, las 4 obligatorias):**
  1. Perfil crediticio activo en la tienda.
  2. Al menos una compra previa pagada a tiempo.
  3. Sin cuotas vencidas impagas.
  4. Monto solicitado dentro del límite de crédito disponible.
- **Flujo:** pre-chequeo de elegibilidad → si cumple, selecciona
  productos del catálogo → envía solicitud.
- **Criterios de aceptación:**
  - Si no cumple elegibilidad, la opción de solicitar no se muestra
    habilitada — pero la validación real la hace el backend (422), la
    app no debe confiar solo en ocultar el botón.
  - La solicitud queda en `PENDIENTE` hasta que el Admin Tienda la
    revise (eso pasa en el frontend web, fuera de esta app).

### US-09 — Consultar estado de solicitud
**Pantalla:** `ReviewRequestScreen`

- **Pre-condición:** cliente con al menos una solicitud enviada.
- **Flujo:** ver si fue aprobada o rechazada (con motivo si aplica); si
  aprobada, ver la venta generada y su cronograma.
- **Criterios de aceptación:**
  - El enlace entre solicitud y venta generada es trazable.
- **Nota de estado real:** hoy solo se ve la confirmación **en el
  momento de enviar** la solicitud. No hay una pantalla a la que volver
  más tarde para revisar el estado de una solicitud ya enviada — es el
  pendiente anotado en `state/pendientes.md`.

### US-10 — Pagar cuenta
**Pantalla:** `PayScreen`

- **Pre-condición:** cliente con monto exigible pendiente.
- **Flujo:** ver total exigible con desglose (capital, interés
  ordinario, mora), confirmar pago íntegro.
- **Criterios de aceptación:**
  - Sin pagos parciales — no existe campo de monto editable.
  - Prelación de imputación (mora → interés → capital) se muestra tal
    cual la devuelve el backend.

### US-15 — Ayuda financiera
**Pantalla:** `HelpScreen`

- **Pre-condición:** ninguna (accesible a cualquier cliente
  autenticado).
- **Flujo:** explicaciones de tasa, método francés, gracia total, fecha
  de corte/pago, mora.
- **Criterios de aceptación:**
  - Contenido estático — no consume ningún endpoint, no requiere
    manejo de loading/error.
