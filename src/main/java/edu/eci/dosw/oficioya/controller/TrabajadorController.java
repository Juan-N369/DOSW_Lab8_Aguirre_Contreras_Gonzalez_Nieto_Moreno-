package edu.eci.dosw.oficioya.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.oficioya.dto.PerfilPublico;
import edu.eci.dosw.oficioya.service.TrabajadorService;

@RestController
@RequestMapping("/api/v1/trabajadores")
public class TrabajadorController {

    private final TrabajadorService trabajadorService;

    public TrabajadorController(TrabajadorService trabajadorService) {
        this.trabajadorService = trabajadorService;
    }

    @GetMapping
    public List<PerfilPublico> listar() {
        return trabajadorService.listar();
    }

    @GetMapping("/{id}")
    public PerfilPublico obtener(@PathVariable Long id) {
        return trabajadorService.obtener(id);
    }
}
