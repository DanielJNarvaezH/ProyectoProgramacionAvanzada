package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.RespuestaComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaComentarioRepository extends JpaRepository<RespuestaComentario, Integer> {

    /**
     * Busca todas las respuestas asociadas a un comentario
     * @param idComentario id del comentario
     * @return lista de respuestas
     */
    List<RespuestaComentario> findByIdComentario(Integer idComentario);

    /**
     * Busca todas las respuestas hechas por un usuario específico
     * @param idUsuario id del usuario
     * @return lista de respuestas
     */
    List<RespuestaComentario> findByIdUsuario(Integer idUsuario);

    /**
     * Cuenta cuántas respuestas tiene un comentario
     * @param idComentario id del comentario
     * @return número de respuestas
     */
    long countByIdComentario(Integer idComentario);
}
