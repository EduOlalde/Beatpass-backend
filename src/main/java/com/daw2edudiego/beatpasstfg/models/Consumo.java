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
public class Consumo {

    private int idConsumo;
    private int idPulsera;
    private String descripcion;
    private double monto;
    private Date fecha;
    private int idFestival;

    public Consumo() {
    }

    public Consumo(int idConsumo, int idPulsera, String descripcion, double monto, Date fecha, int idFestival) {
        this.idConsumo = idConsumo;
        this.idPulsera = idPulsera;
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.idFestival = idFestival;
    }

    public int getIdConsumo() {
        return idConsumo;
    }

    public void setIdConsumo(int idConsumo) {
        this.idConsumo = idConsumo;
    }

    public int getIdPulsera() {
        return idPulsera;
    }

    public void setIdPulsera(int idPulsera) {
        this.idPulsera = idPulsera;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public int getIdFestival() {
        return idFestival;
    }

    public void setIdFestival(int idFestival) {
        this.idFestival = idFestival;
    }

}
