package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.FavoritoDTO;
import com.example.Alojamientos.persistenceLayer.entity.AlojamientoEntity;
import com.example.Alojamientos.persistenceLayer.entity.FavoritoEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FavoritoDataMapper {

    @Mapping(target = "userId", source = "usuario.id")
    @Mapping(target = "lodgingId", source = "alojamiento.id")
    FavoritoDTO toDTO(FavoritoEntity entity);

    @Mapping(target = "usuario", source = "userId", qualifiedByName = "userIdToEntity")
    @Mapping(target = "alojamiento", source = "lodgingId", qualifiedByName = "lodgingIdToEntity")
    FavoritoEntity toEntity(FavoritoDTO dto);

    @Named("userIdToEntity")
    default UsuarioEntity userIdToEntity(Integer userId) {
        if (userId == null) return null;
        return UsuarioEntity.builder().id(userId).build();
    }

    @Named("lodgingIdToEntity")
    default AlojamientoEntity lodgingIdToEntity(Integer lodgingId) {
        if (lodgingId == null) return null;
        return AlojamientoEntity.builder().id(lodgingId).build();
    }
}