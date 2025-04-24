package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.exception.EmailExistenteException;
import com.daw2edudiego.beatpasstfg.exception.PasswordIncorrectoException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Define la interfaz para la lógica de negocio relacionada con la gestión de
 * Usuarios del sistema ({@link RolUsuario#ADMIN}, {@link RolUsuario#PROMOTOR},
 * {@link RolUsuario#CAJERO}). Esta capa coordina las operaciones sobre la
 * entidad {@link Usuario}, interactuando con los repositorios, gestionando la
 * lógica de negocio (como hashing de contraseñas, validaciones) y controlando
 * el acceso a los datos.
 *
 * @see Usuario
 * @see UsuarioDTO
 * @see UsuarioCreacionDTO
 * @see UsuarioServiceImpl
 * @author Eduardo Olalde
 */
public interface UsuarioService {

    /**
     * Crea un nuevo usuario en el sistema (ADMIN, PROMOTOR o CAJERO). Verifica
     * que el correo electrónico proporcionado no esté ya en uso. Hashea la
     * contraseña proporcionada antes de almacenarla. Establece el estado
     * inicial del usuario como activo y requiriendo cambio de contraseña.
     *
     * @param usuarioCreacionDTO DTO que contiene los datos necesarios para la
     * creación (nombre, email, contraseña en texto plano, rol). No debe ser
     * {@code null}.
     * @return El {@link UsuarioDTO} del usuario recién creado, incluyendo su ID
     * generado y excluyendo la contraseña.
     * @throws EmailExistenteException Si el correo electrónico ya está
     * registrado en el sistema.
     * @throws IllegalArgumentException Si los datos del DTO son inválidos (ej:
     * campos obligatorios nulos o vacíos).
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * persistencia.
     */
    UsuarioDTO crearUsuario(UsuarioCreacionDTO usuarioCreacionDTO);

    /**
     * Obtiene la información pública (DTO) de un usuario específico basado en
     * su ID.
     *
     * @param id El ID del usuario a buscar. No debe ser {@code null}.
     * @return Un {@link Optional} que contiene el {@link UsuarioDTO} si el
     * usuario se encuentra, o un {@code Optional} vacío si no se encuentra.
     * @throws IllegalArgumentException Si el ID es {@code null}.
     */
    Optional<UsuarioDTO> obtenerUsuarioPorId(Integer id);

    /**
     * Obtiene la información pública (DTO) de un usuario específico basado en
     * su dirección de correo electrónico.
     *
     * @param email El correo electrónico del usuario a buscar. No debe ser
     * {@code null} ni vacío.
     * @return Un {@link Optional} que contiene el {@link UsuarioDTO} si el
     * usuario se encuentra, o un {@code Optional} vacío si no se encuentra.
     * @throws IllegalArgumentException Si el email es {@code null} o vacío.
     */
    Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email);

    /**
     * Obtiene la entidad {@link Usuario} completa, incluyendo información
     * sensible como el hash de la contraseña y el estado de cambio de
     * contraseña requerido, buscando por correo electrónico.
     * <p>
     * <b>¡Usar con extrema precaución!</b> Este método está diseñado
     * exclusivamente para procesos internos que requieren acceso a la entidad
     * completa, como la autenticación de usuarios (comparación de contraseñas)
     * o la verificación del estado {@code cambioPasswordRequerido}.
     * <b>Nunca</b> se debe exponer la entidad {@code Usuario} completa devuelta
     * por este método directamente a través de una API o interfaz de usuario.
     * </p>
     *
     * @param email El correo electrónico del usuario a buscar. No debe ser
     * {@code null} ni vacío.
     * @return Un {@link Optional} que contiene la entidad {@link Usuario}
     * completa si se encuentra, o un {@code Optional} vacío si no se encuentra.
     * @throws IllegalArgumentException Si el email es {@code null} o vacío.
     */
    Optional<Usuario> obtenerEntidadUsuarioPorEmailParaAuth(String email);

    /**
     * Obtiene una lista de todos los usuarios que tienen asignado un rol
     * específico.
     *
     * @param rol El {@link RolUsuario} por el cual filtrar. No debe ser
     * {@code null}.
     * @return Una lista de {@link UsuarioDTO} que coinciden con el rol
     * especificado, ordenada por nombre. Puede estar vacía.
     * @throws IllegalArgumentException Si el rol es {@code null}.
     */
    List<UsuarioDTO> obtenerUsuariosPorRol(RolUsuario rol);

    /**
     * Actualiza el estado de activación de una cuenta de usuario. Permite
     * activar o desactivar una cuenta.
     *
     * @param id ID del usuario cuyo estado se modificará. No debe ser
     * {@code null}.
     * @param nuevoEstado {@code true} para activar la cuenta, {@code false}
     * para desactivarla.
     * @return El {@link UsuarioDTO} del usuario con el estado actualizado.
     * @throws UsuarioNotFoundException Si no se encuentra un usuario con el ID
     * especificado.
     * @throws IllegalArgumentException Si el ID es {@code null}.
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * actualización.
     */
    UsuarioDTO actualizarEstadoUsuario(Integer id, boolean nuevoEstado);

    /**
     * Elimina un usuario del sistema por su ID.
     * <p>
     * <b>¡Precaución!</b> La eliminación física puede fallar si el usuario
     * tiene datos asociados (ej: un promotor con festivales, un cajero con
     * recargas) debido a restricciones de clave foránea. En muchos casos, es
     * preferible desactivar la cuenta usando
     * {@link #actualizarEstadoUsuario(Integer, boolean)} en lugar de eliminarla
     * físicamente.
     * </p>
     *
     * @param id ID del usuario a eliminar. No debe ser {@code null}.
     * @throws UsuarioNotFoundException Si el usuario no se encuentra.
     * @throws IllegalArgumentException Si el ID es {@code null}.
     * @throws RuntimeException Si no se puede eliminar debido a restricciones
     * de integridad referencial u otro error inesperado.
     */
    void eliminarUsuario(Integer id);

    /**
     * Permite a un usuario cambiar su propia contraseña. Verifica que la
     * contraseña antigua proporcionada coincida con la almacenada antes de
     * hashear y guardar la nueva contraseña. Una vez cambiada la contraseña,
     * marca el indicador {@code cambioPasswordRequerido} como {@code false}.
     *
     * @param userId ID del usuario que está cambiando su contraseña. No debe
     * ser {@code null}.
     * @param passwordAntigua La contraseña actual (en texto plano) del usuario.
     * No debe ser {@code null} ni vacía.
     * @param passwordNueva La nueva contraseña deseada (en texto plano). No
     * debe ser {@code null}, vacía, ni igual a la antigua, y debe cumplir los
     * requisitos de longitud/complejidad.
     * @throws UsuarioNotFoundException Si el usuario con {@code userId} no
     * existe.
     * @throws PasswordIncorrectoException Si la {@code passwordAntigua} no
     * coincide con la contraseña actual almacenada para el usuario.
     * @throws IllegalArgumentException Si alguna de las contraseñas es inválida
     * (nula, vacía, nueva igual a antigua, o no cumple requisitos).
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * actualización.
     */
    void cambiarPassword(Integer userId, String passwordAntigua, String passwordNueva);

    /**
     * Cambia la contraseña de un usuario y marca el indicador
     * {@code cambioPasswordRequerido} a {@code false}. Este método <b>no</b>
     * verifica la contraseña antigua y está diseñado específicamente para el
     * flujo de cambio de contraseña obligatorio que ocurre típicamente en el
     * primer inicio de sesión de un usuario.
     *
     * @param userId ID del usuario cuya contraseña se modificará. No debe ser
     * {@code null}.
     * @param passwordNueva La nueva contraseña (en texto plano) a establecer.
     * No debe ser {@code null} ni vacía y debe cumplir los requisitos de
     * longitud/complejidad.
     * @throws UsuarioNotFoundException Si el usuario con {@code userId} no
     * existe.
     * @throws IllegalArgumentException Si {@code userId} o
     * {@code passwordNueva} son {@code null} o inválidos.
     * @throws RuntimeException Si ocurre un error inesperado durante la
     * transacción.
     */
    void cambiarPasswordYMarcarActualizada(Integer userId, String passwordNueva);

}
