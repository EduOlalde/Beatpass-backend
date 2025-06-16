package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa una línea de detalle dentro de una compra. Mapea
 * la tabla 'compra_entradas'.
 */
@Entity
@Table(name = "compra_entradas")
public class CompraEntrada implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra_entrada")
    private Integer idCompraEntrada;

    @NotNull(message = "La cantidad no puede ser nula.")
    @Min(value = 1, message = "La cantidad debe ser al menos 1.")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio unitario no puede ser nulo.")
    @Positive(message = "El precio unitario debe ser positivo.") // Permite 0? Debería ser >0? Script dice >=0, annotation dice >0
    @Column(name = "precio_unitario", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * La compra a la que pertenece este detalle. Relación muchos a uno. FK
     * 'id_compra' no nula. Fetch LAZY.
     */
    @NotNull(message = "El detalle de compra debe estar asociado a una compra.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra compra;

    /**
     * El tipo de entrada que se compró en este detalle. Relación muchos a uno.
     * FK 'id_tipo_entrada' no nula. Fetch LAZY.
     */
    @NotNull(message = "El detalle de compra debe estar asociado a un tipo de entrada.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_entrada", nullable = false)
    private TipoEntrada tipoEntrada;

    /**
     * Entradas individuales generadas a partir de este detalle. Relación uno a
     * muchos. Cascade ALL, Fetch LAZY, orphanRemoval true.
     */
    @OneToMany(mappedBy = "compraEntrada", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Entrada> entradasGeneradas = new HashSet<>();

    public CompraEntrada() {
    }

    // --- Getters y Setters ---
    public Integer getIdCompraEntrada() {
        return idCompraEntrada;
    }

    public void setIdCompraEntrada(Integer idCompraEntrada) {
        this.idCompraEntrada = idCompraEntrada;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public TipoEntrada getEntrada() {
        return tipoEntrada;
    }

    public void setEntrada(TipoEntrada tipoEntrada) {
        this.tipoEntrada = tipoEntrada;
    }

    public Set<Entrada> getEntradasAsignadasGeneradas() {
        return entradasGeneradas;
    }

    public void setEntradasAsignadasGeneradas(Set<Entrada> entradasGeneradas) {
        this.entradasGeneradas = entradasGeneradas;
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
        CompraEntrada that = (CompraEntrada) o;
        return idCompraEntrada != null && Objects.equals(idCompraEntrada, that.idCompraEntrada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCompraEntrada);
    }

    @Override
    public String toString() {
        return "CompraEntrada{"
                + "idCompraEntrada=" + idCompraEntrada
                + ", cantidad=" + cantidad
                + ", precioUnitario=" + precioUnitario
                + ", compraId=" + (compra != null ? compra.getIdCompra() : "null")
                + ", entradaId=" + (tipoEntrada != null ? tipoEntrada.getIdTipoEntrada() : "null")
                + '}';
    }
}
