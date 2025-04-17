/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.dto;

import com.daw2edudiego.beatpasstfg.model.RolUsuario;

/**
 * DTO específico para la creación de nuevos usuarios (incluye contraseña en
 * texto plano).
 */
public class UsuarioCreacionDTO {

    private String nombre;
    private String email;
    private String password; // Contraseña en texto plano
    private RolUsuario rol;

    // Constructor, Getters y Setters
    public UsuarioCreacionDTO() {
    }

    // Getters y Setters...
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }
}
