package edu.eci.dosw.oficioya;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
class PerfilesSeguridadTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UsuarioRepository usuarioRepository;

    private Long trabajadorId;

    @BeforeEach
    void preparar() {
        Usuario t = new Usuario();
        t.setNombre("Pedro Gómez");
        t.setCorreo("pedro@example.com");
        t.setTelefono("3001234567");
        t.setPasswordHash("hash-de-prueba");
        t.setRol(Rol.TRABAJADOR);
        t.setOficioPrincipal("Plomero");
        t.setZona("Chapinero");
        trabajadorId = usuarioRepository.save(t).getId();
    }

    @Test
    @WithMockUser(username = "ana@example.com", roles = "CONTRATANTE")
    @DisplayName("PT-10: el perfil público no expone correo ni teléfono")
    void perfilPublicoSinContacto() throws Exception {
        mockMvc.perform(get("/api/v1/trabajadores/" + trabajadorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pedro Gómez"))
                .andExpect(jsonPath("$.correo").doesNotExist())
                .andExpect(jsonPath("$.telefono").doesNotExist());
    }

    @Test
    @WithMockUser(username = "pedro@example.com", roles = "TRABAJADOR")
    @DisplayName("PT-13: un TRABAJADOR no puede crear solicitudes: 403")
    void trabajadorNoCreaSolicitudes() throws Exception {
        mockMvc.perform(post("/api/v1/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"trabajadorId\":" + trabajadorId
                        + ",\"descripcion\":\"Tengo una gotera en el baño\"}"))
                .andExpect(status().isForbidden());
    }
}
