package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de asociación de una pulsera, recibiendo su código UID.
 */
public class AsociarPulseraRequestDTO {

    @NotBlank(message = "El código UID de la pulsera es obligatorio.")
    @Size(max = 100, message = "El código UID no puede exceder los 100 caracteres.")
    private String codigoUid;

    public AsociarPulseraRequestDTO() {
    }

    // Getter y Setter
    public String getCodigoUid() {
        return codigoUid;
    }

    public void setCodigoUid(String codigoUid) {
        this.codigoUid = codigoUid;
    }

    @Override
    public String toString() {
        return "AsociarPulseraRequestDTO{"
                + "codigoUid='" + codigoUid + '\''
                + '}';
    }
}
