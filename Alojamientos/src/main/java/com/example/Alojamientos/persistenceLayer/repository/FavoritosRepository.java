package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.Favoritos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoritosRepository extends JpaRepository<Favoritos, Integer> {

    /**
     * Obtiene todos los favoritos de un usuario
     * @param idUsuario id del usuario
     * @return lista de favoritos
     */
    List<Favoritos> findByIdUsuario(Integer idUsuario);

    /**
     * Busca si un usuario ya tiene un alojamiento en favoritos
     * @param idUsuario id del usuario
     * @param idAlojamiento id del alojamiento
     * @return true si ya está en favoritos
     */
    boolean existsByIdUsuarioAndIdAlojamiento(Integer idUsuario, Integer idAlojamiento);

    /**
     * Elimina un alojamiento de los favoritos de un usuario
     * @param idUsuario id del usuario
     * @param idAlojamiento id del alojamiento
     */
    void deleteByIdUsuarioAndIdAlojamiento(Integer idUsuario, Integer idAlojamiento);
}
