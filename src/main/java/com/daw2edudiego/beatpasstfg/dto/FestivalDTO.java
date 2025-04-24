package com.daw2edudiego.beatpasstfg.dto;

import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.fasterxml.jackson.annotation.JsonFormat; // Para formateo de fechas en JSON
import com.fasterxml.jackson.annotation.JsonInclude; // Para excluir nulos
import jakarta.validation.constraints.*; // Validaciones
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) para representar la información de un
 * {@link com.daw2edudiego.beatpasstfg.model.Festival}. Utilizado para
 * transferir datos entre capas y para la entrada/salida en la API REST. Incluye
 * validaciones para la creación/actualización de festivales.
 *
 * @author Eduardo Olalde
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Opcional: Omite campos nulos en JSON
public class FestivalDTO {

    /**
     * ID del festival (null al crear, presente al mostrar/actualizar).
     */
    private Integer idFestival;

    /**
     * Nombre del festival. Obligatorio.
     */
    @NotBlank(message = "El nombre del festival no puede estar vacío.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    /**
     * Descripción del festival (opcional).
     */
    private String descripcion;

    /**
     * Fecha de inicio. Obligatoria. Formato YYYY-MM-DD.
     */
    @NotNull(message = "La fecha de inicio es obligatoria.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // Formato para JSON
    private LocalDate fechaInicio;

    /**
     * Fecha de fin. Obligatoria. Formato YYYY-MM-DD.
     */
    @NotNull(message = "La fecha de fin es obligatoria.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // Formato para JSON
    // @AssertTrue(message = "La fecha de fin debe ser igual o posterior a la fecha de inicio.")
    // La validación AssertTrue requiere un método getter boolean isFechaFinValida()
    private LocalDate fechaFin;

    /**
     * Ubicación (opcional).
     */
    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres.")
    private String ubicacion;

    /**
     * Aforo (opcional, debe ser positivo si se indica).
     */
    @Positive(message = "El aforo debe ser un número positivo.")
    private Integer aforo;

    /**
     * URL de la imagen (opcional, formato URL).
     */
    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres.")
    // @URL(message = "El formato de la URL de la imagen no es válido.") // Requiere dependencia hibernate-validator
    private String imagenUrl;

    /**
     * Estado del festival (generalmente gestionado por el sistema, no enviado
     * al crear).
     */
    private EstadoFestival estado;

    /**
     * ID del promotor asociado (obligatorio al crear si no se obtiene del
     * contexto de seguridad).
     */
    @NotNull(message = "Se requiere el ID del promotor.") // Puede ser opcional si se obtiene del usuario autenticado
    private Integer idPromotor;

    /**
     * Nombre del promotor (informativo, se puede añadir al consultar).
     */
    private String nombrePromotor;

    /**
     * Constructor por defecto.
     */
    public FestivalDTO() {
    }

    // --- Getters y Setters ---
    // (Omitidos por brevedad, pero deben estar presentes todos los getters y setters)
    public Integer getIdFestival() {
        return idFestival;
    }

    public void setIdFestival(Integer idFestival) {
        this.idFestival = idFestival;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getAforo() {
        return aforo;
    }

    public void setAforo(Integer aforo) {
        this.aforo = aforo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public EstadoFestival getEstado() {
        return estado;
    }

    public void setEstado(EstadoFestival estado) {
        this.estado = estado;
    }

    public Integer getIdPromotor() {
        return idPromotor;
    }

    public void setIdPromotor(Integer idPromotor) {
        this.idPromotor = idPromotor;
    }

    public String getNombrePromotor() {
        return nombrePromotor;
    }

    public void setNombrePromotor(String nombrePromotor) {
        this.nombrePromotor = nombrePromotor;
    }

    // --- Método de Validación Adicional (Ejemplo) ---
    /**
     * Método para validación personalizada (ej: fecha fin >= fecha inicio).
     * Debe anotarse con @AssertTrue a nivel de clase si se usa Bean Validation.
     *
     * @return true si las fechas son válidas, false si no.
     */
    /*
    @AssertTrue(message = "La fecha de fin debe ser igual o posterior a la fecha de inicio.")
    public boolean isFechaFinValida() {
        // Permite fechas nulas (gestionado por @NotNull) pero si ambas existen, compara
        return fechaInicio == null || fechaFin == null || !fechaFin.isBefore(fechaInicio);
    }
     */
    // --- equals y hashCode basados en ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FestivalDTO that = (FestivalDTO) o;
        return Objects.equals(idFestival, that.idFestival);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFestival);
    }

    // --- toString ---
    @Override
    public String toString() {
        return "FestivalDTO{"
                + "idFestival=" + idFestival
                + ", nombre='" + nombre + '\''
                + ", fechaInicio=" + fechaInicio
                + ", fechaFin=" + fechaFin
                + ", estado=" + estado
                + ", idPromotor=" + idPromotor
                + '}';
    }
}
