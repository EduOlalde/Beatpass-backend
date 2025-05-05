/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción personalizada para indicar problemas durante la verificación o
 * procesamiento del pago con la pasarela externa (Stripe). Hereda de
 * RuntimeException para evitar la necesidad de declaración 'throws' explícita
 * en cada nivel, pero debe ser capturada adecuadamente en las capas superiores.
 *
 * * @author Eduardo Olalde
 */
public class PagoInvalidoException extends RuntimeException {

    /**
     * Constructor con mensaje de error.
     *
     * @param message Descripción del error de pago.
     */
    public PagoInvalidoException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje de error y causa original.
     *
     * @param message Descripción del error de pago.
     * @param cause La excepción original que causó el error (ej:
     * StripeException).
     */
    public PagoInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
