package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa un evento de festival. Mapea la tabla
 * 'festivales'.
 */
@Entity
@Table(name = "festivales")
public class Festival implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_festival")
    private Integer idFestival;

    @NotBlank(message = "El nombre del festival no puede estar vacío.")
    @Size(max = 100, message = "El nombre del festival no puede exceder los 100 caracteres.")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "La fecha de inicio no puede ser nula.")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin no puede ser nula.")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres.")
    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    @Column(name = "aforo")
    private Integer aforo; // Podría validarse como @Positive

    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres.")
    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @NotNull(message = "El estado del festival no puede ser nulo.")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('BORRADOR', 'PUBLICADO', 'CANCELADO', 'FINALIZADO') DEFAULT 'BORRADOR'")
    private EstadoFestival estado = EstadoFestival.BORRADOR;

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    /**
     * El usuario (promotor) que organiza este festival. Relación muchos a uno.
     * FK 'id_promotor' no nula. Fetch LAZY.
     */
    @NotNull(message = "El festival debe estar asociado a un promotor.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_promotor", nullable = false)
    private Usuario promotor;

    /**
     * Tipos de entrada definidos para este festival. Relación uno a muchos.
     * Cascade ALL, Fetch LAZY, orphanRemoval true.
     */
    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Entrada> tiposEntrada = new HashSet<>();

    /**
     * Consumos realizados dentro de este festival. Relación uno a muchos.
     * Cascade ALL, Fetch LAZY, orphanRemoval true.
     */
    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Consumo> consumos = new HashSet<>();

    /**
     * Estadísticas agregadas para este festival (opcional). Relación uno a uno
     * (lado inverso). Cascade ALL, Fetch LAZY, optional=true, orphanRemoval
     * true.
     */
    @OneToOne(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true, orphanRemoval = true)
    private EstadisticasFestival estadisticas;

    public Festival() {
    }

    // --- Getters y Setters ---
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
        if (estadisticas != null) {
            estadisticas.setFestival(this); // Mantener bidireccionalidad
        }
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
        Festival festival = (Festival) o;
        return idFestival != null && Objects.equals(idFestival, festival.idFestival);
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
                + ", promotorId=" + (promotor != null ? promotor.getIdUsuario() : "null")
                + '}';
    }
}
