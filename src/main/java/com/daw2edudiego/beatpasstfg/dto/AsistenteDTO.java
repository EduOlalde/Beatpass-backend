package com.daw2edudiego.beatpasstfg.dto;

import jakarta.validation.constraints.Email; // Validación
import jakarta.validation.constraints.NotBlank; // Validación
import jakarta.validation.constraints.Size; // Validación
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) para representar la información de un
 * {@link com.daw2edudiego.beatpasstfg.model.Asistente}. Se utiliza para
 * transferir datos de asistentes entre capas (servicio, controlador, vista) y
 * potencialmente para recibir datos de formularios (aunque podría necesitar un
 * DTO específico para creación/edición). Excluye información sensible o
 * relaciones complejas no necesarias para la transferencia.
 *
 * @author Eduardo Olalde
 */
public class AsistenteDTO {

    /**
     * ID del asistente. Generalmente se incluye al mostrar datos, pero podría
     * ser null al crear uno nuevo.
     */
    private Integer idAsistente;

    /**
     * Nombre del asistente. Requerido al crear/actualizar.
     */
    @NotBlank(message = "El nombre del asistente no puede estar vacío.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    /**
     * Email del asistente. Requerido y debe ser único.
     */
    @NotBlank(message = "El email del asistente не може бути порожнім.") // Corregido: mensaje en español
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String email;

    /**
     * Teléfono del asistente (opcional).
     */
    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefono;

    /**
     * Fecha de creación (informativo, generalmente no editable por el usuario).
     */
    private LocalDateTime fechaCreacion;

    /**
     * Constructor por defecto (necesario para frameworks como Jackson/JAX-RS).
     */
    public AsistenteDTO() {
    }

    // --- Getters y Setters ---
    public Integer getIdAsistente() {
        return idAsistente;
    }

    public void setIdAsistente(Integer idAsistente) {
        this.idAsistente = idAsistente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // --- equals y hashCode basados en ID (útil si se usan en colecciones) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AsistenteDTO that = (AsistenteDTO) o;
        // Compara por ID si ambos no son nulos
        return idAsistente != null && Objects.equals(idAsistente, that.idAsistente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAsistente);
    }

    // --- toString (útil para debugging) ---
    @Override
    public String toString() {
        return "AsistenteDTO{"
                + "idAsistente=" + idAsistente
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + ", telefono='" + telefono + '\''
                + '}';
    }
}
