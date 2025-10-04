package com.example.Alojamientos.persistenceLayer.dao.impl;

import com.example.Alojamientos.persistenceLayer.dao.ReservaDao;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservaDaoImpl implements ReservaDao {

    private final ReservaRepository reservaRepository;

    @Override
    public Optional<ReservaEntity> findById(Integer id) {
        return reservaRepository.findById(id);
    }

    @Override
    public List<ReservaEntity> findByHuespedId(Integer idHuesped) {
        // El método real en el repository es findByHuesped_Id
        return reservaRepository.findByHuesped_Id(idHuesped);
    }

    @Override
    public List<ReservaEntity> findByAlojamiento(Integer idAlojamiento) {
        return reservaRepository.findByAlojamiento_Id(idAlojamiento);
    }

    @Override
    public List<ReservaEntity> findReservasSolapadas(Integer idAlojamiento, LocalDate fechaInicio, LocalDate fechaFin) {
        return reservaRepository.findByAlojamiento_IdAndFechaFinAfterAndFechaInicioBefore(
                idAlojamiento, fechaInicio, fechaFin
        );
    }

    @Override
    public boolean existsByHuespedIdAndAlojamientoId(Integer idHuesped, Integer idAlojamiento) {
        return reservaRepository.existsByHuesped_IdAndAlojamiento_Id(idHuesped, idAlojamiento);
    }

    @Override
    public ReservaEntity save(ReservaEntity reserva) {
        return reservaRepository.save(reserva);
    }

    @Override
    public void deleteById(Integer id) {
        reservaRepository.deleteById(id);
    }
}
