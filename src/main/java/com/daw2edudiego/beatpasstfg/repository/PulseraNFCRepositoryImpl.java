package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.PulseraNFC;
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
 * Implementación de PulseraNFCRepository usando JPA EntityManager.
 */
public class PulseraNFCRepositoryImpl implements PulseraNFCRepository {

    private static final Logger log = LoggerFactory.getLogger(PulseraNFCRepositoryImpl.class);

    @Override
    public PulseraNFC save(EntityManager em, PulseraNFC pulsera) {
        if (pulsera == null) {
            throw new IllegalArgumentException("La entidad PulseraNFC no puede ser nula.");
        }
        if (pulsera.getCodigoUid() == null || pulsera.getCodigoUid().isBlank()) {
            throw new IllegalArgumentException("El codigoUid de la PulseraNFC no puede ser nulo ni vacío.");
        }

        log.debug("Intentando guardar PulseraNFC ID: {}, UID: {}", pulsera.getIdPulsera(), pulsera.getCodigoUid());
        try {
            if (pulsera.getIdPulsera() == null) {
                log.trace("Persistiendo nueva PulseraNFC...");
                em.persist(pulsera);
                log.info("Nueva PulseraNFC persistida con ID: {}", pulsera.getIdPulsera());
                return pulsera;
            } else {
                log.trace("Actualizando PulseraNFC con ID: {}", pulsera.getIdPulsera());
                PulseraNFC merged = em.merge(pulsera);
                log.info("PulseraNFC actualizada con ID: {}", merged.getIdPulsera());
                return merged;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar PulseraNFC (ID: {}, UID: {}): {}",
                    pulsera.getIdPulsera(), pulsera.getCodigoUid(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar PulseraNFC (ID: {}, UID: {}): {}",
                    pulsera.getIdPulsera(), pulsera.getCodigoUid(), e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar PulseraNFC", e);
        }
    }

    @Override
    public Optional<PulseraNFC> findById(EntityManager em, Integer id) {
        log.debug("Buscando PulseraNFC ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar PulseraNFC con ID nulo.");
            return Optional.empty();
        }
        try {
            PulseraNFC pulsera = em.find(PulseraNFC.class, id);
            return Optional.ofNullable(pulsera);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar PulseraNFC por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado buscando PulseraNFC ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PulseraNFC> findByCodigoUid(EntityManager em, String codigoUid) {
        log.debug("Buscando PulseraNFC UID: {}", codigoUid);
        if (codigoUid == null || codigoUid.isBlank()) {
            log.warn("Intento de buscar PulseraNFC con código UID nulo o vacío.");
            return Optional.empty();
        }
        try {
            TypedQuery<PulseraNFC> query = em.createQuery(
                    "SELECT p FROM PulseraNFC p WHERE p.codigoUid = :uid", PulseraNFC.class);
            query.setParameter("uid", codigoUid);
            PulseraNFC pulsera = query.getSingleResult();
            return Optional.of(pulsera);
        } catch (NoResultException e) {
            log.trace("PulseraNFC no encontrada con UID: {}", codigoUid);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando PulseraNFC UID {}: {}", codigoUid, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PulseraNFC> findByEntradaId(EntityManager em, Integer idEntrada) {
        log.debug("Buscando PulseraNFC por Entrada ID: {}", idEntrada);
        if (idEntrada == null) {
            log.warn("Intento de buscar PulseraNFC por ID de Entrada nulo.");
            return Optional.empty();
        }
        try {
            TypedQuery<PulseraNFC> query = em.createQuery(
                    "SELECT p FROM PulseraNFC p WHERE p.entrada.idEntrada = :eaId", PulseraNFC.class);
            query.setParameter("eaId", idEntrada);
            List<PulseraNFC> results = query.getResultList();
            if (results.isEmpty()) {
                log.trace("No se encontró PulseraNFC para Entrada ID: {}", idEntrada);
                return Optional.empty();
            } else {
                if (results.size() > 1) {
                    log.warn("¡Inconsistencia! Múltiples PulserasNFC ({}) para Entrada ID: {}. Devolviendo la primera.", results.size(), idEntrada);
                }
                log.trace("Encontrada PulseraNFC ID {} para Entrada ID: {}", results.get(0).getIdPulsera(), idEntrada);
                return Optional.of(results.get(0));
            }
        } catch (Exception e) {
            log.error("Error buscando PulseraNFC por Entrada ID {}: {}", idEntrada, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<PulseraNFC> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando PulserasNFC para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar pulseras para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            // Consulta directa usando la relación PulseraNFC -> Festival
            String jpql = "SELECT p FROM PulseraNFC p WHERE p.festival.idFestival = :festivalId ORDER BY p.idPulsera";
            TypedQuery<PulseraNFC> query = em.createQuery(jpql, PulseraNFC.class);
            query.setParameter("festivalId", idFestival);
            List<PulseraNFC> pulseras = query.getResultList();
            log.debug("Encontradas {} PulserasNFC para Festival ID: {}", pulseras.size(), idFestival);
            return pulseras;
        } catch (Exception e) {
            log.error("Error buscando PulserasNFC para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
