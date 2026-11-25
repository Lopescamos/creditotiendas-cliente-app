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
