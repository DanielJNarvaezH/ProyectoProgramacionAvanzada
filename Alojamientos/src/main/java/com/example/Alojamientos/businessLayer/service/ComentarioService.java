package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.ComentarioDTO;
import com.example.Alojamientos.businessLayer.dto.NotificacionDTO;
import com.example.Alojamientos.persistenceLayer.entity.ComentarioEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ComentarioDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.ComentarioRepository;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ReservaRepository    reservaRepository;
    private final ComentarioDataMapper comentarioMapper;
    private final NotificacionService  notificacionService;

    /**
     * RF25, HU-025: Crear comentario y calificación
     * RN19: Solo usuarios con reservas completadas
     * RN20: Máximo 1 comentario por reserva
     * RN21: Calificación entre 1-5
     * RN22: Máximo 500 caracteres
     * RF28: Notificar al anfitrión sobre el nuevo comentario
     */
    public ComentarioDTO crearComentario(ComentarioDTO dto) {
        if (dto.getReservationId() == null)
            throw new IllegalArgumentException("El ID de la reserva es obligatorio");

        if (dto.getUserId() == null)
            throw new IllegalArgumentException("El ID del usuario es obligatorio");

        ReservaEntity reserva = reservaRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con id: " + dto.getReservationId()));

        if (reserva.getEstado() != ReservaEntity.EstadoReserva.COMPLETADA)
            throw new IllegalArgumentException(
                    "Solo puedes comentar después de completar la estadía. Estado actual: " + reserva.getEstado()
            );

        if (reserva.getFechaFin().isAfter(LocalDate.now()) || reserva.getFechaFin().equals(LocalDate.now()))
            throw new IllegalArgumentException(
                    "Solo puedes comentar después de la fecha de check-out (" + reserva.getFechaFin() + ")"
            );

        if (comentarioRepository.existsByReserva_Id(dto.getReservationId()))
            throw new IllegalArgumentException("Ya existe un comentario para esta reserva. Solo se permite 1 comentario por reserva.");

        if (dto.getRating() == null)
            throw new IllegalArgumentException("La calificación es obligatoria");

        if (dto.getRating() < 1 || dto.getRating() > 5)
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas");

        if (dto.getText() == null || dto.getText().trim().isEmpty())
            throw new IllegalArgumentException("El comentario no puede estar vacío");

        if (dto.getText().length() > 500)
            throw new IllegalArgumentException(
                    "El comentario no puede exceder 500 caracteres. Actual: " + dto.getText().length()
            );

        if (!reserva.getHuesped().getId().equals(dto.getUserId()))
            throw new IllegalArgumentException("Solo el huésped que realizó la reserva puede comentar");

        ComentarioEntity entity = comentarioMapper.toEntity(dto);
        entity.setAlojamiento(reserva.getAlojamiento());
        ComentarioEntity saved = comentarioRepository.save(entity);

        // RF28: Notificar al anfitrión sobre el nuevo comentario
        try {
            Integer anfitrionId = reserva.getAlojamiento().getAnfitrion().getId();
            String  nombreAloj  = reserva.getAlojamiento().getNombre();
            String  estrellas   = "★".repeat(dto.getRating()) + "☆".repeat(5 - dto.getRating());
            notificacionService.crearNotificacion(NotificacionDTO.builder()
                    .userId(anfitrionId)
                    .type("NUEVO_COMENTARIO")
                    .title("Nuevo comentario recibido")
                    .message("Tu alojamiento \"" + nombreAloj + "\" recibió una reseña " +
                            estrellas + ": \"" + truncar(dto.getText(), 80) + "\"")
                    .read(false)
                    .build());
        } catch (Exception e) {
            log.warn("[ComentarioService] No se pudo crear notificación al anfitrión: {}", e.getMessage());
        }

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
        List<ComentarioEntity> comentarios =
                comentarioRepository.findByAlojamiento_IdOrderByFechaCreacionDesc(alojamientoId);

        if (comentarios.isEmpty()) return 0.0;

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

        if (nuevoTexto.length() > 500)
            throw new IllegalArgumentException("El comentario no puede exceder 500 caracteres");

        entity.setTexto(nuevoTexto);
        return comentarioMapper.toDTO(comentarioRepository.save(entity));
    }

    /**
     * RN23, RN24: Eliminar comentario (solo admin o moderación)
     */
    public void eliminarComentario(Integer id) {
        if (!comentarioRepository.existsById(id))
            throw new IllegalArgumentException("Comentario no encontrado");
        comentarioRepository.deleteById(id);
    }

    // ── Utilidad interna ─────────────────────────────────────────────────
    private String truncar(String texto, int maxLen) {
        if (texto == null) return "";
        return texto.length() <= maxLen ? texto : texto.substring(0, maxLen) + "…";
    }
}