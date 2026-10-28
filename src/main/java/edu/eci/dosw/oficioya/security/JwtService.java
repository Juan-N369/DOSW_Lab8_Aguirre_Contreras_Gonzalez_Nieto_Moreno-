package edu.eci.dosw.oficioya.security;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    // TEMPORAL: la implementación real la entrega B
    public String generar(String correo, String rol) {
        return "token-temporal";
    }
}