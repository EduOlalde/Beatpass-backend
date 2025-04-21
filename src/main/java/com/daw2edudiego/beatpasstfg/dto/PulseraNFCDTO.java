package com.daw2edudiego.beatpasstfg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para representar la información de una PulseraNFC, incluyendo datos
 * básicos de la entrada y asistente asociados (si existen).
 */
public class PulseraNFCDTO {

    private Integer idPulsera;
    private String codigoUid;
    private BigDecimal saldo;
    private Boolean activa;
    private LocalDateTime fechaAlta;
    private LocalDateTime ultimaModificacion;

    // Info de la Entrada Asignada asociada (si existe)
    private Integer idEntradaAsignada;
    private String qrEntradaAsignada; // Quizás solo una parte o un flag

    // Info del Asistente asociado (a través de la EntradaAsignada)
    private Integer idAsistente;
    private String nombreAsistente;
    private String emailAsistente;

    // Info del Festival (a través de la EntradaAsignada)
    private Integer idFestival;
    private String nombreFestival;

    // Constructor
    public PulseraNFCDTO() {
    }

    // --- Getters y Setters ---
    public Integer getIdPulsera() {
        return idPulsera;
    }

    public void setIdPulsera(Integer idPulsera) {
        this.idPulsera = idPulsera;
    }

    public String getCodigoUid() {
        return codigoUid;
    }

    public void setCodigoUid(String codigoUid) {
        this.codigoUid = codigoUid;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public LocalDateTime getUltimaModificacion() {
        return ultimaModificacion;
    }

    public void setUltimaModificacion(LocalDateTime ultimaModificacion) {
        this.ultimaModificacion = ultimaModificacion;
    }

    public Integer getIdEntradaAsignada() {
        return idEntradaAsignada;
    }

    public void setIdEntradaAsignada(Integer idEntradaAsignada) {
        this.idEntradaAsignada = idEntradaAsignada;
    }

    public String getQrEntradaAsignada() {
        return qrEntradaAsignada;
    }

    public void setQrEntradaAsignada(String qrEntradaAsignada) {
        this.qrEntradaAsignada = qrEntradaAsignada;
    }

    public Integer getIdAsistente() {
        return idAsistente;
    }

    public void setIdAsistente(Integer idAsistente) {
        this.idAsistente = idAsistente;
    }

    public String getNombreAsistente() {
        return nombreAsistente;
    }

    public void setNombreAsistente(String nombreAsistente) {
        this.nombreAsistente = nombreAsistente;
    }

    public String getEmailAsistente() {
        return emailAsistente;
    }

    public void setEmailAsistente(String emailAsistente) {
        this.emailAsistente = emailAsistente;
    }

    public Integer getIdFestival() {
        return idFestival;
    }

    public void setIdFestival(Integer idFestival) {
        this.idFestival = idFestival;
    }

    public String getNombreFestival() {
        return nombreFestival;
    }

    public void setNombreFestival(String nombreFestival) {
        this.nombreFestival = nombreFestival;
    }

    // --- equals y hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PulseraNFCDTO that = (PulseraNFCDTO) o;
        return Objects.equals(idPulsera, that.idPulsera);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPulsera);
    }

    // --- toString ---
    @Override
    public String toString() {
        return "PulseraNFCDTO{"
                + "idPulsera=" + idPulsera
                + ", codigoUid='" + codigoUid + '\''
                + ", saldo=" + saldo
                + ", activa=" + activa
                + ", idEntradaAsignada=" + idEntradaAsignada
                + ", idAsistente=" + idAsistente
                + '}';
    }
}
