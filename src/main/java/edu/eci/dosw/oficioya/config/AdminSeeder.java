package edu.eci.dosw.oficioya.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

@Configuration
@Profile("dev")   // nunca en pruebas ni en producción
public class AdminSeeder {

    @Bean
    CommandLineRunner crearAdmin(UsuarioRepository repo, PasswordEncoder encoder,
            @Value("${ADMIN_CORREO}") String correo,
            @Value("${ADMIN_PASSWORD}") String password) {
        return args -> {
            String normalizado = correo.trim().toLowerCase(Locale.ROOT);
            if (repo.existsByCorreo(normalizado)) {
                return;   // idempotente: no duplica al reiniciar
            }
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setCorreo(normalizado);
            admin.setTelefono("0000000000");   // la columna es obligatoria; el ADMIN no tiene teléfono real
            admin.setPasswordHash(encoder.encode(password));
            admin.setRol(Rol.ADMIN);
            repo.save(admin);
        };
    }
}
