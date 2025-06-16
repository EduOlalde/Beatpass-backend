package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Entrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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
public class EntradaRepositoryImpl implements EntradaRepository {

    private static final Logger log = LoggerFactory.getLogger(EntradaRepositoryImpl.class);

    @Override
    public Entrada save(EntityManager em, Entrada entrada) {
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

    @Override
    public Optional<Entrada> findById(EntityManager em, Integer id) {
        log.debug("Buscando Entrada con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Entrada con ID nulo.");
            return Optional.empty();
        }
        try {
            Entrada entrada = em.find(Entrada.class, id);
            return Optional.ofNullable(entrada);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar Entrada por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Entrada por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Entrada> findByCodigoQr(EntityManager em, String codigoQr) {
        String qrLog = (codigoQr != null) ? codigoQr.substring(0, Math.min(20, codigoQr.length())) + "..." : "null";
        log.debug("Buscando Entrada con QR: {}", qrLog);
        if (codigoQr == null || codigoQr.isBlank()) {
            log.warn("Intento de buscar Entrada con código QR nulo o vacío.");
            return Optional.empty();
        }
        try {
            TypedQuery<Entrada> query = em.createQuery("SELECT ea FROM Entrada ea WHERE ea.codigoQr = :qr", Entrada.class);
            query.setParameter("qr", codigoQr);
            Entrada entrada = query.getSingleResult();
            return Optional.of(entrada);
        } catch (NoResultException e) {
            log.trace("Entrada no encontrada con QR: {}", qrLog);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando Entrada por QR ({}): {}", qrLog, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Entrada> findByCompraEntradaId(EntityManager em, Integer idCompraEntrada) {
        log.debug("Buscando Entradas para CompraEntrada ID: {}", idCompraEntrada);
        if (idCompraEntrada == null) {
            log.warn("Intento de buscar entradas para un ID de CompraEntrada nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Entrada> query = em.createQuery("SELECT ea FROM Entrada ea WHERE ea.compraEntrada.idCompraEntrada = :ceId ORDER BY ea.idEntrada", Entrada.class);
            query.setParameter("ceId", idCompraEntrada);
            List<Entrada> entradas = query.getResultList();
            log.debug("Encontradas {} Entradas para CompraEntrada ID: {}", entradas.size(), idCompraEntrada);
            return entradas;
        } catch (Exception e) {
            log.error("Error buscando Entradas para CompraEntrada ID {}: {}", idCompraEntrada, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Entrada> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando Entradas para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar entradas para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            String jpql = "SELECT ea FROM Entrada ea "
                    + "JOIN ea.compraEntrada ce "
                    + "JOIN ce.tipoEntrada te "
                    + "WHERE te.festival.idFestival = :festivalId "
                    + "ORDER BY ea.idEntrada";

            TypedQuery<Entrada> query = em.createQuery(jpql, Entrada.class);
            query.setParameter("festivalId", idFestival);
            List<Entrada> entradas = query.getResultList();
            log.debug("Encontradas {} Entradas para Festival ID: {}", entradas.size(), idFestival);
            return entradas;
        } catch (Exception e) {
            log.error("Error buscando Entradas para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
