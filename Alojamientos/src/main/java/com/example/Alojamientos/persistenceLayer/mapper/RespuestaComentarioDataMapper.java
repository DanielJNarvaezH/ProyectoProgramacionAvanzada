package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.RespuestaComentarioDTO;
import com.example.Alojamientos.persistenceLayer.entity.ComentarioEntity;
import com.example.Alojamientos.persistenceLayer.entity.RespuestaComentarioEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RespuestaComentarioDataMapper {

    @Mapping(target = "commentId", source = "comentario.id")
    @Mapping(target = "hostId", source = "usuario.id")
    @Mapping(target = "text", source = "texto")
    RespuestaComentarioDTO toDTO(RespuestaComentarioEntity entity);

    @Mapping(target = "comentario", source = "commentId", qualifiedByName = "commentIdToEntity")
    @Mapping(target = "usuario", source = "hostId", qualifiedByName = "hostIdToEntity")
    @Mapping(target = "texto", source = "text")
    RespuestaComentarioEntity toEntity(RespuestaComentarioDTO dto);

    @Named("commentIdToEntity")
    default ComentarioEntity commentIdToEntity(Integer commentId) {
        if (commentId == null) return null;
        return ComentarioEntity.builder().id(commentId).build();
    }

    @Named("hostIdToEntity")
    default UsuarioEntity hostIdToEntity(Integer hostId) {
        if (hostId == null) return null;
        return UsuarioEntity.builder().id(hostId).build();
    }
}