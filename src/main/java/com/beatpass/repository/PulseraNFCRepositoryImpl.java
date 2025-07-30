package com.beatpass.repository;

import com.beatpass.model.PulseraNFC;
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
 * Implementación de PulseraNFCRepository usando JPA EntityManager.
 */
@ApplicationScoped
public class PulseraNFCRepositoryImpl implements PulseraNFCRepository {

    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;
    private static final Logger log = LoggerFactory.getLogger(PulseraNFCRepositoryImpl.class);

    @Override
    public PulseraNFC save(PulseraNFC pulsera) {
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

    private String getComprehensivePulseraQuery() {
        // CORRECTED: Removed aliases from JOIN FETCH statements for strict JPQL compliance.
        return "SELECT p FROM PulseraNFC p "
                + "LEFT JOIN FETCH p.festival "
                + "LEFT JOIN FETCH p.entrada "
                + "LEFT JOIN FETCH p.entrada.asistente ";
    }

    @Override
    public Optional<PulseraNFC> findById(Integer id) {
        log.debug("Buscando PulseraNFC ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar PulseraNFC con ID nulo.");
            return Optional.empty();
        }
        try {
            TypedQuery<PulseraNFC> query = em.createQuery(getComprehensivePulseraQuery() + "WHERE p.idPulsera = :id", PulseraNFC.class);
            query.setParameter("id", id);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado buscando PulseraNFC ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PulseraNFC> findByCodigoUid(String codigoUid) {
        log.debug("Buscando PulseraNFC UID: {}", codigoUid);
        if (codigoUid == null || codigoUid.isBlank()) {
            log.warn("Intento de buscar PulseraNFC con código UID nulo o vacío.");
            return Optional.empty();
        }
        try {
            TypedQuery<PulseraNFC> query = em.createQuery(getComprehensivePulseraQuery() + "WHERE p.codigoUid = :uid", PulseraNFC.class);
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
    public Optional<PulseraNFC> findByEntradaId(Integer idEntrada) {
        log.debug("Buscando PulseraNFC por Entrada ID: {}", idEntrada);
        if (idEntrada == null) {
            log.warn("Intento de buscar PulseraNFC por ID de Entrada nulo.");
            return Optional.empty();
        }
        try {
            TypedQuery<PulseraNFC> query = em.createQuery(getComprehensivePulseraQuery() + "WHERE p.entrada.idEntrada = :eaId", PulseraNFC.class);
            query.setParameter("eaId", idEntrada);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando PulseraNFC por Entrada ID {}: {}", idEntrada, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<PulseraNFC> findByFestivalId(Integer idFestival) {
        log.debug("Buscando PulserasNFC para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar pulseras para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<PulseraNFC> query = em.createQuery(getComprehensivePulseraQuery() + "WHERE p.festival.idFestival = :festivalId ORDER BY p.idPulsera", PulseraNFC.class);
            query.setParameter("festivalId", idFestival);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando PulserasNFC para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
