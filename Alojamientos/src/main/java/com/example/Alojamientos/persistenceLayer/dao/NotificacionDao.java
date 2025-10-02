package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.NotificacionEntity;

import java.util.List;
import java.util.Optional;

public interface NotificacionDao {

    Optional<NotificacionEntity> findById(Integer id);

    NotificacionEntity save(NotificacionEntity notificacion);

    void deleteById(Integer id);

    List<NotificacionEntity> findByUsuarioId(Integer usuarioId);

    List<NotificacionEntity> findByUsuarioIdAndLeidaFalse(Integer usuarioId);

    long countByUsuarioIdAndLeidaFalse(Integer usuarioId);
}
