package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.Festival;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Define la interfaz para la lógica de negocio relacionada con la gestión de
 * Festivales. Esta capa coordina las operaciones sobre la entidad
 * {@link Festival}, interactuando con los repositorios y gestionando la lógica
 * de negocio, validaciones y control de permisos.
 *
 * @see Festival
 * @see FestivalDTO
 * @see FestivalServiceImpl
 * @author Eduardo Olalde
 */
public interface FestivalService {

    /**
     * Crea un nuevo festival asociado a un promotor específico. Realiza
     * validaciones sobre los datos del DTO y verifica la existencia y validez
     * del promotor. El estado inicial del festival se establece típicamente
     * como BORRADOR.
     *
     * @param festivalDTO DTO con los datos del festival a crear (nombre,
     * fechas, etc.). No debe ser {@code null}.
     * @param idPromotor ID del usuario (con rol PROMOTOR) que crea el festival.
     * No debe ser {@code null}.
     * @return El {@link FestivalDTO} del festival recién creado, incluyendo su
     * ID generado.
     * @throws UsuarioNotFoundException Si el promotor con el ID especificado no
     * existe o no tiene el rol adecuado.
     * @throws IllegalArgumentException Si los datos del DTO son inválidos (ej:
     * nombre vacío, fechas inconsistentes) o si los IDs son nulos.
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * persistencia.
     */
    FestivalDTO crearFestival(FestivalDTO festivalDTO, Integer idPromotor);

    /**
     * Obtiene la información de un festival específico basado en su ID.
     *
     * @param id El ID del festival a buscar. No debe ser {@code null}.
     * @return Un {@link Optional} que contiene el {@link FestivalDTO} si el
     * festival se encuentra, o un {@code Optional} vacío si no se encuentra.
     * @throws IllegalArgumentException Si el ID es {@code null}.
     */
    Optional<FestivalDTO> obtenerFestivalPorId(Integer id);

    /**
     * Actualiza la información de un festival existente. Verifica que el
     * festival exista y que el usuario que realiza la acción
     * ({@code idUsuarioActualizador}) tenga permisos para modificarlo (sea
     * ADMIN o el promotor propietario).
     * <p>
     * <b>Nota:</b> Este método no modifica el estado del festival. Para cambiar
     * el estado, usar
     * {@link #cambiarEstadoFestival(Integer, EstadoFestival, Integer)}.
     * </p>
     *
     * @param id ID del festival a actualizar. No debe ser {@code null}.
     * @param festivalDTO DTO con los nuevos datos para el festival (nombre,
     * fechas, descripción, etc.). No debe ser {@code null}.
     * @param idUsuarioActualizador ID del usuario (ADMIN o PROMOTOR dueño) que
     * realiza la actualización. No debe ser {@code null}.
     * @return El {@link FestivalDTO} del festival actualizado.
     * @throws FestivalNotFoundException Si no se encuentra un festival con el
     * ID proporcionado.
     * @throws UsuarioNotFoundException Si el usuario actualizador no existe.
     * @throws SecurityException Si el usuario no tiene permisos para modificar
     * este festival.
     * @throws IllegalArgumentException Si los IDs o el DTO son nulos, o si los
     * datos del DTO son inválidos.
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * actualización.
     */
    FestivalDTO actualizarFestival(Integer id, FestivalDTO festivalDTO, Integer idUsuarioActualizador);

    /**
     * Elimina un festival específico por su ID. Verifica que el festival exista
     * y que el usuario que realiza la acción tenga permisos para eliminarlo
     * (ADMIN o el promotor propietario).
     * <p>
     * <b>¡Precaución!</b> La eliminación física puede fallar si existen datos
     * asociados (entradas, consumos, etc.) debido a restricciones de clave
     * foránea. Considerar alternativas como la desactivación lógica.
     * </p>
     *
     * @param id ID del festival a eliminar. No debe ser {@code null}.
     * @param idUsuarioEliminador ID del usuario (ADMIN o PROMOTOR dueño) que
     * realiza la eliminación. No debe ser {@code null}.
     * @throws FestivalNotFoundException Si el festival no se encuentra.
     * @throws UsuarioNotFoundException Si el usuario eliminador no existe.
     * @throws SecurityException Si el usuario no tiene permisos para eliminar
     * este festival.
     * @throws IllegalArgumentException Si los IDs son nulos.
     * @throws RuntimeException Si ocurre un error durante la eliminación (ej:
     * violación de FK).
     */
    void eliminarFestival(Integer id, Integer idUsuarioEliminador);

    /**
     * Busca y devuelve una lista de festivales que están en estado PUBLICADO y
     * cuyo periodo (fecha de inicio a fecha de fin) se solapa con el rango de
     * fechas proporcionado. Útil para mostrar festivales activos al público.
     *
     * @param fechaDesde Fecha de inicio del rango de búsqueda (inclusiva).
     * Puede ser {@code null} para buscar desde el inicio de los tiempos.
     * @param fechaHasta Fecha de fin del rango de búsqueda (inclusiva). Puede
     * ser {@code null} para buscar hasta el fin de los tiempos.
     * @return Una lista de {@link FestivalDTO} que cumplen los criterios,
     * ordenada por fecha de inicio. Puede estar vacía.
     */
    List<FestivalDTO> buscarFestivalesPublicados(LocalDate fechaDesde, LocalDate fechaHasta);

    /**
     * Obtiene todos los festivales gestionados por un promotor específico.
     *
     * @param idPromotor ID del usuario promotor. No debe ser {@code null}.
     * @return Una lista de {@link FestivalDTO} de los festivales asociados al
     * promotor, ordenada por fecha de inicio descendente. Puede estar vacía.
     * @throws IllegalArgumentException Si el ID del promotor es {@code null}.
     * @throws UsuarioNotFoundException Si el promotor no existe (implícito,
     * podría devolver lista vacía).
     */
    List<FestivalDTO> obtenerFestivalesPorPromotor(Integer idPromotor);

    /**
     * Cambia el estado de un festival específico. Esta operación está
     * restringida a usuarios con rol ADMIN. Se aplican reglas de transición de
     * estado (ej: no se puede publicar un festival ya cancelado).
     *
     * @param idFestival ID del festival cuyo estado se cambiará. No debe ser
     * {@code null}.
     * @param nuevoEstado El nuevo {@link EstadoFestival} a asignar. No debe ser
     * {@code null}.
     * @param idActor ID del usuario (ADMIN) que realiza la acción. No debe ser
     * {@code null}.
     * @return El {@link FestivalDTO} del festival con el estado actualizado.
     * @throws FestivalNotFoundException Si el festival no se encuentra.
     * @throws UsuarioNotFoundException Si el usuario actor no existe.
     * @throws SecurityException Si el usuario actor no es ADMIN.
     * @throws IllegalStateException Si la transición de estado solicitada no es
     * válida según las reglas de negocio.
     * @throws IllegalArgumentException Si los IDs o el nuevo estado son nulos.
     * @throws RuntimeException Si ocurre un error inesperado.
     */
    FestivalDTO cambiarEstadoFestival(Integer idFestival, EstadoFestival nuevoEstado, Integer idActor);

    /**
     * Obtiene una lista de TODOS los festivales registrados en el sistema. Esta
     * operación está pensada principalmente para usuarios con rol ADMIN.
     *
     * @return Una lista de {@link FestivalDTO} de todos los festivales,
     * ordenada por nombre. Puede estar vacía.
     */
    List<FestivalDTO> obtenerTodosLosFestivales();

    /**
     * Obtiene una lista de festivales filtrando por un estado específico. Si el
     * estado es {@code null}, devuelve todos los festivales (similar a
     * {@link #obtenerTodosLosFestivales()}). Esta operación está pensada
     * principalmente para usuarios con rol ADMIN.
     *
     * @param estado El {@link EstadoFestival} por el cual filtrar, o
     * {@code null} para obtener todos.
     * @return Lista de {@link FestivalDTO} que coinciden con el estado (o todos
     * si estado es null), ordenada por fecha de inicio. Puede estar vacía.
     */
    List<FestivalDTO> obtenerFestivalesPorEstado(EstadoFestival estado);

}
