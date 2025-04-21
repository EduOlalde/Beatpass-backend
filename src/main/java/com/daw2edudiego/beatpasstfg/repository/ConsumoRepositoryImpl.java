package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Consumo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de ConsumoRepository usando JPA.
 */
public class ConsumoRepositoryImpl implements ConsumoRepository {

    private static final Logger log = LoggerFactory.getLogger(ConsumoRepositoryImpl.class);

    @Override
    public Consumo save(EntityManager em, Consumo consumo) {
        if (consumo == null || consumo.getPulseraNFC() == null || consumo.getMonto() == null || consumo.getFestival() == null) {
            throw new IllegalArgumentException("Consumo, Pulsera, Monto y Festival asociados no pueden ser nulos.");
        }
        log.debug("Intentando guardar Consumo para Pulsera ID: {} en Festival ID: {}",
                consumo.getPulseraNFC().getIdPulsera(), consumo.getFestival().getIdFestival());
        try {
            // Siempre es nuevo
            em.persist(consumo);
            em.flush();
            log.info("Nuevo Consumo persistido con ID: {}", consumo.getIdConsumo());
            return consumo;
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Consumo: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Consumo: {}", e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Consumo", e);
        }
    }

    @Override
    public Optional<Consumo> findById(EntityManager em, Integer id) {
        log.debug("Buscando Consumo ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(em.find(Consumo.class, id));
        } catch (Exception e) {
            log.error("Error buscando Consumo ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Consumo> findByPulseraId(EntityManager em, Integer idPulsera) {
        log.debug("Buscando Consumos para Pulsera ID: {}", idPulsera);
        if (idPulsera == null) {
            return Collections.emptyList();
        }
        try {
            TypedQuery<Consumo> query = em.createQuery(
                    "SELECT c FROM Consumo c WHERE c.pulseraNFC.idPulsera = :pulseraId ORDER BY c.fecha DESC", Consumo.class);
            query.setParameter("pulseraId", idPulsera);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando Consumos para Pulsera ID {}: {}", idPulsera, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Consumo> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando Consumos para Festival ID: {}", idFestival);
        if (idFestival == null) {
            return Collections.emptyList();
        }
        try {
            TypedQuery<Consumo> query = em.createQuery(
                    "SELECT c FROM Consumo c WHERE c.festival.idFestival = :festivalId ORDER BY c.fecha DESC", Consumo.class);
            query.setParameter("festivalId", idFestival);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando Consumos para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
