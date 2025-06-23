package com.beatpass.dto;

import com.beatpass.model.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO específico para recibir los datos necesarios para la creación de un nuevo
 * Usuario. Incluye la contraseña en texto plano.
 */
public class UsuarioCreacionDTO {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    private String password;

    @NotNull(message = "El rol es obligatorio.")
    private RolUsuario rol;

    public UsuarioCreacionDTO() {
    }

    // --- Getters y Setters ---
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

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

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    // --- toString ---
    @Override
    public String toString() {
        return "UsuarioCreacionDTO{"
                + "nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + ", password='[PROTEGIDO]'"
                + ", rol=" + rol
                + '}';
    }
}
