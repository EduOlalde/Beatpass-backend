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
public class Recarga {

    private int idRecarga;
    private int idPulsera;
    private double monto;
    private Date fecha;
    private String metodoPago;

    public Recarga() {
    }

    public Recarga(int idRecarga, int idPulsera, double monto, Date fecha, String metodoPago) {
        this.idRecarga = idRecarga;
        this.idPulsera = idPulsera;
        this.monto = monto;
        this.fecha = fecha;
        this.metodoPago = metodoPago;
    }

    public int getIdRecarga() {
        return idRecarga;
    }

    public void setIdRecarga(int idRecarga) {
        this.idRecarga = idRecarga;
    }

    public int getIdPulsera() {
        return idPulsera;
    }

    public void setIdPulsera(int idPulsera) {
        this.idPulsera = idPulsera;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

}
