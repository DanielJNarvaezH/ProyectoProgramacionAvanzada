package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.ComentarioDTO;
import com.example.Alojamientos.persistenceLayer.entity.ComentarioEntity;
import com.example.Alojamientos.persistenceLayer.entity.ReservaEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComentarioDataMapper {

    @Mapping(target = "reservationId", source = "reserva.id")
    @Mapping(target = "userId", source = "usuario.id")
    @Mapping(target = "rating", source = "calificacion")
    @Mapping(target = "text", source = "texto")
    ComentarioDTO toDTO(ComentarioEntity entity);

    @Mapping(target = "reserva", source = "reservationId", qualifiedByName = "reservationIdToEntity")
    @Mapping(target = "usuario", source = "userId", qualifiedByName = "userIdToEntity")
    @Mapping(target = "calificacion", source = "rating")
    @Mapping(target = "texto", source = "text")
    ComentarioEntity toEntity(ComentarioDTO dto);

    @Named("reservationIdToEntity")
    default ReservaEntity reservationIdToEntity(Integer reservationId) {
        if (reservationId == null) return null;
        return ReservaEntity.builder().id(reservationId).build();
    }

    @Named("userIdToEntity")
    default UsuarioEntity userIdToEntity(Integer userId) {
        if (userId == null) return null;
        return UsuarioEntity.builder().id(userId).build();
    }
}