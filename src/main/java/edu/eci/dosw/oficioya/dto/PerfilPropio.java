package edu.eci.dosw.oficioya.dto;

import edu.eci.dosw.oficioya.entity.Rol;

// Datos del usuario autenticado, incluido su contacto (GET /usuarios/me)
public record PerfilPropio(
        Long id,
        String nombre,
        String correo,
        String telefono,
        Rol rol,
        String oficioPrincipal,
        String zona) {}
