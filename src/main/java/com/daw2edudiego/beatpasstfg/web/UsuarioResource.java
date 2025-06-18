package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;

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
import java.util.Optional;

/**
 * Recurso JAX-RS para la gestión de Usuarios (/api/usuarios). La mayoría de
 * operaciones requieren rol ADMIN.
 */
@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    private static final Logger log = LoggerFactory.getLogger(UsuarioResource.class);

    private final UsuarioService usuarioService;

    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    public UsuarioResource() {
        this.usuarioService = new UsuarioServiceImpl();
    }

    /**
     * Crea un nuevo usuario (ADMIN, PROMOTOR o CAJERO). Requiere rol ADMIN.
     *
     * @param usuarioCreacionDTO DTO con datos del nuevo usuario.
     * @return 201 Created con DTO creado, 400/401/403/409 Error, 500 Error.
     */
    @POST
    public Response crearUsuario(@Valid UsuarioCreacionDTO usuarioCreacionDTO) {
        log.info("POST /usuarios para email: {}", usuarioCreacionDTO != null ? usuarioCreacionDTO.getEmail() : "null");
        verificarAccesoAdmin();

        if (usuarioCreacionDTO == null || usuarioCreacionDTO.getEmail() == null || usuarioCreacionDTO.getEmail().isBlank()
                || usuarioCreacionDTO.getPassword() == null || usuarioCreacionDTO.getPassword().isEmpty()
                || usuarioCreacionDTO.getNombre() == null || usuarioCreacionDTO.getNombre().isBlank()
                || usuarioCreacionDTO.getRol() == null) {
            throw new BadRequestException("Datos inválidos (nombre, email, password, rol).");
        }
        if (usuarioCreacionDTO.getPassword().length() < 8) {
            throw new BadRequestException("Contraseña debe tener al menos 8 caracteres.");
        }

        UsuarioDTO usuarioCreado = usuarioService.crearUsuario(usuarioCreacionDTO);
        URI location = uriInfo.getAbsolutePathBuilder().path(usuarioCreado.getIdUsuario().toString()).build();
        log.info("Usuario creado ID: {}, Location: {}", usuarioCreado.getIdUsuario(), location);
        return Response.created(location).entity(usuarioCreado).build();
    }

    /**
     * Obtiene información pública de un usuario por ID. Requiere rol ADMIN o
     * ser el propio usuario.
     *
     * @param id ID del usuario.
     * @return 200 OK con UsuarioDTO, 400/401/403/404 Error, 500 Error.
     */
    @GET
    @Path("/{id}")
    public Response obtenerUsuarioPorId(@PathParam("id") Integer id) {
        log.info("GET /usuarios/{}", id);
        Integer idUsuarioAutenticado;
        boolean esAdmin;
        idUsuarioAutenticado = obtenerIdUsuarioAutenticado();
        esAdmin = esRol(RolUsuario.ADMIN);

        if (id == null) {
            throw new BadRequestException("ID de usuario inválido.");
        }

        if (idUsuarioAutenticado == null || (!esAdmin && !idUsuarioAutenticado.equals(id))) {
            log.warn("Acceso no autorizado a GET /usuarios/{} por usuario {}", id, idUsuarioAutenticado);
            throw new ForbiddenException("Acceso denegado.");
        }

        Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);
        return usuarioOpt
                .map(dto -> Response.ok(dto).build())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado."));
    }

    /**
     * Obtiene lista de usuarios filtrada por rol. Requiere rol ADMIN.
     *
     * @param rolStr Rol a filtrar (QueryParam "rol", ej: "PROMOTOR").
     * @return 200 OK con lista de UsuarioDTO, 400/401/403 Error, 500 Error.
     */
    @GET
    public Response obtenerUsuariosPorRol(@QueryParam("rol") String rolStr) {
        log.info("GET /usuarios?rol={}", rolStr);
        verificarAccesoAdmin(); // Verifica ADMIN - lanza ForbiddenException.

        RolUsuario rol;
        try {
            if (rolStr == null || rolStr.isBlank()) {
                throw new IllegalArgumentException("Parámetro 'rol' obligatorio.");
            }
            rol = RolUsuario.valueOf(rolStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Valor 'rol' inválido. Posibles: ADMIN, PROMOTOR, CAJERO.");
        }

        List<UsuarioDTO> usuarios = usuarioService.obtenerUsuariosPorRol(rol);
        log.info("Devolviendo {} usuarios rol {}", usuarios.size(), rol);
        return Response.ok(usuarios).build();
    }

    /**
     * Actualiza el estado (activo/inactivo) de un usuario. Requiere rol ADMIN.
     *
     * @param id ID del usuario.
     * @param activo Nuevo estado (QueryParam "activo", true/false).
     * @return 200 OK con UsuarioDTO actualizado, 400/401/403/404 Error, 500
     * Error.
     */
    @PUT
    @Path("/{id}/estado")
    public Response actualizarEstadoUsuario(@PathParam("id") Integer id, @QueryParam("activo") Boolean activo) {
        log.info("PUT /usuarios/{}/estado?activo={}", id, activo);
        verificarAccesoAdmin(); // Lanza ForbiddenException

        if (id == null) {
            throw new BadRequestException("ID usuario inválido.");
        }
        if (activo == null) {
            throw new BadRequestException("Parámetro 'activo' (true/false) obligatorio.");
        }

        UsuarioDTO usuarioActualizado = usuarioService.actualizarEstadoUsuario(id, activo);
        log.info("Estado usuario ID {} actualizado a {}.", id, activo);
        return Response.ok(usuarioActualizado).build();
    }

    /**
     * Elimina un usuario. Requiere rol ADMIN. ¡Precaución!
     *
     * @param id ID del usuario a eliminar.
     * @return 204 No Content, 400/401/403/404 Error, 409 Conflict (FKs), 500
     * Error.
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarUsuario(@PathParam("id") Integer id) {
        log.info("DELETE /usuarios/{}", id);
        verificarAccesoAdmin(); // Lanza ForbiddenException
        if (id == null) {
            throw new BadRequestException("ID usuario inválido.");
        }

        // Evitar que un admin se borre a sí mismo
        Integer idAdminAutenticado = obtenerIdUsuarioAutenticado(); // Lanza NotAuthorizedException
        if (id.equals(idAdminAutenticado)) {
            throw new BadRequestException("Un administrador no puede eliminarse a sí mismo.");
        }

        usuarioService.eliminarUsuario(id);
        log.info("Usuario ID {} eliminado.", id);
        return Response.noContent().build();
    }

    // --- Métodos Auxiliares ---
    /**
     * El método `crearRespuestaError` se ha vuelto redundante y se puede
     * eliminar después de implementar el `GenericExceptionMapper`. El
     * `ExceptionMapper` ahora se encarga de traducir las excepciones a
     * respuestas JSON.
     */
    // private Response crearRespuestaError(Response.Status status, String mensaje) { ... }
    /**
     * Obtiene ID del usuario autenticado. Lanza excepción JAX-RS si no
     * autenticado o error.
     */
    private Integer obtenerIdUsuarioAutenticado() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw new NotAuthorizedException("No autenticado.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        try {
            return Integer.parseInt(securityContext.getUserPrincipal().getName());
        } catch (NumberFormatException e) {
            throw new InternalServerErrorException("Error procesando identidad.");
        }
    }

    /**
     * Verifica si el usuario actual tiene el rol especificado.
     */
    private boolean esRol(RolUsuario rol) {
        return securityContext != null && securityContext.isUserInRole(rol.name());
    }

    /**
     * Verifica si el usuario tiene rol ADMIN. Lanza ForbiddenException si no.
     */
    private void verificarAccesoAdmin() {
        if (!esRol(RolUsuario.ADMIN)) {
            log.warn("Acceso denegado. Rol ADMIN requerido.");
            throw new ForbiddenException("Acceso denegado. Se requiere rol de Administrador.");
        }
        log.trace("Acceso ADMIN verificado.");
    }
}
