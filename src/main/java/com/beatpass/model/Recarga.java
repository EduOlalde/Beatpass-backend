package com.beatpass.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA que representa una operación de recarga de saldo en una
 * PulseraNFC. Mapea la tabla 'recargas'.
 */
@Entity
@Table(name = "recargas")
public class Recarga implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recarga")
    private Integer idRecarga;

    @NotNull(message = "El monto de la recarga no puede ser nulo.")
    @Positive(message = "El monto de la recarga debe ser positivo.")
    @Column(name = "monto", nullable = false, precision = 8, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fecha;

    @Size(max = 50, message = "El método de pago no puede exceder los 50 caracteres.")
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago; // Opcional

    /**
     * Pulsera a la que se aplicó la recarga. Relación muchos a uno. FK
     * 'id_pulsera' no nula. Fetch LAZY.
     */
    @NotNull(message = "La recarga debe estar asociada a una pulsera.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pulsera", nullable = false)
    private PulseraNFC pulseraNFC;

    /**
     * Usuario (cajero/operador) que realizó la recarga. Opcional. Relación
     * muchos a uno. FK 'id_usuario_cajero' permite nulos. Fetch LAZY.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cajero")
    private Usuario usuarioCajero;

    public Recarga() {
    }

    // --- Getters y Setters ---
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

    // --- equals, hashCode y toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Recarga recarga = (Recarga) o;
        return idRecarga != null && Objects.equals(idRecarga, recarga.idRecarga);
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
                + ", metodoPago='" + metodoPago + '\''
                + ", pulseraId=" + (pulseraNFC != null ? pulseraNFC.getIdPulsera() : "null")
                + ", cajeroId=" + (usuarioCajero != null ? usuarioCajero.getIdUsuario() : "null")
                + '}';
    }
}
