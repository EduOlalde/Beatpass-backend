package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando no se encuentra
 * una entidad {@link com.beatpass.model.EntradaAsignada}
 * específica en la base de datos, generalmente al buscar por su ID o código QR.
 *
 * @see com.beatpass.model.EntradaAsignada
 * @see com.beatpass.service.EntradaAsignadaService
 */
public class EntradaNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "Entrada asignada con ID
     * X no encontrada.").
     */
    public EntradaNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta.
     */
    public EntradaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
