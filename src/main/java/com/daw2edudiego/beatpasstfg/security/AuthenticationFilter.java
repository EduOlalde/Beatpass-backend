package com.daw2edudiego.beatpasstfg.security; // O tu paquete security

import com.daw2edudiego.beatpasstfg.util.JwtUtil; 

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException; // Importar JwtException de la librería
import jakarta.annotation.Priority; // jakarta.*
import jakarta.ws.rs.Priorities; // jakarta.*
import jakarta.ws.rs.container.ContainerRequestContext; // jakarta.*
import jakarta.ws.rs.container.ContainerRequestFilter; // jakarta.*
import jakarta.ws.rs.core.HttpHeaders; // jakarta.*
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response; // jakarta.*
import jakarta.ws.rs.core.SecurityContext; // jakarta.*
import jakarta.ws.rs.ext.Provider; // jakarta.*
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;

/**
 * Filtro JAX-RS que intercepta las peticiones para validar el token JWT
 * presente en la cabecera Authorization: Bearer, *excepto* para rutas
 * específicas (login, admin, promotor) que usan otros mecanismos (público o sesión).
 * Si el token es válido, establece un SecurityContext con la información del usuario.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    // Rutas que NO requieren validación de token JWT en este filtro
    private static final String LOGIN_PATH = "/auth/login";
    private static final String ADMIN_PATH_PREFIX = "/admin/";   // Rutas de admin (usarán sesión)
    private static final String PROMOTOR_PATH_PREFIX = "/promotor/"; // Rutas de promotor (usarán sesión)
    // Añade aquí otras rutas públicas generales si las tienes (ej: "/festivales/publicados")

    public AuthenticationFilter() {
        // Asegúrate de que JwtUtil usa jakarta.crypto si es necesario
        this.jwtUtil = new JwtUtil(); // ¡Necesitas tu implementación aquí!
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        // Nota: getPath() devuelve la ruta relativa a @ApplicationPath ("/api")
        // Ej: si la URL es /BeatpassTFG/api/admin/promotores, path será "admin/promotores"
        String method = requestContext.getMethod();
        log.debug("AuthenticationFilter: Filtrando petición: {} /api/{}", method, path);

        // --- Bypass para rutas públicas o gestionadas por sesión ---
        if (path.startsWith(LOGIN_PATH.substring(1)) // Quitar la primera '/' de LOGIN_PATH
            || path.startsWith(ADMIN_PATH_PREFIX.substring(1))
            || path.startsWith(PROMOTOR_PATH_PREFIX.substring(1))
            // || path.startsWith("festivales/publicados") // Ejemplo ruta pública
            || "OPTIONS".equals(method)) { // OPTIONS es para preflight requests de CORS

            log.debug("Petición a ruta pública/sesión/OPTIONS ({}) permitida. Saltando validación JWT.", path);
            return; // No aplicar filtro JWT, dejar que el Resource maneje la sesión si es necesario
        }
        // --- Fin Bypass ---


        // --- Lógica de Validación JWT (para el resto de rutas - ej: API cliente final) ---
        log.debug("Ruta /api/{} requiere validación JWT.", path);

        // 1. Obtener la cabecera Authorization
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // 2. Validar si la cabecera existe y tiene el formato "Bearer <token>"
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Cabecera Authorization ausente o mal formada para {} /api/{}", method, path);
            abortUnauthorized(requestContext, "Se requiere cabecera Authorization con formato 'Bearer token'.");
            return;
        }

        // 3. Extraer el token
        String token = authorizationHeader.substring("Bearer ".length()).trim();

        try {
            // 4. Validar el token JWT (firma, expiración) y obtener claims
            Claims claims = jwtUtil.validarTokenYObtenerClaims(token);

            // 5. Extraer información del usuario del token (claims)
            String userId = jwtUtil.obtenerUserIdDeClaims(claims); // Asume que estos métodos existen en tu JwtUtil
            String role = jwtUtil.obtenerRolDeClaims(claims);

            if (userId == null || role == null) {
                 log.error("Token válido pero faltan claims esenciales (subject/role). Token: {}", token);
                 abortUnauthorized(requestContext, "Token inválido (información incompleta).");
                 return;
            }

            log.debug("Token JWT válido para usuario ID: {}, Rol: {}", userId, role);

            // 6. Establecer el SecurityContext personalizado (para las rutas que SÍ usan JWT)
            final SecurityContext originalContext = requestContext.getSecurityContext();
            // Usar la implementación UserSecurityContext o una anónima
            requestContext.setSecurityContext(new UserSecurityContext(userId, role, originalContext.isSecure()));

            log.debug("SecurityContext (JWT) establecido para usuario ID: {}", userId);

        } catch (JwtException e) {
            log.warn("Validación de token JWT fallida para {} /api/{}: {}", method, path, e.getMessage());
            abortUnauthorized(requestContext, "Token inválido o expirado.");
        } catch (Exception e) {
             log.error("Error inesperado al procesar token JWT o establecer SecurityContext: {}", e.getMessage(), e);
             abortUnauthorized(requestContext, "Error procesando autenticación.");
        }
    }

    /**
     * Aborta la petición actual con una respuesta 401 Unauthorized.
     */
    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"BeatpassTFG API\"") // Indica que se espera Bearer token
                    .entity("{\"error\": \"" + message + "\"}") // Devolver JSON
                    .type(MediaType.APPLICATION_JSON)
                    .build());
    }

    // --- Implementación interna o externa de SecurityContext ---
    // (Necesaria si no usas la anónima)
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

        @Override public Principal getUserPrincipal() { return this.principal; }
        @Override public boolean isUserInRole(String requiredRole) { return this.role != null && this.role.equalsIgnoreCase(requiredRole); }
        @Override public boolean isSecure() { return this.secure; }
        @Override public String getAuthenticationScheme() { return "Bearer"; }
    }
}
