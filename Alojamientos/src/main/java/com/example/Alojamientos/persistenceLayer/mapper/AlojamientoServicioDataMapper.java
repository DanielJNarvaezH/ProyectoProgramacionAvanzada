package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.AlojamientoServicioDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoServicioEntity;
import com.example.Alojamientos.persistenceLayer.entity.ServicioEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlojamientoServicioDataMapper {

    @Mapping(target = "lodgingId", source = "alojamiento.id")
    @Mapping(target = "serviceId", source = "servicio.id")
    AlojamientoServicioDTO toDTO(AlojamientoServicioEntity entity);

    @Mapping(target = "alojamiento", source = "lodgingId", qualifiedByName = "lodgingIdToEntity")
    @Mapping(target = "servicio", source = "serviceId", qualifiedByName = "serviceIdToEntity")
    @Mapping(target = "activo", constant = "true")
    AlojamientoServicioEntity toEntity(AlojamientoServicioDTO dto);

    @Named("lodgingIdToEntity")
    default AlojamientoEntity lodgingIdToEntity(Integer lodgingId) {
        if (lodgingId == null) return null;
        return AlojamientoEntity.builder().id(lodgingId).build();
    }

    @Named("serviceIdToEntity")
    default ServicioEntity serviceIdToEntity(Integer serviceId) {
        if (serviceId == null) return null;
        return ServicioEntity.builder().id(serviceId).build();
    }
}