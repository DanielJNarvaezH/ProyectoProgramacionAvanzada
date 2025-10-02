package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.UsuarioEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDaoImpl implements UsuarioDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<UsuarioEntity> findByRol(UsuarioEntity.Rol rol) {
        String jpql = "SELECT u FROM UsuarioEntity u WHERE u.rol = :rol";
        return em.createQuery(jpql, UsuarioEntity.class)
                .setParameter("rol", rol)
                .getResultList();
    }

    @Override
    public Optional<UsuarioEntity> findUsuarioCompletoById(Integer id) {
        String jpql = "SELECT u FROM UsuarioEntity u " +
                "LEFT JOIN FETCH u.alojamientos " + // se activará cuando relaciones Usuario con Alojamiento
                "LEFT JOIN FETCH u.reservas " +     // idem con Reservas
                "WHERE u.id = :id";
        return em.createQuery(jpql, UsuarioEntity.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<UsuarioEntity> findUsuariosConAlojamientosActivos() {
        String jpql = "SELECT DISTINCT u FROM UsuarioEntity u " +
                "JOIN u.alojamientos a " +
                "WHERE a.estado = 'ACTIVO'";
        return em.createQuery(jpql, UsuarioEntity.class)
                .getResultList();
    }

    @Override
    public boolean esMayorDeEdad(Integer usuarioId) {
        String jpql = "SELECT u.fechaNacimiento FROM UsuarioEntity u WHERE u.id = :id";
        LocalDate fecha = em.createQuery(jpql, LocalDate.class)
                .setParameter("id", usuarioId)
                .getSingleResult();
        return fecha.isBefore(LocalDate.now().minusYears(18));
    }
}
