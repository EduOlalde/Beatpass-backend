package com.beatpass.web;

import com.beatpass.dto.*;
import com.beatpass.model.RolUsuario;
import com.beatpass.service.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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
 * Recurso JAX-RS para el panel de Promotores, exponiendo una API RESTful.
 * <p>
 * Proporciona endpoints para que los usuarios con rol
 * {@link RolUsuario#PROMOTOR} gestionen sus propios recursos, como festivales,
 * tipos de entrada, etc. La autenticación y autorización se basa en JWT y
 * {@link SecurityContext}.
 * </p>
 *
 * @author Eduardo Olalde
 */
@Path("/promotor")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("PROMOTOR")
public class PromotorResource {

    private static final Logger log = LoggerFactory.getLogger(PromotorResource.class);

    private final FestivalService festivalService;
    private final UsuarioService usuarioService;
    private final TipoEntradaService tipoEntradaService;
    private final EntradaService entradaService;
    private final AsistenteService asistenteService;
    private final PulseraNFCService pulseraNFCService;
    private final CompraService compraService;

    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    @Inject
    public PromotorResource(FestivalService festivalService, UsuarioService usuarioService, TipoEntradaService tipoEntradaService, EntradaService entradaService, AsistenteService asistenteService, PulseraNFCService pulseraNFCService, CompraService compraService) {
        this.festivalService = festivalService;
        this.usuarioService = usuarioService;
        this.tipoEntradaService = tipoEntradaService;
        this.entradaService = entradaService;
        this.asistenteService = asistenteService;
        this.pulseraNFCService = pulseraNFCService;
        this.compraService = compraService;
    }

    @GET
    @Path("/festivales")
    public Response listarFestivales() {
        log.debug("GET /promotor/festivales (listar) recibido");
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        log.debug("Listando festivales para Promotor ID: {}", idPromotor);
        List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);

        return Response.ok(listaFestivales).build();
    }

    @POST
    @Path("/festivales")
    public Response crearFestival(@Valid FestivalDTO festivalDTO) {
        log.info("POST /promotor/festivales (crear) recibido");
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        FestivalDTO festivalCreado = festivalService.crearFestival(festivalDTO, idPromotor);
        URI location = uriInfo.getAbsolutePathBuilder().path(festivalCreado.getIdFestival().toString()).build();
        return Response.created(location).entity(festivalCreado).build();
    }

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

    @GET
    @Path("/festivales/{id}")
    @RolesAllowed({"ADMIN", "PROMOTOR"})
    public Response obtenerDetallesFestival(@PathParam("id") Integer idFestivalParam) {
        log.debug("GET /promotor/festivales/{} (Detalles JSON) recibido", idFestivalParam);
        Integer idUsuarioAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (idFestivalParam == null || idFestivalParam <= 0) {
            throw new BadRequestException("ID de festival no válido.");
        }

        FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestivalParam, idUsuarioAutenticado)
                .orElseThrow(() -> new NotFoundException("Festival no encontrado o sin permisos de acceso."));

        return Response.ok(festival).build();
    }

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
}
