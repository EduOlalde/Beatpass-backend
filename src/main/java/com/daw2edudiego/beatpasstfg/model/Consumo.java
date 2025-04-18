/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa un consumo (gasto) realizado con una pulsera NFC. Mapea la tabla
 * 'consumos'.
 */
@Entity
@Table(name = "consumos")
public class Consumo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consumo")
    private Integer idConsumo;

    @Lob
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "monto", nullable = false, precision = 8, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "id_punto_venta")
    private Integer idPuntoVenta; // Opcional

    // Relación: Muchos consumos pertenecen a una pulsera
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pulsera", nullable = false)
    private PulseraNFC pulseraNFC;

    // Relación: Muchos consumos ocurren en un festival
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_festival", nullable = false)
    private Festival festival;

    // Constructores
    public Consumo() {
    }

    // Getters y Setters
    public Integer getIdConsumo() {
        return idConsumo;
    }

    public void setIdConsumo(Integer idConsumo) {
        this.idConsumo = idConsumo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getIdPuntoVenta() {
        return idPuntoVenta;
    }

    public void setIdPuntoVenta(Integer idPuntoVenta) {
        this.idPuntoVenta = idPuntoVenta;
    }

    public PulseraNFC getPulseraNFC() {
        return pulseraNFC;
    }

    public void setPulseraNFC(PulseraNFC pulseraNFC) {
        this.pulseraNFC = pulseraNFC;
    }

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
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
        Consumo consumo = (Consumo) o;
        if (idConsumo == null) {
            return false;
        }
        return Objects.equals(idConsumo, consumo.idConsumo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idConsumo);
    }

    @Override
    public String toString() {
        return "Consumo{"
                + "idConsumo=" + idConsumo
                + ", descripcion='" + descripcion + '\''
                + ", monto=" + monto
                + ", fecha=" + fecha
                + ", pulseraId=" + (pulseraNFC != null ? pulseraNFC.getIdPulsera() : null)
                + ", festivalId=" + (festival != null ? festival.getIdFestival() : null)
                + '}';
    }
}
