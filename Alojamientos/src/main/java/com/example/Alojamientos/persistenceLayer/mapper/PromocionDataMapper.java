package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.PromocionDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.PromocionEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromocionDataMapper {

    @Mapping(target = "lodgingId", source = "alojamiento.id")
    @Mapping(target = "name", source = "nombre")
    @Mapping(target = "description", source = "descripcion")
    @Mapping(target = "discountType", source = "tipoDescuento", qualifiedByName = "tipoDescuentoToString")
    @Mapping(target = "discountValue", source = "valorDescuento")
    @Mapping(target = "startDate", source = "fechaInicio", qualifiedByName = "localDateToString")
    @Mapping(target = "endDate", source = "fechaFin", qualifiedByName = "localDateToString")
    @Mapping(target = "promoCode", source = "codigoPromocional")
    @Mapping(target = "active", source = "activa")
    PromocionDTO toDTO(PromocionEntity entity);

    @Mapping(target = "alojamiento", source = "lodgingId", qualifiedByName = "lodgingIdToEntity")
    @Mapping(target = "nombre", source = "name")
    @Mapping(target = "descripcion", source = "description")
    @Mapping(target = "tipoDescuento", source = "discountType", qualifiedByName = "stringToTipoDescuento")
    @Mapping(target = "valorDescuento", source = "discountValue", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "fechaInicio", source = "startDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "fechaFin", source = "endDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "codigoPromocional", source = "promoCode")
    @Mapping(target = "activa", source = "active")
    PromocionEntity toEntity(PromocionDTO dto);

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

    @Named("tipoDescuentoToString")
    default String tipoDescuentoToString(PromocionEntity.TipoDescuento tipo) {
        return tipo != null ? tipo.name() : null;
    }

    @Named("stringToTipoDescuento")
    default PromocionEntity.TipoDescuento stringToTipoDescuento(String type) {
        return type != null ? PromocionEntity.TipoDescuento.valueOf(type.toUpperCase()) : null;
    }

    @Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}