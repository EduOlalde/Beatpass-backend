package com.daw2edudiego.beatpasstfg.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; // Para detalles de entradas
import java.util.Objects;

/**
 * DTO (Data Transfer Object) para representar la información de una
 * {@link com.daw2edudiego.beatpasstfg.model.Compra} realizada. Se utiliza para
 * transferir datos de compras a las vistas o APIs.
 *
 * @author Eduardo Olalde
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // No incluir campos nulos en la respuesta JSON
public class CompraDTO {

    private Integer idCompra;
    private LocalDateTime fechaCompra;
    private BigDecimal total;

    // Información del Asistente/Comprador
    private Integer idAsistente;
    private String nombreAsistente;
    private String emailAsistente;

    // Información resumida de las entradas compradas en esta transacción
    private List<String> resumenEntradas; // Ej: ["2 x General", "1 x VIP"]

    // --- Campos de Pago (Stripe) ---
    /**
     * Identificador del PaymentIntent de Stripe asociado a esta compra. Será
     * null si la compra no usó Stripe o antes de la confirmación.
     */
    private String stripePaymentIntentId;

    /**
     * Estado del pago registrado en nuestro sistema (ej: "PENDIENTE", "PAGADO",
     * "FALLIDO"). Podría ser null si el estado no es relevante o conocido.
     */
    private String estadoPago;

    /**
     * Fecha y hora en que se confirmó el pago. Será null si el pago no está
     * confirmado.
     */
    private LocalDateTime fechaPagoConfirmado;

    // --- Fin Campos de Pago ---
    /**
     * Constructor por defecto.
     */
    public CompraDTO() {
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

    public Integer getIdAsistente() {
        return idAsistente;
    }

    public void setIdAsistente(Integer idAsistente) {
        this.idAsistente = idAsistente;
    }

    public String getNombreAsistente() {
        return nombreAsistente;
    }

    public void setNombreAsistente(String nombreAsistente) {
        this.nombreAsistente = nombreAsistente;
    }

    public String getEmailAsistente() {
        return emailAsistente;
    }

    public void setEmailAsistente(String emailAsistente) {
        this.emailAsistente = emailAsistente;
    }

    public List<String> getResumenEntradas() {
        return resumenEntradas;
    }

    public void setResumenEntradas(List<String> resumenEntradas) {
        this.resumenEntradas = resumenEntradas;
    }

    // --- Getters y Setters para campos de Stripe ---
    /**
     * Obtiene el ID del PaymentIntent de Stripe asociado.
     *
     * @return El ID del PaymentIntent o null.
     */
    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    /**
     * Establece el ID del PaymentIntent de Stripe asociado.
     *
     * @param stripePaymentIntentId El ID del PaymentIntent.
     */
    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    /**
     * Obtiene el estado del pago registrado localmente.
     *
     * @return El estado del pago (ej: "PAGADO") o null.
     */
    public String getEstadoPago() {
        return estadoPago;
    }

    /**
     * Establece el estado del pago registrado localmente.
     *
     * @param estadoPago El estado del pago.
     */
    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    /**
     * Obtiene la fecha y hora en que se confirmó el pago.
     *
     * @return La fecha y hora de confirmación o null.
     */
    public LocalDateTime getFechaPagoConfirmado() {
        return fechaPagoConfirmado;
    }

    /**
     * Establece la fecha y hora en que se confirmó el pago.
     *
     * @param fechaPagoConfirmado La fecha y hora de confirmación.
     */
    public void setFechaPagoConfirmado(LocalDateTime fechaPagoConfirmado) {
        this.fechaPagoConfirmado = fechaPagoConfirmado;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara este CompraDTO con otro objeto. La igualdad se basa únicamente en
     * idCompra.
     *
     * @param o El objeto a comparar.
     * @return true si los IDs son iguales, false en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompraDTO compraDTO = (CompraDTO) o;
        // La igualdad se basa solo en el ID si está presente
        return Objects.equals(idCompra, compraDTO.idCompra);
    }

    /**
     * Calcula el código hash basado únicamente en idCompra.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        // Basar el hash solo en el ID si está presente
        return Objects.hash(idCompra);
    }

    /**
     * Devuelve una representación en cadena del DTO, incluyendo los campos de
     * pago.
     *
     * @return Una cadena representando el CompraDTO.
     */
    @Override
    public String toString() {
        return "CompraDTO{"
                + "idCompra=" + idCompra
                + ", fechaCompra=" + fechaCompra
                + ", total=" + total
                + ", idAsistente=" + idAsistente
                + ", nombreAsistente='" + nombreAsistente + '\''
                + ", emailAsistente='" + emailAsistente + '\''
                + ", resumenEntradas=" + resumenEntradas
                + ", stripePaymentIntentId='" + stripePaymentIntentId + '\''
                + ", estadoPago='" + estadoPago + '\''
                + ", fechaPagoConfirmado=" + fechaPagoConfirmado
                + '}';
    }
}
