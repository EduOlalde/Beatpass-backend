package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.Festival;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de FestivalRepository usando JPA EntityManager.
 */
public class FestivalRepositoryImpl implements FestivalRepository {

    private static final Logger log = LoggerFactory.getLogger(FestivalRepositoryImpl.class);

    @Override
    public Festival save(EntityManager em, Festival festival) {
        if (festival == null) {
            throw new IllegalArgumentException("La entidad Festival no puede ser nula.");
        }
        if (festival.getPromotor() == null || festival.getPromotor().getIdUsuario() == null) {
            throw new IllegalArgumentException("El Promotor asociado al Festival no puede ser nulo y debe tener ID.");
        }

        log.debug("Intentando guardar festival con ID: {}", festival.getIdFestival());
        try {
            if (festival.getIdFestival() == null) {
                log.trace("Persistiendo nuevo Festival...");
                em.persist(festival);
                log.info("Nuevo Festival persistido con ID: {}", festival.getIdFestival());
                return festival;
            } else {
                log.trace("Actualizando Festival con ID: {}", festival.getIdFestival());
                Festival mergedFestival = em.merge(festival);
                log.info("Festival actualizado con ID: {}", mergedFestival.getIdFestival());
                return mergedFestival;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Festival (ID: {}): {}", festival.getIdFestival(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Festival (ID: {}): {}", festival.getIdFestival(), e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Festival", e);
        }
    }

    @Override
    public Optional<Festival> findById(EntityManager em, Integer id) {
        log.debug("Buscando festival con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Festival con ID nulo.");
            return Optional.empty();
        }
        try {
            Festival festival = em.find(Festival.class, id);
            return Optional.ofNullable(festival);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar Festival por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al buscar Festival por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(EntityManager em, Integer id) {
        log.debug("Intentando eliminar festival con ID: {}", id);
        if (id == null) {
            log.warn("Intento de eliminar Festival con ID nulo.");
            return false;
        }
        Optional<Festival> festivalOpt = findById(em, id);
        if (festivalOpt.isPresent()) {
            try {
                em.remove(festivalOpt.get());
                log.info("Festival con ID: {} marcado para eliminación.", id);
                return true;
            } catch (PersistenceException e) {
                log.error("Error de persistencia al eliminar Festival ID {}: {}. Considerar relaciones en cascada.", id, e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error inesperado al eliminar Festival ID {}: {}", id, e.getMessage(), e);
                throw new PersistenceException("Error inesperado al eliminar Festival", e);
            }
        } else {
            log.warn("No se pudo eliminar. Festival no encontrado con ID: {}", id);
            return false;
        }
    }

    @Override
    public List<Festival> findAll(EntityManager em) {
        log.debug("Buscando todos los festivales.");
        try {
            TypedQuery<Festival> query = em.createQuery("SELECT f FROM Festival f ORDER BY f.nombre", Festival.class);
            List<Festival> festivales = query.getResultList();
            log.debug("Encontrados {} festivales.", festivales.size());
            return festivales;
        } catch (Exception e) {
            log.error("Error al buscar todos los festivales: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Festival> findByEstado(EntityManager em, EstadoFestival estado) {
        log.debug("Buscando festivales con estado: {}", estado);
        if (estado == null) {
            log.warn("Intento de buscar festivales con estado nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Festival> query = em.createQuery(
                    "SELECT f FROM Festival f WHERE f.estado = :estadoParam ORDER BY f.fechaInicio", Festival.class);
            query.setParameter("estadoParam", estado);
            List<Festival> festivales = query.getResultList();
            log.debug("Encontrados {} festivales con estado {}.", festivales.size(), estado);
            return festivales;
        } catch (Exception e) {
            log.error("Error al buscar festivales por estado {}: {}", estado, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Festival> findActivosEntreFechas(EntityManager em, LocalDate fechaDesde, LocalDate fechaHasta) {
        log.debug("Buscando festivales activos (PUBLICADO) entre {} y {}", fechaDesde, fechaHasta);
        if (fechaDesde == null || fechaHasta == null || fechaHasta.isBefore(fechaDesde)) {
            log.warn("Fechas inválidas para la búsqueda de festivales activos: desde={}, hasta={}", fechaDesde, fechaHasta);
            return Collections.emptyList();
        }
        try {
            TypedQuery<Festival> query = em.createQuery(
                    "SELECT f FROM Festival f WHERE f.estado = :estadoPublicado "
                    + "AND f.fechaInicio <= :fechaHasta "
                    + "AND f.fechaFin >= :fechaDesde "
                    + "ORDER BY f.fechaInicio", Festival.class);
            query.setParameter("estadoPublicado", EstadoFestival.PUBLICADO);
            query.setParameter("fechaHasta", fechaHasta);
            query.setParameter("fechaDesde", fechaDesde);
            List<Festival> festivales = query.getResultList();
            log.debug("Encontrados {} festivales activos entre {} y {}.", festivales.size(), fechaDesde, fechaHasta);
            return festivales;
        } catch (Exception e) {
            log.error("Error buscando festivales activos entre fechas ({} - {}): {}", fechaDesde, fechaHasta, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Festival> findByPromotorId(EntityManager em, Integer idPromotor) {
        log.debug("Buscando festivales para el promotor ID: {}", idPromotor);
        if (idPromotor == null) {
            log.warn("Intento de buscar festivales para un ID de promotor nulo.");
            return Collections.emptyList();
        }
        try {
            TypedQuery<Festival> query = em.createQuery(
                    "SELECT f FROM Festival f WHERE f.promotor.idUsuario = :promotorId ORDER BY f.fechaInicio DESC",
                    Festival.class);
            query.setParameter("promotorId", idPromotor);
            List<Festival> festivales = query.getResultList();
            log.debug("Encontrados {} festivales para el promotor ID: {}.", festivales.size(), idPromotor);
            return festivales;
        } catch (Exception e) {
            log.error("Error buscando festivales para el promotor ID {}: {}", idPromotor, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
