package com.example.Alojamientos.Servicio;

import com.example.Alojamientos.businessLayer.dto.ServicioDTO;
import com.example.Alojamientos.businessLayer.service.ServicioService;
import com.example.Alojamientos.persistenceLayer.entity.ServicioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ServicioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private ServicioDataMapper servicioMapper;

    @InjectMocks
    private ServicioService servicioService;

    private ServicioDTO servicioDTO;
    private ServicioEntity servicioEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        servicioDTO = ServicioDTO.builder()
                .name("Wifi")
                .description("Internet rápido")
                .icon("wifi-icon")
                .active(true)
                .build();

        servicioEntity = ServicioEntity.builder()
                .id(1)
                .nombre("Wifi")
                .descripcion("Internet rápido")
                .icono("wifi-icon")
                .activo(true)
                .build();
    }

    // --- POST /api/servicios ---
    @Test
    void crearServicio_exitoso() {
        when(servicioRepository.existsByNombre(servicioDTO.getName())).thenReturn(false);
        when(servicioMapper.toEntity(servicioDTO)).thenReturn(servicioEntity);
        when(servicioRepository.save(servicioEntity)).thenReturn(servicioEntity);
        when(servicioMapper.toDTO(servicioEntity)).thenReturn(servicioDTO);

        ServicioDTO result = servicioService.crearServicio(servicioDTO);

        assertNotNull(result);
        assertEquals("Wifi", result.getName());
        verify(servicioRepository, times(1)).save(servicioEntity);
    }

    @Test
    void crearServicio_nombreDuplicado_lanzaExcepcion() {
        when(servicioRepository.existsByNombre(servicioDTO.getName())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> servicioService.crearServicio(servicioDTO));
        verify(servicioRepository, never()).save(any());
    }

    // --- GET /api/servicios ---
    @Test
    void listarActivos_exitoso() {
        when(servicioRepository.findByActivoTrue()).thenReturn(Arrays.asList(servicioEntity));
        when(servicioMapper.toDTO(servicioEntity)).thenReturn(servicioDTO);

        List<ServicioDTO> result = servicioService.listarActivos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Wifi", result.get(0).getName());
    }

    // --- GET /api/servicios/{id} ---
    @Test
    void obtenerPorId_exitoso() {
        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicioEntity));
        when(servicioMapper.toDTO(servicioEntity)).thenReturn(servicioDTO);

        ServicioDTO result = servicioService.obtenerPorId(1);

        assertNotNull(result);
        assertEquals("Wifi", result.getName());
        verify(servicioRepository, times(1)).findById(1);
    }

    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(servicioRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicioService.obtenerPorId(1));
    }

    // --- PUT /api/servicios/{id} ---
    @Test
    void actualizarServicio_exitoso() {
        ServicioDTO actualizado = ServicioDTO.builder()
                .name("Piscina")
                .description("Acceso a piscina")
                .icon("pool-icon")
                .active(true)
                .build();

        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicioEntity));
        when(servicioRepository.save(any(ServicioEntity.class))).thenReturn(servicioEntity);
        when(servicioMapper.toDTO(any(ServicioEntity.class))).thenReturn(actualizado);

        ServicioDTO result = servicioService.actualizarServicio(1, actualizado);

        assertNotNull(result);
        assertEquals("Piscina", result.getName());
        verify(servicioRepository, times(1)).save(any(ServicioEntity.class));
    }

    // --- DELETE /api/servicios/{id} ---
    @Test
    void desactivarServicio_exitoso() {
        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicioEntity));

        servicioService.desactivarServicio(1);

        assertFalse(servicioEntity.getActivo());
        verify(servicioRepository, times(1)).save(servicioEntity);
    }

    @Test
    void desactivarServicio_noExiste_lanzaExcepcion() {
        when(servicioRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicioService.desactivarServicio(1));
    }

    // --- Buscar por nombre ---
    @Test
    void buscarPorNombre_exitoso() {
        when(servicioRepository.findByNombre("Wifi")).thenReturn(Optional.of(servicioEntity));
        when(servicioMapper.toDTO(servicioEntity)).thenReturn(servicioDTO);

        ServicioDTO result = servicioService.buscarPorNombre("Wifi");

        assertNotNull(result);
        assertEquals("Wifi", result.getName());
    }

    @Test
    void buscarPorNombre_noExiste_lanzaExcepcion() {
        when(servicioRepository.findByNombre("Piscina")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicioService.buscarPorNombre("Piscina"));
    }
}
