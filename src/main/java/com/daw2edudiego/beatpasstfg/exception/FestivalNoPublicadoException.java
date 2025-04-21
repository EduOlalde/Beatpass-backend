package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación (ej: venta de
 * entradas) sobre un festival que no se encuentra en estado PUBLICADO.
 */
public class FestivalNoPublicadoException extends RuntimeException {

    public FestivalNoPublicadoException(String message) {
        super(message);
    }

    public FestivalNoPublicadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
