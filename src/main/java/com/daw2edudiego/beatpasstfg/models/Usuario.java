/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.models;

import com.daw2edudiego.beatpasstfg.utils.Seguridad;

/**
 *
 * @author Eduardo Olalde
 */
public class Usuario {

    private int idUsuario;
    private String nombre;     // encriptado
    private String email;      // encriptado
    private String password;   // hash
    private String rol;
    private boolean estado;

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return Seguridad.desencriptar(nombre);
    }

    public void setNombre(String nombre) {
        this.nombre = Seguridad.encriptar(nombre);
    }

    public String getEmail() {
        return Seguridad.desencriptar(email);
    }

    public void setEmail(String email) {
        this.email = Seguridad.encriptar(email);
    }

    public String getPassword() {
        return password; 
    }

    public void setPassword(String passwordPlano) {
        this.password = Seguridad.hashSHA256(passwordPlano);
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
