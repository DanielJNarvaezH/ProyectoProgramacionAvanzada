package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.FavoritoEntity;

import java.util.List;
import java.util.Optional;

public interface FavoritoDao {

    Optional<FavoritoEntity> findById(Integer id);

    FavoritoEntity save(FavoritoEntity favorito);

    void deleteById(Integer id);

    List<FavoritoEntity> findByUsuarioId(Integer usuarioId);

    boolean existsByUsuarioIdAndAlojamientoId(Integer usuarioId, Integer alojamientoId);
    void deleteByUsuarioIdAndAlojamientoId(Integer usuarioId, Integer alojamientoId);

}
