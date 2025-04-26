package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Validación
import jakarta.validation.constraints.NotNull; // Validación
import jakarta.validation.constraints.PositiveOrZero; // Validación
import jakarta.validation.constraints.Size; // Validación
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa una pulsera física con tecnología NFC. Se utiliza
 * para pagos cashless dentro del festival y potencialmente para control de
 * acceso. Mapea la tabla 'pulseras_nfc'. **Modificado para incluir relación
 * directa con Festival.**
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "pulseras_nfc", uniqueConstraints = {
    // Constraint de unicidad explícito para el código UID
    @UniqueConstraint(columnNames = "codigo_uid", name = "uq_pulseranfc_codigouid"),
    // Constraint de unicidad explícito para la relación 1:1 con EntradaAsignada
    @UniqueConstraint(columnNames = "id_entrada_asignada", name = "uq_pulseranfc_entradaasignada")
// Podría añadirse un constraint UNIQUE(codigo_uid, id_festival) si un UID
// pudiera reutilizarse entre festivales, pero la lógica actual asume UID globalmente único.
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

    // Relación 1:1 con EntradaAsignada (lado propietario)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "id_entrada_asignada", unique = true) // Columna FK y constraint UNIQUE
    private EntradaAsignada entradaAsignada;

    // *** NUEVA RELACIÓN: ManyToOne con Festival ***
    /**
     * El festival al que pertenece esta pulsera. Una pulsera está vinculada a
     * un único festival. Relación muchos a uno con Festival. La columna
     * 'id_festival' no puede ser nula. Fetch LAZY.
     */
    @NotNull(message = "La pulsera debe estar asociada a un festival.") // Validación a nivel de objeto
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional=false refuerza not null
    @JoinColumn(name = "id_festival", nullable = false) // Columna FK en la BD, NO NULA
    private Festival festival;
    // *** FIN NUEVA RELACIÓN ***

    // Relaciones OneToMany inversas
    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Recarga> recargas = new HashSet<>();

    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Consumo> consumos = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public PulseraNFC() {
    }

    // --- Getters y Setters (Incluyendo el nuevo para festival) ---
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

    public EntradaAsignada getEntradaAsignada() {
        return entradaAsignada;
    }

    public void setEntradaAsignada(EntradaAsignada entradaAsignada) {
        this.entradaAsignada = entradaAsignada;
    }

    /**
     * Obtiene el festival al que pertenece esta pulsera.
     *
     * @return El objeto Festival asociado.
     */
    public Festival getFestival() {
        return festival;
    }

    /**
     * Establece el festival al que pertenece esta pulsera.
     *
     * @param festival El objeto Festival a asociar.
     */
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

    // --- equals, hashCode y toString (Actualizado toString) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PulseraNFC that = (PulseraNFC) o;
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
                + // Añadido ID Festival
                '}';
    }
}
