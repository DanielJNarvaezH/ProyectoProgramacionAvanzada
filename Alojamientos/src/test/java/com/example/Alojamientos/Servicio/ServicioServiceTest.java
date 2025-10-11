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

import java.util.*;

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

    // --- 1 ---
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

    // --- 2 ---
    @Test
    void crearServicio_nombreDuplicado_lanzaExcepcion() {
        when(servicioRepository.existsByNombre(servicioDTO.getName())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> servicioService.crearServicio(servicioDTO));
        verify(servicioRepository, never()).save(any());
    }

    // --- 3 ---
    @Test
    void listarActivos_exitoso() {
        when(servicioRepository.findByActivoTrue()).thenReturn(Arrays.asList(servicioEntity));
        when(servicioMapper.toDTO(servicioEntity)).thenReturn(servicioDTO);

        List<ServicioDTO> result = servicioService.listarActivos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Wifi", result.get(0).getName());
    }

    // --- 4 ---
    @Test
    void listarActivos_listaVacia() {
        when(servicioRepository.findByActivoTrue()).thenReturn(Collections.emptyList());
        List<ServicioDTO> result = servicioService.listarActivos();
        assertTrue(result.isEmpty());
    }

    // --- 5 ---
    @Test
    void obtenerPorId_exitoso() {
        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicioEntity));
        when(servicioMapper.toDTO(servicioEntity)).thenReturn(servicioDTO);

        ServicioDTO result = servicioService.obtenerPorId(1);

        assertNotNull(result);
        assertEquals("Wifi", result.getName());
        verify(servicioRepository, times(1)).findById(1);
    }

    // --- 6 ---
    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(servicioRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> servicioService.obtenerPorId(1));
    }

    // --- 7 ---
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

    // --- 8 ---
    @Test
    void actualizarServicio_noExiste_lanzaExcepcion() {
        when(servicioRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> servicioService.actualizarServicio(1, servicioDTO));
    }

    // --- 9 ---
    @Test
    void desactivarServicio_exitoso() {
        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicioEntity));
        servicioService.desactivarServicio(1);
        assertFalse(servicioEntity.getActivo());
        verify(servicioRepository, times(1)).save(servicioEntity);
    }

    // --- 10 ---
    @Test
    void desactivarServicio_noExiste_lanzaExcepcion() {
        when(servicioRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> servicioService.desactivarServicio(1));
    }

    // --- 11 ---
    @Test
    void buscarPorNombre_exitoso() {
        when(servicioRepository.findByNombre("Wifi")).thenReturn(Optional.of(servicioEntity));
        when(servicioMapper.toDTO(servicioEntity)).thenReturn(servicioDTO);

        ServicioDTO result = servicioService.buscarPorNombre("Wifi");

        assertNotNull(result);
        assertEquals("Wifi", result.getName());
    }

    // --- 12 ---
    @Test
    void buscarPorNombre_noExiste_lanzaExcepcion() {
        when(servicioRepository.findByNombre("Piscina")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> servicioService.buscarPorNombre("Piscina"));
    }


    // --- 14 ---
    @Test
    void actualizarServicio_sinCambiosMantieneDatos() {
        when(servicioRepository.findById(1)).thenReturn(Optional.of(servicioEntity));
        when(servicioRepository.save(servicioEntity)).thenReturn(servicioEntity);
        when(servicioMapper.toDTO(servicioEntity)).thenReturn(servicioDTO);

        ServicioDTO result = servicioService.actualizarServicio(1, servicioDTO);

        assertEquals("Wifi", result.getName());
        verify(servicioRepository, times(1)).save(servicioEntity);
    }

    // --- 15 ---
    @Test
    void listarTodos_incluyeActivosEInactivos() {
        ServicioEntity inactivo = ServicioEntity.builder()
                .id(2)
                .nombre("Televisión")
                .descripcion("TV por cable")
                .icono("tv-icon")
                .activo(false)
                .build();

        when(servicioRepository.findAll()).thenReturn(Arrays.asList(servicioEntity, inactivo));
        when(servicioMapper.toDTO(any(ServicioEntity.class))).thenReturn(servicioDTO);

        List<ServicioDTO> result = servicioService.listarTodos();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(servicioRepository, times(1)).findAll();
    }
}
