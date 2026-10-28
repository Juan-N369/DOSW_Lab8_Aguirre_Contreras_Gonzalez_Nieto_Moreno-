package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.*;
import edu.eci.dosw.oficioya.entity.*;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;
import edu.eci.dosw.oficioya.mapper.UsuarioMapper;
import edu.eci.dosw.oficioya.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<Rol> ROLES_PUBLICOS = EnumSet.of(Rol.TRABAJADOR, Rol.CONTRATANTE);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper mapper;

    public UsuarioResponse registrar(RegistroRequest req) {
        if (!ROLES_PUBLICOS.contains(req.rol())) {
            throw new RolNoPermitidoException("Rol no permitido en el registro");
        }
        String correo = req.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException(correo);
        }

        Usuario u = new Usuario();
        u.setNombre(req.nombre());
        u.setCorreo(correo);
        u.setTelefono(req.telefono());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRol(req.rol());
        if (req.rol() == Rol.TRABAJADOR) {
            u.setOficioPrincipal(req.oficioPrincipal());
            u.setZona(req.zona());
        }
        return mapper.toResponse(usuarioRepository.save(u));
    }
}
