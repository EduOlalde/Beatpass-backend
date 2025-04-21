package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción personalizada que se lanza cuando no se encuentra una
 * EntradaAsignada específica en la base de datos.
 */
public class EntradaAsignadaNotFoundException extends RuntimeException {

    public EntradaAsignadaNotFoundException(String message) {
        super(message);
    }

    public EntradaAsignadaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
