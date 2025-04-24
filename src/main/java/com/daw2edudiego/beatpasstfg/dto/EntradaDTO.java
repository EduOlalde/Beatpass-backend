package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.*; // Importaciones para Bean Validation
import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) para representar un tipo de entrada de un
 * {@link com.daw2edudiego.beatpasstfg.model.Festival}. Se utiliza para
 * transferir datos entre la capa de servicio y la capa web (API/JSP), y
 * especialmente para recibir y validar los datos al crear o actualizar tipos de
 * entrada.
 *
 * @author Eduardo Olalde
 */
public class EntradaDTO {

    /**
     * ID de la entrada. Será null al crear una nueva, y tendrá valor al mostrar
     * o actualizar.
     */
    private Integer idEntrada;

    /**
     * ID del festival al que pertenece esta entrada. Obligatorio.
     */
    @NotNull(message = "El ID del festival es obligatorio.")
    private Integer idFestival; // ID del festival al que pertenece

    /**
     * Nombre del tipo de entrada (ej: "General", "VIP"). Obligatorio.
     */
    @NotBlank(message = "El tipo de entrada no puede estar vacío.")
    @Size(max = 50, message = "El tipo de entrada no puede exceder los 50 caracteres.")
    private String tipo;

    /**
     * Descripción detallada del tipo de entrada (opcional).
     */
    private String descripcion;

    /**
     * Precio de la entrada. Obligatorio y no negativo.
     */
    @NotNull(message = "El precio no puede ser nulo.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo.")
    // @Digits limita el número de dígitos enteros y fraccionarios
    @Digits(integer = 6, fraction = 2, message = "Formato de precio inválido (máx 999999.99).")
    private BigDecimal precio;

    /**
     * Stock inicial o actual de la entrada. Obligatorio y no negativo.
     */
    @NotNull(message = "El stock no puede ser nulo.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    /**
     * Constructor por defecto (necesario para frameworks como Jackson/JAX-RS).
     */
    public EntradaDTO() {
    }

    // --- Getters y Setters ---
    public Integer getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(Integer idEntrada) {
        this.idEntrada = idEntrada;
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

    // --- toString (útil para debugging) ---
    @Override
    public String toString() {
        return "EntradaDTO{"
                + "idEntrada=" + idEntrada
                + ", idFestival=" + idFestival
                + ", tipo='" + tipo + '\''
                + ", precio=" + precio
                + ", stock=" + stock
                + '}';
    }

    // --- equals y hashCode (basado en idEntrada, útil si se usan en colecciones) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntradaDTO that = (EntradaDTO) o;
        // Compara por ID si ambos no son nulos
        return idEntrada != null && Objects.equals(idEntrada, that.idEntrada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEntrada);
    }
}
