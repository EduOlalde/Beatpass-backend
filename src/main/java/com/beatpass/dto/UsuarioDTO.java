package com.beatpass.dto;

import com.beatpass.model.RolUsuario;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para representar la información de un Usuario para operaciones de
 * lectura. Excluye la contraseña hasheada.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioDTO {

    private Integer idUsuario;
    private String nombre;
    private String email;
    private RolUsuario rol;
    private Boolean estado; // Estado de activación
    private Boolean cambioPasswordRequerido; // ¿Necesita cambiar pass al loguear?
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    public UsuarioDTO() {
    }

    // --- Getters y Setters ---
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
    }

    public void setCambioPasswordRequerido(Boolean cambioPasswordRequerido) {
        this.cambioPasswordRequerido = cambioPasswordRequerido;
    }

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

    // --- equals y hashCode ---
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
                + '}';
    }
}
