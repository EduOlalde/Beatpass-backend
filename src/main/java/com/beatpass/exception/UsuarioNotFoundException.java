package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando no se encuentra un
 * {@link com.beatpass.model.Usuario} específico en el sistema,
 * generalmente al buscar por su ID o email.
 *
 * @see com.beatpass.model.Usuario
 * @see com.beatpass.service.UsuarioService
 */
public class UsuarioNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "Usuario con ID X no
     * encontrado.").
     */
    public UsuarioNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public UsuarioNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
