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
 * Implementación de {@link PulseraNFCRepository} utilizando JPA EntityManager.
 * Proporciona la lógica concreta para interactuar con la base de datos para la
 * entidad PulseraNFC.
 *
 * @author Eduardo Olalde
 */
public class PulseraNFCRepositoryImpl implements PulseraNFCRepository {

    private static final Logger log = LoggerFactory.getLogger(PulseraNFCRepositoryImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public PulseraNFC save(EntityManager em, PulseraNFC pulsera) {
        // Validación de precondiciones
        if (pulsera == null) {
            throw new IllegalArgumentException("La entidad PulseraNFC no puede ser nula.");
        }
        if (pulsera.getCodigoUid() == null || pulsera.getCodigoUid().isBlank()) {
            throw new IllegalArgumentException("El codigoUid de la PulseraNFC no puede ser nulo ni vacío.");
        }

        log.debug("Intentando guardar PulseraNFC ID: {}, UID: {}", pulsera.getIdPulsera(), pulsera.getCodigoUid());
        try {
            if (pulsera.getIdPulsera() == null) {
                // Nueva pulsera, usar persist
                log.trace("Persistiendo nueva PulseraNFC...");
                em.persist(pulsera);
                // em.flush(); // Descomentar si se necesita ID inmediatamente
                log.info("Nueva PulseraNFC persistida con ID: {}", pulsera.getIdPulsera());
                return pulsera;
            } else {
                // Pulsera existente, usar merge para actualizar
                log.trace("Actualizando PulseraNFC con ID: {}", pulsera.getIdPulsera());
                PulseraNFC merged = em.merge(pulsera);
                log.info("PulseraNFC actualizada con ID: {}", merged.getIdPulsera());
                return merged;
            }
        } catch (PersistenceException e) {
            // Capturar errores como violación de constraint único del UID
            log.error("Error de persistencia al guardar PulseraNFC (ID: {}, UID: {}): {}",
                    pulsera.getIdPulsera(), pulsera.getCodigoUid(), e.getMessage(), e);
            throw e; // Relanzar para manejo transaccional
        } catch (Exception e) {
            log.error("Error inesperado al guardar PulseraNFC (ID: {}, UID: {}): {}",
                    pulsera.getIdPulsera(), pulsera.getCodigoUid(), e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar PulseraNFC", e);
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PulseraNFC> findByEntradaAsignadaId(EntityManager em, Integer idEntradaAsignada) {
        log.debug("Buscando PulseraNFC por EntradaAsignada ID: {}", idEntradaAsignada);
        if (idEntradaAsignada == null) {
            log.warn("Intento de buscar PulseraNFC por ID de EntradaAsignada nulo.");
            return Optional.empty();
        }
        try {
            // Consulta JPQL para buscar la pulsera a través de la relación
            // Asume que la relación OneToOne está mapeada en PulseraNFC con el campo 'entradaAsignada'
            TypedQuery<PulseraNFC> query = em.createQuery(
                    "SELECT p FROM PulseraNFC p WHERE p.entradaAsignada.idEntradaAsignada = :eaId", PulseraNFC.class);
            query.setParameter("eaId", idEntradaAsignada);
            // Usar getResultList para manejar el caso de 0 resultados sin excepción
            List<PulseraNFC> results = query.getResultList();
            if (results.isEmpty()) {
                log.trace("No se encontró PulseraNFC para EntradaAsignada ID: {}", idEntradaAsignada);
                return Optional.empty();
            } else {
                // Debería haber solo una por la constraint UNIQUE en la BD, pero por si acaso...
                if (results.size() > 1) {
                    log.warn("¡Inconsistencia de datos! Se encontraron múltiples PulserasNFC ({}) para una única EntradaAsignada ID: {}. Devolviendo la primera.", results.size(), idEntradaAsignada);
                }
                log.trace("Encontrada PulseraNFC ID {} para EntradaAsignada ID: {}", results.get(0).getIdPulsera(), idEntradaAsignada);
                return Optional.of(results.get(0));
            }
        } catch (Exception e) {
            log.error("Error buscando PulseraNFC por EntradaAsignada ID {}: {}", idEntradaAsignada, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PulseraNFC> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando PulserasNFC para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar pulseras para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            // JPQL con Joins: PulseraNFC -> EntradaAsignada -> CompraEntrada -> Entrada -> Festival
            String jpql = "SELECT DISTINCT p FROM PulseraNFC p " // DISTINCT por si hubiera mapeos extraños
                    + "JOIN p.entradaAsignada ea " // Pulsera tiene que estar asociada a una entrada
                    + "JOIN ea.compraEntrada ce "
                    + "JOIN ce.entrada e "
                    + "WHERE e.festival.idFestival = :festivalId "
                    + "ORDER BY p.idPulsera"; // Ordenar por ID de pulsera

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
