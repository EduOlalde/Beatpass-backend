package com.daw2edudiego.beatpasstfg.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para representar la información de un Comprador.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompradorDTO {

    private Integer idComprador;
    private String nombre;
    private String email;
    private String telefono;
    private LocalDateTime fechaCreacion;

    public CompradorDTO() {
    }

    // Getters y Setters
    public Integer getIdComprador() {
        return idComprador;
    }

    public void setIdComprador(Integer idComprador) {
        this.idComprador = idComprador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompradorDTO that = (CompradorDTO) o;
        return Objects.equals(idComprador, that.idComprador);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idComprador);
    }

    @Override
    public String toString() {
        return "CompradorDTO{"
                + "idComprador=" + idComprador
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
