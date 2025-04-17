/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa un evento de festival. Mapea la tabla 'festivales'.
 */
@Entity
@Table(name = "festivales")
public class Festival implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_festival")
    private Integer idFestival;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Lob // Para campos TEXT largos
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    @Column(name = "aforo")
    private Integer aforo;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "ENUM('BORRADOR', 'PUBLICADO', 'CANCELADO', 'FINALIZADO') DEFAULT 'BORRADOR'")
    private EstadoFestival estado = EstadoFestival.BORRADOR;

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    // Relación: Muchos festivales pertenecen a un promotor (Usuario)
    @ManyToOne(fetch = FetchType.LAZY) // LAZY para no cargar el promotor siempre
    @JoinColumn(name = "id_promotor", nullable = false)
    private Usuario promotor;

    // Relación inversa: Un festival tiene muchos tipos de entradas
    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Entrada> tiposEntrada = new HashSet<>();

    // Relación inversa: En un festival se pueden realizar muchos consumos
    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Consumo> consumos = new HashSet<>();

    // Relación OneToOne con EstadisticasFestival (Festival es el propietario de la relación inversa)
    @OneToOne(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private EstadisticasFestival estadisticas;

    // Constructores
    public Festival() {
    }

    // Getters y Setters
    public Integer getIdFestival() {
        return idFestival;
    }

    public void setIdFestival(Integer idFestival) {
        this.idFestival = idFestival;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getAforo() {
        return aforo;
    }

    public void setAforo(Integer aforo) {
        this.aforo = aforo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public EstadoFestival getEstado() {
        return estado;
    }

    public void setEstado(EstadoFestival estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public Usuario getPromotor() {
        return promotor;
    }

    public void setPromotor(Usuario promotor) {
        this.promotor = promotor;
    }

    public Set<Entrada> getTiposEntrada() {
        return tiposEntrada;
    }

    public void setTiposEntrada(Set<Entrada> tiposEntrada) {
        this.tiposEntrada = tiposEntrada;
    }

    public Set<Consumo> getConsumos() {
        return consumos;
    }

    public void setConsumos(Set<Consumo> consumos) {
        this.consumos = consumos;
    }

    public EstadisticasFestival getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(EstadisticasFestival estadisticas) {
        this.estadisticas = estadisticas;
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
        Festival festival = (Festival) o;
        if (idFestival == null) {
            return false;
        }
        return Objects.equals(idFestival, festival.idFestival);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFestival);
    }

    @Override
    public String toString() {
        return "Festival{"
                + "idFestival=" + idFestival
                + ", nombre='" + nombre + '\''
                + ", fechaInicio=" + fechaInicio
                + ", fechaFin=" + fechaFin
                + ", estado=" + estado
                + '}';
    }
}
