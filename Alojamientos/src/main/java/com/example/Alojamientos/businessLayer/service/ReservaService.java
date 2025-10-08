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
        // Validar que los IDs no sean nulos
        if (dto.getGuestId() == null) {
            throw new IllegalArgumentException("El ID del huésped es obligatorio");
        }

        if (dto.getLodgingId() == null) {
            throw new IllegalArgumentException("El ID del alojamiento es obligatorio");
        }

        // Parsear fechas
        LocalDate fechaInicio = LocalDate.parse(dto.getStartDate());
        LocalDate fechaFin = LocalDate.parse(dto.getEndDate());

        // RN13: Validar fechas futuras
        if (fechaInicio.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser anterior a hoy");
        }

        // RN13: Validar mínimo 1 noche
        long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        if (noches < 1) {
            throw new IllegalArgumentException("La reserva debe ser de al menos 1 noche");
        }

        // Validar que fecha fin sea posterior a fecha inicio
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        // Obtener alojamiento
        AlojamientoEntity alojamiento = alojamientoRepository.findById(dto.getLodgingId())
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + dto.getLodgingId()));

        // Validar que el alojamiento esté activo
        if (!alojamiento.getActivo()) {
            throw new IllegalArgumentException("El alojamiento no está disponible");
        }

        // RN14: Validar capacidad máxima
        if (dto.getNumGuests() == null || dto.getNumGuests() < 1) {
            throw new IllegalArgumentException("Debe haber al menos 1 huésped");
        }

        if (dto.getNumGuests() > alojamiento.getCapacidadMaxima()) {
            throw new IllegalArgumentException(
                    "El número de huéspedes (" + dto.getNumGuests() +
                            ") excede la capacidad máxima del alojamiento (" + alojamiento.getCapacidadMaxima() + ")"
            );
        }

        // Validar disponibilidad (no solapamiento) - considerar solo reservas confirmadas o pendientes
        List<ReservaEntity> reservasExistentes = reservaRepository
                .findByAlojamiento_Id(dto.getLodgingId()).stream()
                .filter(r -> r.getEstado() == ReservaEntity.EstadoReserva.CONFIRMADA ||
                        r.getEstado() == ReservaEntity.EstadoReserva.PENDIENTE)
                .filter(r -> !(fechaFin.isBefore(r.getFechaInicio()) || fechaFin.equals(r.getFechaInicio())) &&
                        !(fechaInicio.isAfter(r.getFechaFin()) || fechaInicio.equals(r.getFechaFin())))
                .collect(Collectors.toList());

        if (!reservasExistentes.isEmpty()) {
            throw new IllegalArgumentException(
                    "El alojamiento no está disponible en las fechas seleccionadas (" +
                            dto.getStartDate() + " a " + dto.getEndDate() + ")"
            );
        }

        // Validar precio total positivo
        if (dto.getTotalPrice() == null || dto.getTotalPrice() <= 0) {
            throw new IllegalArgumentException("El precio total debe ser mayor a 0");
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
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con id: " + reservaId));

        // Validar que no esté ya cancelada
        if (entity.getEstado() == ReservaEntity.EstadoReserva.CANCELADA) {
            throw new IllegalArgumentException("La reserva ya fue cancelada previamente");
        }

        // Validar que no esté completada
        if (entity.getEstado() == ReservaEntity.EstadoReserva.COMPLETADA) {
            throw new IllegalArgumentException("No se puede cancelar una reserva que ya fue completada");
        }

        // RN16: Validar 48 horas antes del check-in
        LocalDateTime checkIn = entity.getFechaInicio().atStartOfDay();
        long horasHastaCheckIn = ChronoUnit.HOURS.between(LocalDateTime.now(), checkIn);

        if (horasHastaCheckIn < 48) {
            throw new IllegalArgumentException(
                    "No se puede cancelar la reserva. La cancelación debe hacerse con al menos 48 horas de anticipación. " +
                            "Tiempo restante: " + horasHastaCheckIn + " horas"
            );
        }

        // Validar motivo
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un motivo de cancelación");
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