package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa la cabecera de una compra de entradas. Mapea la
 * tabla 'compras'.
 */
@Entity
@Table(name = "compras")
public class Compra implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Integer idCompra;

    @Column(name = "fecha_compra", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCompra;

    @NotNull(message = "El total de la compra no puede ser nulo.")
    @PositiveOrZero(message = "El total de la compra debe ser positivo o cero.")
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * El asistente que realizó la compra. Relación muchos a uno. FK
     * 'id_asistente' no nula. Fetch LAZY.
     */
    @NotNull(message = "La compra debe estar asociada a un asistente.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asistente", nullable = false)
    private Asistente asistente;

    /**
     * Detalles de esta compra (qué entradas y cuántas). Relación uno a muchos.
     * Cascade ALL, Fetch LAZY, orphanRemoval true.
     */
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CompraEntrada> detallesCompra = new HashSet<>();

    /**
     * Identificador del PaymentIntent de Stripe asociado. Opcional y único.
     */
    @Column(name = "stripe_payment_intent_id", length = 255, unique = true, nullable = true)
    private String stripePaymentIntentId;

    /**
     * Estado del pago registrado localmente (ej: "PAGADO"). Opcional.
     */
    @Column(name = "estado_pago", length = 50, nullable = true)
    private String estadoPago;

    /**
     * Fecha y hora en que se confirmó el pago. Opcional.
     */
    @Column(name = "fecha_pago_confirmado", nullable = true)
    private LocalDateTime fechaPagoConfirmado;

    public Compra() {
    }

    // --- Getters y Setters ---
    public Integer getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Integer idCompra) {
        this.idCompra = idCompra;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Asistente getAsistente() {
        return asistente;
    }

    public void setAsistente(Asistente asistente) {
        this.asistente = asistente;
    }

    public Set<CompraEntrada> getDetallesCompra() {
        return detallesCompra;
    }

    public void setDetallesCompra(Set<CompraEntrada> detallesCompra) {
        this.detallesCompra = detallesCompra;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public LocalDateTime getFechaPagoConfirmado() {
        return fechaPagoConfirmado;
    }

    public void setFechaPagoConfirmado(LocalDateTime fechaPagoConfirmado) {
        this.fechaPagoConfirmado = fechaPagoConfirmado;
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
        Compra compra = (Compra) o;
        return idCompra != null && Objects.equals(idCompra, compra.idCompra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCompra);
    }

    @Override
    public String toString() {
        return "Compra{"
                + "idCompra=" + idCompra
                + ", fechaCompra=" + fechaCompra
                + ", total=" + total
                + ", asistenteId=" + (asistente != null ? asistente.getIdAsistente() : "null")
                + ", stripePaymentIntentId='" + stripePaymentIntentId + '\''
                + ", estadoPago='" + estadoPago + '\''
                + '}';
    }
}
