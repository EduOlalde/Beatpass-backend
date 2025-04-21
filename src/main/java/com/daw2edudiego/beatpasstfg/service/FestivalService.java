/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio relacionada con los Festivales.
 */
public interface FestivalService {

    /**
     * Crea un nuevo festival para un promotor específico.
     *
     * @param festivalDTO Datos del festival a crear.
     * @param idPromotor ID del usuario promotor que crea el festival.
     * @return El DTO del festival creado.
     * @throws // Podría lanzar excepciones si el promotor no existe, datos
     * inválidos, etc.
     */
    FestivalDTO crearFestival(FestivalDTO festivalDTO, Integer idPromotor);

    /**
     * Obtiene un festival por su ID.
     *
     * @param id ID del festival.
     * @return Un Optional con el DTO del festival si existe.
     */
    Optional<FestivalDTO> obtenerFestivalPorId(Integer id);

    /**
     * Actualiza la información de un festival existente.
     *
     * @param id ID del festival a actualizar.
     * @param festivalDTO Nuevos datos para el festival.
     * @param idPromotor ID del promotor que realiza la acción (para
     * validación).
     * @return El DTO del festival actualizado.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws // SecurityException o similar si el promotor no tiene permiso.
     */
    FestivalDTO actualizarFestival(Integer id, FestivalDTO festivalDTO, Integer idPromotor);

    /**
     * Elimina un festival.
     *
     * @param id ID del festival a eliminar.
     * @param idPromotor ID del promotor que realiza la acción (para
     * validación).
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws // SecurityException o similar si el promotor no tiene permiso.
     */
    void eliminarFestival(Integer id, Integer idPromotor);

    /**
     * Busca festivales publicados y activos en un rango de fechas.
     *
     * @param fechaDesde Fecha de inicio.
     * @param fechaHasta Fecha de fin.
     * @return Lista de DTOs de festivales encontrados.
     */
    List<FestivalDTO> buscarFestivalesPublicados(LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Obtiene todos los festivales pertenecientes a un promotor específico.
     *
     * @param idPromotor ID del promotor.
     * @return Lista de DTOs de los festivales del promotor.
     */
    List<FestivalDTO> obtenerFestivalesPorPromotor(Integer idPromotor);

    /**
     * Cambia el estado de un festival.
     *
     * @param idFestival ID del festival.
     * @param nuevoEstado Nuevo estado a asignar.
     * @param idPromotor ID del promotor que realiza la acción.
     * @return El DTO del festival con el estado actualizado.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws // SecurityException o similar si el promotor no tiene permiso.
     */
    FestivalDTO cambiarEstadoFestival(Integer idFestival, EstadoFestival nuevoEstado, Integer idPromotor);

    /**
     * Obtiene una lista de TODOS los festivales registrados en el sistema.
     * Utilizado principalmente por el Administrador.
     *
     * @return Lista de DTOs de todos los festivales.
     */
    List<FestivalDTO> obtenerTodosLosFestivales();

    /**
     * Obtiene una lista de festivales filtrando por estado. Utilizado
     * principalmente por el Administrador.
     *
     * @param estado El estado por el cual filtrar (puede ser null para no
     * filtrar por estado).
     * @return Lista de DTOs de los festivales que coinciden.
     */
    List<FestivalDTO> obtenerFestivalesPorEstado(EstadoFestival estado);

}
