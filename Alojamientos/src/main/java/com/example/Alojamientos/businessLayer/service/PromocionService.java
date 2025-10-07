package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.PromocionDTO;
import com.example.Alojamientos.persistenceLayer.entity.PromocionEntity;
import com.example.Alojamientos.persistenceLayer.mapper.PromocionDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.PromocionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PromocionService {

    private final PromocionRepository promocionRepository;
    private final PromocionDataMapper promocionMapper;

    /**
     * Crear nueva promoción (funcionalidad opcional)
     */
    public PromocionDTO crearPromocion(PromocionDTO dto) {
        // Validar fechas
        LocalDate fechaInicio = LocalDate.parse(dto.getStartDate());
        LocalDate fechaFin = LocalDate.parse(dto.getEndDate());

        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        // Validar valor de descuento
        if (dto.getDiscountValue() <= 0) {
            throw new IllegalArgumentException("El valor del descuento debe ser mayor a 0");
        }

        PromocionEntity entity = promocionMapper.toEntity(dto);
        entity.setActiva(true);

        PromocionEntity saved = promocionRepository.save(entity);
        return promocionMapper.toDTO(saved);
    }

    /**
     * Listar promociones activas de un alojamiento
     */
    @Transactional(readOnly = true)
    public List<PromocionDTO> listarPromocionesActivas(Integer alojamientoId) {
        LocalDate hoy = LocalDate.now();
        return promocionRepository.findByAlojamiento_IdAndActivaTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        alojamientoId, hoy, hoy
                ).stream()
                .map(promocionMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener promoción por ID
     */
    @Transactional(readOnly = true)
    public PromocionDTO obtenerPorId(Integer id) {
        PromocionEntity entity = promocionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada con id: " + id));
        return promocionMapper.toDTO(entity);
    }

    /**
     * Buscar promoción por código
     */
    @Transactional(readOnly = true)
    public PromocionDTO buscarPorCodigo(String codigo) {
        PromocionEntity entity = promocionRepository.findByCodigoPromocionalAndActivaTrue(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Código de promoción no válido"));

        // Validar que esté vigente
        LocalDate hoy = LocalDate.now();
        if (entity.getFechaInicio().isAfter(hoy) || entity.getFechaFin().isBefore(hoy)) {
            throw new IllegalArgumentException("La promoción ha expirado");
        }

        return promocionMapper.toDTO(entity);
    }

    /**
     * Actualizar promoción
     */
    public PromocionDTO actualizarPromocion(Integer id, PromocionDTO dto) {
        PromocionEntity entity = promocionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));

        entity.setNombre(dto.getName());
        entity.setDescripcion(dto.getDescription());
        entity.setValorDescuento(promocionMapper.doubleToBigDecimal(dto.getDiscountValue()));

        PromocionEntity updated = promocionRepository.save(entity);
        return promocionMapper.toDTO(updated);
    }

    /**
     * Desactivar promoción
     */
    public void desactivarPromocion(Integer id) {
        PromocionEntity entity = promocionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoción no encontrada"));

        entity.setActiva(false);
        promocionRepository.save(entity);
    }
}