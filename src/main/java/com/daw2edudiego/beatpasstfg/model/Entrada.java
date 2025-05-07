package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa un tipo de entrada disponible para un festival.
 * Mapea la tabla 'entradas'.
 */
@Entity
@Table(name = "entradas")
public class Entrada implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada")
    private Integer idEntrada;

    @NotBlank(message = "El tipo de entrada no puede estar vacío.")
    @Size(max = 50, message = "El tipo de entrada no puede exceder los 50 caracteres.")
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El precio de la entrada no puede ser nulo.")
    @PositiveOrZero(message = "El precio de la entrada debe ser positivo o cero.")
    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    @NotNull(message = "El stock no puede ser nulo.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    /**
     * El festival al que pertenece este tipo de entrada. Relación muchos a uno.
     * FK 'id_festival' no nula. Fetch LAZY.
     */
    @NotNull(message = "El tipo de entrada debe estar asociado a un festival.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_festival", nullable = false)
    private Festival festival;

    /**
     * Detalles de compra donde aparece este tipo de entrada. Relación uno a
     * muchos. Cascade ALL, Fetch LAZY, orphanRemoval true. ¡Precaución con
     * Cascade ALL!
     */
    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CompraEntrada> comprasDondeAparece = new HashSet<>();

    public Entrada() {
    }

    // --- Getters y Setters ---
    public Integer getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(Integer idEntrada) {
        this.idEntrada = idEntrada;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
    }

    public Set<CompraEntrada> getComprasDondeAparece() {
        return comprasDondeAparece;
    }

    public void setComprasDondeAparece(Set<CompraEntrada> comprasDondeAparece) {
        this.comprasDondeAparece = comprasDondeAparece;
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
        Entrada entrada = (Entrada) o;
        return idEntrada != null && Objects.equals(idEntrada, entrada.idEntrada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEntrada);
    }

    @Override
    public String toString() {
        return "Entrada{"
                + "idEntrada=" + idEntrada
                + ", tipo='" + tipo + '\''
                + ", precio=" + precio
                + ", stock=" + stock
                + ", festivalId=" + (festival != null ? festival.getIdFestival() : "null")
                + '}';
    }
}
