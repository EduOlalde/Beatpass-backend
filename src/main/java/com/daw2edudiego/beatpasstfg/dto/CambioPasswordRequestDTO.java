package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de cambio de contraseña.
 */
public class CambioPasswordRequestDTO {

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres.")
    private String newPassword;

    @NotBlank(message = "La confirmación de la contraseña es obligatoria.")
    private String confirmPassword;

    public CambioPasswordRequestDTO() {
    }

    // Getters y Setters
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Override
    public String toString() {
        return "CambioPasswordRequestDTO{"
                + "newPassword='[PROTECTED]'"
                + ", confirmPassword='[PROTECTED]'"
                + '}';
    }
}
