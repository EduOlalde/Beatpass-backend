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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompraDTO {

    private Integer idCompra;
    private LocalDateTime fechaCompra;
    private BigDecimal total;

    // Información del Asistente/Comprador
    private Integer idAsistente;
    private String nombreAsistente;
    private String emailAsistente;

    // Información resumida de las entradas compradas en esta transacción
    // Podría ser una lista de Strings o una lista de DTOs más pequeños
    private List<String> resumenEntradas; // Ej: ["2 x General", "1 x VIP"]

    // Constructor por defecto
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
                + ", emailAsistente='" + emailAsistente + '\''
                + ", resumenEntradas=" + resumenEntradas
                + '}';
    }
}
