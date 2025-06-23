package com.beatpass.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA que representa estadísticas agregadas para un festival. Relación
 * OneToOne con Festival usando @MapsId. Mapea la tabla 'estadisticas_festival'.
 */
@Entity
@Table(name = "estadisticas_festival")
public class EstadisticasFestival implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID de las estadísticas, coincide con el ID del festival asociado (PK y
     * FK).
     */
    @Id
    @Column(name = "id_festival")
    private Integer idFestival;

    @Column(name = "entradas_vendidas", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer entradasVendidas = 0;

    @Column(name = "ingresos_entradas", nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal ingresosEntradas = BigDecimal.ZERO;

    @Column(name = "recargas_totales", nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal recargasTotales = BigDecimal.ZERO;

    @Column(name = "consumos_totales", nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal consumosTotales = BigDecimal.ZERO;

    @Column(name = "saldo_no_reclamado", nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal saldoNoReclamado = BigDecimal.ZERO;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    /**
     * Festival al que pertenecen estas estadísticas. Lado propietario de la
     * relación @MapsId. Fetch LAZY, JoinColumn explícito.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId // Indica que la PK/FK id_festival mapea a la entidad Festival
    @JoinColumn(name = "id_festival")
    private Festival festival;

    public EstadisticasFestival() {
    }

    // --- Getters y Setters ---
    public Integer getIdFestival() {
        return idFestival;
    }

    public void setIdFestival(Integer idFestival) {
        this.idFestival = idFestival;
    }

    public Integer getEntradasVendidas() {
        return entradasVendidas;
    }

    public void setEntradasVendidas(Integer entradasVendidas) {
        this.entradasVendidas = entradasVendidas;
    }

    public BigDecimal getIngresosEntradas() {
        return ingresosEntradas;
    }

    public void setIngresosEntradas(BigDecimal ingresosEntradas) {
        this.ingresosEntradas = ingresosEntradas;
    }

    public BigDecimal getRecargasTotales() {
        return recargasTotales;
    }

    public void setRecargasTotales(BigDecimal recargasTotales) {
        this.recargasTotales = recargasTotales;
    }

    public BigDecimal getConsumosTotales() {
        return consumosTotales;
    }

    public void setConsumosTotales(BigDecimal consumosTotales) {
        this.consumosTotales = consumosTotales;
    }

    public BigDecimal getSaldoNoReclamado() {
        return saldoNoReclamado;
    }

    public void setSaldoNoReclamado(BigDecimal saldoNoReclamado) {
        this.saldoNoReclamado = saldoNoReclamado;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
        if (festival != null) {
            this.idFestival = festival.getIdFestival(); // Sincronizar ID
        }
    }

    // --- equals, hashCode y toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EstadisticasFestival that = (EstadisticasFestival) o;
        return idFestival != null && Objects.equals(idFestival, that.idFestival);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFestival);
    }

    @Override
    public String toString() {
        return "EstadisticasFestival{"
                + "idFestival=" + idFestival
                + ", entradasVendidas=" + entradasVendidas
                + ", ingresosEntradas=" + ingresosEntradas
                + ", recargasTotales=" + recargasTotales
                + ", consumosTotales=" + consumosTotales
                + '}';
    }
}
