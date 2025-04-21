package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.*; // Importaciones para Bean Validation
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para representar un tipo de entrada de un
 * festival. Se utiliza para transferir datos entre la capa de servicio y la
 * capa web (API/JSP), y para validar los datos de entrada al crear o actualizar
 * tipos de entrada.
 * @author Eduardo Olalde
 */
public class EntradaDTO {

    private Integer idEntrada; // Identificador único (generado por la BD)

    @NotNull(message = "El ID del festival es obligatorio para asociar la entrada.")
    private Integer idFestival; // ID del festival al que pertenece

    @NotBlank(message = "El tipo de entrada no puede estar vacío.")
    @Size(max = 50, message = "El tipo de entrada no puede exceder los 50 caracteres.")
    private String tipo; // Ej: "General", "VIP", "Abono 3 días"

    private String descripcion; // Descripción detallada (opcional)

    @NotNull(message = "El precio no puede ser nulo.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo.")
    @Digits(integer = 6, fraction = 2, message = "Formato de precio inválido (máximo 6 enteros, 2 decimales).") // Ej: hasta 999999.99
    private BigDecimal precio; // Precio de este tipo de entrada

    @NotNull(message = "El stock no puede ser nulo.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock; // Número de entradas de este tipo disponibles

    // Constructor por defecto (necesario para frameworks como Jackson/JAX-RS)
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

    // --- equals y hashCode (opcional, basado en idEntrada si se usa en colecciones) ---
    // Podrían implementarse si se necesita comparar DTOs
}
