package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.exception.EmailExistenteException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
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
import java.util.Map; // Importar Map
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
        verificarAccesoAdmin(); // Lanza excepción si no es ADMIN

        try {
            // Validación básica (complementaria a @Valid)
            if (usuarioCreacionDTO == null || usuarioCreacionDTO.getEmail() == null || usuarioCreacionDTO.getEmail().isBlank()
                    || usuarioCreacionDTO.getPassword() == null || usuarioCreacionDTO.getPassword().isEmpty()
                    || usuarioCreacionDTO.getNombre() == null || usuarioCreacionDTO.getNombre().isBlank()
                    || usuarioCreacionDTO.getRol() == null) {
                return crearRespuestaError(Response.Status.BAD_REQUEST, "Datos inválidos (nombre, email, password, rol).");
            }
            if (usuarioCreacionDTO.getPassword().length() < 8) {
                return crearRespuestaError(Response.Status.BAD_REQUEST, "Contraseña debe tener al menos 8 caracteres.");
            }

            UsuarioDTO usuarioCreado = usuarioService.crearUsuario(usuarioCreacionDTO);
            URI location = uriInfo.getAbsolutePathBuilder().path(usuarioCreado.getIdUsuario().toString()).build();
            log.info("Usuario creado ID: {}, Location: {}", usuarioCreado.getIdUsuario(), location);
            return Response.created(location).entity(usuarioCreado).build();

        } catch (EmailExistenteException e) {
            return crearRespuestaError(Response.Status.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error interno al crear usuario: {}", e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear usuario.");
        }
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
        try {
            idUsuarioAutenticado = obtenerIdUsuarioAutenticado();
            esAdmin = esRol(RolUsuario.ADMIN);
        } catch (NotAuthorizedException e) {
            return e.getResponse();
        } catch (Exception e) {
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error procesando identidad.");
        }

        if (id == null) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "ID de usuario inválido.");
        }

        // Verificar permiso
        if (idUsuarioAutenticado == null || (!esAdmin && !idUsuarioAutenticado.equals(id))) {
            log.warn("Acceso no autorizado a GET /usuarios/{} por usuario {}", id, idUsuarioAutenticado);
            return crearRespuestaError(Response.Status.FORBIDDEN, "Acceso denegado.");
        }

        try {
            Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);
            return usuarioOpt
                    .map(dto -> Response.ok(dto).build())
                    .orElseGet(() -> crearRespuestaError(Response.Status.NOT_FOUND, "Usuario no encontrado."));
        } catch (Exception e) {
            log.error("Error interno al obtener usuario ID {}: {}", id, e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al obtener usuario.");
        }
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
        verificarAccesoAdmin(); // Verifica ADMIN

        RolUsuario rol;
        try {
            if (rolStr == null || rolStr.isBlank()) {
                throw new IllegalArgumentException("Parámetro 'rol' obligatorio.");
            }
            rol = RolUsuario.valueOf(rolStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Valor 'rol' inválido. Posibles: ADMIN, PROMOTOR, CAJERO.");
        }

        try {
            List<UsuarioDTO> usuarios = usuarioService.obtenerUsuariosPorRol(rol);
            log.info("Devolviendo {} usuarios rol {}", usuarios.size(), rol);
            return Response.ok(usuarios).build();
        } catch (Exception e) {
            log.error("Error interno obteniendo usuarios rol {}: {}", rol, e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al obtener usuarios.");
        }
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
        verificarAccesoAdmin();

        if (id == null) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "ID usuario inválido.");
        }
        if (activo == null) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Parámetro 'activo' (true/false) obligatorio.");
        }

        try {
            UsuarioDTO usuarioActualizado = usuarioService.actualizarEstadoUsuario(id, activo);
            log.info("Estado usuario ID {} actualizado a {}.", id, activo);
            return Response.ok(usuarioActualizado).build();
        } catch (UsuarioNotFoundException e) {
            return crearRespuestaError(Response.Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Error interno al actualizar estado usuario ID {}: {}", id, e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al actualizar estado.");
        }
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
        verificarAccesoAdmin();
        if (id == null) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "ID usuario inválido.");
        }

        // Evitar que un admin se borre a sí mismo
        try {
            Integer idAdminAutenticado = obtenerIdUsuarioAutenticado();
            if (id.equals(idAdminAutenticado)) {
                return crearRespuestaError(Response.Status.BAD_REQUEST, "Un administrador no puede eliminarse a sí mismo.");
            }
        } catch (Exception e) {
            /* Ignorar si falla la verificación, la eliminación fallará después si es el caso */ }

        try {
            usuarioService.eliminarUsuario(id);
            log.info("Usuario ID {} eliminado.", id);
            return Response.noContent().build();
        } catch (UsuarioNotFoundException e) {
            return crearRespuestaError(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) { // Captura errores de FK
            log.error("Error al eliminar usuario ID {}: {}", id, e.getMessage());
            return crearRespuestaError(Response.Status.CONFLICT, "No se pudo eliminar: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error interno al eliminar usuario ID {}: {}", id, e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al eliminar usuario.");
        }
    }

    // --- Métodos Auxiliares ---
    /**
     * Crea una respuesta de error estándar JSON.
     */
    private Response crearRespuestaError(Response.Status status, String mensaje) {
        return Response.status(status).entity(Map.of("error", mensaje)).build();
    }

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
