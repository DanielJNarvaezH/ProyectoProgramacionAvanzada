package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Integer> {

    /**
     * Busca todas las imágenes de un alojamiento específico
     * @param idAlojamiento id del alojamiento
     * @return lista de imágenes
     */
    List<Imagen> findByIdAlojamiento(Integer idAlojamiento);

    /**
     * Elimina todas las imágenes asociadas a un alojamiento
     * @param idAlojamiento id del alojamiento
     */
    void deleteByIdAlojamiento(Integer idAlojamiento);

    /**
     * Verifica si un alojamiento ya tiene imágenes guardadas
     * @param idAlojamiento id del alojamiento
     * @return true si existen imágenes
     */
    boolean existsByIdAlojamiento(Integer idAlojamiento);
}
