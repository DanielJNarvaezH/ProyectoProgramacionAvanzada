package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.ImagenEntity;

import java.util.List;
import java.util.Optional;

public interface ImagenDao {

    Optional<ImagenEntity> findById(Integer id);

    List<ImagenEntity> findByAlojamiento(Integer alojamientoId);

    ImagenEntity save(ImagenEntity imagen);

    void deleteById(Integer id);

    void deleteByAlojamiento(Integer alojamientoId);

    boolean existsByAlojamiento(Integer alojamientoId);
}
