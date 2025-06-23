package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando no se encuentra
 * una entidad {@link com.beatpass.model.PulseraNFC} específica
 * en la base de datos, generalmente al buscar por su ID o código UID.
 *
 * @see com.beatpass.model.PulseraNFC
 * @see com.beatpass.service.PulseraNFCService
 */
public class PulseraNFCNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "Pulsera NFC con UID XXX
     * no encontrada.").
     */
    public PulseraNFCNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public PulseraNFCNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
