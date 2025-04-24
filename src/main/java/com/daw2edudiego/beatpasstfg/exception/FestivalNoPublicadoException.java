package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando se intenta
 * realizar una operación que requiere que un
 * {@link com.daw2edudiego.beatpasstfg.model.Festival} esté en estado
 * {@link com.daw2edudiego.beatpasstfg.model.EstadoFestival#PUBLICADO}, pero el
 * festival se encuentra en otro estado (ej: BORRADOR, CANCELADO, FINALIZADO).
 * <p>
 * Un ejemplo típico es intentar registrar una venta de entradas para un
 * festival que aún no ha sido publicado por el administrador.
 * </p>
 *
 * @see
 * com.daw2edudiego.beatpasstfg.service.VentaService#registrarVenta(Integer,
 * Integer, int)
 */
public class FestivalNoPublicadoException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "No se pueden vender
     * entradas para el festival 'X' porque no está publicado.").
     */
    public FestivalNoPublicadoException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public FestivalNoPublicadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
