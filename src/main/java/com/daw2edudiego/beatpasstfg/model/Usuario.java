/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa un usuario del sistema (Administrador o Promotor). Mapea la tabla
 * 'usuarios'.
 */
@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password; // Almacenar siempre el hash

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolUsuario rol;

    @Column(name = "estado", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean estado = true;

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    // Relación inversa: Un promotor puede tener muchos festivales
    @OneToMany(mappedBy = "promotor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Festival> festivales = new HashSet<>();

    // Relación inversa: Un cajero puede realizar muchas recargas (opcional)
    @OneToMany(mappedBy = "usuarioCajero", fetch = FetchType.LAZY)
    private Set<Recarga> recargasRealizadas = new HashSet<>();

    // Constructores
    public Usuario() {
    }

    // Getters y Setters
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

    // equals y hashCode (basados en el ID)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Usuario usuario = (Usuario) o;
        // Si el ID es null, los objetos no son iguales a menos que sean la misma instancia
        if (idUsuario == null) {
            return false;
        }
        return Objects.equals(idUsuario, usuario.idUsuario);
    }

    @Override
    public int hashCode() {
        // Usar un valor constante si el ID es null, o el hash del ID si no lo es
        return Objects.hash(idUsuario); // Simplificado, Objects.hash maneja null
        // Alternativa si idUsuario nunca fuera null tras persistencia: return idUsuario.hashCode();
        // Alternativa más robusta si id puede ser null: return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Usuario{"
                + "idUsuario=" + idUsuario
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + ", rol=" + rol
                + ", estado=" + estado
                + '}';
    }
}
