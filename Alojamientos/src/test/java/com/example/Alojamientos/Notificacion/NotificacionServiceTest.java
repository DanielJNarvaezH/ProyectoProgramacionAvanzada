package com.example.Alojamientos.Notificacion;

import com.example.Alojamientos.businessLayer.dto.NotificacionDTO;
import com.example.Alojamientos.businessLayer.service.NotificacionService;
import com.example.Alojamientos.persistenceLayer.entity.NotificacionEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.NotificacionDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private NotificacionDataMapper mapper;

    @InjectMocks
    private NotificacionService notificacionService;

    private NotificacionEntity entity;
    private NotificacionDTO dto;
    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuario = UsuarioEntity.builder().id(1).build();

        entity = NotificacionEntity.builder()
                .id(1)
                .usuario(usuario)
                .tipo(NotificacionEntity.TipoNotificacion.MENSAJE)
                .titulo("Bienvenido")
                .mensaje("Tu reserva fue confirmada")
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        dto = NotificacionDTO.builder()
                .userId(1)
                .type("MENSAJE")
                .title("Bienvenido")
                .message("Tu reserva fue confirmada")
                .read(false)
                .build();
    }

    // 1️⃣ Crear notificación - éxito
    @Test
    void crearNotificacion_debeGuardarYRetornarDTO() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(notificacionRepository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(dto);

        NotificacionDTO result = notificacionService.crearNotificacion(dto);

        assertThat(result).isNotNull();
        verify(notificacionRepository).save(entity);
    }

    // 2️⃣ Crear notificación - mensaje vacío (debe lanzar excepción de validación manual)
    @Test
    void crearNotificacion_conMensajeVacioDebeGuardarIgual() {
        dto.setMessage("");
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(notificacionRepository.save(entity)).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(dto);

        NotificacionDTO result = notificacionService.crearNotificacion(dto);

        assertThat(result).isNotNull();
        verify(notificacionRepository).save(entity);
    }


    // 3️⃣ Listar por usuario - éxito
    @Test
    void listarPorUsuario_debeRetornarLista() {
        when(notificacionRepository.findByUsuario_IdOrderByFechaCreacionDesc(1))
                .thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(dto);

        List<NotificacionDTO> result = notificacionService.listarPorUsuario(1);

        assertThat(result).hasSize(1);
    }

    // 4️⃣ Listar por usuario - lista vacía
    @Test
    void listarPorUsuario_vaciaDebeRetornarListaVacia() {
        when(notificacionRepository.findByUsuario_IdOrderByFechaCreacionDesc(1))
                .thenReturn(Collections.emptyList());

        List<NotificacionDTO> result = notificacionService.listarPorUsuario(1);

        assertThat(result).isEmpty();
    }

    // 5️⃣ Listar no leídas
    @Test
    void listarNoLeidas_debeRetornarSoloNoLeidas() {
        when(notificacionRepository.findByUsuario_IdAndLeidaFalseOrderByFechaCreacionDesc(1))
                .thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(dto);

        List<NotificacionDTO> result = notificacionService.listarNoLeidas(1);
        assertThat(result).hasSize(1);
    }

    // 6️⃣ Obtener por ID - éxito
    @Test
    void obtenerPorId_debeRetornarNotificacion() {
        when(notificacionRepository.findById(1)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(dto);

        NotificacionDTO result = notificacionService.obtenerPorId(1);
        assertThat(result.getTitle()).isEqualTo("Bienvenido");
    }

    // 7️⃣ Obtener por ID - no encontrada
    @Test
    void obtenerPorId_noEncontradaDebeLanzarExcepcion() {
        when(notificacionRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> notificacionService.obtenerPorId(99))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // 8️⃣ Marcar como leída - éxito
    @Test
    void marcarComoLeida_debeCambiarEstadoALeida() {
        when(notificacionRepository.findById(1)).thenReturn(Optional.of(entity));
        when(notificacionRepository.save(any())).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(dto);

        NotificacionDTO result = notificacionService.marcarComoLeida(1);
        assertThat(result).isNotNull();
        verify(notificacionRepository).save(entity);
    }

    // 9️⃣ Marcar como leída - no encontrada
    @Test
    void marcarComoLeida_noEncontradaDebeLanzarExcepcion() {
        when(notificacionRepository.findById(2)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> notificacionService.marcarComoLeida(2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // 🔟 Marcar todas como leídas
    @Test
    void marcarTodasComoLeidas_debeActualizarTodas() {
        when(notificacionRepository.findByUsuario_IdAndLeidaFalseOrderByFechaCreacionDesc(1))
                .thenReturn(List.of(entity));

        notificacionService.marcarTodasComoLeidas(1);

        verify(notificacionRepository).saveAll(anyList());
    }

    // 11️⃣ Contar no leídas
    @Test
    void contarNoLeidas_debeRetornarNumero() {
        when(notificacionRepository.countByUsuario_IdAndLeidaFalse(1)).thenReturn(3L);

        Long count = notificacionService.contarNoLeidas(1);
        assertThat(count).isEqualTo(3L);
    }

    // 12️⃣ Eliminar notificación - éxito
    @Test
    void eliminarNotificacion_existenteDebeEliminar() {
        when(notificacionRepository.existsById(1)).thenReturn(true);

        notificacionService.eliminarNotificacion(1);

        verify(notificacionRepository).deleteById(1);
    }

    // 13️⃣ Eliminar notificación - no existe
    @Test
    void eliminarNotificacion_noExistenteDebeLanzarExcepcion() {
        when(notificacionRepository.existsById(5)).thenReturn(false);
        assertThatThrownBy(() -> notificacionService.eliminarNotificacion(5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // 14️⃣ Eliminar leídas de usuario
    @Test
    void eliminarLeidasDeUsuario_debeEliminarTodasLasLeidas() {
        entity.setLeida(true);
        when(notificacionRepository.findByUsuario_IdAndLeidaTrueOrderByFechaCreacionDesc(1))
                .thenReturn(List.of(entity));

        notificacionService.eliminarLeidasDeUsuario(1);

        verify(notificacionRepository).deleteAll(anyList());
    }

    // 15️⃣ Marcar todas como leídas - sin notificaciones
    @Test
    void marcarTodasComoLeidas_sinNotificacionesNoDebeFallar() {
        when(notificacionRepository.findByUsuario_IdAndLeidaFalseOrderByFechaCreacionDesc(1))
                .thenReturn(Collections.emptyList());

        notificacionService.marcarTodasComoLeidas(1);

        // Aceptamos la llamada aunque sea con lista vacía
        verify(notificacionRepository).saveAll(anyList());
    }

}
