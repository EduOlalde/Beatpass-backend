package com.daw2edudiego.beatpasstfg.dto;

import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.fasterxml.jackson.annotation.JsonInclude; // Para excluir nulos
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) para representar la información de un
 * {@link com.daw2edudiego.beatpasstfg.model.Usuario} para operaciones de
 * lectura. Excluye información sensible como la contraseña hasheada. Puede
 * incluir información adicional útil para la visualización.
 *
 * @author Eduardo Olalde
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Opcional: Omite campos nulos en JSON
public class UsuarioDTO {

    private Integer idUsuario;
    private String nombre;
    private String email;
    private RolUsuario rol;
    private Boolean estado; // Estado de activación (true/false)
    private Boolean cambioPasswordRequerido; // Añadido para informar al frontend si es necesario
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    /**
     * Constructor por defecto.
     */
    public UsuarioDTO() {
    }

    // --- Getters y Setters ---
    // (Omitidos por brevedad, incluir todos)
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

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

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Boolean getCambioPasswordRequerido() {
        return cambioPasswordRequerido;
    } // Getter añadido

    public void setCambioPasswordRequerido(Boolean cambioPasswordRequerido) {
        this.cambioPasswordRequerido = cambioPasswordRequerido;
    } // Setter añadido

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    // --- equals y hashCode basados en ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsuarioDTO that = (UsuarioDTO) o;
        return Objects.equals(idUsuario, that.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }

    // --- toString ---
    @Override
    public String toString() {
        return "UsuarioDTO{"
                + "idUsuario=" + idUsuario
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + ", rol=" + rol
                + ", estado=" + estado
                + ", cambioPasswordRequerido=" + cambioPasswordRequerido
                + // Añadido
                '}';
    }
}
