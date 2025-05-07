package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Entrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de EntradaRepository usando JPA EntityManager.
 */
public class EntradaRepositoryImpl implements EntradaRepository {

    private static final Logger log = LoggerFactory.getLogger(EntradaRepositoryImpl.class);

    @Override
    public Entrada save(EntityManager em, Entrada entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("La entidad Entrada no puede ser nula.");
        }
        if (entrada.getFestival() == null || entrada.getFestival().getIdFestival() == null) {
            throw new IllegalArgumentException("El Festival asociado a la Entrada no puede ser nulo y debe tener ID.");
        }

        String festivalIdStr = String.valueOf(entrada.getFestival().getIdFestival());
        log.debug("Intentando guardar Entrada con ID: {} para Festival ID: {}", entrada.getIdEntrada(), festivalIdStr);
        try {
            if (entrada.getIdEntrada() == null) {
                log.trace("Persistiendo nueva Entrada...");
                em.persist(entrada);
                log.info("Nueva Entrada persistida con ID: {}", entrada.getIdEntrada());
                return entrada;
            } else {
                log.trace("Actualizando Entrada con ID: {}", entrada.getIdEntrada());
                Entrada mergedEntrada = em.merge(entrada);
                log.info("Entrada actualizada con ID: {}", mergedEntrada.getIdEntrada());
                return mergedEntrada;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Entrada (ID: {}): {}", entrada.getIdEntrada(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Entrada (ID: {}): {}", entrada.getIdEntrada(), e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Entrada", e);
        }
    }

    @Override
    public Optional<Entrada> findById(EntityManager em, Integer id) {
        log.debug("Buscando Entrada con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Entrada con ID nulo.");
            return Optional.empty();
        }
        try {
            Entrada entrada = em.find(Entrada.class, id);
            return Optional.ofNullable(entrada);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar Entrada por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Entrada por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Entrada> findByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando Entradas para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar entradas para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Entrada> query = em.createQuery(
                    "SELECT e FROM Entrada e WHERE e.festival.idFestival = :festivalId ORDER BY e.tipo",
                    Entrada.class);
            query.setParameter("festivalId", idFestival);
            List<Entrada> entradas = query.getResultList();
            log.debug("Encontradas {} Entradas para Festival ID: {}", entradas.size(), idFestival);
            return entradas;
        } catch (Exception e) {
            log.error("Error buscando Entradas para Festival ID {}: {}", idFestival, e.getMessage(), e);
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
        Optional<Entrada> entradaOpt = findById(em, id);
        if (entradaOpt.isPresent()) {
            try {
                em.remove(entradaOpt.get());
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
