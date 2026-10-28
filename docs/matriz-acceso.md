# Matriz de acceso · Worker domain (Squad 3)

El Worker domain gestiona la dimensión profesional de quien ofrece servicios en OficioYa: catálogo de oficios, oficios principales y secundarios de cada trabajador, tarifas, áreas de cobertura y disponibilidad. Las rutas llevan el prefijo `/api/v1`.

En el proyecto una misma persona puede ser contratante y trabajador con una sola cuenta, así que el token puede traer varios roles. Las reglas de abajo exigen que **entre sus roles** esté el indicado.

| Endpoint | Roles permitidos | Regla de propiedad o datos ocultos | Código si se incumple |
| --- | --- | --- | --- |
| `GET /oficios` | `TRABAJADOR`, `CONTRATANTE`, `ADMIN` | Catálogo de solo lectura | `401` sin token |
| `POST /admin/oficios` | `ADMIN` | Solo el administrador agrega oficios al catálogo | `401` sin token; `403` otro rol; `409` si el oficio ya existe |
| `GET /trabajadores/{id}/perfil-profesional` | `TRABAJADOR`, `CONTRATANTE`, `ADMIN` | Perfil público: oficios, tarifas, zonas y disponibilidad, **sin correo ni teléfono** | `401` sin token; `404` si no existe |
| `PUT /trabajadores/me/oficios` | `TRABAJADOR` | Solo modifica sus propios oficios (el id sale del token); exactamente un oficio principal | `401` sin token; `403` otro rol; `400` datos inválidos |
| `PUT /trabajadores/me/tarifas` | `TRABAJADOR` | Solo sus propias tarifas, y solo para oficios que tiene asociados | `401` sin token; `403` otro rol; `400` tarifa inválida |
| `PUT /trabajadores/me/cobertura` | `TRABAJADOR` | Solo sus propias zonas de cobertura (barrios) | `401` sin token; `403` otro rol |
| `PATCH /trabajadores/me/disponible-ahora` | `TRABAJADOR` | Solo cambia su propia disponibilidad inmediata | `401` sin token; `403` otro rol |
| `PATCH /admin/trabajadores/{id}/estado` | `ADMIN` | Moderación: pausar o reactivar un perfil profesional | `401` sin token; `403` otro rol; `404` si no existe |

**Decisión de diseño:** las rutas de escritura usan `/me` en lugar de `/trabajadores/{id}`. Así el trabajador nunca indica a quién modifica: siempre es el usuario del token, y no existe una ruta para tocar el perfil de otro (control de acceso roto, A01).

## Emisión y validación del JWT en la arquitectura

El **Identity domain** (Squad 1) es el único que emite el JWT, después de verificar la contraseña con BCrypt. El **Orchestrator / API Gateway** valida el token (firma, expiración y emisor) en cada petición antes de enrutarla. Además, el microservicio de Worker vuelve a validarlo y aplica sus propias reglas de rol y propiedad, para no confiar solo en la red interna (defensa en profundidad). Todos usan la misma clave HS256, que nunca va en el repositorio: en local llega por la variable de entorno `JWT_SECRET` desde un `.env` ignorado por Git, y en la nube se guarda como secreto en Azure (por ejemplo, Key Vault) y se inyecta a cada contenedor como variable de entorno.
