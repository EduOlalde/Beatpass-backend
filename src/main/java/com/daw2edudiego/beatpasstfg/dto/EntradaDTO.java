package com.daw2edudiego.beatpasstfg.dto;

import com.daw2edudiego.beatpasstfg.model.EstadoEntrada;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para representar una Entrada individual. Incluye información de
 * la entrada, asistente, festival y la imagen QR.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntradaDTO {

    private Integer idEntrada;
    private String codigoQr;
    private EstadoEntrada estado;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaUso;

    // Información relacionada
    private Integer idCompraEntrada;
    private Integer idAsistente;
    private String nombreAsistente;
    private String emailAsistente;
    private Integer idEntradaOriginal;
    private String tipoEntradaOriginal;
    private Integer idFestival;
    private String nombreFestival;
    private Integer idPulseraAsociada;
    private String codigoUidPulsera;

    /**
     * URL de datos (Base64) de la imagen del código QR generada.
     */
    private String qrCodeImageDataUrl;

    public EntradaDTO() {
    }

    // --- Getters y Setters ---
    public Integer getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(Integer idEntrada) {
        this.idEntrada = idEntrada;
    }

    public String getCodigoQr() {
        return codigoQr;
    }

    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }

    public EstadoEntrada getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntrada estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    public void setFechaUso(LocalDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }

    public Integer getIdCompraEntrada() {
        return idCompraEntrada;
    }

    public void setIdCompraEntrada(Integer idCompraEntrada) {
        this.idCompraEntrada = idCompraEntrada;
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

    public Integer getIdEntradaOriginal() {
        return idEntradaOriginal;
    }

    public void setIdEntradaOriginal(Integer idEntradaOriginal) {
        this.idEntradaOriginal = idEntradaOriginal;
    }

    public String getTipoEntradaOriginal() {
        return tipoEntradaOriginal;
    }

    public void setTipoEntradaOriginal(String tipoEntradaOriginal) {
        this.tipoEntradaOriginal = tipoEntradaOriginal;
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

    public Integer getIdPulseraAsociada() {
        return idPulseraAsociada;
    }

    public void setIdPulseraAsociada(Integer idPulseraAsociada) {
        this.idPulseraAsociada = idPulseraAsociada;
    }

    public String getCodigoUidPulsera() {
        return codigoUidPulsera;
    }

    public void setCodigoUidPulsera(String codigoUidPulsera) {
        this.codigoUidPulsera = codigoUidPulsera;
    }

    public String getQrCodeImageDataUrl() {
        return qrCodeImageDataUrl;
    }

    public void setQrCodeImageDataUrl(String qrCodeImageDataUrl) {
        this.qrCodeImageDataUrl = qrCodeImageDataUrl;
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
        EntradaDTO that = (EntradaDTO) o;
        return idEntrada != null && Objects.equals(idEntrada, that.idEntrada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEntrada);
    }

    // --- toString ---
    @Override
    public String toString() {
        return "EntradaDTO{"
                + "idEntrada=" + idEntrada
                + ", codigoQr='" + (codigoQr != null ? codigoQr.substring(0, Math.min(codigoQr.length(), 15)) + "..." : "null") + '\''
                + ", estado=" + estado
                + ", idAsistente=" + idAsistente
                + ", tipoEntradaOriginal='" + tipoEntradaOriginal + '\''
                + ", idFestival=" + idFestival
                + ", qrCodeImageDataUrl='" + (qrCodeImageDataUrl != null ? qrCodeImageDataUrl.substring(0, Math.min(50, qrCodeImageDataUrl.length())) + "..." : "null") + '\''
                + '}';
    }
}
