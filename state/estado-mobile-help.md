# Estado — help

Cubre US-15 (ayuda financiera, UF-07). **Contenido estático — no
consume ningún endpoint ni requiere loading/error.**

## Model (dejado por el agente model)

N/A — no hay red.

## ViewModel (dejado por el agente viewmodel)

N/A — no hay estado reactivo, solo texto estático.

## View (dejado por el agente view)

### `view/HelpScreen.kt`
- Explicaciones de: tasa de interés (nominal vs efectiva), método
  francés, gracia total, fecha de corte / fecha de pago, mora.
- Accesible desde el bottom tab o desde `InstallmentsScreen` (link
  "¿Qué es el método francés?").

## Tests (dejado por el agente tests)

**Sin tests automatizados.** Rationale documentado:
- No hay ViewModel ni red → no hay flujo observable que se pueda
  romper con un cambio en código (no hay `StateFlow`, no hay
  mapeo).
- Un test de UI Compose instrumentado para verificar que "existe el
  texto 'método francés'" exige setup `androidTest` (device /
  emulador) desproporcionado para una pantalla que es puro string
  literal — si el string cambia a propósito, el test se rompería
  sin ganancia real.
- El contenido es revisable visualmente desde Android Studio preview
  en segundos.

Si en el futuro la pantalla pasa a consumir contenido del backend
(p. ej. markdown servido), hay que agregar tests de carga aquí.
