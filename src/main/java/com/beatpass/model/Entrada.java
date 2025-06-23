package com.beatpass.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA que representa una entrada individual generada a partir de una
 * compra. Mapea la tabla 'entradas'.
 */
@Entity
@Table(name = "entradas", uniqueConstraints = {
    @UniqueConstraint(columnNames = "codigo_qr", name = "uq_entradaasignada_codigoqr")
})
public class Entrada implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada")
    private Integer idEntrada;

    @NotBlank(message = "El código QR no puede estar vacío.")
    @Size(max = 255, message = "El código QR no puede exceder los 255 caracteres.")
    @Column(name = "codigo_qr", nullable = false, unique = true, length = 255)
    private String codigoQr;

    @NotNull(message = "El estado de la entrada no puede ser nulo.")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('ACTIVA', 'USADA', 'CANCELADA') DEFAULT 'ACTIVA'")
    private EstadoEntrada estado = EstadoEntrada.ACTIVA;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion; // Opcional

    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso; // Opcional

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    /**
     * Detalle de compra del que se generó esta entrada. Relación muchos a uno.
     * FK 'id_compra_entrada' no nula. Fetch LAZY.
     */
    @NotNull(message = "La entrada debe provenir de un detalle de compra.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compra_entrada", nullable = false)
    private CompraEntrada compraEntrada;

    /**
     * Asistente al que está nominada esta entrada. Opcional (null si no
     * nominada). Relación muchos a uno. FK 'id_asistente' permite nulos. Fetch
     * LAZY.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asistente")
    private Asistente asistente;

    /**
     * Pulsera NFC asociada a esta entrada. Relación uno a uno (lado inverso).
     * Fetch LAZY, Cascade limitado, optional=true.
     */
    @OneToOne(mappedBy = "entrada", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, optional = true)
    private PulseraNFC pulseraAsociada;

    public Entrada() {
    }

    // --- Getters y Setters ---
    public Integer getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(Integer idEntrada) {
        this.idEntrada = idEntrada;
    }

    public String getCodigoQr() {
        return codigoQr;
    }

    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }

    public EstadoEntrada getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntrada estado) {
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

    // --- equals, hashCode y toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entrada that = (Entrada) o;
        return idEntrada != null && Objects.equals(idEntrada, that.idEntrada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEntrada);
    }

    @Override
    public String toString() {
        return "Entrada{"
                + "idEntrada=" + idEntrada
                + ", codigoQr='" + codigoQr + '\''
                + ", estado=" + estado
                + ", asistenteId=" + (asistente != null ? asistente.getIdAsistente() : "null")
                + ", pulseraId=" + (pulseraAsociada != null ? pulseraAsociada.getIdPulsera() : "null")
                + '}';
    }
}
