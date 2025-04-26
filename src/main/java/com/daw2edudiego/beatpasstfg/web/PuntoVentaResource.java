package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.PulseraNFCService;
import com.daw2edudiego.beatpasstfg.service.PulseraNFCServiceImpl;

// Imports JAX-RS
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext; // Para obtener info del usuario autenticado (JWT)
import jakarta.ws.rs.core.UriInfo;      // Para obtener info de la URI

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Clases estándar Java
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Recurso JAX-RS (@Path) que define los endpoints de la API RESTful para
 * operaciones de Punto de Venta (POS), Cajero o Control de Acceso. **Modificado
 * para operar dentro del contexto de un festival específico.**
 *
 * @see PulseraNFCService
 * @see SecurityContext
 * @author Eduardo Olalde
 */
@Path("/pos") // Ruta base para los endpoints de POS/Cajero
@Produces(MediaType.APPLICATION_JSON) // Por defecto, las respuestas serán JSON
public class PuntoVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PuntoVentaResource.class);

    // Inyección manual de dependencias
    private final PulseraNFCService pulseraNFCService;

    // Inyección de contexto JAX-RS
    @Context
    private SecurityContext securityContext;
    @Context
    private UriInfo uriInfo;

    /**
     * Constructor que inicializa los servicios necesarios.
     */
    public PuntoVentaResource() {
        this.pulseraNFCService = new PulseraNFCServiceImpl();
    }

    /**
     * Endpoint GET para obtener los datos (incluyendo saldo) de una pulsera NFC
     * por su UID. Requiere autenticación y que el actor tenga permisos. La
     * verificación de permisos específica (ej: promotor solo ve pulseras de su
     * festival) se realiza en la capa de servicio.
     *
     * @param codigoUid El UID de la pulsera a consultar (en path).
     * @return Respuesta HTTP con el DTO de la pulsera o un error.
     */
    @GET
    @Path("/pulseras/{codigoUid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerDatosPulsera(@PathParam("codigoUid") String codigoUid) {
        log.debug("GET /pos/pulseras/{} recibido", codigoUid);
        Integer idActor;
        try {
            // Verificar autenticación y rol básico (ADMIN, PROMOTOR, CAJERO)
            idActor = verificarAccesoActor();
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso");
        }

        if (codigoUid == null || codigoUid.isBlank()) {
            return manejarErrorApi(new BadRequestException("El código UID de la pulsera es obligatorio."), "obteniendo datos pulsera");
        }

        try {
            // Llamar al servicio (la verificación de permisos específica se hace dentro)
            Optional<PulseraNFCDTO> pulseraDTOOpt = pulseraNFCService.obtenerPulseraPorCodigoUid(codigoUid, idActor);

            if (pulseraDTOOpt.isPresent()) {
                log.debug("Datos obtenidos para pulsera UID {}", codigoUid);
                return Response.ok(pulseraDTOOpt.get()).build(); // 200 OK con DTO
            } else {
                // Si devuelve vacío puede ser porque no existe o porque no tiene permiso
                log.warn("Pulsera UID {} no encontrada o sin permiso para actor ID {}", codigoUid, idActor);
                // Devolver 404 genérico en este caso
                return manejarErrorApi(new PulseraNFCNotFoundException("Pulsera no encontrada o sin permiso: " + codigoUid), "obteniendo datos pulsera");
            }

        } catch (Exception e) {
            // Capturar otras excepciones del servicio o inesperadas
            return manejarErrorApi(e, "obteniendo datos pulsera UID " + codigoUid);
        }
    }

    /**
     * Endpoint POST para registrar una recarga de saldo en una pulsera NFC.
     * Espera los datos como parámetros de formulario y el ID del festival como
     * query param. Requiere autenticación (CAJERO, ADMIN o PROMOTOR). El
     * servicio verificará que la pulsera pertenezca al festival indicado.
     *
     * @param codigoUid UID de la pulsera a recargar (en path).
     * @param festivalId ID del festival en cuyo contexto se realiza la recarga
     * (en query). **Obligatorio**.
     * @param monto Monto a recargar (en form). **Obligatorio y positivo**.
     * @param metodoPago Método de pago (en form, opcional).
     * @return Respuesta HTTP con el DTO actualizado o un error.
     */
    @POST
    @Path("/pulseras/{codigoUid}/recargar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Espera datos de formulario
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarRecarga(
            @PathParam("codigoUid") String codigoUid,
            @QueryParam("festivalId") Integer festivalId, // ID del festival como QueryParam
            @FormParam("monto") BigDecimal monto,
            @FormParam("metodoPago") String metodoPago) {

        log.info("POST /pos/pulseras/{}/recargar?festivalId={} recibido - Monto: {}, Metodo: {}",
                codigoUid, festivalId, monto, metodoPago);
        Integer idUsuarioCajero; // El actor que realiza la recarga
        try {
            idUsuarioCajero = verificarAccesoActor(); // Verifica token y rol
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso");
        }

        // Validar que festivalId se ha proporcionado
        if (festivalId == null) {
            return manejarErrorApi(new BadRequestException("El parámetro 'festivalId' es obligatorio en la URL (?festivalId=...)."), "registrando recarga");
        }
        if (codigoUid == null || codigoUid.isBlank()) {
            return manejarErrorApi(new BadRequestException("El código UID de la pulsera es obligatorio."), "registrando recarga");
        }
        // La validación del monto se delega al servicio

        try {
            // Llamar al servicio, pasando ahora también festivalId
            PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarRecarga(codigoUid, monto, metodoPago, idUsuarioCajero, festivalId);
            log.info("Recarga exitosa para pulsera UID {} en festival {}. Nuevo saldo: {}", codigoUid, festivalId, pulseraActualizada.getSaldo());
            return Response.ok(pulseraActualizada).build(); // 200 OK con DTO actualizado

        } catch (Exception e) {
            // Capturar y mapear excepciones del servicio (NotFound, Security, IllegalArgument, etc.)
            return manejarErrorApi(e, "registrando recarga para UID " + codigoUid + " en festival " + festivalId);
        }
    }

    /**
     * Endpoint POST para registrar un consumo (gasto) con una pulsera NFC.
     * Espera los datos como parámetros de formulario. El ID del festival es
     * obligatorio. Requiere autenticación (CAJERO, ADMIN o PROMOTOR). El
     * servicio verificará que la pulsera pertenezca al festival indicado.
     *
     * @param codigoUid UID de la pulsera (en path).
     * @param monto Monto a consumir (en form). **Obligatorio y positivo**.
     * @param descripcion Descripción del consumo (en form). **Obligatorio**.
     * @param idFestival ID del festival donde ocurre el consumo (en form).
     * **Obligatorio**.
     * @param idPuntoVenta ID opcional del punto de venta (en form).
     * @return Respuesta HTTP con el DTO actualizado o un error.
     */
    @POST
    @Path("/pulseras/{codigoUid}/consumir")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Espera datos de formulario
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarConsumo(
            @PathParam("codigoUid") String codigoUid,
            @FormParam("monto") BigDecimal monto,
            @FormParam("descripcion") String descripcion,
            @FormParam("idFestival") Integer idFestival, // ID del festival en el cuerpo del form
            @FormParam("idPuntoVenta") Integer idPuntoVenta) {

        log.info("POST /pos/pulseras/{}/consumir recibido - Monto: {}, Desc: {}, FestivalID: {}",
                codigoUid, monto, descripcion, idFestival);
        Integer idActor; // El actor (cajero/admin/promotor) que registra el consumo
        try {
            idActor = verificarAccesoActor(); // Verifica token y rol
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso");
        }

        if (codigoUid == null || codigoUid.isBlank()) {
            return manejarErrorApi(new BadRequestException("El código UID de la pulsera es obligatorio."), "registrando consumo");
        }
        // La validación de otros parámetros (monto, descripcion, idFestival) se hace en el servicio

        try {
            // Llamar al servicio (ya recibía idFestival)
            PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarConsumo(codigoUid, monto, descripcion, idFestival, idPuntoVenta, idActor);
            log.info("Consumo de {} registrado exitosamente para pulsera UID {} en festival {}. Nuevo saldo: {}",
                    monto, codigoUid, idFestival, pulseraActualizada.getSaldo());
            return Response.ok(pulseraActualizada).build(); // 200 OK con DTO actualizado

        } catch (Exception e) {
            // Capturar y mapear excepciones del servicio (NotFound, Security, SaldoInsuficiente, etc.)
            return manejarErrorApi(e, "registrando consumo para UID " + codigoUid + " en festival " + idFestival);
        }
    }

    // --- Métodos Auxiliares ---
    /**
     * Verifica si hay un usuario autenticado a través del SecurityContext y si
     * tiene uno de los roles permitidos (CAJERO, ADMIN, PROMOTOR).
     *
     * @return El ID del usuario autenticado.
     * @throws NotAuthorizedException Si no está autenticado.
     * @throws ForbiddenException Si no tiene un rol permitido.
     * @throws InternalServerErrorException Si hay error al obtener el ID.
     */
    private Integer verificarAccesoActor() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            log.warn("Intento de acceso a recurso POS sin SecurityContext válido (JWT?).");
            throw new NotAuthorizedException("Autenticación requerida.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        String userIdStr = securityContext.getUserPrincipal().getName();
        Integer userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            log.error("No se pudo parsear userId '{}' desde el Principal.", userIdStr);
            throw new InternalServerErrorException("Error interno de autenticación.");
        }
        // Verificar roles permitidos para operar en el POS
        boolean tieneRolPermitido = securityContext.isUserInRole(RolUsuario.CAJERO.name())
                || securityContext.isUserInRole(RolUsuario.ADMIN.name())
                || securityContext.isUserInRole(RolUsuario.PROMOTOR.name());
        if (!tieneRolPermitido) {
            log.warn("Usuario ID {} intentó acceder a recurso POS sin rol permitido (CAJERO/ADMIN/PROMOTOR).", userId);
            throw new ForbiddenException("Rol no autorizado para esta operación.");
        }
        log.debug("Acceso POS permitido para actor ID: {}", userId);
        return userId;
    }

    /**
     * Mapea excepciones comunes a respuestas JAX-RS estándar.
     *
     * @param e La excepción capturada.
     * @param operacion Descripción de la operación (para logs).
     * @return Una Response JAX-RS con el estado y mensaje adecuados.
     */
    private Response manejarErrorApi(Exception e, String operacion) {
        Response.Status status;
        String mensaje;

        // Mapeo de excepciones específicas a códigos de estado HTTP
        if (e instanceof NotFoundException || e instanceof PulseraNFCNotFoundException
                || e instanceof EntradaAsignadaNotFoundException || e instanceof AsistenteNotFoundException
                || e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException) {
            status = Response.Status.NOT_FOUND; // 404
            mensaje = e.getMessage();
            log.warn("Error 404 {}: {}", operacion, mensaje);
        } else if (e instanceof SecurityException || e instanceof ForbiddenException) {
            status = Response.Status.FORBIDDEN; // 403
            mensaje = e.getMessage();
            log.warn("Error 403 {}: {}", operacion, mensaje);
        } else if (e instanceof NotAuthorizedException) {
            status = Response.Status.UNAUTHORIZED; // 401
            mensaje = e.getMessage();
            log.warn("Error 401 {}: {}", operacion, mensaje);
        } else if (e instanceof IllegalArgumentException || e instanceof BadRequestException
                || e instanceof SaldoInsuficienteException || e instanceof PulseraYaAsociadaException
                || e instanceof EntradaAsignadaNoNominadaException || e instanceof IllegalStateException) {
            status = Response.Status.BAD_REQUEST; // 400 (o 409 Conflict para algunos casos)
            if (e instanceof SaldoInsuficienteException) {
                // Podríamos usar 409 Conflict o 422 Unprocessable Entity para saldo insuficiente
                status = Response.Status.CONFLICT;
            }
            mensaje = e.getMessage();
            log.warn("Error {} {}: {}", status.getStatusCode(), operacion, mensaje);
        } else {
            // Errores no esperados
            status = Response.Status.INTERNAL_SERVER_ERROR; // 500
            mensaje = "Error interno inesperado.";
            log.error("Error 500 {}: {}", operacion, e.getMessage(), e);
        }

        // Construir cuerpo de respuesta JSON
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", mensaje);

        return Response.status(status).entity(errorResponse).build();
    }
}
