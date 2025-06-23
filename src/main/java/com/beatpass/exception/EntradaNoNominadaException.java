package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando se intenta
 * realizar una operación que requiere que una
 * {@link com.daw2edudiego.beatpasstfg.model.EntradaAsignada} esté nominada a un
 * {@link com.daw2edudiego.beatpasstfg.model.Asistente}, pero la entrada aún no
 * ha sido nominada.
 * <p>
 * Un ejemplo típico es intentar asociar una pulsera NFC a una entrada que no
 * tiene un asistente asignado.
 * </p>
 *
 * @see
 * com.daw2edudiego.beatpasstfg.service.PulseraNFCService#asociarPulseraEntrada(String,
 * Integer, Integer)
 */
public class EntradaNoNominadaException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "La entrada ID X debe
     * estar nominada para asociar una pulsera.").
     */
    public EntradaNoNominadaException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public EntradaNoNominadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
