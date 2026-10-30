package edu.eci.dosw.oficioya.controller;

import edu.eci.dosw.oficioya.dto.*;
import edu.eci.dosw.oficioya.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    // La identidad sale siempre del contexto de seguridad (auth.getName()), nunca del cuerpo ni de la URL

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@RequestBody @Valid SolicitudRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crear(req, auth.getName()));
    }

    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")   // ADMIN: 403
    @GetMapping
    public List<SolicitudResponse> listar(Authentication auth) {
        return solicitudService.listar(auth.getName());
    }

    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping("/{id}")
    public SolicitudResponse obtener(@PathVariable Long id, Authentication auth) {
        return solicitudService.obtener(id, auth.getName());
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/aceptar")
    public SolicitudResponse aceptar(@PathVariable Long id, Authentication auth) {
        return solicitudService.aceptar(id, auth.getName());
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/rechazar")
    public SolicitudResponse rechazar(@PathVariable Long id, Authentication auth) {
        return solicitudService.rechazar(id, auth.getName());
    }

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PatchMapping("/{id}/cancelar")
    public SolicitudResponse cancelar(@PathVariable Long id, Authentication auth) {
        return solicitudService.cancelar(id, auth.getName());
    }
}
