package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando la contraseña
 * proporcionada por un usuario durante un proceso (ej: cambio de contraseña) no
 * coincide con la contraseña actual almacenada (hasheada) para ese usuario.
 *
 * @see
 * com.beatpass.service.UsuarioService#cambiarPassword(Integer,
 * String, String)
 * @author Eduardo Olalde
 */
public class PasswordIncorrectoException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "La contraseña actual
     * introducida es incorrecta.").
     */
    public PasswordIncorrectoException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public PasswordIncorrectoException(String message, Throwable cause) {
        super(message, cause);
    }
}
