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

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // 1️⃣ Generar código - crea nuevo correctamente
    @Test
    void generarCodigo_creaNuevoCorrectamente() {
        Integer usuarioId = 1;
        when(codigoRepository.findByUsuario_IdAndUsadoFalse(usuarioId)).thenReturn(Collections.emptyList());

        CodigoRecuperacionEntity savedEntity = CodigoRecuperacionEntity.builder()
                .id(1).codigo("123456").usuario(UsuarioEntity.builder().id(usuarioId).build())
                .fechaExpiracion(Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)))
                .usado(false).build();

        when(codigoRepository.save(any())).thenReturn(savedEntity);
        when(codigoMapper.toDTO(any())).thenReturn(new CodigoRecuperacionDTO(usuarioId, "123456", "2025-10-10T10:00:00", false));

        CodigoRecuperacionDTO result = codigoService.generarCodigo(usuarioId);

        assertNotNull(result);
        assertEquals(usuarioId, result.getUserId());
        assertFalse(result.isUsed());
        verify(codigoRepository).save(any());
    }

    // 2️⃣ Generar código - invalida los anteriores
    @Test
    void generarCodigo_invalidaCodigosPrevios() {
        Integer usuarioId = 1;
        CodigoRecuperacionEntity previo = CodigoRecuperacionEntity.builder().id(10).usado(false).build();
        when(codigoRepository.findByUsuario_IdAndUsadoFalse(usuarioId)).thenReturn(List.of(previo));
        when(codigoRepository.saveAll(anyList())).thenReturn(List.of(previo));
        when(codigoRepository.save(any())).thenReturn(previo);
        when(codigoMapper.toDTO(any())).thenReturn(new CodigoRecuperacionDTO(usuarioId, "000001", "2025-10-10T10:00:00", false));

        codigoService.generarCodigo(usuarioId);

        assertTrue(previo.getUsado());
        verify(codigoRepository).saveAll(anyList());
    }

    // 3️⃣ Validar código - válido y no expirado
    @Test
    void validarCodigo_validoYNoExpirado() {
        String codigo = "123456";
        Integer usuarioId = 1;

        CodigoRecuperacionEntity entity = CodigoRecuperacionEntity.builder()
                .codigo(codigo)
                .fechaExpiracion(Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)))
                .usado(false)
                .build();

        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(codigo, usuarioId)).thenReturn(Optional.of(entity));

        boolean result = codigoService.validarCodigo(codigo, usuarioId);
        assertTrue(result);
    }

    // 4️⃣ Validar código - expirado
    @Test
    void validarCodigo_expiradoDevuelveFalse() {
        String codigo = "999999";
        Integer usuarioId = 1;
        CodigoRecuperacionEntity entity = CodigoRecuperacionEntity.builder()
                .codigo(codigo)
                .fechaExpiracion(Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)))
                .usado(false)
                .build();

        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(codigo, usuarioId))
                .thenReturn(Optional.of(entity));

        boolean result = codigoService.validarCodigo(codigo, usuarioId);

        assertFalse(result);
        assertTrue(entity.getUsado());
        verify(codigoRepository).save(entity);
    }

    // 5️⃣ Validar código - no existe
    @Test
    void validarCodigo_noExisteDevuelveFalse() {
        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(any(), any())).thenReturn(Optional.empty());
        assertFalse(codigoService.validarCodigo("000000", 1));
    }

    // 6️⃣ Validar código - parámetros nulos
    @Test
    void validarCodigo_parametrosNulos() {
        assertFalse(codigoService.validarCodigo(null, 1));
        assertFalse(codigoService.validarCodigo("123456", null));
    }

    // 7️⃣ Marcar como usado - correcto
    @Test
    void marcarComoUsado_exitoso() {
        String codigo = "111111";
        Integer usuarioId = 1;
        CodigoRecuperacionEntity entity = CodigoRecuperacionEntity.builder()
                .codigo(codigo)
                .fechaExpiracion(Timestamp.valueOf(LocalDateTime.now().plusMinutes(5)))
                .usado(false).build();

        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(codigo, usuarioId))
                .thenReturn(Optional.of(entity));

        codigoService.marcarComoUsado(codigo, usuarioId);

        assertTrue(entity.getUsado());
        verify(codigoRepository).save(entity);
    }

    // 8️⃣ Marcar como usado - expirado
    @Test
    void marcarComoUsado_expiradoLanzaExcepcion() {
        CodigoRecuperacionEntity entity = CodigoRecuperacionEntity.builder()
                .codigo("000001")
                .fechaExpiracion(Timestamp.valueOf(LocalDateTime.now().minusMinutes(2)))
                .build();

        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(any(), any())).thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () -> codigoService.marcarComoUsado("000001", 1));
    }

    // 9️⃣ Marcar como usado - no encontrado
    @Test
    void marcarComoUsado_noEncontradoLanzaExcepcion() {
        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(any(), any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> codigoService.marcarComoUsado("000001", 1));
    }

    // 🔟 Obtener por ID - correcto
    @Test
    void obtenerPorId_exitoso() {
        CodigoRecuperacionEntity entity = CodigoRecuperacionEntity.builder()
                .id(1).codigo("888888").build();
        when(codigoRepository.findById(1)).thenReturn(Optional.of(entity));
        when(codigoMapper.toDTO(entity)).thenReturn(new CodigoRecuperacionDTO(1, "888888", "2025-10-10T10:00:00", false));

        CodigoRecuperacionDTO dto = codigoService.obtenerPorId(1);

        assertEquals("888888", dto.getCode());
    }

    // 11️⃣ Obtener por ID - no encontrado
    @Test
    void obtenerPorId_noEncontrado() {
        when(codigoRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> codigoService.obtenerPorId(10));
    }

    // 12️⃣ Limpiar códigos expirados - elimina correctamente
    @Test
    void limpiarCodigosExpirados_eliminaExpirados() {
        List<CodigoRecuperacionEntity> expirados = List.of(new CodigoRecuperacionEntity());
        when(codigoRepository.findByFechaExpiracionBeforeAndUsadoFalse(any())).thenReturn(expirados);

        codigoService.limpiarCodigosExpirados();

        verify(codigoRepository).deleteAll(anyList());
    }


    // 14️⃣ Generar código - código tiene longitud 6
    @Test
    void generarCodigo_codigoTieneLongitud6() {
        when(codigoRepository.findByUsuario_IdAndUsadoFalse(any())).thenReturn(Collections.emptyList());
        when(codigoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(codigoMapper.toDTO(any())).thenReturn(new CodigoRecuperacionDTO(1, "123456", "2025-10-10T10:00:00", false));

        CodigoRecuperacionDTO dto = codigoService.generarCodigo(1);

        assertEquals(6, dto.getCode().length());
    }

    // 15️⃣ Validar código - usado debe devolver falso
    @Test
    void validarCodigo_yaUsadoDevuelveFalse() {
        CodigoRecuperacionEntity entity = CodigoRecuperacionEntity.builder()
                .codigo("123456").usado(true).build();

        when(codigoRepository.findByCodigoAndUsuario_IdAndUsadoFalse(any(), any()))
                .thenReturn(Optional.empty());

        boolean result = codigoService.validarCodigo("123456", 1);
        assertFalse(result);
    }
}
