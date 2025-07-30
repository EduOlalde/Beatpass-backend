package com.beatpass.repository;

import com.beatpass.model.Entrada;
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
 * Implementación de EntradaRepository usando JPA EntityManager.
 */
@ApplicationScoped
public class EntradaRepositoryImpl implements EntradaRepository {

    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;
    private static final Logger log = LoggerFactory.getLogger(EntradaRepositoryImpl.class);

    @Override
    public Entrada save(Entrada entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("La entidad Entrada no puede ser nula.");
        }
        if (entrada.getCompraEntrada() == null || entrada.getCompraEntrada().getIdCompraEntrada() == null) {
            throw new IllegalArgumentException("La CompraEntrada asociada a Entrada no puede ser nula y debe tener ID.");
        }
        if (entrada.getCodigoQr() == null || entrada.getCodigoQr().isBlank()) {
            throw new IllegalArgumentException("El código QR de Entrada no puede ser nulo ni vacío.");
        }

        String qrLog = entrada.getCodigoQr().substring(0, Math.min(20, entrada.getCodigoQr().length())) + "...";
        log.debug("Intentando guardar Entrada con ID: {} y QR: {}", entrada.getIdEntrada(), qrLog);
        try {
            if (entrada.getIdEntrada() == null) {
                log.trace("Persistiendo nueva Entrada...");
                em.persist(entrada);
                log.info("Nueva Entrada persistida con ID: {}", entrada.getIdEntrada());
                return entrada;
            } else {
                log.trace("Actualizando Entrada con ID: {}", entrada.getIdEntrada());
                Entrada merged = em.merge(entrada);
                log.info("Entrada actualizada con ID: {}", merged.getIdEntrada());
                return merged;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Entrada (ID: {}, QR: {}): {}",
                    entrada.getIdEntrada(), qrLog, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Entrada (ID: {}, QR: {}): {}",
                    entrada.getIdEntrada(), qrLog, e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Entrada", e);
        }
    }

    private String getComprehensiveEntradaQuery() {
        return "SELECT DISTINCT e FROM Entrada e "
                + "LEFT JOIN FETCH e.compraEntrada "
                + "LEFT JOIN FETCH e.compraEntrada.tipoEntrada "
                + "LEFT JOIN FETCH e.compraEntrada.tipoEntrada.festival "
                + "LEFT JOIN FETCH e.asistente "
                + "LEFT JOIN FETCH e.pulseraAsociada ";
    }

    @Override
    public Optional<Entrada> findById(Integer id) {
        log.debug("Buscando Entrada con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Entrada con ID nulo.");
            return Optional.empty();
        }
        try {
            TypedQuery<Entrada> query = em.createQuery(getComprehensiveEntradaQuery() + "WHERE e.idEntrada = :id", Entrada.class);
            query.setParameter("id", id);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            log.trace("Entrada no encontrada con ID: {}", id);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Entrada por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Entrada> findByCodigoQr(String codigoQr) {
        String qrLog = (codigoQr != null) ? codigoQr.substring(0, Math.min(20, codigoQr.length())) + "..." : "null";
        log.debug("Buscando Entrada con QR: {}", qrLog);
        if (codigoQr == null || codigoQr.isBlank()) {
            log.warn("Intento de buscar Entrada con código QR nulo o vacío.");
            return Optional.empty();
        }
        try {
            TypedQuery<Entrada> query = em.createQuery(getComprehensiveEntradaQuery() + "WHERE e.codigoQr = :qr", Entrada.class);
            query.setParameter("qr", codigoQr);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            log.trace("Entrada no encontrada con QR: {}", qrLog);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando Entrada por QR ({}): {}", qrLog, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Entrada> findByCompraEntradaId(Integer idCompraEntrada) {
        log.debug("Buscando Entradas para CompraEntrada ID: {}", idCompraEntrada);
        if (idCompraEntrada == null) {
            log.warn("Intento de buscar entradas para un ID de CompraEntrada nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Entrada> query = em.createQuery(getComprehensiveEntradaQuery() + "WHERE e.compraEntrada.idCompraEntrada = :ceId ORDER BY e.idEntrada", Entrada.class);
            query.setParameter("ceId", idCompraEntrada);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando Entradas para CompraEntrada ID {}: {}", idCompraEntrada, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Entrada> findByFestivalId(Integer idFestival) {
        log.debug("Buscando Entradas para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar entradas para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            String jpql = getComprehensiveEntradaQuery() + "WHERE e.compraEntrada.tipoEntrada.festival.idFestival = :festivalId ORDER BY e.idEntrada";
            TypedQuery<Entrada> query = em.createQuery(jpql, Entrada.class);
            query.setParameter("festivalId", idFestival);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando Entradas para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
