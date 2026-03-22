package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.AlojamientoDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.mapper.AlojamientoDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoRepository;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;


@Service
@RequiredArgsConstructor
@Transactional
public class AlojamientoService {

    private final AlojamientoRepository alojamientoRepository;
    private final ReservaRepository reservaRepository;
    private final AlojamientoDataMapper alojamientoMapper;

    /**
     * RF9, HU-009: Crear nuevo alojamiento
     */
    public AlojamientoDTO crearAlojamiento(AlojamientoDTO dto) {
        if (dto.getMainImage() == null || dto.getMainImage().trim().isEmpty()) {
            throw new IllegalArgumentException("El alojamiento debe tener al menos una imagen principal");
        }
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new IllegalArgumentException("Las coordenadas geográficas son obligatorias");
        }
        if (dto.getLatitude() < -90 || dto.getLatitude() > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90 grados");
        }
        if (dto.getLongitude() < -180 || dto.getLongitude() > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180 grados");
        }
        if (dto.getPricePerNight() == null || dto.getPricePerNight() <= 0) {
            throw new IllegalArgumentException("El precio por noche debe ser mayor a 0");
        }
        if (dto.getMaxCapacity() == null || dto.getMaxCapacity() < 1) {
            throw new IllegalArgumentException("La capacidad máxima debe ser al menos 1 huésped");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del alojamiento es obligatorio");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del alojamiento es obligatoria");
        }

        boolean exists = alojamientoRepository.existsByNombreAndAnfitrion_Id(dto.getName(), dto.getHostId());
        if (exists) {
            throw new IllegalArgumentException("Ya existe un alojamiento con ese nombre para este anfitrión");
        }

        AlojamientoEntity entity = alojamientoMapper.toEntity(dto);
        entity.setActivo(true);

        AlojamientoEntity saved = alojamientoRepository.save(entity);
        return alojamientoMapper.toDTO(saved);
    }

    /**
     * RF10, HU-010: Editar alojamiento existente
     * FIX ALOJ-8/ALOJ-11: ahora actualiza TODOS los campos incluyendo
     * imagenPrincipal, latitud, longitud y activo
     */
    public AlojamientoDTO actualizarAlojamiento(Integer id, AlojamientoDTO dto) {
        AlojamientoEntity entity = alojamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + id));

        // Fix-4: solo bloquear si fue soft-deleted, permitir editar inactivos temporales
        if (Boolean.TRUE.equals(entity.getEliminado())) {
            throw new IllegalArgumentException("No se puede actualizar un alojamiento eliminado");
        }

        // Actualizar todos los campos editables
        entity.setNombre(dto.getName());
        entity.setDescripcion(dto.getDescription());
        entity.setDireccion(dto.getAddress());
        entity.setCiudad(dto.getCity());
        entity.setPrecioPorNoche(alojamientoMapper.doubleToBigDecimal(dto.getPricePerNight()));
        entity.setCapacidadMaxima(dto.getMaxCapacity());
        entity.setLatitud(alojamientoMapper.doubleToBigDecimal(dto.getLatitude()));
        entity.setLongitud(alojamientoMapper.doubleToBigDecimal(dto.getLongitude()));
        entity.setActivo(dto.isActive());

        // FIX Bug 2: actualizar imagenPrincipal si viene en el DTO
        if (dto.getMainImage() != null && !dto.getMainImage().isBlank()) {
            entity.setImagenPrincipal(dto.getMainImage());
        }

        AlojamientoEntity updated = alojamientoRepository.save(entity);
        return alojamientoMapper.toDTO(updated);
    }

    /**
     * RF11, HU-011: Eliminar alojamiento (soft delete)
     */
    public void eliminarAlojamiento(Integer id) {
        AlojamientoEntity entity = alojamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + id));

        List<ReservaEntity> reservasActivas =
                reservaRepository.findByAlojamiento_Id(id).stream()
                        .filter(r -> r.getFechaFin().isAfter(LocalDate.now()) || r.getFechaFin().equals(LocalDate.now()))
                        .filter(r -> r.getEstado() == ReservaEntity.EstadoReserva.CONFIRMADA ||
                                r.getEstado() == ReservaEntity.EstadoReserva.PENDIENTE)
                        .collect(Collectors.toList());

        if (!reservasActivas.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el alojamiento porque tiene " + reservasActivas.size() +
                            " reserva(s) activa(s) o futura(s)"
            );
        }

        entity.setActivo(false);
        entity.setEliminado(true);   // Fix-4: marca como soft-deleted, distinto de "pausado"
        alojamientoRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<AlojamientoDTO> listarPorAnfitrion(Integer hostId) {
        return alojamientoRepository.findByAnfitrion_Id(hostId).stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()) && Boolean.FALSE.equals(a.getEliminado()))
                .map(alojamientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ── Fix 4: panel anfitrión — activos e inactivos, EXCLUYE los soft-deleted ──
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> listarTodosPorAnfitrion(Integer hostId) {
        return alojamientoRepository.findByAnfitrion_Id(hostId).stream()
                .filter(a -> Boolean.FALSE.equals(a.getEliminado()))
                .map(alojamientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlojamientoDTO> buscarPorCiudad(String ciudad) {
        return alojamientoRepository.findByCiudadIgnoreCase(ciudad).stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()) && Boolean.FALSE.equals(a.getEliminado()))
                .map(alojamientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlojamientoDTO> buscarPorRangoPrecio(Double precioMin, Double precioMax) {
        return alojamientoRepository.findByPrecioPorNocheBetween(precioMin, precioMax).stream()
                .filter(AlojamientoEntity::getActivo)
                .map(alojamientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlojamientoDTO obtenerPorId(Integer id) {
        AlojamientoEntity entity = alojamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + id));

        // Fix-4: solo bloquear si fue soft-deleted, no si está pausado temporalmente
        if (Boolean.TRUE.equals(entity.getEliminado())) {
            throw new IllegalArgumentException("Alojamiento no disponible");
        }

        return alojamientoMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<AlojamientoDTO> listarActivos() {
        return alojamientoRepository.findByActivoTrue().stream()
                .filter(a -> Boolean.FALSE.equals(a.getEliminado()))
                .map(alojamientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long obtenerNumeroReservas(Integer alojamientoId) {
        if (!alojamientoRepository.existsById(alojamientoId)) {
            throw new IllegalArgumentException("Alojamiento no encontrado con ID: " + alojamientoId);
        }
        return (long) reservaRepository.findByAlojamiento_Id(alojamientoId).size();
    }
}