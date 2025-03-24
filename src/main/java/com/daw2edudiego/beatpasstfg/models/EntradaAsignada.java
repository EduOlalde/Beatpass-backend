/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.models;

/**
 *
 * @author Eduardo Olalde
 */
public class EntradaAsignada {

    private int idEntradaAsignada;
    private int idCompraEntrada;
    private Integer idUsuario;
    private String estado;

    public EntradaAsignada() {
    }

    public EntradaAsignada(int idEntradaAsignada, int idCompraEntrada, Integer idUsuario, String estado) {
        this.idEntradaAsignada = idEntradaAsignada;
        this.idCompraEntrada = idCompraEntrada;
        this.idUsuario = idUsuario;
        this.estado = estado;
    }

    public int getIdEntradaAsignada() {
        return idEntradaAsignada;
    }

    public void setIdEntradaAsignada(int idEntradaAsignada) {
        this.idEntradaAsignada = idEntradaAsignada;
    }

    public int getIdCompraEntrada() {
        return idCompraEntrada;
    }

    public void setIdCompraEntrada(int idCompraEntrada) {
        this.idCompraEntrada = idCompraEntrada;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
