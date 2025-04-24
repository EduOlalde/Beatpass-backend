package com.daw2edudiego.beatpasstfg.security;

import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Objects;

/**
 * Una implementación personalizada de {@link SecurityContext} para JAX-RS.
 * <p>
 * Este contexto almacena información sobre el usuario autenticado, típicamente
 * obtenida de un JWT validado u otro mecanismo de autenticación. Hace que el
 * {@link Principal} del usuario (representando su identidad, usualmente el ID
 * de usuario) y su(s) rol(es) estén disponibles para los métodos de recurso
 * JAX-RS a través de la inyección {@code @Context SecurityContext}.
 * </p>
 */
public class UserSecurityContext implements SecurityContext {

    private static final Logger log = LoggerFactory.getLogger(UserSecurityContext.class);

    private final String userId;        // El identificador único para el usuario autenticado.
    private final String role;          // El rol asignado al usuario autenticado.
    private final boolean secure;       // Indicador de si la petición original se hizo sobre HTTPS.
    private final Principal principal;  // El objeto Principal que representa al usuario.

    /**
     * Construye un nuevo UserSecurityContext.
     *
     * @param userId El ID del usuario autenticado. No puede ser nulo ni estar
     * en blanco.
     * @param role El rol del usuario autenticado. No puede ser nulo ni estar en
     * blanco.
     * @param secure {@code true} si la petición se hizo sobre HTTPS,
     * {@code false} en caso contrario.
     * @throws NullPointerException si userId o role es nulo.
     * @throws IllegalArgumentException si userId o role está en blanco.
     */
    public UserSecurityContext(String userId, String role, boolean secure) {
        // Validaciones de nulidad y contenido
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
        // Crear un Principal simple donde getName() devuelve el userId.
        this.principal = () -> this.userId; // Expresión lambda para la implementación de Principal

        log.trace("UserSecurityContext creado - UserID: '{}', Role: '{}', Secure: {}", userId, role, secure);
    }

    /**
     * Devuelve el {@link Principal} que representa al usuario autenticado. El
     * nombre del principal típicamente corresponde al ID único del usuario.
     *
     * @return El objeto Principal para el usuario.
     */
    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    /**
     * Comprueba si el usuario autenticado pertenece al rol especificado. La
     * comparación no distingue entre mayúsculas y minúsculas.
     *
     * @param requiredRole El nombre del rol a comprobar (ej., "ADMIN",
     * "PROMOTOR").
     * @return {@code true} si el usuario está asociado con el rol dado,
     * {@code false} en caso contrario.
     */
    @Override
    public boolean isUserInRole(String requiredRole) {
        // Comprobar si requiredRole no es nulo y coincide con el rol del usuario (ignorando mayúsculas/minúsculas)
        boolean isInRole = requiredRole != null && requiredRole.equalsIgnoreCase(this.role);
        log.trace("Comprobación de rol: Usuario '{}' en rol '{}'? Requerido: '{}'. Resultado: {}",
                this.userId, this.role, requiredRole, isInRole);
        return isInRole;
    }

    /**
     * Indica si la petición se realizó usando un canal seguro (HTTPS).
     *
     * @return {@code true} si la petición fue sobre HTTPS, {@code false} en
     * caso contrario.
     */
    @Override
    public boolean isSecure() {
        return this.secure;
    }

    /**
     * Devuelve el esquema de autenticación utilizado para autenticar al
     * usuario. Para autenticación basada en JWT, esto es típicamente "Bearer".
     * Para basada en sesión, podría ser "FORM" o "BASIC", etc.
     *
     * @return El identificador del esquema de autenticación (ej., "Bearer").
     */
    @Override
    public String getAuthenticationScheme() {
        // Asumiendo Bearer para JWT, ajustar si otros esquemas son posibles en este contexto
        return "Bearer";
    }
}
