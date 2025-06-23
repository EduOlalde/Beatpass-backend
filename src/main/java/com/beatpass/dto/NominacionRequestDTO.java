package com.beatpass.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de nominación de una entrada, recibiendo datos del
 * asistente.
 */
public class NominacionRequestDTO {

    @NotBlank(message = "El email del asistente es obligatorio.")
    @Email(message = "El formato del email del asistente no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String emailAsistente;

    @NotBlank(message = "El nombre del asistente es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombreAsistente;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefonoAsistente; // Opcional

    @NotBlank(message = "La confirmación del email es obligatoria.")
    @Email(message = "El formato del email de confirmación no es válido.")
    @Size(max = 100, message = "El email de confirmación no puede exceder los 100 caracteres.")
    private String confirmEmailNominado; // <--- CAMBIO: Nuevo campo para la confirmación del email

    public NominacionRequestDTO() {
    }

    // Getters y Setters
    public String getEmailAsistente() {
        return emailAsistente;
    }

    public void setEmailAsistente(String emailAsistente) {
        this.emailAsistente = emailAsistente;
    }

    public String getNombreAsistente() {
        return nombreAsistente;
    }

    public void setNombreAsistente(String nombreAsistente) {
        this.nombreAsistente = nombreAsistente;
    }

    public String getTelefonoAsistente() {
        return telefonoAsistente;
    }

    public void setTelefonoAsistente(String telefonoAsistente) {
        this.telefonoAsistente = telefonoAsistente;
    }

    public String getConfirmEmailNominado() {
        return confirmEmailNominado;
    }

    public void setConfirmEmailNominado(String confirmEmailNominado) {
        this.confirmEmailNominado = confirmEmailNominado;
    }

    @Override
    public String toString() {
        return "NominacionRequestDTO{"
                + "emailAsistente='" + emailAsistente + '\''
                + ", nombreAsistente='" + nombreAsistente + '\''
                + ", telefonoAsistente='" + telefonoAsistente + '\''
                + ", confirmEmailNominado='" + confirmEmailNominado + '\''
                + '}';
    }
}
