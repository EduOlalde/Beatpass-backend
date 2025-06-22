package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO simple para encapsular y devolver un token JWT.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenDTO {

    @NotBlank(message = "El token no puede estar vacío.")
    private String token;
    private Integer userId;
    private String userName;
    private String userRole;
    private Boolean cambioPasswordRequerido;

    public TokenDTO() {
    }

    /**
     * Constructor para crear un TokenDTO con un token específico.
     *
     * @param token El token JWT a encapsular. No debe ser nulo ni vacío.
     * @param userId El ID del usuario.
     * @param userName El nombre del usuario.
     * @param userRole El rol del usuario.
     * @param cambioPasswordRequerido Flag que indica si el cambio es necesario.
     */
    public TokenDTO(String token, Integer userId, String userName, String userRole, Boolean cambioPasswordRequerido) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo ni vacío para TokenDTO.");
        }
        this.token = token;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.cambioPasswordRequerido = cambioPasswordRequerido;
    }

    // --- Getters y Setters ---
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo ni vacío.");
        }
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Boolean getCambioPasswordRequerido() {
        return cambioPasswordRequerido;
    }

    public void setCambioPasswordRequerido(Boolean cambioPasswordRequerido) {
        this.cambioPasswordRequerido = cambioPasswordRequerido;
    }

    @Override
    public String toString() {
        return "TokenDTO{"
                + "token='" + (token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null") + '\''
                + ", userId=" + userId
                + ", userName='" + userName + '\''
                + ", userRole='" + userRole + '\''
                + ", cambioPasswordRequerido=" + cambioPasswordRequerido
                + '}';
    }
}
