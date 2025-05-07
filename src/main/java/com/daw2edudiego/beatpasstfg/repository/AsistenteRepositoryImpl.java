package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Asistente;
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
 * Implementación de AsistenteRepository usando JPA EntityManager.
 */
public class AsistenteRepositoryImpl implements AsistenteRepository {

    private static final Logger log = LoggerFactory.getLogger(AsistenteRepositoryImpl.class);

    @Override
    public Asistente save(EntityManager em, Asistente asistente) {
        if (asistente == null) {
            throw new IllegalArgumentException("La entidad Asistente no puede ser nula.");
        }
        log.debug("Intentando guardar Asistente con ID: {} y Email: {}", asistente.getIdAsistente(), asistente.getEmail());
        try {
            if (asistente.getIdAsistente() == null) {
                log.trace("Persistiendo nuevo Asistente...");
                em.persist(asistente);
                log.info("Nuevo Asistente persistido con ID: {}", asistente.getIdAsistente());
                return asistente;
            } else {
                log.trace("Actualizando Asistente con ID: {}", asistente.getIdAsistente());
                Asistente mergedAsistente = em.merge(asistente);
                log.info("Asistente actualizado con ID: {}", mergedAsistente.getIdAsistente());
                return mergedAsistente;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Asistente (ID: {}): {}",
                    asistente.getIdAsistente(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Asistente (ID: {}): {}",
                    asistente.getIdAsistente(), e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Asistente", e);
        }
    }

    @Override
    public Optional<Asistente> findById(EntityManager em, Integer id) {
        log.debug("Buscando Asistente con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Asistente con ID nulo.");
            return Optional.empty();
        }
        try {
            Asistente asistente = em.find(Asistente.class, id);
            return Optional.ofNullable(asistente);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar Asistente por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Asistente por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Asistente> findByEmail(EntityManager em, String email) {
        log.debug("Buscando Asistente con email: {}", email);
        if (email == null || email.isBlank()) {
            log.warn("Intento de buscar Asistente con email nulo o vacío.");
            return Optional.empty();
        }
        try {
            TypedQuery<Asistente> query = em.createQuery(
                    "SELECT a FROM Asistente a WHERE a.email = :emailParam", Asistente.class);
            query.setParameter("emailParam", email);
            Asistente asistente = query.getSingleResult();
            return Optional.of(asistente);
        } catch (NoResultException e) {
            log.trace("Asistente no encontrado con email: {}", email);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando Asistente por email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Asistente> findAsistentesByFestivalId(EntityManager em, Integer idFestival) {
        log.debug("Buscando Asistentes únicos para Festival ID: {}", idFestival);
        if (idFestival == null) {
            log.warn("Intento de buscar asistentes para un ID de festival nulo.");
            return Collections.emptyList();
        }
        try {
            String jpql = "SELECT DISTINCT a FROM Asistente a "
                    + "JOIN a.entradasAsignadas ea "
                    + "JOIN ea.compraEntrada ce "
                    + "JOIN ce.entrada e "
                    + "WHERE e.festival.idFestival = :festivalId "
                    + "ORDER BY a.nombre";

            TypedQuery<Asistente> query = em.createQuery(jpql, Asistente.class);
            query.setParameter("festivalId", idFestival);
            List<Asistente> asistentes = query.getResultList();
            log.debug("Encontrados {} asistentes únicos para Festival ID: {}", asistentes.size(), idFestival);
            return asistentes;
        } catch (Exception e) {
            log.error("Error buscando Asistentes para Festival ID {}: {}", idFestival, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Asistente> findAll(EntityManager em) {
        log.debug("Buscando todos los Asistentes");
        try {
            TypedQuery<Asistente> query = em.createQuery("SELECT a FROM Asistente a ORDER BY a.nombre", Asistente.class);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error buscando todos los Asistentes: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void delete(EntityManager em, Asistente asistente) {
        if (asistente == null || asistente.getIdAsistente() == null) {
            throw new IllegalArgumentException("El asistente a eliminar no puede ser nulo y debe tener un ID.");
        }
        log.debug("Intentando eliminar Asistente con ID: {}", asistente.getIdAsistente());
        try {
            Asistente managedAsistente = em.find(Asistente.class, asistente.getIdAsistente());
            if (managedAsistente != null) {
                em.remove(managedAsistente);
                log.info("Asistente eliminado con ID: {}", asistente.getIdAsistente());
            } else {
                log.warn("Intento de eliminar un Asistente que no existe en la BD (ID: {})", asistente.getIdAsistente());
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al eliminar Asistente (ID: {}): {}", asistente.getIdAsistente(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar Asistente (ID: {}): {}", asistente.getIdAsistente(), e.getMessage(), e);
            throw new PersistenceException("Error inesperado al eliminar Asistente", e);
        }
    }
}
