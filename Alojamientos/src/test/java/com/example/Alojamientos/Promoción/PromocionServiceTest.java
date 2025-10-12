package com.example.Alojamientos.Promoción;

import com.example.Alojamientos.businessLayer.dto.PromocionDTO;
import com.example.Alojamientos.businessLayer.service.PromocionService;
import com.example.Alojamientos.persistenceLayer.entity.PromocionEntity;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.mapper.PromocionDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.PromocionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PromocionServiceTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private PromocionDataMapper promocionMapper;

    @InjectMocks
    private PromocionService promocionService;

    private PromocionEntity entity;
    private PromocionDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        entity = PromocionEntity.builder()
                .id(1)
                .alojamiento(AlojamientoEntity.builder().id(10).build())
                .nombre("Promo Octubre")
                .descripcion("Descuento del 10%")
                .tipoDescuento(PromocionEntity.TipoDescuento.PORCENTAJE)
                .valorDescuento(BigDecimal.valueOf(10))
                .fechaInicio(LocalDate.now().minusDays(1))
                .fechaFin(LocalDate.now().plusDays(5))
                .activa(true)
                .build();

        dto = PromocionDTO.builder()
                .lodgingId(10)
                .name("Promo Octubre")
                .description("Descuento del 10%")
                .discountType("PORCENTAJE")
                .discountValue(10.0)
                .startDate(LocalDate.now().minusDays(1).toString())
                .endDate(LocalDate.now().plusDays(5).toString())
                .active(true)
                .build();
    }

    // 1. Crear promoción válida
    @Test
    void crearPromocion_valida() {
        when(promocionMapper.toEntity(dto)).thenReturn(entity);
        when(promocionRepository.save(entity)).thenReturn(entity);
        when(promocionMapper.toDTO(entity)).thenReturn(dto);

        PromocionDTO result = promocionService.crearPromocion(dto);

        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        verify(promocionRepository, times(1)).save(entity);
    }

    // 2. Crear promoción con fecha fin anterior (error)
    @Test
    void crearPromocion_fechaInvalidaDebeFallar() {
        dto.setStartDate(LocalDate.now().toString());
        dto.setEndDate(LocalDate.now().minusDays(1).toString());

        assertThrows(IllegalArgumentException.class, () -> promocionService.crearPromocion(dto));
        verify(promocionRepository, never()).save(any());
    }

    // 3. Crear promoción con descuento <= 0 (error)
    @Test
    void crearPromocion_descuentoInvalidoDebeFallar() {
        dto.setDiscountValue(0.0);
        assertThrows(IllegalArgumentException.class, () -> promocionService.crearPromocion(dto));
    }

    // 4. Listar promociones activas (con resultados)
    @Test
    void listarPromocionesActivas_conResultados() {
        when(promocionRepository.findByAlojamiento_IdAndActivaTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                anyInt(), any(), any())).thenReturn(List.of(entity));
        when(promocionMapper.toDTO(any())).thenReturn(dto);

        var result = promocionService.listarPromocionesActivas(10);
        assertEquals(1, result.size());
    }

    // 5. Listar promociones activas (sin resultados)
    @Test
    void listarPromocionesActivas_sinResultados() {
        when(promocionRepository.findByAlojamiento_IdAndActivaTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                anyInt(), any(), any())).thenReturn(Collections.emptyList());

        var result = promocionService.listarPromocionesActivas(99);
        assertTrue(result.isEmpty());
    }

    // 6. Obtener promoción por ID existente
    @Test
    void obtenerPorId_existente() {
        when(promocionRepository.findById(1)).thenReturn(Optional.of(entity));
        when(promocionMapper.toDTO(entity)).thenReturn(dto);

        var result = promocionService.obtenerPorId(1);
        assertEquals(dto.getName(), result.getName());
    }

    // 7. Obtener promoción por ID inexistente
    @Test
    void obtenerPorId_inexistenteDebeFallar() {
        when(promocionRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> promocionService.obtenerPorId(99));
    }

    // 8. Buscar promoción por código válida
    @Test
    void buscarPorCodigo_valida() {
        entity.setCodigoPromocional("OCT10");
        when(promocionRepository.findByCodigoPromocionalAndActivaTrue("OCT10"))
                .thenReturn(Optional.of(entity));
        when(promocionMapper.toDTO(entity)).thenReturn(dto);

        var result = promocionService.buscarPorCodigo("OCT10");
        assertNotNull(result);
    }

    // 9. Buscar promoción expirada (error)
    @Test
    void buscarPorCodigo_expiradaDebeFallar() {
        entity.setFechaFin(LocalDate.now().minusDays(1));
        when(promocionRepository.findByCodigoPromocionalAndActivaTrue("OCT10"))
                .thenReturn(Optional.of(entity));

        assertThrows(IllegalArgumentException.class, () -> promocionService.buscarPorCodigo("OCT10"));
    }

    // 10. Buscar promoción con código inexistente
    @Test
    void buscarPorCodigo_inexistenteDebeFallar() {
        when(promocionRepository.findByCodigoPromocionalAndActivaTrue("XXX")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> promocionService.buscarPorCodigo("XXX"));
    }

    // 11. Actualizar promoción existente
    @Test
    void actualizarPromocion_existente() {
        when(promocionRepository.findById(1)).thenReturn(Optional.of(entity));
        when(promocionMapper.doubleToBigDecimal(dto.getDiscountValue()))
                .thenReturn(BigDecimal.valueOf(10));
        when(promocionRepository.save(any())).thenReturn(entity);
        when(promocionMapper.toDTO(entity)).thenReturn(dto);

        var result = promocionService.actualizarPromocion(1, dto);
        assertEquals(dto.getName(), result.getName());
        verify(promocionRepository, times(1)).save(any());
    }

    // 12. Actualizar promoción inexistente
    @Test
    void actualizarPromocion_inexistenteDebeFallar() {
        when(promocionRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> promocionService.actualizarPromocion(99, dto));
    }

    // 13. Desactivar promoción existente
    @Test
    void desactivarPromocion_existente() {
        when(promocionRepository.findById(1)).thenReturn(Optional.of(entity));
        promocionService.desactivarPromocion(1);
        assertFalse(entity.getActiva());
        verify(promocionRepository, times(1)).save(entity);
    }

    // 14. Desactivar promoción inexistente
    @Test
    void desactivarPromocion_inexistenteDebeFallar() {
        when(promocionRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> promocionService.desactivarPromocion(99));
    }

    // 15. Validar mapeo DTO <-> Entity con mocks
    @Test
    void mapper_conversionBasica() {
        when(promocionMapper.toEntity(dto)).thenReturn(entity);
        when(promocionMapper.toDTO(entity)).thenReturn(dto);

        PromocionEntity mappedEntity = promocionMapper.toEntity(dto);
        PromocionDTO mappedDto = promocionMapper.toDTO(entity);

        assertEquals(dto.getName(), mappedDto.getName());
        assertEquals(entity.getNombre(), mappedEntity.getNombre());
    }
}
