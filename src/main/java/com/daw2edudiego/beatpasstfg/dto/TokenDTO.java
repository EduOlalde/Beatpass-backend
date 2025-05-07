package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO simple para encapsular y devolver un token JWT.
 */
public class TokenDTO {

    @NotBlank(message = "El token no puede estar vacío.")
    private String token;

    public TokenDTO() {
    }

    /**
     * Constructor para crear un TokenDTO con un token específico.
     *
     * @param token El token JWT a encapsular. No debe ser nulo ni vacío.
     */
    public TokenDTO(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo ni vacío para TokenDTO.");
        }
        this.token = token;
    }

    // --- Getter y Setter ---
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo ni vacío.");
        }
        this.token = token;
    }

    // --- toString ---
    @Override
    public String toString() {
        // Truncar el token en logs por seguridad
        return "TokenDTO{"
                + "token='" + (token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null") + '\''
                + '}';
    }
}
