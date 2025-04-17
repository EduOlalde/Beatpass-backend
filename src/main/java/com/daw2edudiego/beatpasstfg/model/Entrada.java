/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa un tipo de entrada para un festival (ej: General, VIP). Mapea la
 * tabla 'entradas'.
 */
@Entity
@Table(name = "entradas")
public class Entrada implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada")
    private Integer idEntrada;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Lob
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    @Column(name = "stock", nullable = false)
    private Integer stock; // Stock inicial

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    // Relación: Muchas entradas pertenecen a un festival
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_festival", nullable = false)
    private Festival festival;

    // Relación inversa: Un tipo de entrada puede estar en muchos detalles de compra
    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CompraEntrada> comprasDondeAparece = new HashSet<>();

    // Constructores
    public Entrada() {
    }

    // Getters y Setters
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

    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entrada entrada = (Entrada) o;
        if (idEntrada == null) {
            return false;
        }
        return Objects.equals(idEntrada, entrada.idEntrada);
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
                + ", festivalId=" + (festival != null ? festival.getIdFestival() : null)
                + '}';
    }
}
