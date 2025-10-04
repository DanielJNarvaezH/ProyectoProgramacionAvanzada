package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.AlojamientoServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlojamientoServicioRepository extends JpaRepository<AlojamientoServicioEntity, Integer> {

    /**
     * Busca todos los servicios de un alojamiento específico
     * @param idAlojamiento id del alojamiento
     * @return lista de relaciones alojamiento-servicio
     */
    List<AlojamientoServicioEntity> findByIdAlojamiento(Integer idAlojamiento);

    /**
     * Busca todos los alojamientos que tengan un servicio específico
     * @param Servicio_Id id del servicio
     * @return lista de relaciones alojamiento-servicio
     */
    List<AlojamientoServicioEntity> findByServicio_Id(Integer Servicio_Id);

    /**
     * Verifica si un servicio ya está asignado a un alojamiento
     * @param Alojamiento_Id id del alojamiento
     * @param Servicio_Id id del servicio
     * @return true si ya existe la relación
     */
    boolean existsByAlojamiento_IdAndServicio_Id(Integer Alojamiento_Id, Integer Servicio_Id);

    /**
     * Elimina la relación entre un alojamiento y un servicio
     */
    void deleteByAlojamiento_IdAndServicio_Id(Integer Alojamiento_Id, Integer Servicio_Id);
}
