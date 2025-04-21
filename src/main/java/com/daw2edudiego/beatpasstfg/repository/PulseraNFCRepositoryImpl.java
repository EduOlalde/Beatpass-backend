package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.PulseraNFC;
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
 * Implementación de PulseraNFCRepository usando JPA.
 */
public class PulseraNFCRepositoryImpl implements PulseraNFCRepository {

    private static final Logger log = LoggerFactory.getLogger(PulseraNFCRepositoryImpl.class);

    @Override
    public PulseraNFC save(EntityManager em, PulseraNFC pulsera) {
        if (pulsera == null || pulsera.getCodigoUid() == null) {
            throw new IllegalArgumentException("La entidad PulseraNFC y su codigoUid no pueden ser nulos.");
        }
        log.debug("Intentando guardar PulseraNFC ID: {}, UID: {}", pulsera.getIdPulsera(), pulsera.getCodigoUid());
        try {
            if (pulsera.getIdPulsera() == null) {
                em.persist(pulsera);
                em.flush(); // Opcional
                log.info("Nueva PulseraNFC persistida con ID: {}", pulsera.getIdPulsera());
                return pulsera;
            } else {
                PulseraNFC merged = em.merge(pulsera);
                log.info("PulseraNFC actualizada con ID: {}", merged.getIdPulsera());
                return merged;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar PulseraNFC (ID: {}, UID: {}): {}",
                    pulsera.getIdPulsera(), pulsera.getCodigoUid(), e.getMessage(), e);
            // Podría ser por UID duplicado
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
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(em.find(PulseraNFC.class, id));
        } catch (Exception e) {
            log.error("Error buscando PulseraNFC ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PulseraNFC> findByCodigoUid(EntityManager em, String codigoUid) {
        log.debug("Buscando PulseraNFC UID: {}", codigoUid);
        if (codigoUid == null || codigoUid.isBlank()) {
            return Optional.empty();
        }
        try {
            TypedQuery<PulseraNFC> query = em.createQuery(
                    "SELECT p FROM PulseraNFC p WHERE p.codigoUid = :uid", PulseraNFC.class);
            query.setParameter("uid", codigoUid);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            log.trace("PulseraNFC no encontrada con UID: {}", codigoUid);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando PulseraNFC UID {}: {}", codigoUid, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PulseraNFC> findByEntradaAsignadaId(EntityManager em, Integer idEntradaAsignada) {
        log.debug("Buscando PulseraNFC por EntradaAsignada ID: {}", idEntradaAsignada);
        if (idEntradaAsignada == null) {
            return Optional.empty();
        }
        try {
            // Asume que la relación es bidireccional o que PulseraNFC tiene la FK
            TypedQuery<PulseraNFC> query = em.createQuery(
                    "SELECT p FROM PulseraNFC p WHERE p.entradaAsignada.idEntradaAsignada = :eaId", PulseraNFC.class);
            query.setParameter("eaId", idEntradaAsignada);
            // Usamos getResultList porque podría no haber ninguna, getSingleResult lanzaría NoResultException
            List<PulseraNFC> results = query.getResultList();
            if (results.isEmpty()) {
                log.trace("No se encontró PulseraNFC para EntradaAsignada ID: {}", idEntradaAsignada);
                return Optional.empty();
            } else if (results.size() > 1) {
                // Esto indicaría un problema de datos, una entrada solo debe tener una pulsera
                log.warn("Se encontraron múltiples PulserasNFC para EntradaAsignada ID: {}. Devolviendo la primera.", idEntradaAsignada);
                return Optional.of(results.get(0));
            } else {
                log.trace("Encontrada PulseraNFC ID {} para EntradaAsignada ID: {}", results.get(0).getIdPulsera(), idEntradaAsignada);
                return Optional.of(results.get(0));
            }
        } catch (Exception e) {
            log.error("Error buscando PulseraNFC por EntradaAsignada ID {}: {}", idEntradaAsignada, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<PulseraNFC> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando PulserasNFC para Festival ID: {}", idFestival);
        if (idFestival == null) {
            return Collections.emptyList();
        }
        try {
            // Similar a buscar asistentes, necesitamos joins
            String jpql = "SELECT p FROM PulseraNFC p "
                    + "JOIN p.entradaAsignada ea "
                    + "JOIN ea.compraEntrada ce "
                    + "JOIN ce.entrada e "
                    + "WHERE e.festival.idFestival = :festivalId "
                    + "ORDER BY p.idPulsera";
            TypedQuery<PulseraNFC> query = em.createQuery(jpql, PulseraNFC.class);
            query.setParameter("festivalId", idFestival);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando PulserasNFC para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
