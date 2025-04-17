/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.dto;

/**
 * DTO para devolver el token JWT tras un login exitoso.
 */
public class TokenDTO {

    private String token;

    public TokenDTO(String token) {
        this.token = token;
    }

    // Getter y Setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
