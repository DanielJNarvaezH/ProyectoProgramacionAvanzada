package com.example.Alojamientos.AlojamientoServicio;

import com.example.Alojamientos.businessLayer.dto.AlojamientoServicioDTO;
import com.example.Alojamientos.businessLayer.service.AlojamientoServicioService;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoServicioEntity;
import com.example.Alojamientos.persistenceLayer.entity.ServicioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.AlojamientoServicioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlojamientoServicioServiceTest {

    @Mock
    private AlojamientoServicioRepository alojamientoServicioRepository;

    @Mock
    private AlojamientoServicioDataMapper alojamientoServicioMapper;

    @InjectMocks
    private AlojamientoServicioService alojamientoServicioService;

    private AlojamientoServicioDTO dto;
    private AlojamientoServicioEntity entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        dto = AlojamientoServicioDTO.builder()
                .lodgingId(1)
                .serviceId(2)
                .build();

        entity = AlojamientoServicioEntity.builder()
                .id(10)
                .alojamiento(AlojamientoEntity.builder().id(1).build())
                .servicio(ServicioEntity.builder().id(2).build())
                .activo(true)
                .build();
    }

    // 1️⃣ Agregar servicio correctamente
    @Test
    void agregarServicioAAlojamiento_exitoso() {
        when(alojamientoServicioRepository.existsByAlojamiento_IdAndServicio_Id(1, 2))
                .thenReturn(false);
        when(alojamientoServicioMapper.toEntity(dto)).thenReturn(entity);
        when(alojamientoServicioRepository.save(any())).thenReturn(entity);
        when(alojamientoServicioMapper.toDTO(entity)).thenReturn(dto);

        AlojamientoServicioDTO result = alojamientoServicioService.agregarServicioAAlojamiento(dto);

        assertNotNull(result);
        assertEquals(1, result.getLodgingId());
        verify(alojamientoServicioRepository).save(any());
    }

    // 2️⃣ Intentar agregar duplicado
    @Test
    void agregarServicioAAlojamiento_yaExistente_lanzaExcepcion() {
        when(alojamientoServicioRepository.existsByAlojamiento_IdAndServicio_Id(1, 2))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> alojamientoServicioService.agregarServicioAAlojamiento(dto));
        verify(alojamientoServicioRepository, never()).save(any());
    }

    // 3️⃣ Listar servicios de un alojamiento (con resultados)
    @Test
    void listarServiciosDeAlojamiento_conResultados() {
        when(alojamientoServicioRepository.findByAlojamiento_IdAndActivoTrue(1))
                .thenReturn(List.of(entity));
        when(alojamientoServicioMapper.toDTO(entity)).thenReturn(dto);

        List<AlojamientoServicioDTO> result = alojamientoServicioService.listarServiciosDeAlojamiento(1);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getServiceId());
    }

    // 4️⃣ Listar servicios de un alojamiento (sin resultados)
    @Test
    void listarServiciosDeAlojamiento_sinResultados() {
        when(alojamientoServicioRepository.findByAlojamiento_IdAndActivoTrue(1))
                .thenReturn(Collections.emptyList());

        List<AlojamientoServicioDTO> result = alojamientoServicioService.listarServiciosDeAlojamiento(1);

        assertTrue(result.isEmpty());
    }

    // 5️⃣ Listar alojamientos por servicio (con resultados)
    @Test
    void listarAlojamientosPorServicio_conResultados() {
        when(alojamientoServicioRepository.findByServicio_IdAndActivoTrue(2))
                .thenReturn(List.of(entity));
        when(alojamientoServicioMapper.toDTO(entity)).thenReturn(dto);

        List<AlojamientoServicioDTO> result = alojamientoServicioService.listarAlojamientosPorServicio(2);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getLodgingId());
    }

    // 6️⃣ Listar alojamientos por servicio (sin resultados)
    @Test
    void listarAlojamientosPorServicio_sinResultados() {
        when(alojamientoServicioRepository.findByServicio_IdAndActivoTrue(2))
                .thenReturn(Collections.emptyList());

        List<AlojamientoServicioDTO> result = alojamientoServicioService.listarAlojamientosPorServicio(2);

        assertTrue(result.isEmpty());
    }

    // 7️⃣ Obtener relación existente por ID
    @Test
    void obtenerPorId_exitoso() {
        when(alojamientoServicioRepository.findById(10)).thenReturn(Optional.of(entity));
        when(alojamientoServicioMapper.toDTO(entity)).thenReturn(dto);

        AlojamientoServicioDTO result = alojamientoServicioService.obtenerPorId(10);

        assertNotNull(result);
        assertEquals(2, result.getServiceId());
    }

    // 8️⃣ Obtener relación no existente
    @Test
    void obtenerPorId_noExistente_lanzaExcepcion() {
        when(alojamientoServicioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> alojamientoServicioService.obtenerPorId(99));
    }

    // 9️⃣ Eliminar servicio de alojamiento exitosamente
    @Test
    void eliminarServicioDeAlojamiento_exitoso() {
        when(alojamientoServicioRepository.findByAlojamiento_IdAndServicio_Id(1, 2))
                .thenReturn(Optional.of(entity));

        alojamientoServicioService.eliminarServicioDeAlojamiento(1, 2);

        verify(alojamientoServicioRepository).deleteById(10);
    }

    // 🔟 Eliminar servicio no existente
    @Test
    void eliminarServicioDeAlojamiento_noExistente_lanzaExcepcion() {
        when(alojamientoServicioRepository.findByAlojamiento_IdAndServicio_Id(1, 2))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> alojamientoServicioService.eliminarServicioDeAlojamiento(1, 2));
    }

    // 11️⃣ Desactivar servicio existente
    @Test
    void desactivarServicioDeAlojamiento_exitoso() {
        when(alojamientoServicioRepository.findById(10)).thenReturn(Optional.of(entity));

        alojamientoServicioService.desactivarServicioDeAlojamiento(10);

        assertFalse(entity.getActivo());
        verify(alojamientoServicioRepository).save(entity);
    }

    // 12️⃣ Desactivar servicio inexistente
    @Test
    void desactivarServicioDeAlojamiento_noExistente_lanzaExcepcion() {
        when(alojamientoServicioRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> alojamientoServicioService.desactivarServicioDeAlojamiento(10));
    }

    // 13️⃣ Verificar alojamiento tiene servicio (true)
    @Test
    void alojamientoTieneServicio_true() {
        when(alojamientoServicioRepository.existsByAlojamiento_IdAndServicio_IdAndActivoTrue(1, 2))
                .thenReturn(true);

        boolean result = alojamientoServicioService.alojamientoTieneServicio(1, 2);

        assertTrue(result);
    }

    // 14️⃣ Verificar alojamiento tiene servicio (false)
    @Test
    void alojamientoTieneServicio_false() {
        when(alojamientoServicioRepository.existsByAlojamiento_IdAndServicio_IdAndActivoTrue(1, 2))
                .thenReturn(false);

        boolean result = alojamientoServicioService.alojamientoTieneServicio(1, 2);

        assertFalse(result);
    }

    // 15️⃣ Validar mapper se usa correctamente en agregarServicio
    @Test
    void agregarServicioAAlojamiento_verificaMapperYLlamadas() {
        when(alojamientoServicioRepository.existsByAlojamiento_IdAndServicio_Id(1, 2)).thenReturn(false);
        when(alojamientoServicioMapper.toEntity(dto)).thenReturn(entity);
        when(alojamientoServicioRepository.save(entity)).thenReturn(entity);
        when(alojamientoServicioMapper.toDTO(entity)).thenReturn(dto);

        alojamientoServicioService.agregarServicioAAlojamiento(dto);

        verify(alojamientoServicioMapper).toEntity(dto);
        verify(alojamientoServicioRepository).save(entity);
        verify(alojamientoServicioMapper).toDTO(entity);
    }
}
