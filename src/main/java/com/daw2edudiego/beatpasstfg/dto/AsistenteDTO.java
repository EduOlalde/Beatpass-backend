package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * DTO para representar la información de un Asistente. Incluye información
 * básica y un mapa opcional festival -> UID de pulsera.
 */
public class AsistenteDTO {

    private Integer idAsistente;

    @NotBlank(message = "El nombre del asistente no puede estar vacío.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    @NotBlank(message = "El email del asistente no puede estar vacío.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefono;

    private LocalDateTime fechaCreacion;

    /**
     * Mapa que relaciona nombres de festivales con los UIDs de las pulseras
     * asociadas para este asistente en dichos festivales. Clave: Nombre del
     * Festival (String) Valor: Código UID de la Pulsera (String) o null si no
     * hay pulsera.
     */
    private Map<String, String> festivalPulseraInfo;

    public AsistenteDTO() {
    }

    // --- Getters y Setters ---
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

    public Map<String, String> getFestivalPulseraInfo() {
        return festivalPulseraInfo;
    }

    public void setFestivalPulseraInfo(Map<String, String> festivalPulseraInfo) {
        this.festivalPulseraInfo = festivalPulseraInfo;
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
        AsistenteDTO that = (AsistenteDTO) o;
        return idAsistente != null && Objects.equals(idAsistente, that.idAsistente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAsistente);
    }

    @Override
    public String toString() {
        return "AsistenteDTO{"
                + "idAsistente=" + idAsistente
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + ", telefono='" + telefono + '\''
                + ", festivalPulseraInfo=" + festivalPulseraInfo
                + '}';
    }
}
