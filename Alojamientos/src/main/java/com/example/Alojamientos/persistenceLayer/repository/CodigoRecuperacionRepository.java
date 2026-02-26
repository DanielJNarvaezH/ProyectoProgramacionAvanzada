package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.CodigoRecuperacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacionEntity, Long> {

    // Usado por CodigoRecuperacionService
    List<CodigoRecuperacionEntity> findByUsuario_IdAndUsadoFalse(Integer usuarioId);

    Optional<CodigoRecuperacionEntity> findByCodigoAndUsuario_IdAndUsadoFalse(String codigo, Integer usuarioId);

    List<CodigoRecuperacionEntity> findByFechaExpiracionBeforeAndUsadoFalse(Timestamp fechaExpiracion);

    // Usado por AuthService
    Optional<CodigoRecuperacionEntity> findTopByCorreoAndUsadoFalseOrderByFechaExpiracionDesc(String correo);

    void deleteAllByCorreo(String correo);
}