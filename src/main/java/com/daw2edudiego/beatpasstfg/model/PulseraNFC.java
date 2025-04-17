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
 * Representa una pulsera NFC para cashless y/o control de acceso. Mapea la
 * tabla 'pulseras_nfc'.
 */
@Entity
@Table(name = "pulseras_nfc")
public class PulseraNFC implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pulsera")
    private Integer idPulsera;

    @Column(name = "codigo_uid", nullable = false, unique = true, length = 100)
    private String codigoUid; // UID leído del chip NFC

    @Column(name = "saldo", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "activa", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activa = true;

    @Column(name = "fecha_asociacion")
    private LocalDateTime fechaAsociacion; // Cuando se vinculó a la entrada

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    // Relación OneToOne: Una pulsera está asociada a una entrada asignada (o a ninguna si aún no se linkó)
    // PulseraNFC es el lado propietario de la relación OneToOne
    @OneToOne(fetch = FetchType.LAZY, optional = true) // optional=true porque id_entrada_asignada permite NULL
    @JoinColumn(name = "id_entrada_asignada", unique = true) // unique=true asegura que una entrada solo tenga una pulsera
    private EntradaAsignada entradaAsignada;

    // Relación inversa: Una pulsera puede tener muchas recargas
    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Recarga> recargas = new HashSet<>();

    // Relación inversa: Una pulsera puede tener muchos consumos
    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Consumo> consumos = new HashSet<>();

    // Constructores
    public PulseraNFC() {
    }

    // Getters y Setters
    public Integer getIdPulsera() {
        return idPulsera;
    }

    public void setIdPulsera(Integer idPulsera) {
        this.idPulsera = idPulsera;
    }

    public String getCodigoUid() {
        return codigoUid;
    }

    public void setCodigoUid(String codigoUid) {
        this.codigoUid = codigoUid;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechaAsociacion() {
        return fechaAsociacion;
    }

    public void setFechaAsociacion(LocalDateTime fechaAsociacion) {
        this.fechaAsociacion = fechaAsociacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public EntradaAsignada getEntradaAsignada() {
        return entradaAsignada;
    }

    public void setEntradaAsignada(EntradaAsignada entradaAsignada) {
        this.entradaAsignada = entradaAsignada;
    }

    public Set<Recarga> getRecargas() {
        return recargas;
    }

    public void setRecargas(Set<Recarga> recargas) {
        this.recargas = recargas;
    }

    public Set<Consumo> getConsumos() {
        return consumos;
    }

    public void setConsumos(Set<Consumo> consumos) {
        this.consumos = consumos;
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
        PulseraNFC that = (PulseraNFC) o;
        if (idPulsera == null) {
            return false;
        }
        return Objects.equals(idPulsera, that.idPulsera);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPulsera);
    }

    @Override
    public String toString() {
        return "PulseraNFC{"
                + "idPulsera=" + idPulsera
                + ", codigoUid='" + codigoUid + '\''
                + ", saldo=" + saldo
                + ", activa=" + activa
                + ", entradaAsignadaId=" + (entradaAsignada != null ? entradaAsignada.getIdEntradaAsignada() : null)
                + '}';
    }
}
