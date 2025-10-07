package com.example.Alojamientos.businessLayer.service;

import com.example.Alojamientos.businessLayer.dto.PagoDTO;
import com.example.Alojamientos.persistenceLayer.entity.PagoEntity;
import com.example.Alojamientos.persistenceLayer.mapper.PagoDataMapper;
import com.example.Alojamientos.persistenceLayer.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PagoDataMapper pagoMapper;

    /**
     * RN15, RN17: Registrar pago de reserva
     * Funcionalidad opcional de pagos en línea
     */
    public PagoDTO registrarPago(PagoDTO dto) {
        // Validar que no exista pago previo para la reserva
        if (pagoRepository.existsByReserva_Id(dto.getReservationId())) {
            throw new IllegalArgumentException("Ya existe un pago registrado para esta reserva");
        }

        // Validar monto
        if (dto.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        PagoEntity entity = pagoMapper.toEntity(dto);
        entity.setEstado(PagoEntity.EstadoPago.PENDIENTE);

        // Generar referencia externa única
        entity.setReferenciaExterna("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        PagoEntity saved = pagoRepository.save(entity);
        return pagoMapper.toDTO(saved);
    }

    /**
     * Confirmar pago completado
     */
    public PagoDTO confirmarPago(Integer pagoId, String referenciaExterna) {
        PagoEntity entity = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));

        entity.setEstado(PagoEntity.EstadoPago.COMPLETADO);
        entity.setReferenciaExterna(referenciaExterna);

        PagoEntity updated = pagoRepository.save(entity);

        // TODO: Actualizar estado de reserva a CONFIRMADA

        return pagoMapper.toDTO(updated);
    }

    /**
     * Marcar pago como fallido
     */
    public PagoDTO marcarComoFallido(Integer pagoId) {
        PagoEntity entity = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));

        entity.setEstado(PagoEntity.EstadoPago.FALLIDO);
        PagoEntity updated = pagoRepository.save(entity);

        return pagoMapper.toDTO(updated);
    }

    /**
     * Obtener pago por ID
     */
    @Transactional(readOnly = true)
    public PagoDTO obtenerPorId(Integer id) {
        PagoEntity entity = pagoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con id: " + id));
        return pagoMapper.toDTO(entity);
    }

    /**
     * Obtener pago por reserva
     */
    @Transactional(readOnly = true)
    public PagoDTO obtenerPorReserva(Integer reservaId) {
        PagoEntity entity = pagoRepository.findByReserva_Id(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró pago para la reserva"));
        return pagoMapper.toDTO(entity);
    }

    /**
     * Listar todos los pagos
     */
    @Transactional(readOnly = true)
    public List<PagoDTO> listarTodos() {
        return pagoRepository.findAll().stream()
                .map(pagoMapper::toDTO)
                .collect(Collectors.toList());
    }
}