/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.Festival;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de FestivalRepository usando JPA EntityManager. Los métodos
 * asumen que el EntityManager proporcionado está activo y que las transacciones
 * son gestionadas por la capa de Servicio.
 */
public class FestivalRepositoryImpl implements FestivalRepository {

    private static final Logger log = LoggerFactory.getLogger(FestivalRepositoryImpl.class);

    @Override
    public Festival save(EntityManager em, Festival festival) {
        log.debug("Intentando guardar festival con ID: {}", festival.getIdFestival());
        if (festival.getIdFestival() == null) {
            // Es un festival nuevo, persistir
            em.persist(festival);
            log.info("Festival nuevo persistido con ID asignado."); // El ID se asigna después del persist/flush
            return festival; // Devuelve la entidad manejada
        } else {
            // Es una actualización, hacer merge
            Festival mergedFestival = em.merge(festival);
            log.info("Festival actualizado con ID: {}", mergedFestival.getIdFestival());
            return mergedFestival; // Devuelve la entidad actualizada y manejada
        }
        // El commit de la transacción se hará en la capa de Servicio
    }

    @Override
    public Optional<Festival> findById(EntityManager em, Integer id) {
        log.debug("Buscando festival con ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        // em.find() devuelve la entidad o null si no la encuentra
        Festival festival = em.find(Festival.class, id);
        if (festival != null) {
            log.debug("Festival encontrado con ID: {}", id);
        } else {
            log.debug("Festival NO encontrado con ID: {}", id);
        }
        return Optional.ofNullable(festival);
    }

    @Override
    public boolean deleteById(EntityManager em, Integer id) {
        log.debug("Intentando eliminar festival con ID: {}", id);
        Optional<Festival> festivalOpt = findById(em, id); // Reutiliza findById para buscarlo primero
        if (festivalOpt.isPresent()) {
            em.remove(festivalOpt.get()); // Marca la entidad para ser eliminada en el commit
            log.info("Festival con ID: {} marcado para eliminación.", id);
            return true;
        } else {
            log.warn("No se pudo eliminar. Festival no encontrado con ID: {}", id);
            return false;
        }
        // El commit/rollback de la transacción se hará en la capa de Servicio
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
            return Collections.emptyList(); // Devolver lista vacía en caso de error
        }
    }

    @Override
    public List<Festival> findByEstado(EntityManager em, EstadoFestival estado) {
        log.debug("Buscando festivales con estado: {}", estado);
        if (estado == null) {
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
        log.debug("Buscando festivales activos entre {} y {}", fechaDesde, fechaHasta);
        if (fechaDesde == null || fechaHasta == null || fechaHasta.isBefore(fechaDesde)) {
            log.warn("Fechas inválidas para la búsqueda de festivales activos.");
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
            log.error("Error buscando festivales activos entre fechas: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Festival> findByPromotorId(EntityManager em, Integer idPromotor) {
        log.debug("Buscando festivales para el promotor ID: {}", idPromotor);
        if (idPromotor == null) {
            return Collections.emptyList();
        }
        try {
            TypedQuery<Festival> query = em.createQuery(
                    "SELECT f FROM Festival f WHERE f.promotor.idUsuario = :promotorId ORDER BY f.fechaInicio DESC", Festival.class);
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
