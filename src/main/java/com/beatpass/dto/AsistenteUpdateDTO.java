package com.beatpass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO específico para la actualización de datos de un Asistente.
 */
public class AsistenteUpdateDTO {

    @NotBlank(message = "El nombre del asistente no puede estar vacío.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefono;

    public AsistenteUpdateDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
