package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;

import java.util.List;
import java.util.Optional;

public interface UsuarioDao {

    // Buscar por rol
    List<UsuarioEntity> findByRol(UsuarioEntity.Rol rol);

    // Buscar usuario completo (con posibles relaciones en el futuro: reservas, alojamientos, etc.)
    Optional<UsuarioEntity> findUsuarioCompletoById(Integer id);

    // Validar mayoría de edad
    boolean esMayorDeEdad(Integer usuarioId);
}
