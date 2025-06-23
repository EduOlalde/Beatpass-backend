package com.beatpass.exception;

/**
 * Excepción unchecked (RuntimeException) que se lanza cuando se intenta
 * realizar una operación sobre un Asistente que no se encuentra en el sistema
 * (generalmente al buscar por ID o email).
 *
 * @see com.beatpass.model.Asistente
 * @see com.beatpass.service.AsistenteService
 */
public class AsistenteNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje descriptivo del error.
     *
     * @param message Mensaje detallando la causa (ej: "Asistente con ID X no
     * encontrado").
     */
    public AsistenteNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y la causa raíz original. Útil para
     * encadenar excepciones y mantener el stack trace completo.
     *
     * @param message Mensaje detallando la causa de la excepción.
     * @param cause La excepción original que provocó esta (ej:
     * NoResultException de JPA).
     */
    public AsistenteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
