package com.beatpass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO para la creación de un festival por parte de un Administrador. Incluye el
 * ID del promotor al que se asignará el festival.
 */
public class AdminFestivalCreacionDTO {

    @NotBlank(message = "El nombre del festival no puede estar vacío.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La fecha de inicio es obligatoria.")
    // @FutureOrPresent(message = "La fecha de inicio no puede ser pasada.") // Considerar si aplica para festivales históricos
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria.")
    private LocalDate fechaFin;

    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres.")
    private String ubicacion;

    @Positive(message = "El aforo debe ser un número positivo.")
    private Integer aforo;

    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres.")
    private String imagenUrl;

    @NotNull(message = "Se requiere seleccionar un promotor.")
    private Integer idPromotorSeleccionado;

    public AdminFestivalCreacionDTO() {
    }

    // Getters y Setters
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

    public Integer getIdPromotorSeleccionado() {
        return idPromotorSeleccionado;
    }

    public void setIdPromotorSeleccionado(Integer idPromotorSeleccionado) {
        this.idPromotorSeleccionado = idPromotorSeleccionado;
    }

    @Override
    public String toString() {
        return "AdminFestivalCreacionDTO{"
                + "nombre='" + nombre + '\''
                + ", fechaInicio=" + fechaInicio
                + ", fechaFin=" + fechaFin
                + ", idPromotorSeleccionado=" + idPromotorSeleccionado
                + '}';
    }
}
