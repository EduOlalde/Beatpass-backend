/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import java.util.Optional;

/**
 * Recurso JAX-RS para gestionar Usuarios (Administradores y Promotores).
 * NOTA: La mayoría de estas operaciones deberían estar restringidas a Administradores.
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
    private SecurityContext securityContext; // Para verificar roles

    public UsuarioResource() {
        this.usuarioService = new UsuarioServiceImpl();
    }

    /**
     * Endpoint para crear un nuevo usuario (Admin o Promotor).
     * POST /api/usuarios
     * Requiere autenticación como Administrador.
     */
    @POST
    public Response crearUsuario(@Valid UsuarioCreacionDTO usuarioCreacionDTO) {
        log.info("POST /usuarios recibido para email: {}", usuarioCreacionDTO != null ? usuarioCreacionDTO.getEmail() : "null");
        // --- Comprobación de Rol (Placeholder) ---
        if (!esRol(RolUsuario.ADMIN)) {
            log.warn("Acceso no autorizado a POST /usuarios por usuario no ADMIN.");
            return Response.status(Response.Status.FORBIDDEN).entity("Acceso denegado. Se requiere rol de Administrador.").build();
        }
        // --- Fin Placeholder ---

        try {
            // Validaciones adicionales si @Valid no es suficiente
             if (usuarioCreacionDTO == null || usuarioCreacionDTO.getEmail() == null // ... etc ...
                || usuarioCreacionDTO.getPassword() == null || usuarioCreacionDTO.getPassword().length() < 8) { // Ejemplo: longitud mínima
                 log.warn("Datos inválidos para creación de usuario.");
                 return Response.status(Response.Status.BAD_REQUEST).entity("Datos de usuario inválidos o incompletos (contraseña mín. 8 caracteres).").build();
            }

            UsuarioDTO usuarioCreado = usuarioService.crearUsuario(usuarioCreacionDTO);
            URI location = uriInfo.getAbsolutePathBuilder().path(usuarioCreado.getIdUsuario().toString()).build();
            log.info("Usuario creado con ID: {}, Location: {}", usuarioCreado.getIdUsuario(), location);
            // Devolver el UsuarioDTO (sin contraseña)
            return Response.created(location).entity(usuarioCreado).build();

        } catch (EmailExistenteException e) {
            log.warn("Intento de crear usuario con email existente: {}", usuarioCreacionDTO.getEmail());
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build(); // 409 Conflict
        } catch (IllegalArgumentException e) {
             log.warn("Error de validación al crear usuario: {}", e.getMessage());
             return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error interno al crear usuario: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al crear el usuario.").build();
        }
    }

    /**
     * Endpoint para obtener un usuario por su ID.
     * GET /api/usuarios/{id}
     * Requiere autenticación (Admin o el propio usuario si es Promotor).
     */
    @GET
    @Path("/{id}")
    public Response obtenerUsuarioPorId(@PathParam("id") Integer id) {
        log.info("GET /usuarios/{} recibido", id);
        Integer idUsuarioAutenticado = obtenerIdUsuarioAutenticado();
        // --- Comprobación de Permiso (Placeholder) ---
        if (idUsuarioAutenticado == null || (!esRol(RolUsuario.ADMIN) && !idUsuarioAutenticado.equals(id))) {
             log.warn("Acceso no autorizado a GET /usuarios/{} por usuario {}", id, idUsuarioAutenticado);
             return Response.status(Response.Status.FORBIDDEN).entity("Acceso denegado.").build();
        }
        // --- Fin Placeholder ---

        Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);
        return usuarioOpt
                .map(dto -> {
                     log.debug("Usuario ID {} encontrado.", id);
                     return Response.ok(dto).build();
                })
                .orElseGet(() -> {
                    log.warn("Usuario ID {} no encontrado.", id);
                    return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado.").build();
                });
    }

    /**
     * Endpoint para obtener usuarios por rol.
     * GET /api/usuarios?rol=ADMIN o ?rol=PROMOTOR
     * Requiere autenticación como Administrador.
     */
    @GET
    public Response obtenerUsuariosPorRol(@QueryParam("rol") String rolStr) {
        log.info("GET /usuarios recibido con rol: {}", rolStr);
         // --- Comprobación de Rol (Placeholder) ---
        if (!esRol(RolUsuario.ADMIN)) {
            log.warn("Acceso no autorizado a GET /usuarios por usuario no ADMIN.");
            return Response.status(Response.Status.FORBIDDEN).entity("Acceso denegado. Se requiere rol de Administrador.").build();
        }
        // --- Fin Placeholder ---

        RolUsuario rol;
        try {
             if (rolStr == null || rolStr.isBlank()) {
                 throw new IllegalArgumentException("El parámetro 'rol' es obligatorio.");
             }
            rol = RolUsuario.valueOf(rolStr.toUpperCase());
        } catch (IllegalArgumentException e) {
             log.warn("Valor inválido para 'rol' en GET /usuarios: {}", rolStr);
            return Response.status(Response.Status.BAD_REQUEST).entity("Valor de 'rol' inválido. Valores posibles: ADMIN, PROMOTOR.").build();
        }

        List<UsuarioDTO> usuarios = usuarioService.obtenerUsuariosPorRol(rol);
        log.info("Devolviendo {} usuarios con rol {}", usuarios.size(), rol);
        return Response.ok(usuarios).build();
    }


    /**
     * Endpoint para actualizar el estado (activar/desactivar) de un usuario.
     * PUT /api/usuarios/{id}/estado?activo=true o ?activo=false
     * Requiere autenticación como Administrador.
     */
    @PUT
    @Path("/{id}/estado")
    public Response actualizarEstadoUsuario(@PathParam("id") Integer id, @QueryParam("activo") Boolean activo) {
         log.info("PUT /usuarios/{}/estado recibido con activo={}", id, activo);
        // --- Comprobación de Rol (Placeholder) ---
        if (!esRol(RolUsuario.ADMIN)) {
            log.warn("Acceso no autorizado a PUT /usuarios/{}/estado por usuario no ADMIN.", id);
            return Response.status(Response.Status.FORBIDDEN).entity("Acceso denegado. Se requiere rol de Administrador.").build();
        }
        // --- Fin Placeholder ---

        if (activo == null) {
            log.warn("Parámetro 'activo' faltante en PUT /usuarios/{}/estado", id);
            return Response.status(Response.Status.BAD_REQUEST).entity("El parámetro 'activo' (true/false) es obligatorio.").build();
        }

        try {
            UsuarioDTO usuarioActualizado = usuarioService.actualizarEstadoUsuario(id, activo);
            log.info("Estado de usuario ID {} actualizado a {}.", id, activo);
            return Response.ok(usuarioActualizado).build();

        } catch (UsuarioNotFoundException e) {
            log.warn("Usuario ID {} no encontrado para actualizar estado.", id);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error interno al actualizar estado de usuario ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al actualizar el estado del usuario.").build();
        }
    }

    /**
     * Endpoint para eliminar un usuario.
     * DELETE /api/usuarios/{id}
     * Requiere autenticación como Administrador. ¡Usar con precaución!
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarUsuario(@PathParam("id") Integer id) {
        log.info("DELETE /usuarios/{} recibido", id);
         // --- Comprobación de Rol (Placeholder) ---
        if (!esRol(RolUsuario.ADMIN)) {
            log.warn("Acceso no autorizado a DELETE /usuarios/{} por usuario no ADMIN.", id);
            return Response.status(Response.Status.FORBIDDEN).entity("Acceso denegado. Se requiere rol de Administrador.").build();
        }
        // --- Fin Placeholder ---

         // Podrías añadir una comprobación para evitar que un admin se borre a sí mismo

        try {
            usuarioService.eliminarUsuario(id);
            log.info("Usuario ID {} eliminado.", id);
            return Response.noContent().build(); // 204

        } catch (UsuarioNotFoundException e) {
             log.warn("Usuario ID {} no encontrado para eliminar.", id);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (RuntimeException e) { // Captura genérica para errores de FK, etc.
            log.error("Error al eliminar usuario ID {}: {}", id, e.getMessage(), e);
            // Podría ser un 409 Conflict si no se puede borrar por dependencias
            return Response.status(Response.Status.CONFLICT).entity("No se pudo eliminar el usuario: " + e.getMessage()).build();
        } catch (Exception e) {
            log.error("Error interno al eliminar usuario ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al eliminar el usuario.").build();
        }
    }


    // --- Métodos Auxiliares Simulados (Igual que en FestivalResource) ---
    private Integer obtenerIdUsuarioAutenticado() {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            try { return Integer.parseInt(securityContext.getUserPrincipal().getName()); }
            catch (NumberFormatException e) { log.error("Error parseando ID de usuario: {}", securityContext.getUserPrincipal().getName()); return null; }
        }
        log.warn("SecurityContext null. Usando ID fijo 1 para pruebas."); return 1; // SOLO PARA DESARROLLO
    }
    private boolean esRol(RolUsuario rol) {
         if (securityContext != null) {
             // return securityContext.isUserInRole(rol.name());
             log.warn("Comprobación de rol simulada para {}. Asumiendo true.", rol); return true; // SOLO PARA DESARROLLO
         }
         log.warn("SecurityContext null. No se puede verificar rol."); return false;
    }
    
}
