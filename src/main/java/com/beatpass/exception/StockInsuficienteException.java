package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando se intenta
 * realizar una operación que requiere una cantidad específica de un tipo de
 * entrada ({@link com.beatpass.model.Entrada}), pero el stock
 * disponible es menor que la cantidad requerida.
 * <p>
 * Esto ocurre típicamente durante el proceso de registro de una venta.
 * </p>
 *
 * @see
 * com.beatpass.service.VentaService#registrarVenta(Integer,
 * Integer, int)
 */
public class StockInsuficienteException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "No hay suficiente stock
     * para el tipo de entrada 'X'. Disponible: Y.").
     */
    public StockInsuficienteException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public StockInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}
