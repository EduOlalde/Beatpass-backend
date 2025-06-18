package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.TipoEntrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType; // Import LockModeType
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de TipoEntradaRepository usando JPA EntityManager.
 */
public class TipoEntradaRepositoryImpl implements TipoEntradaRepository {

    private static final Logger log = LoggerFactory.getLogger(TipoEntradaRepositoryImpl.class);

    @Override
    public TipoEntrada save(EntityManager em, TipoEntrada tipoEntrada) {
        if (tipoEntrada == null) {
            throw new IllegalArgumentException("La entidad Entrada no puede ser nula.");
        }
        if (tipoEntrada.getFestival() == null || tipoEntrada.getFestival().getIdFestival() == null) {
            throw new IllegalArgumentException("El Festival asociado a la Entrada no puede ser nulo y debe tener ID.");
        }

        String festivalIdStr = String.valueOf(tipoEntrada.getFestival().getIdFestival());
        log.debug("Intentando guardar Entrada con ID: {} para Festival ID: {}", tipoEntrada.getIdTipoEntrada(), festivalIdStr);
        try {
            if (tipoEntrada.getIdTipoEntrada() == null) {
                log.trace("Persistiendo nueva Entrada...");
                em.persist(tipoEntrada);
                log.info("Nueva Entrada persistida con ID: {}", tipoEntrada.getIdTipoEntrada());
                return tipoEntrada;
            } else {
                log.trace("Actualizando Entrada con ID: {}", tipoEntrada.getIdTipoEntrada());
                TipoEntrada mergedEntrada = em.merge(tipoEntrada);
                log.info("Entrada actualizada con ID: {}", mergedEntrada.getIdTipoEntrada());
                return mergedEntrada;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Entrada (ID: {}): {}", tipoEntrada.getIdTipoEntrada(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Entrada (ID: {}): {}", tipoEntrada.getIdTipoEntrada(), e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Entrada", e);
        }
    }

    @Override
    public Optional<TipoEntrada> findById(EntityManager em, Integer id) {
        return findById(em, id, null); 
    }

    @Override
    public Optional<TipoEntrada> findById(EntityManager em, Integer id, LockModeType lockMode) { 
        log.debug("Buscando Entrada con ID: {} (LockMode: {})", id, lockMode);
        if (id == null) {
            log.warn("Intento de buscar Entrada con ID nulo.");
            return Optional.empty();
        }
        try {
            TipoEntrada tipoEntrada;
            if (lockMode != null) {
                tipoEntrada = em.find(TipoEntrada.class, id, lockMode);
            } else {
                tipoEntrada = em.find(TipoEntrada.class, id);
            }
            return Optional.ofNullable(tipoEntrada);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar Entrada por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Entrada por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<TipoEntrada> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando Tipos de Entrada para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar tipos de entrada para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            // CONSULTA CORREGIDA
            TypedQuery<TipoEntrada> query = em.createQuery(
                    "SELECT te FROM TipoEntrada te WHERE te.festival.idFestival = :festivalId ORDER BY te.tipo",
                    TipoEntrada.class
            );
            query.setParameter("festivalId", idFestival);
            List<TipoEntrada> tiposEntrada = query.getResultList();
            log.debug("Encontrados {} Tipos de Entrada para Festival ID: {}", tiposEntrada.size(), idFestival);
            return tiposEntrada;
        } catch (Exception e) {
            log.error("Error buscando Tipos de Entrada para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean deleteById(EntityManager em, Integer id) {
        log.debug("Intentando eliminar Entrada con ID: {}", id);
        if (id == null) {
            log.warn("Intento de eliminar Entrada con ID nulo.");
            return false;
        }
        Optional<TipoEntrada> tipoEntradaOpt = findById(em, id);
        if (tipoEntradaOpt.isPresent()) {
            try {
                em.remove(tipoEntradaOpt.get());
                log.info("Entrada ID: {} marcada para eliminación.", id);
                return true;
            } catch (PersistenceException e) {
                log.error("Error de persistencia al eliminar Entrada ID {}: {}. Causa probable: existen detalles de compra asociados.",
                        id, e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error inesperado al eliminar Entrada ID {}: {}", id, e.getMessage(), e);
                throw new PersistenceException("Error inesperado al eliminar Entrada", e);
            }
        } else {
            log.warn("No se pudo eliminar. Entrada no encontrada con ID: {}", id);
            return false;
        }
    }
}
