package com.beatpass.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA que representa un consumo (gasto) realizado con una pulsera NFC
 * dentro de un festival específico. Mapea la tabla 'consumos'.
 */
@Entity
@Table(name = "consumos")
public class Consumo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consumo")
    private Integer idConsumo;

    @NotBlank(message = "La descripción del consumo no puede estar vacía.")
    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres.")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @NotNull(message = "El monto del consumo no puede ser nulo.")
    @Positive(message = "El monto del consumo debe ser positivo.")
    @Column(name = "monto", nullable = false, precision = 8, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "id_punto_venta")
    private Integer idPuntoVenta; // Opcional

    /**
     * La pulsera NFC con la que se realizó el consumo. Relación muchos a uno.
     * FK 'id_pulsera' no nula. Fetch LAZY.
     */
    @NotNull(message = "El consumo debe estar asociado a una pulsera.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pulsera", nullable = false)
    private PulseraNFC pulseraNFC;

    /**
     * El festival en el que ocurrió el consumo. Relación muchos a uno. FK
     * 'id_festival' no nula. Fetch LAZY.
     */
    @NotNull(message = "El consumo debe estar asociado a un festival.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_festival", nullable = false)
    private Festival festival;

    public Consumo() {
    }

    // --- Getters y Setters ---
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

    // --- equals, hashCode y toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Consumo consumo = (Consumo) o;
        return idConsumo != null && Objects.equals(idConsumo, consumo.idConsumo);
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
                + ", pulseraId=" + (pulseraNFC != null ? pulseraNFC.getIdPulsera() : "null")
                + ", festivalId=" + (festival != null ? festival.getIdFestival() : "null")
                + '}';
    }
}
