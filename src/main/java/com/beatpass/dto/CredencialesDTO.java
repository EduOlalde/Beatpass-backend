package com.beatpass.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para recibir credenciales (email y contraseña) en peticiones de login.
 */
public class CredencialesDTO {

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    private String password;

    public CredencialesDTO() {
    }

    // --- Getters y Setters ---
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // --- toString ---
    @Override
    public String toString() {
        return "CredencialesDTO{"
                + "email='" + email + '\''
                + ", password='[PROTEGIDO]'"
                + '}';
    }
}
