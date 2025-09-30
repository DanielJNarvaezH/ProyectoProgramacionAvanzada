package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    /**
     * Busca un servicio por su nombre (único)
     * @param nombre nombre del servicio
     * @return Optional<Servicio>
     */
    Optional<Servicio> findByNombre(String nombre);

    /**
     * Lista todos los servicios activos
     * @return List<Servicio>
     */
    List<Servicio> findByActivoTrue();

    /**
     * Verifica si existe un servicio con el nombre dado
     * @param nombre nombre del servicio
     * @return boolean
     */
    boolean existsByNombre(String nombre);
}
