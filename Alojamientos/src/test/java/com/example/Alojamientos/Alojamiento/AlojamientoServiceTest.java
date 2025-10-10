package com.example.Alojamientos;

import com.example.Alojamientos.businessLayer.dto.AlojamientoDTO;
import com.example.Alojamientos.businessLayer.service.AlojamientoService;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.mapper.AlojamientoDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoRepository;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import com.example.Alojamientos.presentationLayer.controller.AlojamientoController;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas Unitarias para AlojamientoService y AlojamientoController
 * Cobertura JaCoCo: Métodos CRUD de Alojamientos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de Gestión de Alojamientos")
class AlojamientoServiceTest {

    // ==================== MOCKS ====================
    @Mock
    private AlojamientoRepository alojamientoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private AlojamientoDataMapper alojamientoMapper;

    @InjectMocks
    private AlojamientoService alojamientoService;

    @InjectMocks
    private AlojamientoController alojamientoController;

    // ==================== FIXTURES ====================
    private AlojamientoDTO alojamientoDTO;
    private AlojamientoEntity alojamientoEntity;

    @BeforeEach
    void setUp() {
        // Preparar DTO
        alojamientoDTO = AlojamientoDTO.builder()
                .hostId(1)
                .name("Casa en la playa")
                .description("Hermosa casa frente al mar")
                .address("Calle 123, Cartagena")
                .city("Cartagena")
                .latitude(10.3910)
                .longitude(-75.4794)
                .pricePerNight(200.0)
                .maxCapacity(6)
                .mainImage("https://example.com/image.jpg")
                .active(true)
                .build();

        // Preparar Entity
        alojamientoEntity = AlojamientoEntity.builder()
                .id(1)
                .nombre("Casa en la playa")
                .descripcion("Hermosa casa frente al mar")
                .direccion("Calle 123, Cartagena")
                .ciudad("Cartagena")
                .latitud(BigDecimal.valueOf(10.3910))
                .longitud(BigDecimal.valueOf(-75.4794))
                .precioPorNoche(BigDecimal.valueOf(200.0))
                .capacidadMaxima(6)
                .imagenPrincipal("https://example.com/image.jpg")
                .activo(true)
                .build();
    }

    // ==================== PRUEBAS DE CREACIÓN ====================

    @Test
    @DisplayName("POST /api/alojamientos - Crear alojamiento exitosamente")
    void testCrearAlojamientoExitoso() {
        // Given - Datos válidos
        when(alojamientoMapper.toEntity(alojamientoDTO)).thenReturn(alojamientoEntity);
        when(alojamientoRepository.save(any(AlojamientoEntity.class))).thenReturn(alojamientoEntity);
        when(alojamientoMapper.toDTO(alojamientoEntity)).thenReturn(alojamientoDTO);

        // When
        AlojamientoDTO resultado = alojamientoService.crearAlojamiento(alojamientoDTO);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getName()).isEqualTo("Casa en la playa");
        assertThat(resultado.getPricePerNight()).isEqualTo(200.0);
        verify(alojamientoRepository, times(1)).save(any(AlojamientoEntity.class));
    }

    @Test
    @DisplayName("POST /api/alojamientos - Error: Sin imagen principal")
    void testCrearAlojamientoSinImagenPrincipal() {
        // Given
        alojamientoDTO.setMainImage(null);

        // When & Then
        assertThatThrownBy(() -> alojamientoService.crearAlojamiento(alojamientoDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El alojamiento debe tener al menos una imagen principal");

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/alojamientos - Error: Coordenadas no válidas (latitud fuera de rango)")
    void testCrearAlojamientoLatitudFueraDeRango() {
        // Given
        alojamientoDTO.setLatitude(95.0); // Fuera de rango -90 a 90

        // When & Then
        assertThatThrownBy(() -> alojamientoService.crearAlojamiento(alojamientoDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La latitud debe estar entre -90 y 90 grados");

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/alojamientos - Error: Coordenadas no válidas (longitud fuera de rango)")
    void testCrearAlojamientoLongitudFueraDeRango() {
        // Given
        alojamientoDTO.setLongitude(-200.0); // Fuera de rango -180 a 180

        // When & Then
        assertThatThrownBy(() -> alojamientoService.crearAlojamiento(alojamientoDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La longitud debe estar entre -180 y 180 grados");

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/alojamientos - Error: Precio inválido (menor o igual a 0)")
    void testCrearAlojamientoPrecioInvalido() {
        // Given
        alojamientoDTO.setPricePerNight(0.0);

        // When & Then
        assertThatThrownBy(() -> alojamientoService.crearAlojamiento(alojamientoDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El precio por noche debe ser mayor a 0");

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/alojamientos - Error: Capacidad inválida (menor a 1)")
    void testCrearAlojamientoCapacidadInvalida() {
        // Given
        alojamientoDTO.setMaxCapacity(0);

        // When & Then
        assertThatThrownBy(() -> alojamientoService.crearAlojamiento(alojamientoDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La capacidad máxima debe ser al menos 1 huésped");

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/alojamientos - Error: Nombre vacío")
    void testCrearAlojamientoNombreVacio() {
        // Given
        alojamientoDTO.setName("");

        // When & Then
        assertThatThrownBy(() -> alojamientoService.crearAlojamiento(alojamientoDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El nombre del alojamiento es obligatorio");

        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/alojamientos - Error: Descripción vacía")
    void testCrearAlojamientoDescripcionVacia() {
        // Given
        alojamientoDTO.setDescription(null);

        // When & Then
        assertThatThrownBy(() -> alojamientoService.crearAlojamiento(alojamientoDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La descripción del alojamiento es obligatoria");

        verify(alojamientoRepository, never()).save(any());
    }

    // ==================== PRUEBAS DE LECTURA ====================

    @Test
    @DisplayName("GET /api/alojamientos - Listar todos los alojamientos activos")
    void testListarAlojamientosActivos() {
        // Given
        List<AlojamientoEntity> alojamientos = Arrays.asList(alojamientoEntity);
        List<AlojamientoDTO> alojamientosDTO = Arrays.asList(alojamientoDTO);

        when(alojamientoRepository.findByActivoTrue()).thenReturn(alojamientos);
        when(alojamientoMapper.toDTO(alojamientoEntity)).thenReturn(alojamientoDTO);

        // When
        List<AlojamientoDTO> resultado = alojamientoService.listarActivos();

        // Then
        assertThat(resultado).isNotEmpty();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getName()).isEqualTo("Casa en la playa");
        verify(alojamientoRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("GET /api/alojamientos - Lista vacía cuando no hay alojamientos")
    void testListarAlojamientosVacio() {
        // Given
        when(alojamientoRepository.findByActivoTrue()).thenReturn(Collections.emptyList());

        // When
        List<AlojamientoDTO> resultado = alojamientoService.listarActivos();

        // Then
        assertThat(resultado).isEmpty();
        verify(alojamientoRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("GET /api/alojamientos/{id} - Obtener alojamiento por ID")
    void testObtenerAlojamientoPorIdExitoso() {
        // Given
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(alojamientoMapper.toDTO(alojamientoEntity)).thenReturn(alojamientoDTO);

        // When
        AlojamientoDTO resultado = alojamientoService.obtenerPorId(1);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getName()).isEqualTo("Casa en la playa");
        verify(alojamientoRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("GET /api/alojamientos/{id} - Error: Alojamiento no encontrado")
    void testObtenerAlojamientoPorIdNoEncontrado() {
        // Given
        when(alojamientoRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> alojamientoService.obtenerPorId(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alojamiento no encontrado");

        verify(alojamientoRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("GET /api/alojamientos/{id} - Error: Alojamiento eliminado (inactivo)")
    void testObtenerAlojamientoEliminado() {
        // Given
        alojamientoEntity.setActivo(false);
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));

        // When & Then
        assertThatThrownBy(() -> alojamientoService.obtenerPorId(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alojamiento no disponible");

        verify(alojamientoRepository, times(1)).findById(1);
    }

    // ==================== PRUEBAS DE ACTUALIZACIÓN ====================

    @Test
    @DisplayName("PUT /api/alojamientos/{id} - Actualizar alojamiento exitosamente")
    void testActualizarAlojamientoExitoso() {
        // Given
        AlojamientoDTO alojamientoActualizado = AlojamientoDTO.builder()
                .name("Casa remodelada")
                .description("Casa mejorada")
                .address("Avenida Nueva 123")
                .city("Cartagena")
                .pricePerNight(250.0)
                .maxCapacity(8)
                .build();

        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(alojamientoMapper.doubleToBigDecimal(250.0)).thenReturn(BigDecimal.valueOf(250.0));
        when(alojamientoRepository.save(any(AlojamientoEntity.class))).thenReturn(alojamientoEntity);
        when(alojamientoMapper.toDTO(alojamientoEntity)).thenReturn(alojamientoActualizado);

        // When
        AlojamientoDTO resultado = alojamientoService.actualizarAlojamiento(1, alojamientoActualizado);

        // Then
        assertThat(resultado).isNotNull();
        verify(alojamientoRepository, times(1)).findById(1);
        verify(alojamientoRepository, times(1)).save(any(AlojamientoEntity.class));
    }

    @Test
    @DisplayName("PUT /api/alojamientos/{id} - Error: Alojamiento no encontrado")
    void testActualizarAlojamientoNoEncontrado() {
        // Given
        when(alojamientoRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> alojamientoService.actualizarAlojamiento(999, alojamientoDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alojamiento no encontrado");

        verify(alojamientoRepository, times(1)).findById(999);
        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /api/alojamientos/{id} - Error: No se puede actualizar alojamiento eliminado")
    void testActualizarAlojamientoEliminado() {
        // Given
        alojamientoEntity.setActivo(false);
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));

        // When & Then
        assertThatThrownBy(() -> alojamientoService.actualizarAlojamiento(1, alojamientoDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede actualizar un alojamiento eliminado");

        verify(alojamientoRepository, times(1)).findById(1);
        verify(alojamientoRepository, never()).save(any());
    }

    // ==================== PRUEBAS DE ELIMINACIÓN ====================

    @Test
    @DisplayName("DELETE /api/alojamientos/{id} - Eliminar alojamiento (soft delete) exitoso")
    void testEliminarAlojamientoExitoso() {
        // Given
        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Collections.emptyList());
        when(alojamientoRepository.save(any(AlojamientoEntity.class))).thenReturn(alojamientoEntity);

        // When
        alojamientoService.eliminarAlojamiento(1);

        // Then
        verify(alojamientoRepository, times(1)).findById(1);
        verify(reservaRepository, times(1)).findByAlojamiento_Id(1);
        verify(alojamientoRepository, times(1)).save(any(AlojamientoEntity.class));
        assertThat(alojamientoEntity.getActivo()).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/alojamientos/{id} - Error: Alojamiento no encontrado")
    void testEliminarAlojamientoNoEncontrado() {
        // Given
        when(alojamientoRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> alojamientoService.eliminarAlojamiento(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alojamiento no encontrado");

        verify(alojamientoRepository, times(1)).findById(999);
        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/alojamientos/{id} - Error: No se puede eliminar con reservas activas")
    void testEliminarAlojamientoConReservasActivas() {
        // Given
        ReservaEntity reservaActiva = ReservaEntity.builder()
                .id(1)
                .estado(ReservaEntity.EstadoReserva.CONFIRMADA)
                .fechaInicio(LocalDate.now().plusDays(1))
                .fechaFin(LocalDate.now().plusDays(5))
                .build();

        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Arrays.asList(reservaActiva));

        // When & Then
        assertThatThrownBy(() -> alojamientoService.eliminarAlojamiento(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede eliminar el alojamiento porque tiene");

        verify(alojamientoRepository, times(1)).findById(1);
        verify(alojamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/alojamientos/{id} - No elimina reservas canceladas")
    void testEliminarAlojamientoConReservasCanceladas() {
        // Given
        ReservaEntity reservaCancelada = ReservaEntity.builder()
                .id(1)
                .estado(ReservaEntity.EstadoReserva.CANCELADA)
                .fechaInicio(LocalDate.now().plusDays(1))
                .fechaFin(LocalDate.now().plusDays(5))
                .build();

        when(alojamientoRepository.findById(1)).thenReturn(Optional.of(alojamientoEntity));
        when(reservaRepository.findByAlojamiento_Id(1)).thenReturn(Arrays.asList(reservaCancelada));
        when(alojamientoRepository.save(any(AlojamientoEntity.class))).thenReturn(alojamientoEntity);

        // When
        alojamientoService.eliminarAlojamiento(1);

        // Then
        verify(alojamientoRepository, times(1)).save(any(AlojamientoEntity.class));
        assertThat(alojamientoEntity.getActivo()).isFalse();
    }

    // ==================== PRUEBAS DE CONTROLLER ====================

    @Test
    @DisplayName("Controller: POST /api/alojamientos - Retorna 201 Created")
    void testControllerCrearAlojamiento() {
        // Given
        ResponseEntity<String> expected = ResponseEntity.status(HttpStatus.CREATED).body("Alojamiento creado (mock)");

        // When
        ResponseEntity<String> resultado = alojamientoController.create(alojamientoDTO);

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resultado.getBody()).contains("creado");
    }

    @Test
    @DisplayName("Controller: GET /api/alojamientos - Retorna 200 OK")
    void testControllerListarAlojamientos() {
        // When
        ResponseEntity<List<AlojamientoDTO>> resultado = alojamientoController.getAll();

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Controller: GET /api/alojamientos/{id} - Retorna 200 OK")
    void testControllerObtenerAlojamientoPorId() {
        // When
        ResponseEntity<AlojamientoDTO> resultado = alojamientoController.getById(1L);

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Controller: PUT /api/alojamientos/{id} - Retorna 200 OK")
    void testControllerActualizarAlojamiento() {
        // When
        ResponseEntity<String> resultado = alojamientoController.update(1L, alojamientoDTO);

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).contains("actualizado");
    }

    @Test
    @DisplayName("Controller: DELETE /api/alojamientos/{id} - Retorna 204 No Content")
    void testControllerEliminarAlojamiento() {
        // When
        ResponseEntity<Void> resultado = alojamientoController.delete(1L);

        // Then
        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(resultado.getBody()).isNull();
    }
}