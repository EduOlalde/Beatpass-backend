package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.EntradaAsignada;
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
 * Implementación de {@link EntradaAsignadaRepository} utilizando JPA
 * EntityManager. Proporciona la lógica concreta para interactuar con la base de
 * datos para la entidad EntradaAsignada.
 *
 * @author Eduardo Olalde
 */
public class EntradaAsignadaRepositoryImpl implements EntradaAsignadaRepository {

    private static final Logger log = LoggerFactory.getLogger(EntradaAsignadaRepositoryImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public EntradaAsignada save(EntityManager em, EntradaAsignada entradaAsignada) {
        // Validación de precondiciones
        if (entradaAsignada == null) {
            throw new IllegalArgumentException("La entidad EntradaAsignada no puede ser nula.");
        }
        if (entradaAsignada.getCompraEntrada() == null || entradaAsignada.getCompraEntrada().getIdCompraEntrada() == null) {
            throw new IllegalArgumentException("La CompraEntrada asociada a EntradaAsignada no puede ser nula y debe tener ID.");
        }
        if (entradaAsignada.getCodigoQr() == null || entradaAsignada.getCodigoQr().isBlank()) {
            throw new IllegalArgumentException("El código QR de EntradaAsignada no puede ser nulo ni vacío.");
        }

        // Truncar QR para logging por seguridad/brevedad
        String qrLog = entradaAsignada.getCodigoQr().substring(0, Math.min(20, entradaAsignada.getCodigoQr().length())) + "...";
        log.debug("Intentando guardar EntradaAsignada con ID: {} y QR: {}", entradaAsignada.getIdEntradaAsignada(), qrLog);
        try {
            if (entradaAsignada.getIdEntradaAsignada() == null) {
                // Nueva entrada, usar persist
                log.trace("Persistiendo nueva EntradaAsignada...");
                em.persist(entradaAsignada);
                // em.flush(); // Descomentar si se necesita ID inmediatamente
                log.info("Nueva EntradaAsignada persistida con ID: {}", entradaAsignada.getIdEntradaAsignada());
                return entradaAsignada;
            } else {
                // Entrada existente, usar merge para actualizar
                log.trace("Actualizando EntradaAsignada con ID: {}", entradaAsignada.getIdEntradaAsignada());
                EntradaAsignada merged = em.merge(entradaAsignada);
                log.info("EntradaAsignada actualizada con ID: {}", merged.getIdEntradaAsignada());
                return merged;
            }
        } catch (PersistenceException e) {
            // Capturar errores específicos, como violación de constraint único del QR
            log.error("Error de persistencia al guardar EntradaAsignada (ID: {}, QR: {}): {}",
                    entradaAsignada.getIdEntradaAsignada(), qrLog, e.getMessage(), e);
            throw e; // Relanzar para manejo transaccional
        } catch (Exception e) {
            log.error("Error inesperado al guardar EntradaAsignada (ID: {}, QR: {}): {}",
                    entradaAsignada.getIdEntradaAsignada(), qrLog, e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar EntradaAsignada", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<EntradaAsignada> findById(EntityManager em, Integer id) {
        log.debug("Buscando EntradaAsignada con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar EntradaAsignada con ID nulo.");
            return Optional.empty();
        }
        try {
            EntradaAsignada entrada = em.find(EntradaAsignada.class, id);
            return Optional.ofNullable(entrada);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar EntradaAsignada por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar EntradaAsignada por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<EntradaAsignada> findByCodigoQr(EntityManager em, String codigoQr) {
        String qrLog = (codigoQr != null) ? codigoQr.substring(0, Math.min(20, codigoQr.length())) + "..." : "null";
        log.debug("Buscando EntradaAsignada con QR: {}", qrLog);
        if (codigoQr == null || codigoQr.isBlank()) {
            log.warn("Intento de buscar EntradaAsignada con código QR nulo o vacío.");
            return Optional.empty();
        }
        try {
            TypedQuery<EntradaAsignada> query = em.createQuery(
                    "SELECT ea FROM EntradaAsignada ea WHERE ea.codigoQr = :qr", EntradaAsignada.class);
            query.setParameter("qr", codigoQr);
            EntradaAsignada entrada = query.getSingleResult();
            return Optional.of(entrada);
        } catch (NoResultException e) {
            log.trace("EntradaAsignada no encontrada con QR: {}", qrLog);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando EntradaAsignada por QR ({}): {}", qrLog, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntradaAsignada> findByCompraEntradaId(EntityManager em, Integer idCompraEntrada) {
        log.debug("Buscando EntradasAsignadas para CompraEntrada ID: {}", idCompraEntrada);
        if (idCompraEntrada == null) {
            log.warn("Intento de buscar entradas asignadas para un ID de CompraEntrada nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<EntradaAsignada> query = em.createQuery(
                    "SELECT ea FROM EntradaAsignada ea WHERE ea.compraEntrada.idCompraEntrada = :ceId ORDER BY ea.idEntradaAsignada", EntradaAsignada.class);
            query.setParameter("ceId", idCompraEntrada);
            List<EntradaAsignada> entradas = query.getResultList();
            log.debug("Encontradas {} EntradasAsignadas para CompraEntrada ID: {}", entradas.size(), idCompraEntrada);
            return entradas;
        } catch (Exception e) {
            log.error("Error buscando EntradasAsignadas para CompraEntrada ID {}: {}", idCompraEntrada, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntradaAsignada> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando EntradasAsignadas para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar entradas asignadas para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            // JPQL con Joins para navegar: EntradaAsignada -> CompraEntrada -> Entrada -> Festival
            String jpql = "SELECT ea FROM EntradaAsignada ea "
                    + "JOIN ea.compraEntrada ce "
                    + "JOIN ce.entrada e "
                    + "WHERE e.festival.idFestival = :festivalId "
                    + "ORDER BY ea.idEntradaAsignada"; // Ordenar por ID de la entrada asignada

            TypedQuery<EntradaAsignada> query = em.createQuery(jpql, EntradaAsignada.class);
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
