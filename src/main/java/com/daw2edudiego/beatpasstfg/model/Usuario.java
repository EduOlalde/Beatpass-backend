package com.daw2edudiego.beatpasstfg.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email; // Validación
import jakarta.validation.constraints.NotBlank; // Validación
import jakarta.validation.constraints.NotNull; // Validación
import jakarta.validation.constraints.Size; // Validación
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa un usuario del sistema Beatpass. Puede ser un
 * Administrador (ADMIN), un Promotor (PROMOTOR) o un Cajero (CAJERO). Mapea la
 * tabla 'usuarios'.
 *
 * @author Eduardo Olalde
 */
@Entity
@Table(name = "usuarios", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email", name = "uq_usuario_email") // Constraint explícito
})
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único del usuario (clave primaria). Generado
     * automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    /**
     * Nombre del usuario (puede ser nombre real, alias o identificador). No
     * puede ser nulo/vacío y tiene longitud máxima de 100 caracteres.
     */
    @NotBlank(message = "El nombre de usuario no puede estar vacío.") // Validación
    @Size(max = 100, message = "El nombre de usuario no puede exceder los 100 caracteres.") // Validación
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Dirección de correo electrónico del usuario, utilizada para login y
     * comunicación. Debe ser única, no nula/vacía y tener formato de email
     * válido. Longitud máxima de 100 caracteres.
     */
    @NotBlank(message = "El email del usuario no puede estar vacío.") // Validación
    @Email(message = "El formato del email no es válido.") // Validación
    @Size(max = 100, message = "El email del usuario no puede exceder los 100 caracteres.") // Validación
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Contraseña hasheada del usuario (utilizando BCrypt). No puede ser
     * nula/vacía y tiene longitud máxima de 255 caracteres (suficiente para
     * BCrypt).
     */
    @NotBlank(message = "La contraseña no puede estar vacía.") // Validación (a nivel de objeto, el hash no estará vacío)
    @Size(max = 255, message = "El hash de la contraseña excede la longitud permitida.") // Validación (longitud hash)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Rol del usuario en el sistema (ADMIN, PROMOTOR, CAJERO). Determina los
     * permisos del usuario. Mapeado como ENUM. No puede ser nulo.
     */
    @NotNull(message = "El rol del usuario no puede ser nulo.") // Validación
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolUsuario rol;

    /**
     * Estado de activación de la cuenta de usuario. true si está activa, false
     * si está desactivada. Valor por defecto true.
     */
    @NotNull // Validación
    @Column(name = "estado", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean estado = true;

    /**
     * Indicador de si el usuario debe cambiar su contraseña en el próximo
     * inicio de sesión. Útil para contraseñas iniciales generadas por el
     * administrador. No puede ser nulo, valor por defecto true.
     */
    @NotNull // Validación
    @Column(name = "cambio_password_requerido", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean cambioPasswordRequerido = true;

    /**
     * Fecha y hora de creación de la cuenta de usuario. Gestionado
     * automáticamente por la base de datos (DEFAULT CURRENT_TIMESTAMP). No
     * insertable ni actualizable desde JPA. Nota: El script usa DATETIME, JPA
     * puede usar LocalDateTime. Se ajusta columnDefinition si es necesario.
     */
    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación de la cuenta de usuario.
     * Gestionado automáticamente por la base de datos (ON UPDATE
     * CURRENT_TIMESTAMP). No insertable ni actualizable desde JPA.
     */
    @Column(name = "fecha_modificacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    /**
     * Conjunto de festivales gestionados por este usuario (si es PROMOTOR).
     * Relación uno a muchos (inversa de Festival.promotor). Cascade ALL: Las
     * operaciones sobre Usuario (promotor) se propagan a sus Festivales
     * (¡CUIDADO! Borrar promotor borraría sus festivales). Revisar si es el
     * comportamiento deseado. Fetch LAZY: Los festivales no se cargan hasta que
     * se acceden explícitamente. orphanRemoval true: Si se elimina un Festival
     * de este Set, se elimina de la BD.
     */
    @OneToMany(mappedBy = "promotor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Festival> festivales = new HashSet<>();

    /**
     * Conjunto de recargas realizadas por este usuario (si es CAJERO). Relación
     * uno a muchos (inversa de Recarga.usuarioCajero). No se suele usar Cascade
     * ALL aquí, borrar un cajero no debería borrar las recargas que hizo. Fetch
     * LAZY: Las recargas no se cargan hasta que se acceden explícitamente.
     */
    @OneToMany(mappedBy = "usuarioCajero", fetch = FetchType.LAZY) // No cascade remove
    private Set<Recarga> recargasRealizadas = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Usuario() {
    }

    // --- Getters y Setters ---
    /**
     * Obtiene el ID del usuario. @return El ID.
     */
    public Integer getIdUsuario() {
        return idUsuario;
    }

    /**
     * Establece el ID del usuario. @param idUsuario El nuevo ID.
     */
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Obtiene el nombre del usuario. @return El nombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del usuario. @param nombre El nuevo nombre.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el email del usuario. @return El email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el email del usuario. @param email El nuevo email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la contraseña hasheada del usuario. @return La contraseña
     * hasheada.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña hasheada del usuario. @param password La nueva
     * contraseña hasheada.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene el rol del usuario. @return El RolUsuario.
     */
    public RolUsuario getRol() {
        return rol;
    }

    /**
     * Establece el rol del usuario. @param rol El nuevo RolUsuario.
     */
    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    /**
     * Verifica si el usuario está activo. @return true si está activo, false si
     * no.
     */
    public Boolean getEstado() {
        return estado;
    }

    /**
     * Establece el estado de activación del usuario. @param estado El nuevo
     * estado.
     */
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    /**
     * Verifica si se requiere cambio de contraseña. @return true si se requiere
     * cambio.
     */
    public Boolean getCambioPasswordRequerido() {
        return cambioPasswordRequerido;
    }

    /**
     * Establece si se requiere cambio de contraseña. @param
     * cambioPasswordRequerido El nuevo valor.
     */
    public void setCambioPasswordRequerido(Boolean cambioPasswordRequerido) {
        this.cambioPasswordRequerido = cambioPasswordRequerido;
    }

    /**
     * Obtiene la fecha de creación del usuario. @return La fecha de creación.
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Obtiene la fecha de la última modificación del usuario. @return La fecha
     * de modificación.
     */
    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    /**
     * Obtiene los festivales asociados a este usuario (si es promotor). @return
     * Un Set de Festival.
     */
    public Set<Festival> getFestivales() {
        return festivales;
    }

    /**
     * Establece los festivales asociados a este usuario. @param festivales El
     * nuevo Set de Festival.
     */
    public void setFestivales(Set<Festival> festivales) {
        this.festivales = festivales;
    }

    /**
     * Obtiene las recargas realizadas por este usuario (si es cajero). @return
     * Un Set de Recarga.
     */
    public Set<Recarga> getRecargasRealizadas() {
        return recargasRealizadas;
    }

    /**
     * Establece las recargas realizadas por este usuario. @param
     * recargasRealizadas El nuevo Set de Recarga.
     */
    public void setRecargasRealizadas(Set<Recarga> recargasRealizadas) {
        this.recargasRealizadas = recargasRealizadas;
    }

    // --- equals, hashCode y toString ---
    /**
     * Compara este Usuario con otro objeto para determinar si son iguales. Dos
     * usuarios son iguales si tienen el mismo idUsuario.
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
        Usuario usuario = (Usuario) o;
        return idUsuario != null && Objects.equals(idUsuario, usuario.idUsuario);
    }

    /**
     * Calcula el código hash para este Usuario. Se basa únicamente en el
     * idUsuario.
     *
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }

    /**
     * Devuelve una representación en cadena de este Usuario. Incluye id,
     * nombre, email, rol y estado.
     *
     * @return Una cadena representando al Usuario.
     */
    @Override
    public String toString() {
        return "Usuario{"
                + "idUsuario=" + idUsuario
                + ", nombre='" + nombre + '\''
                + ", email='" + email + '\''
                + ", rol=" + rol
                + ", estado=" + estado
                + ", cambioPasswordRequerido=" + cambioPasswordRequerido
                + '}';
    }
}
