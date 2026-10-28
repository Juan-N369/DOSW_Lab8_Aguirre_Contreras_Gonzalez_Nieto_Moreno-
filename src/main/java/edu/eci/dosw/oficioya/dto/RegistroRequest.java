package edu.eci.dosw.oficioya.dto;

import jakarta.validation.constraints.*;
import edu.eci.dosw.oficioya.entity.Rol;

public record RegistroRequest(
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        @NotBlank String telefono,
        @NotBlank @Size(min = 8) String password,
        @NotNull Rol rol,
        String oficioPrincipal,
        String zona) {

        }