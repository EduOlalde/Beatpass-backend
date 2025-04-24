package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.Email; // Validación
import jakarta.validation.constraints.NotBlank; // Validación
import jakarta.validation.constraints.Size; // Validación

/**
 * DTO (Data Transfer Object) utilizado específicamente para recibir las
 * credenciales (email y contraseña) en peticiones de inicio de sesión (login).
 *
 * @author Eduardo Olalde
 */
public class CredencialesDTO {

    /**
     * Email del usuario que intenta iniciar sesión. Requerido, debe tener
     * formato de email.
     */
    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String email;

    /**
     * Contraseña proporcionada por el usuario. Requerida. La validación de
     * longitud/complejidad podría añadirse si es necesario, aunque la
     * verificación principal se hace contra el hash almacenado.
     */
    @NotBlank(message = "La contraseña es obligatoria.")
    private String password;

    /**
     * Constructor por defecto (necesario para frameworks como Jackson/JAX-RS).
     */
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

    // --- toString (útil para debugging, ¡cuidado con loguear contraseñas!) ---
    @Override
    public String toString() {
        // Evitar incluir la contraseña en logs generales por seguridad
        return "CredencialesDTO{"
                + "email='" + email + '\''
                + ", password='[PROTEGIDO]'"
                + // No mostrar contraseña en logs
                '}';
    }
}
