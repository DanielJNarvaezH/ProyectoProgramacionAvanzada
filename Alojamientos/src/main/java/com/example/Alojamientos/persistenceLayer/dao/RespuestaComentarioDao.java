package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.RespuestaComentarioEntity;

import java.util.List;
import java.util.Optional;

public interface RespuestaComentarioDao {

    Optional<RespuestaComentarioEntity> findById(Integer id);

    RespuestaComentarioEntity save(RespuestaComentarioEntity respuesta);

    void deleteById(Integer id);

    List<RespuestaComentarioEntity> findByComentarioId(Integer comentarioId);

    List<RespuestaComentarioEntity> findByUsuarioId(Integer usuarioId);

    long countByComentarioId(Integer comentarioId);
}
