package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    /**
     * Busca todos los pagos asociados a una reserva
     * @param idReserva id de la reserva
     * @return lista de pagos
     */
    List<Pago> findByIdReserva(Integer idReserva);

    /**
     * Busca todos los pagos realizados por un usuario
     * @param idUsuario id del usuario
     * @return lista de pagos
     */
    List<Pago> findByIdUsuario(Integer idUsuario);

    /**
     * Busca un pago por el código de transacción único
     * @param codigoTransaccion código único
     * @return pago encontrado
     */
    Optional<Pago> findByCodigoTransaccion(String codigoTransaccion);

    /**
     * Obtiene los pagos filtrados por estado (ej: PENDIENTE, APROBADO, RECHAZADO)
     * @param estado estado del pago
     * @return lista de pagos en ese estado
     */
    List<Pago> findByEstado(String estado);

    /**
     * Verifica si una reserva ya tiene un pago aprobado
     * @param idReserva id de la reserva
     * @param estado estado del pago
     * @return true si existe un pago con ese estado
     */
    boolean existsByIdReservaAndEstado(Integer idReserva, String estado);
}
