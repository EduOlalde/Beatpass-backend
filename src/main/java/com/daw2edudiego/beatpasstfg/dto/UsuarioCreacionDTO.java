package com.daw2edudiego.beatpasstfg.dto;

import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import jakarta.validation.constraints.Email; // Validación
import jakarta.validation.constraints.NotBlank; // Validación
import jakarta.validation.constraints.NotNull; // Validación
import jakarta.validation.constraints.Size; // Validación

/**
 * DTO (Data Transfer Object) específico para recibir los datos necesarios para
 * la creación de un nuevo {@link com.daw2edudiego.beatpasstfg.model.Usuario}.
 * Incluye la contraseña en texto plano, la cual debe ser hasheada antes de
 * persistir la entidad Usuario.
 *
 * @author Eduardo Olalde
 */
public class UsuarioCreacionDTO {

    /**
     * Nombre del nuevo usuario. Obligatorio.
     */
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    /**
     * Email del nuevo usuario. Obligatorio, formato válido y único en el
     * sistema.
     */
    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String email;

    /**
     * Contraseña en texto plano para el nuevo usuario. Obligatoria.
     */
    @NotBlank(message = "La contraseña es obligatoria.")
    // Se podrían añadir validaciones de longitud/complejidad aquí (@Size, @Pattern)
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    private String password;

    /**
     * Rol asignado al nuevo usuario. Obligatorio.
     */
    @NotNull(message = "El rol es obligatorio.")
    private RolUsuario rol;

    /**
     * Constructor por defecto.
     */
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

    /**
     * Obtiene la contraseña en texto plano. ¡Usar con precaución! Solo para el
     * proceso de creación y hasheo.
     *
     * @return La contraseña en texto plano.
     */
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
        // Nunca incluir la contraseña en texto plano en logs generales
        return "UsuarioCreacionDTO{"
                + "nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + ", password='[PROTEGIDO]'"
                + ", rol=" + rol
                + '}';
    }
}
