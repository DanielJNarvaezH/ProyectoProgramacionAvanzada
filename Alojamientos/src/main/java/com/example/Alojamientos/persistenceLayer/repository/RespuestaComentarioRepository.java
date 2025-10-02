package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.RespuestaComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaComentarioRepository extends JpaRepository<RespuestaComentarioEntity, Integer> {

    /**
     * Busca todas las respuestas asociadas a un comentario
     */
    List<RespuestaComentarioEntity> findByComentario_Id(Integer idComentario);

    /**
     * Busca todas las respuestas hechas por un usuario específico
     */
    List<RespuestaComentarioEntity> findByUsuario_Id(Integer idUsuario);

    /**
     * Cuenta cuántas respuestas tiene un comentario
     */
    long countByComentario_Id(Integer idComentario);
}
