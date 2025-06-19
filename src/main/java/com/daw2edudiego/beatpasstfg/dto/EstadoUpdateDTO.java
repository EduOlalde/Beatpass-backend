package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO genérico para recibir un nuevo estado (como String) en una petición de
 * actualización. Utilizado para estados de Usuario o Festival.
 */
public class EstadoUpdateDTO {

    @NotNull(message = "El nuevo estado es obligatorio.")
    @NotBlank(message = "El nuevo estado no puede estar vacío.")
    @Size(max = 50, message = "El estado no puede exceder los 50 caracteres.")
    private String nuevoEstado;

    public EstadoUpdateDTO() {
    }

    public String getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(String nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    @Override
    public String toString() {
        return "EstadoUpdateDTO{"
                + "nuevoEstado='" + nuevoEstado + '\''
                + '}';
    }
}
