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
 * tabla 'pulseras_nfc'. ACTUALIZADO: Añadidos getters para fechaAlta y
 * ultimaModificacion.
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

    @Column(name = "fecha_alta", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "ultima_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime ultimaModificacion;

    // Relación: Una pulsera está asociada a una entrada asignada (la que le dio acceso)
    // Es OneToOne porque una entrada solo puede tener una pulsera y viceversa (en un momento dado)
    // Usamos LAZY para no cargar la entrada siempre. Nullable=true porque la pulsera puede existir antes de asociarse.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrada_asignada", unique = true) // unique=true refuerza la relación 1:1
    private EntradaAsignada entradaAsignada;

    // Relación: Una pulsera tiene muchas recargas
    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Recarga> recargas = new HashSet<>();

    // Relación: Una pulsera tiene muchos consumos
    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Consumo> consumos = new HashSet<>();

    // Constructores
    public PulseraNFC() {
    }

    // --- Getters y Setters ---
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

    // *** GETTERS AÑADIDOS ***
    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public LocalDateTime getUltimaModificacion() {
        return ultimaModificacion;
    }
    // ************************

    // Setter para fechas (generalmente no necesarios ya que los gestiona la BD)
    // public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
    // public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }
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
        // Si los IDs son nulos, solo son iguales si es la misma instancia.
        // Si uno es nulo, no son iguales. Si ambos no son nulos, comparar IDs.
        if (idPulsera == null || that.idPulsera == null) {
            // Podríamos comparar por UID si el ID es nulo, asumiendo que UID no es nulo
            if (this == o) {
                return true; // Misma instancia
            }
            if (idPulsera == null && that.idPulsera == null) {
                // Si ambos IDs son nulos, comparar por UID si no es nulo
                return Objects.equals(codigoUid, that.codigoUid);
            }
            return false; // Uno es nulo y el otro no
        }
        return Objects.equals(idPulsera, that.idPulsera);
    }

    @Override
    public int hashCode() {
        // Usar ID si no es nulo, sino UID si no es nulo, sino la identidad
        if (idPulsera != null) {
            return Objects.hash(idPulsera);
        } else if (codigoUid != null) {
            return Objects.hash(codigoUid);
        } else {
            return super.hashCode(); // O una constante
        }
    }

    @Override
    public String toString() {
        return "PulseraNFC{"
                + "idPulsera=" + idPulsera
                + ", codigoUid='" + codigoUid + '\''
                + ", saldo=" + saldo
                + ", activa=" + activa
                + ", idEntradaAsignada=" + (entradaAsignada != null ? entradaAsignada.getIdEntradaAsignada() : "null")
                + '}';
    }
}
