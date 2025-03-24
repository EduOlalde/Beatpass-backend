/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.models;

import java.sql.Date;

/**
 *
 * @author Eduardo Olalde
 */
public class Compra {

    private int idCompra;
    private int idAsistente;
    private Date fechaCompra;
    private double total;

    public Compra() {
    }

    public Compra(int idCompra, int idAsistente, Date fechaCompra, double total) {
        this.idCompra = idCompra;
        this.idAsistente = idAsistente;
        this.fechaCompra = fechaCompra;
        this.total = total;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdAsistente() {
        return idAsistente;
    }

    public void setIdAsistente(int idAsistente) {
        this.idAsistente = idAsistente;
    }

    public Date getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(Date fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

}
