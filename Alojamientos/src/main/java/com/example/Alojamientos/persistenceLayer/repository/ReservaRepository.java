package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Integer> {

    // Reservas por huésped — ordenadas por fecha de reserva descendente (más reciente primero)
    List<ReservaEntity> findByHuesped_IdOrderByFechaReservaDesc(Integer idHuesped);

    // Reservas por alojamiento
    List<ReservaEntity> findByAlojamiento_Id(Integer idAlojamiento);

    // Verificar solapamiento de fechas en un alojamiento
    List<ReservaEntity> findByAlojamiento_IdAndFechaFinAfterAndFechaInicioBefore(
            Integer idAlojamiento,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    // Verificar si un usuario ya tiene reservas en un alojamiento
    boolean existsByHuesped_IdAndAlojamiento_Id(Integer idHuesped, Integer idAlojamiento);

    // ReservaScheduler: reservas CONFIRMADAS cuya fecha de fin ya pasó
    List<ReservaEntity> findByEstadoAndFechaFinBefore(
            ReservaEntity.EstadoReserva estado,
            LocalDate fecha
    );
}