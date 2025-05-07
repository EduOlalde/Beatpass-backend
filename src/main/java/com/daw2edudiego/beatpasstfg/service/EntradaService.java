package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.EntradaNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio para la gestión de Tipos de Entrada.
 */
public interface EntradaService {

    /**
     * Crea un nuevo tipo de entrada para un festival. Verifica permisos del
     * promotor. Es transaccional.
     *
     * @param entradaDTO DTO con los datos del nuevo tipo de entrada.
     * @param idFestival ID del festival al que pertenece.
     * @param idPromotor ID del promotor que realiza la acción.
     * @return El EntradaDTO creado con su ID.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es dueño del festival.
     * @throws IllegalArgumentException si los datos son inválidos.
     */
    EntradaDTO crearEntrada(EntradaDTO entradaDTO, Integer idFestival, Integer idPromotor);

    /**
     * Obtiene los tipos de entrada asociados a un festival. Verifica permisos
     * del promotor.
     *
     * @param idFestival ID del festival.
     * @param idPromotor ID del promotor solicitante.
     * @return Lista de EntradaDTO.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor);

    /**
     * Obtiene los tipos de entrada de un festival público (estado PUBLICADO).
     * No requiere autenticación de promotor.
     *
     * @param idFestival ID del festival.
     * @return Lista de EntradaDTO.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws FestivalNoPublicadoException si el festival no está publicado.
     * @throws IllegalArgumentException si el ID es nulo.
     */
    List<EntradaDTO> obtenerEntradasPublicasPorFestival(Integer idFestival);

    /**
     * Actualiza un tipo de entrada existente. Verifica permisos del promotor.
     * Es transaccional.
     *
     * @param idEntrada ID de la entrada a actualizar.
     * @param entradaDTO DTO con los nuevos datos.
     * @param idPromotor ID del promotor que realiza la acción.
     * @return El EntradaDTO actualizado.
     * @throws EntradaNotFoundException si la entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los datos son inválidos.
     */
    EntradaDTO actualizarEntrada(Integer idEntrada, EntradaDTO entradaDTO, Integer idPromotor);

    /**
     * Elimina un tipo de entrada. Verifica permisos del promotor. Es
     * transaccional. ¡Precaución con FKs!
     *
     * @param idEntrada ID de la entrada a eliminar.
     * @param idPromotor ID del promotor que realiza la acción.
     * @throws EntradaNotFoundException si la entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los IDs son nulos.
     * @throws RuntimeException si falla por FKs u otro error.
     */
    void eliminarEntrada(Integer idEntrada, Integer idPromotor);

    /**
     * Obtiene los detalles de un tipo de entrada por su ID. Verifica permisos
     * del promotor.
     *
     * @param idEntrada ID de la entrada a buscar.
     * @param idPromotor ID del promotor solicitante.
     * @return Optional con EntradaDTO si se encuentra y hay permisos, o vacío.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor);

}
