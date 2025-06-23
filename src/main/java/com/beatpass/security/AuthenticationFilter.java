package com.beatpass.security;

import com.beatpass.util.JwtUtil;
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
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Filtro JAX-RS para autenticación basada en JWT. Intercepta peticiones a
 * /api/*, excluyendo rutas públicas/paneles web, y valida el token Bearer.
 * Establece UserSecurityContext si es válido.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JwtUtil jwtUtil = new JwtUtil();

    private static final List<String> EXCLUDED_PATHS_PREFIXES = Arrays.asList(
            "auth/login",
            "public/"
    );

    private static final Pattern PUBLIC_GET_FESTIVAL_DETAIL_PATTERN = Pattern.compile("^festivales/\\d+$");
    private static final Pattern PUBLIC_GET_FESTIVAL_TICKETS_PATTERN = Pattern.compile("^festivales/\\d+/tipos-entrada$");

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        String path = uriInfo.getPath();
        String method = requestContext.getMethod();

        log.trace("AuthenticationFilter procesando petición: {} /api/{}", method, path);

        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("Petición OPTIONS para ruta: /api/{}. Abortando con OK para preflight CORS.", path);
            return;
        }

        if (isPathExcluded(path)) {
            log.debug("Ruta '/api/{}' excluida del filtro JWT por prefijo.", path);
            return;
        }

        if ("GET".equalsIgnoreCase(method) && isPublicGetPath(path)) {
            log.debug("Ruta GET pública específica '/api/{}' permitida sin token JWT.", path);
            return;
        }

        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.toLowerCase().startsWith("bearer ")) {
            log.warn("Cabecera Authorization ausente o mal formada para la ruta protegida: /api/{}", path);
            abortUnauthorized(requestContext, "Se requiere cabecera Authorization: Bearer <token>.");
            return;
        }

        String token = authorizationHeader.substring("Bearer".length()).trim();
        if (token.isEmpty()) {
            log.warn("Cabecera Authorization presente pero el token está vacío para la ruta: /api/{}", path);
            abortUnauthorized(requestContext, "El token Bearer proporcionado está vacío.");
            return;
        }

        try {
            Claims claims = jwtUtil.validarTokenYObtenerClaims(token);
            String userId = jwtUtil.obtenerUserIdDeClaims(claims);
            String role = jwtUtil.obtenerRolDeClaims(claims);

            if (userId == null || role == null) {
                log.error("Token válido pero faltan claims esenciales (userId o role). Token sub: {}", claims.getSubject());
                abortUnauthorized(requestContext, "Token inválido: información de usuario incompleta.");
                return;
            }

            log.debug("JWT validado exitosamente para userId: {}, role: {} en ruta: /api/{}", userId, role, path);

            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            UserSecurityContext userSecurityContext = new UserSecurityContext(
                    userId,
                    role,
                    currentSecurityContext.isSecure()
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
     * Comprueba si la ruta relativa coincide con patrones GET públicos
     * específicos.
     */
    private boolean isPublicGetPath(String relativePath) {
        if (relativePath == null) {
            return false;
        }
        String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return PUBLIC_GET_FESTIVAL_DETAIL_PATTERN.matcher(cleanPath).matches()
                || PUBLIC_GET_FESTIVAL_TICKETS_PATTERN.matcher(cleanPath).matches();
    }

    /**
     * Aborta la petición con 401 Unauthorized y cabecera WWW-Authenticate.
     */
    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        log.debug("Abortando petición con 401 Unauthorized. Mensaje: {}", message);
        Response unauthorizedResponse = Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"Beatpass API\"") // Informa al cliente
                .entity("{\"error\": \"" + message + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        requestContext.abortWith(unauthorizedResponse);
    }
}
