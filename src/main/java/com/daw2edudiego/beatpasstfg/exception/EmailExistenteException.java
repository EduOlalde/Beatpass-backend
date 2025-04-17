/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción lanzada al intentar crear un usuario con un email que ya existe.
 */
public class EmailExistenteException extends RuntimeException {

    public EmailExistenteException(String message) {
        super(message);
    }
}
