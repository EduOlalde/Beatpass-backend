/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa una entrada individual, nominada y lista para usar/validar. Mapea
 * la tabla 'entradas_asignadas'.
 */
@Entity
@Table(name = "entradas_asignadas")
public class EntradaAsignada implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada_asignada")
    private Integer idEntradaAsignada;

    @Column(name = "codigo_qr", nullable = false, unique = true, length = 255)
    private String codigoQr;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "ENUM('ACTIVA', 'USADA', 'CANCELADA') DEFAULT 'ACTIVA'")
    private EstadoEntradaAsignada estado = EstadoEntradaAsignada.ACTIVA;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion; // Cuando se nominó

    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso; // Cuando se validó

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    // Relación: Muchas entradas asignadas provienen de un detalle de compra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra_entrada", nullable = false)
    private CompraEntrada compraEntrada;

    // Relación: Una entrada asignada pertenece a un asistente (puede ser null si no está nominada)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asistente") // Permite NULL
    private Asistente asistente;

    // Relación OneToOne con PulseraNFC (EntradaAsignada es el lado inverso)
    // Una entrada puede tener asociada una pulsera (o ninguna)
    // El cascade ALL aquí podría ser peligroso si no quieres borrar la pulsera al borrar la entrada
    @OneToOne(mappedBy = "entradaAsignada", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, optional = true)
    private PulseraNFC pulseraAsociada;

    // Constructores
    public EntradaAsignada() {
    }

    // Getters y Setters
    public Integer getIdEntradaAsignada() {
        return idEntradaAsignada;
    }

    public void setIdEntradaAsignada(Integer idEntradaAsignada) {
        this.idEntradaAsignada = idEntradaAsignada;
    }

    public String getCodigoQr() {
        return codigoQr;
    }

    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }

    public EstadoEntradaAsignada getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntradaAsignada estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    public void setFechaUso(LocalDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public CompraEntrada getCompraEntrada() {
        return compraEntrada;
    }

    public void setCompraEntrada(CompraEntrada compraEntrada) {
        this.compraEntrada = compraEntrada;
    }

    public Asistente getAsistente() {
        return asistente;
    }

    public void setAsistente(Asistente asistente) {
        this.asistente = asistente;
    }

    public PulseraNFC getPulseraAsociada() {
        return pulseraAsociada;
    }

    public void setPulseraAsociada(PulseraNFC pulseraAsociada) {
        this.pulseraAsociada = pulseraAsociada;
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
        EntradaAsignada that = (EntradaAsignada) o;
        if (idEntradaAsignada == null) {
            return false;
        }
        return Objects.equals(idEntradaAsignada, that.idEntradaAsignada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEntradaAsignada);
    }

    @Override
    public String toString() {
        return "EntradaAsignada{"
                + "idEntradaAsignada=" + idEntradaAsignada
                + ", codigoQr='" + codigoQr + '\''
                + ", estado=" + estado
                + ", asistenteId=" + (asistente != null ? asistente.getIdAsistente() : null)
                + '}';
    }
}
