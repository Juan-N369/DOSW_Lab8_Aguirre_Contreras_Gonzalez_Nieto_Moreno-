# Informe de seguridad · OficioYa

## 1. Codificar, cifrar y hashear

- **Codificar:** el JWT lleva header y payload en Base64URL. Es solo un formato, no protege nada: cualquiera puede leerlo.
- **Cifrar:** no lo usamos sobre datos del usuario. La confidencialidad se logra ocultando correo y teléfono en los DTO públicos.
- **Hashear:** la contraseña se guarda en `usuarios.password_hash` con BCrypt (`PasswordEncoder`), nunca en texto plano.

La contraseña se hashea y no se cifra porque el hash es de una sola vía: si se filtra la base de datos no se puede recuperar la contraseña original. Con cifrado, quien robe la llave obtiene todas las contraseñas. BCrypt además agrega una sal aleatoria y es lento a propósito, lo que dificulta los ataques de fuerza bruta.

## 2. Payload del token y alteración del rol

El payload decodificado en jwt.io contiene `sub` (correo), `role`, `iss` (oficioya), `iat` y `exp` (30 minutos). No lleva contraseña, teléfono ni datos sensibles, porque cualquiera puede leerlo.

Al cambiar `role` a `ADMIN` y reenviar el token, la API respondió **401** [PENDIENTE: confirmar con el ataque 2 y adjuntar evidencia]. El servidor lo detectó porque la firma HS256 se calcula sobre header y payload con la clave secreta (`JWT_SECRET`). Al modificar el payload, la firma original ya no coincide, y sin la clave el atacante no puede generar una válida. `JwtService.validar` lanza una `JwtException` y el filtro deja la petición sin autenticar, de modo que el punto de entrada responde 401. La prueba PT-08 cubre este caso.

## 3. Ejemplos de 401, 403 y 404

- **401:** `GET /api/v1/solicitudes` sin token. No sabemos quién es el usuario (PT-07).
- **403:** un contratante llama a `GET /api/v1/admin/usuarios`. Sabemos quién es, pero su rol no alcanza [PENDIENTE: evidencia ataque 5].
- **404:** con el token de Luis se pide una solicitud de Ana [PENDIENTE: evidencia ataque 4; prueba PT-15].

Una solicitud ajena responde 404 y no 403 porque un 403 confirmaría que esa solicitud existe y revelaría información a quien no debería tenerla. Con 404, una solicitud ajena y una inexistente son indistinguibles, ya que la consulta incluye al participante autenticado.
