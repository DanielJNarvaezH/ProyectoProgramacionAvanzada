package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;

import java.util.List;
import java.util.Optional;

public interface UsuarioDao {

    // Buscar por rol
    List<UsuarioEntity> findByRol(UsuarioEntity.Rol rol);

    // Buscar usuario completo (con posibles relaciones en el futuro: reservas, alojamientos, etc.)
    Optional<UsuarioEntity> findUsuarioCompletoById(Integer id);

    // Usuarios con alojamientos activos (cuando se agregue la relación con AlojamientoEntity)
    List<UsuarioEntity> findUsuariosConAlojamientosActivos();

    // Validar mayoría de edad
    boolean esMayorDeEdad(Integer usuarioId);
}
