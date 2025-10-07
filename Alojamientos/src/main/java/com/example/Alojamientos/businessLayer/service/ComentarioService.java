package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.ComentarioDTO;
import com.example.Alojamientos.persistenceLayer.entity.ComentarioEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ComentarioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.ComentarioRepository;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ReservaRepository reservaRepository;
    private final ComentarioDataMapper comentarioMapper;

    /**
     * RF25, HU-025: Crear comentario y calificación
     * RN19: Solo usuarios con reservas completadas
     * RN20: Máximo 1 comentario por reserva
     * RN21: Calificación entre 1-5
     * RN22: Máximo 500 caracteres
     */
    public ComentarioDTO crearComentario(ComentarioDTO dto) {
        // Obtener reserva
        ReservaEntity reserva = reservaRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // RN19: Validar que la reserva esté completada
        if (reserva.getEstado() != ReservaEntity.EstadoReserva.COMPLETADA) {
            throw new IllegalArgumentException("Solo puedes comentar después de completar la estadía");
        }

        // Validar que la fecha de check-out haya pasado
        if (reserva.getFechaFin().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Solo puedes comentar después del check-out");
        }

        // RN20: Validar que no exista comentario previo para esta reserva
        if (comentarioRepository.existsByReserva_Id(dto.getReservationId())) {
            throw new IllegalArgumentException("Ya existe un comentario para esta reserva");
        }

        // RN21: Validar calificación (ya validada por @Min @Max en DTO, pero reforzamos)
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
        }

        // RN22: Validar longitud texto (ya validada por @Size, pero reforzamos)
        if (dto.getText().length() > 500) {
            throw new IllegalArgumentException("El comentario no puede exceder 500 caracteres");
        }

        // Crear entidad
        ComentarioEntity entity = comentarioMapper.toEntity(dto);
        ComentarioEntity saved = comentarioRepository.save(entity);

        // TODO: RF28 - Enviar notificación al anfitrión de nuevo comentario

        return comentarioMapper.toDTO(saved);
    }

    /**
     * RF27, HU-027: Listar comentarios de un alojamiento
     * Ordenados por fecha (más recientes primero)
     */
    @Transactional(readOnly = true)
    public List<ComentarioDTO> listarPorAlojamiento(Integer alojamientoId) {
        return comentarioRepository.findByAlojamiento_IdOrderByFechaCreacionDesc(alojamientoId).stream()
                .map(comentarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener comentario por ID
     */
    @Transactional(readOnly = true)
    public ComentarioDTO obtenerPorId(Integer id) {
        ComentarioEntity entity = comentarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado con id: " + id));
        return comentarioMapper.toDTO(entity);
    }

    /**
     * RF27: Calcular promedio de calificaciones de un alojamiento
     */
    @Transactional(readOnly = true)
    public Double obtenerPromedioCalificaciones(Integer alojamientoId) {
        List<ComentarioEntity> comentarios = comentarioRepository.findByAlojamiento_IdOrderByFechaCreacionDesc(alojamientoId);

        if (comentarios.isEmpty()) {
            return 0.0;
        }

        double suma = comentarios.stream()
                .mapToInt(ComentarioEntity::getCalificacion)
                .sum();

        return suma / comentarios.size();
    }

    /**
     * Actualizar comentario (solo texto, no calificación)
     */
    public ComentarioDTO actualizarComentario(Integer id, String nuevoTexto) {
        ComentarioEntity entity = comentarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado"));

        if (nuevoTexto.length() > 500) {
            throw new IllegalArgumentException("El comentario no puede exceder 500 caracteres");
        }

        entity.setTexto(nuevoTexto);
        ComentarioEntity updated = comentarioRepository.save(entity);
        return comentarioMapper.toDTO(updated);
    }

    /**
     * RN23, RN24: Eliminar comentario (solo admin o moderación)
     */
    public void eliminarComentario(Integer id) {
        if (!comentarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Comentario no encontrado");
        }
        comentarioRepository.deleteById(id);
    }
}