package com.beatpass.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

/**
 * DTO para la solicitud de confirmación de compra con pago, recibiendo todos
 * los detalles del comprador y el pago.
 */
public class ConfirmarCompraRequestDTO {

    @NotNull(message = "El ID de la entrada es obligatorio.")
    private Integer idEntrada;

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad debe ser al menos 1.")
    private Integer cantidad;

    @NotBlank(message = "El email del comprador es obligatorio.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String emailComprador;

    @NotBlank(message = "El nombre del comprador es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombreComprador;

    @Size(max = 20, message = "El teléfono del comprador no puede exceder los 20 caracteres.")
    private String telefonoComprador; // Opcional

    @NotBlank(message = "El ID del Payment Intent es obligatorio.")
    private String paymentIntentId;

    public ConfirmarCompraRequestDTO() {
    }

    // Getters y Setters
    public Integer getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(Integer idEntrada) {
        this.idEntrada = idEntrada;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getEmailComprador() {
        return emailComprador;
    }

    public void setEmailComprador(String emailComprador) {
        this.emailComprador = emailComprador;
    }

    public String getNombreComprador() {
        return nombreComprador;
    }

    public void setNombreComprador(String nombreComprador) {
        this.nombreComprador = nombreComprador;
    }

    public String getTelefonoComprador() {
        return telefonoComprador;
    }

    public void setTelefonoComprador(String telefonoComprador) {
        this.telefonoComprador = telefonoComprador;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    @Override
    public String toString() {
        return "ConfirmarCompraRequestDTO{"
                + "idEntrada=" + idEntrada
                + ", cantidad=" + cantidad
                + ", emailComprador='" + emailComprador + '\''
                + ", nombreComprador='" + nombreComprador + '\''
                + ", telefonoComprador='" + telefonoComprador + '\''
                + ", paymentIntentId='" + paymentIntentId + '\''
                + '}';
    }
}
