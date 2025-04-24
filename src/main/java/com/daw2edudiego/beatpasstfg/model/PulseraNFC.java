package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Validación
import jakarta.validation.constraints.NotNull; // Validación
import jakarta.validation.constraints.PositiveOrZero; // Validación
import jakarta.validation.constraints.Size; // Validación
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa una pulsera física con tecnología NFC. Se utiliza
 * para pagos cashless dentro del festival y potencialmente para control de
 * acceso. Mapea la tabla 'pulseras_nfc'.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "pulseras_nfc", uniqueConstraints = {
    // Constraint de unicidad explícito para el código UID
    @UniqueConstraint(columnNames = "codigo_uid", name = "uq_pulseranfc_codigouid"),
    // Constraint de unicidad explícito para la relación 1:1 con EntradaAsignada
    @UniqueConstraint(columnNames = "id_entrada_asignada", name = "uq_pulseranfc_entradaasignada")
})
public class PulseraNFC implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único de la pulsera (clave primaria). Generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pulsera")
    private Integer idPulsera;

    /**
     * Código UID (Identificador Único) leído del chip NFC de la pulsera. Debe
     * ser único, no nulo/vacío y tiene longitud máxima de 100 caracteres.
     */
    @NotBlank(message = "El código UID de la pulsera no puede estar vacío.") // Validación
    @Size(max = 100, message = "El código UID no puede exceder los 100 caracteres.") // Validación
    @Column(name = "codigo_uid", nullable = false, unique = true, length = 100)
    private String codigoUid;

    /**
     * Saldo monetario actual cargado en la pulsera para pagos cashless. Valor
     * por defecto 0.00. Debe ser positivo o cero. Mapeado a DECIMAL(10,2).
     */
    @NotNull(message = "El saldo no puede ser nulo.") // Validación
    @PositiveOrZero(message = "El saldo no puede ser negativo.") // Validación
    @Column(name = "saldo", nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal saldo = BigDecimal.ZERO;

    /**
     * Indica si la pulsera está activa y puede ser utilizada. Valor por defecto
     * true.
     */
    @NotNull // Validación (aunque la BD tenga default, asegurar a nivel de objeto)
    @Column(name = "activa", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activa = true;

    /**
     * Fecha y hora en que se dio de alta (registró) la pulsera en el sistema.
     * Gestionado automáticamente por la base de datos (DEFAULT
     * CURRENT_TIMESTAMP). No insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_alta", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaAlta;

    /**
     * Fecha y hora de la última modificación del registro de la pulsera.
     * Gestionado automáticamente por la base de datos (ON UPDATE
     * CURRENT_TIMESTAMP). No insertable ni actualizable desde JPA. Nota: El
     * script SQL usa 'fecha_modificacion' y 'ultima_modificacion', revisar
     * consistencia. Usaremos 'ultima_modificacion' como en el script.
     */
    @Column(name = "ultima_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime ultimaModificacion;

    // El script SQL tiene fecha_creacion y fecha_asociacion que no están aquí mapeadas explícitamente.
    // fecha_creacion probablemente sea igual a fecha_alta.
    // fecha_asociacion podría mapearse si fuera necesario.
    /**
     * La entrada asignada que se utilizó para asociar esta pulsera
     * (generalmente en el primer acceso). Relación uno a uno con
     * EntradaAsignada. PulseraNFC es el lado propietario de la relación. Una
     * pulsera puede no estar asociada a ninguna entrada inicialmente (permite
     * NULL). La columna 'id_entrada_asignada' es UNIQUE para asegurar la
     * relación 1:1. Fetch LAZY: La entrada asignada no se carga hasta que se
     * accede explícitamente. Cascade: Se omite CascadeType.ALL para evitar
     * borrar la EntradaAsignada si se borra la pulsera. ON DELETE SET NULL en
     * la BD maneja la desvinculación.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}) // Evitar CascadeType.REMOVE
    @JoinColumn(name = "id_entrada_asignada", unique = true) // Columna FK y constraint UNIQUE
    private EntradaAsignada entradaAsignada;

    /**
     * Conjunto de recargas de saldo realizadas en esta pulsera. Relación uno a
     * muchos (inversa de Recarga.pulseraNFC). Cascade ALL: Las operaciones
     * sobre PulseraNFC se propagan a sus Recargas. orphanRemoval true: Si se
     * elimina una Recarga de este Set, se elimina de la BD.
     */
    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Recarga> recargas = new HashSet<>();

    /**
     * Conjunto de consumos realizados con esta pulsera. Relación uno a muchos
     * (inversa de Consumo.pulseraNFC). Cascade ALL: Las operaciones sobre
     * PulseraNFC se propagan a sus Consumos. orphanRemoval true: Si se elimina
     * un Consumo de este Set, se elimina de la BD.
     */
    @OneToMany(mappedBy = "pulseraNFC", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Consumo> consumos = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public PulseraNFC() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID de la pulsera. @return El ID.
     */
    public Integer getIdPulsera() {
        return idPulsera;
    }

    /**
     * Establece el ID de la pulsera. @param idPulsera El nuevo ID.
     */
    public void setIdPulsera(Integer idPulsera) {
        this.idPulsera = idPulsera;
    }

    /**
     * Obtiene el código UID de la pulsera. @return El código UID.
     */
    public String getCodigoUid() {
        return codigoUid;
    }

    /**
     * Establece el código UID de la pulsera. @param codigoUid El nuevo código
     * UID.
     */
    public void setCodigoUid(String codigoUid) {
        this.codigoUid = codigoUid;
    }

    /**
     * Obtiene el saldo actual de la pulsera. @return El saldo.
     */
    public BigDecimal getSaldo() {
        return saldo;
    }

    /**
     * Establece el saldo actual de la pulsera. @param saldo El nuevo saldo.
     */
    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    /**
     * Verifica si la pulsera está activa. @return true si está activa, false si
     * no.
     */
    public Boolean getActiva() {
        return activa;
    }

    /**
     * Establece el estado de activación de la pulsera. @param activa El nuevo
     * estado.
     */
    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    /**
     * Obtiene la fecha de alta de la pulsera. @return La fecha de alta.
     */
    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }
    // public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; } // Generalmente no necesario

    /**
     * Obtiene la fecha de la última modificación. @return La fecha de
     * modificación.
     */
    public LocalDateTime getUltimaModificacion() {
        return ultimaModificacion;
    }
    // public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; } // Generalmente no necesario

    /**
     * Obtiene la entrada asignada asociada a la pulsera. @return La
     * EntradaAsignada, o null si no está asociada.
     */
    public EntradaAsignada getEntradaAsignada() {
        return entradaAsignada;
    }

    /**
     * Establece la entrada asignada asociada a la pulsera. Importante:
     * Gestionar también el lado inverso de la relación si es necesario.
     *
     * @param entradaAsignada La EntradaAsignada a asociar.
     */
    public void setEntradaAsignada(EntradaAsignada entradaAsignada) {
        this.entradaAsignada = entradaAsignada;
    }

    /**
     * Obtiene el conjunto de recargas de la pulsera. @return Un Set de Recarga.
     */
    public Set<Recarga> getRecargas() {
        return recargas;
    }

    /**
     * Establece el conjunto de recargas de la pulsera. @param recargas El nuevo
     * Set de Recarga.
     */
    public void setRecargas(Set<Recarga> recargas) {
        this.recargas = recargas;
    }

    /**
     * Obtiene el conjunto de consumos de la pulsera. @return Un Set de Consumo.
     */
    public Set<Consumo> getConsumos() {
        return consumos;
    }

    /**
     * Establece el conjunto de consumos de la pulsera. @param consumos El nuevo
     * Set de Consumo.
     */
    public void setConsumos(Set<Consumo> consumos) {
        this.consumos = consumos;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara esta PulseraNFC con otro objeto para determinar si son iguales.
     * La comparación se basa primero en el idPulsera si no es nulo. Si ambos
     * IDs son nulos, se compara por codigoUid (asumiendo que es un
     * identificador natural único antes de persistir).
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
        PulseraNFC that = (PulseraNFC) o;

        // Si los IDs no son nulos, comparar por ID
        if (idPulsera != null && that.idPulsera != null) {
            return Objects.equals(idPulsera, that.idPulsera);
        }
        // Si los IDs son nulos (objetos no persistidos), comparar por codigoUid si ambos no son nulos
        if (idPulsera == null && that.idPulsera == null && codigoUid != null && that.codigoUid != null) {
            return Objects.equals(codigoUid, that.codigoUid);
        }
        // En cualquier otro caso (uno ID nulo y otro no, o UIDs nulos/diferentes con IDs nulos)
        return false;
    }

    /**
     * Calcula el código hash para esta PulseraNFC. Se basa preferentemente en
     * el idPulsera si no es nulo, o en el codigoUid si el ID es nulo.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        // Usar ID si está disponible, sino UID
        return Objects.hash(idPulsera != null ? idPulsera : codigoUid);
    }

    /**
     * Devuelve una representación en cadena de esta PulseraNFC. Incluye id,
     * UID, saldo, estado y el ID de la entrada asociada (si existe).
     *
     * @return Una cadena representando la PulseraNFC.
     */
    @Override
    public String toString() {
        return "PulseraNFC{"
                + "idPulsera=" + idPulsera
                + ", codigoUid='" + codigoUid + '\''
                + ", saldo=" + saldo
                + ", activa=" + activa
                + ", idEntradaAsignada=" + (entradaAsignada != null ? entradaAsignada.getIdEntradaAsignada() : "null")
                + '}';
    }
}
