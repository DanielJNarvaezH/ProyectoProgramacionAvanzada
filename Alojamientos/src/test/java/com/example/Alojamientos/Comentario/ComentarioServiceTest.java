package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.ComentarioDTO;
import com.example.Alojamientos.persistenceLayer.entity.*;
import com.example.Alojamientos.persistenceLayer.mapper.ComentarioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.ComentarioRepository;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private ComentarioDataMapper comentarioMapper;

    @InjectMocks
    private ComentarioService comentarioService;

    private ComentarioDTO dto;
    private ComentarioEntity entity;
    private ReservaEntity reserva;
    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = UsuarioEntity.builder().id(1).build();
        reserva = ReservaEntity.builder()
                .id(10)
                .estado(ReservaEntity.EstadoReserva.COMPLETADA)
                .fechaFin(LocalDate.now().minusDays(2))
                .huesped(usuario)
                .build();

        dto = ComentarioDTO.builder()
                .reservationId(10)
                .userId(1)
                .rating(5)
                .text("Excelente alojamiento, muy limpio y cómodo.")
                .build();

        entity = ComentarioEntity.builder()
                .id(100)
                .calificacion(5)
                .texto("Excelente alojamiento, muy limpio y cómodo.")
                .reserva(reserva)
                .usuario(usuario)
                .build();
    }

    // ---------- CREAR COMENTARIO ----------
    @Test
    void crearComentario_Exitoso() {
        when(reservaRepository.findById(10)).thenReturn(Optional.of(reserva));
        when(comentarioRepository.existsByReserva_Id(10)).thenReturn(false);
        when(comentarioMapper.toEntity(dto)).thenReturn(entity);
        when(comentarioRepository.save(any())).thenReturn(entity);
        when(comentarioMapper.toDTO(entity)).thenReturn(dto);

        ComentarioDTO result = comentarioService.crearComentario(dto);

        assertNotNull(result);
        assertEquals(dto.getText(), result.getText());
        verify(comentarioRepository, times(1)).save(any());
    }

    @Test
    void crearComentario_FallaPorReservaNoCompletada() {
        reserva.setEstado(ReservaEntity.EstadoReserva.CONFIRMADA);
        when(reservaRepository.findById(10)).thenReturn(Optional.of(reserva));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> comentarioService.crearComentario(dto));
        assertTrue(ex.getMessage().contains("Solo puedes comentar"));
    }

    @Test
    void crearComentario_FallaPorComentarioExistente() {
        when(reservaRepository.findById(10)).thenReturn(Optional.of(reserva));
        when(comentarioRepository.existsByReserva_Id(10)).thenReturn(true);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> comentarioService.crearComentario(dto));
        assertTrue(ex.getMessage().contains("Ya existe un comentario"));
    }

    @Test
    void crearComentario_FallaPorCalificacionFueraDeRango() {
        dto.setRating(6);
        when(reservaRepository.findById(10)).thenReturn(Optional.of(reserva));
        when(comentarioRepository.existsByReserva_Id(10)).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> comentarioService.crearComentario(dto));
        assertTrue(ex.getMessage().contains("entre 1 y 5"));
    }

    @Test
    void crearComentario_FallaPorUsuarioDiferente() {
        reserva.setHuesped(UsuarioEntity.builder().id(2).build());
        when(reservaRepository.findById(10)).thenReturn(Optional.of(reserva));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> comentarioService.crearComentario(dto));
        assertTrue(ex.getMessage().contains("Solo el huésped"));
    }

    // ---------- OBTENER COMENTARIO ----------
    @Test
    void obtenerPorId_Exitoso() {
        when(comentarioRepository.findById(100)).thenReturn(Optional.of(entity));
        when(comentarioMapper.toDTO(entity)).thenReturn(dto);

        ComentarioDTO result = comentarioService.obtenerPorId(100);

        assertNotNull(result);
        assertEquals(dto.getText(), result.getText());
    }

    @Test
    void obtenerPorId_FallaPorNoEncontrado() {
        when(comentarioRepository.findById(999)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> comentarioService.obtenerPorId(999));
        assertTrue(ex.getMessage().contains("Comentario no encontrado"));
    }

    // ---------- LISTAR ----------
    @Test
    void listarPorAlojamiento_DevuelveLista() {
        when(comentarioRepository.findByAlojamiento_IdOrderByFechaCreacionDesc(5))
                .thenReturn(List.of(entity));
        when(comentarioMapper.toDTO(any())).thenReturn(dto);

        List<ComentarioDTO> result = comentarioService.listarPorAlojamiento(5);

        assertEquals(1, result.size());
        verify(comentarioRepository).findByAlojamiento_IdOrderByFechaCreacionDesc(5);
    }

    // ---------- PROMEDIO ----------
    @Test
    void obtenerPromedioCalificaciones_CalculaCorrecto() {
        ComentarioEntity c1 = ComentarioEntity.builder().calificacion(4).build();
        ComentarioEntity c2 = ComentarioEntity.builder().calificacion(2).build();
        when(comentarioRepository.findByAlojamiento_IdOrderByFechaCreacionDesc(7))
                .thenReturn(List.of(c1, c2));

        Double promedio = comentarioService.obtenerPromedioCalificaciones(7);

        assertEquals(3.0, promedio);
    }

    @Test
    void obtenerPromedioCalificaciones_SinComentarios() {
        when(comentarioRepository.findByAlojamiento_IdOrderByFechaCreacionDesc(7))
                .thenReturn(Collections.emptyList());

        Double promedio = comentarioService.obtenerPromedioCalificaciones(7);

        assertEquals(0.0, promedio);
    }

    // ---------- ACTUALIZAR ----------
    @Test
    void actualizarComentario_Exitoso() {
        when(comentarioRepository.findById(100)).thenReturn(Optional.of(entity));
        when(comentarioRepository.save(any())).thenReturn(entity);
        when(comentarioMapper.toDTO(any())).thenReturn(dto);

        ComentarioDTO result = comentarioService.actualizarComentario(100, "Nuevo texto");

        assertNotNull(result);
        verify(comentarioRepository).save(any());
    }

    @Test
    void actualizarComentario_FallaPorLongitud() {
        when(comentarioRepository.findById(100)).thenReturn(Optional.of(entity));

        String textoLargo = "x".repeat(501);
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.actualizarComentario(100, textoLargo));

        assertTrue(ex.getMessage().contains("500 caracteres"));
    }

    // ---------- ELIMINAR ----------
    @Test
    void eliminarComentario_Exitoso() {
        when(comentarioRepository.existsById(100)).thenReturn(true);

        comentarioService.eliminarComentario(100);

        verify(comentarioRepository).deleteById(100);
    }

    @Test
    void eliminarComentario_FallaPorNoExistir() {
        when(comentarioRepository.existsById(100)).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.eliminarComentario(100));

        assertTrue(ex.getMessage().contains("no encontrado"));
    }
}
