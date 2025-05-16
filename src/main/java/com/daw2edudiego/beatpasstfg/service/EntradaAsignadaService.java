package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio para la gestión de Entradas Asignadas.
 */
public interface EntradaAsignadaService {

    /**
     * Asigna (nomina) una entrada específica a un asistente, identificado por
     * el ID de la entrada asignada. Este método es típicamente usado por un
     * promotor. Crea el asistente si no existe. Verifica permisos del promotor
     * y estado de la entrada. Es transaccional. Envía un email de confirmación
     * al asistente nominado.
     *
     * @param idEntradaAsignada ID de la entrada a nominar.
     * @param emailAsistente Email del asistente (obligatorio).
     * @param nombreAsistente Nombre del asistente (obligatorio si se crea).
     * @param telefonoAsistente Teléfono del asistente (opcional).
     * @param idPromotor ID del promotor que realiza la acción.
     * @return EntradaAsignadaDTO con los datos de la entrada nominada.
     * @throws EntradaAsignadaNotFoundException si la entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no es dueño del festival
     * asociado.
     * @throws IllegalStateException si la entrada no está ACTIVA o ya está
     * nominada.
     * @throws IllegalArgumentException si faltan datos o son inválidos.
     */
    EntradaAsignadaDTO nominarEntrada(Integer idEntradaAsignada, String emailAsistente, String nombreAsistente, String telefonoAsistente, Integer idPromotor);

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
     * @return EntradaAsignadaDTO con los datos de la entrada nominada.
     * @throws EntradaAsignadaNotFoundException si la entrada no existe con ese
     * código QR.
     * @throws IllegalStateException si la entrada no está ACTIVA o ya está
     * nominada.
     * @throws IllegalArgumentException si faltan datos o son inválidos.
     */
    EntradaAsignadaDTO nominarEntradaPorQr(String codigoQr, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistenteNominado);

    /**
     * Obtiene las entradas asignadas para un festival específico. Verifica
     * permisos del promotor sobre el festival.
     *
     * @param idFestival ID del festival.
     * @param idPromotor ID del promotor solicitante.
     * @return Lista de EntradaAsignadaDTO.
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws UsuarioNotFoundException si el promotor no se encuentra.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    List<EntradaAsignadaDTO> obtenerEntradasAsignadasPorFestival(Integer idFestival, Integer idPromotor);

    /**
     * Cancela una entrada asignada (si está ACTIVA) e incrementa el stock
     * original. Verifica permisos del promotor. Es transaccional.
     *
     * @param idEntradaAsignada ID de la entrada a cancelar.
     * @param idPromotor ID del promotor que realiza la acción.
     * @throws EntradaAsignadaNotFoundException si la entrada no existe.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws SecurityException si el promotor no tiene permisos.
     * @throws IllegalStateException si la entrada no está ACTIVA.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    void cancelarEntrada(Integer idEntradaAsignada, Integer idPromotor);

    /**
     * Obtiene los detalles de una entrada asignada por su ID. Verifica permisos
     * del promotor sobre el festival asociado.
     *
     * @param idEntradaAsignada ID de la entrada asignada.
     * @param idPromotor ID del promotor solicitante.
     * @return Optional con EntradaAsignadaDTO si se encuentra y hay permisos, o
     * vacío.
     * @throws UsuarioNotFoundException si el promotor no existe.
     * @throws IllegalArgumentException si los IDs son nulos.
     */
    Optional<EntradaAsignadaDTO> obtenerEntradaAsignadaPorId(Integer idEntradaAsignada, Integer idPromotor);

}
