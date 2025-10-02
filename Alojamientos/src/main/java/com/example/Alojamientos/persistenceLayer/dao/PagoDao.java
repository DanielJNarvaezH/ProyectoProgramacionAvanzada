package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.PagoEntity;

import java.util.List;
import java.util.Optional;

public interface PagoDao {

    /**
     * Buscar un pago por su ID.
     */
    Optional<PagoEntity> findById(Integer id);

    /**
     * Guardar o actualizar un pago.
     */
    PagoEntity save(PagoEntity pago);

    /**
     * Eliminar un pago por su ID.
     */
    void deleteById(Integer id);

    /**
     * Listar todos los pagos de una reserva.
     */
    List<PagoEntity> findByReservaId(Integer reservaId);

    /**
     * Listar todos los pagos de un usuario a través de sus reservas.
     */
    List<PagoEntity> findByUsuarioId(Integer usuarioId);

    /**
     * Buscar un pago por su referencia externa.
     */
    Optional<PagoEntity> findByReferenciaExterna(String referenciaExterna);

    /**
     * Listar pagos por estado (PENDIENTE, COMPLETADO, etc).
     */
    List<PagoEntity> findByEstado(PagoEntity.EstadoPago estado);

    /**
     * Verificar si una reserva ya tiene un pago con cierto estado.
     */
    boolean existsByReservaAndEstado(Integer reservaId, PagoEntity.EstadoPago estado);
}
