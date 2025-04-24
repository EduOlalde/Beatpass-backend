package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.NotBlank; // Validación

/**
 * DTO (Data Transfer Object) simple para encapsular y devolver un token JWT
 * (JSON Web Token) al cliente, típicamente después de un proceso de
 * autenticación exitoso.
 *
 * @author Eduardo Olalde
 */
public class TokenDTO {

    /**
     * El token JWT generado. No debe estar vacío.
     */
    @NotBlank(message = "El token no puede estar vacío.") // Validación básica
    private String token;

    /**
     * Constructor por defecto (necesario para algunos frameworks como Jackson).
     * Se recomienda no usarlo directamente si el token siempre es requerido.
     */
    public TokenDTO() {
    }

    /**
     * Constructor para crear un TokenDTO con un token específico.
     *
     * @param token El token JWT a encapsular. No debe ser nulo ni vacío.
     * @throws IllegalArgumentException si el token es nulo o vacío.
     */
    public TokenDTO(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo ni vacío para TokenDTO.");
        }
        this.token = token;
    }

    // --- Getter y Setter ---
    /**
     * Obtiene el token JWT.
     *
     * @return El token JWT.
     */
    public String getToken() {
        return token;
    }

    /**
     * Establece el token JWT.
     *
     * @param token El nuevo token JWT.
     */
    public void setToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo ni vacío.");
        }
        this.token = token;
    }

    // --- toString ---
    @Override
    public String toString() {
        // Por seguridad, podríamos truncar el token en el log o no mostrarlo
        return "TokenDTO{"
                + "token='" + (token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null") + '\''
                + '}';
    }
}
