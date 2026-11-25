# Pendientes — App Mobile (Cliente)

Backlog real, tomado del propio README del proyecto. No incluye nada
del frontend web (otro repo).

## Backlog

### 1. Notificaciones push
- **Prioridad:** media
- **Por qué falta:** hoy el cliente no se entera de un pago próximo a
  vencer ni de que su solicitud fue aprobada/rechazada salvo que abra
  la app.
- **Depende de:** nada del backend documentado aún — habría que definir
  el proveedor (Firebase Cloud Messaging) y si el backend expone un
  endpoint para registrar el token del dispositivo.

### 2. Pantalla dedicada para US-09 (estado de solicitud)
- **Prioridad:** alta
- **Por qué falta:** hoy el estado de una solicitud solo se ve en el
  momento de enviarla (`ReviewRequestScreen`); no hay forma de volver
  después a consultar si ya fue aprobada/rechazada.
- **Depende de:** nada nuevo del backend —
  `GET /api/v1/credit-profiles/{id}/purchase-requests` ya existe y
  devuelve el estado.

### 3. Botones de reintento ante fallos de red
- **Prioridad:** media
- **Por qué falta:** si una llamada falla (sin internet, timeout), hoy
  no hay una forma estándar de reintentar sin salir y volver a entrar
  a la pantalla.

### 4. Soporte offline (Room)
- **Prioridad:** baja
- **Por qué falta:** la app no funciona sin conexión — ni para ver
  datos ya consultados antes. Agregar una capa de cache local con Room
  permitiría al menos lectura offline de lo último sincronizado.

## Completado

Las 6 user stories de `CLIENT` están implementadas — ver
`docs/user-stories.md` para el detalle (US-09 queda marcada como
parcial, ver pendiente #2).
