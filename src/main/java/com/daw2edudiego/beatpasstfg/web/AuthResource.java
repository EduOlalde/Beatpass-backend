package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.CredencialesDTO;
import com.daw2edudiego.beatpasstfg.dto.TokenDTO;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;
import com.daw2edudiego.beatpasstfg.util.JwtUtil;
import com.daw2edudiego.beatpasstfg.util.PasswordUtil;

import jakarta.validation.Valid; // Para validar el DTO de entrada
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Recurso JAX-RS (@Path) dedicado a la autenticación de usuarios. Proporciona
 * el endpoint de login para la API.
 *
 * @see CredencialesDTO
 * @see TokenDTO
 * @see UsuarioService
 * @see JwtUtil
 * @see PasswordUtil
 * @author Eduardo Olalde
 */
@Path("/auth") // Ruta base para los endpoints de autenticación
@Produces(MediaType.APPLICATION_JSON) // Por defecto, produce respuestas JSON
@Consumes(MediaType.APPLICATION_JSON) // Por defecto, consume JSON
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger(AuthResource.class);

    // Inyección manual de dependencias (en un entorno CDI/Spring se usaría @Inject/@Autowired)
    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    /**
     * Constructor que inicializa los servicios necesarios.
     */
    public AuthResource() {
        this.usuarioService = new UsuarioServiceImpl();
        this.jwtUtil = new JwtUtil();
    }

    /**
     * Endpoint para autenticar un usuario mediante email y contraseña. Si la
     * autenticación es exitosa y la cuenta está activa, genera y devuelve un
     * JSON Web Token (JWT).
     *
     * @param credenciales DTO que contiene el email y la contraseña del
     * usuario. Se valida usando Bean Validation (@Valid).
     * @return Una respuesta JAX-RS:
     * <ul>
     * <li><b>200 OK:</b> con un cuerpo JSON {@link TokenDTO} si la
     * autenticación es exitosa.</li>
     * <li><b>400 Bad Request:</b> si faltan credenciales o son inválidas.</li>
     * <li><b>401 Unauthorized:</b> si el email no existe, la contraseña es
     * incorrecta o la cuenta está inactiva.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error inesperado
     * durante el proceso.</li>
     * </ul>
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON) // Asegura que consume JSON
    @Produces(MediaType.APPLICATION_JSON) // Asegura que produce JSON
    public Response login(@Valid CredencialesDTO credenciales) {
        log.info("Intento de login para email: {}", credenciales != null ? credenciales.getEmail() : "null");

        // Validación básica de entrada (complementaria a @Valid)
        if (credenciales == null || credenciales.getEmail() == null || credenciales.getEmail().isBlank()
                || credenciales.getPassword() == null || credenciales.getPassword().isEmpty()) {
            log.warn("Login fallido: Credenciales incompletas o inválidas.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Email y contraseña son obligatorios.\"}")
                    .build();
        }

        try {
            // 1. Buscar la entidad completa del usuario por email (necesaria para password y estado)
            Optional<Usuario> usuarioCompletoOpt = usuarioService.obtenerEntidadUsuarioPorEmailParaAuth(credenciales.getEmail());

            if (usuarioCompletoOpt.isPresent()) {
                Usuario usuario = usuarioCompletoOpt.get();

                // 2. Verificar si la cuenta está activa
                if (!Boolean.TRUE.equals(usuario.getEstado())) {
                    log.warn("Login fallido: Cuenta inactiva para email {}", credenciales.getEmail());
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\": \"Cuenta inactiva.\"}")
                            .build();
                }

                // 3. Verificar contraseña usando PasswordUtil
                if (PasswordUtil.checkPassword(credenciales.getPassword(), usuario.getPassword())) {
                    // ¡Autenticación exitosa!
                    log.info("Autenticación exitosa para email: {}", credenciales.getEmail());

                    // 4. Generar Token JWT incluyendo ID de usuario y Rol
                    String token = jwtUtil.generarToken(
                            usuario.getIdUsuario().toString(), // ID como String
                            usuario.getRol().name() // Rol como String (ej: "ADMIN")
                    );

                    // 5. Devolver respuesta OK con el token en un DTO
                    return Response.ok(new TokenDTO(token)).build();

                } else {
                    // Contraseña incorrecta
                    log.warn("Login fallido: Contraseña incorrecta para email {}", credenciales.getEmail());
                    // Devolver error genérico para no dar pistas sobre si existe el email
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\": \"Email o contraseña incorrectos.\"}")
                            .build();
                }
            } else {
                // Usuario no encontrado por email
                log.warn("Login fallido: Usuario no encontrado con email {}", credenciales.getEmail());
                // Devolver error genérico
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Email o contraseña incorrectos.\"}")
                        .build();
            }

        } catch (Exception e) {
            // Capturar cualquier error inesperado durante el proceso
            log.error("Error interno durante el login para email {}: {}", credenciales.getEmail(), e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error interno al procesar el login.\"}")
                    .build();
        }
    }

    // Aquí podrían añadirse otros endpoints relacionados con autenticación si fueran necesarios,
    // como /refresh-token, /validate-token, /request-password-reset, etc.
}
