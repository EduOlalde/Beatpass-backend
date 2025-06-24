package com.beatpass.service;

import com.beatpass.dto.FestivalDTO;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.UsuarioNotFoundException;
import com.beatpass.model.EstadoFestival;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio para la gestión de Festivales.
 */
public interface FestivalService {

    /**
     * Crea un nuevo festival asociado a un promotor. Estado inicial BORRADOR.
     *
     * @param festivalDTO DTO con los datos del festival (nombre, fechas, etc.).
     * @param idPromotor ID del promotor que crea el festival.
     * @return El FestivalDTO creado con su ID.
     * @throws UsuarioNotFoundException si el promotor no existe o no es válido.
     * @throws IllegalArgumentException si los datos del DTO o IDs son
     * inválidos.
     */
    FestivalDTO crearFestival(FestivalDTO festivalDTO, Integer idPromotor);

    /**
     * Obtiene la información de un festival por su ID (uso público, sin chequeo
     * de permisos).
     *
     * @param id ID del usuario
     * @return Optional con FestivalDTO si se encuentra.
     */
    Optional<FestivalDTO> obtenerFestivalPorId(Integer id);

    /**
     * Obtiene la información de un festival por su ID, verificando permisos del
     * actor..
     *
     * @param id El ID del festival a buscar.
     * @param idActor ID del usuario
     * @return Optional con FestivalDTO si se encuentra.
     */
    Optional<FestivalDTO> obtenerFestivalPorId(Integer id, Integer idActor);

    /**
     * Actualiza la información de un festival existente (nombre, fechas, etc.).
     * Verifica permisos (ADMIN o promotor dueño). No modifica el estado.
     *
     * @param id ID del festival a actualizar.
     * @param festivalDTO DTO con los nuevos datos.
     * @param idUsuarioActualizador ID del usuario (ADMIN o PROMOTOR dueño).
     * @return El FestivalDTO actualizado.
     * @throws FestivalNotFoundException si no se encuentra el festival.
     * @throws UsuarioNotFoundException si el usuario actualizador no existe.
     * @throws SecurityException si el usuario no tiene permisos.
     * @throws IllegalArgumentException si los datos son inválidos.
     */
    FestivalDTO actualizarFestival(Integer id, FestivalDTO festivalDTO, Integer idUsuarioActualizador);

    /**
     * Elimina un festival. Verifica permisos (ADMIN o promotor dueño).
     * ¡Precaución con FKs!
     *
     * @param id ID del festival a eliminar.
     * @param idUsuarioEliminador ID del usuario (ADMIN o PROMOTOR dueño).
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws UsuarioNotFoundException si el usuario eliminador no existe.
     * @throws SecurityException si el usuario no tiene permisos.
     * @throws IllegalArgumentException si los IDs son nulos.
     * @throws RuntimeException si falla por FKs u otro error.
     */
    void eliminarFestival(Integer id, Integer idUsuarioEliminador);

    /**
     * Busca festivales PUBLICADOS cuyo periodo se solapa con el rango de
     * fechas.
     *
     * @param fechaDesde Fecha de inicio del rango (inclusiva).
     * @param fechaHasta Fecha de fin del rango (inclusiva).
     * @return Lista de FestivalDTO coincidentes.
     */
    List<FestivalDTO> buscarFestivalesPublicados(LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Obtiene todos los festivales gestionados por un promotor específico.
     *
     * @param idPromotor ID del promotor.
     * @return Lista de FestivalDTO.
     * @throws IllegalArgumentException si el ID del promotor es nulo.
     */
    List<FestivalDTO> obtenerFestivalesPorPromotor(Integer idPromotor);

    /**
     * Cambia el estado de un festival. Restringido a ADMIN. Aplica reglas de
     * transición.
     *
     * @param idFestival ID del festival.
     * @param nuevoEstado Nuevo EstadoFestival.
     * @param idActor ID del ADMIN que realiza la acción.
     * @return El FestivalDTO actualizado.
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws UsuarioNotFoundException si el actor no existe.
     * @throws SecurityException si el actor no es ADMIN.
     * @throws IllegalStateException si la transición de estado no es válida.
     * @throws IllegalArgumentException si los IDs o el estado son nulos.
     */
    FestivalDTO cambiarEstadoFestival(Integer idFestival, EstadoFestival nuevoEstado, Integer idActor);

    /**
     * Obtiene TODOS los festivales. Pensado para ADMIN.
     *
     * @return Lista de todos los FestivalDTO.
     */
    List<FestivalDTO> obtenerTodosLosFestivales();

    /**
     * Obtiene festivales filtrando por estado. Si estado es null, devuelve
     * todos. Pensado para ADMIN.
     *
     * @param estado EstadoFestival por el cual filtrar, o null.
     * @return Lista de FestivalDTO.
     */
    List<FestivalDTO> obtenerFestivalesPorEstado(EstadoFestival estado);

}
