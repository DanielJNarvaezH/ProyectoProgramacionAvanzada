package com.example.Alojamientos.persistenceLayer.dao;

import com.example.Alojamientos.persistenceLayer.entity.CodigoRecuperacionEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CodigoRecuperacionDaoImpl implements CodigoRecuperacionDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<CodigoRecuperacionEntity> findCodigoValido(String codigo) {
        String jpql = "SELECT c FROM CodigoRecuperacionEntity c " +
                "WHERE c.codigo = :codigo " +
                "AND c.usado = false " +
                "AND c.fechaExpiracion > :now";
        return em.createQuery(jpql, CodigoRecuperacionEntity.class)
                .setParameter("codigo", codigo)
                .setParameter("now", Timestamp.valueOf(LocalDateTime.now()))
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<CodigoRecuperacionEntity> findCodigosActivosByUsuario(Integer idUsuario) {
        String jpql = "SELECT c FROM CodigoRecuperacionEntity c " +
                "WHERE c.usuario.id = :idUsuario " +
                "AND c.usado = false " +
                "AND c.fechaExpiracion > :now";
        return em.createQuery(jpql, CodigoRecuperacionEntity.class)
                .setParameter("idUsuario", idUsuario)
                .setParameter("now", Timestamp.valueOf(LocalDateTime.now()))
                .getResultList();
    }

    @Override
    public void marcarComoUsado(Integer id) {
        String jpql = "UPDATE CodigoRecuperacionEntity c " +
                "SET c.usado = true " +
                "WHERE c.id = :id";
        em.createQuery(jpql)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public int eliminarCodigosExpirados() {
        String jpql = "DELETE FROM CodigoRecuperacionEntity c " +
                "WHERE c.fechaExpiracion < :now";
        return em.createQuery(jpql)
                .setParameter("now", Timestamp.valueOf(LocalDateTime.now()))
                .executeUpdate();
    }
}
