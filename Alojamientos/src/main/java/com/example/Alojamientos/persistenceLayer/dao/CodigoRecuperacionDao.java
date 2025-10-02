package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.CodigoRecuperacionEntity;

import java.util.List;
import java.util.Optional;

public interface CodigoRecuperacionDao {

    // Buscar código válido (no usado y no expirado)
    Optional<CodigoRecuperacionEntity> findCodigoValido(String codigo);

    // Listar códigos activos de un usuario
    List<CodigoRecuperacionEntity> findCodigosActivosByUsuario(Integer idUsuario);

    // Marcar un código como usado
    void marcarComoUsado(Integer id);

    // Eliminar códigos expirados
    int eliminarCodigosExpirados();
}
