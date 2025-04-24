package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.exception.*; // Importar todas las excepciones relevantes
import com.daw2edudiego.beatpasstfg.model.PulseraNFC;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Define la interfaz para la lógica de negocio relacionada con las entidades
 * {@link PulseraNFC}. Gestiona operaciones como asociación, consulta, recarga y
 * consumo, aplicando reglas de negocio y control de permisos.
 *
 * @see PulseraNFC
 * @see PulseraNFCDTO
 * @see PulseraNFCServiceImpl
 * @author Eduardo Olalde
 */
public interface PulseraNFCService {

    /**
     * Asocia una pulsera NFC (identificada por su UID) a una entrada asignada
     * específica. Si la pulsera con el UID proporcionado no existe en la base
     * de datos, se crea una nueva.
     * <p>
     * Se realizan las siguientes verificaciones antes de la asociación:
     * <ul>
     * <li>Existencia y validez del usuario actor ({@code idActor}).</li>
     * <li>Permisos del actor (ADMIN, PROMOTOR o CAJERO pueden asociar).</li>
     * <li>Existencia de la entrada asignada ({@code idEntradaAsignada}).</li>
     * <li>Estado ACTIVA de la entrada asignada.</li>
     * <li>Nominación de la entrada asignada a un asistente.</li>
     * <li>Que la entrada asignada no tenga ya otra pulsera asociada.</li>
     * <li>Si la pulsera (por UID) ya existe:
     * <ul>
     * <li>Que esté ACTIVA.</li>
     * <li>Que no esté ya asociada a OTRA entrada ACTIVA del MISMO
     * festival.</li>
     * </ul>
     * </li>
     * <li>Si el actor es PROMOTOR, que sea el dueño del festival asociado a la
     * entrada.</li>
     * </ul>
     * La operación es transaccional.
     *
     * @param codigoUid UID (identificador único del chip) de la pulsera a
     * asociar o crear. No debe ser {@code null} ni vacío.
     * @param idEntradaAsignada ID de la entrada asignada a la que se asociará
     * la pulsera. Debe existir, estar ACTIVA y nominada. No debe ser
     * {@code null}.
     * @param idActor ID del usuario (ADMIN, PROMOTOR o CAJERO) que realiza la
     * acción. No debe ser {@code null}.
     * @return El {@link PulseraNFCDTO} de la pulsera asociada (ya sea existente
     * actualizada o recién creada).
     * @throws UsuarioNotFoundException Si el usuario actor no existe.
     * @throws EntradaAsignadaNotFoundException Si la entrada asignada no
     * existe.
     * @throws EntradaAsignadaNoNominadaException Si la entrada no está
     * nominada.
     * @throws PulseraNFCNotFoundException Si se produce un error inesperado al
     * buscar la pulsera por UID (poco probable).
     * @throws PulseraYaAsociadaException Si la pulsera ya está asociada a otra
     * entrada activa del mismo festival.
     * @throws IllegalStateException Si la entrada o la pulsera (si existe) no
     * están en estado ACTIVA, o si la entrada ya tiene una pulsera.
     * @throws SecurityException Si el usuario actor no tiene los permisos
     * necesarios.
     * @throws IllegalArgumentException Si alguno de los parámetros requeridos
     * es {@code null} o inválido.
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * operación.
     */
    PulseraNFCDTO asociarPulseraEntrada(String codigoUid, Integer idEntradaAsignada, Integer idActor);

    /**
     * Obtiene los detalles de una pulsera NFC específica por su ID de base de
     * datos. Se verifica que el usuario solicitante ({@code idActor}) tenga
     * permisos para ver la información (ADMIN y CAJERO pueden ver todas,
     * PROMOTOR solo las asociadas a sus festivales).
     *
     * @param idPulsera ID de la pulsera a buscar. No debe ser {@code null}.
     * @param idActor ID del usuario que realiza la solicitud. No debe ser
     * {@code null}.
     * @return Un {@link Optional} que contiene el {@link PulseraNFCDTO} si se
     * encuentra y el actor tiene permisos, o un {@code Optional} vacío en caso
     * contrario.
     * @throws UsuarioNotFoundException Si el usuario actor no existe.
     * @throws IllegalArgumentException Si alguno de los IDs es {@code null}.
     * @throws SecurityException Si el actor es PROMOTOR e intenta acceder a una
     * pulsera no asociada a sus festivales.
     * @throws RuntimeException Si ocurre un error inesperado.
     */
    Optional<PulseraNFCDTO> obtenerPulseraPorId(Integer idPulsera, Integer idActor);

    /**
     * Obtiene los detalles de una pulsera NFC específica por su código UID. Se
     * verifica que el usuario solicitante ({@code idActor}) tenga permisos para
     * ver la información (ADMIN y CAJERO pueden ver todas, PROMOTOR solo las
     * asociadas a sus festivales).
     *
     * @param codigoUid Código UID de la pulsera a buscar. No debe ser
     * {@code null} ni vacío.
     * @param idActor ID del usuario que realiza la solicitud. No debe ser
     * {@code null}.
     * @return Un {@link Optional} que contiene el {@link PulseraNFCDTO} si se
     * encuentra y el actor tiene permisos, o un {@code Optional} vacío en caso
     * contrario.
     * @throws UsuarioNotFoundException Si el usuario actor no existe.
     * @throws IllegalArgumentException Si {@code codigoUid} o {@code idActor}
     * son {@code null} o inválidos.
     * @throws SecurityException Si el actor es PROMOTOR e intenta acceder a una
     * pulsera no asociada a sus festivales.
     * @throws RuntimeException Si ocurre un error inesperado.
     */
    Optional<PulseraNFCDTO> obtenerPulseraPorCodigoUid(String codigoUid, Integer idActor);

    /**
     * Obtiene el saldo monetario actual de una pulsera NFC específica. Verifica
     * los permisos del solicitante antes de devolver el saldo.
     *
     * @param idPulsera ID de la pulsera cuyo saldo se quiere consultar. No debe
     * ser {@code null}.
     * @param idActor ID del usuario que realiza la solicitud. No debe ser
     * {@code null}.
     * @return El {@link BigDecimal} que representa el saldo actual de la
     * pulsera. Devuelve {@code BigDecimal.ZERO} si el saldo es nulo en la BD.
     * @throws PulseraNFCNotFoundException Si la pulsera no se encuentra.
     * @throws UsuarioNotFoundException Si el usuario actor no existe.
     * @throws SecurityException Si el actor no tiene permisos para consultar el
     * saldo de esta pulsera.
     * @throws IllegalArgumentException Si alguno de los IDs es {@code null}.
     * @throws RuntimeException Si ocurre un error inesperado.
     */
    BigDecimal obtenerSaldo(Integer idPulsera, Integer idActor);

    /**
     * Obtiene una lista de todas las pulseras NFC que están asociadas (a través
     * de su EntradaAsignada vinculada) a un festival específico. Se verifica
     * que el usuario solicitante ({@code idActor}) tenga permisos para ver esta
     * información (ADMIN o el PROMOTOR propietario del festival).
     *
     * @param idFestival ID del festival cuyas pulseras asociadas se quieren
     * listar. No debe ser {@code null}.
     * @param idActor ID del usuario (ADMIN o PROMOTOR dueño) que realiza la
     * solicitud. No debe ser {@code null}.
     * @return Una lista de {@link PulseraNFCDTO} de las pulseras asociadas al
     * festival. Puede estar vacía si no hay pulseras o si ocurre un error.
     * @throws FestivalNotFoundException Si el festival no se encuentra.
     * @throws UsuarioNotFoundException Si el usuario actor no existe.
     * @throws SecurityException Si el actor no tiene permisos (no es ADMIN ni
     * el PROMOTOR dueño).
     * @throws IllegalArgumentException Si alguno de los IDs es {@code null}.
     * @throws RuntimeException Si ocurre un error inesperado.
     */
    List<PulseraNFCDTO> obtenerPulserasPorFestival(Integer idFestival, Integer idActor);

    /**
     * Registra una operación de recarga de saldo para una pulsera NFC. Busca la
     * pulsera por su UID, verifica que esté activa y que el usuario
     * (cajero/admin) tenga permisos. Actualiza el saldo de la pulsera de forma
     * atómica (usando bloqueo pesimista) y crea un registro de la recarga.
     * <p>
     * La operación es transaccional.
     * </p>
     *
     * @param codigoUid UID de la pulsera a recargar. No debe ser {@code null}
     * ni vacío.
     * @param monto Cantidad a recargar. Debe ser un valor {@link BigDecimal}
     * estrictamente positivo.
     * @param metodoPago Descripción del método de pago (ej: "Efectivo",
     * "Tarjeta"). Puede ser {@code null} o vacío.
     * @param idUsuarioCajero ID del usuario (con rol CAJERO, ADMIN o PROMOTOR)
     * que registra la recarga. No debe ser {@code null}.
     * @return El {@link PulseraNFCDTO} de la pulsera con el saldo actualizado.
     * @throws PulseraNFCNotFoundException Si no se encuentra una pulsera con el
     * UID especificado.
     * @throws UsuarioNotFoundException Si el usuario cajero no existe.
     * @throws SecurityException Si el usuario cajero no tiene el rol adecuado
     * (CAJERO, ADMIN o PROMOTOR).
     * @throws IllegalStateException Si la pulsera no está activa.
     * @throws IllegalArgumentException Si {@code codigoUid}, {@code monto} o
     * {@code idUsuarioCajero} son {@code null} o inválidos (ej: monto no
     * positivo).
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * operación.
     */
    PulseraNFCDTO registrarRecarga(String codigoUid, BigDecimal monto, String metodoPago, Integer idUsuarioCajero);

    /**
     * Registra una operación de consumo (gasto) realizada con una pulsera NFC
     * en un festival específico. Busca la pulsera por UID, verifica que esté
     * activa y tenga saldo suficiente. Verifica la existencia del festival y
     * los permisos del actor. Actualiza el saldo de la pulsera de forma atómica
     * (usando bloqueo pesimista) y crea un registro del consumo asociado al
     * festival.
     * <p>
     * La operación es transaccional.
     * </p>
     *
     * @param codigoUid UID de la pulsera que realiza el consumo. No debe ser
     * {@code null} ni vacío.
     * @param monto Cantidad a consumir. Debe ser un valor {@link BigDecimal}
     * estrictamente positivo.
     * @param descripcion Descripción del bien o servicio consumido (ej:
     * "Cerveza"). No debe ser {@code null} ni vacía.
     * @param idFestival ID del festival donde se realiza el consumo. No debe
     * ser {@code null}.
     * @param idPuntoVenta ID opcional del punto de venta donde se efectuó el
     * consumo. Puede ser {@code null}.
     * @param idActor ID del usuario (con rol CAJERO, ADMIN o PROMOTOR) que
     * registra el consumo. No debe ser {@code null}.
     * @return El {@link PulseraNFCDTO} de la pulsera con el saldo actualizado.
     * @throws PulseraNFCNotFoundException Si no se encuentra una pulsera con el
     * UID especificado.
     * @throws FestivalNotFoundException Si el festival no se encuentra.
     * @throws UsuarioNotFoundException Si el usuario actor no existe.
     * @throws SaldoInsuficienteException Si la pulsera no tiene saldo
     * suficiente para cubrir el monto del consumo.
     * @throws SecurityException Si el usuario actor no tiene el rol adecuado
     * (CAJERO, ADMIN o PROMOTOR).
     * @throws IllegalStateException Si la pulsera no está activa.
     * @throws IllegalArgumentException Si {@code codigoUid}, {@code monto},
     * {@code descripcion}, {@code idFestival} o {@code idActor} son
     * {@code null} o inválidos (ej: monto no positivo).
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * operación.
     */
    PulseraNFCDTO registrarConsumo(String codigoUid, BigDecimal monto, String descripcion, Integer idFestival, Integer idPuntoVenta, Integer idActor);

}
