package com.daw2edudiego.beatpasstfg.dto;

import com.daw2edudiego.beatpasstfg.model.EstadoEntradaAsignada;
import com.fasterxml.jackson.annotation.JsonInclude; // Para excluir nulos en JSON
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) para representar una
 * {@link com.daw2edudiego.beatpasstfg.model.EntradaAsignada} individual.
 * <p>
 * Se utiliza para transferir información detallada sobre una entrada
 * específica, incluyendo su estado, el asistente (si está nominada), datos del
 * tipo de entrada y festival asociados, y la URL de datos de la imagen QR
 * generada.
 * </p>
 * <p>
 * Usar {@code @JsonInclude(JsonInclude.Include.NON_NULL)} para no enviar campos
 * nulos en respuestas JSON, manteniendo las respuestas limpias.
 * </p>
 *
 * @author Eduardo Olalde
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Omite campos nulos en la serialización JSON
public class EntradaAsignadaDTO {

    /**
     * ID único de la entrada asignada.
     */
    private Integer idEntradaAsignada;

    /**
     * Contenido textual único codificado en el QR (ej: BEATPASS-TICKET-uuid).
     */
    private String codigoQr;

    /**
     * Estado actual de la entrada (ACTIVA, USADA, CANCELADA).
     *
     * @see com.daw2edudiego.beatpasstfg.model.EstadoEntradaAsignada
     */
    private EstadoEntradaAsignada estado;

    /**
     * Fecha y hora en que la entrada fue nominada a un asistente. Será
     * {@code null} si no está nominada.
     */
    private LocalDateTime fechaAsignacion;

    /**
     * Fecha y hora en que la entrada fue utilizada/validada. Será {@code null}
     * si no ha sido usada.
     */
    private LocalDateTime fechaUso;

    // --- Información relacionada ---
    /**
     * ID del detalle de compra original
     * ({@link com.daw2edudiego.beatpasstfg.model.CompraEntrada}) del que
     * proviene esta entrada.
     */
    private Integer idCompraEntrada;

    /**
     * ID del asistente ({@link com.daw2edudiego.beatpasstfg.model.Asistente})
     * al que está nominada la entrada. Será {@code null} si no está nominada.
     */
    private Integer idAsistente;

    /**
     * Nombre del asistente nominado. Será {@code null} si no está nominada.
     */
    private String nombreAsistente;

    /**
     * Email del asistente nominado. Será {@code null} si no está nominada.
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
     * ID del festival ({@link com.daw2edudiego.beatpasstfg.model.Festival}) al
     * que pertenece la entrada.
     */
    private Integer idFestival;

    /**
     * Nombre del festival al que pertenece la entrada.
     */
    private String nombreFestival;

    /**
     * ID de la pulsera NFC
     * ({@link com.daw2edudiego.beatpasstfg.model.PulseraNFC}) asociada a esta
     * entrada. Será {@code null} si no hay ninguna asociada.
     */
    private Integer idPulseraAsociada;

    /**
     * Código UID de la pulsera NFC asociada. Será {@code null} si no hay
     * ninguna asociada.
     */
    private String codigoUidPulsera;

    // --- Campo para la Imagen QR ---
    /**
     * URL de datos (Base64) de la imagen del código QR generada a partir de
     * {@code codigoQr}. Se utiliza para mostrar la imagen directamente en
     * interfaces web (ej: en una etiqueta {@code <img>}). Será {@code null} si
     * no se pudo generar la imagen QR. Ejemplo:
     * "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
     */
    private String qrCodeImageDataUrl;

    /**
     * Constructor por defecto. Necesario para frameworks como JAX-RS/Jackson.
     */
    public EntradaAsignadaDTO() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID de la entrada asignada.
     *
     * @return El ID único de la entrada asignada.
     */
    public Integer getIdEntradaAsignada() {
        return idEntradaAsignada;
    }

    /**
     * Establece el ID de la entrada asignada.
     *
     * @param idEntradaAsignada El nuevo ID.
     */
    public void setIdEntradaAsignada(Integer idEntradaAsignada) {
        this.idEntradaAsignada = idEntradaAsignada;
    }

    /**
     * Obtiene el contenido textual del código QR.
     *
     * @return La cadena única del QR.
     */
    public String getCodigoQr() {
        return codigoQr;
    }

    /**
     * Establece el contenido textual del código QR.
     *
     * @param codigoQr La nueva cadena del QR.
     */
    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }

    /**
     * Obtiene el estado actual de la entrada.
     *
     * @return El estado (ACTIVA, USADA, CANCELADA).
     */
    public EstadoEntradaAsignada getEstado() {
        return estado;
    }

    /**
     * Establece el estado actual de la entrada.
     *
     * @param estado El nuevo estado.
     */
    public void setEstado(EstadoEntradaAsignada estado) {
        this.estado = estado;
    }

    /**
     * Obtiene la fecha y hora de nominación.
     *
     * @return La fecha de nominación, o {@code null}.
     */
    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    /**
     * Establece la fecha y hora de nominación.
     *
     * @param fechaAsignacion La nueva fecha de nominación.
     */
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    /**
     * Obtiene la fecha y hora de uso.
     *
     * @return La fecha de uso, o {@code null}.
     */
    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    /**
     * Establece la fecha y hora de uso.
     *
     * @param fechaUso La nueva fecha de uso.
     */
    public void setFechaUso(LocalDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }

    /**
     * Obtiene el ID del detalle de compra original.
     *
     * @return El ID del CompraEntrada.
     */
    public Integer getIdCompraEntrada() {
        return idCompraEntrada;
    }

    /**
     * Establece el ID del detalle de compra original.
     *
     * @param idCompraEntrada El nuevo ID del CompraEntrada.
     */
    public void setIdCompraEntrada(Integer idCompraEntrada) {
        this.idCompraEntrada = idCompraEntrada;
    }

    /**
     * Obtiene el ID del asistente nominado.
     *
     * @return El ID del asistente, o {@code null}.
     */
    public Integer getIdAsistente() {
        return idAsistente;
    }

    /**
     * Establece el ID del asistente nominado.
     *
     * @param idAsistente El nuevo ID del asistente.
     */
    public void setIdAsistente(Integer idAsistente) {
        this.idAsistente = idAsistente;
    }

    /**
     * Obtiene el nombre del asistente nominado.
     *
     * @return El nombre del asistente, o {@code null}.
     */
    public String getNombreAsistente() {
        return nombreAsistente;
    }

    /**
     * Establece el nombre del asistente nominado.
     *
     * @param nombreAsistente El nuevo nombre del asistente.
     */
    public void setNombreAsistente(String nombreAsistente) {
        this.nombreAsistente = nombreAsistente;
    }

    /**
     * Obtiene el email del asistente nominado.
     *
     * @return El email del asistente, o {@code null}.
     */
    public String getEmailAsistente() {
        return emailAsistente;
    }

    /**
     * Establece el email del asistente nominado.
     *
     * @param emailAsistente El nuevo email del asistente.
     */
    public void setEmailAsistente(String emailAsistente) {
        this.emailAsistente = emailAsistente;
    }

    /**
     * Obtiene el ID del tipo de entrada original.
     *
     * @return El ID de la Entrada.
     */
    public Integer getIdEntradaOriginal() {
        return idEntradaOriginal;
    }

    /**
     * Establece el ID del tipo de entrada original.
     *
     * @param idEntradaOriginal El nuevo ID de la Entrada.
     */
    public void setIdEntradaOriginal(Integer idEntradaOriginal) {
        this.idEntradaOriginal = idEntradaOriginal;
    }

    /**
     * Obtiene el nombre del tipo de entrada original.
     *
     * @return El tipo de entrada (ej: "General").
     */
    public String getTipoEntradaOriginal() {
        return tipoEntradaOriginal;
    }

    /**
     * Establece el nombre del tipo de entrada original.
     *
     * @param tipoEntradaOriginal El nuevo tipo de entrada.
     */
    public void setTipoEntradaOriginal(String tipoEntradaOriginal) {
        this.tipoEntradaOriginal = tipoEntradaOriginal;
    }

    /**
     * Obtiene el ID del festival asociado.
     *
     * @return El ID del festival.
     */
    public Integer getIdFestival() {
        return idFestival;
    }

    /**
     * Establece el ID del festival asociado.
     *
     * @param idFestival El nuevo ID del festival.
     */
    public void setIdFestival(Integer idFestival) {
        this.idFestival = idFestival;
    }

    /**
     * Obtiene el nombre del festival asociado.
     *
     * @return El nombre del festival.
     */
    public String getNombreFestival() {
        return nombreFestival;
    }

    /**
     * Establece el nombre del festival asociado.
     *
     * @param nombreFestival El nuevo nombre del festival.
     */
    public void setNombreFestival(String nombreFestival) {
        this.nombreFestival = nombreFestival;
    }

    /**
     * Obtiene el ID de la pulsera NFC asociada.
     *
     * @return El ID de la pulsera, o {@code null}.
     */
    public Integer getIdPulseraAsociada() {
        return idPulseraAsociada;
    }

    /**
     * Establece el ID de la pulsera NFC asociada.
     *
     * @param idPulseraAsociada El nuevo ID de la pulsera.
     */
    public void setIdPulseraAsociada(Integer idPulseraAsociada) {
        this.idPulseraAsociada = idPulseraAsociada;
    }

    /**
     * Obtiene el código UID de la pulsera NFC asociada.
     *
     * @return El código UID, o {@code null}.
     */
    public String getCodigoUidPulsera() {
        return codigoUidPulsera;
    }

    /**
     * Establece el código UID de la pulsera NFC asociada.
     *
     * @param codigoUidPulsera El nuevo código UID.
     */
    public void setCodigoUidPulsera(String codigoUidPulsera) {
        this.codigoUidPulsera = codigoUidPulsera;
    }

    /**
     * Obtiene la URL de datos de la imagen QR generada.
     *
     * @return La URL de datos (ej: "data:image/png;base64,..."), o
     * {@code null}.
     */
    public String getQrCodeImageDataUrl() {
        return qrCodeImageDataUrl;
    }

    /**
     * Establece la URL de datos de la imagen QR generada.
     *
     * @param qrCodeImageDataUrl La nueva URL de datos.
     */
    public void setQrCodeImageDataUrl(String qrCodeImageDataUrl) {
        this.qrCodeImageDataUrl = qrCodeImageDataUrl;
    }

    // --- equals y hashCode basados en ID ---
    /**
     * Compara este objeto con otro para determinar igualdad. Dos DTOs de
     * EntradaAsignada son iguales si sus {@code idEntradaAsignada} son iguales.
     *
     * @param o El objeto a comparar.
     * @return {@code true} si los objetos son iguales, {@code false} en caso
     * contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntradaAsignadaDTO that = (EntradaAsignadaDTO) o;
        // Solo compara por ID si ambos no son nulos
        return idEntradaAsignada != null && Objects.equals(idEntradaAsignada, that.idEntradaAsignada);
    }

    /**
     * Calcula el código hash para este objeto. Se basa únicamente en el
     * {@code idEntradaAsignada}.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idEntradaAsignada);
    }

    // --- toString ---
    /**
     * Devuelve una representación en cadena de este DTO, útil para logging y
     * depuración. Incluye los campos principales y trunca el contenido del QR y
     * la URL de datos de imagen.
     *
     * @return Una cadena representando el DTO.
     */
    @Override
    public String toString() {
        return "EntradaAsignadaDTO{"
                + "idEntradaAsignada=" + idEntradaAsignada
                + ", codigoQr='" + (codigoQr != null ? codigoQr.substring(0, Math.min(codigoQr.length(), 15)) + "..." : "null") + '\''
                + ", estado=" + estado
                + ", idAsistente=" + idAsistente
                + ", tipoEntradaOriginal='" + tipoEntradaOriginal + '\''
                + ", idFestival=" + idFestival
                + ", qrCodeImageDataUrl='" + (qrCodeImageDataUrl != null ? qrCodeImageDataUrl.substring(0, Math.min(50, qrCodeImageDataUrl.length())) + "..." : "null") + '\''
                + '}';
    }
}
