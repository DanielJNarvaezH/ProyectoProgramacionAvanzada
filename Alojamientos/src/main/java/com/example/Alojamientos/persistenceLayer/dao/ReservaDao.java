package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaDao {

    Optional<ReservaEntity> findById(Integer id);

    List<ReservaEntity> findByHuesped(Integer idHuesped);

    List<ReservaEntity> findByAlojamiento(Integer idAlojamiento);

    List<ReservaEntity> findReservasSolapadas(Integer idAlojamiento, LocalDate fechaInicio, LocalDate fechaFin);

    boolean existsByHuespedAndAlojamiento(Integer idHuesped, Integer idAlojamiento);

    ReservaEntity save(ReservaEntity reserva);

    void deleteById(Integer id);
}
