package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.example.Alojamientos.persistenceLayer.entity.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Integer> {

    /**
     * Obtiene todas las notificaciones de un usuario
     */
    List<NotificacionEntity> findByUsuario_Id(Integer idUsuario);

    /**
     * Obtiene todas las notificaciones no leídas de un usuario
     */
    List<NotificacionEntity> findByUsuario_IdAndLeida(Integer idUsuario, Boolean leida);

    /**
     * Cuenta cuántas notificaciones no leídas tiene un usuario
     */
    long countByUsuario_IdAndLeida(Integer idUsuario, Boolean leida);

    List<NotificacionEntity> findByUsuario_IdOrderByFechaCreacionDesc(Integer usuarioId);

    List<NotificacionEntity> findByUsuario_IdAndLeidaFalseOrderByFechaCreacionDesc(Integer usuarioId);

    List<NotificacionEntity> findByUsuario_IdAndLeidaTrueOrderByFechaCreacionDesc(Integer usuarioId);

    Long countByUsuario_IdAndLeidaFalse(Integer usuarioId);
}
