package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.AlojamientoDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlojamientoDataMapper {

    @Mapping(target = "hostId", source = "anfitrion.id")
    @Mapping(target = "name", source = "nombre")
    @Mapping(target = "description", source = "descripcion")
    @Mapping(target = "address", source = "direccion")
    @Mapping(target = "city", source = "ciudad")
    @Mapping(target = "latitude", source = "latitud")
    @Mapping(target = "longitude", source = "longitud")
    @Mapping(target = "pricePerNight", source = "precioPorNoche")
    @Mapping(target = "maxCapacity", source = "capacidadMaxima")
    @Mapping(target = "mainImage", source = "imagenPrincipal")
    @Mapping(target = "active", source = "activo")
    AlojamientoDTO toDTO(AlojamientoEntity entity);

    @Mapping(target = "anfitrion", source = "hostId", qualifiedByName = "hostIdToEntity")
    @Mapping(target = "nombre", source = "name")
    @Mapping(target = "descripcion", source = "description")
    @Mapping(target = "direccion", source = "address")
    @Mapping(target = "ciudad", source = "city")
    @Mapping(target = "latitud", source = "latitude", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "longitud", source = "longitude", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "precioPorNoche", source = "pricePerNight", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "capacidadMaxima", source = "maxCapacity")
    @Mapping(target = "imagenPrincipal", source = "mainImage")
    @Mapping(target = "activo", source = "active")
    AlojamientoEntity toEntity(AlojamientoDTO dto);

    @Named("hostIdToEntity")
    default UsuarioEntity hostIdToEntity(Integer hostId) {
        if (hostId == null) return null;
        return UsuarioEntity.builder().id(hostId).build();
    }

    @Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}