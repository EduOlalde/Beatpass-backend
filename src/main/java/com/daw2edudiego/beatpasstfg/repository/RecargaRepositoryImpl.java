package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Recarga;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de RecargaRepository usando JPA EntityManager.
 */
public class RecargaRepositoryImpl implements RecargaRepository {

    private static final Logger log = LoggerFactory.getLogger(RecargaRepositoryImpl.class);

    @Override
    public Recarga save(EntityManager em, Recarga recarga) {
        if (recarga == null) {
            throw new IllegalArgumentException("La entidad Recarga no puede ser nula.");
        }
        if (recarga.getPulseraNFC() == null || recarga.getPulseraNFC().getIdPulsera() == null) {
            throw new IllegalArgumentException("La PulseraNFC asociada a la Recarga no puede ser nula y debe tener ID.");
        }
        if (recarga.getMonto() == null) {
            throw new IllegalArgumentException("El monto de la Recarga no puede ser nulo.");
        }

        log.debug("Intentando guardar Recarga para Pulsera ID: {}", recarga.getPulseraNFC().getIdPulsera());
        try {
            em.persist(recarga);
            log.info("Nueva Recarga persistida con ID: {}", recarga.getIdRecarga());
            return recarga;
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Recarga: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Recarga: {}", e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Recarga", e);
        }
    }

    @Override
    public Optional<Recarga> findById(EntityManager em, Integer id) {
        log.debug("Buscando Recarga ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Recarga con ID nulo.");
            return Optional.empty();
        }
        try {
            Recarga recarga = em.find(Recarga.class, id);
            return Optional.ofNullable(recarga);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar Recarga por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado buscando Recarga ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Recarga> findByPulseraId(EntityManager em, Integer idPulsera) {
        log.debug("Buscando Recargas para Pulsera ID: {}", idPulsera);
        if (idPulsera == null) {
            log.warn("Intento de buscar recargas para un ID de pulsera nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Recarga> query = em.createQuery(
                    "SELECT r FROM Recarga r WHERE r.pulseraNFC.idPulsera = :pulseraId ORDER BY r.fecha DESC", Recarga.class);
            query.setParameter("pulseraId", idPulsera);
            List<Recarga> recargas = query.getResultList();
            log.debug("Encontradas {} recargas para Pulsera ID: {}", recargas.size(), idPulsera);
            return recargas;
        } catch (Exception e) {
            log.error("Error buscando Recargas para Pulsera ID {}: {}", idPulsera, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
