/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.security;

import com.daw2edudiego.beatpasstfg.util.JwtUtil; // ¡Necesitas implementar esta clase!

import io.jsonwebtoken.Claims; // Importar de la librería JWT que uses
import io.jsonwebtoken.JwtException;
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
import javax.annotation.Priority;

/**
 * Filtro JAX-RS que intercepta las peticiones para validar el token JWT
 * presente en la cabecera Authorization: Bearer. Si el token es válido,
 * establece un SecurityContext con la información del usuario.
 */
@Provider // Indica a JAX-RS que esta clase es un proveedor (filtro)
@Priority(Priorities.AUTHENTICATION) // Define la prioridad de ejecución del filtro
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JwtUtil jwtUtil; // ¡Asume que esta clase existe y está implementada!

    // Rutas que NO requieren autenticación (ej: login, recursos públicos)
    private static final String LOGIN_PATH = "/auth/login";
    private static final String PUBLIC_FESTIVAL_PATH = "/festivales/publicados";
    // Añade aquí otras rutas públicas si las tienes

    public AuthenticationFilter() {
        this.jwtUtil = new JwtUtil(); // ¡Necesitas tu implementación aquí!
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();
        log.debug("Filtrando petición: {} {}", method, path);

        // Permitir acceso a rutas públicas sin token
        if (path.startsWith(LOGIN_PATH) || path.startsWith(PUBLIC_FESTIVAL_PATH) || "OPTIONS".equals(method)) {
            // OPTIONS es para preflight requests de CORS
            log.debug("Petición a ruta pública o OPTIONS permitida sin autenticación.");
            return; // No aplicar filtro
        }

        // 1. Obtener la cabecera Authorization
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // 2. Validar si la cabecera existe y tiene el formato "Bearer <token>"
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Cabecera Authorization ausente o mal formada para {} {}", method, path);
            abortUnauthorized(requestContext, "Se requiere cabecera Authorization con formato 'Bearer token'.");
            return;
        }

        // 3. Extraer el token
        String token = authorizationHeader.substring("Bearer ".length()).trim();

        try {
            Claims claims = jwtUtil.validarTokenYObtenerClaims(token);
            String userId = jwtUtil.obtenerUserIdDeClaims(claims); // Usando los métodos de JwtUtil actualizado
            String role = jwtUtil.obtenerRolDeClaims(claims);

            if (userId == null || role == null) {
                log.error("Token válido pero faltan claims esenciales (subject/role). Token: {}", token);
                abortUnauthorized(requestContext, "Token inválido (información incompleta).");
                return;
            }

            log.debug("Token válido para usuario ID: {}, Rol: {}", userId, role);

            // Crear e instanciar el nuevo SecurityContext
            final SecurityContext originalContext = requestContext.getSecurityContext();
            UserSecurityContext userSecurityContext = new UserSecurityContext(userId, role, originalContext.isSecure()); // Usar la nueva clase

            // Establecer el contexto de seguridad para la petición actual
            requestContext.setSecurityContext(userSecurityContext);

            log.debug("UserSecurityContext establecido para usuario ID: {}", userId);

        } catch (JwtException e) { // Captura más específica de la librería JWT
            log.warn("Validación de token fallida para {} {}: {}", method, path, e.getMessage());
            abortUnauthorized(requestContext, "Token inválido o expirado.");
        } catch (Exception e) { // Captura genérica por si acaso
            log.error("Error inesperado al procesar token o establecer SecurityContext: {}", e.getMessage(), e);
            abortUnauthorized(requestContext, "Error procesando autenticación.");
        }
    }

    /**
     * Aborta la petición actual con una respuesta 401 Unauthorized.
     */
    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"BeatpassTFG\"") // Opcional: indica cómo autenticarse
                        .entity(message)
                        .type(MediaType.APPLICATION_JSON) // O TEXT_PLAIN
                        .build());
    }
}
