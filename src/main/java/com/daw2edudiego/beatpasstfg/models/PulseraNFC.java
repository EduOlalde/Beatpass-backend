/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.models;

/**
 *
 * @author Eduardo Olalde
 */
public class PulseraNFC {

    private int idPulsera;
    private String codigoUID;
    private int idEntrada;
    private double saldo;
    private boolean activa;

    public PulseraNFC() {
    }

    public PulseraNFC(int idPulsera, String codigoUID, int idEntrada, double saldo, boolean activa) {
        this.idPulsera = idPulsera;
        this.codigoUID = codigoUID;
        this.idEntrada = idEntrada;
        this.saldo = saldo;
        this.activa = activa;
    }

    public int getIdPulsera() {
        return idPulsera;
    }

    public void setIdPulsera(int idPulsera) {
        this.idPulsera = idPulsera;
    }

    public String getCodigoUID() {
        return codigoUID;
    }

    public void setCodigoUID(String codigoUID) {
        this.codigoUID = codigoUID;
    }

    public int getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(int idEntrada) {
        this.idEntrada = idEntrada;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

}
