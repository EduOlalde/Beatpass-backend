package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.PulseraNFCService;
import com.daw2edudiego.beatpasstfg.service.PulseraNFCServiceImpl;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap; 
import java.util.Map;
import java.util.Optional;

/**
 * Recurso JAX-RS para operaciones de Punto de Venta (POS) (/api/pos). Requiere
 * autenticación JWT (CAJERO, ADMIN o PROMOTOR). Opera en el contexto de un
 * festival específico.
 */
@Path("/pos")
@Produces(MediaType.APPLICATION_JSON)
public class PuntoVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PuntoVentaResource.class);

    private final PulseraNFCService pulseraNFCService;

    @Context
    private SecurityContext securityContext;
    @Context
    private UriInfo uriInfo; // No se usa actualmente, pero disponible

    public PuntoVentaResource() {
        this.pulseraNFCService = new PulseraNFCServiceImpl();
    }

    /**
     * Obtiene datos (incluyendo saldo) de una pulsera por UID. Requiere rol
     * CAJERO, ADMIN o PROMOTOR.
     *
     * @param codigoUid UID de la pulsera.
     * @return 200 OK con PulseraNFCDTO, 400/401/403/404 Error, 500 Error.
     */
    @GET
    @Path("/pulseras/{codigoUid}")
    public Response obtenerDatosPulsera(@PathParam("codigoUid") String codigoUid) {
        log.debug("GET /pos/pulseras/{}", codigoUid);
        Integer idActor;
        try {
            idActor = verificarAccesoActor();
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso para obtener datos pulsera");
        }

        if (codigoUid == null || codigoUid.isBlank()) {
            return manejarErrorApi(new BadRequestException("Código UID obligatorio."), "obtener datos pulsera");
        }

        try {
            Optional<PulseraNFCDTO> pulseraDTOOpt = pulseraNFCService.obtenerPulseraPorCodigoUid(codigoUid, idActor);
            return pulseraDTOOpt
                    .map(dto -> {
                        log.debug("Datos obtenidos para pulsera UID {}", codigoUid);
                        return Response.ok(dto).build();
                    })
                    .orElseGet(() -> {
                        log.warn("Pulsera UID {} no encontrada o sin permiso para actor ID {}", codigoUid, idActor);
                        return manejarErrorApi(new PulseraNFCNotFoundException("Pulsera no encontrada o sin permiso: " + codigoUid), "obteniendo datos pulsera");
                    });
        } catch (Exception e) {
            return manejarErrorApi(e, "obteniendo datos pulsera UID " + codigoUid);
        }
    }

    /**
     * Registra una recarga de saldo en una pulsera. Requiere rol CAJERO, ADMIN
     * o PROMOTOR.
     *
     * @param codigoUid UID de la pulsera.
     * @param festivalId ID del festival (QueryParam obligatorio).
     * @param monto Monto a recargar (FormParam obligatorio > 0).
     * @param metodoPago Método de pago (FormParam opcional).
     * @return 200 OK con PulseraNFCDTO actualizado, 400/401/403/404 Error, 500
     * Error.
     */
    @POST
    @Path("/pulseras/{codigoUid}/recargar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response registrarRecarga(
            @PathParam("codigoUid") String codigoUid,
            @QueryParam("festivalId") Integer festivalId, // Mantenido como QueryParam según el original
            @FormParam("monto") BigDecimal monto,
            @FormParam("metodoPago") String metodoPago) {

        log.info("POST /pos/pulseras/{}/recargar?festivalId={} - Monto: {}, Metodo: {}",
                codigoUid, festivalId, monto, metodoPago);
        Integer idUsuarioCajero;
        try {
            idUsuarioCajero = verificarAccesoActor();
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso para registrar recarga");
        }

        if (festivalId == null) {
            return manejarErrorApi(new BadRequestException("Parámetro 'festivalId' obligatorio."), "registrando recarga");
        }
        if (codigoUid == null || codigoUid.isBlank()) {
            return manejarErrorApi(new BadRequestException("Código UID obligatorio."), "registrando recarga");
        }
        // Validación del monto se delega al servicio

        try {
            PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarRecarga(codigoUid, monto, metodoPago, idUsuarioCajero, festivalId);
            log.info("Recarga exitosa UID {} en festival {}. Nuevo saldo: {}", codigoUid, festivalId, pulseraActualizada.getSaldo());
            return Response.ok(pulseraActualizada).build();
        } catch (Exception e) {
            // manejarErrorApi se encarga de loguear y mapear la excepción
            return manejarErrorApi(e, "registrando recarga UID " + codigoUid + " fest " + festivalId);
        }
    }

    /**
     * Registra un consumo (gasto) con una pulsera. Requiere rol CAJERO, ADMIN o
     * PROMOTOR.
     *
     * @param codigoUid UID de la pulsera.
     * @param monto Monto a consumir (FormParam obligatorio > 0).
     * @param descripcion Descripción (FormParam obligatorio).
     * @param idFestival ID del festival (FormParam obligatorio).
     * @param idPuntoVenta ID opcional del punto de venta (FormParam).
     * @return 200 OK con PulseraNFCDTO actualizado, 400/401/403/404/409 Error,
     * 500 Error.
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
        Integer idActor;
        try {
            idActor = verificarAccesoActor();
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso para registrar consumo");
        }

        if (codigoUid == null || codigoUid.isBlank()) {
            return manejarErrorApi(new BadRequestException("Código UID obligatorio."), "registrando consumo");
        }
        if (idFestival == null) { // Aseguramos que idFestival sea obligatorio
            return manejarErrorApi(new BadRequestException("Parámetro 'idFestival' obligatorio."), "registrando consumo");
        }
        // Validación de otros params en servicio

        try {
            PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarConsumo(codigoUid, monto, descripcion, idFestival, idPuntoVenta, idActor);
            log.info("Consumo {} registrado UID {} fest {}. Nuevo saldo: {}", monto, codigoUid, idFestival, pulseraActualizada.getSaldo());
            return Response.ok(pulseraActualizada).build();
        } catch (Exception e) {
            return manejarErrorApi(e, "registrando consumo UID " + codigoUid + " fest " + idFestival);
        }
    }

    /**
     * Asocia una pulsera NFC a una entrada mediante el código QR de la entrada.
     * Requiere rol CAJERO, ADMIN o PROMOTOR.
     *
     * @param codigoQrEntrada Código QR de la entrada.
     * @param codigoUidPulsera UID de la pulsera NFC.
     * @param idFestival ID del festival (obligatorio).
     * @return 200 OK con mensaje y datos de la pulsera, o error 4xx/5xx.
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

        try {
            // Verificar acceso del actor (cajero, admin, promotor)
            verificarAccesoActor();
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso para asociar pulsera a entrada");
        }

        if (codigoQrEntrada == null || codigoQrEntrada.isBlank()
                || codigoUidPulsera == null || codigoUidPulsera.isBlank()) {
            return manejarErrorApi(new BadRequestException("Los parámetros 'codigoQrEntrada' y 'codigoUidPulsera' son obligatorios."), "asociando pulsera");
        }
        if (idFestival == null) {
            return manejarErrorApi(new BadRequestException("El parámetro 'idFestival' es obligatorio."), "asociando pulsera");
        }

        try {
            PulseraNFCDTO pulseraAsociadaDTO = pulseraNFCService.asociarPulseraViaQrEntrada(codigoQrEntrada, codigoUidPulsera, idFestival);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("mensaje", "Pulsera UID " + pulseraAsociadaDTO.getCodigoUid() + " asociada correctamente a la entrada con QR.");
            successResponse.put("pulsera", pulseraAsociadaDTO);

            log.info("Pulsera UID {} asociada a entrada QR {} en festival {}", pulseraAsociadaDTO.getCodigoUid(), qrLog, idFestival);
            return Response.ok(successResponse).build();

        } catch (Exception e) {
            return manejarErrorApi(e, "asociando pulsera UID " + codigoUidPulsera + " a QR " + qrLog);
        }
    }

    // --- Métodos Auxiliares ---
    /**
     * Verifica autenticación y rol (CAJERO, ADMIN, PROMOTOR). Lanza excepciones
     * JAX-RS.
     *
     * @return User ID del actor autenticado.
     * @throws NotAuthorizedException si no está autenticado.
     * @throws ForbiddenException si no tiene el rol adecuado.
     * @throws InternalServerErrorException si hay problemas con el ID de
     * usuario.
     */
    private Integer verificarAccesoActor() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            log.warn("Intento de acceso POS sin SecurityContext válido.");
            throw new NotAuthorizedException("Autenticación requerida.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        String userIdStr = securityContext.getUserPrincipal().getName();
        Integer userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            log.error("No se pudo parsear userId '{}' desde Principal.", userIdStr, e);
            throw new InternalServerErrorException("Error interno de autenticación al parsear ID de usuario.");
        }

        boolean tieneRolPermitido = securityContext.isUserInRole(RolUsuario.CAJERO.name())
                || securityContext.isUserInRole(RolUsuario.ADMIN.name())
                || securityContext.isUserInRole(RolUsuario.PROMOTOR.name());

        if (!tieneRolPermitido) {
            log.warn("Usuario ID {} (Roles: {}) intentó acceder a POS sin rol permitido (CAJERO, ADMIN, PROMOTOR).", userId, securityContext.getUserPrincipal());
            throw new ForbiddenException("Rol no autorizado para esta operación.");
        }
        log.debug("Acceso POS permitido para actor ID: {}", userId);
        return userId;
    }

    /**
     * Mapea excepciones comunes a respuestas JAX-RS estándar con cuerpo JSON.
     *
     * @param e Excepción capturada.
     * @param operacion Descripción de la operación donde ocurrió el error.
     * @return Objeto Response con el estado y mensaje de error apropiados.
     */
    private Response manejarErrorApi(Exception e, String operacion) {
        Response.Status status;
        String mensaje;

        if (e instanceof NotFoundException || e instanceof PulseraNFCNotFoundException || e instanceof EntradaNotFoundException
                || e instanceof AsistenteNotFoundException || e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException) {
            status = Response.Status.NOT_FOUND; // 404
            mensaje = e.getMessage();
            log.warn("Error 404 durante '{}': {}", operacion, mensaje);
        } else if (e instanceof SecurityException || e instanceof ForbiddenException) {
            status = Response.Status.FORBIDDEN; // 403
            mensaje = e.getMessage();
            log.warn("Error 403 durante '{}': {}", operacion, mensaje);
        } else if (e instanceof NotAuthorizedException) {
            status = Response.Status.UNAUTHORIZED; // 401
            mensaje = e.getMessage();
            log.warn("Error 401 durante '{}': {}", operacion, mensaje);
        } else if (e instanceof IllegalArgumentException || e instanceof BadRequestException
                || e instanceof EntradaNoNominadaException || e instanceof IllegalStateException) {
            // PulseraYaAsociadaException también podría ser CONFLICT (409) pero el servicio podría decidir.
            status = Response.Status.BAD_REQUEST; // 400
            mensaje = e.getMessage();
            log.warn("Error 400/Bad Request durante '{}': {}", operacion, mensaje);
        } else if (e instanceof SaldoInsuficienteException || e instanceof PulseraYaAsociadaException || e instanceof StockInsuficienteException) {
            // Considerar StockInsuficienteException aquí también si aplica a POS.
            status = Response.Status.CONFLICT; // 409
            mensaje = e.getMessage();
            log.warn("Error 409/Conflict durante '{}': {}", operacion, mensaje);
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR; // 500
            mensaje = "Error interno inesperado al procesar la solicitud."; // Mensaje genérico para el cliente
            log.error("Error 500/Interno durante '{}': {}", operacion, e.getMessage(), e); // Loguear el detalle real
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", mensaje);
        return Response.status(status).entity(errorResponse).build();
    }
}
