package com.example.Alojamientos.persistenceLayer.mapper;

import com.example.Alojamientos.businessLayer.dto.UsuarioDTO;
import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioDataMapper {

    @Mapping(target = "name", source = "nombre")
    @Mapping(target = "email", source = "correo")
    @Mapping(target = "password", source = "contrasena")
    @Mapping(target = "phone", source = "telefono")
    @Mapping(target = "birthDate", source = "fechaNacimiento", qualifiedByName = "localDateToString")
    @Mapping(target = "role", source = "rol", qualifiedByName = "rolToString")
    UsuarioDTO toDTO(UsuarioEntity entity);

    @Mapping(target = "nombre", source = "name")
    @Mapping(target = "correo", source = "email")
    @Mapping(target = "contrasena", source = "password")
    @Mapping(target = "telefono", source = "phone")
    @Mapping(target = "fechaNacimiento", source = "birthDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "rol", source = "role", qualifiedByName = "stringToRol")
    @Mapping(target = "activo", constant = "true")
    UsuarioEntity toEntity(UsuarioDTO dto);

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        return date != null ? LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    @Named("rolToString")
    default String rolToString(UsuarioEntity.Rol rol) {
        return rol != null ? rol.name() : null;
    }

    @Named("stringToRol")
    default UsuarioEntity.Rol stringToRol(String role) {
        return role != null ? UsuarioEntity.Rol.valueOf(role.toUpperCase()) : null;
    }
}