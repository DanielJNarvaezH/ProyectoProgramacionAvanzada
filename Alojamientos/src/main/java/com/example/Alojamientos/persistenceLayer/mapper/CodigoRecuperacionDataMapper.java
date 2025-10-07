package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.CodigoRecuperacionDTO;
import com.example.Alojamientos.persistenceLayer.entity.CodigoRecuperacionEntity;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import org.mapstruct.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CodigoRecuperacionDataMapper {

    @Mapping(target = "userId", source = "usuario.id")
    @Mapping(target = "code", source = "codigo")
    @Mapping(target = "expirationDate", source = "fechaExpiracion", qualifiedByName = "timestampToString")
    @Mapping(target = "used", source = "usado")
    CodigoRecuperacionDTO toDTO(CodigoRecuperacionEntity entity);

    @Mapping(target = "usuario", source = "userId", qualifiedByName = "userIdToEntity")
    @Mapping(target = "codigo", source = "code")
    @Mapping(target = "fechaExpiracion", source = "expirationDate", qualifiedByName = "stringToTimestamp")
    @Mapping(target = "usado", source = "used")
    CodigoRecuperacionEntity toEntity(CodigoRecuperacionDTO dto);

    @Named("userIdToEntity")
    default UsuarioEntity userIdToEntity(Integer userId) {
        if (userId == null) return null;
        return UsuarioEntity.builder().id(userId).build();
    }

    @Named("timestampToString")
    default String timestampToString(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Named("stringToTimestamp")
    default Timestamp stringToTimestamp(String dateTimeStr) {
        if (dateTimeStr == null) return null;
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return Timestamp.valueOf(dateTime);
    }
}