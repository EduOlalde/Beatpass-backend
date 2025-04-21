package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción lanzada cuando no se encuentra una PulseraNFC específica.
 */
public class PulseraNFCNotFoundException extends RuntimeException {

    public PulseraNFCNotFoundException(String message) {
        super(message);
    }

    public PulseraNFCNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
