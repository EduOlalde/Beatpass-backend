package com.beatpass.web;

import com.beatpass.dto.PulseraNFCDTO;
import com.beatpass.exception.PulseraNFCNotFoundException;
import com.beatpass.service.PulseraNFCService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Recurso JAX-RS para operaciones de Punto de Venta (POS) (/api/pos). Requiere
 * autenticación JWT (CAJERO, ADMIN o PROMOTOR).
 */
@Path("/pos")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"CAJERO", "ADMIN", "PROMOTOR"})
@RequestScoped
public class PuntoVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PuntoVentaResource.class);

    @Inject
    private PulseraNFCService pulseraNFCService;

    @Context
    private SecurityContext securityContext;

    public PuntoVentaResource() {
    }

    /**
     * Obtiene los datos completos de una pulsera NFC a partir de su código UID.
     *
     * @param codigoUid El código UID de la pulsera.
     * @return Una respuesta HTTP 200 OK con los datos de la pulsera.
     * @throws PulseraNFCNotFoundException si la pulsera no se encuentra o no
     * hay permisos.
     */
    @GET
    @Path("/pulseras/{codigoUid}")
    public Response obtenerDatosPulsera(@PathParam("codigoUid") String codigoUid) {
        log.debug("GET /pos/pulseras/{}", codigoUid);
        Integer idActor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (codigoUid == null || codigoUid.isBlank()) {
            throw new BadRequestException("Código UID obligatorio.");
        }

        Optional<PulseraNFCDTO> pulseraDTOOpt = pulseraNFCService.obtenerPulseraPorCodigoUid(codigoUid, idActor);
        return pulseraDTOOpt
                .map(dto -> {
                    log.debug("Datos obtenidos para pulsera UID {}", codigoUid);
                    return Response.ok(dto).build();
                })
                .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada o sin permiso: " + codigoUid));
    }

    /**
     * Registra una recarga de saldo en una pulsera NFC.
     *
     * @param codigoUid El UID de la pulsera a recargar.
     * @param festivalId El ID del festival en el que se realiza la operación.
     * @param monto El importe a recargar.
     * @param metodoPago El método de pago utilizado (ej. "EFECTIVO",
     * "TARJETA").
     * @return Una respuesta HTTP 200 OK con los datos actualizados de la
     * pulsera.
     */
    @POST
    @Path("/pulseras/{codigoUid}/recargar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response registrarRecarga(
            @PathParam("codigoUid") String codigoUid,
            @QueryParam("festivalId") Integer festivalId,
            @FormParam("monto") BigDecimal monto,
            @FormParam("metodoPago") String metodoPago) {

        log.info("POST /pos/pulseras/{}/recargar?festivalId={} - Monto: {}, Metodo: {}",
                codigoUid, festivalId, monto, metodoPago);
        Integer idUsuarioCajero = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (festivalId == null) {
            throw new BadRequestException("Parámetro 'festivalId' obligatorio.");
        }
        if (codigoUid == null || codigoUid.isBlank()) {
            throw new BadRequestException("Código UID obligatorio.");
        }

        PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarRecarga(codigoUid, monto, metodoPago, idUsuarioCajero, festivalId);
        log.info("Recarga exitosa UID {} en festival {}. Nuevo saldo: {}", codigoUid, festivalId, pulseraActualizada.getSaldo());
        return Response.ok(pulseraActualizada).build();
    }

    /**
     * Registra un consumo o gasto realizado con una pulsera NFC.
     *
     * @param codigoUid El UID de la pulsera con la que se paga.
     * @param monto El importe del consumo.
     * @param descripcion Una breve descripción del producto o servicio.
     * @param idFestival El ID del festival donde se realiza el consumo.
     * @param idPuntoVenta ID opcional del punto de venta que registra el
     * consumo.
     * @return Una respuesta HTTP 200 OK con los datos actualizados de la
     * pulsera.
     */
    @POST
    @Path("/pulseras/{codigoUid}/consumir")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response registrarConsumo(
            @PathParam("codigoUid") String codigoUid,
            @FormParam("monto") BigDecimal monto,
            @FormParam("descripcion") String descripcion,
            @FormParam("idFestival") Integer idFestival,
            @FormParam("idPuntoVenta") Integer idPuntoVenta) {

        log.info("POST /pos/pulseras/{}/consumir - Monto: {}, Desc: {}, FestivalID: {}",
                codigoUid, monto, descripcion, idFestival);
        Integer idActor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (codigoUid == null || codigoUid.isBlank()) {
            throw new BadRequestException("Código UID obligatorio.");
        }
        if (idFestival == null) {
            throw new BadRequestException("Parámetro 'idFestival' obligatorio.");
        }

        PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarConsumo(codigoUid, monto, descripcion, idFestival, idPuntoVenta, idActor);
        log.info("Consumo {} registrado UID {} fest {}. Nuevo saldo: {}", monto, codigoUid, idFestival, pulseraActualizada.getSaldo());
        return Response.ok(pulseraActualizada).build();
    }

    /**
     * Asocia una pulsera NFC a una entrada escaneando el QR de la entrada. Este
     * proceso marca la entrada como 'USADA'.
     *
     * @param codigoQrEntrada El código QR de la entrada.
     * @param codigoUidPulsera El UID de la pulsera a asociar.
     * @param idFestival El ID del festival para validación de contexto.
     * @return Una respuesta HTTP 200 OK con un mensaje de éxito y los datos de
     * la pulsera.
     */
    @POST
    @Path("/pulseras/asociar-pulsera")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response asociarPulseraAEntrada(
            @FormParam("codigoQrEntrada") String codigoQrEntrada,
            @FormParam("codigoUidPulsera") String codigoUidPulsera,
            @FormParam("idFestival") Integer idFestival) {

        String qrLog = (codigoQrEntrada != null) ? codigoQrEntrada.substring(0, Math.min(20, codigoQrEntrada.length())) + "..." : "null";
        log.info("POST /pos/pulseras/asociar-pulsera - QR Entrada: {}, UID Pulsera: {}, FestivalID: {}",
                qrLog, codigoUidPulsera, idFestival);

        if (codigoQrEntrada == null || codigoQrEntrada.isBlank()
                || codigoUidPulsera == null || codigoUidPulsera.isBlank()) {
            throw new BadRequestException("Los parámetros 'codigoQrEntrada' y 'codigoUidPulsera' son obligatorios.");
        }
        if (idFestival == null) {
            throw new BadRequestException("El parámetro 'idFestival' es obligatorio.");
        }

        PulseraNFCDTO pulseraAsociadaDTO = pulseraNFCService.asociarPulseraViaQrEntrada(codigoQrEntrada, codigoUidPulsera, idFestival);

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("mensaje", "Pulsera UID " + pulseraAsociadaDTO.getCodigoUid() + " asociada correctamente a la entrada con QR.");
        successResponse.put("pulsera", pulseraAsociadaDTO);

        log.info("Pulsera UID {} asociada a entrada QR {} en festival {}", pulseraAsociadaDTO.getCodigoUid(), qrLog, idFestival);
        return Response.ok(successResponse).build();
    }
}
