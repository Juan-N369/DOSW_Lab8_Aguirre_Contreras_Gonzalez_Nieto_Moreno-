package edu.eci.dosw.oficioya.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.dto.PerfilPublico;
import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

@Service
public class TrabajadorService {

    private final UsuarioRepository usuarioRepository;

    public TrabajadorService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<PerfilPublico> listar() {
        return usuarioRepository.findByRol(Rol.TRABAJADOR).stream()
                .map(this::aPerfil)
                .toList();
    }

    public PerfilPublico obtener(Long id) {
        return usuarioRepository.findByIdAndRol(id, Rol.TRABAJADOR)
                .map(this::aPerfil)
                .orElseThrow(() -> new RecursoNoEncontradoException("Trabajador no encontrado"));
    }

    private PerfilPublico aPerfil(Usuario u) {
        return new PerfilPublico(u.getId(), u.getNombre(), u.getOficioPrincipal(), u.getZona());
    }
}
