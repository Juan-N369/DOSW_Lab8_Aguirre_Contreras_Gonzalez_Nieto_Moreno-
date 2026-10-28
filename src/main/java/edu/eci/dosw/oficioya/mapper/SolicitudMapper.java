package edu.eci.dosw.oficioya.mapper;

import org.springframework.stereotype.Component;
import edu.eci.dosw.oficioya.entity.Solicitud;
import edu.eci.dosw.oficioya.dto.SolicitudResponse;

@Component
public class SolicitudMapper {

    public SolicitudResponse toResponse(Solicitud s) {
        return new SolicitudResponse(
                s.getId(),
                s.getDescripcion(),
                s.getEstado(),
                s.getContratante().getId(),
                s.getContratante().getNombre(),
                s.getTrabajador().getId(),
                s.getTrabajador().getNombre(),
                s.getFechaCreacion());
    }
}
