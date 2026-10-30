package edu.eci.dosw.oficioya.mapper;

import org.springframework.stereotype.Component;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.dto.PerfilAdmin;
import edu.eci.dosw.oficioya.dto.PerfilPropio;
import edu.eci.dosw.oficioya.dto.UsuarioResponse;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getRol());
    }

    public PerfilPropio toPerfilPropio(Usuario u) {
        return new PerfilPropio(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(),
                u.getRol(), u.getOficioPrincipal(), u.getZona());
    }

    public PerfilAdmin toPerfilAdmin(Usuario u) {
        return new PerfilAdmin(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(),
                u.getRol(), u.getOficioPrincipal(), u.getZona());
    }
}
