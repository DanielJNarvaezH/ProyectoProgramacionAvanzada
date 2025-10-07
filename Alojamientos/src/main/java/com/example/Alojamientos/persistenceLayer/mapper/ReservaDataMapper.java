package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.ReservaDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservaDataMapper {

    @Mapping(target = "guestId", source = "huesped.id")
    @Mapping(target = "lodgingId", source = "alojamiento.id")
    @Mapping(target = "startDate", source = "fechaInicio", qualifiedByName = "localDateToString")
    @Mapping(target = "endDate", source = "fechaFin", qualifiedByName = "localDateToString")
    @Mapping(target = "numGuests", source = "numHuespedes")
    @Mapping(target = "totalPrice", source = "precioTotal")
    @Mapping(target = "status", source = "estado", qualifiedByName = "estadoToString")
    @Mapping(target = "cancelDate", source = "fechaCancelacion", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "cancelReason", source = "motivoCancelacion")
    ReservaDTO toDTO(ReservaEntity entity);

    @Mapping(target = "huesped", source = "guestId", qualifiedByName = "guestIdToEntity")
    @Mapping(target = "alojamiento", source = "lodgingId", qualifiedByName = "lodgingIdToEntity")
    @Mapping(target = "fechaInicio", source = "startDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "fechaFin", source = "endDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "numHuespedes", source = "numGuests")
    @Mapping(target = "precioTotal", source = "totalPrice", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "estado", source = "status", qualifiedByName = "stringToEstado")
    @Mapping(target = "motivoCancelacion", source = "cancelReason")
    ReservaEntity toEntity(ReservaDTO dto);

    @Named("guestIdToEntity")
    default UsuarioEntity guestIdToEntity(Integer guestId) {
        if (guestId == null) return null;
        return UsuarioEntity.builder().id(guestId).build();
    }

    @Named("lodgingIdToEntity")
    default AlojamientoEntity lodgingIdToEntity(Integer lodgingId) {
        if (lodgingId == null) return null;
        return AlojamientoEntity.builder().id(lodgingId).build();
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        return date != null ? LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    @Named("localDateTimeToString")
    default String localDateTimeToString(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    @Named("estadoToString")
    default String estadoToString(ReservaEntity.EstadoReserva estado) {
        return estado != null ? estado.name() : null;
    }

    @Named("stringToEstado")
    default ReservaEntity.EstadoReserva stringToEstado(String status) {
        return status != null ? ReservaEntity.EstadoReserva.valueOf(status.toUpperCase()) : null;
    }

    @Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}