package com.daw2edudiego.beatpasstfg.security;

import com.daw2edudiego.beatpasstfg.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro JAX-RS {@link ContainerRequestFilter} para la autenticación basada en
 * JSON Web Tokens (JWT).
 * <p>
 * Intercepta las peticiones entrantes a la API (rutas bajo `/api/*`, excluyendo
 * las definidas en {@code EXCLUDED_PATHS_PREFIXES}) para validar el token JWT
 * presente en la cabecera {@code Authorization: Bearer <token>}.
 * </p>
 * <p>
 * Si el token es válido:
 * <ul>
 * <li>Extrae el ID de usuario (subject) y el rol (claim personalizado).</li>
 * <li>Establece un {@link UserSecurityContext} personalizado para la petición,
 * haciendo que la identidad y el rol estén disponibles para los endpoints
 * JAX-RS mediante {@code @Context SecurityContext}.</li>
 * </ul>
 * Si el token falta, es inválido, ha expirado o contiene información
 * incompleta, la petición es abortada con una respuesta HTTP 401 Unauthorized.
 * </p>
 * <p>
 * Las peticiones OPTIONS (usadas para preflight CORS) y las rutas
 * explícitamente excluidas (como login o paneles web basados en sesión) no son
 * procesadas por este filtro JWT.
 * </p>
 *
 * @see JwtUtil
 * @see UserSecurityContext
 * @see Provider
 * @see Priority
 * @author Eduardo Olalde
 */
@Provider // Marca esta clase como un proveedor de extensión JAX-RS (descubrible automáticamente)
@Priority(Priorities.AUTHENTICATION) // Define una alta prioridad para que se ejecute antes que otros filtros/endpoints
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    // Instancia de la utilidad JWT para validar tokens
    private final JwtUtil jwtUtil = new JwtUtil();

    /**
     * Lista de prefijos de rutas que deben ser excluidas de la validación JWT.
     * Incluye típicamente endpoints de login públicos y rutas de paneles web
     * que usan otros mecanismos de autenticación (ej: sesión HTTP). TODO:
     * Considerar hacer esta lista configurable externamente.
     */
    private static final List<String> EXCLUDED_PATHS_PREFIXES = Arrays.asList(
            "auth/login", // Endpoint de login API
            "admin", // Prefijo para el panel web de admin (asume gestión por sesión)
            "promotor" // Prefijo para el panel web de promotor (asume gestión por sesión)
    // Añadir aquí otros prefijos si es necesario (ej: "public/", "assets/")
    );

    /**
     * Método principal del filtro que procesa cada petición entrante.
     *
     * @param requestContext El contexto de la petición JAX-RS, permite acceder
     * a cabeceras, URI, etc., y modificar el flujo de la petición.
     * @throws IOException Si ocurre un error de E/S (raro en este contexto).
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        log.trace("AuthenticationFilter procesando petición: {} {}", method, path);

        // 1. Permitir peticiones CORS preflight (OPTIONS) sin autenticación
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("Petición OPTIONS para ruta: {}. Permitida para preflight CORS.", path);
            // Abortar con respuesta OK permite que otros filtros (como CorsFilter) actúen
            requestContext.abortWith(Response.ok().build());
            return;
        }

        // 2. Comprobar si la ruta debe ser excluida de la validación JWT
        if (isPathExcluded(path)) {
            log.debug("Ruta {} excluida del filtro de autenticación JWT.", path);
            return; // Continuar con el siguiente filtro o el endpoint
        }

        // 3. Obtener la cabecera Authorization
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // 4. Validar presencia y formato del Header ("Bearer <token>")
        if (authorizationHeader == null || !authorizationHeader.toLowerCase().startsWith("bearer ")) {
            log.warn("Cabecera Authorization ausente o mal formada para la ruta protegida: {}", path);
            abortUnauthorized(requestContext, "Se requiere cabecera Authorization: Bearer <token>.");
            return; // Abortar petición
        }

        // 5. Extraer la cadena del token
        String token = authorizationHeader.substring("Bearer".length()).trim();
        if (token.isEmpty()) {
            log.warn("Cabecera Authorization presente pero el token está vacío para la ruta: {}", path);
            abortUnauthorized(requestContext, "El token Bearer proporcionado está vacío.");
            return; // Abortar petición
        }

        // 6. Validar el token y extraer claims
        try {
            Claims claims = jwtUtil.validarTokenYObtenerClaims(token); // Valida firma y expiración
            String userId = jwtUtil.obtenerUserIdDeClaims(claims);
            String role = jwtUtil.obtenerRolDeClaims(claims);

            // Verificar que los claims esenciales no sean nulos
            if (userId == null || role == null) {
                log.error("Token válido pero faltan claims esenciales (userId o role). Token sub: {}", claims.getSubject());
                abortUnauthorized(requestContext, "Token inválido: información de usuario incompleta.");
                return;
            }

            log.debug("JWT validado exitosamente para userId: {}, role: {} en ruta: {}", userId, role, path);

            // 7. Establecer el SecurityContext personalizado para la petición actual
            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            UserSecurityContext userSecurityContext = new UserSecurityContext(
                    userId,
                    role,
                    currentSecurityContext.isSecure() // Mantener si la conexión original era HTTPS
            );
            requestContext.setSecurityContext(userSecurityContext);
            log.trace("UserSecurityContext establecido para la petición.");

            // Si todo es correcto, la petición continúa hacia el endpoint JAX-RS
        } catch (ExpiredJwtException eje) {
            log.warn("Validación JWT fallida para ruta {}: Token expirado.", path);
            abortUnauthorized(requestContext, "El token ha expirado.");
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // Capturar errores específicos de formato, firma o soporte JWT
            log.warn("Validación JWT fallida para ruta {}: Token inválido ({}).", path, e.getClass().getSimpleName());
            abortUnauthorized(requestContext, "Token inválido o mal formado.");
        } catch (JwtException e) {
            // Capturar otros errores genéricos de la librería JWT
            log.error("Error inesperado procesando JWT para ruta {}: {}", path, e.getMessage(), e);
            abortUnauthorized(requestContext, "Error procesando token.");
        } catch (Exception e) {
            // Capturar cualquier otro error inesperado dentro del filtro
            log.error("Error inesperado en AuthenticationFilter para ruta {}: {}", path, e.getMessage(), e);
            // Devolver una respuesta de error genérica del servidor 500
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error interno del servidor durante la autenticación.\"}")
                    .type(MediaType.APPLICATION_JSON).build());
        }
    }

    /**
     * Comprueba si la ruta de la petición actual comienza con alguno de los
     * prefijos definidos en {@link #EXCLUDED_PATHS_PREFIXES}.
     *
     * @param path La ruta de la petición (ej: "admin/dashboard",
     * "api/festivales").
     * @return {@code true} si la ruta coincide con algún prefijo excluido,
     * {@code false} en caso contrario.
     */
    private boolean isPathExcluded(String path) {
        if (path == null) {
            return false;
        }
        // Comprueba si la ruta (sin la barra inicial si existe) empieza con algún prefijo excluido
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        return EXCLUDED_PATHS_PREFIXES.stream().anyMatch(prefix -> cleanPath.startsWith(prefix));
    }

    /**
     * Aborta el procesamiento de la petición actual devolviendo una respuesta
     * HTTP 401 Unauthorized. Incluye la cabecera {@code WWW-Authenticate}
     * requerida por el estándar para indicar el esquema de autenticación
     * esperado (Bearer).
     *
     * @param requestContext El contexto de la petición JAX-RS.
     * @param message Mensaje descriptivo del error a incluir en el cuerpo JSON
     * de la respuesta.
     */
    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        log.debug("Abortando petición con 401 Unauthorized. Mensaje: {}", message);
        Response unauthorizedResponse = Response.status(Response.Status.UNAUTHORIZED)
                // Cabecera estándar para indicar el desafío de autenticación
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"BeatpassTFG API\"")
                // Cuerpo de respuesta JSON simple
                .entity("{\"error\": \"" + message + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        requestContext.abortWith(unauthorizedResponse);
    }

}
