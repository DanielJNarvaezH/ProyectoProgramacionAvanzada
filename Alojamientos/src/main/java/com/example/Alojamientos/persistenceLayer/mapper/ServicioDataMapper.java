package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.ServicioDTO;
import com.example.Alojamientos.persistenceLayer.entity.ServicioEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServicioDataMapper {

    @Mapping(target = "name", source = "nombre")
    @Mapping(target = "description", source = "descripcion")
    @Mapping(target = "icon", source = "icono")
    @Mapping(target = "active", source = "activo")
    ServicioDTO toDTO(ServicioEntity entity);

    @Mapping(target = "nombre", source = "name")
    @Mapping(target = "descripcion", source = "description")
    @Mapping(target = "icono", source = "icon")
    @Mapping(target = "activo", source = "active")
    ServicioEntity toEntity(ServicioDTO dto);
}