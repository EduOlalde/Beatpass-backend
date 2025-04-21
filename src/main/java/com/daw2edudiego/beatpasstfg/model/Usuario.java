package com.daw2edudiego.beatpasstfg.model; // O tu paquete model

// Asegúrate de que los imports usan jakarta.*
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa un usuario del sistema (Administrador o Promotor). Mapea la tabla
 * 'usuarios'. ¡ACTUALIZADO con cambioPasswordRequerido!
 */
@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L; // Mantener o actualizar si cambian campos serializables

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolUsuario rol;

    @Column(name = "estado", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean estado = true;

    // --- NUEVO CAMPO ---
    @Column(name = "cambio_password_requerido", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean cambioPasswordRequerido = true;
    // --- FIN NUEVO CAMPO ---

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion; // Usar DATETIME(6) para precisión microsegundos si es necesario

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    // Relaciones (sin cambios)
    @OneToMany(mappedBy = "promotor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Festival> festivales = new HashSet<>();

    @OneToMany(mappedBy = "usuarioCajero", fetch = FetchType.LAZY)
    private Set<Recarga> recargasRealizadas = new HashSet<>();

    // Constructores
    public Usuario() {
    }

    // Getters y Setters (Incluir para el nuevo campo)
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

    // Getters y Setters para el nuevo campo
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

    // equals y hashCode (Basado solo en ID para consistencia con JPA)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Usuario usuario = (Usuario) o;
        // Si ambos IDs son null, no son iguales a menos que sea la misma instancia.
        // Si uno es null y el otro no, no son iguales.
        // Si ambos no son null, comparar IDs.
        if (idUsuario == null || usuario.idUsuario == null) {
            return false;
        }
        return Objects.equals(idUsuario, usuario.idUsuario);
    }

    @Override
    public int hashCode() {
        // Usar ID si no es null, sino usar una constante (o la identidad de la clase)
        return Objects.hash(idUsuario); // Objects.hash maneja null
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
                + // Añadido
                '}';
    }
}
