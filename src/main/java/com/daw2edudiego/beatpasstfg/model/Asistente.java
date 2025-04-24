package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email; // Añadir para validación de email
import jakarta.validation.constraints.NotBlank; // Añadir para validación de no vacío
import jakarta.validation.constraints.Size; // Añadir para validación de tamaño
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa un asistente o cliente que compra entradas. Mapea
 * la tabla 'asistentes' en la base de datos.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "asistentes", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email", name = "uq_asistente_email") // Definir constraint explícitamente
})
public class Asistente implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único del asistente (clave primaria). Generado
     * automáticamente por la base de datos (IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistente")
    private Integer idAsistente;

    /**
     * Nombre completo del asistente. No puede ser nulo y tiene una longitud
     * máxima de 100 caracteres.
     */
    @NotBlank(message = "El nombre del asistente no puede estar vacío.") // Validación
    @Size(max = 100, message = "El nombre del asistente no puede exceder los 100 caracteres.") // Validación
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Dirección de correo electrónico del asistente. Debe ser única, no nula y
     * tener un formato de email válido. Longitud máxima de 100 caracteres.
     */
    @NotBlank(message = "El email del asistente no puede estar vacío.") // Validación
    @Email(message = "El formato del email no es válido.") // Validación
    @Size(max = 100, message = "El email del asistente no puede exceder los 100 caracteres.") // Validación
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Número de teléfono del asistente (opcional). Longitud máxima de 20
     * caracteres.
     */
    @Size(max = 20, message = "El teléfono del asistente no puede exceder los 20 caracteres.") // Validación
    @Column(name = "telefono", length = 20)
    private String telefono;

    /**
     * Fecha y hora de creación del registro del asistente. Gestionado
     * automáticamente por la base de datos (DEFAULT CURRENT_TIMESTAMP). No
     * insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación del registro del asistente.
     * Gestionado automáticamente por la base de datos (ON UPDATE
     * CURRENT_TIMESTAMP). No insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    /**
     * Conjunto de compras realizadas por este asistente. Relación uno a muchos
     * (inversa de Compra.asistente). Cascade ALL: Las operaciones (persist,
     * merge, remove) sobre Asistente se propagan a sus Compras. Fetch LAZY: Las
     * compras no se cargan hasta que se acceden explícitamente. orphanRemoval
     * true: Si se elimina una Compra de este Set, se elimina de la BD.
     */
    @OneToMany(mappedBy = "asistente", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Compra> compras = new HashSet<>();

    /**
     * Conjunto de entradas asignadas (nominadas) a este asistente. Relación uno
     * a muchos (inversa de EntradaAsignada.asistente). Fetch LAZY: Las entradas
     * asignadas no se cargan hasta que se acceden explícitamente. No se usa
     * CascadeType.ALL para evitar borrar entradas si se borra el asistente
     * (podrían reasignarse).
     */
    @OneToMany(mappedBy = "asistente", fetch = FetchType.LAZY)
    private Set<EntradaAsignada> entradasAsignadas = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Asistente() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID del asistente.
     *
     * @return El ID del asistente.
     */
    public Integer getIdAsistente() {
        return idAsistente;
    }

    /**
     * Establece el ID del asistente.
     *
     * @param idAsistente El nuevo ID del asistente.
     */
    public void setIdAsistente(Integer idAsistente) {
        this.idAsistente = idAsistente;
    }

    /**
     * Obtiene el nombre del asistente.
     *
     * @return El nombre del asistente.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del asistente.
     *
     * @param nombre El nuevo nombre del asistente.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el email del asistente.
     *
     * @return El email del asistente.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el email del asistente.
     *
     * @param email El nuevo email del asistente.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene el teléfono del asistente.
     *
     * @return El teléfono del asistente, o null si no tiene.
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Establece el teléfono del asistente.
     *
     * @param telefono El nuevo teléfono del asistente.
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * Obtiene la fecha de creación del asistente.
     *
     * @return La fecha y hora de creación.
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Obtiene la fecha de la última modificación del asistente.
     *
     * @return La fecha y hora de la última modificación.
     */
    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    /**
     * Obtiene el conjunto de compras realizadas por el asistente.
     *
     * @return Un Set de objetos Compra.
     */
    public Set<Compra> getCompras() {
        return compras;
    }

    /**
     * Establece el conjunto de compras del asistente.
     *
     * @param compras El nuevo Set de objetos Compra.
     */
    public void setCompras(Set<Compra> compras) {
        this.compras = compras;
    }

    /**
     * Obtiene el conjunto de entradas asignadas al asistente.
     *
     * @return Un Set de objetos EntradaAsignada.
     */
    public Set<EntradaAsignada> getEntradasAsignadas() {
        return entradasAsignadas;
    }

    /**
     * Establece el conjunto de entradas asignadas al asistente.
     *
     * @param entradasAsignadas El nuevo Set de objetos EntradaAsignada.
     */
    public void setEntradasAsignadas(Set<EntradaAsignada> entradasAsignadas) {
        this.entradasAsignadas = entradasAsignadas;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara este Asistente con otro objeto para determinar si son iguales.
     * Dos asistentes son iguales si tienen el mismo idAsistente.
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
        Asistente asistente = (Asistente) o;
        // Si ambos IDs son nulos, no son iguales. Si uno es nulo, no son iguales.
        // Solo compara IDs si ambos no son nulos.
        return idAsistente != null && Objects.equals(idAsistente, asistente.idAsistente);
    }

    /**
     * Calcula el código hash para este Asistente. Se basa únicamente en el
     * idAsistente.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        // Usa Objects.hash para manejar el caso de idAsistente nulo (aunque no debería serlo si persiste)
        return Objects.hash(idAsistente);
    }

    /**
     * Devuelve una representación en cadena de este Asistente. Incluye id,
     * nombre y email.
     *
     * @return Una cadena representando al Asistente.
     */
    @Override
    public String toString() {
        return "Asistente{"
                + "idAsistente=" + idAsistente
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
