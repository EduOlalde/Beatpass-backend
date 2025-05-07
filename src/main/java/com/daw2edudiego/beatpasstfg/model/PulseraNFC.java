package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
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
 * Entidad JPA que representa una pulsera física NFC. Mapea la tabla
 * 'pulseras_nfc'. Incluye relación directa con Festival.
 */
@Entity
@Table(name = "pulseras_nfc", uniqueConstraints = {
    @UniqueConstraint(columnNames = "codigo_uid", name = "uq_pulseranfc_codigouid"),
    @UniqueConstraint(columnNames = "id_entrada_asignada", name = "uq_pulseranfc_entradaasignada")
})
public class PulseraNFC implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pulsera")
    private Integer idPulsera;

    @NotBlank(message = "El código UID de la pulsera no puede estar vacío.")
    @Size(max = 100, message = "El código UID no puede exceder los 100 caracteres.")
    @Column(name = "codigo_uid", nullable = false, unique = true, length = 100)
    private String codigoUid;

    @NotNull(message = "El saldo no puede ser nulo.")
    @PositiveOrZero(message = "El saldo no puede ser negativo.")
    @Column(name = "saldo", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal saldo = BigDecimal.ZERO;

    @NotNull
    @Column(name = "activa", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activa = true;

    @Column(name = "fecha_alta", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "ultima_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime ultimaModificacion;

    /**
     * Fecha y hora en que la pulsera fue asociada a una entrada. Permite nulos
     * si la pulsera no está asociada.
     */
    @Column(name = "fecha_asociacion", nullable = true)
    private LocalDateTime fechaAsociacion;

    /**
     * Entrada a la que está asociada esta pulsera. Relación 1:1 (lado
     * propietario). FK 'id_entrada_asignada' única. Cascade limitado, Fetch
     * LAZY.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "id_entrada_asignada", unique = true)
    private EntradaAsignada entradaAsignada;

    /**
     * El festival al que pertenece esta pulsera. Relación muchos a uno. FK
     * 'id_festival' no nula. Fetch LAZY.
     */
    @NotNull(message = "La pulsera debe estar asociada a un festival.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_festival", nullable = false)
    private Festival festival;

    /**
     * Recargas realizadas en esta pulsera. Relación uno a muchos. Cascade ALL,
     * Fetch LAZY, orphanRemoval true.
     */
    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Recarga> recargas = new HashSet<>();

    /**
     * Consumos realizados con esta pulsera. Relación uno a muchos. Cascade ALL,
     * Fetch LAZY, orphanRemoval true.
     */
    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Consumo> consumos = new HashSet<>();

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

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public LocalDateTime getUltimaModificacion() {
        return ultimaModificacion;
    }

    public LocalDateTime getFechaAsociacion() {
        return fechaAsociacion;
    }

    public void setFechaAsociacion(LocalDateTime fechaAsociacion) {
        this.fechaAsociacion = fechaAsociacion;
    }

    public EntradaAsignada getEntradaAsignada() {
        return entradaAsignada;
    }

    public void setEntradaAsignada(EntradaAsignada entradaAsignada) {
        this.entradaAsignada = entradaAsignada;
    }

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
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

    // --- equals, hashCode y toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PulseraNFC that = (PulseraNFC) o;
        // Comparación robusta: por ID si ambos existen, sino por UID si ambos existen
        if (idPulsera != null && that.idPulsera != null) {
            return Objects.equals(idPulsera, that.idPulsera);
        }
        if (idPulsera == null && that.idPulsera == null && codigoUid != null && that.codigoUid != null) {
            return Objects.equals(codigoUid, that.codigoUid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        // Hash basado en ID si existe, sino en UID
        return Objects.hash(idPulsera != null ? idPulsera : codigoUid);
    }

    @Override
    public String toString() {
        return "PulseraNFC{"
                + "idPulsera=" + idPulsera
                + ", codigoUid='" + codigoUid + '\''
                + ", saldo=" + saldo
                + ", activa=" + activa
                + ", idEntradaAsignada=" + (entradaAsignada != null ? entradaAsignada.getIdEntradaAsignada() : "null")
                + ", idFestival=" + (festival != null ? festival.getIdFestival() : "null")
                + ", fechaAsociacion=" + fechaAsociacion
                + // <-- Añadido al toString
                '}';
    }
}
