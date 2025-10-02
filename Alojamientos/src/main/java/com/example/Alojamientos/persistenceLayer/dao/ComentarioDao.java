package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.ComentarioEntity;

import java.util.List;
import java.util.Optional;

public interface ComentarioDao {

    Optional<ComentarioEntity> findById(Integer id);

    ComentarioEntity save(ComentarioEntity comentario);

    void deleteById(Integer id);

    List<ComentarioEntity> findByAlojamientoId(Integer alojamientoId);

    List<ComentarioEntity> findByUsuarioId(Integer usuarioId);

    List<ComentarioEntity> findByAlojamientoIdOrderByFechaCreacionDesc(Integer alojamientoId);

    long countByAlojamientoId(Integer alojamientoId);
}
