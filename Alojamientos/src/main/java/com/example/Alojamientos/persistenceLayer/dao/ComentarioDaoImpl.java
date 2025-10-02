package com.example.Alojamientos.persistenceLayer.dao.impl;

import com.example.Alojamientos.persistenceLayer.dao.ComentarioDao;
import com.example.Alojamientos.persistenceLayer.entity.ComentarioEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ComentarioDaoImpl implements ComentarioDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ComentarioEntity> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(ComentarioEntity.class, id));
    }

    @Override
    public ComentarioEntity save(ComentarioEntity comentario) {
        if (comentario.getId() == null) {
            entityManager.persist(comentario);
            return comentario;
        } else {
            return entityManager.merge(comentario);
        }
    }

    @Override
    public void deleteById(Integer id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public List<ComentarioEntity> findByAlojamientoId(Integer alojamientoId) {
        TypedQuery<ComentarioEntity> query = entityManager.createQuery(
                "SELECT c FROM ComentarioEntity c WHERE c.alojamiento.id = :alojamientoId ORDER BY c.fechaCreacion DESC",
                ComentarioEntity.class
        );
        query.setParameter("alojamientoId", alojamientoId);
        return query.getResultList();
    }

    @Override
    public List<ComentarioEntity> findByUsuarioId(Integer usuarioId) {
        TypedQuery<ComentarioEntity> query = entityManager.createQuery(
                "SELECT c FROM ComentarioEntity c WHERE c.usuario.id = :usuarioId ORDER BY c.fechaCreacion DESC",
                ComentarioEntity.class
        );
        query.setParameter("usuarioId", usuarioId);
        return query.getResultList();
    }

    @Override
    public List<ComentarioEntity> findByAlojamientoIdOrderByFechaCreacionDesc(Integer alojamientoId){
        TypedQuery<ComentarioEntity> query = entityManager.createQuery(
                "SELECT c FROM ComentarioEntity c WHERE c.alojamiento.id = :alojamientoId ORDER BY c.fechaCreacion DESC",
                ComentarioEntity.class
        );
        query.setParameter("alojamientoId", alojamientoId);
        return query.getResultList();
    }

    @Override
    public long countByAlojamientoId(Integer alojamientoId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(c) FROM ComentarioEntity c WHERE c.alojamiento.id = :alojamientoId",
                Long.class
        );
        query.setParameter("alojamientoId", alojamientoId);
        return query.getSingleResult();
    }
}
