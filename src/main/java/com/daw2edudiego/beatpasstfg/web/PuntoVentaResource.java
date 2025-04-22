package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.PulseraNFCService;
import com.daw2edudiego.beatpasstfg.service.PulseraNFCServiceImpl;
// Quitamos UsuarioService si solo se usa para verificar rol en sesión
// import com.daw2edudiego.beatpasstfg.service.UsuarioService;
// import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;

// Quitamos imports de Servlet si ya no se usan para sesión
// import jakarta.servlet.RequestDispatcher;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext; // <-- Importar SecurityContext
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import java.io.IOException; // No necesario si no usamos forward
import java.math.BigDecimal;
// import java.net.URI; // No necesario si no redirigimos
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Recurso JAX-RS para operaciones de Punto de Venta (POS) / Cajero / Acceso.
 * Relacionado principalmente con la gestión de Pulseras NFC. La autenticación
 * se basa en TOKEN JWT (Bearer). ACTUALIZADO: Usa SecurityContext para
 * verificar actor y devuelve JSON.
 */
@Path("/pos")
@Produces(MediaType.APPLICATION_JSON) // Por defecto devuelve JSON
@Consumes(MediaType.APPLICATION_JSON) // Por defecto espera JSON (excepto forms)
public class PuntoVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PuntoVentaResource.class);

    private final PulseraNFCService pulseraNFCService;
    // private final UsuarioService usuarioService; // Ya no es necesario aquí

    // Inyectar SecurityContext para obtener info del usuario autenticado (JWT)
    @Context
    private SecurityContext securityContext;

    // @Context private HttpServletRequest request; // Ya no necesario para sesión
    // @Context private HttpServletResponse response; // Ya no necesario para forward
    @Context
    private UriInfo uriInfo; // Puede ser útil para logs o links

    public PuntoVentaResource() {
        this.pulseraNFCService = new PulseraNFCServiceImpl();
        // this.usuarioService = new UsuarioServiceImpl(); // Ya no necesario
    }

    // --- Endpoints para interfaz web simple (JSP) ---
    // Estos endpoints ya no son necesarios si la simulación es una app separada HTML+JS
    // GET /api/pos/ -> Lo manejará el servidor web que sirva el HTML
    // GET /api/pos/buscar-pulsera -> La lógica estará en el JS que llama a la API
    // --- Endpoints API (devuelven JSON) ---
    /**
     * Asocia una pulsera a una entrada asignada. Espera datos como form params.
     * Devuelve JSON.
     */
    @POST
    @Path("/asociar-pulsera")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Mantenemos form params por simplicidad desde HTML
    @Produces(MediaType.APPLICATION_JSON)
    public Response asociarPulsera(
            @FormParam("codigoUid") String codigoUid,
            @FormParam("idEntradaAsignada") Integer idEntradaAsignada) {

        log.info("POST /pos/asociar-pulsera recibido - UID: {}, EntradaID: {}", codigoUid, idEntradaAsignada);
        Integer idActor = verificarAccesoActor(); // Verifica token y obtiene ID

        try {
            PulseraNFCDTO pulseraDTO = pulseraNFCService.asociarPulseraEntrada(codigoUid, idEntradaAsignada, idActor);
            log.info("Asociación exitosa para pulsera UID {}", codigoUid);
            return Response.ok(pulseraDTO).build(); // Devuelve DTO como JSON
        } catch (Exception e) {
            return manejarErrorApi(e, "asociando pulsera");
        }
    }

    /**
     * Obtiene el saldo y detalles de una pulsera por su UID. Devuelve JSON.
     */
    @GET
    @Path("/pulseras/{codigoUid}") // Endpoint más RESTful para obtener datos de la pulsera
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerDatosPulsera(@PathParam("codigoUid") String codigoUid) {
        log.debug("GET /pos/pulseras/{} recibido", codigoUid);
        Integer idActor = verificarAccesoActor();

        try {
            PulseraNFCDTO pulseraDTO = pulseraNFCService.obtenerPulseraPorCodigoUid(codigoUid, idActor)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada o sin permiso: " + codigoUid));
            log.debug("Datos obtenidos para pulsera UID {}", codigoUid);
            return Response.ok(pulseraDTO).build(); // Devolver el DTO completo
        } catch (Exception e) {
            return manejarErrorApi(e, "obteniendo datos pulsera");
        }
    }

    /**
     * Registra una recarga para una pulsera. Devuelve JSON.
     */
    @POST
    @Path("/pulseras/{codigoUid}/recargar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarRecarga(
            @PathParam("codigoUid") String codigoUid,
            @FormParam("monto") BigDecimal monto,
            @FormParam("metodoPago") String metodoPago) {

        log.info("POST /pos/pulseras/{}/recargar recibido - Monto: {}, Metodo: {}", codigoUid, monto, metodoPago);
        Integer idUsuarioCajero = verificarAccesoActor();

        try {
            PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarRecarga(codigoUid, monto, metodoPago, idUsuarioCajero);
            log.info("Recarga exitosa para pulsera UID {}", codigoUid);
            return Response.ok(pulseraActualizada).build();
        } catch (Exception e) {
            return manejarErrorApi(e, "registrando recarga");
        }
    }

    /**
     * Registra un consumo para una pulsera. Devuelve JSON.
     */
    @POST
    @Path("/pulseras/{codigoUid}/consumir")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarConsumo(
            @PathParam("codigoUid") String codigoUid,
            @FormParam("monto") BigDecimal monto,
            @FormParam("descripcion") String descripcion,
            @FormParam("idFestival") Integer idFestival, // Necesario para asociar el consumo
            @FormParam("idPuntoVenta") Integer idPuntoVenta) { // Opcional

        log.info("POST /pos/pulseras/{}/consumir recibido - Monto: {}, Desc: {}, FestivalID: {}", codigoUid, monto, descripcion, idFestival);
        Integer idActor = verificarAccesoActor();

        try {
            PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarConsumo(codigoUid, monto, descripcion, idFestival, idPuntoVenta, idActor);
            log.info("Consumo exitoso para pulsera UID {}", codigoUid);
            return Response.ok(pulseraActualizada).build();
        } catch (Exception e) {
            return manejarErrorApi(e, "registrando consumo");
        }
    }

    // --- Métodos Auxiliares ---
    /**
     * Verifica si hay un usuario autenticado vía JWT (SecurityContext) y si
     * tiene el rol adecuado (CAJERO, ADMIN o PROMOTOR por ahora). Devuelve el
     * ID del usuario o lanza NotAuthorizedException.
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
                || securityContext.isUserInRole(RolUsuario.PROMOTOR.name()); // Permitir a promotor temporalmente?

        if (!tieneRolPermitido) {
            log.warn("Usuario ID {} intentó acceder a recurso POS sin rol permitido (CAJERO/ADMIN/PROMOTOR).", userId);
            throw new ForbiddenException("Rol no autorizado para esta operación.");
        }

        log.debug("Acceso POS permitido para actor ID: {}", userId);
        return userId;
    }

    /**
     * Maneja excepciones y genera respuesta de error JSON adecuada.
     */
    private Response manejarErrorApi(Exception e, String operacion) {
        Response.Status status;
        String mensaje;
        if (e instanceof NotFoundException || e instanceof PulseraNFCNotFoundException || e instanceof EntradaAsignadaNotFoundException || e instanceof AsistenteNotFoundException || e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException) {
            status = Response.Status.NOT_FOUND;
            mensaje = e.getMessage();
            log.warn("Error 404 {}: {}", operacion, mensaje);
        } else if (e instanceof SecurityException || e instanceof ForbiddenException || e instanceof NotAuthorizedException) {
            status = Response.Status.FORBIDDEN;
            mensaje = e.getMessage();
            log.warn("Error 403/401 {}: {}", operacion, mensaje);
        } else if (e instanceof IllegalArgumentException || e instanceof BadRequestException || e instanceof SaldoInsuficienteException || e instanceof PulseraYaAsociadaException || e instanceof EntradaAsignadaNoNominadaException || e instanceof IllegalStateException) {
            status = Response.Status.BAD_REQUEST;
            mensaje = e.getMessage();
            log.warn("Error 400 {}: {}", operacion, mensaje);
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
            mensaje = "Error interno inesperado.";
            log.error("Error 500 {}: {}", operacion, e.getMessage(), e);
        }
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", mensaje);
        return Response.status(status).entity(errorResponse).build();
    }

    // Ya no necesitamos mostrarMensajeFlash ni los forwards a JSPs desde aquí
}
