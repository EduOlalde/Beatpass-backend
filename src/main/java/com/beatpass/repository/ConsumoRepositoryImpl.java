package com.beatpass.repository;

import com.beatpass.model.Consumo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de ConsumoRepository usando JPA EntityManager.
 */
public class ConsumoRepositoryImpl implements ConsumoRepository {

    private static final Logger log = LoggerFactory.getLogger(ConsumoRepositoryImpl.class);

    @Override
    public Consumo save(EntityManager em, Consumo consumo) {
        if (consumo == null) {
            throw new IllegalArgumentException("La entidad Consumo no puede ser nula.");
        }
        if (consumo.getPulseraNFC() == null || consumo.getPulseraNFC().getIdPulsera() == null) {
            throw new IllegalArgumentException("La PulseraNFC asociada al Consumo no puede ser nula y debe tener ID.");
        }
        if (consumo.getFestival() == null || consumo.getFestival().getIdFestival() == null) {
            throw new IllegalArgumentException("El Festival asociado al Consumo no puede ser nulo y debe tener ID.");
        }
        if (consumo.getMonto() == null) {
            throw new IllegalArgumentException("El monto del Consumo no puede ser nulo.");
        }

        log.debug("Intentando guardar Consumo para Pulsera ID: {} en Festival ID: {}",
                consumo.getPulseraNFC().getIdPulsera(), consumo.getFestival().getIdFestival());
        try {
            em.persist(consumo);
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
        log.debug("Buscando Consumo con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Consumo con ID nulo.");
            return Optional.empty();
        }
        try {
            Consumo consumo = em.find(Consumo.class, id);
            return Optional.ofNullable(consumo);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar Consumo por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Consumo por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Consumo> findByPulseraId(EntityManager em, Integer idPulsera) {
        log.debug("Buscando Consumos para Pulsera ID: {}", idPulsera);
        if (idPulsera == null) {
            log.warn("Intento de buscar consumos para un ID de pulsera nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Consumo> query = em.createQuery(
                    "SELECT c FROM Consumo c WHERE c.pulseraNFC.idPulsera = :pulseraId ORDER BY c.fecha DESC", Consumo.class);
            query.setParameter("pulseraId", idPulsera);
            List<Consumo> consumos = query.getResultList();
            log.debug("Encontrados {} consumos para Pulsera ID: {}", consumos.size(), idPulsera);
            return consumos;
        } catch (Exception e) {
            log.error("Error buscando Consumos para Pulsera ID {}: {}", idPulsera, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Consumo> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando Consumos para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar consumos para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Consumo> query = em.createQuery(
                    "SELECT c FROM Consumo c WHERE c.festival.idFestival = :festivalId ORDER BY c.fecha DESC", Consumo.class);
            query.setParameter("festivalId", idFestival);
            List<Consumo> consumos = query.getResultList();
            log.debug("Encontrados {} consumos para Festival ID: {}", consumos.size(), idFestival);
            return consumos;
        } catch (Exception e) {
            log.error("Error buscando Consumos para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
