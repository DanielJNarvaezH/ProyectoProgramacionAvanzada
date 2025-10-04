package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;


public interface AlojamientoDao {

    Optional<AlojamientoEntity> findById(Integer id);

    List<AlojamientoEntity> findByCiudad(String ciudad);

    List<AlojamientoEntity> findByAnfitrionId(Integer idAnfitrion);

    List<AlojamientoEntity> findActivos();

    List<AlojamientoEntity> findByCapacidadMinima(Integer capacidad);

    List<AlojamientoEntity> findByRangoPrecio(BigDecimal precioMin, BigDecimal precioMax);

    AlojamientoEntity save(AlojamientoEntity alojamiento);

    void deleteById(Integer id);
}
