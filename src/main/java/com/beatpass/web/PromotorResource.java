package com.beatpass.web;

import com.beatpass.dto.*;
import com.beatpass.model.RolUsuario;
import com.beatpass.service.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
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
 */
@Path("/promotor")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("PROMOTOR")
@RequestScoped
public class PromotorResource {

    private static final Logger log = LoggerFactory.getLogger(PromotorResource.class);

    @Inject
    private FestivalService festivalService;
    @Inject
    private UsuarioService usuarioService;
    @Inject
    private TipoEntradaService tipoEntradaService;
    @Inject
    private EntradaService entradaService;
    @Inject
    private AsistenteService asistenteService;
    @Inject
    private PulseraNFCService pulseraNFCService;
    @Inject
    private CompraService compraService;

    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    public PromotorResource() {
    }

    /**
     * Obtiene la lista de festivales pertenecientes al promotor autenticado.
     *
     * @return Una respuesta HTTP 200 OK con la lista de sus festivales.
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
     * Crea un nuevo festival para el promotor autenticado.
     *
     * @param festivalDTO DTO con los datos del festival a crear.
     * @return Una respuesta HTTP 201 Created con la ubicación y los datos del
     * nuevo festival.
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
     * Actualiza un festival existente perteneciente al promotor autenticado.
     *
     * @param idFestivalParam El ID del festival a actualizar.
     * @param festivalDTO DTO con los nuevos datos del festival.
     * @return Una respuesta HTTP 200 OK con los datos del festival actualizado.
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
     * Obtiene los detalles de un festival específico, verificando que el
     * promotor autenticado tenga permiso para verlo.
     *
     * @param idFestivalParam El ID del festival a obtener.
     * @return Una respuesta HTTP 200 OK con los datos del festival.
     * @throws NotFoundException si el festival no se encuentra o el usuario no
     * tiene permisos.
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

        FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestivalParam, idUsuarioAutenticado)
                .orElseThrow(() -> new NotFoundException("Festival no encontrado o sin permisos de acceso."));

        return Response.ok(festival).build();
    }

    /**
     * Lista todos los tipos de entrada para un festival específico del
     * promotor.
     *
     * @param idFestival El ID del festival.
     * @return Una respuesta HTTP 200 OK con la lista de tipos de entrada.
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
     * Crea un nuevo tipo de entrada para uno de los festivales del promotor.
     *
     * @param idFestival El ID del festival al que se añadirá el tipo de
     * entrada.
     * @param tipoEntradaDTO DTO con los datos del nuevo tipo de entrada.
     * @return Una respuesta HTTP 201 Created con la ubicación y los datos del
     * nuevo tipo de entrada.
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
     * Actualiza un tipo de entrada existente.
     *
     * @param idTipoEntrada El ID del tipo de entrada a modificar.
     * @param tipoEntradaUpdateDTO DTO con los nuevos datos.
     * @return Una respuesta HTTP 200 OK con el tipo de entrada actualizado.
     */
    @PUT
    @Path("/tipos-entrada/{idTipoEntrada}")
    public Response actualizarTipoEntrada(
            @PathParam("idTipoEntrada") Integer idTipoEntrada,
            @Valid TipoEntradaUpdateDTO tipoEntradaUpdateDTO) { 

        log.info("PUT /promotor/tipos-entrada/{} (actualizar) recibido", idTipoEntrada);
        Integer idPromotor = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idTipoEntrada == null || idTipoEntrada <= 0) {
            throw new BadRequestException("ID de tipo de entrada no válido.");
        }

        TipoEntradaDTO actualizada = tipoEntradaService.actualizarTipoEntrada(idTipoEntrada, tipoEntradaUpdateDTO, idPromotor);

        return Response.ok(actualizada).build();
    }

    /**
     * Elimina un tipo de entrada.
     *
     * @param idTipoEntrada El ID del tipo de entrada a eliminar.
     * @return Una respuesta HTTP 204 No Content si la eliminación fue exitosa.
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

    /**
     * Lista todas las entradas generadas para un festival específico del
     * promotor.
     *
     * @param idFestival El ID del festival.
     * @return Una respuesta HTTP 200 OK con la lista de entradas.
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
     * Nomina (asigna) una entrada a un asistente específico.
     *
     * @param idEntrada El ID de la entrada a nominar.
     * @param nominacionRequest DTO con los datos del asistente.
     * @return Una respuesta HTTP 200 OK con la entrada actualizada.
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
     * Cancela una entrada, devolviéndola al stock del tipo de entrada original.
     *
     * @param idEntrada El ID de la entrada a cancelar.
     * @return Una respuesta HTTP 200 OK con un mensaje de confirmación.
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
     * Asocia una pulsera NFC a una entrada específica.
     *
     * @param idEntrada El ID de la entrada a la que se asociará la pulsera.
     * @param asociarPulseraRequest DTO con el UID de la pulsera.
     * @return Una respuesta HTTP 200 OK con los datos de la pulsera asociada.
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

    /**
     * Lista todos los asistentes de un festival específico del promotor.
     *
     * @param idFestival El ID del festival.
     * @return Una respuesta HTTP 200 OK con la lista de asistentes.
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
     * Lista todas las compras de un festival específico del promotor.
     *
     * @param idFestival El ID del festival.
     * @return Una respuesta HTTP 200 OK con la lista de compras.
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

    /**
     * Procesa el cambio de contraseña obligatorio que se solicita al usuario
     * tras su primer inicio de sesión.
     *
     * @param cambioPasswordRequest DTO con la nueva contraseña y su
     * confirmación.
     * @return Una respuesta HTTP 200 OK con un mensaje de confirmación.
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
     * Lista todas las pulseras NFC asociadas a un festival específico.
     *
     * @param idFestival El ID del festival.
     * @return Una respuesta HTTP 200 OK con la lista de pulseras.
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
}
