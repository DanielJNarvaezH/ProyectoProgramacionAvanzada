package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.FavoritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<FavoritoEntity, Integer> {

    /**
     * Obtiene todos los favoritos de un usuario (sin orden)
     */
    List<FavoritoEntity> findByUsuario_Id(Integer idUsuario);

    /**
     * Obtiene todos los favoritos de un usuario ordenados por fecha (más recientes primero)
     */
    List<FavoritoEntity> findByUsuario_IdOrderByFechaAgregadoDesc(Integer idUsuario);

    /**
     * Verifica si un usuario ya tiene un alojamiento marcado como favorito
     */
    boolean existsByUsuario_IdAndAlojamiento_Id(Integer idUsuario, Integer idAlojamiento);

    /**
     * Elimina un alojamiento de los favoritos de un usuario
     */
    void deleteByUsuario_IdAndAlojamiento_Id(Integer idUsuario, Integer idAlojamiento);

    /**
     * Cuenta cuántas veces un alojamiento ha sido marcado como favorito
     */
    Long countByAlojamiento_Id(Integer alojamientoId);

    /**
     * Busca un favorito específico por usuario y alojamiento
     */
    Optional<FavoritoEntity> findByUsuario_IdAndAlojamiento_Id(Integer usuarioId, Integer alojamientoId);
}
