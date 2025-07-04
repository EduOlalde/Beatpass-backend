package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando no se encuentra un
 * {@link com.beatpass.model.Festival} específico en la base de
 * datos, generalmente al buscar por su ID.
 *
 * @see com.beatpass.model.Festival
 * @see com.beatpass.service.FestivalService
 */
public class FestivalNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "Festival con ID X no
     * encontrado.").
     */
    public FestivalNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public FestivalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
