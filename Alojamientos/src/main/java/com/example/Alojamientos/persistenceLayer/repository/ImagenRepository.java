package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.ImagenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagenRepository extends JpaRepository<ImagenEntity, Integer> {

    /**
     * Busca todas las imágenes de un alojamiento específico
     * @param Alojamiento_Id id del alojamiento
     * @return lista de imágenes
     */
    List<ImagenEntity> findByAlojamiento_Id(Integer Alojamiento_Id);

    /**
     * Elimina todas las imágenes asociadas a un alojamiento
     * @param Alojamiento_Id id del alojamiento
     */
    void deleteByAlojamiento_Id(Integer Alojamiento_Id);

    /**
     * Verifica si un alojamiento ya tiene imágenes guardadas
     * @param Alojamiento_Id id del alojamiento
     * @return true si existen imágenes
     */
    boolean existsByAlojamiento_Id(Integer Alojamiento_Id);
}
