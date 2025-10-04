package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlojamientoRepository extends JpaRepository<AlojamientoEntity, Integer> {

    /**
     * Busca alojamientos por ciudad
     */
    List<AlojamientoEntity> findByCiudadIgnoreCase(String ciudad);

    /**
     * Busca alojamientos por anfitrión (usando el id del anfitrión)
     */
    List<AlojamientoEntity> findByAnfitrion_Id(Integer idAnfitrion);

    /**
     * Obtiene todos los alojamientos activos
     */
    List<AlojamientoEntity> findByActivoTrue();

    /**
     * Busca alojamientos con capacidad mayor o igual al número de huéspedes
     */
    List<AlojamientoEntity> findByCapacidadMaximaGreaterThanEqual(Integer capacidad);

    /**
     * Busca alojamientos por rango de precio
     */
    List<AlojamientoEntity> findByPrecioPorNocheBetween(Double precioMin, Double precioMax);
}