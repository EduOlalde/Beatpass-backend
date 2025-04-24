package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min; // Para validación
import jakarta.validation.constraints.NotBlank; // Para validación
import jakarta.validation.constraints.NotNull; // Para validación
import jakarta.validation.constraints.PositiveOrZero; // Para validación
import jakarta.validation.constraints.Size; // Para validación
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa un tipo de entrada disponible para un festival
 * (ej: "Entrada General", "Abono VIP", "Entrada de Día"). Contiene información
 * sobre el tipo, precio y stock disponible. Mapea la tabla 'entradas'.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "entradas")
public class Entrada implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único del tipo de entrada (clave primaria). Generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada")
    private Integer idEntrada;

    /**
     * Nombre o tipo de la entrada (ej: "General", "VIP"). No puede ser
     * nulo/vacío y tiene longitud máxima de 50 caracteres.
     */
    @NotBlank(message = "El tipo de entrada no puede estar vacío.") // Validación
    @Size(max = 50, message = "El tipo de entrada no puede exceder los 50 caracteres.") // Validación
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    /**
     * Descripción detallada del tipo de entrada (opcional). Mapeado como TEXT
     * en la base de datos.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT") // @Lob no es estándar para String
    private String descripcion;

    /**
     * Precio de venta de este tipo de entrada. No puede ser nulo y debe ser
     * positivo o cero. Mapeado a DECIMAL(8,2) en la base de datos.
     */
    @NotNull(message = "El precio de la entrada no puede ser nulo.") // Validación
    @PositiveOrZero(message = "El precio de la entrada debe ser positivo o cero.") // Validación
    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    /**
     * Número de unidades disponibles (stock) de este tipo de entrada. No puede
     * ser nulo y debe ser cero o un entero positivo. Este valor se decrementará
     * con cada venta (o generación de EntradaAsignada).
     */
    @NotNull(message = "El stock no puede ser nulo.") // Validación
    @Min(value = 0, message = "El stock no puede ser negativo.") // Validación
    @Column(name = "stock", nullable = false)
    private Integer stock;

    /**
     * Fecha y hora de creación del registro de este tipo de entrada. Gestionado
     * automáticamente por la base de datos (DEFAULT CURRENT_TIMESTAMP). No
     * insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación del registro de este tipo de
     * entrada. Gestionado automáticamente por la base de datos (ON UPDATE
     * CURRENT_TIMESTAMP). No insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    /**
     * El festival al que pertenece este tipo de entrada. Relación muchos a uno
     * con Festival. La columna 'id_festival' no puede ser nula. Fetch LAZY: El
     * festival no se carga hasta que se accede explícitamente.
     */
    @NotNull(message = "El tipo de entrada debe estar asociado a un festival.") // Validación (en el objeto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_festival", nullable = false) // Columna FK en la BD
    private Festival festival;

    /**
     * Conjunto de detalles de compra donde aparece este tipo de entrada.
     * Relación uno a muchos (inversa de CompraEntrada.entrada). Cascade ALL:
     * Las operaciones sobre Entrada se propagan a sus CompraEntrada (¡CUIDADO!
     * Borrar un tipo de entrada borraría las líneas de compra asociadas).
     * Considerar si es el comportamiento deseado. Fetch LAZY: Los detalles de
     * compra no se cargan hasta que se acceden explícitamente. orphanRemoval
     * true: Si se elimina un CompraEntrada de este Set, se elimina de la BD.
     */
    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CompraEntrada> comprasDondeAparece = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Entrada() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID del tipo de entrada.
     *
     * @return El ID del tipo de entrada.
     */
    public Integer getIdEntrada() {
        return idEntrada;
    }

    /**
     * Establece el ID del tipo de entrada.
     *
     * @param idEntrada El nuevo ID del tipo de entrada.
     */
    public void setIdEntrada(Integer idEntrada) {
        this.idEntrada = idEntrada;
    }

    /**
     * Obtiene el nombre/tipo de la entrada.
     *
     * @return El tipo de entrada.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Establece el nombre/tipo de la entrada.
     *
     * @param tipo El nuevo tipo de entrada.
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Obtiene la descripción de la entrada.
     *
     * @return La descripción, o null si no tiene.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción de la entrada.
     *
     * @param descripcion La nueva descripción.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el precio de la entrada.
     *
     * @return El precio.
     */
    public BigDecimal getPrecio() {
        return precio;
    }

    /**
     * Establece el precio de la entrada.
     *
     * @param precio El nuevo precio.
     */
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    /**
     * Obtiene el stock disponible de este tipo de entrada.
     *
     * @return El stock actual.
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * Establece el stock disponible de este tipo de entrada.
     *
     * @param stock El nuevo stock.
     */
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    /**
     * Obtiene la fecha de creación del tipo de entrada.
     *
     * @return La fecha y hora de creación.
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Obtiene la fecha de la última modificación del tipo de entrada.
     *
     * @return La fecha y hora de la última modificación.
     */
    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    /**
     * Obtiene el festival al que pertenece este tipo de entrada.
     *
     * @return El objeto Festival asociado.
     */
    public Festival getFestival() {
        return festival;
    }

    /**
     * Establece el festival al que pertenece este tipo de entrada.
     *
     * @param festival El objeto Festival a asociar.
     */
    public void setFestival(Festival festival) {
        this.festival = festival;
    }

    /**
     * Obtiene el conjunto de detalles de compra donde se incluye este tipo de
     * entrada.
     *
     * @return Un Set de objetos CompraEntrada.
     */
    public Set<CompraEntrada> getComprasDondeAparece() {
        return comprasDondeAparece;
    }

    /**
     * Establece el conjunto de detalles de compra donde se incluye este tipo de
     * entrada.
     *
     * @param comprasDondeAparece El nuevo Set de objetos CompraEntrada.
     */
    public void setComprasDondeAparece(Set<CompraEntrada> comprasDondeAparece) {
        this.comprasDondeAparece = comprasDondeAparece;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara esta Entrada con otro objeto para determinar si son iguales. Dos
     * tipos de entrada son iguales si tienen el mismo idEntrada.
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
        Entrada entrada = (Entrada) o;
        return idEntrada != null && Objects.equals(idEntrada, entrada.idEntrada);
    }

    /**
     * Calcula el código hash para esta Entrada. Se basa únicamente en el
     * idEntrada.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idEntrada);
    }

    /**
     * Devuelve una representación en cadena de esta Entrada. Incluye id, tipo,
     * precio, stock y el ID del festival asociado.
     *
     * @return Una cadena representando la Entrada.
     */
    @Override
    public String toString() {
        return "Entrada{"
                + "idEntrada=" + idEntrada
                + ", tipo='" + tipo + '\''
                + ", precio=" + precio
                + ", stock=" + stock
                + ", festivalId=" + (festival != null ? festival.getIdFestival() : "null")
                + '}';
    }
}
