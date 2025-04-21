package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción lanzada al intentar realizar un consumo con una pulsera que no
 * tiene saldo suficiente.
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(String message) {
        super(message);
    }

    public SaldoInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}
