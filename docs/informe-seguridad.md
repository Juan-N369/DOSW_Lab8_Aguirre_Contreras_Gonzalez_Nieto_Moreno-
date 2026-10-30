# Informe de seguridad · OficioYa

## 1. Codificar, cifrar y hashear

- **Codificar:** el JWT lleva header y payload en Base64URL. Es solo un formato, no protege nada: cualquiera puede leerlo.
- **Cifrar:** no lo usamos sobre datos del usuario. La confidencialidad se logra ocultando correo y teléfono en los DTO públicos.
- **Hashear:** la contraseña se guarda en `usuarios.password_hash` con BCrypt (`PasswordEncoder`), nunca en texto plano.

La contraseña se hashea y no se cifra porque el hash es de una sola vía: si se filtra la base de datos no se puede recuperar la contraseña original. Con cifrado, quien robe la llave obtiene todas las contraseñas. BCrypt además agrega una sal aleatoria y es lento a propósito, lo que dificulta los ataques de fuerza bruta.

## 2. Payload del token y alteración del rol

El payload decodificado en jwt.io contiene `sub` (correo), `role`, `iss` (oficioya), `iat` y `exp` (30 minutos). No lleva contraseña, teléfono ni datos sensibles, porque cualquiera puede leerlo.

Al cambiar `role` a `ADMIN` y reenviar el token, la API respondió **401** 

<img width="953" height="1024" alt="Captura de pantalla 2026-10-29 225344" src="https://github.com/user-attachments/assets/964aaaf1-0f8f-4dbb-ae71-2be693d13e4f" />

. El servidor lo detectó porque la firma HS256 se calcula sobre header y payload con la clave secreta (`JWT_SECRET`). Al modificar el payload, la firma original ya no coincide, y sin la clave el atacante no puede generar una válida. `JwtService.validar` lanza una `JwtException` y el filtro deja la petición sin autenticar, de modo que el punto de entrada responde 401. La prueba PT-08 cubre este caso.

## 3. Ejemplos de 401, 403 y 404

- **401:** `GET /api/v1/solicitudes` sin token. No sabemos quién es el usuario (PT-07).
- **403:** un contratante llama a `GET /api/v1/admin/usuarios`. Sabemos quién es, pero su rol no alcanza
- <img width="956" height="1030" alt="Captura de pantalla 2026-10-29 225453" src="https://github.com/user-attachments/assets/f5acda87-5fba-4019-a43c-3a9b07fb3110" />

- **404:** con el token de Luis se pide una solicitud de Ana
- <img width="954" height="1020" alt="ataque3" src="https://github.com/user-attachments/assets/4b2a59b4-ed87-4a00-ac64-36079a8e67c2" />


Una solicitud ajena responde 404 y no 403 porque un 403 confirmaría que esa solicitud existe y revelaría información a quien no debería tenerla. Con 404, una solicitud ajena y una inexistente son indistinguibles, ya que la consulta incluye al participante autenticado.

## 4. ¿Por qué el contratante no envía su propio id?

`SolicitudRequest` solo trae `trabajadorId` y `descripcion`. El contratante sale del token: `SolicitudController.crear` pasa `auth.getName()` (el `sub` del JWT, que es el correo) y `SolicitudService` lo busca con `findByCorreo`. Si el id viniera en el cuerpo, cualquier usuario podría crear solicitudes a nombre de otro con solo cambiar ese número (A01 · control de acceso roto). El token está firmado por el servidor, así que la identidad no se puede falsificar; el cuerpo de la petición sí. La misma regla se aplica en `/usuarios/me`, que tampoco recibe un id.

## 5. CSRF y cierre de sesión

Desactivamos CSRF en `SecurityConfig` porque la API es stateless (`SessionCreationPolicy.STATELESS`) y no usa cookies de sesión: el JWT viaja en el encabezado `Authorization: Bearer`, que el navegador no adjunta solo en una petición entre sitios. El ataque CSRF depende justamente de que el navegador envíe credenciales de forma automática, así que aquí no aplica. Si el token se guardara en una cookie, habría que volver a activarlo.

La limitación: no hay revocación. El servidor no guarda tokens, así que "cerrar sesión" solo significa que el cliente borra el suyo. Un token robado o el de un usuario que cerró sesión sigue siendo válido hasta su `exp` (30 minutos). Mitigaciones: tokens de vida corta con refresh token, o una lista de tokens revocados (por `jti`) consultada en el filtro.

## 6. Clave del JWT expuesta o corta

Si `JWT_SECRET` se sube a GitHub, cualquiera puede firmar tokens válidos con `"role": "ADMIN"` y el servidor los aceptaría, porque la firma HS256 coincidiría: se pierde toda la autenticación y la autorización. Borrar el commit no basta (queda en el historial y en clones): hay que rotar la clave, lo que invalida todos los tokens emitidos. Si la clave es corta, se puede obtener por fuerza bruta a partir de un token capturado; por eso exigimos al menos 32 bytes (`Keys.hmacShaKeyFor` rechaza claves menores) y la generamos con `openssl rand -base64 48`.

Para producción cambiaríamos: el secreto en un gestor (Azure Key Vault o similar) inyectado como variable de entorno, con rotación periódica; HTTPS obligatorio; refresh tokens y revocación; límite de intentos en `/auth/login`; `spring.jpa.show-sql=false` y revisión de logs; quitar el `AdminSeeder` y crear administradores por un proceso controlado; y considerar RS256 si varios servicios validan el token, para que solo el emisor tenga la clave privada.
