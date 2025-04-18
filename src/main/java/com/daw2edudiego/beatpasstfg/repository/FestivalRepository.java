/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.Festival;
import javax.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Define las operaciones de acceso a datos para la entidad Festival.
 */
public interface FestivalRepository {

    /**
     * Guarda (crea o actualiza) un festival en la base de datos. Asume que se
     * ejecuta dentro de una transacción activa gestionada externamente
     * (Servicio).
     *
     * @param em EntityManager activo.
     * @param festival El festival a guardar.
     * @return El festival guardado (manejado por el EntityManager).
     */
    Festival save(EntityManager em, Festival festival);

    /**
     * Busca un festival por su ID.
     *
     * @param em EntityManager activo.
     * @param id El ID del festival a buscar.
     * @return Un Optional conteniendo el festival si se encuentra, o vacío si
     * no.
     */
    Optional<Festival> findById(EntityManager em, Integer id);

    /**
     * Elimina un festival de la base de datos. Asume que se ejecuta dentro de
     * una transacción activa gestionada externamente (Servicio). Primero busca
     * la entidad y luego la marca para eliminar.
     *
     * @param em EntityManager activo.
     * @param id El ID del festival a eliminar.
     * @return true si se encontró y marcó para eliminar, false si no se
     * encontró.
     */
    boolean deleteById(EntityManager em, Integer id);

    /**
     * Busca todos los festivales.
     *
     * @param em EntityManager activo.
     * @return Una lista con todos los festivales.
     */
    List<Festival> findAll(EntityManager em);

    /**
     * Busca festivales por estado.
     *
     * @param em EntityManager activo.
     * @param estado El estado a buscar.
     * @return Lista de festivales con ese estado.
     */
    List<Festival> findByEstado(EntityManager em, EstadoFestival estado);

    /**
     * Busca festivales activos (publicados) en un rango de fechas.
     *
     * @param em EntityManager activo.
     * @param fechaDesde Fecha de inicio del rango.
     * @param fechaHasta Fecha de fin del rango.
     * @return Lista de festivales activos en esas fechas.
     */
    List<Festival> findActivosEntreFechas(EntityManager em, LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Busca festivales de un promotor específico.
     *
     * @param em EntityManager activo.
     * @param idPromotor ID del promotor.
     * @return Lista de festivales de ese promotor.
     */
    List<Festival> findByPromotorId(EntityManager em, Integer idPromotor);

}
