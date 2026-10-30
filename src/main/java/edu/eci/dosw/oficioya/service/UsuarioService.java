package edu.eci.dosw.oficioya.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.dto.PerfilAdmin;
import edu.eci.dosw.oficioya.dto.PerfilPropio;
import edu.eci.dosw.oficioya.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.mapper.UsuarioMapper;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper mapper) {
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
    }

    // El correo llega del sub del token (auth.getName()), nunca de la petición
    public PerfilPropio me(String correoAutenticado) {
        return usuarioRepository.findByCorreo(correoAutenticado)
                .map(mapper::toPerfilPropio)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    public List<PerfilAdmin> listarParaAdmin() {
        return usuarioRepository.findAll().stream()
                .map(mapper::toPerfilAdmin)
                .toList();
    }
}
