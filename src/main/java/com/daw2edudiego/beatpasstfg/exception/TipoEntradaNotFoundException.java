package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando no se encuentra un
 * tipo de entrada específico
 * ({@link com.daw2edudiego.beatpasstfg.model.Entrada}) en la base de datos,
 * generalmente al buscar por su ID.
 *
 * @see com.daw2edudiego.beatpasstfg.model.Entrada
 * @see com.daw2edudiego.beatpasstfg.service.EntradaService
 * @see com.daw2edudiego.beatpasstfg.service.VentaService
 * @author Eduardo Olalde
 */
public class TipoEntradaNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "Tipo de entrada con ID X
     * no encontrado.").
     */
    public TipoEntradaNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public TipoEntradaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
