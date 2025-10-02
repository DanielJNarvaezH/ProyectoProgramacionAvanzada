package com.example.Alojamientos.persistenceLayer.dao.impl;

import com.example.Alojamientos.persistenceLayer.dao.NotificacionDao;
import com.example.Alojamientos.persistenceLayer.entity.NotificacionEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class NotificacionDaoImpl implements NotificacionDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<NotificacionEntity> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(NotificacionEntity.class, id));
    }

    @Override
    public NotificacionEntity save(NotificacionEntity notificacion) {
        if (notificacion.getId() == null) {
            entityManager.persist(notificacion);
            return notificacion;
        } else {
            return entityManager.merge(notificacion);
        }
    }

    @Override
    public void deleteById(Integer id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public List<NotificacionEntity> findByUsuarioId(Integer usuarioId) {
        TypedQuery<NotificacionEntity> query = entityManager.createQuery(
                "SELECT n FROM NotificacionEntity n WHERE n.usuario.id = :usuarioId ORDER BY n.fechaCreacion DESC",
                NotificacionEntity.class
        );
        query.setParameter("usuarioId", usuarioId);
        return query.getResultList();
    }

    @Override
    public List<NotificacionEntity> findUnreadByUsuarioId(Integer usuarioId) {
        TypedQuery<NotificacionEntity> query = entityManager.createQuery(
                "SELECT n FROM NotificacionEntity n WHERE n.usuario.id = :usuarioId AND n.leida = false ORDER BY n.fechaCreacion DESC",
                NotificacionEntity.class
        );
        query.setParameter("usuarioId", usuarioId);
        return query.getResultList();
    }

    @Override
    public long countUnreadByUsuarioId(Integer usuarioId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(n) FROM NotificacionEntity n WHERE n.usuario.id = :usuarioId AND n.leida = false",
                Long.class
        );
        query.setParameter("usuarioId", usuarioId);
        return query.getSingleResult();
    }
}
