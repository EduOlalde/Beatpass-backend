package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.PulseraNFCService;
import com.daw2edudiego.beatpasstfg.service.PulseraNFCServiceImpl;
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo; // Para construir URIs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI; // Para construir URIs
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Recurso JAX-RS para operaciones de Punto de Venta (POS) / Cajero / Acceso.
 * Relacionado principalmente con la gestión de Pulseras NFC. La autenticación
 * se simula vía sesión por ahora.
 */
@Path("/pos")
public class PuntoVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PuntoVentaResource.class);

    private final PulseraNFCService pulseraNFCService;
    private final UsuarioService usuarioService;

    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response; // Necesario para forward
    @Context
    private UriInfo uriInfo; // Necesario para construir URIs

    public PuntoVentaResource() {
        this.pulseraNFCService = new PulseraNFCServiceImpl();
        this.usuarioService = new UsuarioServiceImpl(); // Para verificar permisos
    }

    // --- Endpoints para interfaz web simple (JSP) ---
    /**
     * GET /api/pos/ Muestra la página principal del POS (buscar pulsera).
     */
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarIndexPOS() throws ServletException, IOException {
        log.debug("GET /pos/ recibido");
        Integer idActor = verificarAccesoActor(); // Verifica sesión
        // Podríamos pasar el ID del actor al JSP si fuera necesario
        mostrarMensajeFlash(request); // Mostrar mensajes si venimos de otra acción
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/pos/pos-index.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build(); // Necesario aunque hagamos forward
    }

    /**
     * GET /api/pos/buscar-pulsera?codigoUid=... Busca una pulsera por UID y
     * muestra la página de operaciones si la encuentra.
     */
    @GET
    @Path("/buscar-pulsera")
    @Produces(MediaType.TEXT_HTML)
    public Response buscarYMostrarPulsera(@QueryParam("codigoUid") String codigoUid) throws ServletException, IOException {
        log.debug("GET /pos/buscar-pulsera con UID: {}", codigoUid);
        Integer idActor = verificarAccesoActor();

        if (codigoUid == null || codigoUid.isBlank()) {
            request.setAttribute("error", "Debe proporcionar un Código UID.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/pos/pos-index.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();
        }

        try {
            // Buscamos la pulsera (el servicio verifica permisos si es necesario)
            PulseraNFCDTO pulseraDTO = pulseraNFCService.obtenerPulseraPorCodigoUid(codigoUid, idActor)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));

            // Pasamos el DTO al JSP de operaciones
            request.setAttribute("pulsera", pulseraDTO);
            mostrarMensajeFlash(request);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/pos/pos-operacion-pulsera.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (PulseraNFCNotFoundException | SecurityException e) {
            log.warn("Error buscando pulsera UID {}: {}", codigoUid, e.getMessage());
            request.setAttribute("error", e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/pos/pos-index.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();
        } catch (Exception e) {
            log.error("Error inesperado buscando pulsera UID {}: {}", codigoUid, e.getMessage(), e);
            request.setAttribute("error", "Error interno al buscar la pulsera.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/pos/pos-index.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();
        }
    }

    // --- Endpoints API (podrían devolver JSON) ---
    /**
     * Asocia una pulsera a una entrada asignada. Espera datos como form params.
     * Devuelve JSON.
     */
    @POST
    @Path("/asociar-pulsera")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response asociarPulsera(
            @FormParam("codigoUid") String codigoUid,
            @FormParam("idEntradaAsignada") Integer idEntradaAsignada) {

        log.info("POST /pos/asociar-pulsera recibido - UID: {}, EntradaID: {}", codigoUid, idEntradaAsignada);
        Integer idActor = verificarAccesoActor();

        try {
            PulseraNFCDTO pulseraDTO = pulseraNFCService.asociarPulseraEntrada(codigoUid, idEntradaAsignada, idActor);
            log.info("Asociación exitosa para pulsera UID {}", codigoUid);
            return Response.ok(pulseraDTO).build(); // Devuelve DTO como JSON
        } catch (Exception e) {
            return manejarErrorApi(e, "asociando pulsera");
        }
    }

    /**
     * Obtiene el saldo de una pulsera por su UID. Devuelve JSON.
     */
    @GET
    @Path("/pulseras/{codigoUid}/saldo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerSaldoPulsera(@PathParam("codigoUid") String codigoUid) {
        log.debug("GET /pos/pulseras/{}/saldo recibido", codigoUid);
        Integer idActor = verificarAccesoActor();

        try {
            PulseraNFCDTO pulseraDTO = pulseraNFCService.obtenerPulseraPorCodigoUid(codigoUid, idActor)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada o sin permiso: " + codigoUid));
            BigDecimal saldo = pulseraNFCService.obtenerSaldo(pulseraDTO.getIdPulsera(), idActor);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("codigoUid", codigoUid);
            responseData.put("saldo", saldo);
            return Response.ok(responseData).build();
        } catch (Exception e) {
            return manejarErrorApi(e, "obteniendo saldo");
        }
    }

    /**
     * Registra una recarga para una pulsera. Devuelve JSON.
     */
    @POST
    @Path("/pulseras/{codigoUid}/recargar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Podría ser JSON también
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
            return Response.ok(pulseraActualizada).build(); // Devuelve DTO actualizado
        } catch (Exception e) {
            return manejarErrorApi(e, "registrando recarga");
        }
    }

    /**
     * Registra un consumo para una pulsera. Devuelve JSON.
     */
    @POST
    @Path("/pulseras/{codigoUid}/consumir")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Podría ser JSON
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarConsumo(
            @PathParam("codigoUid") String codigoUid,
            @FormParam("monto") BigDecimal monto,
            @FormParam("descripcion") String descripcion,
            @FormParam("idFestival") Integer idFestival,
            @FormParam("idPuntoVenta") Integer idPuntoVenta) { // Opcional

        log.info("POST /pos/pulseras/{}/consumir recibido - Monto: {}, Desc: {}, FestivalID: {}", codigoUid, monto, descripcion, idFestival);
        Integer idActor = verificarAccesoActor();

        try {
            PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarConsumo(codigoUid, monto, descripcion, idFestival, idPuntoVenta, idActor);
            log.info("Consumo exitoso para pulsera UID {}", codigoUid);
            return Response.ok(pulseraActualizada).build(); // Devuelve DTO actualizado
        } catch (Exception e) {
            return manejarErrorApi(e, "registrando consumo");
        }
    }

    // --- Métodos Auxiliares ---
    /**
     * Verifica acceso simulado vía sesión
     */
    private Integer verificarAccesoActor() {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            log.warn("Intento de acceso a recurso POS sin sesión válida.");
            throw new NotAuthorizedException("Se requiere autenticación.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        Integer userId = (Integer) session.getAttribute("userId");
        // String userRole = (String) session.getAttribute("userRole");
        // TODO: Implementar verificación de rol específica para POS/Cajero
        log.debug("Acceso POS permitido para actor ID: {}", userId);
        return userId;
    }

    /**
     * Maneja excepciones y genera respuesta de error JSON
     */
    private Response manejarErrorApi(Exception e, String operacion) {
        Response.Status status;
        String mensaje;
        if (e instanceof NotFoundException || e instanceof PulseraNFCNotFoundException || e instanceof EntradaAsignadaNotFoundException || e instanceof AsistenteNotFoundException || e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException) {
            status = Response.Status.NOT_FOUND;
            mensaje = e.getMessage();
            log.warn("Error 404 {}: {}", operacion, mensaje);
        } else if (e instanceof SecurityException || e instanceof ForbiddenException) {
            status = Response.Status.FORBIDDEN;
            mensaje = e.getMessage();
            log.warn("Error 403 {}: {}", operacion, mensaje);
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

    /**
     * Muestra mensajes flash de la sesión en el request actual
     */
    private void mostrarMensajeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (session.getAttribute("mensaje") != null) {
                request.setAttribute("mensaje", session.getAttribute("mensaje"));
                session.removeAttribute("mensaje");
            }
            if (session.getAttribute("error") != null) {
                request.setAttribute("error", session.getAttribute("error"));
                session.removeAttribute("error");
            }
        }
    }
}
