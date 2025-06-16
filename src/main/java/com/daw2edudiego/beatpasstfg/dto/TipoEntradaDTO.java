package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO para representar un tipo de entrada de un Festival.
 */
public class TipoEntradaDTO {

    private Integer idTipoEntrada;

    @NotNull(message = "El ID del festival es obligatorio.")
    private Integer idFestival;

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

    public TipoEntradaDTO() {
    }

    // --- Getters y Setters ---
    public Integer getIdTipoEntrada() {
        return idTipoEntrada;
    }

    public void setIdTipoEntrada(Integer idTipoEntrada) {
        this.idTipoEntrada = idTipoEntrada;
    }

    public Integer getIdFestival() {
        return idFestival;
    }

    public void setIdFestival(Integer idFestival) {
        this.idFestival = idFestival;
    }

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

    // --- toString ---
    @Override
    public String toString() {
        return "EntradaDTO{"
                + "idEntrada=" + idTipoEntrada
                + ", idFestival=" + idFestival
                + ", tipo='" + tipo + '\''
                + ", precio=" + precio
                + ", stock=" + stock
                + '}';
    }

    // --- equals y hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TipoEntradaDTO that = (TipoEntradaDTO) o;
        return idTipoEntrada != null && Objects.equals(idTipoEntrada, that.idTipoEntrada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTipoEntrada);
    }
}
