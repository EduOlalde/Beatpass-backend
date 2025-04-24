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

/**
 * Recurso JAX-RS (@Path) que define los endpoints de la API RESTful para
 * operaciones de Punto de Venta (POS), Cajero o Control de Acceso.
 * <p>
 * Se centra en operaciones relacionadas con {@link PulseraNFCService}, como
 * asociar pulseras, consultar saldo/datos, registrar recargas y consumos. La
 * autenticación para estos endpoints se espera que sea gestionada mediante JSON
 * Web Tokens (JWT) validados por un filtro (ej: {@code AuthenticationFilter}),
 * y la información del usuario se obtiene a través del {@link SecurityContext}
 * inyectado.
 * </p>
 * <p>
 * Por defecto, los endpoints producen respuestas en formato
 * {@link MediaType#APPLICATION_JSON}. Los endpoints POST consumen datos de
 * formulario ({@link MediaType#APPLICATION_FORM_URLENCODED}).
 * </p>
 *
 * @see PulseraNFCService
 * @see SecurityContext
 * @see AuthenticationFilter (Implícito)
 * @author Eduardo Olalde
 */
@Path("/pos") // Ruta base para los endpoints de POS/Cajero
@Produces(MediaType.APPLICATION_JSON) // Por defecto, las respuestas serán JSON
@Consumes(MediaType.APPLICATION_JSON) // Por defecto, espera JSON (se sobrescribe en POSTs)
public class PuntoVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PuntoVentaResource.class);

    // Inyección manual de dependencias (o usar @Inject con CDI/Spring)
    private final PulseraNFCService pulseraNFCService;

    // Inyección de contexto JAX-RS
    @Context
    private SecurityContext securityContext; // Para obtener info del usuario autenticado (JWT)
    @Context
    private UriInfo uriInfo; // Útil para logs o construir URIs

    /**
     * Constructor que inicializa los servicios necesarios.
     */
    public PuntoVentaResource() {
        this.pulseraNFCService = new PulseraNFCServiceImpl();
    }

    // --- Endpoints API (devuelven JSON) ---
    /**
     * Endpoint POST para asociar una pulsera NFC (por UID) a una entrada
     * asignada. Espera los datos como parámetros de formulario. Requiere
     * autenticación con rol CAJERO, ADMIN o PROMOTOR.
     *
     * @param codigoUid UID de la pulsera a asociar.
     * @param idEntradaAsignada ID de la entrada asignada a la que asociar.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con el DTO de la pulsera asociada/actualizada en el
     * cuerpo.</li>
     * <li><b>400 Bad Request:</b> si faltan datos, son inválidos, o la
     * operación no es permitida (ej: entrada no nominada, pulsera ya
     * asociada).</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si el rol no es permitido.</li>
     * <li><b>404 Not Found:</b> si la entrada asignada no existe.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @POST
    @Path("/asociar-pulsera")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Espera datos de formulario
    @Produces(MediaType.APPLICATION_JSON)
    public Response asociarPulsera(
            @FormParam("codigoUid") String codigoUid,
            @FormParam("idEntradaAsignada") Integer idEntradaAsignada) {

        log.info("POST /pos/asociar-pulsera recibido - UID: {}, EntradaID: {}", codigoUid, idEntradaAsignada);
        Integer idActor;
        try {
            idActor = verificarAccesoActor(); // Verifica token y obtiene ID y rol permitido
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) { // Captura error interno en verificación
            return manejarErrorApi(e, "verificando acceso");
        }

        try {
            // Validar parámetros básicos (el servicio hará validaciones más profundas)
            if (codigoUid == null || codigoUid.isBlank() || idEntradaAsignada == null) {
                throw new BadRequestException("Parámetros 'codigoUid' y 'idEntradaAsignada' son obligatorios.");
            }

            // Llamar al servicio
            PulseraNFCDTO pulseraDTO = pulseraNFCService.asociarPulseraEntrada(codigoUid, idEntradaAsignada, idActor);
            log.info("Asociación exitosa para pulsera UID {}", codigoUid);
            return Response.ok(pulseraDTO).build(); // 200 OK con DTO como JSON

        } catch (Exception e) {
            // Centralizar manejo de errores (NotFound, BadRequest, Forbidden, Internal)
            return manejarErrorApi(e, "asociando pulsera UID " + codigoUid);
        }
    }

    /**
     * Endpoint GET para obtener los datos (incluyendo saldo) de una pulsera NFC
     * por su UID. Requiere autenticación con rol CAJERO, ADMIN o PROMOTOR.
     *
     * @param codigoUid El UID de la pulsera a consultar.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con el DTO de la pulsera en el cuerpo.</li>
     * <li><b>400 Bad Request:</b> si el UID es inválido.</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si el rol no es permitido.</li>
     * <li><b>404 Not Found:</b> si la pulsera no existe o no se tienen
     * permisos.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @GET
    @Path("/pulseras/{codigoUid}") // Ruta más RESTful
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerDatosPulsera(@PathParam("codigoUid") String codigoUid) {
        log.debug("GET /pos/pulseras/{} recibido", codigoUid);
        Integer idActor;
        try {
            idActor = verificarAccesoActor(); // Verifica token y rol
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso");
        }

        if (codigoUid == null || codigoUid.isBlank()) {
            throw new BadRequestException("El código UID de la pulsera es obligatorio.");
        }

        try {
            // Llamar al servicio para obtener los datos
            PulseraNFCDTO pulseraDTO = pulseraNFCService.obtenerPulseraPorCodigoUid(codigoUid, idActor)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada o sin permiso: " + codigoUid)); // Lanza 404 si Optional vacío

            log.debug("Datos obtenidos para pulsera UID {}", codigoUid);
            return Response.ok(pulseraDTO).build(); // 200 OK con DTO

        } catch (Exception e) {
            return manejarErrorApi(e, "obteniendo datos pulsera UID " + codigoUid);
        }
    }

    /**
     * Endpoint POST para registrar una recarga de saldo en una pulsera NFC.
     * Espera los datos como parámetros de formulario. Requiere autenticación
     * con rol CAJERO, ADMIN o PROMOTOR.
     *
     * @param codigoUid UID de la pulsera a recargar.
     * @param monto Monto a recargar (debe ser positivo).
     * @param metodoPago Método de pago (opcional).
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con el DTO de la pulsera actualizado en el
     * cuerpo.</li>
     * <li><b>400 Bad Request:</b> si faltan datos, son inválidos (ej: monto no
     * positivo) o la pulsera no está activa.</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si el rol no es permitido.</li>
     * <li><b>404 Not Found:</b> si la pulsera no existe.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @POST
    @Path("/pulseras/{codigoUid}/recargar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Espera datos de formulario
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarRecarga(
            @PathParam("codigoUid") String codigoUid,
            @FormParam("monto") BigDecimal monto,
            @FormParam("metodoPago") String metodoPago) {

        log.info("POST /pos/pulseras/{}/recargar recibido - Monto: {}, Metodo: {}", codigoUid, monto, metodoPago);
        Integer idUsuarioCajero; // El actor que realiza la recarga
        try {
            idUsuarioCajero = verificarAccesoActor();
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso");
        }

        if (codigoUid == null || codigoUid.isBlank()) {
            throw new BadRequestException("El código UID de la pulsera es obligatorio.");
        }

        try {
            // Validar monto (el servicio también valida, pero una verificación temprana es útil)
            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("El monto de la recarga debe ser un valor positivo.");
            }

            // Llamar al servicio
            PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarRecarga(codigoUid, monto, metodoPago, idUsuarioCajero);
            log.info("Recarga exitosa para pulsera UID {}. Nuevo saldo: {}", codigoUid, pulseraActualizada.getSaldo());
            return Response.ok(pulseraActualizada).build(); // 200 OK con DTO actualizado

        } catch (Exception e) {
            return manejarErrorApi(e, "registrando recarga para UID " + codigoUid);
        }
    }

    /**
     * Endpoint POST para registrar un consumo (gasto) con una pulsera NFC.
     * Espera los datos como parámetros de formulario. Requiere autenticación
     * con rol CAJERO, ADMIN o PROMOTOR.
     *
     * @param codigoUid UID de la pulsera que realiza el consumo.
     * @param monto Monto a consumir (debe ser positivo).
     * @param descripcion Descripción del consumo (obligatorio).
     * @param idFestival ID del festival donde ocurre el consumo (obligatorio).
     * @param idPuntoVenta ID opcional del punto de venta.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con el DTO de la pulsera actualizado en el
     * cuerpo.</li>
     * <li><b>400 Bad Request:</b> si faltan datos, son inválidos (ej: monto no
     * positivo, saldo insuficiente), o la pulsera no está activa.</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si el rol no es permitido.</li>
     * <li><b>404 Not Found:</b> si la pulsera o el festival no existen.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @POST
    @Path("/pulseras/{codigoUid}/consumir")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Espera datos de formulario
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarConsumo(
            @PathParam("codigoUid") String codigoUid,
            @FormParam("monto") BigDecimal monto,
            @FormParam("descripcion") String descripcion,
            @FormParam("idFestival") Integer idFestival, // Necesario para asociar el consumo
            @FormParam("idPuntoVenta") Integer idPuntoVenta) { // Opcional

        log.info("POST /pos/pulseras/{}/consumir recibido - Monto: {}, Desc: {}, FestivalID: {}", codigoUid, monto, descripcion, idFestival);
        Integer idActor; // El actor (cajero/admin/promotor) que registra el consumo
        try {
            idActor = verificarAccesoActor();
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        } catch (Exception e) {
            return manejarErrorApi(e, "verificando acceso");
        }

        if (codigoUid == null || codigoUid.isBlank()) {
            throw new BadRequestException("El código UID de la pulsera es obligatorio.");
        }

        try {
            // Validar parámetros obligatorios
            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("El monto del consumo debe ser un valor positivo.");
            }
            if (descripcion == null || descripcion.isBlank()) {
                throw new BadRequestException("La descripción del consumo es obligatoria.");
            }
            if (idFestival == null) {
                throw new BadRequestException("El ID del festival es obligatorio para registrar el consumo.");
            }

            // Llamar al servicio
            PulseraNFCDTO pulseraActualizada = pulseraNFCService.registrarConsumo(codigoUid, monto, descripcion, idFestival, idPuntoVenta, idActor);
            log.info("Consumo de {} registrado exitosamente para pulsera UID {}. Nuevo saldo: {}", monto, codigoUid, pulseraActualizada.getSaldo());
            return Response.ok(pulseraActualizada).build(); // 200 OK con DTO actualizado

        } catch (Exception e) {
            return manejarErrorApi(e, "registrando consumo para UID " + codigoUid);
        }
    }

    // --- Métodos Auxiliares ---
    /**
     * Verifica si hay un usuario autenticado a través del
     * {@link SecurityContext} (inyectado, probablemente poblado por un filtro
     * JWT) y si tiene uno de los roles permitidos para operar en el POS
     * (CAJERO, ADMIN, PROMOTOR).
     *
     * @return El ID del usuario autenticado si la verificación es exitosa.
     * @throws NotAuthorizedException Si no hay contexto de seguridad o
     * principal válido.
     * @throws ForbiddenException Si el usuario no tiene un rol permitido.
     * @throws InternalServerErrorException Si ocurre un error al parsear el ID
     * del usuario.
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
                || securityContext.isUserInRole(RolUsuario.PROMOTOR.name()); // ¿Permitir a promotor? Revisar reglas de negocio.

        if (!tieneRolPermitido) {
            log.warn("Usuario ID {} intentó acceder a recurso POS sin rol permitido (CAJERO/ADMIN/PROMOTOR).", userId);
            throw new ForbiddenException("Rol no autorizado para esta operación.");
        }

        log.debug("Acceso POS permitido para actor ID: {}", userId);
        return userId;
    }

    /**
     * Mapea excepciones comunes lanzadas por los servicios a respuestas JAX-RS
     * estándar con el código de estado HTTP apropiado y un cuerpo JSON de
     * error.
     *
     * @param e La excepción capturada.
     * @param operacion Descripción de la operación que falló (para logging).
     * @return Una {@link Response} JAX-RS representando el error.
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
            status = Response.Status.BAD_REQUEST; // 400 (o 409 Conflict para algunos casos como PulseraYaAsociada)
            // Podríamos refinar esto, ej: if (e instanceof PulseraYaAsociadaException) status = Response.Status.CONFLICT;
            mensaje = e.getMessage();
            log.warn("Error 400/409 {}: {}", operacion, mensaje);
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
