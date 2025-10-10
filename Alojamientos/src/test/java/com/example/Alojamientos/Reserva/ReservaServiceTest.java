package com.example.Alojamientos;

import com.example.Alojamientos.businessLayer.dto.ReservaDTO;
import com.example.Alojamientos.businessLayer.service.ReservaService;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ReservaDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoRepository;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import com.example.Alojamientos.presentationLayer.controller.ReservaController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

/**
 * Pruebas Unitarias para ReservaService y ReservaController
 * Cobertura JaCoCo: Métodos CRUD de Reservas
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

    @InjectMocks
    private ReservaController reservaController;

    // ==================== FIXTURES ====================
    private ReservaDTO reservaDTO;
    private ReservaEntity reservaEntity;
    private AlojamientoEntity alojamientoEntity;

    @BeforeEach
    void setUp() {
        // Preparar alojamiento
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

        // Preparar DTO
        reservaDTO = ReservaDTO.builder()
                .guestId(2)
                .lodgingId(1)
                .startDate("2025-12-01")
                .endDate("2025-12-05")
                .numGuests(4)
                .totalPrice(800.0)
                .status("CONFIRMADA")
                .build();

        // Preparar Entity
        reservaEntity = ReservaEntity.builder()
                .id(1)
                .numHuespedes(4)
                .precioTotal(BigDecimal.valueOf(800.0))
                .estado(ReservaEntity.EstadoReserva.CONFIRMADA)
                .fechaInicio(LocalDate.parse("2025-12-01"))
                .fechaFin(LocalDate.parse("2025-12-05"))
                .fechaReserva(LocalDateTime.now())
                .build();
    }

    // ==================== PRUEBAS DE CREACIÓN ====================

    @Test
    @DisplayName("POST /api/reservas - Crear reserva exitosamente")
    void testCrearReservaExitosa() {
        // Given
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Collections.emptyList());
        when(reservaMapper.toEntity(any(ReservaDTO.class))).thenReturn(reservaEntity);
        when(reservaRepository.save(any(ReservaEntity.class))).thenReturn(reservaEntity);
        when(reservaMapper.toDTO(any(ReservaEntity.class))).thenReturn(reservaDTO);

        // When
        ReservaDTO resultado = reservaService.crearReserva(reservaDTO);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumGuests()).isEqualTo(4);
        assertThat(resultado.getTotalPrice()).isEqualTo(800.0);
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    @Test
    @DisplayName("POST /api/reservas - Error: ID del huésped nulo")
    void testCrearReservaHuespedNulo() {
        // Given
        reservaDTO.setGuestId(null);

        // When & Then
        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El ID del huésped es obligatorio");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: ID del alojamiento nulo")
    void testCrearReservaAlojamientoNulo() {
        // Given
        reservaDTO.setLodgingId(null);

        // When & Then
        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El ID del alojamiento es obligatorio");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Fecha de inicio anterior a hoy")
    void testCrearReservaFechaInicioPasada() {
        // Given
        reservaDTO.setStartDate(LocalDate.now().minusDays(5).toString());

        // When & Then
        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La fecha de inicio no puede ser anterior a hoy");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Menos de 1 noche")
    void testCrearReservaMenosDeUnaNoche() {
        // Given - Misma fecha inicio y fin
        reservaDTO.setStartDate("2025-12-01");
        reservaDTO.setEndDate("2025-12-01");

        // When & Then
        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La reserva debe ser de al menos 1 noche");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Alojamiento no encontrado")
    void testCrearReservaAlojamientoNoEncontrado() {
        // Given
        when(alojamientoRepository.findById(999)).thenReturn(Optional.empty());

        reservaDTO.setLodgingId(999);

        // When & Then
        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alojamiento no encontrado");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Alojamiento inactivo")
    void testCrearReservaAlojamientoInactivo() {
        // Given
        alojamientoEntity.setActivo(false);
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));

        // When & Then
        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El alojamiento no está disponible");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Número de huéspedes excede capacidad")
    void testCrearReservaCapacidadExcedida() {
        // Given
        reservaDTO.setNumGuests(10); // Capacidad máxima es 6
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));

        // When & Then
        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede la capacidad máxima");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Precio total inválido (0 o negativo)")
    void testCrearReservaPrecioInvalido() {
        // Given
        reservaDTO.setTotalPrice(0.0);
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El precio total debe ser mayor a 0");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/reservas - Error: Solapamiento de fechas con otra reserva")
    void testCrearReservaSolapamientoFechas() {
        // Given
        ReservaEntity reservaExistente = ReservaEntity.builder()
                .id(2)
                .estado(ReservaEntity.EstadoReserva.CONFIRMADA)
                .fechaInicio(LocalDate.parse("2025-12-03"))
                .fechaFin(LocalDate.parse("2025-12-07"))
                .build();

        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Arrays.asList(reservaExistente));

        // When & Then
        assertThatThrownBy(() -> reservaService.crearReserva(reservaDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está disponible en las fechas seleccionadas");

        verify(reservaRepository, never()).save(any());
    }

    // ==================== PRUEBAS DE LECTURA ====================

    @Test
    @DisplayName("GET /api/reservas/{id} - Obtener reserva por ID")
    void testObtenerReservaPorIdExitoso() {
        // Given
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));
        when(reservaMapper.toDTO(reservaEntity)).thenReturn(reservaDTO);

        // When
        ReservaDTO resultado = reservaService.obtenerPorId(1);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumGuests()).isEqualTo(4);
        verify(reservaRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("GET /api/reservas/{id} - Error: Reserva no encontrada")
    void testObtenerReservaPorIdNoEncontrado() {
        // Given
        when(reservaRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservaService.obtenerPorId(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reserva no encontrada");

        verify(reservaRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("GET /api/reservas - Listar reservas por huésped")
    void testListarReservasPorHuesped() {
        // Given
        List<ReservaEntity> reservas = Arrays.asList(reservaEntity);
        when(reservaRepository.findByHuesped_Id(2)).thenReturn(reservas);
        when(reservaMapper.toDTO(any(ReservaEntity.class))).thenReturn(reservaDTO);

        // When
        List<ReservaDTO> resultado = reservaService.listarPorHuesped(2);

        // Then
        assertThat(resultado).isNotEmpty();
        assertThat(resultado).hasSize(1);
        verify(reservaRepository, times(1)).findByHuesped_Id(2);
    }

    @Test
    @DisplayName("GET /api/reservas - Listar reservas por alojamiento")
    void testListarReservasPorAlojamiento() {
        // Given
        List<ReservaEntity> reservas = Arrays.asList(reservaEntity);
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(reservas);
        when(reservaMapper.toDTO(any(ReservaEntity.class))).thenReturn(reservaDTO);

        // When
        List<ReservaDTO> resultado = reservaService.listarPorAlojamiento(1);

        // Then
        assertThat(resultado).isNotEmpty();
        assertThat(resultado).hasSize(1);
        verify(reservaRepository, times(1)).findByAlojamiento_Id(1);
    }

    @Test
    @DisplayName("GET /api/reservas - Lista vacía")
    void testListarReservasVacio() {
        // Given
        when(reservaRepository.findByHuesped_Id(999)).thenReturn(Collections.emptyList());

        // When
        List<ReservaDTO> resultado = reservaService.listarPorHuesped(999);

        // Then
        assertThat(resultado).isEmpty();
        verify(reservaRepository, times(1)).findByHuesped_Id(999);
    }

    // ==================== PRUEBAS DE ACTUALIZACIÓN ====================

    @Test
    void obtenerPorId_deberiaLanzarExcepcionCuandoNoExiste() {
        // given
        Integer idInexistente = 999;

        // when / then
        assertThrows(IllegalArgumentException.class, () ->
                reservaService.obtenerPorId(idInexistente)
        );
    }

    // ==================== PRUEBAS DE CANCELACIÓN ====================

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Cancelar reserva exitosamente")
    void testCancelarReservaExitosa() {
        // Given - Reserva con check-in en más de 48 horas
        reservaEntity.setFechaInicio(LocalDate.now().plusDays(3));
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));
        when(reservaRepository.save(any(ReservaEntity.class))).thenReturn(reservaEntity);

        // When
        reservaService.cancelarReserva(1, "Cambio de planes");

        // Then
        assertThat(reservaEntity.getEstado()).isEqualTo(ReservaEntity.EstadoReserva.CANCELADA);
        assertThat(reservaEntity.getMotivoCancelacion()).isEqualTo("Cambio de planes");
        verify(reservaRepository, times(1)).findById(1);
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Reserva no encontrada")
    void testCancelarReservaNoEncontrada() {
        // Given
        when(reservaRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservaService.cancelarReserva(999, "Motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reserva no encontrada");

        verify(reservaRepository, times(1)).findById(999);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Reserva ya cancelada")
    void testCancelarReservaYaCancelada() {
        // Given
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.CANCELADA);
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));

        // When & Then
        assertThatThrownBy(() -> reservaService.cancelarReserva(1, "Motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La reserva ya fue cancelada previamente");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Reserva completada no se puede cancelar")
    void testCancelarReservaCompletada() {
        // Given
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.COMPLETADA);
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));

        // When & Then
        assertThatThrownBy(() -> reservaService.cancelarReserva(1, "Motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede cancelar una reserva que ya fue completada");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Menos de 48 horas para check-in")
    void testCancelarReservaMenosDe48Horas() {
        // Given - Check-in en 24 horas
        reservaEntity.setFechaInicio(LocalDate.now().plusDays(1));
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.CONFIRMADA);
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));

        // When & Then
        assertThatThrownBy(() -> reservaService.cancelarReserva(1, "Motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("48 horas de anticipación");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Error: Motivo de cancelación vacío")
    void testCancelarReservaSinMotivo() {
        // Given
        reservaEntity.setFechaInicio(LocalDate.now().plusDays(3));
        reservaEntity.setEstado(ReservaEntity.EstadoReserva.CONFIRMADA);
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reservaEntity));

        // When & Then
        assertThatThrownBy(() -> reservaService.cancelarReserva(1, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("motivo de cancelación");

        verify(reservaRepository, never()).save(any());
    }

    // ==================== PRUEBAS DE CONTROLLER ====================

    @Test
    @DisplayName("Controller: POST /api/reservas - Retorna 201 Created")
    void testControllerCrearReserva() {
        // When
        ResponseEntity<String> resultado = reservaController.create(reservaDTO);

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resultado.getBody()).contains("creada");
    }

    @Test
    @DisplayName("Controller: GET /api/reservas - Retorna 200 OK")
    void testControllerListarReservas() {
        // When
        ResponseEntity<List<ReservaDTO>> resultado = reservaController.getAll();

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Controller: GET /api/reservas/{id} - Retorna 200 OK")
    void testControllerObtenerReservaPorId() {
        // When
        ResponseEntity<ReservaDTO> resultado = reservaController.getById(1L);

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Controller: PUT /api/reservas/{id} - Retorna 200 OK")
    void testControllerActualizarReserva() {
        // When
        ResponseEntity<String> resultado = reservaController.update(1L, reservaDTO);

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).contains("actualizada");
    }

    @Test
    @DisplayName("Controller: DELETE /api/reservas/{id} - Retorna 204 No Content")
    void testControllerCancelarReserva() {
        // When
        ResponseEntity<Void> resultado = reservaController.delete(1L);

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(resultado.getBody()).isNull();
    }
}