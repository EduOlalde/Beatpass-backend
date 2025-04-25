package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map; // Importar Map
import java.util.Objects;

/**
 * DTO (Data Transfer Object) para representar la información de un
 * {@link com.daw2edudiego.beatpasstfg.model.Asistente}. Se utiliza para
 * transferir datos de asistentes entre capas (servicio, controlador, vista).
 * <p>
 * Incluye información básica del asistente y un mapa opcional que relaciona los
 * festivales en los que tiene entradas activas con el UID de la pulsera
 * asociada para ese festival (si existe).
 * </p>
 *
 * @author Eduardo Olalde
 */
public class AsistenteDTO {

    /**
     * ID único del asistente. Puede ser null si se usa para crear un nuevo
     * asistente.
     */
    private Integer idAsistente;

    /**
     * Nombre completo del asistente. Requerido al crear/actualizar.
     */
    @NotBlank(message = "El nombre del asistente no puede estar vacío.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    /**
     * Dirección de correo electrónico del asistente. Requerido, debe ser único
     * y tener formato válido.
     */
    @NotBlank(message = "El email del asistente no puede estar vacío.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String email;

    /**
     * Número de teléfono del asistente (opcional).
     */
    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefono;

    /**
     * Fecha y hora en que se creó el registro del asistente. Generalmente
     * informativo, no editable.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Mapa que almacena los nombres de los festivales a los que el asistente
     * tiene entradas activas y el UID de la pulsera asociada (si existe) para
     * cada uno de esos festivales.
     * <p>
     * Clave: Nombre del Festival (String) <br>
     * Valor: Código UID de la Pulsera (String) o {@code null} si no hay pulsera
     * asociada para esa entrada/festival.
     * </p>
     * Este mapa se poblará en la capa de servicio cuando sea necesario mostrar
     * esta información detallada.
     */
    private Map<String, String> festivalPulseraInfo;

    /**
     * Constructor por defecto. Necesario para frameworks como Jackson/JAX-RS.
     */
    public AsistenteDTO() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID del asistente.
     *
     * @return El ID del asistente, o null si es un DTO para creación.
     */
    public Integer getIdAsistente() {
        return idAsistente;
    }

    /**
     * Establece el ID del asistente.
     *
     * @param idAsistente El nuevo ID.
     */
    public void setIdAsistente(Integer idAsistente) {
        this.idAsistente = idAsistente;
    }

    /**
     * Obtiene el nombre del asistente.
     *
     * @return El nombre del asistente.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del asistente.
     *
     * @param nombre El nuevo nombre.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el email del asistente.
     *
     * @return El email del asistente.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el email del asistente.
     *
     * @param email El nuevo email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene el teléfono del asistente.
     *
     * @return El teléfono, o null si no tiene.
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Establece el teléfono del asistente.
     *
     * @param telefono El nuevo teléfono.
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * Obtiene la fecha de creación del registro del asistente.
     *
     * @return La fecha y hora de creación.
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Establece la fecha de creación del registro del asistente.
     *
     * @param fechaCreacion La nueva fecha de creación.
     */
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Obtiene el mapa que relaciona nombres de festivales con los UIDs de las
     * pulseras asociadas para este asistente.
     *
     * @return Un Map donde la clave es el nombre del festival y el valor es el
     * UID de la pulsera (o null si no hay pulsera para ese festival). Puede ser
     * null si no se ha poblado esta información.
     */
    public Map<String, String> getFestivalPulseraInfo() {
        return festivalPulseraInfo;
    }

    /**
     * Establece el mapa que relaciona nombres de festivales con los UIDs de las
     * pulseras asociadas.
     *
     * @param festivalPulseraInfo El nuevo mapa.
     */
    public void setFestivalPulseraInfo(Map<String, String> festivalPulseraInfo) {
        this.festivalPulseraInfo = festivalPulseraInfo;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara este DTO con otro objeto para determinar igualdad. Dos DTOs de
     * Asistente son iguales si sus {@code idAsistente} son iguales (y no
     * nulos).
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
        AsistenteDTO that = (AsistenteDTO) o;
        return idAsistente != null && Objects.equals(idAsistente, that.idAsistente);
    }

    /**
     * Calcula el código hash para este DTO. Se basa únicamente en el
     * {@code idAsistente}.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idAsistente);
    }

    /**
     * Devuelve una representación en cadena de este DTO. Útil para logging y
     * depuración.
     *
     * @return Una cadena representando el DTO.
     */
    @Override
    public String toString() {
        return "AsistenteDTO{"
                + "idAsistente=" + idAsistente
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + ", telefono='" + telefono + '\''
                + ", festivalPulseraInfo=" + festivalPulseraInfo
                + '}';
    }
}
