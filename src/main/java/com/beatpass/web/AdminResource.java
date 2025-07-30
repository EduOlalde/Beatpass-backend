package com.beatpass.web;

import com.beatpass.dto.*;
import com.beatpass.model.EstadoFestival;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recurso JAX-RS para el panel de Administración (/api/admin). Requiere rol
 * ADMIN y gestiona Usuarios, Festivales, Asistentes y Pulseras.
 */
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@RequestScoped
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    @Inject
    private UsuarioService usuarioService;
    @Inject
    private FestivalService festivalService;
    @Inject
    private AsistenteService asistenteService;
    @Inject
    private PulseraNFCService pulseraNFCService;
    @Inject
    private CompradorService compradorService;

    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    public AdminResource() {
    }

    // --- Gestión de Usuarios ---
    /**
     * Obtiene una lista de todos los usuarios con el rol de ADMIN.
     *
     * @return Una respuesta HTTP 200 OK con la lista de administradores.
     */
    @GET
    @Path("/admins")
    public Response listarAdmins() {
        return listarUsuariosPorRol(RolUsuario.ADMIN);
    }

    /**
     * Obtiene una lista de todos los usuarios con el rol de PROMOTOR.
     *
     * @return Una respuesta HTTP 200 OK con la lista de promotores.
     */
    @GET
    @Path("/promotores")
    public Response listarPromotores() {
        return listarUsuariosPorRol(RolUsuario.PROMOTOR);
    }

    /**
     * Obtiene una lista de todos los usuarios con el rol de CAJERO.
     *
     * @return Una respuesta HTTP 200 OK con la lista de cajeros.
     */
    @GET
    @Path("/cajeros")
    public Response listarCajeros() {
        return listarUsuariosPorRol(RolUsuario.CAJERO);
    }

    private Response listarUsuariosPorRol(RolUsuario rol) {
        log.debug("GET /admin/usuarios/listar para rol: {}", rol);
        List<UsuarioDTO> listaUsuarios = usuarioService.obtenerUsuariosPorRol(rol);
        return Response.ok(listaUsuarios).build();
    }

    /**
     * Obtiene un usuario específico por su ID.
     *
     * @param idUsuario El ID del usuario a buscar.
     * @return Una respuesta HTTP 200 OK con los datos del usuario.
     * @throws NotFoundException si el usuario no se encuentra.
     */
    @GET
    @Path("/usuarios/{idUsuario}")
    public Response obtenerUsuarioPorId(@PathParam("idUsuario") Integer idUsuario) {
        log.debug("GET /admin/usuarios/{}", idUsuario);
        if (idUsuario == null) {
            throw new BadRequestException("ID Usuario no válido.");
        }

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorId(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + idUsuario));
        return Response.ok(usuario).build();
    }

    /**
     * Lista clientes (Compradores o Asistentes) con una opción de búsqueda.
     *
     * @param tab Indica si se listan "compradores" o "asistentes".
     * @param searchTerm Término opcional para filtrar por nombre o email.
     * @return Una respuesta HTTP 200 OK con los datos solicitados.
     */
    @GET
    @Path("/clientes")
    public Response listarClientes(@QueryParam("tab") String tab, @QueryParam("buscar") String searchTerm) {
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
     * Crea un nuevo usuario en el sistema (ADMIN, PROMOTOR, o CAJERO).
     *
     * @param usuarioCreacionDTO DTO con los datos del nuevo usuario.
     * @return Una respuesta HTTP 201 Created con la ubicación y datos del nuevo
     * usuario.
     */
    @POST
    @Path("/usuarios")
    public Response crearUsuario(@Valid UsuarioCreacionDTO usuarioCreacionDTO) {
        log.info("POST /admin/usuarios (CREACIÓN Admin) para email: {}", usuarioCreacionDTO.getEmail());

        UsuarioDTO creado = usuarioService.crearUsuario(usuarioCreacionDTO);
        URI location = uriInfo.getAbsolutePathBuilder().path(creado.getIdUsuario().toString()).build();
        return Response.created(location).entity(creado).build();
    }

    /**
     * Actualiza el nombre de un usuario existente.
     *
     * @param idUsuario El ID del usuario a modificar.
     * @param updateRequest DTO con el nuevo nombre.
     * @return Una respuesta HTTP 200 OK con los datos del usuario actualizado.
     */
    @PUT
    @Path("/usuarios/{idUsuario}")
    public Response actualizarNombreUsuario(
            @PathParam("idUsuario") Integer idUsuario,
            @Valid UsuarioUpdateDTO updateRequest) {

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
     * Cambia el estado (activo/inactivo) de un usuario.
     *
     * @param idUsuario El ID del usuario a modificar.
     * @param estadoUpdate DTO que contiene el nuevo estado como 'true' o
     * 'false'.
     * @return Una respuesta HTTP 200 OK con los datos del usuario actualizado.
     */
    @PUT
    @Path("/usuarios/{idUsuario}/estado")
    public Response cambiarEstadoUsuario(
            @PathParam("idUsuario") Integer idUsuario,
            EstadoUpdateDTO estadoUpdate) {

        log.info("PUT /admin/usuarios/{}/estado a {}", idUsuario, estadoUpdate.getNuevoEstado());
        if (idUsuario == null || estadoUpdate.getNuevoEstado() == null || estadoUpdate.getNuevoEstado().isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos (idUsuario, nuevoEstado).");
        }

        boolean nuevoEstadoBoolean;
        try {
            nuevoEstadoBoolean = Boolean.parseBoolean(estadoUpdate.getNuevoEstado());
        } catch (Exception e) {
            throw new BadRequestException("Valor de 'nuevoEstado' inválido. Debe ser 'true' o 'false'.", e);
        }

        UsuarioDTO actualizado = usuarioService.actualizarEstadoUsuario(idUsuario, nuevoEstadoBoolean);
        return Response.ok(actualizado).build();
    }

    /**
     * Elimina un usuario del sistema. Un administrador no puede eliminarse a sí
     * mismo.
     *
     * @param idUsuario El ID del usuario a eliminar.
     * @return Una respuesta HTTP 204 No Content si la eliminación fue exitosa.
     */
    @DELETE
    @Path("/usuarios/{idUsuario}")
    public Response eliminarUsuario(@PathParam("idUsuario") Integer idUsuario) {
        if (idUsuario == null) {
            throw new BadRequestException("ID Usuario no válido.");
        }
        Integer idAdminAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idUsuario.equals(idAdminAutenticado)) {
            throw new BadRequestException("Un administrador no puede eliminarse a sí mismo.");
        }

        usuarioService.eliminarUsuario(idUsuario);
        return Response.noContent().build();
    }

    // --- Gestión de Festivales ---
    /**
     * Crea un nuevo festival y lo asigna a un promotor existente.
     *
     * @param festivalCreacionRequest DTO con los detalles del festival y el ID
     * del promotor.
     * @return Una respuesta HTTP 201 Created con la ubicación y datos del nuevo
     * festival.
     */
    @POST
    @Path("/festivales")
    public Response crearFestivalAdmin(
            @Valid AdminFestivalCreacionDTO festivalCreacionRequest) {

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

    /**
     * Obtiene una lista de todos los festivales, con opción de filtrar por
     * estado.
     *
     * @param estadoFilter Estado opcional para filtrar los resultados
     * (BORRADOR, PUBLICADO, etc.).
     * @return Una respuesta HTTP 200 OK con la lista de festivales.
     */
    @GET
    @Path("/festivales")
    public Response listarTodosFestivales(@QueryParam("estado") String estadoFilter) {
        log.debug("GET /admin/festivales. Filtro estado: '{}'", estadoFilter);

        List<FestivalDTO> listaFestivales;
        EstadoFestival estadoEnum = null;

        try {
            if (estadoFilter != null && !estadoFilter.isBlank()) {
                estadoEnum = EstadoFestival.valueOf(estadoFilter.toUpperCase());
            }
            listaFestivales = festivalService.obtenerFestivalesPorEstado(estadoEnum);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado de filtro inválido: '" + estadoFilter + "'.", e);
        }

        return Response.ok(listaFestivales).build();
    }

    /**
     * Cambia el estado de un festival a PUBLICADO.
     *
     * @param idFestival El ID del festival a publicar.
     * @return Una respuesta HTTP 200 OK con los datos del festival actualizado.
     */
    @PUT
    @Path("/festivales/{idFestival}/confirmar")
    public Response confirmarFestival(@PathParam("idFestival") Integer idFestival) {
        log.info("PUT /admin/festivales/{}/confirmar", idFestival);
        Integer idAdmin = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idFestival == null) {
            throw new BadRequestException("Falta idFestival.");
        }

        FestivalDTO confirmado = festivalService.cambiarEstadoFestival(idFestival, EstadoFestival.PUBLICADO, idAdmin);
        return Response.ok(confirmado).build();
    }

    /**
     * Cambia el estado de un festival a un estado específico (BORRADOR,
     * PUBLICADO, CANCELADO, FINALIZADO).
     *
     * @param idFestival El ID del festival a modificar.
     * @param estadoUpdate DTO que contiene el nuevo estado.
     * @return Una respuesta HTTP 200 OK con los datos del festival actualizado.
     */
    @PUT
    @Path("/festivales/{idFestival}/estado")
    public Response cambiarEstadoFestivalAdmin(
            @PathParam("idFestival") Integer idFestival,
            EstadoUpdateDTO estadoUpdate) {

        log.info("PUT /admin/festivales/{}/estado a {}", idFestival, estadoUpdate.getNuevoEstado());
        Integer idAdmin = Integer.parseInt(securityContext.getUserPrincipal().getName());
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
    /**
     * Obtiene una lista de todos los asistentes, con opción de búsqueda.
     *
     * @param searchTerm Término opcional para filtrar por nombre o email.
     * @return Una respuesta HTTP 200 OK con la lista de asistentes.
     */
    @GET
    @Path("/asistentes")
    public Response listarAsistentes(@QueryParam("buscar") String searchTerm) {
        log.debug("GET /admin/asistentes. Término búsqueda: '{}'", searchTerm);
        List<AsistenteDTO> listaAsistentes = asistenteService.buscarAsistentes(searchTerm);
        return Response.ok(listaAsistentes).build();
    }

    /**
     * Obtiene los detalles de un asistente específico.
     *
     * @param idAsistente El ID del asistente a buscar.
     * @return Una respuesta HTTP 200 OK con los datos del asistente.
     * @throws NotFoundException si el asistente no se encuentra.
     */
    @GET
    @Path("/asistentes/{idAsistente}")
    public Response verDetalleAsistente(@PathParam("idAsistente") Integer idAsistente) {
        log.debug("GET /admin/asistentes/{}", idAsistente);
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no proporcionado.");
        }

        AsistenteDTO asistente = asistenteService.obtenerAsistentePorId(idAsistente)
                .orElseThrow(() -> new NotFoundException("Asistente no encontrado con ID: " + idAsistente));
        return Response.ok(asistente).build();
    }

    /**
     * Actualiza los datos (nombre y teléfono) de un asistente.
     *
     * @param idAsistente El ID del asistente a modificar.
     * @param asistenteUpdateDTO DTO con los nuevos datos.
     * @return Una respuesta HTTP 200 OK con los datos del asistente
     * actualizado.
     */
    @PUT
    @Path("/asistentes/{idAsistente}")
    public Response actualizarAsistente(
            @PathParam("idAsistente") Integer idAsistente,
            @Valid AsistenteUpdateDTO asistenteUpdateDTO) { 

        log.info("PUT /admin/asistentes/{}", idAsistente);
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no válido.");
        }

        AsistenteDTO actualizado = asistenteService.actualizarAsistente(idAsistente, asistenteUpdateDTO);
        return Response.ok(actualizado).build();
    }

    // --- Gestión de Pulseras NFC ---
    /**
     * Obtiene una lista de todas las pulseras NFC asociadas a un festival
     * específico.
     *
     * @param idFestival El ID del festival.
     * @return Una respuesta HTTP 200 OK con la lista de pulseras.
     */
    @GET
    @Path("/festivales/{idFestival}/pulseras-nfc")
    public Response listarPulserasPorFestivalAdmin(@PathParam("idFestival") Integer idFestival) {
        log.debug("GET /admin/festivales/{}/pulseras-nfc", idFestival);
        Integer idAdmin = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (idFestival == null) {
            throw new BadRequestException("ID festival no válido.");
        }

        List<PulseraNFCDTO> listaPulseras = pulseraNFCService.obtenerPulserasPorFestival(idFestival, idAdmin);
        return Response.ok(listaPulseras).build();
    }
}
