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
public class Asistente {

    private int idAsistente;
    private String nombre;     // encriptado
    private String email;      // encriptado
    private String telefono;   // encriptado

    public Asistente() {
    }

    public int getIdAsistente() {
        return idAsistente;
    }

    public void setIdAsistente(int idAsistente) {
        this.idAsistente = idAsistente;
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

    public String getTelefono() {
        return Seguridad.desencriptar(telefono);
    }

    public void setTelefono(String telefono) {
        this.telefono = Seguridad.encriptar(telefono);
    }
}
