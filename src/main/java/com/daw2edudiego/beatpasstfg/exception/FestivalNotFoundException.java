/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.exception;

/**
 * Excepción lanzada cuando no se encuentra un festival.
 */
public class FestivalNotFoundException extends RuntimeException {

    public FestivalNotFoundException(String message) {
        super(message);
    }
}
