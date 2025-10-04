package com.example.Alojamientos.persistenceLayer.repository;

import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

    /**
     * Busca un usuario por correo electrónico
     * @param correo correo del usuario
     * @return Optional<Usuario>
     */
    Optional<UsuarioEntity> findByCorreo(String correo);

    /**
     * Verifica si existe un usuario con un correo dado
     * @param correo correo del usuario
     * @return true si existe, false en caso contrario
     */
    boolean existsByCorreo(String correo);

    /**
     * Busca un usuario por teléfono
     * @param telefono número de teléfono
     * @return Optional<Usuario>
     */
    Optional<UsuarioEntity> findByTelefono(String telefono);

    /**
     * Verifica si existe un usuario con un teléfono dado
     * @param telefono número de teléfono
     * @return true si existe, false en caso contrario
     */
    boolean existsByTelefono(String telefono);
}
