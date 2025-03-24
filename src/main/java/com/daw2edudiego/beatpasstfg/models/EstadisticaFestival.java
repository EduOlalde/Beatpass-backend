/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.models;

/**
 *
 * @author Eduardo Olalde
 */
public class EstadisticaFestival {

    private int idFestival;
    private int entradasVendidas;
    private double ingresosTotales;
    private double recargasTotales;
    private double consumosTotales;

    public EstadisticaFestival() {
    }

    public EstadisticaFestival(int idFestival, int entradasVendidas, double ingresosTotales, double recargasTotales, double consumosTotales) {
        this.idFestival = idFestival;
        this.entradasVendidas = entradasVendidas;
        this.ingresosTotales = ingresosTotales;
        this.recargasTotales = recargasTotales;
        this.consumosTotales = consumosTotales;
    }

    public int getIdFestival() {
        return idFestival;
    }

    public void setIdFestival(int idFestival) {
        this.idFestival = idFestival;
    }

    public int getEntradasVendidas() {
        return entradasVendidas;
    }

    public void setEntradasVendidas(int entradasVendidas) {
        this.entradasVendidas = entradasVendidas;
    }

    public double getIngresosTotales() {
        return ingresosTotales;
    }

    public void setIngresosTotales(double ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public double getRecargasTotales() {
        return recargasTotales;
    }

    public void setRecargasTotales(double recargasTotales) {
        this.recargasTotales = recargasTotales;
    }

    public double getConsumosTotales() {
        return consumosTotales;
    }

    public void setConsumosTotales(double consumosTotales) {
        this.consumosTotales = consumosTotales;
    }

}
