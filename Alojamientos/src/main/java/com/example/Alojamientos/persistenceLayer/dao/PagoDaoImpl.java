package com.example.Alojamientos.persistenceLayer.dao.impl;

import com.example.Alojamientos.persistenceLayer.dao.PagoDao;
import com.example.Alojamientos.persistenceLayer.entity.PagoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class PagoDaoImpl implements PagoDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<PagoEntity> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(PagoEntity.class, id));
    }

    @Override
    public PagoEntity save(PagoEntity pago) {
        if (pago.getId() == null) {
            entityManager.persist(pago);
            return pago;
        } else {
            return entityManager.merge(pago);
        }
    }

    @Override
    public void deleteById(Integer id) {
        findById(id).ifPresent(p -> entityManager.remove(p));
    }

    @Override
    public List<PagoEntity> findByReservaId(Integer reservaId) {
        TypedQuery<PagoEntity> query = entityManager.createQuery(
                "SELECT p FROM PagoEntity p WHERE p.reserva.id = :reservaId",
                PagoEntity.class
        );
        query.setParameter("reservaId", reservaId);
        return query.getResultList();
    }

    @Override
    public List<PagoEntity> findByReserva_Huesped_Id(Integer usuarioId) {
        TypedQuery<PagoEntity> query = entityManager.createQuery(
                "SELECT p FROM PagoEntity p " +
                        "JOIN p.reserva r " +
                        "JOIN r.huesped u " +
                        "WHERE u.id = :usuarioId",
                PagoEntity.class
        );
        query.setParameter("usuarioId", usuarioId);
        return query.getResultList();
    }

    @Override
    public Optional<PagoEntity> findByReferenciaExterna(String referenciaExterna) {
        TypedQuery<PagoEntity> query = entityManager.createQuery(
                "SELECT p FROM PagoEntity p WHERE p.referenciaExterna = :referencia",
                PagoEntity.class
        );
        query.setParameter("referencia", referenciaExterna);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public List<PagoEntity> findByEstado(PagoEntity.EstadoPago estado) {
        TypedQuery<PagoEntity> query = entityManager.createQuery(
                "SELECT p FROM PagoEntity p WHERE p.estado = :estado",
                PagoEntity.class
        );
        query.setParameter("estado", estado);
        return query.getResultList();
    }

    @Override
    public boolean existsByReservaIdAndEstado(Integer reservaId, PagoEntity.EstadoPago estado) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(p) FROM PagoEntity p " +
                        "WHERE p.reserva.id = :reservaId AND p.estado = :estado",
                Long.class
        );
        query.setParameter("reservaId", reservaId);
        query.setParameter("estado", estado);
        return query.getSingleResult() > 0;
    }
}
