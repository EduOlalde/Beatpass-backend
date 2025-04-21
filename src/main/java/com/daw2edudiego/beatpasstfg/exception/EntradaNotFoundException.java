package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción personalizada que se lanza cuando no se encuentra un tipo de
 * entrada específico (Entrada) en la base de datos, generalmente al buscar por
 * su ID.
 *
 * @author Eduardo Olalde
 */
public class EntradaNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "Tipo de entrada con ID X
     * no encontrado").
     */
    public EntradaNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que causó esta.
     */
    public EntradaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
