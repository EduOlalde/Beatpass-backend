package com.daw2edudiego.beatpasstfg.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para transferir información de Asistentes. No incluye información
 * sensible si la hubiera.
 */
public class AsistenteDTO {

    private Integer idAsistente;
    private String nombre;
    private String email;
    private String telefono; // Opcional
    private LocalDateTime fechaCreacion; // Informativo

    // Constructor por defecto
    public AsistenteDTO() {
    }

    // Getters y Setters
    public Integer getIdAsistente() {
        return idAsistente;
    }

    public void setIdAsistente(Integer idAsistente) {
        this.idAsistente = idAsistente;
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

    // --- equals y hashCode basados en ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AsistenteDTO that = (AsistenteDTO) o;
        return Objects.equals(idAsistente, that.idAsistente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAsistente);
    }

    // --- toString ---
    @Override
    public String toString() {
        return "AsistenteDTO{"
                + "idAsistente=" + idAsistente
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
