package com.daw2edudiego.beatpasstfg.dto;

import com.daw2edudiego.beatpasstfg.model.EstadoEntradaAsignada;
import java.time.LocalDateTime;

/**
 * DTO para representar una Entrada Asignada (individual). Incluye información
 * básica de la entrada, su estado, y opcionalmente datos del asistente si está
 * nominada y del tipo de entrada original.
 */
public class EntradaAsignadaDTO {

    private Integer idEntradaAsignada;
    private String codigoQr; // Podríamos mostrar solo una parte por seguridad
    private EstadoEntradaAsignada estado;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaUso;

    // Info de la compra original
    private Integer idCompraEntrada;

    // Info del Asistente (si está nominada)
    private Integer idAsistente;
    private String nombreAsistente;
    private String emailAsistente;

    // Info del Tipo de Entrada original (opcional, útil para mostrar)
    private Integer idEntradaOriginal;
    private String tipoEntradaOriginal;

    // Info del Festival (opcional, útil para contexto)
    private Integer idFestival;
    private String nombreFestival;

    // Constructor
    public EntradaAsignadaDTO() {
    }

    // --- Getters y Setters ---
    public Integer getIdEntradaAsignada() {
        return idEntradaAsignada;
    }

    public void setIdEntradaAsignada(Integer idEntradaAsignada) {
        this.idEntradaAsignada = idEntradaAsignada;
    }

    public String getCodigoQr() {
        return codigoQr;
    }

    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }

    public EstadoEntradaAsignada getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntradaAsignada estado) {
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

    // --- toString ---
    @Override
    public String toString() {
        return "EntradaAsignadaDTO{"
                + "idEntradaAsignada=" + idEntradaAsignada
                + ", estado=" + estado
                + ", idAsistente=" + idAsistente
                + ", nombreAsistente='" + nombreAsistente + '\''
                + ", tipoEntradaOriginal='" + tipoEntradaOriginal + '\''
                + '}';
    }
}
