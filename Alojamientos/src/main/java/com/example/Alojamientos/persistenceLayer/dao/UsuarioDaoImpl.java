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
                "WHERE u.id = :id";
        return em.createQuery(jpql, UsuarioEntity.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }


}
