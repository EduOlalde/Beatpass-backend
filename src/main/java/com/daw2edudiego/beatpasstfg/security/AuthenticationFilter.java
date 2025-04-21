package com.daw2edudiego.beatpasstfg.security; // O tu paquete security

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
import java.security.Principal;

/**
 * Filtro JAX-RS que intercepta las peticiones para validar el token JWT
 * presente en la cabecera Authorization: Bearer, *excepto* para rutas
 * específicas (login, admin, promotor, pos) que usan otros mecanismos. Si el
 * token es válido, establece un SecurityContext con la info del usuario.
 * ACTUALIZADO: Excluye también la ruta /api/pos.
 */
@Provider
@Priority(Priorities.AUTHENTICATION) // Se ejecuta antes que otros filtros como los de autorización
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JwtUtil jwtUtil = new JwtUtil(); // Instancia de la utilidad JWT

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        log.debug("AuthenticationFilter interceptando path: {}", path);

        // --- Rutas a Excluir del Filtro JWT ---
        // Excluir login, paneles web (admin, promotor) y ahora POS (que usan sesión)
        if (path.startsWith("auth/login") || path.startsWith("admin") || path.startsWith("promotor") || path.startsWith("pos")) {
            log.debug("Ruta {} excluida del filtro JWT.", path);
            return; // No aplicar filtro JWT a estas rutas
        }
        // --- Fin Rutas a Excluir ---

        log.debug("Aplicando filtro JWT para la ruta: {}", path);
        // 1. Obtener la cabecera Authorization
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // 2. Validar si la cabecera existe y tiene el formato Bearer
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Cabecera Authorization ausente o mal formada para path: {}", path);
            abortUnauthorized(requestContext, "Se requiere cabecera Authorization con formato 'Bearer token'.");
            return;
        }

        // 3. Extraer el token
        String token = authorizationHeader.substring("Bearer".length()).trim();

        try {
            // 4. Validar el token y obtener los claims
            Claims claims = jwtUtil.validarTokenYObtenerClaims(token);

            // 5. Extraer información del usuario (ID y Rol)
            String userId = jwtUtil.obtenerUserIdDeClaims(claims);
            String role = jwtUtil.obtenerRolDeClaims(claims);

            if (userId == null || role == null) {
                log.error("Token válido pero faltan claims de userId o role. Token: {}", token);
                abortUnauthorized(requestContext, "Token inválido (información incompleta).");
                return;
            }

            log.debug("Token JWT validado exitosamente para userId: {}, role: {}", userId, role);

            // 6. Crear y establecer el SecurityContext personalizado
            // Se pasa el esquema original (HTTPS?) y la información del usuario
            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            UserSecurityContext userSecurityContext = new UserSecurityContext(userId, role, currentSecurityContext.isSecure());
            requestContext.setSecurityContext(userSecurityContext);
            log.trace("SecurityContext establecido para la petición.");

        } catch (ExpiredJwtException eje) {
            log.warn("Token JWT expirado: {}", eje.getMessage());
            abortUnauthorized(requestContext, "Token expirado.");
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // Captura otras excepciones comunes de JJWT
            log.warn("Token JWT inválido: {}", e.getMessage());
            abortUnauthorized(requestContext, "Token inválido o mal formado.");
        } catch (JwtException e) {
            // Captura genérica para otros errores de JWT no esperados
            log.error("Error inesperado al procesar token JWT: {}", e.getMessage(), e);
            abortUnauthorized(requestContext, "Error procesando token.");
        } catch (Exception e) {
            // Captura para cualquier otro error inesperado
            log.error("Error inesperado en AuthenticationFilter: {}", e.getMessage(), e);
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error interno del servidor\"}")
                    .type(MediaType.APPLICATION_JSON).build());
        }
    }

    /**
     * Aborta la petición actual con una respuesta 401 Unauthorized.
     */
    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"BeatpassTFG API\"")
                        .entity("{\"error\": \"" + message + "\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build());
    }

    // --- Implementación interna o externa de SecurityContext ---
    // (UserSecurityContext sin cambios)
    private static class UserSecurityContext implements SecurityContext {

        private final String userId;
        private final String role;
        private final boolean secure;
        private final Principal principal;

        public UserSecurityContext(String userId, String role, boolean secure) {
            this.userId = userId;
            this.role = role;
            this.secure = secure;
            this.principal = () -> userId;
        }

        @Override
        public Principal getUserPrincipal() {
            return this.principal;
        }

        @Override
        public boolean isUserInRole(String requiredRole) {
            return this.role != null && this.role.equalsIgnoreCase(requiredRole);
        }

        @Override
        public boolean isSecure() {
            return this.secure;
        }

        @Override
        public String getAuthenticationScheme() {
            return "Bearer";
        }
    }
}
