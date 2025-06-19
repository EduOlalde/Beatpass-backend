package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.*;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.*;
import jakarta.validation.Valid;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recurso JAX-RS para el panel de Administración (/api/admin). Gestiona
 * Usuarios, Festivales, Asistentes y Pulseras. Requiere rol ADMIN en JWT (via
 * SecurityContext). Ahora devuelve JSON.
 */
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    private final UsuarioService usuarioService;
    private final FestivalService festivalService;
    private final AsistenteService asistenteService;
    private final PulseraNFCService pulseraNFCService;
    private final CompradorService compradorService;

    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    public AdminResource() {
        this.usuarioService = new UsuarioServiceImpl();
        this.festivalService = new FestivalServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.pulseraNFCService = new PulseraNFCServiceImpl();
        this.compradorService = new CompradorServiceImpl();
    }

    // --- Gestión de Usuarios ---
    @GET
    @Path("/admins")
    public Response listarAdmins() {
        return listarUsuariosPorRol(RolUsuario.ADMIN);
    }

    @GET
    @Path("/promotores")
    public Response listarPromotores() {
        return listarUsuariosPorRol(RolUsuario.PROMOTOR);
    }

    @GET
    @Path("/cajeros")
    public Response listarCajeros() {
        return listarUsuariosPorRol(RolUsuario.CAJERO);
    }

    private Response listarUsuariosPorRol(RolUsuario rol) {
        log.debug("GET /admin/usuarios/listar para rol: {}", rol);
        Integer idAdmin = verificarAccesoAdmin();
        List<UsuarioDTO> listaUsuarios = usuarioService.obtenerUsuariosPorRol(rol);
        return Response.ok(listaUsuarios).build();
    }

    /**
     * Endpoint GET para obtener los datos de un usuario por ID (para edición o
     * detalle).
     *
     * @param idUsuario ID del usuario.
     * @return 200 OK con UsuarioDTO.
     */
    @GET
    @Path("/usuarios/{idUsuario}")
    public Response obtenerUsuarioPorId(@PathParam("idUsuario") Integer idUsuario) {
        log.debug("GET /admin/usuarios/{}", idUsuario);
        Integer idAdmin = verificarAccesoAdmin();
        if (idUsuario == null) {
            throw new BadRequestException("ID Usuario no válido.");
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorId(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + idUsuario));
        return Response.ok(usuario).build();
    }

    /**
     * Endpoint GET para listar clientes (compradores o asistentes).
     *
     * @param tab Pestaña activa ('compradores' o 'asistentes').
     * @param searchTerm Término de búsqueda.
     * @return 200 OK con un Map que contiene las listas de
     * compradores/asistentes.
     */
    @GET
    @Path("/clientes")
    public Response listarClientes(@QueryParam("tab") String tab, @QueryParam("buscar") String searchTerm) {
        verificarAccesoAdmin();
        String activeTab = "compradores".equalsIgnoreCase(tab) ? "compradores" : "asistentes";

        Map<String, List<?>> data = new HashMap<>();

        if ("compradores".equals(activeTab)) {
            List<CompradorDTO> compradores = compradorService.buscarCompradores(searchTerm);
            data.put("compradores", compradores);
        } else {
            List<AsistenteDTO> asistentes = asistenteService.obtenerTodosLosAsistentesConFiltro(searchTerm);
            data.put("asistentes", asistentes);
        }
        return Response.ok(data).build();
    }

    /**
     * Endpoint POST para crear un nuevo usuario.
     *
     * @param usuarioCreacionDTO DTO con los datos del nuevo usuario.
     * @return 201 Created con el UsuarioDTO creado.
     */
    @POST
    @Path("/usuarios")
    public Response crearUsuario(@Valid UsuarioCreacionDTO usuarioCreacionDTO) {
        verificarAccesoAdmin();
        log.info("POST /admin/usuarios (CREACIÓN Admin) para email: {}", usuarioCreacionDTO.getEmail());

        UsuarioDTO creado = usuarioService.crearUsuario(usuarioCreacionDTO);
        URI location = uriInfo.getAbsolutePathBuilder().path(creado.getIdUsuario().toString()).build();
        return Response.created(location).entity(creado).build();
    }

    /**
     * Endpoint PUT para actualizar el nombre de un usuario existente.
     *
     * @param idUsuario ID del usuario a actualizar.
     * @param updateRequest DTO con el nuevo nombre (y quizás otros campos si se
     * expande).
     * @return 200 OK con el UsuarioDTO actualizado.
     */
    @PUT
    @Path("/usuarios/{idUsuario}")
    public Response actualizarNombreUsuario(
            @PathParam("idUsuario") Integer idUsuario,
            @Valid UsuarioUpdateDTO updateRequest) {

        verificarAccesoAdmin();
        if (idUsuario == null) {
            throw new BadRequestException("ID Usuario no válido.");
        }
        if (updateRequest.getNombre() == null || updateRequest.getNombre().isBlank()) {
            throw new BadRequestException("El nombre del usuario no puede ser vacío.");
        }

        UsuarioDTO actualizado = usuarioService.actualizarNombreUsuario(idUsuario, updateRequest.getNombre());
        return Response.ok(actualizado).build();
    }

    /**
     * Endpoint PUT para cambiar el estado (activo/inactivo) de un usuario.
     *
     * @param idUsuario ID del usuario.
     * @param estadoUpdate DTO con el nuevo estado.
     * @return 200 OK con el UsuarioDTO actualizado.
     */
    @PUT
    @Path("/usuarios/{idUsuario}/estado")
    public Response cambiarEstadoUsuario(
            @PathParam("idUsuario") Integer idUsuario,
            EstadoUpdateDTO estadoUpdate) {

        log.info("PUT /admin/usuarios/{}/estado a {}", idUsuario, estadoUpdate.getNuevoEstado());
        verificarAccesoAdmin();
        if (idUsuario == null || estadoUpdate.getNuevoEstado() == null || estadoUpdate.getNuevoEstado().isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos (idUsuario, nuevoEstado).");
        }

        boolean nuevoEstadoBoolean;
        try {
            nuevoEstadoBoolean = Boolean.parseBoolean(estadoUpdate.getNuevoEstado());
        } catch (Exception e) {
            throw new BadRequestException("Valor de 'nuevoEstado' inválido. Debe ser 'true' o 'false'.", e);
        }

        UsuarioDTO actualizado = usuarioService.actualizarEstadoUsuario(idUsuario, nuevoEstadoBoolean); // Pasa el boolean
        return Response.ok(actualizado).build();
    }

    /**
     * Endpoint DELETE para eliminar un usuario.
     *
     * @param idUsuario ID del usuario a eliminar.
     * @return 204 No Content.
     */
    @DELETE
    @Path("/usuarios/{idUsuario}")
    public Response eliminarUsuario(@PathParam("idUsuario") Integer idUsuario) {
        verificarAccesoAdmin();
        if (idUsuario == null) {
            throw new BadRequestException("ID Usuario no válido.");
        }
        Integer idAdminAutenticado = obtenerIdUsuarioAutenticado();
        if (idUsuario.equals(idAdminAutenticado)) {
            throw new BadRequestException("Un administrador no puede eliminarse a sí mismo.");
        }

        usuarioService.eliminarUsuario(idUsuario);
        return Response.noContent().build();
    }

    // --- Gestión de Festivales ---
    /**
     * Endpoint POST para crear un festival (desde admin, asignando promotor).
     *
     * @param festivalCreacionRequest DTO con los datos del festival y el ID del
     * promotor.
     * @return 201 Created con el FestivalDTO creado.
     */
    @POST
    @Path("/festivales")
    public Response crearFestivalAdmin(
            @Valid AdminFestivalCreacionDTO festivalCreacionRequest) {

        verificarAccesoAdmin();
        log.info("POST /admin/festivales (CREACIÓN Admin) para promotor ID: {}", festivalCreacionRequest.getIdPromotorSeleccionado());

        FestivalDTO festivalDTO = new FestivalDTO();
        festivalDTO.setNombre(festivalCreacionRequest.getNombre());
        festivalDTO.setDescripcion(festivalCreacionRequest.getDescripcion());
        festivalDTO.setFechaInicio(festivalCreacionRequest.getFechaInicio());
        festivalDTO.setFechaFin(festivalCreacionRequest.getFechaFin());
        festivalDTO.setUbicacion(festivalCreacionRequest.getUbicacion());
        festivalDTO.setAforo(festivalCreacionRequest.getAforo());
        festivalDTO.setImagenUrl(festivalCreacionRequest.getImagenUrl());

        FestivalDTO creado = festivalService.crearFestival(festivalDTO, festivalCreacionRequest.getIdPromotorSeleccionado());
        URI location = uriInfo.getAbsolutePathBuilder().path(creado.getIdFestival().toString()).build();
        return Response.created(location).entity(creado).build();
    }

    @GET
    @Path("/festivales")
    public Response listarTodosFestivales(@QueryParam("estado") String estadoFilter) {
        log.debug("GET /admin/festivales. Filtro estado: '{}'", estadoFilter);
        verificarAccesoAdmin();

        List<FestivalDTO> listaFestivales;
        EstadoFestival estadoEnum = null;

        try {
            if (estadoFilter != null && !estadoFilter.isBlank()) {
                estadoEnum = EstadoFestival.valueOf(estadoFilter.toUpperCase());
            }
            listaFestivales = festivalService.obtenerFestivalesPorEstado(estadoEnum);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado de filtro inválido: '" + estadoFilter + "'.", e);
        } catch (Exception e) {
            log.error("Error obteniendo lista de festivales para admin: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Error crítico al cargar la lista de festivales.", e);
        }

        return Response.ok(listaFestivales).build();
    }

    /**
     * Endpoint PUT para confirmar un festival (cambiar a PUBLICADO).
     *
     * @param idFestival ID del festival.
     * @return 200 OK con el FestivalDTO actualizado.
     */
    @PUT
    @Path("/festivales/{idFestival}/confirmar")
    public Response confirmarFestival(@PathParam("idFestival") Integer idFestival) {
        log.info("PUT /admin/festivales/{}/confirmar", idFestival);
        Integer idAdmin = verificarAccesoAdmin();
        if (idFestival == null) {
            throw new BadRequestException("Falta idFestival.");
        }

        FestivalDTO confirmado = festivalService.cambiarEstadoFestival(idFestival, EstadoFestival.PUBLICADO, idAdmin);
        return Response.ok(confirmado).build();
    }

    /**
     * Endpoint PUT para cambiar el estado de un festival (CANCELADO,
     * FINALIZADO).
     *
     * @param idFestival ID del festival.
     * @param estadoUpdate DTO con el nuevo estado.
     * @return 200 OK con el FestivalDTO actualizado.
     */
    @PUT
    @Path("/festivales/{idFestival}/estado")
    public Response cambiarEstadoFestivalAdmin(
            @PathParam("idFestival") Integer idFestival,
            EstadoUpdateDTO estadoUpdate) {

        log.info("PUT /admin/festivales/{}/estado a {}", idFestival, estadoUpdate.getNuevoEstado());
        Integer idAdmin = verificarAccesoAdmin();
        if (idFestival == null || estadoUpdate.getNuevoEstado() == null || estadoUpdate.getNuevoEstado().isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos (idFestival, nuevoEstado).");
        }

        EstadoFestival nuevoEstado;
        try {
            nuevoEstado = EstadoFestival.valueOf(estadoUpdate.getNuevoEstado().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Valor de 'nuevoEstado' inválido. Posibles valores: BORRADOR, PUBLICADO, CANCELADO, FINALIZADO.", e);
        }

        FestivalDTO actualizado = festivalService.cambiarEstadoFestival(idFestival, nuevoEstado, idAdmin);
        return Response.ok(actualizado).build();
    }

    // --- Gestión de Asistentes ---
    @GET
    @Path("/asistentes")
    public Response listarAsistentes(@QueryParam("buscar") String searchTerm) {
        log.debug("GET /admin/asistentes. Término búsqueda: '{}'", searchTerm);
        verificarAccesoAdmin();
        List<AsistenteDTO> listaAsistentes;
        try {
            listaAsistentes = asistenteService.buscarAsistentes(searchTerm);
        } catch (Exception e) {
            log.error("Error obteniendo lista de asistentes: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar la lista de asistentes.", e);
        }
        return Response.ok(listaAsistentes).build();
    }

    @GET
    @Path("/asistentes/{idAsistente}")
    public Response verDetalleAsistente(@PathParam("idAsistente") Integer idAsistente) {
        log.debug("GET /admin/asistentes/{}", idAsistente);
        verificarAccesoAdmin();
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no proporcionado.");
        }

        AsistenteDTO asistente = asistenteService.obtenerAsistentePorId(idAsistente)
                .orElseThrow(() -> new NotFoundException("Asistente no encontrado con ID: " + idAsistente));
        return Response.ok(asistente).build();
    }

    @PUT
    @Path("/asistentes/{idAsistente}")
    public Response actualizarAsistente(
            @PathParam("idAsistente") Integer idAsistente,
            @Valid AsistenteDTO asistenteDTO) {

        log.info("PUT /admin/asistentes/{}", idAsistente);
        verificarAccesoAdmin();
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no válido.");
        }

        AsistenteDTO actualizado = asistenteService.actualizarAsistente(idAsistente, asistenteDTO);
        return Response.ok(actualizado).build();
    }

    // --- Gestión de Pulseras NFC ---
    @GET
    @Path("/festivales/{idFestival}/pulseras-nfc")
    public Response listarPulserasPorFestivalAdmin(@PathParam("idFestival") Integer idFestival) {
        log.debug("GET /admin/festivales/{}/pulseras-nfc", idFestival);
        Integer idAdmin = verificarAccesoAdmin();
        if (idFestival == null) {
            throw new BadRequestException("ID festival no válido.");
        }

        List<PulseraNFCDTO> listaPulseras = pulseraNFCService.obtenerPulserasPorFestival(idFestival, idAdmin);
        return Response.ok(listaPulseras).build();
    }

    // --- Métodos Auxiliares ---
    private Integer verificarAccesoAdmin() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw new NotAuthorizedException("No hay sesión activa.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        String userIdStr = securityContext.getUserPrincipal().getName();
        Integer userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            log.error("No se pudo parsear userId '{}' desde Principal.", userIdStr, e);
            throw new InternalServerErrorException("Error interno de autenticación al parsear ID de usuario.");
        }

        if (!securityContext.isUserInRole(RolUsuario.ADMIN.name())) {
            log.warn("Usuario ID {} con rol {} intentó acceder a recurso Admin.", userId, "DESCONOCIDO");
            throw new ForbiddenException("Acceso denegado. Se requiere rol ADMIN.");
        }
        log.debug("Acceso permitido para admin ID: {}", userId);
        return userId;
    }

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
}
