package edu.eci.dosw.oficioya.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.oficioya.dto.PerfilAdmin;
import edu.eci.dosw.oficioya.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Protegido también por URL en SecurityConfig (/api/v1/admin/**)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios")
    public List<PerfilAdmin> listar() {
        return usuarioService.listarParaAdmin();
    }
}
