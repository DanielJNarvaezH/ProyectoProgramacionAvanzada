package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.FavoritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.example.Alojamientos.persistenceLayer.entity.FavoritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<FavoritoEntity, Integer> {

    /**
     * Obtiene todos los favoritos de un usuario
     */
    List<FavoritoEntity> findByUsuario_Id(Integer idUsuario);

    /**
     * Busca si un usuario ya tiene un alojamiento en favoritos
     */
    boolean existsByUsuario_IdAndAlojamiento_Id(Integer idUsuario, Integer idAlojamiento);

    /**
     * Elimina un alojamiento de los favoritos de un usuario
     */
    void deleteByUsuario_IdAndAlojamiento_Id(Integer idUsuario, Integer idAlojamiento);


    Long countByAlojamiento_Id(Integer alojamientoId);

    Optional<FavoritoEntity> findByUsuario_IdAndAlojamiento_Id(Integer usuarioId, Integer alojamientoId);
}
