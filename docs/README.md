# Evidencias de los ataques manuales · Bloque 8

Capturas tomadas con Postman y [jwt.io](https://jwt.io) contra la aplicación corriendo con el perfil `dev` y conectada a PostgreSQL en Docker. Los comandos `curl` y las respuestas completas de los siete ataques están en [`ataques.md`](ataques.md).

Usuarios de prueba: **Ana** (contratante A, dueña de la solicitud 7), **Luis** (contratante B) y **Pedro** (trabajador).

| Evidencia | Ataque | Esperado | Obtenido |
| --- | --- | --- | --- |
| [Payload del token](#payload-del-token-en-jwtio) | — | Sin datos sensibles | ✅ |
| [Sin token](#ataque-1--pedir-solicitudes-sin-token--401) | 1 | `401` | `401` ✅ |
| [Token alterado](#ataque-2--token-con-role-cambiado-a-admin--401) | 2 | `401` | `401` ✅ |
| [Solicitud ajena](#ataque-4--luis-pide-la-solicitud-de-ana--404) | 4 | `404` | `404` ✅ |
| [Rol insuficiente](#ataque-5--contratante-llama-a-adminusuarios--403) | 5 | `403` | `403` ✅ |

## Payload del token en jwt.io

![Payload del token de Luis decodificado en jwt.io](<evidencias/acces Token Luis.png>)

Token de Luis decodificado en jwt.io. El header indica `HS256` y el payload solo contiene `sub` (correo), `role` (`CONTRATANTE`), `iss` (`oficioya`), `iat` y `exp` (30 minutos después). No lleva contraseña, teléfono ni otros datos sensibles, porque cualquiera puede leer el payload: está codificado en Base64URL, no cifrado.

## Ataque 1 · Pedir `/solicitudes` sin token · `401`

![GET /solicitudes sin token responde 401](<evidencias/Captura de pantalla 2026-10-29 225344.png>)

`GET /api/v1/solicitudes` con *No Auth*. La API no sabe quién hace la petición y responde `401 Unauthorized` con el formato `ApiError`. Es también el ejemplo de `401` que pide la sección 10.

## Ataque 2 · Token con `role` cambiado a `ADMIN` · `401`

![Token alterado en jwt.io y respuesta 401 en Postman](<evidencias/token invalido.png>)

A la izquierda, en jwt.io, se cambió el `role` del payload de Luis a `ADMIN` y se firmó con una clave inventada (`clave-falsa-del-atacante-1234567890`), porque el atacante no conoce el `JWT_SECRET`. A la derecha, ese token se envía a `GET /api/v1/admin/usuarios` y la API responde `401` "Token inválido o expirado": la firma HS256 no coincide con la del servidor, así que el token se rechaza antes de mirar el rol.

## Ataque 4 · Luis pide la solicitud de Ana · `404`

![GET /solicitudes/7 con el token de Luis responde 404](<evidencias/ataque3.png>)

`GET /api/v1/solicitudes/7` con el token de Luis (`{{tokenLuis}}`). La solicitud 7 existe, pero es de Ana, así que la API responde `404` "Solicitud no encontrada". Se responde `404` y no `403` para no confirmar que la solicitud existe: para Luis, una solicitud ajena y una inexistente son indistinguibles. Es también el ejemplo de `404` de la sección 10.

## Ataque 5 · Contratante llama a `/admin/usuarios` · `403`

![GET /admin/usuarios con el token de Luis responde 403](<evidencias/Captura de pantalla 2026-10-29 225453.png>)

`GET /api/v1/admin/usuarios` con el token de Luis. El token es válido, así que la API sabe quién es, pero su rol `CONTRATANTE` no alcanza: responde `403 Forbidden` "No tiene permisos para acceder a este recurso". Es también el ejemplo de `403` de la sección 10.

## Ataques sin captura

Los ataques 3 (token vencido, `401`), 6 (registro con `"rol": "ADMIN"`, `400`) y 7 (login con correo inexistente y con clave errónea, mismo `401` y mismo mensaje) están documentados con sus comandos y respuestas en [`ataques.md`](ataques.md).
