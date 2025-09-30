package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    /**
     * Busca todas las reservas de un usuario (huésped)
     * @param idUsuario id del usuario
     * @return lista de reservas
     */
    List<Reserva> findByIdUsuario(Integer idUsuario);

    /**
     * Busca todas las reservas de un alojamiento
     * @param idAlojamiento id del alojamiento
     * @return lista de reservas
     */
    List<Reserva> findByIdAlojamiento(Integer idAlojamiento);

    /**
     * Busca reservas de un alojamiento que se solapan con un rango de fechas
     * (para verificar disponibilidad)
     * @param idAlojamiento id del alojamiento
     * @param fechaInicio fecha inicial
     * @param fechaFin fecha final
     * @return lista de reservas en conflicto
     */
    List<Reserva> findByIdAlojamientoAndFechaFinAfterAndFechaInicioBefore(
            Integer idAlojamiento,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    /**
     * Verifica si un usuario ya tiene reservas en un alojamiento
     * @param idUsuario id del usuario
     * @param idAlojamiento id del alojamiento
     * @return true si existe
     */
    boolean existsByIdUsuarioAndIdAlojamiento(Integer idUsuario, Integer idAlojamiento);
}
