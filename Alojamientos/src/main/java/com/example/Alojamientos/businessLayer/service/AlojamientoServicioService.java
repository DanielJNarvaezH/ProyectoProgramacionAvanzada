package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.AlojamientoServicioDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoServicioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.AlojamientoServicioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AlojamientoServicioService {

    private final AlojamientoServicioRepository alojamientoServicioRepository;
    private final AlojamientoServicioDataMapper alojamientoServicioMapper;

    /**
     * RF10, RN12: Asociar servicio a alojamiento
     */
    public AlojamientoServicioDTO agregarServicioAAlojamiento(AlojamientoServicioDTO dto) {
        // Validar que no exista ya la relación
        if (alojamientoServicioRepository.existsByAlojamiento_IdAndServicio_Id(
                dto.getLodgingId(), dto.getServiceId())) {
            throw new IllegalArgumentException("El servicio ya está asociado a este alojamiento");
        }

        AlojamientoServicioEntity entity = alojamientoServicioMapper.toEntity(dto);
        entity.setActivo(true);

        AlojamientoServicioEntity saved = alojamientoServicioRepository.save(entity);
        return alojamientoServicioMapper.toDTO(saved);
    }

    /**
     * Listar servicios de un alojamiento
     */
    @Transactional(readOnly = true)
    public List<AlojamientoServicioDTO> listarServiciosDeAlojamiento(Integer alojamientoId) {
        return alojamientoServicioRepository.findByAlojamiento_IdAndActivoTrue(alojamientoId).stream()
                .map(alojamientoServicioMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar alojamientos que tienen un servicio específico
     */
    @Transactional(readOnly = true)
    public List<AlojamientoServicioDTO> listarAlojamientosPorServicio(Integer servicioId) {
        return alojamientoServicioRepository.findByServicio_IdAndActivoTrue(servicioId).stream()
                .map(alojamientoServicioMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener relación por ID
     */
    @Transactional(readOnly = true)
    public AlojamientoServicioDTO obtenerPorId(Integer id) {
        AlojamientoServicioEntity entity = alojamientoServicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Relación no encontrada con id: " + id));
        return alojamientoServicioMapper.toDTO(entity);
    }

    /**
     * Eliminar servicio de alojamiento
     */
    public void eliminarServicioDeAlojamiento(Integer alojamientoId, Integer servicioId) {
        AlojamientoServicioEntity entity = alojamientoServicioRepository
                .findByAlojamiento_IdAndServicio_Id(alojamientoId, servicioId)
                .orElseThrow(() -> new IllegalArgumentException("Relación no encontrada"));

        alojamientoServicioRepository.deleteById(entity.getId());
    }

    /**
     * Desactivar servicio de alojamiento (soft delete)
     */
    public void desactivarServicioDeAlojamiento(Integer id) {
        AlojamientoServicioEntity entity = alojamientoServicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Relación no encontrada"));

        entity.setActivo(false);
        alojamientoServicioRepository.save(entity);
    }

    /**
     * Verificar si un alojamiento tiene un servicio
     */
    @Transactional(readOnly = true)
    public boolean alojamientoTieneServicio(Integer alojamientoId, Integer servicioId) {
        return alojamientoServicioRepository.existsByAlojamiento_IdAndServicio_IdAndActivoTrue(
                alojamientoId, servicioId);
    }
}