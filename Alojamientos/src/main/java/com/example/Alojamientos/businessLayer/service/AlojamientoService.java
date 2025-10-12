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
     * RN8: Debe tener al menos 1 imagen
     * RN11: Coordenadas geográficas obligatorias
     */
    public AlojamientoDTO crearAlojamiento(AlojamientoDTO dto) {
        // RN8: Validar que tenga imagen principal
        if (dto.getMainImage() == null || dto.getMainImage().trim().isEmpty()) {
            throw new IllegalArgumentException("El alojamiento debe tener al menos una imagen principal");
        }

        // RN11: Validar coordenadas
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new IllegalArgumentException("Las coordenadas geográficas son obligatorias");
        }

        // Validar rango de coordenadas
        if (dto.getLatitude() < -90 || dto.getLatitude() > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90 grados");
        }

        if (dto.getLongitude() < -180 || dto.getLongitude() > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180 grados");
        }

        // Validar precio positivo
        if (dto.getPricePerNight() == null || dto.getPricePerNight() <= 0) {
            throw new IllegalArgumentException("El precio por noche debe ser mayor a 0");
        }

        // Validar capacidad mínima
        if (dto.getMaxCapacity() == null || dto.getMaxCapacity() < 1) {
            throw new IllegalArgumentException("La capacidad máxima debe ser al menos 1 huésped");
        }

        // Validar que el nombre no esté vacío
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del alojamiento es obligatorio");
        }

        // Validar que la descripción no esté vacía
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del alojamiento es obligatoria");
        }

        boolean exists = alojamientoRepository.existsByNombreAndAnfitrion_Id(dto.getName(), dto.getHostId());
        if (exists) {
            throw new IllegalArgumentException("Ya existe un alojamiento con ese nombre para este anfitrión");
        }

        // Convertir y guardar
        AlojamientoEntity entity = alojamientoMapper.toEntity(dto);
        entity.setActivo(true);

        AlojamientoEntity saved = alojamientoRepository.save(entity);

        // TODO: RF9 - Enviar notificación al administrador de nuevo alojamiento

        return alojamientoMapper.toDTO(saved);
    }

    /**
     * RF10, HU-010: Editar alojamiento existente
     */
    public AlojamientoDTO actualizarAlojamiento(Integer id, AlojamientoDTO dto) {
        AlojamientoEntity entity = alojamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + id));

        // RN9: No actualizar si está eliminado
        if (!entity.getActivo()) {
            throw new IllegalArgumentException("No se puede actualizar un alojamiento eliminado");
        }

        // Actualizar campos
        entity.setNombre(dto.getName());
        entity.setDescripcion(dto.getDescription());
        entity.setDireccion(dto.getAddress());
        entity.setCiudad(dto.getCity());
        entity.setPrecioPorNoche(alojamientoMapper.doubleToBigDecimal(dto.getPricePerNight()));
        entity.setCapacidadMaxima(dto.getMaxCapacity());

        AlojamientoEntity updated = alojamientoRepository.save(entity);
        return alojamientoMapper.toDTO(updated);
    }

    /**
     * RF11, HU-011: Eliminar alojamiento (soft delete)
     * RN10: Solo si no tiene reservas futuras
     */
    public void eliminarAlojamiento(Integer id) {
        AlojamientoEntity entity = alojamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + id));

        // RN10: Validar que no tenga reservas futuras o activas
        List<com.example.Alojamientos.persistenceLayer.entity.ReservaEntity> reservasActivas =
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

        // Soft delete
        entity.setActivo(false);
        alojamientoRepository.save(entity);
    }

    /**
     * RF12, HU-012: Listar alojamientos de un anfitrión
     */
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> listarPorAnfitrion(Integer hostId) {
        return alojamientoRepository.findByAnfitrion_Id(hostId).stream()
                .filter(AlojamientoEntity::getActivo) // RN9: No mostrar eliminados
                .map(alojamientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * RF14, HU-014: Buscar alojamientos por ciudad
     * RN25: No mostrar eliminados
     */
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> buscarPorCiudad(String ciudad) {
        return alojamientoRepository.findByCiudadIgnoreCase(ciudad).stream()
                .filter(AlojamientoEntity::getActivo) // RN25: Solo activos
                .map(alojamientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * RF16, HU-016: Filtrar por rango de precio
     */
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> buscarPorRangoPrecio(Double precioMin, Double precioMax) {
        return alojamientoRepository.findByPrecioPorNocheBetween(precioMin, precioMax).stream()
                .filter(AlojamientoEntity::getActivo)
                .map(alojamientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * RF18, HU-018: Obtener alojamiento por ID
     */
    @Transactional(readOnly = true)
    public AlojamientoDTO obtenerPorId(Integer id) {
        AlojamientoEntity entity = alojamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + id));

        if (!entity.getActivo()) {
            throw new IllegalArgumentException("Alojamiento no disponible");
        }

        return alojamientoMapper.toDTO(entity);
    }

    /**
     * RF18, RN26: Listar todos los alojamientos activos (con paginación en controller)
     */
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> listarActivos() {
        return alojamientoRepository.findByActivoTrue().stream()
                .map(alojamientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * RF13, HU-013: Obtener métricas de un alojamiento
     * Retorna número de reservas (implementación básica)
     */
    @Transactional(readOnly = true)
    public Long obtenerNumeroReservas(Integer alojamientoId) {
        return (long) reservaRepository.findByAlojamiento_Id(alojamientoId).size();
    }
}