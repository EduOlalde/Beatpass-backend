package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza al intentar realizar una
 * operación que vincularía una
 * {@link com.beatpass.model.PulseraNFC} a una
 * {@link com.beatpass.model.EntradaAsignada}, pero dicha
 * pulsera ya se encuentra asociada a otra entrada activa dentro del mismo
 * contexto relevante (normalmente, el mismo festival).
 *
 * @see
 * com.beatpass.service.PulseraNFCService#asociarPulseraEntrada(String,
 * Integer, Integer)
 */
public class PulseraYaAsociadaException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "La pulsera UID XXX ya
     * está asociada a otra entrada activa.").
     */
    public PulseraYaAsociadaException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public PulseraYaAsociadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
