package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import com.daw2edudiego.beatpasstfg.exception.*; // Importar excepciones necesarias

import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define las operaciones de negocio para gestionar Entradas
 * Asignadas, incluyendo la nominación.
 */
public interface EntradaAsignadaService {

    /**
     * Asigna (nomina) una entrada específica a un asistente. Verifica permisos
     * y estado de la entrada.
     *
     * @param idEntradaAsignada ID de la entrada a nominar.
     * @param idAsistente ID del asistente al que se nomina la entrada.
     * @param idPromotor ID del promotor que realiza la acción (para
     * verificación de permisos).
     * @throws EntradaAsignadaNotFoundException si la entrada no existe.
     * @throws AsistenteNotFoundException si el asistente no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es dueño del festival
     * asociado.
     * @throws IllegalStateException si la entrada ya está nominada, usada o
     * cancelada.
     * @throws RuntimeException si ocurre un error inesperado.
     */
    void nominarEntrada(Integer idEntradaAsignada, Integer idAsistente, Integer idPromotor);

    /**
     * Obtiene la lista de DTOs de entradas asignadas para un festival
     * específico. Verifica que el promotor sea el dueño del festival.
     *
     * @param idFestival ID del festival.
     * @param idPromotor ID del promotor que solicita la lista.
     * @return Lista de EntradaAsignadaDTO para el festival.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es dueño del festival.
     * @throws RuntimeException si ocurre un error inesperado.
     */
    List<EntradaAsignadaDTO> obtenerEntradasAsignadasPorFestival(Integer idFestival, Integer idPromotor);

    /**
     * Cancela una entrada asignada específica. Solo se pueden cancelar entradas
     * en estado ACTIVA. Verifica que el promotor que realiza la acción sea el
     * dueño del festival. Incrementa el stock del tipo de entrada original.
     *
     * @param idEntradaAsignada ID de la entrada a cancelar.
     * @param idPromotor ID del promotor que realiza la acción.
     * @throws EntradaAsignadaNotFoundException si la entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es dueño del festival
     * asociado.
     * @throws IllegalStateException si la entrada no está en estado ACTIVA.
     * @throws RuntimeException si ocurre un error inesperado.
     */
    void cancelarEntrada(Integer idEntradaAsignada, Integer idPromotor); 

    /**
     * Obtiene los detalles de una entrada asignada específica por su ID. Útil
     * para obtener el idFestival para redirecciones. Verifica propiedad.
     *
     * @param idEntradaAsignada ID de la entrada asignada.
     * @param idPromotor ID del promotor solicitante.
     * @return Optional con el DTO si se encuentra y pertenece al promotor.
     */
    Optional<EntradaAsignadaDTO> obtenerEntradaAsignadaPorId(Integer idEntradaAsignada, Integer idPromotor);
}
