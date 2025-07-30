package com.beatpass.repository;

import com.beatpass.model.Compra;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de CompraRepository usando JPA EntityManager.
 */
@ApplicationScoped
public class CompraRepositoryImpl implements CompraRepository {

    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;
    private static final Logger log = LoggerFactory.getLogger(CompraRepositoryImpl.class);

    @Override
    public Compra save(Compra compra) {
        if (compra == null) {
            throw new IllegalArgumentException("La entidad Compra no puede ser nula.");
        }
        if (compra.getComprador() == null || compra.getComprador().getIdComprador() == null) {
            throw new IllegalArgumentException("El Comprador asociado a la Compra no puede ser nulo y debe tener ID.");
        }

        log.debug("Intentando persistir nueva Compra para Comprador ID: {}", compra.getComprador().getIdComprador());
        try {
            em.persist(compra);
            log.info("Nueva Compra persistida con ID: {}", compra.getIdCompra());
            return compra;
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Compra: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Compra: {}", e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Compra", e);
        }
    }

    @Override
    public Optional<Compra> findById(Integer id) {
        log.debug("Buscando Compra con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Compra con ID nulo.");
            return Optional.empty();
        }
        try {
            TypedQuery<Compra> query = em.createQuery(
                    "SELECT c FROM Compra c LEFT JOIN FETCH c.comprador WHERE c.idCompra = :id", Compra.class);
            query.setParameter("id", id);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Compra por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Compra> findByCompradorId(Integer idComprador) {
        log.debug("Buscando Compras para Comprador ID: {}", idComprador);
        if (idComprador == null) {
            log.warn("Intento de buscar compras para un ID de comprador nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Compra> query = em.createQuery(
                    "SELECT c FROM Compra c LEFT JOIN FETCH c.comprador WHERE c.comprador.idComprador = :compradorId ORDER BY c.fechaCompra DESC",
                    Compra.class);
            query.setParameter("compradorId", idComprador);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando Compras para Comprador ID {}: {}", idComprador, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Compra> findByFestivalId(Integer idFestival) {
        log.debug("Buscando Compras para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar compras para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            String subquery = "SELECT DISTINCT c.idCompra FROM Compra c "
                    + "JOIN c.detallesCompra cd "
                    + "JOIN cd.tipoEntrada te "
                    + "WHERE te.festival.idFestival = :festivalId";

            List<Integer> compraIds = em.createQuery(subquery, Integer.class)
                    .setParameter("festivalId", idFestival)
                    .getResultList();

            if (compraIds.isEmpty()) {
                return Collections.emptyList();
            }

            String mainQuery = "SELECT DISTINCT c FROM Compra c "
                    + "LEFT JOIN FETCH c.comprador "
                    + "LEFT JOIN FETCH c.detallesCompra "
                    + "LEFT JOIN FETCH c.detallesCompra.tipoEntrada "
                    + "WHERE c.idCompra IN (:compraIds) "
                    + "ORDER BY c.fechaCompra DESC";

            return em.createQuery(mainQuery, Compra.class)
                    .setParameter("compraIds", compraIds)
                    .getResultList();

        } catch (Exception e) {
            log.error("Error buscando Compras para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
