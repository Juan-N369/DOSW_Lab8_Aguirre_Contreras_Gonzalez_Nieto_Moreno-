package edu.eci.dosw.oficioya.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import edu.eci.dosw.oficioya.entity.Rol;
import edu.eci.dosw.oficioya.entity.Usuario;
import edu.eci.dosw.oficioya.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerTest {

    private static final String SOFIA = "sofia@example.com";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UsuarioRepository usuarioRepository;

    @BeforeEach
    void preparar() {
        Usuario s = new Usuario();
        s.setNombre("Sofía Ruiz");
        s.setCorreo(SOFIA);
        s.setTelefono("3109876543");
        s.setPasswordHash("hash-de-prueba");
        s.setRol(Rol.CONTRATANTE);
        usuarioRepository.save(s);
    }

    @Test
    @WithMockUser(username = SOFIA, roles = "CONTRATANTE")
    @DisplayName("PT-11: un CONTRATANTE que consulta /admin/usuarios recibe 403")
    void contratanteNoAccedeAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.ruta").value("/api/v1/admin/usuarios"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("PT-12: el ADMIN consulta /admin/usuarios y recibe los datos de contacto")
    void adminVeContacto() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].correo", hasItem(SOFIA)))
                .andExpect(jsonPath("$[*].telefono", hasItem("3109876543")))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(username = SOFIA, roles = "CONTRATANTE")
    @DisplayName("/usuarios/me devuelve los datos propios, incluido el contacto")
    void meIncluyeContacto() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value(SOFIA))
                .andExpect(jsonPath("$.telefono").value("3109876543"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("/usuarios/me sin autenticación responde 401")
    void meSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }
}
