package edu.eci.dosw.oficioya.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// El contratante no se envía: sale del token
public record SolicitudRequest(
        @NotNull Long trabajadorId,
        @NotBlank @Size(max = 500) String descripcion) {}
