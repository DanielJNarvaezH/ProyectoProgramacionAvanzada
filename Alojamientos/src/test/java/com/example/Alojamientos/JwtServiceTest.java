package com.example.Alojamientos;

import com.example.Alojamientos.securityLayer.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // Clave Base64 válida de 256 bits para pruebas
    private static final String TEST_SECRET =
        "dGVzdFNlY3JldEtleVBhcmFQcnVlYmFzRGVKV1RTZXJ2aWNlMTIz";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
    }

    @Test
    void generarToken_debeRetornarTokenNoNulo() {
        String token = jwtService.generarToken("juan@correo.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extraerEmail_debeRetornarEmailCorrecto() {
        String token = jwtService.generarToken("juan@correo.com");
        assertEquals("juan@correo.com", jwtService.extraerEmail(token));
    }

    @Test
    void generarTokenConRol_debeIncluirRolEnClaims() {
        String token = jwtService.generarTokenConRol("host@correo.com", "ANFITRION");
        assertEquals("ANFITRION", jwtService.extraerRol(token));
    }

    @Test
    void esTokenValido_debeRetornarTrueConEmailCorrecto() {
        String token = jwtService.generarToken("juan@correo.com");
        assertTrue(jwtService.esTokenValido(token, "juan@correo.com"));
    }

    @Test
    void esTokenValido_debeRetornarFalseConEmailIncorrecto() {
        String token = jwtService.generarToken("juan@correo.com");
        assertFalse(jwtService.esTokenValido(token, "otro@correo.com"));
    }

    @Test
    void estaExpirado_debeRetornarFalseParaTokenNuevo() {
        String token = jwtService.generarToken("juan@correo.com");
        assertFalse(jwtService.estaExpirado(token));
    }

    @Test
    void esTokenEstructuralmenteValido_debeRetornarFalseParaTokenInvalido() {
        assertFalse(jwtService.esTokenEstructuralmenteValido("esto.no.es.un.token"));
    }

    @Test
    void extraerClaims_conClaimsExtra_debeRetornarlosCorrectamente() {
        Map<String, Object> extras = Map.of("userId", 42);
        String token = jwtService.generarToken("juan@correo.com", extras);
        Integer userId = jwtService.extraerClaim(token,
            claims -> claims.get("userId", Integer.class));
        assertEquals(42, userId);
    }
}


