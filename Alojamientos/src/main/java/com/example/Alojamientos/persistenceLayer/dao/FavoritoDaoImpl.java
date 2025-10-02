package com.example.Alojamientos.persistenceLayer.dao.impl;

import com.example.Alojamientos.persistenceLayer.dao.FavoritoDao;
import com.example.Alojamientos.persistenceLayer.entity.FavoritoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class FavoritoDaoImpl implements FavoritoDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<FavoritoEntity> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(FavoritoEntity.class, id));
    }

    @Override
    public FavoritoEntity save(FavoritoEntity favorito) {
        if (favorito.getId() == null) {
            entityManager.persist(favorito);
            return favorito;
        } else {
            return entityManager.merge(favorito);
        }
    }

    @Override
    public void deleteById(Integer id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public List<FavoritoEntity> findByUsuarioId(Integer usuarioId) {
        TypedQuery<FavoritoEntity> query = entityManager.createQuery(
                "SELECT f FROM FavoritoEntity f WHERE f.usuario.id = :usuarioId ORDER BY f.fechaAgregado DESC",
                FavoritoEntity.class
        );
        query.setParameter("usuarioId", usuarioId);
        return query.getResultList();
    }

    @Override
    public boolean existsByUsuarioAndAlojamiento(Integer usuarioId, Integer alojamientoId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(f) FROM FavoritoEntity f WHERE f.usuario.id = :usuarioId AND f.alojamiento.id = :alojamientoId",
                        Long.class
                )
                .setParameter("usuarioId", usuarioId)
                .setParameter("alojamientoId", alojamientoId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public void deleteByUsuarioAndAlojamiento(Integer usuarioId, Integer alojamientoId) {
        entityManager.createQuery(
                        "DELETE FROM FavoritoEntity f WHERE f.usuario.id = :usuarioId AND f.alojamiento.id = :alojamientoId"
                )
                .setParameter("usuarioId", usuarioId)
                .setParameter("alojamientoId", alojamientoId)
                .executeUpdate();
    }
}
