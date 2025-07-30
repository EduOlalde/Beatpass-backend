package com.beatpass.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO específico para la actualización de un Tipo de Entrada.
 */
public class TipoEntradaUpdateDTO {

    @NotBlank(message = "El tipo de entrada no puede estar vacío.")
    @Size(max = 50, message = "El tipo de entrada no puede exceder los 50 caracteres.")
    private String tipo;

    private String descripcion;

    @NotNull(message = "El precio no puede ser nulo.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo.")
    @Digits(integer = 6, fraction = 2, message = "Formato de precio inválido (máx 999999.99).")
    private BigDecimal precio;

    @NotNull(message = "El stock no puede ser nulo.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    private Boolean requiereNominacion;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getRequiereNominacion() {
        return requiereNominacion;
    }

    public void setRequiereNominacion(Boolean requiereNominacion) {
        this.requiereNominacion = requiereNominacion;
    }
}
