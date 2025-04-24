package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA que representa estadísticas agregadas para un festival. Esta
 * entidad es opcional y podría usarse para optimizar consultas que necesiten
 * calcular totales frecuentemente (ej: total vendido, total recaudado). Su
 * actualización debería gestionarse mediante triggers en la BD o lógica de
 * servicio. Mapea la tabla 'estadisticas_festival'. Utiliza una relación
 * OneToOne con Festival, donde el ID de esta entidad es también la clave
 * foránea a Festival (@MapsId).
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "estadisticas_festival")
public class EstadisticasFestival implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador de las estadísticas, que coincide con el ID del festival
     * asociado. Es la clave primaria de esta tabla y también una clave foránea
     * a 'festivales'.
     */
    @Id
    @Column(name = "id_festival")
    private Integer idFestival;

    /**
     * Número total de entradas vendidas para el festival. Valor por defecto 0.
     */
    @Column(name = "entradas_vendidas", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer entradasVendidas = 0;

    /**
     * Ingresos totales generados por la venta de entradas. Valor por defecto
     * 0.00. Mapeado a DECIMAL(12,2).
     */
    @Column(name = "ingresos_entradas", nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal ingresosEntradas = BigDecimal.ZERO;

    /**
     * Monto total de las recargas realizadas en las pulseras NFC del festival.
     * Valor por defecto 0.00. Mapeado a DECIMAL(12,2).
     */
    @Column(name = "recargas_totales", nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal recargasTotales = BigDecimal.ZERO;

    /**
     * Monto total de los consumos realizados con las pulseras NFC del festival.
     * Valor por defecto 0.00. Mapeado a DECIMAL(12,2).
     */
    @Column(name = "consumos_totales", nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal consumosTotales = BigDecimal.ZERO;

    /**
     * Saldo total remanente en las pulseras que no ha sido reclamado tras el
     * festival (si aplica). Valor por defecto 0.00. Mapeado a DECIMAL(12,2).
     */
    @Column(name = "saldo_no_reclamado", nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal saldoNoReclamado = BigDecimal.ZERO;

    /**
     * Fecha y hora de la última actualización de estas estadísticas.
     */
    @Column(name = "ultima_actualizacion") // Podría ser gestionado por trigger o servicio
    private LocalDateTime ultimaActualizacion;

    /**
     * El festival al que pertenecen estas estadísticas. Relación uno a uno
     * bidireccional con Festival. `@MapsId` indica que la clave primaria
     * (`idFestival`) de esta entidad se deriva de la relación con `Festival`.
     * `Festival` es el lado propietario de la relación inversa (`mappedBy =
     * "estadisticas"` en Festival). Fetch LAZY: El festival no se carga hasta
     * que se accede explícitamente.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false) // Un registro de estadísticas debe pertenecer a un festival
    @MapsId // Mapea la PK/FK id_festival a la entidad Festival
    @JoinColumn(name = "id_festival") // Especifica la columna FK
    private Festival festival;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public EstadisticasFestival() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID del festival asociado a estas estadísticas.
     *
     * @return El ID del festival.
     */
    public Integer getIdFestival() {
        return idFestival;
    }

    /**
     * Establece el ID del festival. Usado internamente por JPA al mapear la
     * relación.
     *
     * @param idFestival El ID del festival.
     */
    public void setIdFestival(Integer idFestival) {
        this.idFestival = idFestival;
    }

    /**
     * Obtiene el número total de entradas vendidas.
     *
     * @return El número de entradas vendidas.
     */
    public Integer getEntradasVendidas() {
        return entradasVendidas;
    }

    /**
     * Establece el número total de entradas vendidas.
     *
     * @param entradasVendidas El nuevo número de entradas vendidas.
     */
    public void setEntradasVendidas(Integer entradasVendidas) {
        this.entradasVendidas = entradasVendidas;
    }

    /**
     * Obtiene los ingresos totales por venta de entradas.
     *
     * @return Los ingresos totales.
     */
    public BigDecimal getIngresosEntradas() {
        return ingresosEntradas;
    }

    /**
     * Establece los ingresos totales por venta de entradas.
     *
     * @param ingresosEntradas Los nuevos ingresos totales.
     */
    public void setIngresosEntradas(BigDecimal ingresosEntradas) {
        this.ingresosEntradas = ingresosEntradas;
    }

    /**
     * Obtiene el monto total de recargas en pulseras.
     *
     * @return El total de recargas.
     */
    public BigDecimal getRecargasTotales() {
        return recargasTotales;
    }

    /**
     * Establece el monto total de recargas en pulseras.
     *
     * @param recargasTotales El nuevo total de recargas.
     */
    public void setRecargasTotales(BigDecimal recargasTotales) {
        this.recargasTotales = recargasTotales;
    }

    /**
     * Obtiene el monto total de consumos con pulseras.
     *
     * @return El total de consumos.
     */
    public BigDecimal getConsumosTotales() {
        return consumosTotales;
    }

    /**
     * Establece el monto total de consumos con pulseras.
     *
     * @param consumosTotales El nuevo total de consumos.
     */
    public void setConsumosTotales(BigDecimal consumosTotales) {
        this.consumosTotales = consumosTotales;
    }

    /**
     * Obtiene el saldo total no reclamado en pulseras.
     *
     * @return El saldo no reclamado.
     */
    public BigDecimal getSaldoNoReclamado() {
        return saldoNoReclamado;
    }

    /**
     * Establece el saldo total no reclamado en pulseras.
     *
     * @param saldoNoReclamado El nuevo saldo no reclamado.
     */
    public void setSaldoNoReclamado(BigDecimal saldoNoReclamado) {
        this.saldoNoReclamado = saldoNoReclamado;
    }

    /**
     * Obtiene la fecha de la última actualización de las estadísticas.
     *
     * @return La fecha y hora de la última actualización.
     */
    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    /**
     * Establece la fecha de la última actualización de las estadísticas.
     *
     * @param ultimaActualizacion La nueva fecha y hora de actualización.
     */
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    /**
     * Obtiene el festival asociado a estas estadísticas.
     *
     * @return El objeto Festival asociado.
     */
    public Festival getFestival() {
        return festival;
    }

    /**
     * Establece el festival asociado a estas estadísticas. También establece el
     * ID de las estadísticas si es necesario.
     *
     * @param festival El objeto Festival a asociar.
     */
    public void setFestival(Festival festival) {
        this.festival = festival;
        // Sincronizar el ID si el festival no es nulo
        if (festival != null) {
            this.idFestival = festival.getIdFestival();
        }
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara estas EstadisticasFestival con otro objeto para determinar si son
     * iguales. Dos estadísticas son iguales si pertenecen al mismo festival
     * (mismo idFestival).
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
        EstadisticasFestival that = (EstadisticasFestival) o;
        // La PK es idFestival, que no debería ser nula si la entidad está gestionada
        return idFestival != null && Objects.equals(idFestival, that.idFestival);
    }

    /**
     * Calcula el código hash para estas EstadisticasFestival. Se basa
     * únicamente en el idFestival.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        // Usa Objects.hash para manejar el caso de idFestival nulo
        return Objects.hash(idFestival);
    }

    /**
     * Devuelve una representación en cadena de estas EstadisticasFestival.
     * Incluye el ID del festival y los valores principales de las estadísticas.
     *
     * @return Una cadena representando las EstadisticasFestival.
     */
    @Override
    public String toString() {
        return "EstadisticasFestival{"
                + "idFestival=" + idFestival
                + ", entradasVendidas=" + entradasVendidas
                + ", ingresosEntradas=" + ingresosEntradas
                + ", recargasTotales=" + recargasTotales
                + ", consumosTotales=" + consumosTotales
                + // ", saldoNoReclamado=" + saldoNoReclamado + // Opcional incluir
                // ", ultimaActualizacion=" + ultimaActualizacion + // Opcional incluir
                '}';
    }
}
