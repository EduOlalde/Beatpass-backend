package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent; // Para validación de fechas
import jakarta.validation.constraints.NotBlank; // Para validación
import jakarta.validation.constraints.NotNull; // Para validación
import jakarta.validation.constraints.Size; // Para validación
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa un evento de festival. Contiene información sobre
 * el nombre, fechas, ubicación, promotor asociado, etc. Mapea la tabla
 * 'festivales'.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "festivales")
public class Festival implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único del festival (clave primaria). Generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_festival")
    private Integer idFestival;

    /**
     * Nombre oficial del festival. No puede ser nulo/vacío y tiene longitud
     * máxima de 100 caracteres.
     */
    @NotBlank(message = "El nombre del festival no puede estar vacío.") // Validación
    @Size(max = 100, message = "El nombre del festival no puede exceder los 100 caracteres.") // Validación
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Descripción detallada del festival (opcional). Mapeado como TEXT en la
     * base de datos.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT") // @Lob no es estándar para String
    private String descripcion;

    /**
     * Fecha de inicio del festival. No puede ser nula.
     */
    @NotNull(message = "La fecha de inicio no puede ser nula.") // Validación
    //@FutureOrPresent(message = "La fecha de inicio debe ser hoy o en el futuro.") // Validación (Considerar si aplica siempre)
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    /**
     * Fecha de fin del festival. No puede ser nula y debe ser igual o posterior
     * a la fecha de inicio. (La validación de posterioridad se haría en la
     * lógica de servicio).
     */
    @NotNull(message = "La fecha de fin no puede ser nula.") // Validación
    //@FutureOrPresent(message = "La fecha de fin debe ser hoy o en el futuro.") // Validación (Considerar si aplica siempre)
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    /**
     * Ubicación física donde se celebra el festival (opcional). Longitud máxima
     * de 255 caracteres.
     */
    @Size(max = 255, message = "La ubicación no puede exceder los 255 caracteres.") // Validación
    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    /**
     * Aforo máximo estimado del festival (opcional).
     */
    @Column(name = "aforo")
    private Integer aforo; // Podría validarse como @Positive

    /**
     * URL de la imagen principal o cartel del festival (opcional). Longitud
     * máxima de 255 caracteres.
     */
    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres.") // Validación
    // @URL(message = "La URL de la imagen no es válida.") // Validación (si se quiere validar formato URL)
    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    /**
     * Estado actual del festival (BORRADOR, PUBLICADO, CANCELADO, FINALIZADO).
     * Mapeado como ENUM en la base de datos, con valor por defecto 'BORRADOR'.
     * No puede ser nulo.
     */
    @NotNull(message = "El estado del festival no puede ser nulo.") // Validación
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('BORRADOR', 'PUBLICADO', 'CANCELADO', 'FINALIZADO') DEFAULT 'BORRADOR'")
    private EstadoFestival estado = EstadoFestival.BORRADOR;

    /**
     * Fecha y hora de creación del registro del festival. Gestionado
     * automáticamente por la base de datos (DEFAULT CURRENT_TIMESTAMP). No
     * insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación del registro del festival.
     * Gestionado automáticamente por la base de datos (ON UPDATE
     * CURRENT_TIMESTAMP). No insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    /**
     * El usuario (promotor) que organiza y gestiona este festival. Relación
     * muchos a uno con Usuario. La columna 'id_promotor' no puede ser nula.
     * Fetch LAZY: El promotor no se carga hasta que se accede explícitamente.
     */
    @NotNull(message = "El festival debe estar asociado a un promotor.") // Validación (en el objeto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_promotor", nullable = false) // Columna FK en la BD
    private Usuario promotor;

    /**
     * Conjunto de tipos de entrada definidos para este festival. Relación uno a
     * muchos (inversa de Entrada.festival). Cascade ALL: Las operaciones sobre
     * Festival se propagan a sus Tipos de Entrada. Fetch LAZY: Los tipos de
     * entrada no se cargan hasta que se acceden explícitamente. orphanRemoval
     * true: Si se elimina un Entrada de este Set, se elimina de la BD.
     */
    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Entrada> tiposEntrada = new HashSet<>();

    /**
     * Conjunto de consumos realizados dentro de este festival. Relación uno a
     * muchos (inversa de Consumo.festival). Cascade ALL: Las operaciones sobre
     * Festival se propagan a sus Consumos. Fetch LAZY: Los consumos no se
     * cargan hasta que se acceden explícitamente. orphanRemoval true: Si se
     * elimina un Consumo de este Set, se elimina de la BD.
     */
    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Consumo> consumos = new HashSet<>();

    /**
     * Estadísticas agregadas para este festival (opcional). Relación uno a uno
     * bidireccional con EstadisticasFestival. Festival es el lado inverso. La
     * relación se define en EstadisticasFestival mediante `@MapsId` y
     * `@JoinColumn`. Cascade ALL: Las operaciones sobre Festival se propagan a
     * sus Estadisticas. Fetch LAZY: Las estadísticas no se cargan hasta que se
     * acceden explícitamente. `optional = true` porque un festival puede no
     * tener estadísticas aún.
     */
    @OneToOne(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true, orphanRemoval = true)
    private EstadisticasFestival estadisticas;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Festival() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID del festival. @return El ID.
     */
    public Integer getIdFestival() {
        return idFestival;
    }

    /**
     * Establece el ID del festival. @param idFestival El nuevo ID.
     */
    public void setIdFestival(Integer idFestival) {
        this.idFestival = idFestival;
    }

    /**
     * Obtiene el nombre del festival. @return El nombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del festival. @param nombre El nuevo nombre.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripción del festival. @return La descripción.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción del festival. @param descripcion La nueva
     * descripción.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la fecha de inicio del festival. @return La fecha de inicio.
     */
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    /**
     * Establece la fecha de inicio del festival. @param fechaInicio La nueva
     * fecha de inicio.
     */
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /**
     * Obtiene la fecha de fin del festival. @return La fecha de fin.
     */
    public LocalDate getFechaFin() {
        return fechaFin;
    }

    /**
     * Establece la fecha de fin del festival. @param fechaFin La nueva fecha de
     * fin.
     */
    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    /**
     * Obtiene la ubicación del festival. @return La ubicación.
     */
    public String getUbicacion() {
        return ubicacion;
    }

    /**
     * Establece la ubicación del festival. @param ubicacion La nueva ubicación.
     */
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    /**
     * Obtiene el aforo del festival. @return El aforo.
     */
    public Integer getAforo() {
        return aforo;
    }

    /**
     * Establece el aforo del festival. @param aforo El nuevo aforo.
     */
    public void setAforo(Integer aforo) {
        this.aforo = aforo;
    }

    /**
     * Obtiene la URL de la imagen del festival. @return La URL de la imagen.
     */
    public String getImagenUrl() {
        return imagenUrl;
    }

    /**
     * Establece la URL de la imagen del festival. @param imagenUrl La nueva
     * URL.
     */
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    /**
     * Obtiene el estado actual del festival. @return El estado.
     */
    public EstadoFestival getEstado() {
        return estado;
    }

    /**
     * Establece el estado actual del festival. @param estado El nuevo estado.
     */
    public void setEstado(EstadoFestival estado) {
        this.estado = estado;
    }

    /**
     * Obtiene la fecha de creación del festival. @return La fecha de creación.
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Obtiene la fecha de la última modificación del festival. @return La fecha
     * de modificación.
     */
    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    /**
     * Obtiene el promotor del festival. @return El Usuario promotor.
     */
    public Usuario getPromotor() {
        return promotor;
    }

    /**
     * Establece el promotor del festival. @param promotor El nuevo Usuario
     * promotor.
     */
    public void setPromotor(Usuario promotor) {
        this.promotor = promotor;
    }

    /**
     * Obtiene los tipos de entrada del festival. @return Un Set de Entrada.
     */
    public Set<Entrada> getTiposEntrada() {
        return tiposEntrada;
    }

    /**
     * Establece los tipos de entrada del festival. @param tiposEntrada El nuevo
     * Set de Entrada.
     */
    public void setTiposEntrada(Set<Entrada> tiposEntrada) {
        this.tiposEntrada = tiposEntrada;
    }

    /**
     * Obtiene los consumos registrados en el festival. @return Un Set de
     * Consumo.
     */
    public Set<Consumo> getConsumos() {
        return consumos;
    }

    /**
     * Establece los consumos registrados en el festival. @param consumos El
     * nuevo Set de Consumo.
     */
    public void setConsumos(Set<Consumo> consumos) {
        this.consumos = consumos;
    }

    /**
     * Obtiene las estadísticas agregadas del festival. @return Las
     * EstadisticasFestival.
     */
    public EstadisticasFestival getEstadisticas() {
        return estadisticas;
    }

    /**
     * Establece las estadísticas agregadas del festival. Importante: También
     * establece la referencia inversa en EstadisticasFestival.
     *
     * @param estadisticas Las nuevas EstadisticasFestival.
     */
    public void setEstadisticas(EstadisticasFestival estadisticas) {
        this.estadisticas = estadisticas;
        if (estadisticas != null) {
            estadisticas.setFestival(this); // Mantener la bidireccionalidad
        }
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara este Festival con otro objeto para determinar si son iguales. Dos
     * festivales son iguales si tienen el mismo idFestival.
     *
     * @param o El objeto a comparar.
     * @return true si los objetos son iguales, false en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Festival festival = (Festival) o;
        return idFestival != null && Objects.equals(idFestival, festival.idFestival);
    }

    /**
     * Calcula el código hash para este Festival. Se basa únicamente en el
     * idFestival.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idFestival);
    }

    /**
     * Devuelve una representación en cadena de este Festival. Incluye id,
     * nombre, fechas y estado.
     *
     * @return Una cadena representando el Festival.
     */
    @Override
    public String toString() {
        return "Festival{"
                + "idFestival=" + idFestival
                + ", nombre='" + nombre + '\''
                + ", fechaInicio=" + fechaInicio
                + ", fechaFin=" + fechaFin
                + ", estado=" + estado
                + ", promotorId=" + (promotor != null ? promotor.getIdUsuario() : "null")
                + '}';
    }
}
