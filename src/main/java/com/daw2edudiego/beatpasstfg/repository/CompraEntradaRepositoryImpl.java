package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.CompraEntrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de CompraEntradaRepository usando JPA EntityManager.
 */
public class CompraEntradaRepositoryImpl implements CompraEntradaRepository {

    private static final Logger log = LoggerFactory.getLogger(CompraEntradaRepositoryImpl.class);

    @Override
    public CompraEntrada save(EntityManager em, CompraEntrada compraEntrada) {
        if (compraEntrada == null) {
            throw new IllegalArgumentException("La entidad CompraEntrada no puede ser nula.");
        }
        if (compraEntrada.getCompra() == null || compraEntrada.getCompra().getIdCompra() == null) {
            throw new IllegalArgumentException("La Compra asociada a CompraEntrada no puede ser nula y debe tener ID.");
        }
        if (compraEntrada.getEntrada() == null || compraEntrada.getEntrada().getIdEntrada() == null) {
            throw new IllegalArgumentException("La Entrada asociada a CompraEntrada no puede ser nula y debe tener ID.");
        }

        log.debug("Intentando persistir nuevo CompraEntrada para Compra ID: {} y Entrada ID: {}",
                compraEntrada.getCompra().getIdCompra(), compraEntrada.getEntrada().getIdEntrada());
        try {
            em.persist(compraEntrada);
            log.info("Nuevo CompraEntrada persistido con ID: {}", compraEntrada.getIdCompraEntrada());
            return compraEntrada;
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar CompraEntrada: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar CompraEntrada: {}", e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar CompraEntrada", e);
        }
    }

    @Override
    public Optional<CompraEntrada> findById(EntityManager em, Integer id) {
        log.debug("Buscando CompraEntrada con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar CompraEntrada con ID nulo.");
            return Optional.empty();
        }
        try {
            CompraEntrada detalle = em.find(CompraEntrada.class, id);
            return Optional.ofNullable(detalle);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar CompraEntrada por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar CompraEntrada por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<CompraEntrada> findByCompraId(EntityManager em, Integer idCompra) {
        log.debug("Buscando CompraEntradas para Compra ID: {}", idCompra);
        if (idCompra == null) {
            log.warn("Intento de buscar detalles para un ID de compra nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<CompraEntrada> query = em.createQuery(
                    "SELECT ce FROM CompraEntrada ce WHERE ce.compra.idCompra = :compraId", CompraEntrada.class);
            query.setParameter("compraId", idCompra);
            List<CompraEntrada> detalles = query.getResultList();
            log.debug("Encontrados {} detalles para Compra ID: {}", detalles.size(), idCompra);
            return detalles;
        } catch (Exception e) {
            log.error("Error buscando CompraEntradas para Compra ID {}: {}", idCompra, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
