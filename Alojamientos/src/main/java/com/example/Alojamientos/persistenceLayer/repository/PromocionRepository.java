package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    /**
     * Busca todas las promociones activas en la fecha actual
     * @param fecha fecha de consulta
     * @return lista de promociones vigentes
     */
    List<Promocion> findByFechaInicioBeforeAndFechaFinAfter(LocalDate fecha);

    /**
     * Obtiene las promociones de un alojamiento específico
     * @param idAlojamiento id del alojamiento
     * @return lista de promociones
     */
    List<Promocion> findByIdAlojamiento(Integer idAlojamiento);

    /**
     * Busca promociones por tipo de descuento (porcentaje, valor fijo, etc.)
     * @param tipoDescuento tipo de descuento
     * @return lista de promociones
     */
    List<Promocion> findByTipoDescuento(String tipoDescuento);
}
