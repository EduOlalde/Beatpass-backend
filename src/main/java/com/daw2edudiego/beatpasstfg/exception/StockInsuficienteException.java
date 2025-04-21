package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación (ej: venta) para
 * la que no hay suficiente stock disponible.
 */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String message) {
        super(message);
    }

    public StockInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}
