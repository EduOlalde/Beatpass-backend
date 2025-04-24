package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.exception.EmailExistenteException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;

import jakarta.validation.Valid; // Para validar DTOs si se usa Bean Validation
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext; // Para verificar roles
import jakarta.ws.rs.core.UriInfo; // Para construir URIs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Recurso JAX-RS que expone endpoints RESTful para la gestión de Usuarios del
 * sistema ({@link RolUsuario#ADMIN}, {@link RolUsuario#PROMOTOR},
 * {@link RolUsuario#CAJERO}).
 * <p>
 * La mayoría de las operaciones (creación, listado por rol, cambio de estado,
 * eliminación) están restringidas a usuarios con rol ADMIN. La obtención de un
 * usuario por ID permite al propio usuario (si es PROMOTOR) ver sus datos.
 * </p>
 * <p>
 * La seguridad se basa en la información obtenida del {@link SecurityContext}
 * inyectado. <b>¡ADVERTENCIA! Los métodos de verificación de seguridad en esta
 * clase son actualmente simulados/placeholders y deben ser reemplazados por una
 * implementación real.</b>
 * </p>
 * Produce y consume principalmente {@link MediaType#APPLICATION_JSON}.
 *
 * @see UsuarioService
 * @see UsuarioDTO
 * @see UsuarioCreacionDTO
 * @see SecurityContext
 * @author Eduardo Olalde
 */
@Path("/usuarios") // Ruta base para los endpoints de usuarios
@Produces(MediaType.APPLICATION_JSON) // Por defecto, las respuestas serán JSON
@Consumes(MediaType.APPLICATION_JSON) // Por defecto, las peticiones con cuerpo esperan JSON
public class UsuarioResource {

    private static final Logger log = LoggerFactory.getLogger(UsuarioResource.class);

    // Inyección manual de dependencias (o usar @Inject con CDI/Spring)
    private final UsuarioService usuarioService;

    // Inyección de contexto JAX-RS
    @Context
    private UriInfo uriInfo; // Para construir URIs (ej: Location header)
    @Context
    private SecurityContext securityContext; // Para verificar roles (implementación simulada actualmente)

    /**
     * Constructor que inicializa el servicio de usuarios.
     */
    public UsuarioResource() {
        this.usuarioService = new UsuarioServiceImpl();
    }

    /**
     * Endpoint POST para crear un nuevo usuario (ADMIN, PROMOTOR o CAJERO).
     * Requiere autenticación como Administrador.
     *
     * @param usuarioCreacionDTO DTO con los datos del usuario a crear (nombre,
     * email, password, rol). Se valida con {@code @Valid} si está configurado.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>201 Created:</b> con cabecera 'Location' y DTO del usuario creado
     * en el cuerpo.</li>
     * <li><b>400 Bad Request:</b> si los datos son inválidos (faltan campos,
     * contraseña corta).</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si no tiene rol ADMIN.</li>
     * <li><b>409 Conflict:</b> si el email ya existe.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @POST
    public Response crearUsuario(@Valid UsuarioCreacionDTO usuarioCreacionDTO) {
        log.info("POST /usuarios recibido para email: {}", usuarioCreacionDTO != null ? usuarioCreacionDTO.getEmail() : "null");

        // --- Comprobación de Rol ADMIN (¡Implementación Simulada!) ---
        if (!esRol(RolUsuario.ADMIN)) {
            log.warn("Acceso no autorizado a POST /usuarios por usuario no ADMIN.");
            return Response.status(Response.Status.FORBIDDEN).entity("{\"error\": \"Acceso denegado. Se requiere rol de Administrador.\"}").build();
        }
        // --- Fin Placeholder ---

        try {
            // Validaciones básicas adicionales (complementarias a @Valid)
            if (usuarioCreacionDTO == null || usuarioCreacionDTO.getEmail() == null || usuarioCreacionDTO.getEmail().isBlank()
                    || usuarioCreacionDTO.getPassword() == null || usuarioCreacionDTO.getPassword().isEmpty()
                    || usuarioCreacionDTO.getNombre() == null || usuarioCreacionDTO.getNombre().isBlank()
                    || usuarioCreacionDTO.getRol() == null) {
                log.warn("Datos inválidos para creación de usuario.");
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Datos de usuario inválidos o incompletos (nombre, email, password, rol).\"}").build();
            }
            if (usuarioCreacionDTO.getPassword().length() < 8) {
                log.warn("Contraseña demasiado corta para creación de usuario.");
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"La contraseña debe tener al menos 8 caracteres.\"}").build();
            }

            // Llamar al servicio para crear
            UsuarioDTO usuarioCreado = usuarioService.crearUsuario(usuarioCreacionDTO);

            // Construir URI del nuevo recurso
            URI location = uriInfo.getAbsolutePathBuilder().path(usuarioCreado.getIdUsuario().toString()).build();
            log.info("Usuario creado con ID: {}, Location: {}", usuarioCreado.getIdUsuario(), location);

            // Devolver 201 Created con el DTO (sin contraseña)
            return Response.created(location).entity(usuarioCreado).build();

        } catch (EmailExistenteException e) {
            log.warn("Intento de crear usuario con email existente: {}", usuarioCreacionDTO.getEmail());
            return Response.status(Response.Status.CONFLICT).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 409 Conflict
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear usuario: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 400
        } catch (Exception e) {
            log.error("Error interno al crear usuario: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al crear el usuario.\"}").build(); // 500
        }
    }

    /**
     * Endpoint GET para obtener la información pública (DTO) de un usuario por
     * su ID. Requiere autenticación como Administrador o ser el propio usuario
     * (si es PROMOTOR).
     *
     * @param id El ID del usuario a obtener.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con el DTO del usuario en el cuerpo.</li>
     * <li><b>400 Bad Request:</b> si el ID es inválido.</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si no es ADMIN ni el propio usuario.</li>
     * <li><b>404 Not Found:</b> si el usuario no existe.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @GET
    @Path("/{id}")
    public Response obtenerUsuarioPorId(@PathParam("id") Integer id) {
        log.info("GET /usuarios/{} recibido", id);
        Integer idUsuarioAutenticado;
        boolean esAdmin;
        try {
            idUsuarioAutenticado = obtenerIdUsuarioAutenticado(); // Puede lanzar NotAuthorized o InternalServer
            esAdmin = esRol(RolUsuario.ADMIN); // Usa el helper simulado
        } catch (NotAuthorizedException e) {
            return e.getResponse();
        } catch (Exception e) { // Captura InternalServer del helper
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error procesando identidad.\"}").build();
        }

        // --- Comprobación de Permiso ---
        if (idUsuarioAutenticado == null || (!esAdmin && !idUsuarioAutenticado.equals(id))) {
            log.warn("Acceso no autorizado a GET /usuarios/{} por usuario {}", id, idUsuarioAutenticado);
            return Response.status(Response.Status.FORBIDDEN).entity("{\"error\": \"Acceso denegado.\"}").build();
        }
        // --- Fin Comprobación ---

        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"ID de usuario inválido.\"}").build();
        }

        try {
            Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);
            return usuarioOpt
                    .map(dto -> {
                        log.debug("Usuario ID {} encontrado.", id);
                        return Response.ok(dto).build(); // 200 OK
                    })
                    .orElseGet(() -> {
                        log.warn("Usuario ID {} no encontrado.", id);
                        return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"Usuario no encontrado.\"}").build(); // 404
                    });
        } catch (Exception e) {
            log.error("Error interno al obtener usuario ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al obtener el usuario.\"}").build();
        }
    }

    /**
     * Endpoint GET para obtener una lista de usuarios filtrada por rol.
     * Requiere autenticación como Administrador.
     *
     * @param rolStr String que representa el rol a filtrar (ej: "ADMIN",
     * "PROMOTOR"). Obligatorio.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con una lista (posiblemente vacía) de DTOs de
     * usuarios.</li>
     * <li><b>400 Bad Request:</b> si el parámetro 'rol' falta o es
     * inválido.</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si no tiene rol ADMIN.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @GET
    public Response obtenerUsuariosPorRol(@QueryParam("rol") String rolStr) {
        log.info("GET /usuarios recibido con rol: {}", rolStr);

        // --- Comprobación de Rol ADMIN (¡Implementación Simulada!) ---
        if (!esRol(RolUsuario.ADMIN)) {
            log.warn("Acceso no autorizado a GET /usuarios por usuario no ADMIN.");
            return Response.status(Response.Status.FORBIDDEN).entity("{\"error\": \"Acceso denegado. Se requiere rol de Administrador.\"}").build();
        }
        // --- Fin Placeholder ---

        RolUsuario rol;
        try {
            if (rolStr == null || rolStr.isBlank()) {
                throw new IllegalArgumentException("El parámetro 'rol' es obligatorio.");
            }
            // Convertir string a Enum (ignora mayúsculas/minúsculas)
            rol = RolUsuario.valueOf(rolStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Valor inválido para 'rol' en GET /usuarios: {}", rolStr);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Valor de 'rol' inválido. Valores posibles: ADMIN, PROMOTOR, CAJERO.\"}")
                    .build();
        }

        try {
            List<UsuarioDTO> usuarios = usuarioService.obtenerUsuariosPorRol(rol);
            log.info("Devolviendo {} usuarios con rol {}", usuarios.size(), rol);
            return Response.ok(usuarios).build(); // 200 OK con la lista
        } catch (Exception e) {
            log.error("Error interno obteniendo usuarios por rol {}: {}", rol, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al obtener usuarios.\"}").build();
        }
    }

    /**
     * Endpoint PUT para actualizar el estado (activo/inactivo) de un usuario.
     * Requiere autenticación como Administrador.
     *
     * @param id ID del usuario a modificar.
     * @param activo Booleano indicando el nuevo estado (true=activo,
     * false=inactivo). Obligatorio.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con el DTO del usuario actualizado.</li>
     * <li><b>400 Bad Request:</b> si el ID o el parámetro 'activo' son
     * inválidos/faltan.</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si no tiene rol ADMIN.</li>
     * <li><b>404 Not Found:</b> si el usuario no existe.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @PUT
    @Path("/{id}/estado")
    public Response actualizarEstadoUsuario(@PathParam("id") Integer id, @QueryParam("activo") Boolean activo) {
        log.info("PUT /usuarios/{}/estado recibido con activo={}", id, activo);

        // --- Comprobación de Rol ADMIN (¡Implementación Simulada!) ---
        if (!esRol(RolUsuario.ADMIN)) {
            log.warn("Acceso no autorizado a PUT /usuarios/{}/estado por usuario no ADMIN.", id);
            return Response.status(Response.Status.FORBIDDEN).entity("{\"error\": \"Acceso denegado. Se requiere rol de Administrador.\"}").build();
        }
        // --- Fin Placeholder ---

        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"ID de usuario inválido.\"}").build();
        }
        if (activo == null) {
            log.warn("Parámetro 'activo' faltante en PUT /usuarios/{}/estado", id);
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"El parámetro 'activo' (true/false) es obligatorio.\"}").build();
        }

        try {
            // Llamar al servicio para actualizar estado
            UsuarioDTO usuarioActualizado = usuarioService.actualizarEstadoUsuario(id, activo);
            log.info("Estado de usuario ID {} actualizado a {}.", id, activo);
            return Response.ok(usuarioActualizado).build(); // 200 OK con DTO actualizado

        } catch (UsuarioNotFoundException e) {
            log.warn("Usuario ID {} no encontrado para actualizar estado.", id);
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 404
        } catch (Exception e) {
            log.error("Error interno al actualizar estado de usuario ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al actualizar el estado del usuario.\"}").build(); // 500
        }
    }

    /**
     * Endpoint DELETE para eliminar un usuario. Requiere autenticación como
     * Administrador. ¡Usar con precaución!
     *
     * @param id El ID del usuario a eliminar.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>204 No Content:</b> si la eliminación tiene éxito.</li>
     * <li><b>400 Bad Request:</b> si el ID es inválido.</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si no tiene rol ADMIN.</li>
     * <li><b>404 Not Found:</b> si el usuario no existe.</li>
     * <li><b>409 Conflict:</b> si no se puede eliminar debido a dependencias
     * (restricciones FK).</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarUsuario(@PathParam("id") Integer id) {
        log.info("DELETE /usuarios/{} recibido", id);

        // --- Comprobación de Rol ADMIN (¡Implementación Simulada!) ---
        if (!esRol(RolUsuario.ADMIN)) {
            log.warn("Acceso no autorizado a DELETE /usuarios/{} por usuario no ADMIN.", id);
            return Response.status(Response.Status.FORBIDDEN).entity("{\"error\": \"Acceso denegado. Se requiere rol de Administrador.\"}").build();
        }
        // --- Fin Placeholder ---

        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"ID de usuario inválido.\"}").build();
        }

        // Podrías añadir una comprobación para evitar que un admin se borre a sí mismo
        // Integer idAdminAutenticado = obtenerIdUsuarioAutenticado();
        // if (id.equals(idAdminAutenticado)) { ... return Response.status(Response.Status.BAD_REQUEST)... }
        try {
            // Llamar al servicio para eliminar
            usuarioService.eliminarUsuario(id);
            log.info("Usuario ID {} eliminado.", id);
            return Response.noContent().build(); // 204 No Content

        } catch (UsuarioNotFoundException e) {
            log.warn("Usuario ID {} no encontrado para eliminar.", id);
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 404
        } catch (RuntimeException e) { // Captura genérica para errores de FK (IllegalStateException/PersistenceException del servicio)
            log.error("Error al eliminar usuario ID {}: {}", id, e.getMessage());
            // Devolver 409 Conflict si no se puede borrar por dependencias
            return Response.status(Response.Status.CONFLICT).entity("{\"error\": \"No se pudo eliminar el usuario: " + e.getMessage() + "\"}").build();
        } catch (Exception e) { // Captura genérica para otros errores
            log.error("Error interno al eliminar usuario ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al eliminar el usuario.\"}").build(); // 500
        }
    }

    // --- Métodos Auxiliares de Seguridad (Simulados/Placeholder) ---
    // ESTOS MÉTODOS NECESITAN SER REEMPLAZADOS CON LA LÓGICA REAL
    // BASADA EN EL SecurityContext INYECTADO Y EL MECANISMO DE AUTENTICACIÓN (JWT)
    /**
     * Obtiene el ID del usuario autenticado desde el SecurityContext.
     * <b>¡IMPLEMENTACIÓN SIMULADA!</b> Reemplazar con lógica real.
     *
     * @return El ID del usuario (simulado).
     * @throws NotAuthorizedException Si no hay contexto/principal.
     * @throws InternalServerErrorException Si el ID no es parseable.
     */
    private Integer obtenerIdUsuarioAutenticado() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            log.warn("SecurityContext o UserPrincipal es null. No autenticado.");
            throw new NotAuthorizedException("No autenticado.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        try {
            return Integer.parseInt(securityContext.getUserPrincipal().getName());
        } catch (NumberFormatException e) {
            log.error("Error parseando ID de usuario desde Principal: {}", securityContext.getUserPrincipal().getName(), e);
            throw new InternalServerErrorException("Error procesando identidad de usuario.");
        }
        // log.warn("Usando ID fijo 1 para pruebas."); return 1; // Eliminar en producción
    }

    /**
     * Verifica si el usuario autenticado tiene el rol especificado.
     * <b>¡IMPLEMENTACIÓN SIMULADA!</b> Reemplazar con lógica real.
     *
     * @param rol El rol requerido.
     * @return {@code true} (simulado).
     */
    private boolean esRol(RolUsuario rol) {
        if (securityContext == null) {
            log.warn("SecurityContext null. No se puede verificar rol.");
            return false; // O lanzar NotAuthorizedException si se prefiere
        }
        boolean tieneRol = securityContext.isUserInRole(rol.name());
        log.warn("Comprobación de rol {} simulada. Resultado real: {}", rol, tieneRol);
        return true; // ¡¡¡ SOLO PARA DESARROLLO/PRUEBAS !!! -> Cambiar a 'return tieneRol;' en producción
        // return tieneRol;
    }
}
