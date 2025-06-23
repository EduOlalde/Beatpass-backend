package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza al intentar realizar una
 * operación de consumo ({@link com.daw2edudiego.beatpasstfg.model.Consumo}) con
 * una {@link com.daw2edudiego.beatpasstfg.model.PulseraNFC} que no dispone de
 * saldo suficiente para cubrir el importe del consumo.
 *
 * @see
 * com.daw2edudiego.beatpasstfg.service.PulseraNFCService#registrarConsumo(String,
 * java.math.BigDecimal, String, Integer, Integer, Integer)
 */
public class SaldoInsuficienteException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "Saldo insuficiente (X €)
     * para realizar el consumo de Y €.").
     */
    public SaldoInsuficienteException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public SaldoInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}
