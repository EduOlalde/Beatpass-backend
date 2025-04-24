package com.daw2edudiego.beatpasstfg.dto;

import com.daw2edudiego.beatpasstfg.model.EstadoEntradaAsignada;
import com.fasterxml.jackson.annotation.JsonInclude; // Para excluir nulos en JSON
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) para representar una
 * {@link com.daw2edudiego.beatpasstfg.model.EntradaAsignada} individual. Se
 * utiliza para transferir información detallada sobre una entrada específica,
 * incluyendo su estado, el asistente (si está nominada), y datos del tipo de
 * entrada y festival asociados. Usar
 * {@code @JsonInclude(JsonInclude.Include.NON_NULL)} para no enviar campos
 * nulos en respuestas JSON.
 *
 * @author Eduardo Olalde
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Opcional: Omite campos nulos en la serialización JSON
public class EntradaAsignadaDTO {

    private Integer idEntradaAsignada;
    private String codigoQr; // Podría mostrarse truncado o completo según necesidad
    private EstadoEntradaAsignada estado;
    private LocalDateTime fechaAsignacion; // Fecha de nominación
    private LocalDateTime fechaUso; // Fecha de validación/uso

    // --- Información relacionada ---
    /**
     * ID del detalle de compra original del que proviene esta entrada.
     */
    private Integer idCompraEntrada;

    /**
     * ID del asistente al que está nominada la entrada (null si no nominada).
     */
    private Integer idAsistente;
    /**
     * Nombre del asistente nominado (null si no nominada).
     */
    private String nombreAsistente;
    /**
     * Email del asistente nominado (null si no nominada).
     */
    private String emailAsistente;

    /**
     * ID del tipo de entrada original
     * ({@link com.daw2edudiego.beatpasstfg.model.Entrada}).
     */
    private Integer idEntradaOriginal;
    /**
     * Nombre del tipo de entrada original (ej: "General", "VIP").
     */
    private String tipoEntradaOriginal;

    /**
     * ID del festival al que pertenece la entrada.
     */
    private Integer idFestival;
    /**
     * Nombre del festival al que pertenece la entrada.
     */
    private String nombreFestival;

    /**
     * ID de la pulsera asociada (null si no hay).
     */
    private Integer idPulseraAsociada;
    /**
     * Código UID de la pulsera asociada (null si no hay).
     */
    private String codigoUidPulsera;

    /**
     * Constructor por defecto.
     */
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

    // --- equals y hashCode basados en ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntradaAsignadaDTO that = (EntradaAsignadaDTO) o;
        return Objects.equals(idEntradaAsignada, that.idEntradaAsignada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEntradaAsignada);
    }

    // --- toString ---
    @Override
    public String toString() {
        return "EntradaAsignadaDTO{"
                + "idEntradaAsignada=" + idEntradaAsignada
                + ", codigoQr='" + (codigoQr != null ? codigoQr.substring(0, Math.min(codigoQr.length(), 15)) + "..." : "null") + '\''
                + // Mostrar solo parte del QR
                ", estado=" + estado
                + ", idAsistente=" + idAsistente
                + ", nombreAsistente='" + nombreAsistente + '\''
                + ", tipoEntradaOriginal='" + tipoEntradaOriginal + '\''
                + ", idFestival=" + idFestival
                + ", idPulseraAsociada=" + idPulseraAsociada
                + '}';
    }
}
