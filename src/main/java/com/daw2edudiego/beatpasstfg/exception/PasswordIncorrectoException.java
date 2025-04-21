/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción lanzada cuando la contraseña proporcionada no es correcta.
 *
 * @author Eduardo Olalde
 *
 */
public class PasswordIncorrectoException extends RuntimeException {

    public PasswordIncorrectoException(String message) {
        super(message);
    }
}
