package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio para las Pulseras NFC. Gestiona asociación,
 * consultas, recargas y consumos.
 */
public interface PulseraNFCService {

    /**
     * Asocia una pulsera NFC a una entrada. Crea la pulsera si no
     * existe. Vincula la pulsera al festival de la entrada. Verifica permisos y
     * estados. Es transaccional.
     *
     * @param codigoUid UID de la pulsera (obligatorio).
     * @param idEntrada ID de la entrada a asociar (obligatorio, debe
     * estar ACTIVA y nominada).
     * @param idActor ID del usuario (ADMIN/PROMOTOR/CAJERO) que realiza la
     * acción.
     * @return El PulseraNFCDTO de la pulsera asociada/creada.
     * @throws UsuarioNotFoundException, EntradaAsignadaNotFoundException,
     * PulseraYaAsociadaException, etc.
     */
    PulseraNFCDTO asociarPulseraEntrada(String codigoUid, Integer idEntrada, Integer idActor);

    /**
     * Obtiene los detalles de una pulsera por su ID. Verifica permisos del
     * actor.
     *
     * @param idPulsera ID de la pulsera.
     * @param idActor ID del usuario solicitante.
     * @return Optional con PulseraNFCDTO si se encuentra y hay permisos.
     */
    Optional<PulseraNFCDTO> obtenerPulseraPorId(Integer idPulsera, Integer idActor);

    /**
     * Obtiene los detalles de una pulsera por su UID. Verifica permisos del
     * actor.
     *
     * @param codigoUid UID de la pulsera.
     * @param idActor ID del usuario solicitante.
     * @return Optional con PulseraNFCDTO si se encuentra y hay permisos.
     */
    Optional<PulseraNFCDTO> obtenerPulseraPorCodigoUid(String codigoUid, Integer idActor);

    /**
     * Obtiene el saldo actual de una pulsera. Verifica permisos del actor.
     *
     * @param idPulsera ID de la pulsera.
     * @param idActor ID del usuario solicitante.
     * @return El saldo actual.
     * @throws PulseraNFCNotFoundException si no se encuentra o no hay permisos.
     */
    BigDecimal obtenerSaldo(Integer idPulsera, Integer idActor);

    /**
     * Obtiene las pulseras asociadas a un festival. Verifica permisos (ADMIN o
     * promotor dueño).
     *
     * @param idFestival ID del festival.
     * @param idActor ID del usuario solicitante.
     * @return Lista de PulseraNFCDTO.
     * @throws FestivalNotFoundException si el festival no existe.
     * @throws SecurityException si no tiene permisos.
     */
    List<PulseraNFCDTO> obtenerPulserasPorFestival(Integer idFestival, Integer idActor);

    /**
     * Registra una recarga de saldo en una pulsera. Verifica que la pulsera
     * pertenezca al festival indicado. Verifica permisos del cajero/actor. Es
     * transaccional.
     *
     * @param codigoUid UID de la pulsera (obligatorio).
     * @param monto Cantidad a recargar (obligatorio, > 0).
     * @param metodoPago Método de pago (opcional).
     * @param idUsuarioCajero ID del usuario (CAJERO/ADMIN/PROMOTOR) que
     * registra.
     * @param idFestival ID del festival donde se realiza la recarga
     * (obligatorio).
     * @return El PulseraNFCDTO actualizado.
     * @throws PulseraNFCNotFoundException, UsuarioNotFoundException,
     * SecurityException, etc.
     */
    PulseraNFCDTO registrarRecarga(String codigoUid, BigDecimal monto, String metodoPago, Integer idUsuarioCajero, Integer idFestival);

    /**
     * Registra un consumo con una pulsera en un festival. Verifica saldo y
     * pertenencia al festival. Verifica permisos del actor. Es transaccional.
     *
     * @param codigoUid UID de la pulsera (obligatorio).
     * @param monto Cantidad a consumir (obligatorio, > 0).
     * @param descripcion Descripción del consumo (obligatorio).
     * @param idFestival ID del festival donde ocurre (obligatorio).
     * @param idPuntoVenta ID opcional del punto de venta.
     * @param idActor ID del usuario (CAJERO/ADMIN/PROMOTOR) que registra.
     * @return El PulseraNFCDTO actualizado.
     * @throws PulseraNFCNotFoundException, FestivalNotFoundException,
     * SaldoInsuficienteException, SecurityException, etc.
     */
    PulseraNFCDTO registrarConsumo(String codigoUid, BigDecimal monto, String descripcion, Integer idFestival, Integer idPuntoVenta, Integer idActor);

    /**
     * Asocia una pulsera NFC a una entrada identificada por su código
     * QR. Este método está pensado para ser usado por un endpoint público en
     * los puntos de acceso.
     *
     * @param codigoQrEntrada El código QR de la EntradaAsignada.
     * @param codigoUidPulsera El UID de la PulseraNFC.
     * @param idFestivalContexto (Opcional) El ID del festival para validación
     * adicional.
     * @return El PulseraNFCDTO de la pulsera asociada/creada y vinculada.
     * @throws EntradaNotFoundException si la entrada no se encuentra o
     * no es válida.
     * @throws PulseraNFCNotFoundException si la pulsera existe pero hay un
     * problema (raro en este flujo si se crea).
     * @throws PulseraYaAsociadaException si la pulsera ya está asociada de
     * forma conflictiva.
     * @throws EntradaNoNominadaException si la entrada no está
     * nominada.
     * @throws IllegalStateException si la entrada o pulsera no está en un
     * estado válido para la asociación.
     * @throws FestivalNotFoundException si el festival inferido o proporcionado
     * no se encuentra.
     * @throws SecurityException si hay un conflicto de festivales o un intento
     * de operación no permitida.
     */
    PulseraNFCDTO asociarPulseraViaQrEntrada(String codigoQrEntrada, String codigoUidPulsera, Integer idFestivalContexto)
            throws EntradaNotFoundException, PulseraNFCNotFoundException,
            PulseraYaAsociadaException, EntradaNoNominadaException,
            IllegalStateException, FestivalNotFoundException, SecurityException;

}
