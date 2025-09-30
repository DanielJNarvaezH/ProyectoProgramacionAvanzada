package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    /**
     * Obtiene todas las notificaciones de un usuario
     * @param idUsuario id del usuario
     * @return lista de notificaciones
     */
    List<Notificacion> findByIdUsuario(Integer idUsuario);

    /**
     * Obtiene todas las notificaciones no leídas de un usuario
     * @param idUsuario id del usuario
     * @param leida estado de lectura
     * @return lista de notificaciones
     */
    List<Notificacion> findByIdUsuarioAndLeida(Integer idUsuario, Boolean leida);

    /**
     * Cuenta cuántas notificaciones no leídas tiene un usuario
     * @param idUsuario id del usuario
     * @param leida estado de lectura
     * @return cantidad de notificaciones pendientes
     */
    long countByIdUsuarioAndLeida(Integer idUsuario, Boolean leida);
}
