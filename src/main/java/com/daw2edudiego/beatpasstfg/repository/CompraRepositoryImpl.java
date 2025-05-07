package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Compra;
import jakarta.persistence.EntityManager;
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
public class CompraRepositoryImpl implements CompraRepository {

    private static final Logger log = LoggerFactory.getLogger(CompraRepositoryImpl.class);

    @Override
    public Compra save(EntityManager em, Compra compra) {
        if (compra == null) {
            throw new IllegalArgumentException("La entidad Compra no puede ser nula.");
        }
        if (compra.getAsistente() == null || compra.getAsistente().getIdAsistente() == null) {
            throw new IllegalArgumentException("El Asistente asociado a la Compra no puede ser nulo y debe tener ID.");
        }

        log.debug("Intentando persistir nueva Compra para Asistente ID: {}", compra.getAsistente().getIdAsistente());
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
    public Optional<Compra> findById(EntityManager em, Integer id) {
        log.debug("Buscando Compra con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Compra con ID nulo.");
            return Optional.empty();
        }
        try {
            Compra compra = em.find(Compra.class, id);
            return Optional.ofNullable(compra);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar Compra por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Compra por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Compra> findByAsistenteId(EntityManager em, Integer idAsistente) {
        log.debug("Buscando Compras para Asistente ID: {}", idAsistente);
        if (idAsistente == null) {
            log.warn("Intento de buscar compras para un ID de asistente nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Compra> query = em.createQuery(
                    "SELECT c FROM Compra c WHERE c.asistente.idAsistente = :asistenteId ORDER BY c.fechaCompra DESC",
                    Compra.class);
            query.setParameter("asistenteId", idAsistente);
            List<Compra> compras = query.getResultList();
            log.debug("Encontradas {} compras para Asistente ID: {}", compras.size(), idAsistente);
            return compras;
        } catch (Exception e) {
            log.error("Error buscando Compras para Asistente ID {}: {}", idAsistente, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Compra> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando Compras para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar compras para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            String jpql = "SELECT DISTINCT c FROM Compra c "
                    + "JOIN c.detallesCompra ce "
                    + "JOIN ce.entrada e "
                    + "WHERE e.festival.idFestival = :festivalId "
                    + "ORDER BY c.fechaCompra DESC";

            TypedQuery<Compra> query = em.createQuery(jpql, Compra.class);
            query.setParameter("festivalId", idFestival);
            List<Compra> compras = query.getResultList();
            log.debug("Encontradas {} compras para Festival ID: {}", compras.size(), idFestival);
            return compras;
        } catch (Exception e) {
            log.error("Error buscando Compras para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
