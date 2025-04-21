package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.EntradaAsignada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de EntradaAsignadaRepository usando JPA.
 */
public class EntradaAsignadaRepositoryImpl implements EntradaAsignadaRepository {

    private static final Logger log = LoggerFactory.getLogger(EntradaAsignadaRepositoryImpl.class);

    @Override
    public EntradaAsignada save(EntityManager em, EntradaAsignada entradaAsignada) {
        if (entradaAsignada == null || entradaAsignada.getCompraEntrada() == null || entradaAsignada.getCodigoQr() == null) {
            throw new IllegalArgumentException("EntradaAsignada, CompraEntrada asociada y Código QR no pueden ser nulos.");
        }
        log.debug("Intentando guardar EntradaAsignada con ID: {} y QR: {}...", entradaAsignada.getIdEntradaAsignada(), entradaAsignada.getCodigoQr().substring(0, Math.min(20, entradaAsignada.getCodigoQr().length())));
        try {
            if (entradaAsignada.getIdEntradaAsignada() == null) {
                em.persist(entradaAsignada);
                em.flush();
                log.info("Nueva EntradaAsignada persistida con ID: {}", entradaAsignada.getIdEntradaAsignada());
                return entradaAsignada;
            } else {
                EntradaAsignada merged = em.merge(entradaAsignada);
                log.info("EntradaAsignada actualizada con ID: {}", merged.getIdEntradaAsignada());
                return merged;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar EntradaAsignada: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar EntradaAsignada: {}", e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar EntradaAsignada", e);
        }
    }

    @Override
    public Optional<EntradaAsignada> findById(EntityManager em, Integer id) {
        // ... (sin cambios) ...
        log.debug("Buscando EntradaAsignada con ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(em.find(EntradaAsignada.class, id));
        } catch (Exception e) {
            log.error("Error al buscar EntradaAsignada por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<EntradaAsignada> findByCodigoQr(EntityManager em, String codigoQr) {
        // ... (sin cambios) ...
        log.debug("Buscando EntradaAsignada con QR: {}...", codigoQr.substring(0, Math.min(20, codigoQr.length())));
        if (codigoQr == null || codigoQr.isBlank()) {
            return Optional.empty();
        }
        try {
            TypedQuery<EntradaAsignada> query = em.createQuery(
                    "SELECT ea FROM EntradaAsignada ea WHERE ea.codigoQr = :qr", EntradaAsignada.class);
            query.setParameter("qr", codigoQr);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            log.trace("EntradaAsignada no encontrada con QR: {}...", codigoQr.substring(0, Math.min(20, codigoQr.length())));
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando EntradaAsignada por QR: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<EntradaAsignada> findByCompraEntradaId(EntityManager em, Integer idCompraEntrada) {
        // ... (sin cambios) ...
        log.debug("Buscando EntradasAsignadas para CompraEntrada ID: {}", idCompraEntrada);
        if (idCompraEntrada == null) {
            return Collections.emptyList();
        }
        try {
            TypedQuery<EntradaAsignada> query = em.createQuery(
                    "SELECT ea FROM EntradaAsignada ea WHERE ea.compraEntrada.idCompraEntrada = :ceId ORDER BY ea.idEntradaAsignada", EntradaAsignada.class);
            query.setParameter("ceId", idCompraEntrada);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando EntradasAsignadas para CompraEntrada ID {}: {}", idCompraEntrada, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Busca todas las entradas asignadas para un festival específico. Requiere
     * joins a través de CompraEntrada y Entrada.
     */
    @Override
    public List<EntradaAsignada> findByFestivalId(EntityManager em, Integer idFestival) { // <-- NUEVO MÉTODO
        log.debug("Buscando EntradasAsignadas para Festival ID: {}", idFestival);
        if (idFestival == null) {
            return Collections.emptyList();
        }
        try {
            // JPQL con Joins para llegar al festival
            TypedQuery<EntradaAsignada> query = em.createQuery(
                    "SELECT ea FROM EntradaAsignada ea "
                    + "JOIN ea.compraEntrada ce "
                    + "JOIN ce.entrada e "
                    + "WHERE e.festival.idFestival = :festivalId "
                    + "ORDER BY ea.idEntradaAsignada", // Ordenar por ID de entrada asignada
                    EntradaAsignada.class);
            query.setParameter("festivalId", idFestival);
            List<EntradaAsignada> entradas = query.getResultList();
            log.debug("Encontradas {} EntradasAsignadas para Festival ID: {}", entradas.size(), idFestival);
            return entradas;
        } catch (Exception e) {
            log.error("Error buscando EntradasAsignadas para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
