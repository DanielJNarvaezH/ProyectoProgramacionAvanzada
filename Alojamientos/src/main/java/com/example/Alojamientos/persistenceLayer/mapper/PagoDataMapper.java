package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.PagoDTO;
import com.example.Alojamientos.persistenceLayer.entity.PagoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PagoDataMapper {

    @Mapping(target = "reservationId", source = "reserva.id")
    @Mapping(target = "amount", source = "monto")
    @Mapping(target = "method", source = "metodo", qualifiedByName = "metodoPagoToString")
    @Mapping(target = "status", source = "estado", qualifiedByName = "estadoPagoToString")
    @Mapping(target = "externalRef", source = "referenciaExterna")
    PagoDTO toDTO(PagoEntity entity);

    @Mapping(target = "reserva", source = "reservationId", qualifiedByName = "reservationIdToEntity")
    @Mapping(target = "monto", source = "amount", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "metodo", source = "method", qualifiedByName = "stringToMetodoPago")
    @Mapping(target = "estado", source = "status", qualifiedByName = "stringToEstadoPago")
    @Mapping(target = "referenciaExterna", source = "externalRef")
    PagoEntity toEntity(PagoDTO dto);

    @Named("reservationIdToEntity")
    default ReservaEntity reservationIdToEntity(Integer reservationId) {
        if (reservationId == null) return null;
        return ReservaEntity.builder().id(reservationId).build();
    }

    @Named("metodoPagoToString")
    default String metodoPagoToString(PagoEntity.MetodoPago metodo) {
        return metodo != null ? metodo.name() : null;
    }

    @Named("stringToMetodoPago")
    default PagoEntity.MetodoPago stringToMetodoPago(String method) {
        return method != null ? PagoEntity.MetodoPago.valueOf(method.toUpperCase()) : null;
    }

    @Named("estadoPagoToString")
    default String estadoPagoToString(PagoEntity.EstadoPago estado) {
        return estado != null ? estado.name() : null;
    }

    @Named("stringToEstadoPago")
    default PagoEntity.EstadoPago stringToEstadoPago(String status) {
        return status != null ? PagoEntity.EstadoPago.valueOf(status.toUpperCase()) : null;
    }

    @Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}