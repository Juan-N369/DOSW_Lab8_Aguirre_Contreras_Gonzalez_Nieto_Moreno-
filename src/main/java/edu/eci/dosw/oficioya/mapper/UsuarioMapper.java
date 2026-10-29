package edu.eci.dosw.oficioya.mapper;

import org.springframework.stereotype.Component;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.dto.UsuarioResponse;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getRol());
    }
}
