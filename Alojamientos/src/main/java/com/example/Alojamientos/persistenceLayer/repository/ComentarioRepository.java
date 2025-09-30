package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    /**
     * Busca todos los comentarios de un alojamiento
     * @param idAlojamiento id del alojamiento
     * @return lista de comentarios
     */
    List<Comentario> findByIdAlojamiento(Integer idAlojamiento);

    /**
     * Busca todos los comentarios de un usuario (huésped o anfitrión)
     * @param idUsuario id del usuario
     * @return lista de comentarios
     */
    List<Comentario> findByIdUsuario(Integer idUsuario);

    /**
     * Obtiene los comentarios de un alojamiento ordenados por fecha de creación descendente
     * (los más recientes primero)
     * @param idAlojamiento id del alojamiento
     * @return lista de comentarios ordenados
     */
    List<Comentario> findByIdAlojamientoOrderByFechaCreacionDesc(Integer idAlojamiento);

    /**
     * Cuenta cuántos comentarios tiene un alojamiento
     * @param idAlojamiento id del alojamiento
     * @return número de comentarios
     */
    long countByIdAlojamiento(Integer idAlojamiento);
}
