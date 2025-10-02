package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<PagoEntity, Integer> {

    /**
     * Busca el pago asociado a una reserva
     */
    Optional<PagoEntity> findByReserva_Id(Integer idReserva);

    /**
     * Busca un pago por su referencia externa (código único)
     */
    Optional<PagoEntity> findByReferenciaExterna(String referenciaExterna);

    /**
     * Obtiene los pagos filtrados por estado
     */
    List<PagoEntity> findByEstado(PagoEntity.EstadoPago estado);

    /**
     * Verifica si una reserva ya tiene un pago con un estado específico
     */
    boolean existsByReserva_IdAndEstado(Integer idReserva, PagoEntity.EstadoPago estado);
}
