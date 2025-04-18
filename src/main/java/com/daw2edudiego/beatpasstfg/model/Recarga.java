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
 * Representa una recarga de saldo en una pulsera NFC. Mapea la tabla
 * 'recargas'.
 */
@Entity
@Table(name = "recargas")
public class Recarga implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recarga")
    private Integer idRecarga;

    @Column(name = "monto", nullable = false, precision = 8, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    // Relación: Muchas recargas pertenecen a una pulsera
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pulsera", nullable = false)
    private PulseraNFC pulseraNFC;

    // Relación: Una recarga puede ser realizada por un usuario (cajero)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cajero") // Permite NULL
    private Usuario usuarioCajero;

    // Constructores
    public Recarga() {
    }

    // Getters y Setters
    public Integer getIdRecarga() {
        return idRecarga;
    }

    public void setIdRecarga(Integer idRecarga) {
        this.idRecarga = idRecarga;
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

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public PulseraNFC getPulseraNFC() {
        return pulseraNFC;
    }

    public void setPulseraNFC(PulseraNFC pulseraNFC) {
        this.pulseraNFC = pulseraNFC;
    }

    public Usuario getUsuarioCajero() {
        return usuarioCajero;
    }

    public void setUsuarioCajero(Usuario usuarioCajero) {
        this.usuarioCajero = usuarioCajero;
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
        Recarga recarga = (Recarga) o;
        if (idRecarga == null) {
            return false;
        }
        return Objects.equals(idRecarga, recarga.idRecarga);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRecarga);
    }

    @Override
    public String toString() {
        return "Recarga{"
                + "idRecarga=" + idRecarga
                + ", monto=" + monto
                + ", fecha=" + fecha
                + ", pulseraId=" + (pulseraNFC != null ? pulseraNFC.getIdPulsera() : null)
                + '}';
    }
}
