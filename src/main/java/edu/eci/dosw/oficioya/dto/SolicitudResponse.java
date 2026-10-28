package edu.eci.dosw.oficioya.dto;

import edu.eci.dosw.oficioya.entity.EstadoSolicitud;
import java.time.LocalDateTime;

// Sin correo ni teléfono de los participantes
public record SolicitudResponse(
        Long id,
        String descripcion,
        EstadoSolicitud estado,
        Long contratanteId,
        String contratanteNombre,
        Long trabajadorId,
        String trabajadorNombre,
        LocalDateTime fechaCreacion) {}
