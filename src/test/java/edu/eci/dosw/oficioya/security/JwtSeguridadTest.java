package edu.eci.dosw.oficioya.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtSeguridadTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JwtService jwtService;
    @Value("${app.jwt.secret}")
    String secret;

    @Test
    @DisplayName("PT-07: sin token responde 401 con formato ApiError")
    void sinToken() throws Exception {
        mockMvc.perform(get("/api/v1/trabajadores"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.ruta").value("/api/v1/trabajadores"));
    }

    @Test
    @DisplayName("PT-08: token alterado responde 401")
    void tokenAlterado() throws Exception {
        String token = jwtService.generar("ana@example.com", "CONTRATANTE");
        mockMvc.perform(get("/api/v1/trabajadores").header("Authorization", "Bearer " + alterar(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PT-09: token vencido responde 401")
    void tokenVencido() throws Exception {
        String vencido = new JwtService(secret, "oficioya", -1).generar("ana@example.com", "CONTRATANTE");
        mockMvc.perform(get("/api/v1/trabajadores").header("Authorization", "Bearer " + vencido))
                .andExpect(status().isUnauthorized());
    }

    private String alterar(String token) {
        int i = token.length() - 10;
        char c = token.charAt(i) == 'A' ? 'B' : 'A';
        return token.substring(0, i) + c + token.substring(i + 1);
    }
}
