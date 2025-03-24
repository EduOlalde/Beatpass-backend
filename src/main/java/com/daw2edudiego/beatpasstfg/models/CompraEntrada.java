/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.models;

/**
 *
 * @author Eduardo Olalde
 */
public class CompraEntrada {

    private int idCompraEntrada;
    private int idCompra;
    private int idEntrada;
    private double precioUnitario;

    public CompraEntrada() {
    }

    public CompraEntrada(int idCompraEntrada, int idCompra, int idEntrada, double precioUnitario) {
        this.idCompraEntrada = idCompraEntrada;
        this.idCompra = idCompra;
        this.idEntrada = idEntrada;
        this.precioUnitario = precioUnitario;
    }

    public int getIdCompraEntrada() {
        return idCompraEntrada;
    }

    public void setIdCompraEntrada(int idCompraEntrada) {
        this.idCompraEntrada = idCompraEntrada;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(int idEntrada) {
        this.idEntrada = idEntrada;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

}
