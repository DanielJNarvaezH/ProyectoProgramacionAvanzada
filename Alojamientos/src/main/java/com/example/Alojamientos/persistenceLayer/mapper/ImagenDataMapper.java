package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.ImagenDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.ImagenEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImagenDataMapper {

    @Mapping(target = "lodgingId", source = "alojamiento.id")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "description", source = "descripcion")
    @Mapping(target = "order", source = "ordenVisualizacion")
    ImagenDTO toDTO(ImagenEntity entity);

    @Mapping(target = "alojamiento", source = "lodgingId", qualifiedByName = "lodgingIdToEntity")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "descripcion", source = "description")
    @Mapping(target = "ordenVisualizacion", source = "order")
    ImagenEntity toEntity(ImagenDTO dto);

    @Named("lodgingIdToEntity")
    default AlojamientoEntity lodgingIdToEntity(Integer lodgingId) {
        if (lodgingId == null) return null;
        return AlojamientoEntity.builder().id(lodgingId).build();
    }
}