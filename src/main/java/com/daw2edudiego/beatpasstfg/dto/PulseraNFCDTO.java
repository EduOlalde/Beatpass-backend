package com.daw2edudiego.beatpasstfg.dto;

import com.fasterxml.jackson.annotation.JsonInclude; // Para excluir nulos
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) para representar la información de una
 * {@link com.daw2edudiego.beatpasstfg.model.PulseraNFC}. Se utiliza para
 * transferir datos de pulseras, incluyendo opcionalmente información básica de
 * la entrada asignada y el asistente asociados.
 *
 * @author Eduardo Olalde
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Opcional: Omite campos nulos en JSON
public class PulseraNFCDTO {

    /**
     * ID de la pulsera.
     */
    private Integer idPulsera;

    /**
     * Código UID único de la pulsera.
     */
    private String codigoUid;

    /**
     * Saldo actual de la pulsera.
     */
    private BigDecimal saldo;

    /**
     * Estado de activación de la pulsera.
     */
    private Boolean activa;

    /**
     * Fecha de alta de la pulsera en el sistema.
     */
    private LocalDateTime fechaAlta;

    /**
     * Fecha de la última modificación del registro de la pulsera.
     */
    private LocalDateTime ultimaModificacion;

    // --- Información Asociada (Opcional) ---
    /**
     * ID de la entrada asignada asociada (null si no está asociada).
     */
    private Integer idEntradaAsignada;

    /**
     * Código QR de la entrada asignada (puede ser null o truncado).
     */
    private String qrEntradaAsignada; // Considerar si realmente necesario en este DTO

    /**
     * ID del asistente asociado a través de la entrada (null si no
     * asociada/nominada).
     */
    private Integer idAsistente;
    /**
     * Nombre del asistente asociado (null si no asociada/nominada).
     */
    private String nombreAsistente;
    /**
     * Email del asistente asociado (null si no asociada/nominada).
     */
    private String emailAsistente;

    /**
     * ID del festival asociado (a través de la entrada).
     */
    private Integer idFestival; // Se obtendría navegando EntradaAsignada -> CompraEntrada -> Entrada -> Festival
    /**
     * Nombre del festival asociado.
     */
    private String nombreFestival;

    /**
     * Constructor por defecto.
     */
    public PulseraNFCDTO() {
    }

    // --- Getters y Setters ---
    // (Omitidos por brevedad, pero deben estar presentes todos los getters y setters)
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

    // --- equals y hashCode basados en ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PulseraNFCDTO that = (PulseraNFCDTO) o;
        // Compara por ID si ambos no son nulos
        return idPulsera != null && Objects.equals(idPulsera, that.idPulsera);
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
