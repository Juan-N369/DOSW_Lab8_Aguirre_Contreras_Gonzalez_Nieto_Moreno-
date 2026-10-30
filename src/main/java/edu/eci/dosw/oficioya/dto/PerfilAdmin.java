package edu.eci.dosw.oficioya.dto;

import edu.eci.dosw.oficioya.entity.Rol;

// Incluye los datos de contacto: solo lo ve el ADMIN
public record PerfilAdmin(
        Long id,
        String nombre,
        String correo,
        String telefono,
        Rol rol,
        String oficioPrincipal,
        String zona) {}
