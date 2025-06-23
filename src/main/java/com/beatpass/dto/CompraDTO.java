package com.beatpass.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * DTO para representar la información de una Compra realizada.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompraDTO {

    private Integer idCompra;
    private LocalDateTime fechaCompra;
    private BigDecimal total;

    // Información del Comprador
    private Integer idComprador;
    private String nombreComprador;
    private String emailComprador;

    // Información resumida de las entradas compradas
    private List<String> resumenEntradas; // Ej: ["2 x General", "1 x VIP"]

    /**
     * Lista de DTOs de las entradas individuales generadas para esta compra. Se
     * poblará solo en la respuesta de confirmación.
     */
    private List<EntradaDTO> entradasGeneradas;

    // --- Campos de Pago (Stripe) ---
    private String stripePaymentIntentId;
    private String estadoPago; // Ej: "PAGADO"
    private LocalDateTime fechaPagoConfirmado;

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

    public Integer getIdComprador() {
        return idComprador;
    }

    public void setIdComprador(Integer idComprador) {
        this.idComprador = idComprador;
    }

    public String getNombreComprador() {
        return nombreComprador;
    }

    public void setNombreComprador(String nombreComprador) {
        this.nombreComprador = nombreComprador;
    }

    public String getEmailComprador() {
        return emailComprador;
    }

    public void setEmailComprador(String emailComprador) {
        this.emailComprador = emailComprador;
    }

    public List<String> getResumenEntradas() {
        return resumenEntradas;
    }

    public void setResumenEntradas(List<String> resumenEntradas) {
        this.resumenEntradas = resumenEntradas;
    }

    public List<EntradaDTO> getEntradasGeneradas() {
        return entradasGeneradas;
    }

    public void setEntradasGeneradas(List<EntradaDTO> entradasGeneradas) {
        this.entradasGeneradas = entradasGeneradas;
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
        CompraDTO compraDTO = (CompraDTO) o;
        return Objects.equals(idCompra, compraDTO.idCompra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCompra);
    }

    @Override
    public String toString() {
        return "CompraDTO{"
                + "idCompra=" + idCompra
                + ", fechaCompra=" + fechaCompra
                + ", total=" + total
                + ", idComprador=" + idComprador
                + ", nombreComprador='" + nombreComprador + '\''
                + ", emailComprador='" + emailComprador + '\''
                + ", resumenEntradas=" + resumenEntradas
                + ", entradasGeneradasCount=" + (entradasGeneradas != null ? entradasGeneradas.size() : 0)
                + ", stripePaymentIntentId='" + stripePaymentIntentId + '\''
                + ", estadoPago='" + estadoPago + '\''
                + ", fechaPagoConfirmado=" + fechaPagoConfirmado
                + '}';
    }
}
