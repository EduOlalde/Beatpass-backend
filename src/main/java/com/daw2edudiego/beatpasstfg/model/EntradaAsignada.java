package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Para validación
import jakarta.validation.constraints.NotNull; // Para validación
import jakarta.validation.constraints.Size; // Para validación
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA que representa una entrada individual generada a partir de una
 * compra. Esta entrada tiene un código QR único, un estado (ACTIVA, USADA,
 * CANCELADA), puede estar nominada a un asistente específico y puede asociarse
 * a una pulsera NFC. Mapea la tabla 'entradas_asignadas'.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "entradas_asignadas", uniqueConstraints = {
    @UniqueConstraint(columnNames = "codigo_qr", name = "uq_entradaasignada_codigoqr") // Constraint explícito
})
public class EntradaAsignada implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único de la entrada asignada (clave primaria). Generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada_asignada")
    private Integer idEntradaAsignada;

    /**
     * Código único (simula el contenido de un QR) asociado a esta entrada
     * individual. No puede ser nulo/vacío, debe ser único y tiene longitud
     * máxima de 255 caracteres.
     */
    @NotBlank(message = "El código QR no puede estar vacío.") // Validación
    @Size(max = 255, message = "El código QR no puede exceder los 255 caracteres.") // Validación
    @Column(name = "codigo_qr", nullable = false, unique = true, length = 255)
    private String codigoQr;

    /**
     * Estado actual de la entrada asignada (ACTIVA, USADA, CANCELADA). Mapeado
     * como ENUM en la base de datos, con valor por defecto 'ACTIVA'. No puede
     * ser nulo.
     */
    @NotNull(message = "El estado de la entrada asignada no puede ser nulo.") // Validación
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('ACTIVA', 'USADA', 'CANCELADA') DEFAULT 'ACTIVA'")
    private EstadoEntradaAsignada estado = EstadoEntradaAsignada.ACTIVA;

    /**
     * Fecha y hora en que la entrada fue asignada/nominada a un asistente
     * (opcional).
     */
    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    /**
     * Fecha y hora en que la entrada fue utilizada/validada (opcional).
     */
    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;

    /**
     * Fecha y hora de creación del registro de esta entrada asignada.
     * Gestionado automáticamente por la base de datos (DEFAULT
     * CURRENT_TIMESTAMP). No insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación del registro de esta entrada
     * asignada. Gestionado automáticamente por la base de datos (ON UPDATE
     * CURRENT_TIMESTAMP). No insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    /**
     * El detalle de compra del que se generó esta entrada asignada. Relación
     * muchos a uno con CompraEntrada. La columna 'id_compra_entrada' no puede
     * ser nula. Fetch LAZY: El detalle de compra no se carga hasta que se
     * accede explícitamente.
     */
    @NotNull(message = "La entrada asignada debe provenir de un detalle de compra.") // Validación (en el objeto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compra_entrada", nullable = false) // Columna FK en la BD
    private CompraEntrada compraEntrada;

    /**
     * El asistente al que está nominada esta entrada (opcional). Si es null, la
     * entrada aún no está nominada. Relación muchos a uno con Asistente. La
     * columna 'id_asistente' permite nulos. Fetch LAZY: El asistente no se
     * carga hasta que se accede explícitamente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asistente") // Permite NULL
    private Asistente asistente;

    /**
     * La pulsera NFC asociada a esta entrada (si existe). Relación uno a uno
     * con PulseraNFC. EntradaAsignada es el lado inverso (no propietario). La
     * relación se define en PulseraNFC mediante 'entradaAsignada'. `optional =
     * true` indica que una EntradaAsignada puede no tener pulsera. Se usan
     * CascadeType específicos para evitar borrados no deseados de la pulsera.
     * Fetch LAZY: La pulsera no se carga hasta que se accede explícitamente.
     */
    @OneToOne(mappedBy = "entradaAsignada", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY, optional = true)
    private PulseraNFC pulseraAsociada;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public EntradaAsignada() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID de la entrada asignada.
     *
     * @return El ID.
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
     * Obtiene el código QR único de la entrada.
     *
     * @return El código QR.
     */
    public String getCodigoQr() {
        return codigoQr;
    }

    /**
     * Establece el código QR único de la entrada.
     *
     * @param codigoQr El nuevo código QR.
     */
    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }

    /**
     * Obtiene el estado actual de la entrada (ACTIVA, USADA, CANCELADA).
     *
     * @return El estado.
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
     * Obtiene la fecha y hora de asignación/nominación.
     *
     * @return La fecha de asignación, o null si no está asignada.
     */
    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    /**
     * Establece la fecha y hora de asignación/nominación.
     *
     * @param fechaAsignacion La nueva fecha de asignación.
     */
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    /**
     * Obtiene la fecha y hora de uso/validación.
     *
     * @return La fecha de uso, o null si no ha sido usada.
     */
    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    /**
     * Establece la fecha y hora de uso/validación.
     *
     * @param fechaUso La nueva fecha de uso.
     */
    public void setFechaUso(LocalDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }

    /**
     * Obtiene la fecha de creación del registro.
     *
     * @return La fecha y hora de creación.
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Obtiene la fecha de la última modificación del registro.
     *
     * @return La fecha y hora de la última modificación.
     */
    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    /**
     * Obtiene el detalle de compra asociado.
     *
     * @return El objeto CompraEntrada.
     */
    public CompraEntrada getCompraEntrada() {
        return compraEntrada;
    }

    /**
     * Establece el detalle de compra asociado.
     *
     * @param compraEntrada El objeto CompraEntrada a asociar.
     */
    public void setCompraEntrada(CompraEntrada compraEntrada) {
        this.compraEntrada = compraEntrada;
    }

    /**
     * Obtiene el asistente al que está nominada la entrada.
     *
     * @return El objeto Asistente, o null si no está nominada.
     */
    public Asistente getAsistente() {
        return asistente;
    }

    /**
     * Establece el asistente al que se nomina la entrada.
     *
     * @param asistente El objeto Asistente a nominar.
     */
    public void setAsistente(Asistente asistente) {
        this.asistente = asistente;
    }

    /**
     * Obtiene la pulsera NFC asociada a esta entrada.
     *
     * @return El objeto PulseraNFC, o null si no hay ninguna asociada.
     */
    public PulseraNFC getPulseraAsociada() {
        return pulseraAsociada;
    }

    /**
     * Establece la pulsera NFC asociada a esta entrada. Es importante gestionar
     * también el lado propietario de la relación (en PulseraNFC).
     *
     * @param pulseraAsociada El objeto PulseraNFC a asociar.
     */
    public void setPulseraAsociada(PulseraNFC pulseraAsociada) {
        this.pulseraAsociada = pulseraAsociada;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara esta EntradaAsignada con otro objeto para determinar si son
     * iguales. Dos entradas asignadas son iguales si tienen el mismo
     * idEntradaAsignada.
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
        EntradaAsignada that = (EntradaAsignada) o;
        return idEntradaAsignada != null && Objects.equals(idEntradaAsignada, that.idEntradaAsignada);
    }

    /**
     * Calcula el código hash para esta EntradaAsignada. Se basa únicamente en
     * el idEntradaAsignada.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idEntradaAsignada);
    }

    /**
     * Devuelve una representación en cadena de esta EntradaAsignada. Incluye
     * id, código QR, estado y el ID del asistente asociado (si existe).
     *
     * @return Una cadena representando la EntradaAsignada.
     */
    @Override
    public String toString() {
        return "EntradaAsignada{"
                + "idEntradaAsignada=" + idEntradaAsignada
                + ", codigoQr='" + codigoQr + '\''
                + ", estado=" + estado
                + ", asistenteId=" + (asistente != null ? asistente.getIdAsistente() : "null")
                + ", pulseraId=" + (pulseraAsociada != null ? pulseraAsociada.getIdPulsera() : "null")
                + '}';
    }
}
