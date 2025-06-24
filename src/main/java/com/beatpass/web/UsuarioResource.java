package com.beatpass.web;

import com.beatpass.dto.UsuarioCreacionDTO;
import com.beatpass.dto.UsuarioDTO;
import com.beatpass.model.RolUsuario;
import com.beatpass.service.UsuarioService;

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
import java.util.Optional;

/**
 * Recurso JAX-RS para la gestión de Usuarios (/api/usuarios). La mayoría de las
 * operaciones requieren rol de Administrador.
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

    @Inject
    public UsuarioResource(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response crearUsuario(@Valid UsuarioCreacionDTO usuarioCreacionDTO) {
        log.info("POST /usuarios para email: {}", usuarioCreacionDTO != null ? usuarioCreacionDTO.getEmail() : "null");

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

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "PROMOTOR", "CAJERO"})
    public Response obtenerUsuarioPorId(@PathParam("id") Integer id) {
        log.info("GET /usuarios/{}", id);
        Integer idUsuarioAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());
        boolean esAdmin = securityContext.isUserInRole(RolUsuario.ADMIN.name());

        if (id == null) {
            throw new BadRequestException("ID de usuario inválido.");
        }

        if (!esAdmin && !idUsuarioAutenticado.equals(id)) {
            log.warn("Acceso no autorizado a GET /usuarios/{} por usuario {}", id, idUsuarioAutenticado);
            throw new ForbiddenException("Acceso denegado. Solo puede ver su propio perfil o debe ser Administrador.");
        }

        Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);
        return usuarioOpt
                .map(dto -> Response.ok(dto).build())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado."));
    }

    @GET
    @RolesAllowed("ADMIN")
    public Response obtenerUsuariosPorRol(@QueryParam("rol") String rolStr) {
        log.info("GET /usuarios?rol={}", rolStr);

        RolUsuario rol;
        try {
            if (rolStr == null || rolStr.isBlank()) {
                throw new BadRequestException("Parámetro 'rol' obligatorio.");
            }
            rol = RolUsuario.valueOf(rolStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Valor 'rol' inválido. Posibles: ADMIN, PROMOTOR, CAJERO.");
        }

        List<UsuarioDTO> usuarios = usuarioService.obtenerUsuariosPorRol(rol);
        log.info("Devolviendo {} usuarios rol {}", usuarios.size(), rol);
        return Response.ok(usuarios).build();
    }

    @PUT
    @Path("/{id}/estado")
    @RolesAllowed("ADMIN")
    public Response actualizarEstadoUsuario(@PathParam("id") Integer id, @QueryParam("activo") Boolean activo) {
        log.info("PUT /usuarios/{}/estado?activo={}", id, activo);

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

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response eliminarUsuario(@PathParam("id") Integer id) {
        log.info("DELETE /usuarios/{}", id);
        if (id == null) {
            throw new BadRequestException("ID usuario inválido.");
        }

        Integer idAdminAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (id.equals(idAdminAutenticado)) {
            throw new BadRequestException("Un administrador no puede eliminarse a sí mismo.");
        }

        usuarioService.eliminarUsuario(id);
        log.info("Usuario ID {} eliminado.", id);
        return Response.noContent().build();
    }
}
