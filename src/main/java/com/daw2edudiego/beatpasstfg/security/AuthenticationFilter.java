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
import jakarta.ws.rs.core.UriInfo; // Importar UriInfo
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern; // Importar Pattern

/**
 * Filtro JAX-RS {@link ContainerRequestFilter} para la autenticación basada en
 * JSON Web Tokens (JWT).
 * <p>
 * Intercepta las peticiones entrantes a la API (rutas bajo `/api/*`) y aplica
 * la siguiente lógica:
 * <ol>
 * <li>Ignora las peticiones OPTIONS (preflight CORS) abortando con OK.</li>
 * <li>Ignora las rutas cuyos prefijos están en
 * {@code EXCLUDED_PATHS_PREFIXES}.</li>
 * <li>Ignora las peticiones GET a rutas públicas específicas (festivales/id,
 * festivales/id/entradas).</li>
 * <li>Para todas las demás peticiones, valida el token JWT.</li>
 * </ol>
 * Si el token es válido, establece un {@link UserSecurityContext}. Si no,
 * aborta con 401 Unauthorized.
 * </p>
 *
 * @see JwtUtil
 * @see UserSecurityContext
 * @see Provider
 * @see Priority
 * @author Eduardo Olalde
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    // Instancia de la utilidad JWT para validar tokens
    private final JwtUtil jwtUtil = new JwtUtil();

    /**
     * Lista de prefijos de rutas que deben ser excluidas de la validación JWT.
     */
    private static final List<String> EXCLUDED_PATHS_PREFIXES = Arrays.asList(
            "auth/login", // Endpoint de login API
            "admin", // Prefijo para el panel web de admin
            "promotor", // Prefijo para el panel web de promotor
            "public/" // Rutas públicas generales
    );

    /**
     * Patrón regex para la ruta GET pública de detalles de un festival.
     * Coincide con "festivales/" seguido de uno o más dígitos y nada más.
     */
    private static final Pattern PUBLIC_GET_FESTIVAL_DETAIL_PATTERN = Pattern.compile("^festivales/\\d+$");
    /**
     * Patrón regex para la ruta GET pública de tipos de entrada de un festival.
     * Coincide con "festivales/" seguido de dígitos, "/entradas" y nada más.
     */
    private static final Pattern PUBLIC_GET_FESTIVAL_TICKETS_PATTERN = Pattern.compile("^festivales/\\d+/entradas$");

    /**
     * Método principal del filtro que procesa cada petición entrante.
     * @param requestContext
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo(); // Obtener UriInfo
        String path = uriInfo.getPath(); // Obtener ruta relativa
        String method = requestContext.getMethod();

        log.trace("AuthenticationFilter procesando petición: {} /api/{}", method, path);

        // 1. Permitir peticiones CORS preflight (OPTIONS) sin autenticación
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("Petición OPTIONS para ruta: /api/{}. Abortando con OK para preflight CORS.", path);
            return;
        }

        // 2. Comprobar si la ruta debe ser excluida por prefijo
        if (isPathExcluded(path)) {
            log.debug("Ruta '/api/{}' excluida del filtro JWT por prefijo.", path);
            return; // Continuar sin validar token
        }

        // 3. Comprobar si es una ruta GET pública específica ---
        if ("GET".equalsIgnoreCase(method) && isPublicGetPath(path)) {
            log.debug("Ruta GET pública específica '/api/{}' permitida sin token JWT.", path);
            return; // Permitir acceso público sin token
        }

        // 4. Obtener la cabecera Authorization
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // 5. Validar presencia y formato del Header ("Bearer <token>")
        if (authorizationHeader == null || !authorizationHeader.toLowerCase().startsWith("bearer ")) {
            log.warn("Cabecera Authorization ausente o mal formada para la ruta protegida: /api/{}", path);
            abortUnauthorized(requestContext, "Se requiere cabecera Authorization: Bearer <token>.");
            return; // Abortar petición
        }

        // 6. Extraer la cadena del token
        String token = authorizationHeader.substring("Bearer".length()).trim();
        if (token.isEmpty()) {
            log.warn("Cabecera Authorization presente pero el token está vacío para la ruta: /api/{}", path);
            abortUnauthorized(requestContext, "El token Bearer proporcionado está vacío.");
            return; // Abortar petición
        }

        // 7. Validar el token y extraer claims
        try {
            // Usar la instancia jwtUtil (como en tu código original)
            Claims claims = jwtUtil.validarTokenYObtenerClaims(token);
            String userId = jwtUtil.obtenerUserIdDeClaims(claims);
            String role = jwtUtil.obtenerRolDeClaims(claims);

            // Verificar que los claims esenciales no sean nulos
            if (userId == null || role == null) {
                log.error("Token válido pero faltan claims esenciales (userId o role). Token sub: {}", claims.getSubject());
                abortUnauthorized(requestContext, "Token inválido: información de usuario incompleta.");
                return;
            }

            log.debug("JWT validado exitosamente para userId: {}, role: {} en ruta: /api/{}", userId, role, path);

            // 8. Establecer el SecurityContext personalizado
            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            UserSecurityContext userSecurityContext = new UserSecurityContext(
                    userId,
                    role,
                    currentSecurityContext.isSecure() // Mantener si la conexión original era HTTPS
            );
            requestContext.setSecurityContext(userSecurityContext);
            log.trace("UserSecurityContext establecido para la petición.");

        } catch (ExpiredJwtException eje) {
            log.warn("Validación JWT fallida para ruta /api/{}: Token expirado.", path);
            abortUnauthorized(requestContext, "El token ha expirado.");
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("Validación JWT fallida para ruta /api/{}: Token inválido ({}).", path, e.getClass().getSimpleName());
            abortUnauthorized(requestContext, "Token inválido o mal formado.");
        } catch (JwtException e) {
            log.error("Error inesperado procesando JWT para ruta /api/{}: {}", path, e.getMessage(), e);
            abortUnauthorized(requestContext, "Error procesando token.");
        } catch (Exception e) {
            log.error("Error inesperado en AuthenticationFilter para ruta /api/{}: {}", path, e.getMessage(), e);
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error interno del servidor durante la autenticación.\"}")
                    .type(MediaType.APPLICATION_JSON).build());
        }
    }

    /**
     * Comprueba si la ruta relativa coincide con algún prefijo excluido.
     */
    private boolean isPathExcluded(String relativePath) {
        if (relativePath == null) {
            return false;
        }
        String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return EXCLUDED_PATHS_PREFIXES.stream().anyMatch(cleanPath::startsWith);
    }

    /**
     * Comprueba si la ruta relativa coincide con patrones GET
     * públicos específicos. ---
     */
    private boolean isPublicGetPath(String relativePath) {
        if (relativePath == null) {
            return false;
        }
        String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        // Comprobar si coincide con alguno de los patrones definidos
        return PUBLIC_GET_FESTIVAL_DETAIL_PATTERN.matcher(cleanPath).matches()
                || PUBLIC_GET_FESTIVAL_TICKETS_PATTERN.matcher(cleanPath).matches();
    }

    /**
     * Aborta la petición con 401 Unauthorized.
     */
    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        log.debug("Abortando petición con 401 Unauthorized. Mensaje: {}", message);
        Response unauthorizedResponse = Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"BeatpassTFG API\"")
                .entity("{\"error\": \"" + message + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        requestContext.abortWith(unauthorizedResponse);
    }
}
