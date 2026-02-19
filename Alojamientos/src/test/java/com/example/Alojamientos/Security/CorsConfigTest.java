package com.example.Alojamientos.Security;

import com.example.Alojamientos.config.CorsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para verificar la configuración CORS de AUTH-10.
 *
 * <p>Verifica que:
 * <ul>
 *   <li>Las requests preflight OPTIONS sean respondidas correctamente.</li>
 *   <li>El header Authorization sea permitido en requests al API.</li>
 *   <li>El header Authorization sea expuesto en las respuestas.</li>
 *   <li>Los orígenes permitidos sean aceptados correctamente.</li>
 *   <li>Los métodos HTTP necesarios estén habilitados.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:4200",
        "app.jwt.secret=dGVzdFNlY3JldEtleVBhcmFQcnVlYmFzRGVKV1RTZXJ2aWNlMTIz",
        "app.jwt.expiration=86400000"
})
@DisplayName("Pruebas de Configuración CORS para Autenticación")
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ORIGIN_PERMITIDO   = "http://localhost:4200";
    private static final String ORIGIN_NO_PERMITIDO = "http://dominio-malicioso.com";

    // ─────────────────────────────────────────────────────────────────
    // TESTS DE PREFLIGHT (OPTIONS)
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("OPTIONS /api/auth/login - Preflight debe retornar 200 desde origen permitido")
    void preflight_origenPermitido_debeRetornar200() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ORIGIN_PERMITIDO)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OPTIONS /api/auth/login - Header Authorization debe estar permitido en preflight")
    void preflight_debePermitirHeaderAuthorization() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ORIGIN_PERMITIDO)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    @DisplayName("OPTIONS /api/auth/login - Header Content-Type debe estar permitido en preflight")
    void preflight_debePermitirHeaderContentType() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ORIGIN_PERMITIDO)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    // ─────────────────────────────────────────────────────────────────
    // TESTS DE MÉTODOS HTTP PERMITIDOS
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("OPTIONS /api/alojamientos - Método GET debe estar permitido")
    void preflight_debePermitirMetodoGET() throws Exception {
        MvcResult result = mockMvc.perform(options("/api/alojamientos")
                        .header("Origin", ORIGIN_PERMITIDO)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andReturn();

        String allowedMethods = result.getResponse()
                .getHeader("Access-Control-Allow-Methods");
        assertThat(allowedMethods).contains("GET");
    }

    @Test
    @DisplayName("OPTIONS /api/auth/login - Método POST debe estar permitido")
    void preflight_debePermitirMetodoPOST() throws Exception {
        MvcResult result = mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ORIGIN_PERMITIDO)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andReturn();

        String allowedMethods = result.getResponse()
                .getHeader("Access-Control-Allow-Methods");
        assertThat(allowedMethods).contains("POST");
    }

    // ─────────────────────────────────────────────────────────────────
    // TESTS DE ORIGEN
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("OPTIONS - Origen permitido debe recibir Access-Control-Allow-Origin")
    void preflight_origenPermitido_debeRecibirHeaderAllowOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ORIGIN_PERMITIDO)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN_PERMITIDO));
    }

    @Test
    @DisplayName("OPTIONS - Origen no permitido NO debe recibir Access-Control-Allow-Origin")
    void preflight_origenNoPermitido_noDebeRecibirHeaderAllowOrigin() throws Exception {
        MvcResult result = mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ORIGIN_NO_PERMITIDO)
                        .header("Access-Control-Request-Method", "POST"))
                .andReturn();

        String allowOrigin = result.getResponse()
                .getHeader("Access-Control-Allow-Origin");
        assertThat(allowOrigin).isNotEqualTo(ORIGIN_NO_PERMITIDO);
    }

    // ─────────────────────────────────────────────────────────────────
    // TEST DE EXPOSED HEADERS
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("OPTIONS - Header Authorization debe estar en Access-Control-Expose-Headers")
    void preflight_debeExponerHeaderAuthorization() throws Exception {
        MvcResult result = mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ORIGIN_PERMITIDO)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andReturn();

        String exposeHeaders = result.getResponse()
                .getHeader("Access-Control-Expose-Headers");
        assertThat(exposeHeaders).contains("Authorization");
    }
}