package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción lanzada al intentar asociar una pulsera que ya está vinculada a
 * otra entrada activa en el mismo contexto (ej: festival).
 */
public class PulseraYaAsociadaException extends RuntimeException {

    public PulseraYaAsociadaException(String message) {
        super(message);
    }

    public PulseraYaAsociadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
