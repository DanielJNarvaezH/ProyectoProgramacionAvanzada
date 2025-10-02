package com.example.Alojamientos.persistenceLayer.dao.impl;

import com.example.Alojamientos.persistenceLayer.dao.RespuestaComentarioDao;
import com.example.Alojamientos.persistenceLayer.entity.RespuestaComentarioEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class RespuestaComentarioDaoImpl implements RespuestaComentarioDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<RespuestaComentarioEntity> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(RespuestaComentarioEntity.class, id));
    }

    @Override
    public RespuestaComentarioEntity save(RespuestaComentarioEntity respuesta) {
        if (respuesta.getId() == null) {
            entityManager.persist(respuesta);
            return respuesta;
        } else {
            return entityManager.merge(respuesta);
        }
    }

    @Override
    public void deleteById(Integer id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public List<RespuestaComentarioEntity> findByComentarioId(Integer comentarioId) {
        TypedQuery<RespuestaComentarioEntity> query = entityManager.createQuery(
                "SELECT r FROM RespuestaComentarioEntity r WHERE r.comentario.id = :comentarioId ORDER BY r.fechaCreacion ASC",
                RespuestaComentarioEntity.class
        );
        query.setParameter("comentarioId", comentarioId);
        return query.getResultList();
    }

    @Override
    public List<RespuestaComentarioEntity> findByUsuarioId(Integer usuarioId) {
        TypedQuery<RespuestaComentarioEntity> query = entityManager.createQuery(
                "SELECT r FROM RespuestaComentarioEntity r WHERE r.usuario.id = :usuarioId ORDER BY r.fechaCreacion DESC",
                RespuestaComentarioEntity.class
        );
        query.setParameter("usuarioId", usuarioId);
        return query.getResultList();
    }

    @Override
    public long countByComentarioId(Integer comentarioId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM RespuestaComentarioEntity r WHERE r.comentario.id = :comentarioId",
                Long.class
        );
        query.setParameter("comentarioId", comentarioId);
        return query.getSingleResult();
    }
}
