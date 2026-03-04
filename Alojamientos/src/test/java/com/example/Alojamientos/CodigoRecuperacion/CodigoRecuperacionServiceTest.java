package com.example.Alojamientos.CodigoRecuperacion;

import com.example.Alojamientos.businessLayer.dto.CodigoRecuperacionDTO;
import com.example.Alojamientos.businessLayer.service.CodigoRecuperacionService;
import com.example.Alojamientos.persistenceLayer.entity.CodigoRecuperacionEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.CodigoRecuperacionDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.CodigoRecuperacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CodigoRecuperacionServiceTest {

    @Mock
    private CodigoRecuperacionRepository codigoRepository;

    @Mock
    private CodigoRecuperacionDataMapper codigoMapper;

    @InjectMocks
    private CodigoRecuperacionService codigoService;

    // DTO de respuesta reutilizable
    private CodigoRecuperacionDTO dtoRespuesta;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        dtoRespuesta = CodigoRecuperacionDTO.builder()
                .userId(1)
                .code("123456")
                .expirationDate("2025-10-10T10:00:00")
                .used(false)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    // generarCodigo
    // ─────────────────────────────────────────────────────────────────

    // 1️⃣ Generar código - crea nuevo correctamente
    @Test
    void generarCodigo_creaNuevoCorrectamente() {
        when(codigoRepository.findByUsuario_IdAndUsadoFalse(1))
                .thenReturn(Collections.emptyList());
        when(codigoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(codigoMapper.toDTO(any())).thenReturn(dtoRespuesta);

        CodigoRecuperacionDTO result = codigoService.generarCodigo(1);

        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertFalse(result.isUsed());
        verify(codigoRepository).save(any());
    }

    // 2️⃣ Generar código - invalida los anteriores
    @Test
    void generarCodigo_invalidaCodigosPrevios() {
        // La entidad tiene boolean primitivo → setUsado/isUsado
        CodigoRecuperacionEntity previo = new CodigoRecuperacionEntity();
        previo.setUsado(false);

        when(codigoRepository.findByUsuario_IdAndUsadoFalse(1))
                .thenReturn(List.of(previo));
        when(codigoRepository.saveAll(anyList())).thenReturn(List.of(previo));
        when(codigoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(codigoMapper.toDTO(any())).thenReturn(dtoRespuesta);

        codigoService.generarCodigo(1);

        // Después de generarCodigo los previos deben estar marcados como usados
        assertTrue(previo.isUsado());
        verify(codigoRepository).saveAll(anyList());
    }

    // ─────────────────────────────────────────────────────────────────
    // validarCodigo
    // ─────────────────────────────────────────────────────────────────

    // 3️⃣ Válido y no expirado
    @Test
    void validarCodigo_validoYNoExpirado() {
        CodigoRecuperacionEntity entity = new CodigoRecuperacionEntity();
        entity.setCodigo("123456");
        entity.setFechaExpiracion(Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));
        entity.setUsado(false);

        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse("123456", 1))
                .thenReturn(Optional.of(entity));

        assertTrue(codigoService.validarCodigo("123456", 1));
    }

    // 4️⃣ Expirado → devuelve false y lo marca como usado
    @Test
    void validarCodigo_expiradoDevuelveFalse() {
        CodigoRecuperacionEntity entity = new CodigoRecuperacionEntity();
        entity.setCodigo("999999");
        entity.setFechaExpiracion(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)));
        entity.setUsado(false);

        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse("999999", 1))
                .thenReturn(Optional.of(entity));

        boolean result = codigoService.validarCodigo("999999", 1);

        assertFalse(result);
        assertTrue(entity.isUsado());
        verify(codigoRepository).save(entity);
    }

    // 5️⃣ No existe → devuelve false
    @Test
    void validarCodigo_noExisteDevuelveFalse() {
        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(any(), any()))
                .thenReturn(Optional.empty());

        assertFalse(codigoService.validarCodigo("000000", 1));
    }

    // 6️⃣ Parámetros nulos → devuelve false
    @Test
    void validarCodigo_parametrosNulos() {
        assertFalse(codigoService.validarCodigo(null, 1));
        assertFalse(codigoService.validarCodigo("123456", null));
    }

    // 7️⃣ Ya usado → devuelve false (repository no lo encuentra por usadoFalse)
    @Test
    void validarCodigo_yaUsadoDevuelveFalse() {
        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(any(), any()))
                .thenReturn(Optional.empty());

        assertFalse(codigoService.validarCodigo("123456", 1));
    }

    // ─────────────────────────────────────────────────────────────────
    // marcarComoUsado
    // ─────────────────────────────────────────────────────────────────

    // 8️⃣ Correcto
    @Test
    void marcarComoUsado_exitoso() {
        CodigoRecuperacionEntity entity = new CodigoRecuperacionEntity();
        entity.setCodigo("111111");
        entity.setFechaExpiracion(Timestamp.valueOf(LocalDateTime.now().plusMinutes(5)));
        entity.setUsado(false);

        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse("111111", 1))
                .thenReturn(Optional.of(entity));

        codigoService.marcarComoUsado("111111", 1);

        assertTrue(entity.isUsado());
        verify(codigoRepository).save(entity);
    }

    // 9️⃣ Expirado → lanza excepción
    @Test
    void marcarComoUsado_expiradoLanzaExcepcion() {
        CodigoRecuperacionEntity entity = new CodigoRecuperacionEntity();
        entity.setCodigo("000001");
        entity.setFechaExpiracion(Timestamp.valueOf(LocalDateTime.now().minusMinutes(2)));
        entity.setUsado(false);

        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(any(), any()))
                .thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class,
                () -> codigoService.marcarComoUsado("000001", 1));
    }

    // 🔟 No encontrado → lanza excepción
    @Test
    void marcarComoUsado_noEncontradoLanzaExcepcion() {
        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> codigoService.marcarComoUsado("000001", 1));
    }

    // ─────────────────────────────────────────────────────────────────
    // obtenerPorId  — recibe Long
    // ─────────────────────────────────────────────────────────────────

    // 11️⃣ Exitoso
    @Test
    void obtenerPorId_exitoso() {
        CodigoRecuperacionEntity entity = new CodigoRecuperacionEntity();
        entity.setId(1L);
        entity.setCodigo("888888");

        when(codigoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(codigoMapper.toDTO(entity)).thenReturn(
                CodigoRecuperacionDTO.builder()
                        .userId(1).code("888888")
                        .expirationDate("2025-10-10T10:00:00").used(false)
                        .build());

        CodigoRecuperacionDTO dto = codigoService.obtenerPorId(1L);

        assertEquals("888888", dto.getCode());
    }

    // 12️⃣ No encontrado → lanza excepción
    @Test
    void obtenerPorId_noEncontrado() {
        when(codigoRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> codigoService.obtenerPorId(10L));
    }

    // ─────────────────────────────────────────────────────────────────
    // limpiarCodigosExpirados
    // ─────────────────────────────────────────────────────────────────

    // 13️⃣ Elimina correctamente
    @Test
    void limpiarCodigosExpirados_eliminaExpirados() {
        CodigoRecuperacionEntity expirado = new CodigoRecuperacionEntity();
        when(codigoRepository.findByFechaExpiracionBeforeAndUsadoFalse(any()))
                .thenReturn(List.of(expirado));

        codigoService.limpiarCodigosExpirados();

        verify(codigoRepository).deleteAll(anyList());
    }

    // 14️⃣ Lista vacía → no llama deleteAll
    @Test
    void limpiarCodigosExpirados_sinExpirados_noEliminaNada() {
        when(codigoRepository.findByFechaExpiracionBeforeAndUsadoFalse(any()))
                .thenReturn(Collections.emptyList());

        codigoService.limpiarCodigosExpirados();

        verify(codigoRepository).deleteAll(Collections.emptyList());
    }

    // ─────────────────────────────────────────────────────────────────
    // generarCodigo — longitud del código
    // ─────────────────────────────────────────────────────────────────

    // 15️⃣ Código tiene longitud 6
    @Test
    void generarCodigo_codigoTieneLongitud6() {
        when(codigoRepository.findByUsuario_IdAndUsadoFalse(any()))
                .thenReturn(Collections.emptyList());
        when(codigoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(codigoMapper.toDTO(any())).thenReturn(dtoRespuesta);

        CodigoRecuperacionDTO dto = codigoService.generarCodigo(1);

        assertEquals(6, dto.getCode().length());
    }
}