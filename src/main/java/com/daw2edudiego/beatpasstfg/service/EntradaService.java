package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.EntradaNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define la lógica de negocio y las operaciones relacionadas con
 * la gestión de Tipos de Entrada
 * ({@link com.daw2edudiego.beatpasstfg.model.Entrada}) para los festivales.
 *
 * @author Eduardo Olalde
 */
public interface EntradaService {

    /**
     * Crea un nuevo tipo de entrada para un festival específico. Verifica que
     * el festival exista y que el usuario (promotor) que realiza la acción sea
     * el propietario del festival.
     * <p>
     * La operación es transaccional.
     * </p>
     *
     * @param entradaDTO DTO con los datos del nuevo tipo de entrada (tipo,
     * precio, stock, descripción). El campo {@code idFestival} dentro del DTO
     * debe coincidir con el parámetro {@code idFestival}.
     * @param idFestival ID del festival al que se añadirá el tipo de entrada.
     * @param idPromotor ID del usuario (promotor) que realiza la acción para
     * verificación de permisos.
     * @return El {@link EntradaDTO} del tipo de entrada recién creado,
     * incluyendo su ID generado.
     * @throws FestivalNotFoundException si el festival con idFestival no
     * existe.
     * @throws UsuarioNotFoundException si el promotor con idPromotor no existe.
     * @throws SecurityException si el promotor no es el dueño del festival.
     * @throws IllegalArgumentException si los datos del DTO son inválidos (ej:
     * tipo vacío, precio negativo) o si el idFestival del DTO no coincide.
     * @throws RuntimeException si ocurre un error inesperado durante la
     * persistencia.
     */
    EntradaDTO crearEntrada(EntradaDTO entradaDTO, Integer idFestival, Integer idPromotor);

    /**
     * Obtiene una lista de todos los tipos de entrada asociados a un festival
     * específico. Verifica que el festival exista y que el usuario (promotor)
     * que realiza la acción sea el propietario del festival.
     *
     * @param idFestival El ID del festival cuyos tipos de entrada se quieren
     * obtener.
     * @param idPromotor El ID del usuario (promotor) que realiza la consulta.
     * @return Una lista (posiblemente vacía) de {@link EntradaDTO} asociados al
     * festival.
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws UsuarioNotFoundException si el promotor no se encuentra.
     * @throws SecurityException si el promotor no tiene permisos sobre el
     * festival.
     * @throws IllegalArgumentException si idFestival o idPromotor son nulos.
     * @throws RuntimeException si ocurre un error inesperado durante la
     * consulta.
     */
    List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor);

    /**
     * Actualiza la información de un tipo de entrada existente. Verifica que la
     * entrada exista y que el usuario (promotor) que realiza la acción sea el
     * propietario del festival al que pertenece la entrada.
     * <p>
     * La operación es transaccional.
     * </p>
     *
     * @param idEntrada ID del tipo de entrada a actualizar.
     * @param entradaDTO DTO con los nuevos datos para el tipo de entrada (tipo,
     * descripción, precio, stock).
     * @param idPromotor ID del usuario (promotor) que realiza la acción.
     * @return El {@link EntradaDTO} del tipo de entrada actualizado.
     * @throws EntradaNotFoundException si el tipo de entrada con idEntrada no
     * existe.
     * @throws UsuarioNotFoundException si el promotor con idPromotor no existe.
     * @throws SecurityException si el promotor no es el dueño del festival
     * asociado a esta entrada.
     * @throws IllegalArgumentException si los IDs o el DTO son nulos, o si los
     * datos del DTO son inválidos.
     * @throws RuntimeException si ocurre un error inesperado durante la
     * actualización.
     */
    EntradaDTO actualizarEntrada(Integer idEntrada, EntradaDTO entradaDTO, Integer idPromotor);

    /**
     * Elimina un tipo de entrada específico por su ID. Verifica que la entrada
     * exista y que el usuario (promotor) que realiza la acción sea el
     * propietario del festival asociado.
     * <p>
     * <b>¡Precaución!</b> La eliminación física puede fallar si existen
     * {@link com.daw2edudiego.beatpasstfg.model.CompraEntrada} (ventas)
     * asociadas a este tipo de entrada, debido a restricciones de clave
     * foránea. En muchos casos, es preferible desactivar la entrada o poner el
     * stock a cero en lugar de eliminarla.
     * </p>
     * <p>
     * La operación es transaccional.
     * </p>
     *
     * @param idEntrada ID del tipo de entrada a eliminar.
     * @param idPromotor ID del usuario (promotor) que realiza la acción.
     * @throws EntradaNotFoundException si el tipo de entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es el dueño del festival
     * asociado.
     * @throws IllegalArgumentException si idEntrada o idPromotor son nulos.
     * @throws RuntimeException si ocurre un error durante la eliminación (ej:
     * violación de FK).
     */
    void eliminarEntrada(Integer idEntrada, Integer idPromotor);

    /**
     * Obtiene los detalles (DTO) de un tipo de entrada específico por su ID.
     * Verifica que la entrada exista y que el usuario (promotor) que realiza la
     * solicitud sea el propietario del festival asociado.
     *
     * @param idEntrada El ID del tipo de entrada a buscar.
     * @param idPromotor El ID del usuario (promotor) que realiza la consulta.
     * @return Un {@link Optional} conteniendo el {@link EntradaDTO} si se
     * encuentra y el promotor tiene permisos, de lo contrario, un Optional
     * vacío.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws IllegalArgumentException si idEntrada o idPromotor son nulos.
     * @throws RuntimeException si ocurre un error inesperado.
     * @throws SecurityException si el promotor no tiene permisos (implícito si
     * devuelve Optional vacío).
     * @throws EntradaNotFoundException si la entrada no existe (implícito si
     * devuelve Optional vacío).
     */
    Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor);

}
