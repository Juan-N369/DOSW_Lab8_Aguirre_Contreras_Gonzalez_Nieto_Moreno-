package edu.eci.dosw.oficioya.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import edu.eci.dosw.oficioya.entity.*;
import edu.eci.dosw.oficioya.repository.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolicitudControllerTest {

    private static final String ANA = "ana@example.com";
    private static final String LUIS = "luis@example.com";
    private static final String PEDRO = "pedro@example.com";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    SolicitudRepository solicitudRepository;

    private Long solicitudDeAna;
    private Long solicitudDeLuis;

    @BeforeEach
    void preparar() {
        solicitudRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario ana = crearUsuario("Ana", ANA, Rol.CONTRATANTE);
        Usuario luis = crearUsuario("Luis", LUIS, Rol.CONTRATANTE);
        Usuario pedro = crearUsuario("Pedro", PEDRO, Rol.TRABAJADOR);

        solicitudDeAna = crearSolicitud(ana, pedro, "Gotera en el baño");
        crearSolicitud(ana, pedro, "Cambiar la llave del lavamanos");
        solicitudDeLuis = crearSolicitud(luis, pedro, "Arreglar el calentador");
    }

    @Test
    @WithMockUser(username = ANA, roles = "CONTRATANTE")
    @DisplayName("PT-14: el contratante solo lista sus propias solicitudes")
    void contratanteListaSoloLasPropias() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].contratanteNombre").value("Ana"))
                .andExpect(jsonPath("$[1].contratanteNombre").value("Ana"))
                .andExpect(jsonPath("$[0].correo").doesNotExist());
    }

    @Test
    @WithMockUser(username = LUIS, roles = "CONTRATANTE")
    @DisplayName("PT-15: consultar una solicitud ajena responde 404")
    void consultarSolicitudAjena() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes/" + solicitudDeAna))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada"));
    }

    @Test
    @WithMockUser(username = LUIS, roles = "CONTRATANTE")
    @DisplayName("PT-15: cancelar una solicitud ajena responde 404 y no cambia la base de datos")
    void cancelarSolicitudAjena() throws Exception {
        mockMvc.perform(patch("/api/v1/solicitudes/" + solicitudDeAna + "/cancelar"))
                .andExpect(status().isNotFound());

        assertEquals(EstadoSolicitud.PENDIENTE,
                solicitudRepository.findById(solicitudDeAna).orElseThrow().getEstado());
    }

    @Test
    @WithMockUser(username = LUIS, roles = "CONTRATANTE")
    @DisplayName("PT-15: una solicitud ajena y una inexistente responden el mismo 404")
    void ajenaEInexistenteIguales() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada"));
        mockMvc.perform(get("/api/v1/solicitudes/" + solicitudDeAna))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada"));
        // la propia sí la ve
        mockMvc.perform(get("/api/v1/solicitudes/" + solicitudDeLuis))
                .andExpect(status().isOk());
    }

    private Usuario crearUsuario(String nombre, String correo, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setTelefono("3001234567");
        u.setPasswordHash("$2a$10$hashDePruebaNoUsadoParaLogin000000000000000000000000");
        u.setRol(rol);
        return usuarioRepository.save(u);
    }

    private Long crearSolicitud(Usuario contratante, Usuario trabajador, String descripcion) {
        Solicitud s = new Solicitud();
        s.setDescripcion(descripcion);
        s.setEstado(EstadoSolicitud.PENDIENTE);
        s.setContratante(contratante);
        s.setTrabajador(trabajador);
        return solicitudRepository.save(s).getId();
    }
}
