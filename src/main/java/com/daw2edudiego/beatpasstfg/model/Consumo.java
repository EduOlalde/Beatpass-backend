package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Para validación
import jakarta.validation.constraints.NotNull; // Para validación
import jakarta.validation.constraints.Positive; // Para validación
import jakarta.validation.constraints.Size; // Para validación
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA que representa un consumo (gasto) realizado con una pulsera NFC
 * dentro de un festival específico. Mapea la tabla 'consumos'.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "consumos")
public class Consumo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único del consumo (clave primaria). Generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consumo")
    private Integer idConsumo;

    /**
     * Descripción del consumo (ej: "Cerveza", "Bocadillo"). Mapeado como TEXT o
     * VARCHAR largo.
     */
    @NotBlank(message = "La descripción del consumo no puede estar vacía.") // Validación
    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres.") // Validación
    @Column(name = "descripcion", length = 255) // @Lob no es estándar para String, usar length
    private String descripcion;

    /**
     * Monto (importe) del consumo. No puede ser nulo y debe ser positivo.
     * Mapeado a DECIMAL(8,2) en la base de datos.
     */
    @NotNull(message = "El monto del consumo no puede ser nulo.") // Validación
    @Positive(message = "El monto del consumo debe ser positivo.") // Validación
    @Column(name = "monto", nullable = false, precision = 8, scale = 2)
    private BigDecimal monto;

    /**
     * Fecha y hora en que se registró el consumo. Gestionado automáticamente
     * por la base de datos (DEFAULT CURRENT_TIMESTAMP). No insertable ni
     * actualizable desde JPA.
     */
    @Column(name = "fecha", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fecha;

    /**
     * Identificador opcional del punto de venta donde se realizó el consumo.
     */
    @Column(name = "id_punto_venta")
    private Integer idPuntoVenta;

    /**
     * La pulsera NFC con la que se realizó el consumo. Relación muchos a uno
     * con PulseraNFC. La columna 'id_pulsera' no puede ser nula. Fetch LAZY: La
     * pulsera no se carga hasta que se accede explícitamente.
     */
    @NotNull(message = "El consumo debe estar asociado a una pulsera.") // Validación (en el objeto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pulsera", nullable = false) // Columna FK en la BD
    private PulseraNFC pulseraNFC;

    /**
     * El festival en el que ocurrió el consumo. Relación muchos a uno con
     * Festival. La columna 'id_festival' no puede ser nula. Fetch LAZY: El
     * festival no se carga hasta que se accede explícitamente.
     */
    @NotNull(message = "El consumo debe estar asociado a un festival.") // Validación (en el objeto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_festival", nullable = false) // Columna FK en la BD
    private Festival festival;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Consumo() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID del consumo.
     *
     * @return El ID del consumo.
     */
    public Integer getIdConsumo() {
        return idConsumo;
    }

    /**
     * Establece el ID del consumo.
     *
     * @param idConsumo El nuevo ID del consumo.
     */
    public void setIdConsumo(Integer idConsumo) {
        this.idConsumo = idConsumo;
    }

    /**
     * Obtiene la descripción del consumo.
     *
     * @return La descripción.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción del consumo.
     *
     * @param descripcion La nueva descripción.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el monto del consumo.
     *
     * @return El monto.
     */
    public BigDecimal getMonto() {
        return monto;
    }

    /**
     * Establece el monto del consumo.
     *
     * @param monto El nuevo monto.
     */
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    /**
     * Obtiene la fecha y hora del consumo.
     *
     * @return La fecha y hora.
     */
    public LocalDateTime getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha y hora del consumo. (Nota: Generalmente gestionado por
     * la BD, usar con precaución).
     *
     * @param fecha La nueva fecha y hora.
     */
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene el ID del punto de venta (opcional).
     *
     * @return El ID del punto de venta, o null si no está especificado.
     */
    public Integer getIdPuntoVenta() {
        return idPuntoVenta;
    }

    /**
     * Establece el ID del punto de venta.
     *
     * @param idPuntoVenta El nuevo ID del punto de venta.
     */
    public void setIdPuntoVenta(Integer idPuntoVenta) {
        this.idPuntoVenta = idPuntoVenta;
    }

    /**
     * Obtiene la pulsera NFC asociada al consumo.
     *
     * @return El objeto PulseraNFC asociado.
     */
    public PulseraNFC getPulseraNFC() {
        return pulseraNFC;
    }

    /**
     * Establece la pulsera NFC asociada al consumo.
     *
     * @param pulseraNFC El objeto PulseraNFC a asociar.
     */
    public void setPulseraNFC(PulseraNFC pulseraNFC) {
        this.pulseraNFC = pulseraNFC;
    }

    /**
     * Obtiene el festival asociado al consumo.
     *
     * @return El objeto Festival asociado.
     */
    public Festival getFestival() {
        return festival;
    }

    /**
     * Establece el festival asociado al consumo.
     *
     * @param festival El objeto Festival a asociar.
     */
    public void setFestival(Festival festival) {
        this.festival = festival;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara este Consumo con otro objeto para determinar si son iguales. Dos
     * consumos son iguales si tienen el mismo idConsumo.
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
        Consumo consumo = (Consumo) o;
        return idConsumo != null && Objects.equals(idConsumo, consumo.idConsumo);
    }

    /**
     * Calcula el código hash para este Consumo. Se basa únicamente en el
     * idConsumo.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idConsumo);
    }

    /**
     * Devuelve una representación en cadena de este Consumo. Incluye id,
     * descripción, monto, fecha, ID de pulsera y ID de festival.
     *
     * @return Una cadena representando el Consumo.
     */
    @Override
    public String toString() {
        return "Consumo{"
                + "idConsumo=" + idConsumo
                + ", descripcion='" + descripcion + '\''
                + ", monto=" + monto
                + ", fecha=" + fecha
                + ", pulseraId=" + (pulseraNFC != null ? pulseraNFC.getIdPulsera() : "null")
                + ", festivalId=" + (festival != null ? festival.getIdFestival() : "null")
                + '}';
    }
}
