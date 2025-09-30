package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.CodigoRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Integer> {

    /**
     * Busca un código activo y no usado por su valor
     * @param codigo código de recuperación
     * @return Optional<CodigoRecuperacion>
     */
    Optional<CodigoRecuperacion> findByCodigoAndUsadoFalse(String codigo);

    /**
     * Lista los códigos de un usuario que aún no han sido usados
     * @param idUsuario id del usuario
     * @return List<CodigoRecuperacion>
     */
    List<CodigoRecuperacion> findByIdUsuarioAndUsadoFalse(Integer idUsuario);

    /**
     * Elimina los códigos expirados
     * @param fechaActual fecha actual (usualmente NOW() en SQL)
     */
    void deleteByFechaExpiracionBefore(java.time.LocalDateTime fechaActual);
}
