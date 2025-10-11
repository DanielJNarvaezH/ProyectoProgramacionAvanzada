package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.ServicioDTO;
import com.example.Alojamientos.persistenceLayer.entity.ServicioEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ServicioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ServicioDataMapper servicioMapper;

    /**
     * RF10, RN12: Crear nuevo servicio
     * Solo administrador puede agregar servicios nuevos
     */
    public ServicioDTO crearServicio(ServicioDTO dto) {
        // Validar nombre único
        if (servicioRepository.existsByNombre(dto.getName())) {
            throw new IllegalArgumentException("Ya existe un servicio con ese nombre");
        }

        ServicioEntity entity = servicioMapper.toEntity(dto);
        entity.setActivo(true);

        ServicioEntity saved = servicioRepository.save(entity);
        return servicioMapper.toDTO(saved);
    }

    /**
     * Listar todos los servicios activos
     */
    @Transactional(readOnly = true)
    public List<ServicioDTO> listarActivos() {
        return servicioRepository.findByActivoTrue().stream()
                .map(servicioMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener servicio por ID
     */
    @Transactional(readOnly = true)
    public ServicioDTO obtenerPorId(Integer id) {
        ServicioEntity entity = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con id: " + id));
        return servicioMapper.toDTO(entity);
    }

    /**
     * Actualizar servicio
     */
    public ServicioDTO actualizarServicio(Integer id, ServicioDTO dto) {
        ServicioEntity entity = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        entity.setNombre(dto.getName());
        entity.setDescripcion(dto.getDescription());
        entity.setIcono(dto.getIcon());

        ServicioEntity updated = servicioRepository.save(entity);
        return servicioMapper.toDTO(updated);
    }

    /**
     * Desactivar servicio (soft delete)
     */
    public void desactivarServicio(Integer id) {
        ServicioEntity entity = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        entity.setActivo(false);
        servicioRepository.save(entity);
    }

    /**
     * Buscar servicio por nombre
     */
    @Transactional(readOnly = true)
    public ServicioDTO buscarPorNombre(String nombre) {
        ServicioEntity entity = servicioRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con nombre: " + nombre));
        return servicioMapper.toDTO(entity);
    }
    /**
     * Listar todos los servicios (activos e inactivos)
     */
    @Transactional(readOnly = true)
    public List<ServicioDTO> listarTodos() {
        return servicioRepository.findAll().stream()
                .map(servicioMapper::toDTO)
                .collect(Collectors.toList());
    }

}