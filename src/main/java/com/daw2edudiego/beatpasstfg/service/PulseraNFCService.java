package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.exception.*; // Importar excepciones NFC

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para la lógica de negocio relacionada con Pulseras NFC.
 */
public interface PulseraNFCService {

    /**
     * Asocia una pulsera (identificada por su UID) a una entrada asignada. Si
     * la pulsera con ese UID no existe, la crea. Verifica permisos del actor,
     * estado de la entrada y de la pulsera.
     *
     * @param codigoUid UID de la pulsera a asociar o crear.
     * @param idEntradaAsignada ID de la entrada asignada (debe estar nominada y
     * activa).
     * @param idActor ID del usuario (Admin, Promotor, Cajero?) que realiza la
     * acción.
     * @return DTO de la pulsera asociada/creada.
     * @throws PulseraNFCNotFoundException si se intenta asociar una pulsera
     * existente pero no se encuentra por UID (raro).
     * @throws EntradaAsignadaNotFoundException si la entrada asignada no
     * existe.
     * @throws EntradaAsignadaNoNominadaException si la entrada no está
     * nominada.
     * @throws IllegalStateException si la entrada no está ACTIVA o la pulsera
     * no está ACTIVA.
     * @throws PulseraYaAsociadaException si la pulsera ya está asociada a otra
     * entrada activa del mismo festival.
     * @throws SecurityException si el actor no tiene permisos.
     * @throws RuntimeException por otros errores.
     */
    PulseraNFCDTO asociarPulseraEntrada(String codigoUid, Integer idEntradaAsignada, Integer idActor);

    /**
     * Obtiene los detalles de una pulsera por su ID de BD.
     *
     * @param idPulsera ID de la pulsera.
     * @param idActor ID del actor solicitante (para permisos).
     * @return Optional con el DTO de la pulsera.
     */
    Optional<PulseraNFCDTO> obtenerPulseraPorId(Integer idPulsera, Integer idActor);

    /**
     * Obtiene los detalles de una pulsera por su Código UID.
     *
     * @param codigoUid UID de la pulsera.
     * @param idActor ID del actor solicitante (para permisos).
     * @return Optional con el DTO de la pulsera.
     */
    Optional<PulseraNFCDTO> obtenerPulseraPorCodigoUid(String codigoUid, Integer idActor);

    /**
     * Obtiene el saldo actual de una pulsera.
     *
     * @param idPulsera ID de la pulsera.
     * @param idActor ID del actor solicitante (para permisos).
     * @return El saldo de la pulsera.
     * @throws PulseraNFCNotFoundException si la pulsera no existe.
     * @throws SecurityException si el actor no tiene permisos.
     */
    BigDecimal obtenerSaldo(Integer idPulsera, Integer idActor);

    /**
     * Obtiene la lista de pulseras asociadas a un festival.
     *
     * @param idFestival ID del festival.
     * @param idActor ID del actor solicitante (Admin o Promotor dueño).
     * @return Lista de DTOs de las pulseras.
     */
    List<PulseraNFCDTO> obtenerPulserasPorFestival(Integer idFestival, Integer idActor);

    /**
     * Registra una recarga de saldo para una pulsera. Actualiza el saldo de la
     * pulsera.
     *
     * @param codigoUid UID de la pulsera a recargar.
     * @param monto Cantidad a recargar (debe ser positiva).
     * @param metodoPago Método de pago utilizado (ej: "Efectivo", "Tarjeta").
     * @param idUsuarioCajero ID del usuario (cajero/admin) que realiza la
     * recarga.
     * @return El DTO de la pulsera con el saldo actualizado.
     * @throws PulseraNFCNotFoundException si la pulsera no existe.
     * @throws IllegalArgumentException si el monto es inválido.
     * @throws SecurityException si el usuario cajero no tiene permiso.
     * @throws RuntimeException por otros errores.
     */
    PulseraNFCDTO registrarRecarga(String codigoUid, BigDecimal monto, String metodoPago, Integer idUsuarioCajero); // <-- NUEVO

    /**
     * Registra un consumo (gasto) realizado con una pulsera. Verifica que haya
     * saldo suficiente y actualiza el saldo. Asocia el consumo al festival
     * activo donde se realiza.
     *
     * @param codigoUid UID de la pulsera que realiza el consumo.
     * @param monto Cantidad a consumir (debe ser positiva).
     * @param descripcion Descripción del consumo (ej: "Bebida", "Comida").
     * @param idFestival ID del festival donde ocurre el consumo.
     * @param idPuntoVenta ID opcional del punto de venta.
     * @param idActor ID del usuario (cajero/admin) que registra el consumo.
     * @return El DTO de la pulsera con el saldo actualizado.
     * @throws PulseraNFCNotFoundException si la pulsera no existe.
     * @throws SaldoInsuficienteException si no hay saldo suficiente. // <--
     * NUEVA EXCEPCIÓN @throws Ill
     * egalArgumentException si el monto es inválido.
     * @throws SecurityException si el actor no tiene permiso.
     * @throws RuntimeException por otros errores.
     */
    PulseraNFCDTO registrarConsumo(String codigoUid, BigDecimal monto, String descripcion, Integer idFestival, Integer idPuntoVenta, Integer idActor); // <-- NUEVO

    
}
