package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.EntradaNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define el contrato para la lógica de negocio relacionada con la
 * gestión de Tipos de Entrada de los festivales.
 *
 * @author Eduardo Olalde
 */
public interface EntradaService {

    /**
     * Crea un nuevo tipo de entrada para un festival específico. Verifica que
     * el promotor que realiza la acción sea el dueño del festival.
     *
     * @param entradaDTO DTO con los datos del nuevo tipo de entrada (tipo,
     * precio, stock, descripción). El idFestival dentro del DTO debe coincidir
     * con el idFestival del parámetro.
     * @param idFestival ID del festival al que se añadirá el tipo de entrada.
     * @param idPromotor ID del usuario promotor que realiza la acción.
     * @return El DTO del tipo de entrada recién creado, incluyendo su ID
     * generado.
     * @throws FestivalNotFoundException si el festival con idFestival no
     * existe.
     * @throws UsuarioNotFoundException si el promotor con idPromotor no existe.
     * @throws SecurityException si el promotor no es el dueño del festival.
     * @throws IllegalArgumentException si los datos del DTO son inválidos o
     * inconsistentes.
     * @throws RuntimeException si ocurre un error inesperado durante la
     * persistencia.
     */
    EntradaDTO crearEntrada(EntradaDTO entradaDTO, Integer idFestival, Integer idPromotor);

    /**
     * Obtiene la lista de todos los tipos de entrada asociados a un festival
     * específico. Verifica que el promotor que realiza la acción sea el dueño
     * del festival.
     *
     * @param idFestival ID del festival cuyos tipos de entrada se quieren
     * obtener.
     * @param idPromotor ID del usuario promotor que realiza la acción.
     * @return Una lista (posiblemente vacía) de EntradaDTO.
     * @throws FestivalNotFoundException si el festival con idFestival no
     * existe.
     * @throws UsuarioNotFoundException si el promotor con idPromotor no existe.
     * @throws SecurityException si el promotor no es el dueño del festival.
     * @throws RuntimeException si ocurre un error inesperado durante la
     * consulta.
     */
    List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor);

    /**
     * Actualiza la información de un tipo de entrada existente. Verifica que el
     * promotor que realiza la acción sea el dueño del festival al que pertenece
     * la entrada.
     *
     * @param idEntrada ID del tipo de entrada a actualizar.
     * @param entradaDTO DTO con los nuevos datos para el tipo de entrada.
     * @param idPromotor ID del usuario promotor que realiza la acción.
     * @return El DTO del tipo de entrada actualizado.
     * @throws EntradaNotFoundException si el tipo de entrada con idEntrada no
     * existe.
     * @throws UsuarioNotFoundException si el promotor con idPromotor no existe.
     * @throws SecurityException si el promotor no es el dueño del festival
     * asociado a esta entrada.
     * @throws IllegalArgumentException si los datos del DTO son inválidos.
     * @throws RuntimeException si ocurre un error inesperado durante la
     * actualización.
     */
    EntradaDTO actualizarEntrada(Integer idEntrada, EntradaDTO entradaDTO, Integer idPromotor);

    /**
     * Elimina un tipo de entrada. Verifica que el promotor que realiza la
     * acción sea el dueño del festival al que pertenece la entrada.
     * ¡Precaución! Podría fallar si ya se han vendido entradas de este tipo
     * (depende de las restricciones FK). Considerar la desactivación en lugar
     * de la eliminación física.
     *
     * @param idEntrada ID del tipo de entrada a eliminar.
     * @param idPromotor ID del usuario promotor que realiza la acción.
     * @throws EntradaNotFoundException si el tipo de entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es el dueño del festival
     * asociado a esta entrada.
     * @throws RuntimeException si ocurre un error durante la eliminación (ej:
     * violación de FK).
     */
    void eliminarEntrada(Integer idEntrada, Integer idPromotor);

    /**
     * Obtiene los detalles de un tipo de entrada específico por su ID. Verifica
     * que el promotor sea el dueño del festival asociado.
     *
     * @param idEntrada ID del tipo de entrada a buscar.
     * @param idPromotor ID del promotor que realiza la acción.
     * @return Optional con el EntradaDTO si se encuentra y pertenece al
     * promotor.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es dueño del festival
     * asociado.
     * @throws RuntimeException si ocurre un error inesperado.
     */
    Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor); // <-- NUEVO MÉTODO

}
