/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa datos estadísticos agregados para un festival (opcional, para
 * optimizar). Mapea la tabla 'estadisticas_festival'.
 */
@Entity
@Table(name = "estadisticas_festival")
public class EstadisticasFestival implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    // No @GeneratedValue, el ID es el mismo que el del Festival asociado
    @Column(name = "id_festival")
    private Integer idFestival;

    @Column(name = "entradas_vendidas", columnDefinition = "INT DEFAULT 0")
    private Integer entradasVendidas = 0;

    @Column(name = "ingresos_entradas", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal ingresosEntradas = BigDecimal.ZERO;

    @Column(name = "recargas_totales", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal recargasTotales = BigDecimal.ZERO;

    @Column(name = "consumos_totales", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal consumosTotales = BigDecimal.ZERO;

    @Column(name = "saldo_no_reclamado", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal saldoNoReclamado = BigDecimal.ZERO;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    // Relación OneToOne: Las estadísticas pertenecen a un Festival
    // Usamos @MapsId para indicar que el ID de esta entidad se mapea
    // desde la relación con Festival. Festival es el propietario.
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Indica que la PK de esta entidad es también una FK a Festival
    @JoinColumn(name = "id_festival")
    private Festival festival;

    // Constructores
    public EstadisticasFestival() {
    }

    // Getters y Setters
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
    }

    // equals y hashCode (basado en idFestival que es la PK)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EstadisticasFestival that = (EstadisticasFestival) o;
        if (idFestival == null) {
            return false;
        }
        return Objects.equals(idFestival, that.idFestival);
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
