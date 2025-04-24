package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull; // Para validación
import jakarta.validation.constraints.PositiveOrZero; // Para validación
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa la cabecera de una compra de entradas realizada
 * por un asistente. Contiene información general de la transacción y la
 * referencia al asistente. Mapea la tabla 'compras'.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "compras")
public class Compra implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único de la compra (clave primaria). Generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Integer idCompra;

    /**
     * Fecha y hora en que se realizó la compra. Gestionado automáticamente por
     * la base de datos (DEFAULT CURRENT_TIMESTAMP). No insertable ni
     * actualizable desde JPA.
     */
    @Column(name = "fecha_compra", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCompra;

    /**
     * Importe total de la compra. No puede ser nulo y debe ser un valor
     * positivo o cero. Mapeado a DECIMAL(10,2) en la base de datos.
     */
    @NotNull(message = "El total de la compra no puede ser nulo.") // Validación
    @PositiveOrZero(message = "El total de la compra debe ser positivo o cero.") // Validación
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * El asistente que realizó la compra. Relación muchos a uno con Asistente.
     * La columna 'id_asistente' no puede ser nula. Fetch LAZY: El asistente no
     * se carga hasta que se accede explícitamente.
     */
    @NotNull(message = "La compra debe estar asociada a un asistente.") // Validación (en el objeto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional=false refuerza not null
    @JoinColumn(name = "id_asistente", nullable = false) // Columna FK en la BD
    private Asistente asistente;

    /**
     * Conjunto de detalles (líneas) de esta compra. Cada detalle especifica un
     * tipo de entrada y la cantidad comprada. Relación uno a muchos (inversa de
     * CompraEntrada.compra). Cascade ALL: Las operaciones sobre Compra se
     * propagan a sus detalles. Fetch LAZY: Los detalles no se cargan hasta que
     * se acceden explícitamente. orphanRemoval true: Si se elimina un
     * CompraEntrada de este Set, se elimina de la BD.
     */
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CompraEntrada> detallesCompra = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Compra() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID de la compra.
     *
     * @return El ID de la compra.
     */
    public Integer getIdCompra() {
        return idCompra;
    }

    /**
     * Establece el ID de la compra.
     *
     * @param idCompra El nuevo ID de la compra.
     */
    public void setIdCompra(Integer idCompra) {
        this.idCompra = idCompra;
    }

    /**
     * Obtiene la fecha y hora de la compra.
     *
     * @return La fecha y hora de la compra.
     */
    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    /**
     * Establece la fecha y hora de la compra. (Nota: Generalmente gestionado
     * por la BD, usar con precaución).
     *
     * @param fechaCompra La nueva fecha y hora de la compra.
     */
    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    /**
     * Obtiene el importe total de la compra.
     *
     * @return El importe total.
     */
    public BigDecimal getTotal() {
        return total;
    }

    /**
     * Establece el importe total de la compra.
     *
     * @param total El nuevo importe total.
     */
    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    /**
     * Obtiene el asistente que realizó la compra.
     *
     * @return El objeto Asistente asociado.
     */
    public Asistente getAsistente() {
        return asistente;
    }

    /**
     * Establece el asistente que realizó la compra.
     *
     * @param asistente El objeto Asistente a asociar.
     */
    public void setAsistente(Asistente asistente) {
        this.asistente = asistente;
    }

    /**
     * Obtiene el conjunto de detalles (líneas) de la compra.
     *
     * @return Un Set de objetos CompraEntrada.
     */
    public Set<CompraEntrada> getDetallesCompra() {
        return detallesCompra;
    }

    /**
     * Establece el conjunto de detalles de la compra.
     *
     * @param detallesCompra El nuevo Set de objetos CompraEntrada.
     */
    public void setDetallesCompra(Set<CompraEntrada> detallesCompra) {
        this.detallesCompra = detallesCompra;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara esta Compra con otro objeto para determinar si son iguales. Dos
     * compras son iguales si tienen el mismo idCompra.
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
        Compra compra = (Compra) o;
        return idCompra != null && Objects.equals(idCompra, compra.idCompra);
    }

    /**
     * Calcula el código hash para esta Compra. Se basa únicamente en el
     * idCompra.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idCompra);
    }

    /**
     * Devuelve una representación en cadena de esta Compra. Incluye id, fecha,
     * total y el ID del asistente asociado.
     *
     * @return Una cadena representando la Compra.
     */
    @Override
    public String toString() {
        return "Compra{"
                + "idCompra=" + idCompra
                + ", fechaCompra=" + fechaCompra
                + ", total=" + total
                + ", asistenteId=" + (asistente != null ? asistente.getIdAsistente() : "null")
                + '}';
    }
}
