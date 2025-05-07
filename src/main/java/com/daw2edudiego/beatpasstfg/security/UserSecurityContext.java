package com.daw2edudiego.beatpasstfg.security;

import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Objects;

/**
 * Implementación personalizada de SecurityContext para JAX-RS. Almacena
 * información del usuario autenticado (ID, rol) y el estado de seguridad de la
 * conexión.
 */
public class UserSecurityContext implements SecurityContext {

    private static final Logger log = LoggerFactory.getLogger(UserSecurityContext.class);

    private final String userId;
    private final String role;
    private final boolean secure;
    private final Principal principal;

    /**
     * Construye un nuevo UserSecurityContext.
     *
     * @param userId ID del usuario autenticado (no nulo/vacío).
     * @param role Rol del usuario autenticado (no nulo/vacío).
     * @param secure true si la petición original se hizo sobre HTTPS.
     */
    public UserSecurityContext(String userId, String role, boolean secure) {
        Objects.requireNonNull(userId, "El ID de usuario no puede ser nulo para SecurityContext.");
        Objects.requireNonNull(role, "El Rol no puede ser nulo para SecurityContext.");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("El ID de usuario no puede estar en blanco para SecurityContext.");
        }
        if (role.isBlank()) {
            throw new IllegalArgumentException("El Rol no puede estar en blanco para SecurityContext.");
        }

        this.userId = userId;
        this.role = role;
        this.secure = secure;
        this.principal = () -> this.userId; // Principal simple basado en userId

        log.trace("UserSecurityContext creado - UserID: '{}', Role: '{}', Secure: {}", userId, role, secure);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isUserInRole(String requiredRole) {
        boolean isInRole = requiredRole != null && requiredRole.equalsIgnoreCase(this.role);
        log.trace("Comprobación de rol: Usuario '{}' en rol '{}'? Requerido: '{}'. Resultado: {}",
                this.userId, this.role, requiredRole, isInRole);
        return isInRole;
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    @Override
    public String getAuthenticationScheme() {
        // Asumiendo Bearer para JWT
        return "Bearer";
    }
}
