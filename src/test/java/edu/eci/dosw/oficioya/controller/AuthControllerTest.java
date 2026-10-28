package edu.eci.dosw.oficioya.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;

import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static String registroJson(String correo, String password, String rol) {
        return """
                {"nombre":"Pedro Gomez","correo":"%s","telefono":"3001234567","password":"%s","rol":"%s"}
                """.formatted(correo, password, rol);
    }

    private ResultActions registrar(String correo, String password, String rol) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registroJson(correo, password, rol)));
    }

    @Test
    @DisplayName("PT-01: registro válido responde 201 sin contraseña ni hash")
    void registroValido() throws Exception {
        registrar("pedro@example.com", "ContrasenaDemo123!", "TRABAJADOR")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("pedro@example.com"))
                .andExpect(jsonPath("$.rol").value("TRABAJADOR"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("PT-02: correo repetido responde 409")
    void correoRepetido() throws Exception {
        registrar("pedro@example.com", "ContrasenaDemo123!", "TRABAJADOR")
                .andExpect(status().isCreated());
        registrar("PEDRO@example.com", "ContrasenaDemo123!", "CONTRATANTE")
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PT-03: rol ADMIN responde 400")
    void rolAdminRechazado() throws Exception {
        registrar("admin@example.com", "ContrasenaDemo123!", "ADMIN")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Rol no permitido en el registro"));
    }

    @Test
    @DisplayName("PT-03: contraseña corta responde 400")
    void contrasenaCorta() throws Exception {
        registrar("ana@example.com", "corta", "CONTRATANTE")
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PT-04: la contraseña se guarda como hash BCrypt")
    void contrasenaHasheada() throws Exception {
        String clave = "ContrasenaDemo123!";
        registrar("pedro@example.com", clave, "TRABAJADOR")
                .andExpect(status().isCreated());

        Usuario u = usuarioRepository.findByCorreo("pedro@example.com").orElseThrow();

        assertNotEquals(clave, u.getPasswordHash());
        assertTrue(u.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches(clave, u.getPasswordHash()));
    }

    private ResultActions login(String correo, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"correo":"%s","password":"%s"}
                        """.formatted(correo, password)));
    }

    @Test
    @DisplayName("PT-05: login correcto responde 200 con token")
    void loginCorrecto() throws Exception {
        registrar("pedro@example.com", "ContrasenaDemo123!", "TRABAJADOR")
                .andExpect(status().isCreated());

        login("pedro@example.com", "ContrasenaDemo123!")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiraEn").value(1800));
    }

    @Test
    @DisplayName("PT-06: clave errónea y correo inexistente responden 401 con el mismo mensaje")
    void loginFallido() throws Exception {
        registrar("pedro@example.com", "ContrasenaDemo123!", "TRABAJADOR")
                .andExpect(status().isCreated());

        login("pedro@example.com", "ClaveIncorrecta99")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));

        login("nadie@example.com", "ContrasenaDemo123!")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));
    }
}


