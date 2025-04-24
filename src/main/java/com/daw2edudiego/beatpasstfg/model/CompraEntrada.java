package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min; // Para validación
import jakarta.validation.constraints.NotNull; // Para validación
import jakarta.validation.constraints.Positive; // Para validación
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa una línea de detalle dentro de una compra.
 * Especifica qué tipo de entrada se compró, cuántas unidades y a qué precio
 * unitario. Mapea la tabla 'compra_entradas'.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "compra_entradas")
public class CompraEntrada implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único del detalle de compra (clave primaria). Generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra_entrada")
    private Integer idCompraEntrada;

    /**
     * Cantidad de entradas de este tipo compradas en esta línea. No puede ser
     * nulo y debe ser al menos 1.
     */
    @NotNull(message = "La cantidad no puede ser nula.") // Validación
    @Min(value = 1, message = "La cantidad debe ser al menos 1.") // Validación
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Precio unitario de la entrada en el momento de la compra. Se almacena
     * aquí para mantener el histórico de precios. No puede ser nulo y debe ser
     * un valor positivo. Mapeado a DECIMAL(8,2) en la base de datos.
     */
    @NotNull(message = "El precio unitario no puede ser nulo.") // Validación
    @Positive(message = "El precio unitario debe ser positivo.") // Validación
    @Column(name = "precio_unitario", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * La compra a la que pertenece este detalle. Relación muchos a uno con
     * Compra. La columna 'id_compra' no puede ser nula. Fetch LAZY: La compra
     * no se carga hasta que se accede explícitamente.
     */
    @NotNull(message = "El detalle de compra debe estar asociado a una compra.") // Validación (en el objeto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compra", nullable = false) // Columna FK en la BD
    private Compra compra;

    /**
     * El tipo de entrada que se compró en este detalle. Relación muchos a uno
     * con Entrada. La columna 'id_entrada' no puede ser nula. Fetch LAZY: El
     * tipo de entrada no se carga hasta que se accede explícitamente.
     */
    @NotNull(message = "El detalle de compra debe estar asociado a un tipo de entrada.") // Validación (en el objeto)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_entrada", nullable = false) // Columna FK en la BD
    private Entrada entrada;

    /**
     * Conjunto de entradas individuales (asignadas) generadas a partir de este
     * detalle de compra. Si cantidad es 3, aquí habrá 3 objetos
     * EntradaAsignada. Relación uno a muchos (inversa de
     * EntradaAsignada.compraEntrada). Cascade ALL: Las operaciones sobre
     * CompraEntrada se propagan a sus EntradasAsignadas. Fetch LAZY: Las
     * entradas asignadas no se cargan hasta que se acceden explícitamente.
     * orphanRemoval true: Si se elimina una EntradaAsignada de este Set, se
     * elimina de la BD.
     */
    @OneToMany(mappedBy = "compraEntrada", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<EntradaAsignada> entradasAsignadasGeneradas = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public CompraEntrada() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID del detalle de compra.
     *
     * @return El ID del detalle.
     */
    public Integer getIdCompraEntrada() {
        return idCompraEntrada;
    }

    /**
     * Establece el ID del detalle de compra.
     *
     * @param idCompraEntrada El nuevo ID del detalle.
     */
    public void setIdCompraEntrada(Integer idCompraEntrada) {
        this.idCompraEntrada = idCompraEntrada;
    }

    /**
     * Obtiene la cantidad de entradas compradas en esta línea.
     *
     * @return La cantidad.
     */
    public Integer getCantidad() {
        return cantidad;
    }

    /**
     * Establece la cantidad de entradas compradas en esta línea.
     *
     * @param cantidad La nueva cantidad.
     */
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * Obtiene el precio unitario de la entrada en el momento de la compra.
     *
     * @return El precio unitario.
     */
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    /**
     * Establece el precio unitario de la entrada en el momento de la compra.
     *
     * @param precioUnitario El nuevo precio unitario.
     */
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    /**
     * Obtiene la compra a la que pertenece este detalle.
     *
     * @return El objeto Compra asociado.
     */
    public Compra getCompra() {
        return compra;
    }

    /**
     * Establece la compra a la que pertenece este detalle.
     *
     * @param compra El objeto Compra a asociar.
     */
    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    /**
     * Obtiene el tipo de entrada asociado a este detalle.
     *
     * @return El objeto Entrada asociado.
     */
    public Entrada getEntrada() {
        return entrada;
    }

    /**
     * Establece el tipo de entrada asociado a este detalle.
     *
     * @param entrada El objeto Entrada a asociar.
     */
    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }

    /**
     * Obtiene el conjunto de entradas individuales generadas a partir de este
     * detalle.
     *
     * @return Un Set de objetos EntradaAsignada.
     */
    public Set<EntradaAsignada> getEntradasAsignadasGeneradas() {
        return entradasAsignadasGeneradas;
    }

    /**
     * Establece el conjunto de entradas individuales generadas a partir de este
     * detalle.
     *
     * @param entradasAsignadasGeneradas El nuevo Set de objetos
     * EntradaAsignada.
     */
    public void setEntradasAsignadasGeneradas(Set<EntradaAsignada> entradasAsignadasGeneradas) {
        this.entradasAsignadasGeneradas = entradasAsignadasGeneradas;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara este CompraEntrada con otro objeto para determinar si son
     * iguales. Dos detalles de compra son iguales si tienen el mismo
     * idCompraEntrada.
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
        CompraEntrada that = (CompraEntrada) o;
        return idCompraEntrada != null && Objects.equals(idCompraEntrada, that.idCompraEntrada);
    }

    /**
     * Calcula el código hash para este CompraEntrada. Se basa únicamente en el
     * idCompraEntrada.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idCompraEntrada);
    }

    /**
     * Devuelve una representación en cadena de este CompraEntrada. Incluye id,
     * cantidad, precio unitario, ID de compra y ID de entrada.
     *
     * @return Una cadena representando el CompraEntrada.
     */
    @Override
    public String toString() {
        return "CompraEntrada{"
                + "idCompraEntrada=" + idCompraEntrada
                + ", cantidad=" + cantidad
                + ", precioUnitario=" + precioUnitario
                + ", compraId=" + (compra != null ? compra.getIdCompra() : "null")
                + ", entradaId=" + (entrada != null ? entrada.getIdEntrada() : "null")
                + '}';
    }
}
