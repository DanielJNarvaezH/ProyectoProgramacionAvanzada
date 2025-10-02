package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;

import java.util.List;
import java.util.Optional;

public interface AlojamientoDao {

    Optional<AlojamientoEntity> findById(Integer id);

    List<AlojamientoEntity> findByCiudad(String ciudad);

    List<AlojamientoEntity> findByAnfitrion(Integer idAnfitrion);

    List<AlojamientoEntity> findActivos();

    List<AlojamientoEntity> findByCapacidadMinima(Integer capacidad);

    List<AlojamientoEntity> findByRangoPrecio(Double precioMin, Double precioMax);

    AlojamientoEntity save(AlojamientoEntity alojamiento);

    void deleteById(Integer id);
}
