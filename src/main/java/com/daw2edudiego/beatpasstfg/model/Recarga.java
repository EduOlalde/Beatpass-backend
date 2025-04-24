package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Validación
import jakarta.validation.constraints.NotNull; // Validación
import jakarta.validation.constraints.Positive; // Validación
import jakarta.validation.constraints.Size; // Validación
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA que representa una operación de recarga de saldo en una
 * {@link PulseraNFC}. Mapea la tabla 'recargas'.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "recargas")
public class Recarga implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único de la recarga (clave primaria). Generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recarga")
    private Integer idRecarga;

    /**
     * Monto (importe) de la recarga. No puede ser nulo y debe ser un valor
     * positivo. Mapeado a DECIMAL(8,2).
     */
    @NotNull(message = "El monto de la recarga no puede ser nulo.") // Validación
    @Positive(message = "El monto de la recarga debe ser positivo.") // Validación
    @Column(name = "monto", nullable = false, precision = 8, scale = 2)
    private BigDecimal monto;

    /**
     * Fecha y hora en que se realizó la recarga. Gestionado automáticamente por
     * la base de datos (DEFAULT CURRENT_TIMESTAMP). No insertable ni
     * actualizable desde JPA.
     */
    @Column(name = "fecha", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fecha;

    /**
     * Método de pago utilizado para la recarga (ej: "Efectivo", "Tarjeta",
     * "Online"). Opcional, longitud máxima de 50 caracteres.
     */
    @Size(max = 50, message = "El método de pago no puede exceder los 50 caracteres.") // Validación
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    /**
     * La pulsera NFC a la que se aplicó la recarga. Relación muchos a uno con
     * PulseraNFC. La columna 'id_pulsera' no puede ser nula. Fetch LAZY: La
     * pulsera no se carga hasta que se accede explícitamente.
     */
    @NotNull(message = "La recarga debe estar asociada a una pulsera.") // Validación (en el objeto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pulsera", nullable = false) // Columna FK en la BD
    private PulseraNFC pulseraNFC;

    /**
     * El usuario (cajero/operador) que realizó la recarga en un punto físico
     * (opcional). Si es null, puede indicar una recarga online o automática.
     * Relación muchos a uno con Usuario. La columna 'id_usuario_cajero' permite
     * nulos. Fetch LAZY: El usuario no se carga hasta que se accede
     * explícitamente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cajero") // Permite NULL
    private Usuario usuarioCajero;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Recarga() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID de la recarga. @return El ID.
     */
    public Integer getIdRecarga() {
        return idRecarga;
    }

    /**
     * Establece el ID de la recarga. @param idRecarga El nuevo ID.
     */
    public void setIdRecarga(Integer idRecarga) {
        this.idRecarga = idRecarga;
    }

    /**
     * Obtiene el monto de la recarga. @return El monto.
     */
    public BigDecimal getMonto() {
        return monto;
    }

    /**
     * Establece el monto de la recarga. @param monto El nuevo monto.
     */
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    /**
     * Obtiene la fecha de la recarga. @return La fecha y hora.
     */
    public LocalDateTime getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha de la recarga. (Generalmente no necesario). @param
     * fecha La nueva fecha.
     */
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene el método de pago. @return El método de pago, o null.
     */
    public String getMetodoPago() {
        return metodoPago;
    }

    /**
     * Establece el método de pago. @param metodoPago El nuevo método de pago.
     */
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    /**
     * Obtiene la pulsera asociada a la recarga. @return La PulseraNFC.
     */
    public PulseraNFC getPulseraNFC() {
        return pulseraNFC;
    }

    /**
     * Establece la pulsera asociada a la recarga. @param pulseraNFC La
     * PulseraNFC.
     */
    public void setPulseraNFC(PulseraNFC pulseraNFC) {
        this.pulseraNFC = pulseraNFC;
    }

    /**
     * Obtiene el usuario (cajero) que realizó la recarga. @return El Usuario, o
     * null.
     */
    public Usuario getUsuarioCajero() {
        return usuarioCajero;
    }

    /**
     * Establece el usuario (cajero) que realizó la recarga. @param
     * usuarioCajero El Usuario.
     */
    public void setUsuarioCajero(Usuario usuarioCajero) {
        this.usuarioCajero = usuarioCajero;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara esta Recarga con otro objeto para determinar si son iguales. Dos
     * recargas son iguales si tienen el mismo idRecarga.
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
        Recarga recarga = (Recarga) o;
        return idRecarga != null && Objects.equals(idRecarga, recarga.idRecarga);
    }

    /**
     * Calcula el código hash para esta Recarga. Se basa únicamente en el
     * idRecarga.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idRecarga);
    }

    /**
     * Devuelve una representación en cadena de esta Recarga. Incluye id, monto,
     * fecha y el ID de la pulsera asociada.
     *
     * @return Una cadena representando la Recarga.
     */
    @Override
    public String toString() {
        return "Recarga{"
                + "idRecarga=" + idRecarga
                + ", monto=" + monto
                + ", fecha=" + fecha
                + ", metodoPago='" + metodoPago + '\''
                + ", pulseraId=" + (pulseraNFC != null ? pulseraNFC.getIdPulsera() : "null")
                + ", cajeroId=" + (usuarioCajero != null ? usuarioCajero.getIdUsuario() : "null")
                + '}';
    }
}
