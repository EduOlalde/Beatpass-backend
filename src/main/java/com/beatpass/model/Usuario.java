package com.beatpass.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa un usuario del sistema (ADMIN, PROMOTOR, CAJERO).
 * Mapea la tabla 'usuarios'.
 */
@Entity
@Table(name = "usuarios", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email", name = "uq_usuario_email")
})
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @NotBlank(message = "El nombre de usuario no puede estar vacío.")
    @Size(max = 100, message = "El nombre de usuario no puede exceder los 100 caracteres.")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El email del usuario no puede estar vacío.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email del usuario no puede exceder los 100 caracteres.")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacío.") // El hash no estará vacío
    @Size(max = 255, message = "El hash de la contraseña excede la longitud permitida.")
    @Column(name = "password", nullable = false, length = 255)
    private String password; // Contraseña hasheada

    @NotNull(message = "El rol del usuario no puede ser nulo.")
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolUsuario rol;

    @NotNull
    @Column(name = "estado", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean estado = true; // Activo por defecto

    @NotNull
    @Column(name = "cambio_password_requerido", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean cambioPasswordRequerido = true; // Requiere cambio por defecto

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    /**
     * Festivales gestionados por este usuario (si es PROMOTOR). Relación 1:N.
     * Cascade ALL, Fetch LAZY, orphanRemoval true. ¡Precaución con Cascade ALL!
     */
    @OneToMany(mappedBy = "promotor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Festival> festivales = new HashSet<>();

    /**
     * Recargas realizadas por este usuario (si es CAJERO). Relación 1:N. No
     * Cascade ALL. Fetch LAZY.
     */
    @OneToMany(mappedBy = "usuarioCajero", fetch = FetchType.LAZY)
    private Set<Recarga> recargasRealizadas = new HashSet<>();

    public Usuario() {
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

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public Set<Festival> getFestivales() {
        return festivales;
    }

    public void setFestivales(Set<Festival> festivales) {
        this.festivales = festivales;
    }

    public Set<Recarga> getRecargasRealizadas() {
        return recargasRealizadas;
    }

    public void setRecargasRealizadas(Set<Recarga> recargasRealizadas) {
        this.recargasRealizadas = recargasRealizadas;
    }

    // --- equals, hashCode y toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Usuario usuario = (Usuario) o;
        return idUsuario != null && Objects.equals(idUsuario, usuario.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }

    @Override
    public String toString() {
        return "Usuario{"
                + "idUsuario=" + idUsuario
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + ", rol=" + rol
                + ", estado=" + estado
                + ", cambioPasswordRequerido=" + cambioPasswordRequerido
                + '}';
    }
}
