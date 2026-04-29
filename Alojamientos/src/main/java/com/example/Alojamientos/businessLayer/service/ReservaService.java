package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.NotificacionDTO;
import com.example.Alojamientos.businessLayer.dto.ReservaDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.mapper.ReservaDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.AlojamientoRepository;
import com.example.Alojamientos.persistenceLayer.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservaService {

    private final ReservaRepository      reservaRepository;
    private final AlojamientoRepository  alojamientoRepository;
    private final ReservaDataMapper      reservaMapper;
    private final NotificacionService    notificacionService;

    /**
     * RF15, HU-019: Crear una nueva reserva
     * RN13: Fechas futuras y mínimo 1 noche
     * RN14: Respetar capacidad máxima
     * RN15: Estado inicial "CONFIRMADA"
     * RF20: Notificar al anfitrión sobre la nueva reserva
     */
    public ReservaDTO crearReserva(ReservaDTO dto) {
        if (dto.getGuestId() == null)
            throw new IllegalArgumentException("El ID del huésped es obligatorio");

        if (dto.getLodgingId() == null)
            throw new IllegalArgumentException("El ID del alojamiento es obligatorio");

        LocalDate fechaInicio = LocalDate.parse(dto.getStartDate());
        LocalDate fechaFin    = LocalDate.parse(dto.getEndDate());

        if (fechaInicio.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("La fecha de inicio no puede ser anterior a hoy");

        long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        if (noches < 1)
            throw new IllegalArgumentException("La reserva debe ser de al menos 1 noche");

        if (!fechaFin.isAfter(fechaInicio))
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");

        AlojamientoEntity alojamiento = alojamientoRepository.findById(dto.getLodgingId())
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado con id: " + dto.getLodgingId()));

        if (!alojamiento.getActivo())
            throw new IllegalArgumentException("El alojamiento no está disponible");

        if (dto.getNumGuests() == null || dto.getNumGuests() < 1)
            throw new IllegalArgumentException("Debe haber al menos 1 huésped");

        if (dto.getNumGuests() > alojamiento.getCapacidadMaxima())
            throw new IllegalArgumentException(
                    "El número de huéspedes (" + dto.getNumGuests() +
                            ") excede la capacidad máxima del alojamiento (" + alojamiento.getCapacidadMaxima() + ")"
            );

        List<ReservaEntity> reservasExistentes = reservaRepository
                .findByAlojamiento_Id(dto.getLodgingId()).stream()
                .filter(r -> r.getEstado() == ReservaEntity.EstadoReserva.CONFIRMADA ||
                        r.getEstado() == ReservaEntity.EstadoReserva.PENDIENTE)
                .filter(r -> !(fechaFin.isBefore(r.getFechaInicio()) || fechaFin.equals(r.getFechaInicio())) &&
                        !(fechaInicio.isAfter(r.getFechaFin()) || fechaInicio.equals(r.getFechaFin())))
                .collect(Collectors.toList());

        if (!reservasExistentes.isEmpty())
            throw new IllegalArgumentException(
                    "El alojamiento no está disponible en las fechas seleccionadas (" +
                            dto.getStartDate() + " a " + dto.getEndDate() + ")"
            );

        if (dto.getTotalPrice() == null || dto.getTotalPrice() <= 0)
            throw new IllegalArgumentException("El precio total debe ser mayor a 0");

        ReservaEntity entity = reservaMapper.toEntity(dto);
        entity.setEstado(ReservaEntity.EstadoReserva.CONFIRMADA);
        entity.setFechaReserva(LocalDateTime.now());

        ReservaEntity saved = reservaRepository.save(entity);

        // RF20: Notificar al anfitrión sobre la nueva reserva
        try {
            Integer anfitrionId = alojamiento.getAnfitrion().getId();
            notificacionService.crearNotificacion(NotificacionDTO.builder()
                    .userId(anfitrionId)
                    .type("NUEVA_RESERVA")
                    .title("Nueva reserva recibida")
                    .message("Tienes una nueva reserva en \"" + alojamiento.getNombre() +
                            "\" del " + dto.getStartDate() + " al " + dto.getEndDate() +
                            " (" + noches + " noche" + (noches != 1 ? "s" : "") + ")")
                    .read(false)
                    .build());
        } catch (Exception e) {
            log.warn("[ReservaService] No se pudo crear notificación al anfitrión: {}", e.getMessage());
        }

        return reservaMapper.toDTO(saved);
    }

    /**
     * RF21, HU-021: Cancelar reserva
     * RN16: Solo hasta 48 horas antes del check-in
     * RF22: Notificar al anfitrión sobre la cancelación
     */
    public void cancelarReserva(Integer reservaId, String motivo) {
        ReservaEntity entity = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con id: " + reservaId));

        if (entity.getEstado() == ReservaEntity.EstadoReserva.CANCELADA)
            throw new IllegalArgumentException("La reserva ya fue cancelada previamente");

        if (entity.getEstado() == ReservaEntity.EstadoReserva.COMPLETADA)
            throw new IllegalArgumentException("No se puede cancelar una reserva que ya fue completada");

        LocalDateTime checkIn         = entity.getFechaInicio().atStartOfDay();
        long          horasHastaCheckIn = ChronoUnit.HOURS.between(LocalDateTime.now(), checkIn);

        if (horasHastaCheckIn < 48)
            throw new IllegalArgumentException(
                    "No se puede cancelar la reserva. La cancelación debe hacerse con al menos 48 horas de anticipación. " +
                            "Tiempo restante: " + horasHastaCheckIn + " horas"
            );

        if (motivo == null || motivo.trim().isEmpty())
            throw new IllegalArgumentException("Debe proporcionar un motivo de cancelación");

        entity.setEstado(ReservaEntity.EstadoReserva.CANCELADA);
        entity.setFechaCancelacion(LocalDateTime.now());
        entity.setMotivoCancelacion(motivo);

        reservaRepository.save(entity);

        // RF22: Notificar al anfitrión sobre la cancelación
        try {
            Integer anfitrionId = entity.getAlojamiento().getAnfitrion().getId();
            String  nombreAloj  = entity.getAlojamiento().getNombre();
            notificacionService.crearNotificacion(NotificacionDTO.builder()
                    .userId(anfitrionId)
                    .type("CANCELACION_RESERVA")
                    .title("Reserva cancelada")
                    .message("La reserva en \"" + nombreAloj +
                            "\" del " + entity.getFechaInicio() + " al " + entity.getFechaFin() +
                            " fue cancelada. Motivo: " + motivo)
                    .read(false)
                    .build());
        } catch (Exception e) {
            log.warn("[ReservaService] No se pudo crear notificación de cancelación: {}", e.getMessage());
        }
    }

    /**
     * RF23, HU-023: Historial de reservas de un usuario
     * Ordenadas por fechaReserva DESC — la más reciente aparece primero.
     */
    @Transactional(readOnly = true)
    public List<ReservaDTO> listarPorHuesped(Integer huespedId) {
        return reservaRepository.findByHuesped_IdOrderByFechaReservaDesc(huespedId).stream()
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

        if (entity.getFechaFin().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("La reserva aún no ha finalizado");

        entity.setEstado(ReservaEntity.EstadoReserva.COMPLETADA);
        reservaRepository.save(entity);
    }
}