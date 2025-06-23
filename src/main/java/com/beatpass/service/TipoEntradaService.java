package com.beatpass.service;

import com.beatpass.dto.TipoEntradaDTO;
import com.beatpass.exception.TipoEntradaNotFoundException;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.UsuarioNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio para la gestión de Tipos de Entrada.
 */
public interface TipoEntradaService {

    /**
     * Crea un nuevo tipo de entrada para un festival. Verifica permisos del
     * promotor. Es transaccional.
     *
     * @param tipoEntradaDTO DTO con los datos del nuevo tipo de entrada.
     * @param idFestival ID del festival al que pertenece.
     * @param idPromotor ID del promotor que realiza la acción.
     * @return El TipoEntradaDTO creado con su ID.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es dueño del festival.
     * @throws IllegalArgumentException si los datos son inválidos.
     */
    TipoEntradaDTO crearTipoEntrada(TipoEntradaDTO tipoEntradaDTO, Integer idFestival, Integer idPromotor);

    /**
     * Obtiene los tipos de entrada asociados a un festival. Verifica permisos
     * del promotor.
     *
     * @param idFestival ID del festival.
     * @param idPromotor ID del promotor solicitante.
     * @return Lista de TipoEntradaDTO.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    List<TipoEntradaDTO> obtenerTipoEntradasPorFestival(Integer idFestival, Integer idPromotor);

    /**
     * Obtiene los tipos de entrada de un festival público (estado PUBLICADO).
     * No requiere autenticación de promotor.
     *
     * @param idFestival ID del festival.
     * @return Lista de TipoEntradaDTO.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws FestivalNoPublicadoException si el festival no está publicado.
     * @throws IllegalArgumentException si el ID es nulo.
     */
    List<TipoEntradaDTO> obtenerTiposEntradaPublicasPorFestival(Integer idFestival);

    /**
     * Actualiza un tipo de entrada existente. Verifica permisos del promotor.
     * Es transaccional.
     *
     * @param idEntrada ID de la entrada a actualizar.
     * @param tipoEntradaDTO DTO con los nuevos datos.
     * @param idPromotor ID del promotor que realiza la acción.
     * @return El TipoEntradaDTO actualizado.
     * @throws TipoEntradaNotFoundException si la entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los datos son inválidos.
     */
    TipoEntradaDTO actualizarTipoEntrada(Integer idEntrada, TipoEntradaDTO tipoEntradaDTO, Integer idPromotor);

    /**
     * Elimina un tipo de entrada. Verifica permisos del promotor. Es
     * transaccional. ¡Precaución con FKs!
     *
     * @param idEntrada ID de la entrada a eliminar.
     * @param idPromotor ID del promotor que realiza la acción.
     * @throws TipoEntradaNotFoundException si la entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los IDs son nulos.
     * @throws RuntimeException si falla por FKs u otro error.
     */
    void eliminarTipoEntrada(Integer idEntrada, Integer idPromotor);

    /**
     * Obtiene los detalles de un tipo de entrada por su ID. Verifica permisos
     * del promotor.
     *
     * @param idEntrada ID de la entrada a buscar.
     * @param idPromotor ID del promotor solicitante.
     * @return Optional con TipoEntradaDTO si se encuentra y hay permisos, o vacío.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    Optional<TipoEntradaDTO> obtenerTipoEntradaPorId(Integer idEntrada, Integer idPromotor);

}
