package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.Asistente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Implementación de AsistenteRepository usando JPA.
 *
 * @author Eduardo Olalde
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
                em.persist(asistente);
                em.flush(); // Opcional
                log.info("Nuevo Asistente persistido con ID: {}", asistente.getIdAsistente());
                return asistente;
            } else {
                Asistente mergedAsistente = em.merge(asistente);
                log.info("Asistente actualizado con ID: {}", mergedAsistente.getIdAsistente());
                return mergedAsistente;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Asistente (ID: {}): {}", asistente.getIdAsistente(), e.getMessage(), e);
            // Podría ser por email duplicado (UniqueConstraintViolation)
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Asistente (ID: {}): {}", asistente.getIdAsistente(), e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Asistente", e);
        }
    }

    @Override
    public Optional<Asistente> findById(EntityManager em, Integer id) {
        log.debug("Buscando Asistente con ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(em.find(Asistente.class, id));
        } catch (Exception e) {
            log.error("Error al buscar Asistente por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Asistente> findByEmail(EntityManager em, String email) {
        log.debug("Buscando Asistente con email: {}", email);
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            TypedQuery<Asistente> query = em.createQuery(
                    "SELECT a FROM Asistente a WHERE a.email = :emailParam", Asistente.class);
            query.setParameter("emailParam", email);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            log.trace("Asistente no encontrado con email: {}", email);
            return Optional.empty(); // No encontrado, normal
        } catch (Exception e) {
            log.error("Error buscando Asistente por email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Asistente> findAsistentesByFestivalId(EntityManager em, Integer idFestival) { // <-- NUEVO MÉTODO
        log.debug("Buscando Asistentes para Festival ID: {}", idFestival);
        if (idFestival == null) {
            return Collections.emptyList();
        }
        try {
            // Consulta JPQL para obtener asistentes únicos asociados a entradas asignadas de un festival
            String jpql = "SELECT DISTINCT a FROM Asistente a "
                    + "JOIN a.entradasAsignadas ea "
                    + // Asistente -> EntradaAsignada
                    "JOIN ea.compraEntrada ce "
                    + // EntradaAsignada -> CompraEntrada
                    "JOIN ce.entrada e "
                    + // CompraEntrada -> Entrada (tipo)
                    "WHERE e.festival.idFestival = :festivalId "
                    + // Filtrar por ID de Festival
                    "ORDER BY a.nombre"; // Ordenar por nombre

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
}
