package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza al intentar crear un
 * nuevo usuario ({@link com.beatpass.model.Usuario}) o
 * asistente ({@link com.beatpass.model.Asistente}) con una
 * dirección de correo electrónico que ya está registrada en el sistema,
 * violando la restricción de unicidad del email.
 *
 * @see
 * com.beatpasst.service.UsuarioService#crearUsuario(com.beatpassdto.UsuarioCreacionDTO)
 * @see
 * com.beatpass.service.AsistenteService#obtenerOcrearAsistentePorEmail(String,
 * String, String)
 */
public class EmailExistenteException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "El email
     * 'test@example.com' ya está registrado.").
     */
    public EmailExistenteException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta (ej:
     * PersistenceException por violación de constraint UNIQUE).
     */
    public EmailExistenteException(String message, Throwable cause) {
        super(message, cause);
    }
}
