package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción lanzada al intentar realizar una operación (ej: asociar pulsera)
 * sobre una EntradaAsignada que aún no ha sido nominada a un Asistente.
 */
public class EntradaAsignadaNoNominadaException extends RuntimeException {

    public EntradaAsignadaNoNominadaException(String message) {
        super(message);
    }

    public EntradaAsignadaNoNominadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
