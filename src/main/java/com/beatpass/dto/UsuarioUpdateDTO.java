package com.beatpass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la actualización parcial de un usuario (ej. solo el nombre).
 */
public class UsuarioUpdateDTO {

    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    public UsuarioUpdateDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "UsuarioUpdateDTO{"
                + "nombre='" + nombre + '\''
                + '}';
    }
}
