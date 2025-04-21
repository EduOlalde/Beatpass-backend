package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.CompraEntrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery; // Importar TypedQuery
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections; // Importar Collections
import java.util.List; // Importar List
import java.util.Optional; // Importar Optional

/**
 * Implementación de CompraEntradaRepository usando JPA.
 * @author Eduardo Olalde
 */
public class CompraEntradaRepositoryImpl implements CompraEntradaRepository {

    private static final Logger log = LoggerFactory.getLogger(CompraEntradaRepositoryImpl.class);

    @Override
    public CompraEntrada save(EntityManager em, CompraEntrada compraEntrada) {
        if (compraEntrada == null || compraEntrada.getCompra() == null || compraEntrada.getEntrada() == null) {
            throw new IllegalArgumentException("CompraEntrada, Compra y Entrada asociadas no pueden ser nulos.");
        }
        // Siempre es nuevo, usamos persist
        log.debug("Intentando persistir nuevo CompraEntrada para Compra ID: {} y Entrada ID: {}",
                compraEntrada.getCompra().getIdCompra(), compraEntrada.getEntrada().getIdEntrada());
        try {
            em.persist(compraEntrada);
            em.flush(); // Opcional
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
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(em.find(CompraEntrada.class, id));
        } catch (Exception e) {
            log.error("Error al buscar CompraEntrada por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<CompraEntrada> findByCompraId(EntityManager em, Integer idCompra) {
        log.debug("Buscando CompraEntradas para Compra ID: {}", idCompra);
        if (idCompra == null) {
            return Collections.emptyList();
        }
        try {
            TypedQuery<CompraEntrada> query = em.createQuery(
                    "SELECT ce FROM CompraEntrada ce WHERE ce.compra.idCompra = :compraId", CompraEntrada.class);
            query.setParameter("compraId", idCompra);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando CompraEntradas para Compra ID {}: {}", idCompra, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
