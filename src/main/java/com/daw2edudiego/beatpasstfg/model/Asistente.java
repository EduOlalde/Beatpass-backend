/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa un asistente/cliente que compra entradas. Mapea la tabla
 * 'asistentes'.
 */
@Entity
@Table(name = "asistentes")
public class Asistente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistente")
    private Integer idAsistente;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "telefono", length = 20) // Opcional
    private String telefono;

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    // Relación inversa: Un asistente puede realizar muchas compras
    @OneToMany(mappedBy = "asistente", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Compra> compras = new HashSet<>();

    // Relación inversa: Un asistente puede tener muchas entradas asignadas a su nombre
    @OneToMany(mappedBy = "asistente", fetch = FetchType.LAZY) // No cascade all aquí, borrar asistente no borra entrada
    private Set<EntradaAsignada> entradasAsignadas = new HashSet<>();

    // Constructores
    public Asistente() {
    }

    // Getters y Setters
    public Integer getIdAsistente() {
        return idAsistente;
    }

    public void setIdAsistente(Integer idAsistente) {
        this.idAsistente = idAsistente;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public Set<Compra> getCompras() {
        return compras;
    }

    public void setCompras(Set<Compra> compras) {
        this.compras = compras;
    }

    public Set<EntradaAsignada> getEntradasAsignadas() {
        return entradasAsignadas;
    }

    public void setEntradasAsignadas(Set<EntradaAsignada> entradasAsignadas) {
        this.entradasAsignadas = entradasAsignadas;
    }

    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Asistente asistente = (Asistente) o;
        if (idAsistente == null) {
            return false;
        }
        return Objects.equals(idAsistente, asistente.idAsistente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAsistente);
    }

    @Override
    public String toString() {
        return "Asistente{"
                + "idAsistente=" + idAsistente
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
