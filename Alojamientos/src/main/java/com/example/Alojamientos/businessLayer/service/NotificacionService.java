package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.NotificacionDTO;
import com.example.Alojamientos.persistenceLayer.entity.NotificacionEntity;
import com.example.Alojamientos.persistenceLayer.mapper.NotificacionDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final NotificacionDataMapper notificacionMapper;

    /**
     * RN18: Crear notificación para usuario
     */
    public NotificacionDTO crearNotificacion(NotificacionDTO dto) {
        NotificacionEntity entity = notificacionMapper.toEntity(dto);
        entity.setLeidaentity.setLeida(false);

        NotificacionEntity saved = notificacionRepository.save(entity);

        // TODO: Enviar notificación push o email según configuración del usuario

        return notificacionMapper.toDTO(saved);
    }

    /**
     * Listar notificaciones de un usuario (no leídas primero)
     */
    @Transactional(readOnly = true)
    public List<NotificacionDTO> listarPorUsuario(Integer usuarioId) {
        return notificacionRepository.findByUsuario_IdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(notificacionMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar solo notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public List<NotificacionDTO> listarNoLeidas(Integer usuarioId) {
        return notificacionRepository.findByUsuario_IdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId).stream()
                .map(notificacionMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Marcar notificación como leída
     */
    public NotificacionDTO marcarComoLeida(Integer notificacionId) {
        NotificacionEntity entity = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));

        entity.setLeida(true);
        NotificacionEntity updated = notificacionRepository.save(entity);

        return notificacionMapper.toDTO(updated);
    }

    /**
     * Marcar todas las notificaciones de un usuario como leídas
     */
    public void marcarTodasComoLeidas(Integer usuarioId) {
        List<NotificacionEntity> notificaciones =
                notificacionRepository.findByUsuario_IdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId);

        notificaciones.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(notificaciones);
    }

    /**
     * Obtener notificación por ID
     */
    @Transactional(readOnly = true)
    public NotificacionDTO obtenerPorId(Integer id) {
        NotificacionEntity entity = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada con id: " + id));
        return notificacionMapper.toDTO(entity);
    }

    /**
     * Contar notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public Long contarNoLeidas(Integer usuarioId) {
        return notificacionRepository.countByUsuario_IdAndLeidaFalse(usuarioId);
    }

    /**
     * Eliminar notificación
     */
    public void eliminarNotificacion(Integer id) {
        if (!notificacionRepository.existsById(id)) {
            throw new IllegalArgumentException("Notificación no encontrada");
        }
        notificacionRepository.deleteById(id);
    }

    /**
     * Eliminar todas las notificaciones leídas de un usuario
     */
    public void eliminarLeidasDeUsuario(Integer usuarioId) {
        List<NotificacionEntity> notificaciones =
                notificacionRepository.findByUsuario_IdAndLeidaTrueOrderByFechaCreacionDesc(usuarioId);

        notificacionRepository.deleteAll(notificaciones);
    }
}