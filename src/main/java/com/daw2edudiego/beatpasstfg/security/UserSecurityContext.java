/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.security;

import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

/**
 * Implementación personalizada de SecurityContext para JAX-RS. Almacena la
 * información del usuario autenticado (obtenida del token JWT) y la hace
 * disponible para los recursos JAX-RS a través de la inyección @Context.
 */
public class UserSecurityContext implements SecurityContext {

    private static final Logger log = LoggerFactory.getLogger(UserSecurityContext.class);

    private final String userId;        // ID del usuario autenticado (del subject del token)
    private final String role;          // Rol del usuario autenticado (del claim "role")
    private final boolean secure;       // Si la conexión original era HTTPS
    private final Principal principal;  // Principal que representa al usuario

    /**
     * Constructor para crear el contexto de seguridad.
     *
     * @param userId El ID del usuario autenticado.
     * @param role El rol del usuario autenticado.
     * @param secure True si la conexión es HTTPS, false si no.
     */
    public UserSecurityContext(String userId, String role, boolean secure) {
        if (userId == null || userId.isBlank() || role == null || role.isBlank()) {
            log.error("Intento de crear UserSecurityContext con userId o role nulos/vacíos.");
            throw new IllegalArgumentException("UserId y Role no pueden ser nulos o vacíos para SecurityContext.");
        }
        this.userId = userId;
        this.role = role;
        this.secure = secure;
        // Creamos un Principal simple que devuelve el userId en getName()
        this.principal = () -> userId;
        log.trace("UserSecurityContext creado para userId: {}, role: {}, secure: {}", userId, role, secure);
    }

    /**
     * Devuelve el Principal que representa al usuario autenticado. El nombre
     * del principal es el ID del usuario.
     *
     * @return El Principal del usuario.
     */
    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    /**
     * Comprueba si el usuario autenticado pertenece a un rol específico. La
     * comparación ignora mayúsculas y minúsculas.
     *
     * @param requiredRole El rol a verificar (ej: "ADMIN", "PROMOTOR").
     * @return true si el usuario tiene el rol, false en caso contrario.
     */
    @Override
    public boolean isUserInRole(String requiredRole) {
        boolean isInRole = this.role != null && this.role.equalsIgnoreCase(requiredRole);
        log.trace("Comprobando rol: usuario {} tiene rol '{}'? Requerido: '{}'. Resultado: {}", userId, this.role, requiredRole, isInRole);
        return isInRole;
    }

    /**
     * Indica si la petición original se realizó sobre una conexión segura
     * (HTTPS).
     *
     * @return true si era HTTPS, false si no.
     */
    @Override
    public boolean isSecure() {
        return this.secure;
    }

    /**
     * Devuelve el esquema de autenticación utilizado. Para JWT, comúnmente es
     * "Bearer".
     *
     * @return La cadena "Bearer".
     */
    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
