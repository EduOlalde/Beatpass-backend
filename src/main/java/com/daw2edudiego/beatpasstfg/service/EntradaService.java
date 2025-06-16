package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio para la gestión de Entradas.
 */
public interface EntradaService {

    /**
     * Asigna (nomina) una entrada específica a un asistente, identificado por
     * el ID de la entrada. Este método es típicamente usado por un
     * promotor. Crea el asistente si no existe. Verifica permisos del promotor
     * y estado de la entrada. Es transaccional. Envía un email de confirmación
     * al asistente nominado.
     *
     * @param idEntrada ID de la entrada a nominar.
     * @param emailAsistente Email del asistente (obligatorio).
     * @param nombreAsistente Nombre del asistente (obligatorio si se crea).
     * @param telefonoAsistente Teléfono del asistente (opcional).
     * @param idPromotor ID del promotor que realiza la acción.
     * @return EntradaDTO con los datos de la entrada nominada.
     * @throws EntradaNotFoundException si la entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es dueño del festival
     * asociado.
     * @throws IllegalStateException si la entrada no está ACTIVA o ya está
     * nominada.
     * @throws IllegalArgumentException si faltan datos o son inválidos.
     */
    EntradaDTO nominarEntrada(Integer idEntrada, String emailAsistente, String nombreAsistente, String telefonoAsistente, Integer idPromotor);

    /**
     * Asigna (nomina) una entrada específica a un asistente, identificado por
     * su código QR. Este método es típicamente usado para la nominación pública
     * por el propio usuario/comprador. Crea el asistente si no existe. Verifica
     * el estado de la entrada. Es transaccional. Envía un email de confirmación
     * al asistente nominado.
     *
     * @param codigoQr Código QR de la entrada a nominar.
     * @param emailAsistenteNominado Email del asistente (obligatorio).
     * @param nombreAsistenteNominado Nombre del asistente (obligatorio si se
     * crea).
     * @param telefonoAsistenteNominado Teléfono del asistente (opcional).
     * @return EntradaDTO con los datos de la entrada nominada.
     * @throws EntradaNotFoundException si la entrada no existe con ese
     * código QR.
     * @throws IllegalStateException si la entrada no está ACTIVA o ya está
     * nominada.
     * @throws IllegalArgumentException si faltan datos o son inválidos.
     */
    EntradaDTO nominarEntradaPorQr(String codigoQr, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistenteNominado);

    /**
     * Obtiene las entradas para un festival específico. Verifica
     * permisos del promotor sobre el festival.
     *
     * @param idFestival ID del festival.
     * @param idPromotor ID del promotor solicitante.
     * @return Lista de EntradaDTO.
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws UsuarioNotFoundException si el promotor no se encuentra.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor);

    /**
     * Cancela una entrada (si está ACTIVA) e incrementa el stock
     * original. Verifica permisos del promotor. Es transaccional.
     *
     * @param idEntrada ID de la entrada a cancelar.
     * @param idPromotor ID del promotor que realiza la acción.
     * @throws EntradaNotFoundException si la entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalStateException si la entrada no está ACTIVA.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    void cancelarEntrada(Integer idEntrada, Integer idPromotor);

    /**
     * Obtiene los detalles de una entrada por su ID. Verifica permisos
     * del promotor sobre el festival asociado.
     *
     * @param idEntrada ID de la entrada.
     * @param idPromotor ID del promotor solicitante.
     * @return Optional con EntradaDTO si se encuentra y hay permisos, o
 vacío.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor);

    /**
     * Obtiene los detalles de una entrada por su código QR,
     * específicamente para ser usada en un contexto público de nominación. Este
     * método es ideal para validar una entrada antes de mostrar la página de
     * nominación. No requiere autenticación de promotor, ya que es para un
     * flujo público.
     *
     * @param codigoQr El código QR de la entrada.
     * @return Optional con EntradaDTO si se encuentra y es válida para
 nominación, o vacío en caso contrario.
     */
    Optional<EntradaDTO> obtenerParaNominacionPublicaPorQr(String codigoQr);

}
