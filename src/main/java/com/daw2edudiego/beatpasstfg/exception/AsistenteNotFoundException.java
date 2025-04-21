package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción personalizada que se lanza cuando no se encuentra un asistente
 * específico en la base de datos, generalmente al buscar por su ID o email.
 */
public class AsistenteNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "Asistente con ID X no
     * encontrado").
     */
    public AsistenteNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que causó esta.
     */
    public AsistenteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
