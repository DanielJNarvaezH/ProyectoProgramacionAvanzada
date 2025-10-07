package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.AlojamientoServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.example.Alojamientos.persistenceLayer.entity.AlojamientoServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlojamientoServicioRepository extends JpaRepository<AlojamientoServicioEntity, Integer> {

    /**
     * Busca todos los servicios de un alojamiento específico
     * @param idAlojamiento id del alojamiento
     * @return lista de relaciones alojamiento-servicio
     */
    List<AlojamientoServicioEntity> findByAlojamiento_Id(Integer idAlojamiento);

    /**
     * Busca todos los alojamientos que tengan un servicio específico
     * @param idServicio id del servicio
     * @return lista de relaciones alojamiento-servicio
     */
    List<AlojamientoServicioEntity> findByServicio_Id(Integer idServicio);

    /**
     * Verifica si un servicio ya está asignado a un alojamiento
     * @param idAlojamiento id del alojamiento
     * @param idServicio id del servicio
     * @return true si ya existe la relación
     */
    boolean existsByAlojamiento_IdAndServicio_Id(Integer idAlojamiento, Integer idServicio);

    /**
     * Elimina la relación entre un alojamiento y un servicio
     */
    void deleteByAlojamiento_IdAndServicio_Id(Integer idAlojamiento, Integer idServicio);



    List<AlojamientoServicioEntity> findByAlojamiento_IdAndActivoTrue(Integer alojamientoId);

    List<AlojamientoServicioEntity> findByServicio_IdAndActivoTrue(Integer servicioId);

    Optional<AlojamientoServicioEntity> findByAlojamiento_IdAndServicio_Id(Integer alojamientoId, Integer servicioId);

    boolean existsByAlojamiento_IdAndServicio_IdAndActivoTrue(Integer alojamientoId, Integer servicioId);
    //
}