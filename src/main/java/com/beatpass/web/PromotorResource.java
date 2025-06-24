package com.beatpass.web;

import com.beatpass.dto.AsociarPulseraRequestDTO;
import com.beatpass.dto.CompraDTO;
import com.beatpass.dto.AsistenteDTO;
import com.beatpass.dto.TipoEntradaDTO;
import com.beatpass.dto.FestivalDTO;
import com.beatpass.dto.NominacionRequestDTO;
import com.beatpass.dto.CambioPasswordRequestDTO;
import com.beatpass.dto.EntradaDTO;
import com.beatpass.dto.PulseraNFCDTO;
import com.beatpass.service.UsuarioServiceImpl;
import com.beatpass.service.EntradaServiceImpl;
import com.beatpass.service.AsistenteService;
import com.beatpass.service.FestivalServiceImpl;
import com.beatpass.service.EntradaService;
import com.beatpass.service.TipoEntradaService;
import com.beatpass.service.CompraServiceImpl;
import com.beatpass.service.FestivalService;
import com.beatpass.service.PulseraNFCServiceImpl;
import com.beatpass.service.AsistenteServiceImpl;
import com.beatpass.service.CompraService;
import com.beatpass.service.PulseraNFCService;
import com.beatpass.service.TipoEntradaServiceImpl;
import com.beatpass.service.UsuarioService;
import com.beatpass.model.RolUsuario;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import java.util.List;
import java.util.Map;

/**
 * Recurso JAX-RS que define los endpoints para el panel web del Promotor.
 * Transformado para devolver JSON para una SPA.
 * <p>
 * Proporciona funcionalidades para que los usuarios con rol
 * {@link RolUsuario#PROMOTOR} gestionen sus propios recursos:
 * <ul>
 * <li>Festivales: Listar los propios, ver detalles, crear solicitud, guardar
 * cambios.</li>
 * <li>Tipos de Entrada: Añadir, editar, eliminar (asociados a sus
 * festivales).</li>
 * <li>Entradas: Listar por festival, nominar a asistentes, cancelar, asociar
 * pulseras.</li>
 * <li>Asistentes: Listar los asociados a sus festivales.</li>
 * <li>Pulseras NFC: Listar las asociadas a sus festivales.</li>
 * <li>Cambio de Contraseña: Gestionar el cambio de contraseña obligatorio
 * inicial.</li>
 * </ul>
 * La autenticación y autorización se basa en JWT y `SecurityContext`. Las
 * respuestas son JSON.
 * </p>
 *
 * @see FestivalService
 * @see TipoEntradaService
 * @see EntradaService
 * @see VentaService
 * @see AsistenteService
 * @see PulseraNFCService
 * @see UsuarioService
 * @see CompraService
 * @author Eduardo Olalde
 */
@Path("/promotor")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("PROMOTOR")
public class PromotorResource {

    private static final Logger log = LoggerFactory.getLogger(PromotorResource.class);

    // Services (Manual Dependency Injection)
    private final FestivalService festivalService;
    private final UsuarioService usuarioService;
    private final TipoEntradaService tipoEntradaService;
    private final EntradaService entradaService;
    private final AsistenteService asistenteService;
    private final PulseraNFCService pulseraNFCService;
    private final CompraService compraService;

    // JAX-RS Context Injection
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    /**
     * Constructor que inicializa las instancias de los servicios necesarios.
     */
    public PromotorResource() {
        this.festivalService = new FestivalServiceImpl();
        this.usuarioService = new UsuarioServiceImpl();
        this.tipoEntradaService = new TipoEntradaServiceImpl();
        this.entradaService = new EntradaServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.pulseraNFCService = new PulseraNFCServiceImpl();
        this.compraService = new CompraServiceImpl();
    }

    // --- Endpoints para Gestión de Festivales del Promotor ---
    /**
     * Endpoint GET para listar los festivales propios del promotor autenticado.
     * Ahora devuelve JSON. Requiere rol PROMOTOR.
     *
     * @return 200 OK con lista de FestivalDTO.
     * @throws NotAuthorizedException Si no hay sesión activa.
     * @throws ForbiddenException Si el usuario en sesión no es PROMOTOR.
     */
    @GET
    @Path("/festivales")
    public Response listarFestivales() {
        log.debug("GET /promotor/festivales (listar) recibido");
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        log.debug("Listando festivales para Promotor ID: {}", idPromotor);
        List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);

        return Response.ok(listaFestivales).build();
    }

    /**
     * Endpoint POST para guardar un festival (crear uno nuevo). Recibe un DTO
     * JSON. Si es nuevo, lo crea en estado BORRADOR. Si es existente, actualiza
     * sus datos (excepto el estado y el promotor). Requiere rol PROMOTOR y que
     * el promotor sea dueño del festival si es actualización.
     *
     * @param festivalDTO DTO con los datos del festival a crear o actualizar.
     * @return 201 Created con el FestivalDTO si es creación, 200 OK si es
     * actualización.
     * @throws BadRequestException Si los datos son inválidos.
     * @throws FestivalNotFoundException Si el festival a actualizar no existe.
     * @throws SecurityException Si el promotor no tiene permisos para el
     * festival.
     */
    @POST
    @Path("/festivales")
    public Response crearFestival(@Valid FestivalDTO festivalDTO) {
        log.info("POST /promotor/festivales (crear) recibido");
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        FestivalDTO festivalCreado = festivalService.crearFestival(festivalDTO, idPromotor);
        URI location = uriInfo.getAbsolutePathBuilder().path(festivalCreado.getIdFestival().toString()).build();
        return Response.created(location).entity(festivalCreado).build();
    }

    /**
     * Endpoint PUT para actualizar un festival existente. Recibe un DTO JSON.
     * Requiere rol PROMOTOR y que el promotor sea dueño del festival.
     *
     * @param idFestivalParam ID del festival a actualizar.
     * @param festivalDTO DTO con los datos a actualizar.
     * @return 200 OK con el FestivalDTO actualizado.
     * @throws BadRequestException Si el ID o los datos son inválidos.
     * @throws FestivalNotFoundException Si el festival a actualizar no existe.
     * @throws SecurityException Si el promotor no tiene permisos.
     */
    @PUT
    @Path("/festivales/{id}")
    public Response actualizarFestival(@PathParam("id") Integer idFestivalParam, @Valid FestivalDTO festivalDTO) {
        log.info("PUT /promotor/festivales/{} (actualizar) recibido", idFestivalParam);
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (idFestivalParam == null) {
            throw new BadRequestException("ID de festival no válido.");
        }

        FestivalDTO festivalActualizado = festivalService.actualizarFestival(idFestivalParam, festivalDTO, idPromotor);
        return Response.ok(festivalActualizado).build();
    }

    /**
     * Endpoint GET para mostrar los detalles (JSON) de un festival específico
     * perteneciente al promotor autenticado. Requiere rol PROMOTOR en sesión o
     * ADMIN y, si es PROMOTOR, ser dueño del festival.
     *
     * @param idFestivalParam ID del festival a visualizar, obtenido del path.
     * @return 200 OK con FestivalDTO.
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el festival no se encuentra.
     * @throws ForbiddenException Si el promotor no es dueño del festival o el
     * usuario no es ADMIN/PROMOTOR.
     */
    @GET
    @Path("/festivales/{id}")
    @RolesAllowed({"ADMIN", "PROMOTOR"})
    public Response obtenerDetallesFestival(@PathParam("id") Integer idFestivalParam) {
        log.debug("GET /promotor/festivales/{} (Detalles JSON) recibido", idFestivalParam);
        Integer idUsuarioAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (idFestivalParam == null || idFestivalParam <= 0) {
            throw new BadRequestException("ID de festival no válido.");
        }

        FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestivalParam)
                .filter(f -> securityContext.isUserInRole(RolUsuario.ADMIN.name()) || idUsuarioAutenticado.equals(f.getIdPromotor()))
                .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

        return Response.ok(festival).build();
    }

    /**
     * Endpoint GET para listar los tipos de entrada de un festival específico
     * del promotor autenticado. Permite acceso a PROMOTORES (dueños) y ADMINS.
     *
     * @param idFestival ID del festival.
     * @return 200 OK con lista de TipoEntradaDTO.
     * @throws BadRequestException Si el ID del festival es inválido.
     * @throws SecurityException Si el promotor no tiene permisos.
     */
    @GET
    @Path("/festivales/{idFestival}/tipos-entrada")
    @RolesAllowed({"ADMIN", "PROMOTOR"})
    public Response listarTiposEntrada(@PathParam("idFestival") Integer idFestival) {
        log.debug("GET /promotor/festivales/{}/tipos-entrada (listar) recibido", idFestival);
        Integer idUsuarioAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID festival inválido.");
        }
        List<TipoEntradaDTO> listaTiposEntrada = tipoEntradaService.obtenerTipoEntradasPorFestival(idFestival, idUsuarioAutenticado);
        return Response.ok(listaTiposEntrada).build();
    }

    /**
     * Endpoint POST para añadir un nuevo tipo de entrada a un festival
     * existente del promotor autenticado. Recibe datos en JSON. Requiere rol
     * PROMOTOR.
     *
     * @param idFestival ID del festival al que se añade la entrada, obtenido
     * del path.
     * @param tipoEntradaDTO DTO con los datos del tipo de entrada a crear.
     * @return 201 Created con el TipoEntradaDTO creado.
     * @throws BadRequestException Si el ID del festival o los datos son
     * inválidos.
     */
    @POST
    @Path("/festivales/{idFestival}/tipos-entrada")
    public Response crearTipoEntrada(
            @PathParam("idFestival") Integer idFestival,
            @Valid TipoEntradaDTO tipoEntradaDTO) {

        log.info("POST /promotor/festivales/{}/tipos-entrada (crear) recibido", idFestival);
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID festival inválido.");
        }

        TipoEntradaDTO tipoEntradaCreado = tipoEntradaService.crearTipoEntrada(tipoEntradaDTO, idFestival, idPromotor);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(idFestival))
                .path("tipos-entrada")
                .path(tipoEntradaCreado.getIdTipoEntrada().toString())
                .build();

        return Response.created(location).entity(tipoEntradaCreado).build();
    }

    /**
     * Endpoint PUT para procesar la actualización de un tipo de entrada
     * existente. Recibe datos en JSON. Requiere rol PROMOTOR.
     *
     * @param idTipoEntrada ID del tipo de entrada a actualizar, obtenido del
     * path.
     * @param tipoEntradaDTO DTO con los nuevos datos.
     * @return 200 OK con el TipoEntradaDTO actualizado.
     * @throws BadRequestException Si el ID o los datos son inválidos.
     * @throws TipoEntradaNotFoundException Si el tipo de entrada no existe.
     * @throws SecurityException Si el promotor no tiene permisos.
     */
    @PUT
    @Path("/tipos-entrada/{idTipoEntrada}")
    public Response actualizarTipoEntrada(
            @PathParam("idTipoEntrada") Integer idTipoEntrada,
            @Valid TipoEntradaDTO tipoEntradaDTO) {

        log.info("PUT /promotor/tipos-entrada/{} (actualizar) recibido", idTipoEntrada);
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idTipoEntrada == null || idTipoEntrada <= 0) {
            throw new BadRequestException("ID de tipo de entrada no válido.");
        }

        TipoEntradaDTO actualizada = tipoEntradaService.actualizarTipoEntrada(idTipoEntrada, tipoEntradaDTO, idPromotor);

        return Response.ok(actualizada).build();
    }

    /**
     * Endpoint DELETE para eliminar un tipo de entrada específico. Requiere rol
     * PROMOTOR.
     *
     * @param idTipoEntrada ID del tipo de entrada a eliminar, obtenido del
     * path.
     * @return 204 No Content si la eliminación fue exitosa.
     * @throws BadRequestException Si el ID no es válido.
     * @throws TipoEntradaNotFoundException Si el tipo de entrada no existe.
     * @throws SecurityException Si el promotor no tiene permisos.
     * @throws RuntimeException Si falla por FKs u otro error.
     */
    @DELETE
    @Path("/tipos-entrada/{idTipoEntrada}")
    public Response eliminarTipoEntrada(@PathParam("idTipoEntrada") Integer idTipoEntrada) {
        log.info("DELETE /promotor/tipos-entrada/{} (eliminar) recibido", idTipoEntrada);
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idTipoEntrada == null || idTipoEntrada <= 0) {
            throw new BadRequestException("ID de tipo de entrada no válido.");
        }

        tipoEntradaService.eliminarTipoEntrada(idTipoEntrada, idPromotor);

        return Response.noContent().build();
    }

    // --- Endpoints para Gestión de Entradas ---
    /**
     * Endpoint GET para listar las entradas (vendidas/generadas) de un festival
     * específico perteneciente al promotor autenticado. Ahora devuelve JSON.
     * Permite acceso a PROMOTORES (dueños) y ADMINS.
     *
     * @param idFestival ID del festival cuyas entradas se listarán, obtenido
     * del path.
     * @return 200 OK con lista de EntradaDTO.
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el festival no se encuentra.
     * @throws ForbiddenException Si el promotor no es dueño del festival o el
     * usuario no es ADMIN/PROMOTOR.
     */
    @GET
    @Path("/festivales/{idFestival}/entradas")
    @RolesAllowed({"ADMIN", "PROMOTOR"})
    public Response listarEntradas(@PathParam("idFestival") Integer idFestival) {
        log.debug("GET /promotor/festivales/{}/entradas recibido", idFestival);
        Integer idUsuarioAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID festival inválido.");
        }

        List<EntradaDTO> listaEntradas = entradaService.obtenerEntradasPorFestival(idFestival, idUsuarioAutenticado);

        return Response.ok(listaEntradas).build();
    }

    /**
     * Endpoint POST para nominar una entrada a un asistente. Recibe un DTO
     * JSON. Requiere rol PROMOTOR.
     *
     * @param idEntrada ID de la entrada a nominar, obtenido del path.
     * @param nominacionRequest DTO con email, nombre y teléfono del asistente.
     * @return 200 OK con la EntradaDTO nominada.
     */
    @POST
    @Path("/entradas/{idEntrada}/nominar")
    public Response nominarEntrada(
            @PathParam("idEntrada") Integer idEntrada,
            @Valid NominacionRequestDTO nominacionRequest) {

        log.info("POST /promotor/entradas/{}/nominar recibido para asistente email {}", idEntrada, nominacionRequest.getEmailAsistente());
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (idEntrada == null || idEntrada <= 0) {
            throw new BadRequestException("ID de entrada no válido.");
        }

        EntradaDTO entradaNominadaDTO = entradaService.nominarEntrada(
                idEntrada,
                nominacionRequest.getEmailAsistente(),
                nominacionRequest.getNombreAsistente(),
                nominacionRequest.getTelefonoAsistente(),
                idPromotor
        );

        return Response.ok(entradaNominadaDTO).build();
    }

    /**
     * Endpoint POST para cancelar una entrada. Requiere rol PROMOTOR.
     *
     * @param idEntrada ID de la entrada a cancelar, obtenido del path.
     * @return 200 OK con un mensaje de éxito.
     */
    @POST
    @Path("/entradas/{idEntrada}/cancelar")
    public Response cancelarEntrada(@PathParam("idEntrada") Integer idEntrada) {
        log.info("POST /promotor/entradas/{}/cancelar recibido", idEntrada);
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idEntrada == null || idEntrada <= 0) {
            throw new BadRequestException("ID entrada inválido.");
        }

        entradaService.cancelarEntrada(idEntrada, idPromotor);

        return Response.ok(Map.of("message", "Entrada ID " + idEntrada + " cancelada correctamente.")).build();
    }

    /**
     * Endpoint POST para asociar una pulsera NFC a una entrada. Recibe un DTO
     * JSON. Requiere rol PROMOTOR.
     *
     * @param idEntrada ID de la entrada a la que asociar la pulsera, obtenido
     * del path.
     * @param asociarPulseraRequest DTO con el código UID de la pulsera.
     * @return 200 OK con la PulseraNFCDTO actualizada.
     */
    @POST
    @Path("/entradas/{idEntrada}/asociar-pulsera")
    public Response asociarPulseraPromotor(
            @PathParam("idEntrada") Integer idEntrada,
            @Valid AsociarPulseraRequestDTO asociarPulseraRequest) {

        log.info("POST /promotor/entradas/{}/asociar-pulsera con UID: {}", idEntrada, asociarPulseraRequest.getCodigoUid());
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (idEntrada == null || idEntrada <= 0) {
            throw new BadRequestException("ID de entrada no válido.");
        }
        PulseraNFCDTO pulseraAsociada = pulseraNFCService.asociarPulseraEntrada(asociarPulseraRequest.getCodigoUid(), idEntrada, idPromotor);

        return Response.ok(pulseraAsociada).build();
    }

    // --- Endpoints para Consulta de Asistentes y Pulseras por Festival ---
    /**
     * Endpoint GET para mostrar la lista de asistentes únicos con entradas para
     * un festival específico del promotor autenticado. Ahora devuelve JSON.
     * Permite acceso a PROMOTORES (dueños) y ADMINS.
     *
     * @param idFestival ID del festival cuyos asistentes se listarán, obtenido
     * del path.
     * @return 200 OK con lista de AsistenteDTO.
     */
    @GET
    @Path("/festivales/{idFestival}/asistentes")
    @RolesAllowed({"ADMIN", "PROMOTOR"})
    public Response listarAsistentesPorFestival(@PathParam("idFestival") Integer idFestival) {
        log.debug("GET /promotor/festivales/{}/asistentes recibido", idFestival);
        Integer idUsuarioAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID de festival no válido.");
        }

        List<AsistenteDTO> listaAsistentes = asistenteService.obtenerAsistentesPorFestival(idFestival, idUsuarioAutenticado);

        return Response.ok(listaAsistentes).build();
    }

    /**
     * Endpoint GET para mostrar la lista de compras realizadas para un festival
     * específico del promotor autenticado. Ahora devuelve JSON. Permite acceso
     * a PROMOTORES (dueños) y ADMINS.
     *
     * @param idFestival ID del festival cuyas compras se listarán, obtenido del
     * path.
     * @return 200 OK con lista de CompraDTO.
     */
    @GET
    @Path("/festivales/{idFestival}/compras")
    @RolesAllowed({"ADMIN", "PROMOTOR"})
    public Response listarComprasPorFestival(@PathParam("idFestival") Integer idFestival) {
        log.debug("GET /promotor/festivales/{}/compras recibido", idFestival);
        Integer idUsuarioAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID de festival no válido.");
        }

        List<CompraDTO> listaCompras = compraService.obtenerComprasPorFestival(idFestival, idUsuarioAutenticado);

        return Response.ok(listaCompras).build();
    }

    // --- Endpoints para Cambio de Contraseña Obligatorio ---
    /**
     * Endpoint POST para procesar el cambio de contraseña obligatorio. Ahora
     * recibe JSON.
     *
     * @param cambioPasswordRequest DTO con la nueva contraseña y su
     * confirmación.
     * @return 200 OK con un mensaje de éxito.
     */
    @POST
    @Path("/cambiar-password-obligatorio")
    @RolesAllowed({"ADMIN", "PROMOTOR", "CAJERO"})
    public Response procesarCambioPasswordObligatorio(
            @Valid CambioPasswordRequestDTO cambioPasswordRequest) {

        Integer userId = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (!cambioPasswordRequest.getNewPassword().equals(cambioPasswordRequest.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden.");
        }
        if (cambioPasswordRequest.getNewPassword().length() < 8) {
            throw new BadRequestException("La nueva contraseña debe tener al menos 8 caracteres.");
        }

        usuarioService.cambiarPasswordYMarcarActualizada(userId, cambioPasswordRequest.getNewPassword());

        return Response.ok(Map.of("message", "Contraseña actualizada correctamente.")).build();
    }

    /**
     * Endpoint GET para mostrar la lista de pulseras NFC asociadas a un
     * festival específico del promotor autenticado. Ahora devuelve JSON.
     * Permite acceso a PROMOTORES (dueños) y ADMINS.
     *
     * @param idFestival ID del festival cuyas pulseras se listarán, obtenido
     * del path.
     * @return 200 OK con lista de PulseraNFCDTO.
     */
    @GET
    @Path("/festivales/{idFestival}/pulseras")
    @RolesAllowed({"ADMIN", "PROMOTOR", "CAJERO"})
    public Response listarPulserasPorFestivalPromotor(@PathParam("idFestival") Integer idFestival) {
        log.debug("GET /promotor/festivales/{}/pulseras recibido", idFestival);
        Integer idUsuarioAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID de festival no válido.");
        }

        List<PulseraNFCDTO> listaPulseras = pulseraNFCService.obtenerPulserasPorFestival(idFestival, idUsuarioAutenticado);

        return Response.ok(listaPulseras).build();
    }

    // --- Métodos Auxiliares ---
    /**
     * Obtiene ID de usuario autenticado desde SecurityContext. Lanza
     * excepciones JAX-RS si no autenticado o error.
     */
    private Integer obtenerIdUsuarioAutenticado() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw new NotAuthorizedException("No autenticado.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        try {
            return Integer.parseInt(securityContext.getUserPrincipal().getName());
        } catch (NumberFormatException e) {
            throw new InternalServerErrorException("Error procesando identidad del usuario.");
        }
    }

    private Integer obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario rolRequerido) {
        Integer userId = obtenerIdUsuarioAutenticado();
        if (!securityContext.isUserInRole(rolRequerido.name())) {
            log.warn("Usuario ID {} no tiene el rol requerido {}", userId, rolRequerido);
            throw new ForbiddenException("Acceso denegado. Rol requerido: " + rolRequerido);
        }
        log.trace("Usuario ID {} verificado con rol {}", userId, rolRequerido);
        return userId;
    }
}
