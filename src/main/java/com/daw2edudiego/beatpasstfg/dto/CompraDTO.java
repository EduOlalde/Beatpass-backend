package com.daw2edudiego.beatpasstfg.dto;

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

    // Información del Asistente/Comprador
    private Integer idAsistente;
    private String nombreAsistente;
    private String emailAsistente;

    // Información resumida de las entradas compradas
    private List<String> resumenEntradas; // Ej: ["2 x General", "1 x VIP"]

    /**
     * Lista de DTOs de las entradas individuales generadas para esta compra. Se
     * poblará solo en la respuesta de confirmación.
     */
    private List<EntradaAsignadaDTO> entradasGeneradas;

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

    public List<EntradaAsignadaDTO> getEntradasGeneradas() {
        return entradasGeneradas;
    }

    public void setEntradasGeneradas(List<EntradaAsignadaDTO> entradasGeneradas) {
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
                + ", idAsistente=" + idAsistente
                + ", nombreAsistente='" + nombreAsistente + '\''
                + ", emailAsistente='" + emailAsistente + '\''
                + ", resumenEntradas=" + resumenEntradas
                + ", entradasGeneradasCount=" + (entradasGeneradas != null ? entradasGeneradas.size() : 0)
                + ", stripePaymentIntentId='" + stripePaymentIntentId + '\''
                + ", estadoPago='" + estadoPago + '\''
                + ", fechaPagoConfirmado=" + fechaPagoConfirmado
                + '}';
    }
}
