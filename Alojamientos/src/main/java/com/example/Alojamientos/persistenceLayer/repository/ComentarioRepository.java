package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.ComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<ComentarioEntity, Integer> {

    /**
     * Busca todos los comentarios de un alojamiento
     */
    List<ComentarioEntity> findByAlojamiento_Id(Integer idAlojamiento);

    /**
     * Busca todos los comentarios de un usuario (huésped o anfitrión)
     */
    List<ComentarioEntity> findByUsuario_Id(Integer idUsuario);

    /**
     * Obtiene los comentarios de un alojamiento ordenados por fecha de creación descendente
     */
    List<ComentarioEntity> findByAlojamiento_IdOrderByFechaCreacionDesc(Integer idAlojamiento);

    /**
     * Cuenta cuántos comentarios tiene un alojamiento
     */
    long countByAlojamiento_Id(Integer idAlojamiento);
}
