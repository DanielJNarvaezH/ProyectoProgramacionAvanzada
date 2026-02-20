package com.example.Alojamientos.Reserva;

import com.example.Alojamientos.businessLayer.dto.ReservaDTO;
import com.example.Alojamientos.businessLayer.service.ReservaService;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ReservaDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoRepository;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas Unitarias para ReservaService
 * Cobertura JaCoCo: Métodos CRUD de Reservas
 *
 * CORRECCIONES aplicadas:
 * 1. Fechas dinámicas futuras (LocalDate.now().plusDays()) en lugar de fechas
 *    hardcodeadas en 2025 que ya quedaron en el pasado.
 * 2. Eliminado @InjectMocks de ReservaController para evitar conflicto
 *    de inyección de mocks con ReservaService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de Gestión de Reservas")
class ReservaServiceTest {

    // ==================== MOCKS ====================
    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private AlojamientoRepository alojamientoRepository;

    @Mock
    private ReservaDataMapper reservaMapper;

    @InjectMocks
    private ReservaService reservaService;

    // ==================== FIXTURES ====================
    private ReservaDTO reservaDTO;
    private ReservaEntity reservaEntity;
    private AlojamientoEntity alojamientoEntity;

    // Fechas dinámicas siempre en el futuro
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @BeforeEach
    void setUp() {
        // ── Fechas SIEMPRE futuras (se calculan en cada ejecución) ──────
        fechaInicio = LocalDate.now().plusDays(10);
        fechaFin    = LocalDate.now().plusDays(14);

        // ── Alojamiento de prueba ────────────────────────────────────────
        alojamientoEntity = AlojamientoEntity.builder()
                .id(1)
                .nombre("Casa en la playa")
                .descripcion("Hermosa casa")
                .direccion("Calle 123")
                .ciudad("Cartagena")
                .latitud(BigDecimal.valueOf(10.3910))
                .longitud(BigDecimal.valueOf(-75.4794))
                .precioPorNoche(BigDecimal.valueOf(200.0))
                .capacidadMaxima(6)
                .imagenPrincipal("image.jpg")
                .activo(true)
                .build();

        // ── DTO de prueba ────────────────────────────────────────────────
        reservaDTO = ReservaDTO.builder()
                .guestId(2)
                .lodgingId(1)
                .startDate(fechaInicio.toString())
                .endDate(fechaFin.toString())
                .numGuests(4)
                .totalPrice(800.0)
                .status("CONFIRMADA")
                .build();

        // ── Entity de prueba ─────────────────────────────────────────────
        reservaEntity = ReservaEntity.builder()
                .id(1)
                .numHuespedes(4)
                .precioTotal(BigDecimal.valueOf(800.0))
                .estado(ReservaEntity.EstadoReserva.CONFIRMADA)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .fechaReserva(LocalDateTime.now())
                .build();
    }

    // ==================== PRUEBAS DE CREACIÓN ====================

    @Test
    @DisplayName("POST /api/reservas - Crear reserva exitosamente")
    void testCrearReservaExitosa() {
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Collections.emptyList());
        when(reservaMapper.toEntity(any(ReservaDTO.class))).thenReturn(reservaEntity);
        when(reservaRepository.save(any(ReservaEntity.class))).thenReturn(reservaEntity);
        when(reservaMapper.toDTO(any(ReservaEntity.class))).thenReturn(reservaDTO);

        ReservaDTO resultado = reservaService.crearReserva(reservaDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumGuests()).isEqualTo(4);
        assertThat(resultado.getTotalPrice()).isEqualTo(800.0);
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    @Test
    @DisplayName("POST /api/reservas - Error: ID del huésped nulo")
    void testCrearReservaHuespedNulo() {
        reservaDTO.setGuestId(null);

        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El ID del huésped es obligatorio");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: ID del alojamiento nulo")
    void testCrearReservaAlojamientoNulo() {
        reservaDTO.setLodgingId(null);

        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El ID del alojamiento es obligatorio");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Fecha de inicio anterior a hoy")
    void testCrearReservaFechaInicioPasada() {
        reservaDTO.setStartDate(LocalDate.now().minusDays(5).toString());

        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La fecha de inicio no puede ser anterior a hoy");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Menos de 1 noche")
    void testCrearReservaMenosDeUnaNoche() {
        // Misma fecha inicio y fin → 0 noches
        reservaDTO.setStartDate(fechaInicio.toString());
        reservaDTO.setEndDate(fechaInicio.toString());

        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La reserva debe ser de al menos 1 noche");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Alojamiento no encontrado")
    void testCrearReservaAlojamientoNoEncontrado() {
        when(alojamientoRepository.findById(999)).thenReturn(Optional.empty());
        reservaDTO.setLodgingId(999);

        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alojamiento no encontrado");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Alojamiento inactivo")
    void testCrearReservaAlojamientoInactivo() {
        alojamientoEntity.setActivo(false);
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));

        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El alojamiento no está disponible");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Número de huéspedes excede capacidad")
    void testCrearReservaCapacidadExcedida() {
        reservaDTO.setNumGuests(10); // Capacidad máxima es 6
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));

        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede la capacidad máxima");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Precio total inválido (0 o negativo)")
    void testCrearReservaPrecioInvalido() {
        reservaDTO.setTotalPrice(0.0);
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El precio total debe ser mayor a 0");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Solapamiento de fechas con otra reserva")
    void testCrearReservaSolapamientoFechas() {
        // Reserva existente que se solapa con la nueva
        ReservaEntity reservaExistente = ReservaEntity.builder()
                .id(2)
                .estado(ReservaEntity.EstadoReserva.CONFIRMADA)
                .fechaInicio(fechaInicio.plusDays(2))
                .fechaFin(fechaFin.plusDays(2))
                .build();

        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Arrays.asList(reservaExistente));

        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está disponible en las fechas seleccionadas");

        verify(reservaRepository, never()).save(any());
    }

    // ==================== PRUEBAS DE LECTURA ====================

    @Test
    @DisplayName("GET /api/reservas/{id} - Obtener reserva por ID")
    void testObtenerReservaPorIdExitoso() {
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));
        when(reservaMapper.toDTO(reservaEntity)).thenReturn(reservaDTO);

        ReservaDTO resultado = reservaService.obtenerPorId(1);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumGuests()).isEqualTo(4);
        verify(reservaRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("GET /api/reservas/{id} - Error: Reserva no encontrada")
    void testObtenerReservaPorIdNoEncontrado() {
        when(reservaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.obtenerPorId(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reserva no encontrada");

        verify(reservaRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("GET /api/reservas - Listar reservas por huésped")
    void testListarReservasPorHuesped() {
        when(reservaRepository.findByHuesped_Id(2)).thenReturn(Arrays.asList(reservaEntity));
        when(reservaMapper.toDTO(any(ReservaEntity.class))).thenReturn(reservaDTO);

        List<ReservaDTO> resultado = reservaService.listarPorHuesped(2);

        assertThat(resultado).isNotEmpty().hasSize(1);
        verify(reservaRepository, times(1)).findByHuesped_Id(2);
    }

    @Test
    @DisplayName("GET /api/reservas - Listar reservas por alojamiento")
    void testListarReservasPorAlojamiento() {
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Arrays.asList(reservaEntity));
        when(reservaMapper.toDTO(any(ReservaEntity.class))).thenReturn(reservaDTO);

        List<ReservaDTO> resultado = reservaService.listarPorAlojamiento(1);

        assertThat(resultado).isNotEmpty().hasSize(1);
        verify(reservaRepository, times(1)).findByAlojamiento_Id(1);
    }

    @Test
    @DisplayName("GET /api/reservas - Lista vacía")
    void testListarReservasVacio() {
        when(reservaRepository.findByHuesped_Id(999)).thenReturn(Collections.emptyList());

        List<ReservaDTO> resultado = reservaService.listarPorHuesped(999);

        assertThat(resultado).isEmpty();
        verify(reservaRepository, times(1)).findByHuesped_Id(999);
    }

    @Test
    @DisplayName("GET /api/reservas/{id} - Error: ID inexistente lanza excepción")
    void obtenerPorId_deberiaLanzarExcepcionCuandoNoExiste() {
        assertThrows(IllegalArgumentException.class, () ->
                reservaService.obtenerPorId(999)
        );
    }

    // ==================== PRUEBAS DE CANCELACIÓN ====================

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Cancelar reserva exitosamente")
    void testCancelarReservaExitosa() {
        reservaEntity.setFechaInicio(LocalDate.now().plusDays(3));
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));
        when(reservaRepository.save(any(ReservaEntity.class))).thenReturn(reservaEntity);

        reservaService.cancelarReserva(1, "Cambio de planes");

        assertThat(reservaEntity.getEstado()).isEqualTo(ReservaEntity.EstadoReserva.CANCELADA);
        assertThat(reservaEntity.getMotivoCancelacion()).isEqualTo("Cambio de planes");
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Reserva no encontrada")
    void testCancelarReservaNoEncontrada() {
        when(reservaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.cancelarReserva(999, "Motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reserva no encontrada");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Reserva ya cancelada")
    void testCancelarReservaYaCancelada() {
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.CANCELADA);
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));

        assertThatThrownBy(() -> reservaService.cancelarReserva(1, "Motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La reserva ya fue cancelada previamente");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Reserva completada no se puede cancelar")
    void testCancelarReservaCompletada() {
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.COMPLETADA);
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));

        assertThatThrownBy(() -> reservaService.cancelarReserva(1, "Motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede cancelar una reserva que ya fue completada");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Menos de 48 horas para check-in")
    void testCancelarReservaMenosDe48Horas() {
        reservaEntity.setFechaInicio(LocalDate.now().plusDays(1));
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.CONFIRMADA);
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));

        assertThatThrownBy(() -> reservaService.cancelarReserva(1, "Motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("48 horas de anticipación");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Motivo de cancelación vacío")
    void testCancelarReservaSinMotivo() {
        reservaEntity.setFechaInicio(LocalDate.now().plusDays(3));
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.CONFIRMADA);
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));

        assertThatThrownBy(() -> reservaService.cancelarReserva(1, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("motivo de cancelación");

        verify(reservaRepository, never()).save(any());
    }
}