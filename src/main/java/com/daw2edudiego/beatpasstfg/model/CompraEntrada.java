/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa el detalle de una compra (qué tipo de entrada y cuántas). Mapea la
 * tabla 'compra_entradas'.
 */
@Entity
@Table(name = "compra_entradas")
public class CompraEntrada implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra_entrada")
    private Integer idCompraEntrada;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioUnitario; // Precio en el momento de la compra

    // Relación: Muchos detalles pertenecen a una compra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra compra;

    // Relación: Muchos detalles se refieren a un tipo de entrada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrada", nullable = false)
    private Entrada entrada; // El tipo de entrada comprado

    // Relación inversa: De este detalle de compra se generan varias entradas asignadas (individuales)
    @OneToMany(mappedBy = "compraEntrada", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<EntradaAsignada> entradasAsignadasGeneradas = new HashSet<>();

    // Constructores
    public CompraEntrada() {
    }

    // Getters y Setters
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

    public Entrada getEntrada() {
        return entrada;
    }

    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }

    public Set<EntradaAsignada> getEntradasAsignadasGeneradas() {
        return entradasAsignadasGeneradas;
    }

    public void setEntradasAsignadasGeneradas(Set<EntradaAsignada> entradasAsignadasGeneradas) {
        this.entradasAsignadasGeneradas = entradasAsignadasGeneradas;
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
        CompraEntrada that = (CompraEntrada) o;
        if (idCompraEntrada == null) {
            return false;
        }
        return Objects.equals(idCompraEntrada, that.idCompraEntrada);
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
                + ", compraId=" + (compra != null ? compra.getIdCompra() : null)
                + ", entradaId=" + (entrada != null ? entrada.getIdEntrada() : null)
                + '}';
    }
}
