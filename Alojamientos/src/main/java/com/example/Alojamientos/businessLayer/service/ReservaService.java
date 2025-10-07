package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.ReservaDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ReservaDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoRepository;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final ReservaDataMapper reservaMapper;

    /**
     * RF15, HU-019: Crear una nueva reserva
     * RN13: Fechas futuras y mínimo 1 noche
     * RN14: Respetar capacidad máxima
     * RN15: Estado inicial "CONFIRMADA" (si no requiere aprobación manual)
     */
    public ReservaDTO crearReserva(ReservaDTO dto) {
        // Parsear fechas
        LocalDate fechaInicio = LocalDate.parse(dto.getStartDate());
        LocalDate fechaFin = LocalDate.parse(dto.getEndDate());

        // RN13: Validar fechas futuras
        if (fechaInicio.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden hacer reservas con fechas pasadas");
        }

        // RN13: Validar mínimo 1 noche
        if (ChronoUnit.DAYS.between(fechaInicio, fechaFin) < 1) {
            throw new IllegalArgumentException("La reserva debe ser de al menos 1 noche");
        }

        // Obtener alojamiento
        AlojamientoEntity alojamiento = alojamientoRepository.findById(dto.getLodgingId())
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado"));

        // RN14: Validar capacidad máxima
        if (dto.getNumGuests() > alojamiento.getCapacidadMaxima()) {
            throw new IllegalArgumentException("El número de huéspedes excede la capacidad máxima del alojamiento");
        }

        // Validar disponibilidad (no solapamiento)
        List<ReservaEntity> reservasExistentes = reservaRepository
                .findByAlojamiento_IdAndFechaFinAfterAndFechaInicioBefore(
                        dto.getLodgingId(),
                        fechaInicio,
                        fechaFin
                );

        if (!reservasExistentes.isEmpty()) {
            throw new IllegalArgumentException("El alojamiento no está disponible en las fechas seleccionadas");
        }

        // Crear entidad
        ReservaEntity entity = reservaMapper.toEntity(dto);
        entity.setEstado(ReservaEntity.EstadoReserva.CONFIRMADA); // RN15
        entity.setFechaReserva(LocalDateTime.now());

        ReservaEntity saved = reservaRepository.save(entity);

        // TODO: RF19 - Enviar email de confirmación al huésped
        // TODO: RF20 - Enviar notificación al anfitrión

        return reservaMapper.toDTO(saved);
    }

    /**
     * RF21, HU-021: Cancelar reserva
     * RN16: Solo hasta 48 horas antes del check-in
     */
    public void cancelarReserva(Integer reservaId, String motivo) {
        ReservaEntity entity = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // Validar que no esté ya cancelada o completada
        if (entity.getEstado() == ReservaEntity.EstadoReserva.CANCELADA) {
            throw new IllegalArgumentException("La reserva ya está cancelada");
        }

        if (entity.getEstado() == ReservaEntity.EstadoReserva.COMPLETADA) {
            throw new IllegalArgumentException("No se puede cancelar una reserva completada");
        }

        // RN16: Validar 48 horas antes del check-in
        long horasHastaCheckIn = ChronoUnit.HOURS.between(LocalDateTime.now(), entity.getFechaInicio().atStartOfDay());
        if (horasHastaCheckIn < 48) {
            throw new IllegalArgumentException("No se puede cancelar la reserva con menos de 48 horas de anticipación");
        }

        // Actualizar estado
        entity.setEstado(ReservaEntity.EstadoReserva.CANCELADA);
        entity.setFechaCancelacion(LocalDateTime.now());
        entity.setMotivoCancelacion(motivo);

        reservaRepository.save(entity);

        // TODO: RF22 - Enviar notificación de cancelación al anfitrión
    }

    /**
     * RF23, HU-023: Historial de reservas de un usuario
     */
    @Transactional(readOnly = true)
    public List<ReservaDTO> listarPorHuesped(Integer huespedId) {
        return reservaRepository.findByHuesped_Id(huespedId).stream()
                .map(reservaMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * RF24, HU-024: Reservas de un alojamiento (para anfitrión)
     */
    @Transactional(readOnly = true)
    public List<ReservaDTO> listarPorAlojamiento(Integer alojamientoId) {
        return reservaRepository.findByAlojamiento_Id(alojamientoId).stream()
                .map(reservaMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener reserva por ID
     */
    @Transactional(readOnly = true)
    public ReservaDTO obtenerPorId(Integer id) {
        ReservaEntity entity = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con id: " + id));
        return reservaMapper.toDTO(entity);
    }

    /**
     * RF18: Actualizar estado de reserva a COMPLETADA (después del check-out)
     */
    public void marcarComoCompletada(Integer reservaId) {
        ReservaEntity entity = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (entity.getFechaFin().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La reserva aún no ha finalizado");
        }

        entity.setEstado(ReservaEntity.EstadoReserva.COMPLETADA);
        reservaRepository.save(entity);
    }
}