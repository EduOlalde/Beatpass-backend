package com.beatpass.dto;

import com.beatpass.model.EstadoFestival;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO para representar la información de un Festival.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FestivalDTO {

    private Integer idFestival;

    @NotBlank(message = "El nombre del festival no puede estar vacío.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La fecha de inicio es obligatoria.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres.")
    private String ubicacion;

    @Positive(message = "El aforo debe ser un número positivo.")
    private Integer aforo;

    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres.")
    private String imagenUrl;

    private EstadoFestival estado;

    @NotNull(message = "Se requiere el ID del promotor.")
    private Integer idPromotor;

    private String nombrePromotor; // Informativo

    public FestivalDTO() {
    }

    // --- Getters y Setters ---
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

    // --- equals y hashCode ---
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
