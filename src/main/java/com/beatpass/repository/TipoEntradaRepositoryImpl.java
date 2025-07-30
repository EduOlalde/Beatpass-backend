package com.beatpass.repository;

import com.beatpass.model.TipoEntrada;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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
 * Implementación de TipoEntradaRepository usando JPA EntityManager.
 */
@ApplicationScoped
public class TipoEntradaRepositoryImpl implements TipoEntradaRepository {

    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;
    private static final Logger log = LoggerFactory.getLogger(TipoEntradaRepositoryImpl.class);

    @Override
    public TipoEntrada save(TipoEntrada tipoEntrada) {
        if (tipoEntrada == null) {
            throw new IllegalArgumentException("La entidad Entrada no puede ser nula.");
        }
        if (tipoEntrada.getFestival() == null || tipoEntrada.getFestival().getIdFestival() == null) {
            throw new IllegalArgumentException("El Festival asociado a la Entrada no puede ser nulo y debe tener ID.");
        }

        String festivalIdStr = String.valueOf(tipoEntrada.getFestival().getIdFestival());
        log.debug("Intentando guardar Entrada dengan ID: {} untuk Festival ID: {}", tipoEntrada.getIdTipoEntrada(), festivalIdStr);
        try {
            if (tipoEntrada.getIdTipoEntrada() == null) {
                log.trace("Persistiendo nueva Entrada...");
                em.persist(tipoEntrada);
                log.info("Nueva Entrada persistida dengan ID: {}", tipoEntrada.getIdTipoEntrada());
                return tipoEntrada;
            } else {
                log.trace("Actualizando Entrada dengan ID: {}", tipoEntrada.getIdTipoEntrada());
                TipoEntrada mergedEntrada = em.merge(tipoEntrada);
                log.info("Entrada actualizada dengan ID: {}", mergedEntrada.getIdTipoEntrada());
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
    public Optional<TipoEntrada> findById(Integer id) {
        return findById(id, null);
    }

    @Override
    public Optional<TipoEntrada> findById(Integer id, LockModeType lockMode) {
        log.debug("Buscando Entrada dengan ID: {} (LockMode: {})", id, lockMode);
        if (id == null) {
            log.warn("Intento de buscar Entrada dengan ID nulo.");
            return Optional.empty();
        }
        try {
            String jpql = "SELECT te FROM TipoEntrada te LEFT JOIN FETCH te.festival WHERE te.idTipoEntrada = :id";
            TypedQuery<TipoEntrada> query = em.createQuery(jpql, TipoEntrada.class);
            query.setParameter("id", id);

            if (lockMode != null) {
                query.setLockMode(lockMode);
            }

            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            log.trace("TipoEntrada tidak ditemukan dengan ID: {}", id);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Entrada por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<TipoEntrada> findByFestivalId(Integer idFestival) {
        log.debug("Buscando Tipos de Entrada untuk Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar tipos de entrada untuk ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<TipoEntrada> query = em.createQuery(
                    "SELECT te FROM TipoEntrada te LEFT JOIN FETCH te.festival WHERE te.festival.idFestival = :festivalId ORDER BY te.tipo",
                    TipoEntrada.class
            );
            query.setParameter("festivalId", idFestival);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando Tipos de Entrada untuk Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        log.debug("Intentando eliminar Entrada dengan ID: {}", id);
        if (id == null) {
            log.warn("Intento de eliminar Entrada dengan ID nulo.");
            return false;
        }
        Optional<TipoEntrada> tipoEntradaOpt = findById(id);
        if (tipoEntradaOpt.isPresent()) {
            try {
                em.remove(tipoEntradaOpt.get());
                log.info("Entrada ID: {} marcado para eliminación.", id);
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
            log.warn("No se pudo eliminar. Entrada no encontrada dengan ID: {}", id);
            return false;
        }
    }
}
