package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.PromocionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import com.example.Alojamientos.persistenceLayer.entity.PromocionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromocionRepository extends JpaRepository<PromocionEntity, Integer> {

    /**
     * Busca todas las promociones activas en la fecha dada
     * @param fecha fecha de consulta
     * @return lista de promociones vigentes
     */
    @Query("SELECT p FROM PromocionEntity p WHERE p.fechaInicio <= :fecha AND p.fechaFin >= :fecha")
    List<PromocionEntity> findActivasEnFecha(@Param("fecha") LocalDate fecha);

    /**
     * Obtiene las promociones de un alojamiento específico
     * @param idAlojamiento id del alojamiento
     * @return lista de promociones
     */
    List<PromocionEntity> findByAlojamiento_Id(Integer idAlojamiento);

    /**
     * Busca promociones por tipo de descuento (porcentaje, valor fijo, etc.)
     * @param tipoDescuento tipo de descuento
     * @return lista de promociones
     */
    List<PromocionEntity> findByTipoDescuento(String tipoDescuento);


    List<PromocionEntity> findByAlojamiento_IdAndActivaTrueAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            Integer alojamientoId, LocalDate fechaInicio, LocalDate fechaFin);

    Optional<PromocionEntity> findByCodigoPromocionalAndActivaTrue(String codigo);
}
