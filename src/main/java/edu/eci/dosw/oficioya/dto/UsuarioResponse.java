package edu.eci.dosw.oficioya.dto;

import edu.eci.dosw.oficioya.entity.Rol;

public record UsuarioResponse(
        Long id, 
        String nombre, 
        String correo, 
        Rol rol) {}
