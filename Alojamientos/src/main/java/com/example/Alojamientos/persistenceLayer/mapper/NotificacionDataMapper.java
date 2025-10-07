package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.NotificacionDTO;
import com.example.Alojamientos.persistenceLayer.entity.NotificacionEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificacionDataMapper {

    @Mapping(target = "userId", source = "usuario.id")
    @Mapping(target = "type", source = "tipo", qualifiedByName = "tipoNotificacionToString")
    @Mapping(target = "title", source = "titulo")
    @Mapping(target = "message", source = "mensaje")
    @Mapping(target = "read", source = "leida")
    @Mapping(target = "readDate", source = "fechaCreacion", qualifiedByName = "localDateTimeToString")
    NotificacionDTO toDTO(NotificacionEntity entity);

    @Mapping(target = "usuario", source = "userId", qualifiedByName = "userIdToEntity")
    @Mapping(target = "tipo", source = "type", qualifiedByName = "stringToTipoNotificacion")
    @Mapping(target = "titulo", source = "title")
    @Mapping(target = "mensaje", source = "message")
    @Mapping(target = "leida", source = "read")
    NotificacionEntity toEntity(NotificacionDTO dto);

    @Named("userIdToEntity")
    default UsuarioEntity userIdToEntity(Integer userId) {
        if (userId == null) return null;
        return UsuarioEntity.builder().id(userId).build();
    }

    @Named("tipoNotificacionToString")
    default String tipoNotificacionToString(NotificacionEntity.TipoNotificacion tipo) {
        return tipo != null ? tipo.name() : null;
    }

    @Named("stringToTipoNotificacion")
    default NotificacionEntity.TipoNotificacion stringToTipoNotificacion(String type) {
        return type != null ? NotificacionEntity.TipoNotificacion.valueOf(type.toUpperCase()) : null;
    }

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
}