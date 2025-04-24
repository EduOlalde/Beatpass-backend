package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para la entidad {@link Usuario}. Define las
 * operaciones de persistencia estándar para los usuarios del sistema (Admin,
 * Promotor, Cajero).
 *
 * @author Eduardo Olalde
 */
public interface UsuarioRepository {

    /**
     * Guarda (crea o actualiza) una entidad Usuario. Si el usuario tiene ID
     * nulo, se persiste. Si tiene ID, se actualiza (merge).
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param usuario El usuario a guardar. No debe ser nulo.
     * @return La entidad Usuario guardada o actualizada.
     * @throws IllegalArgumentException si el usuario es nulo.
     * @throws jakarta.persistence.PersistenceException si ocurre un error (ej:
     * email duplicado).
     */
    Usuario save(EntityManager em, Usuario usuario);

    /**
     * Busca un Usuario por su identificador único (clave primaria).
     *
     * @param em El EntityManager activo.
     * @param id El ID del usuario a buscar.
     * @return Un {@link Optional} que contiene el Usuario si se encuentra, o un
     * Optional vacío si no se encuentra o si el ID es nulo.
     */
    Optional<Usuario> findById(EntityManager em, Integer id);

    /**
     * Busca un Usuario por su dirección de correo electrónico, que debe ser
     * única.
     *
     * @param em El EntityManager activo.
     * @param email El email del usuario a buscar.
     * @return Un {@link Optional} que contiene el Usuario si se encuentra, o un
     * Optional vacío si no se encuentra o si el email es nulo o vacío.
     */
    Optional<Usuario> findByEmail(EntityManager em, String email);

    /**
     * Busca y devuelve todos los usuarios registrados en el sistema. Los
     * resultados se ordenan por nombre. ¡Precaución! Puede devolver muchos
     * resultados. Considerar paginación en aplicaciones reales.
     *
     * @param em El EntityManager activo.
     * @return Una lista (posiblemente vacía) con todos los usuarios.
     */
    List<Usuario> findAll(EntityManager em);

    /**
     * Busca y devuelve todos los usuarios que tienen un rol específico. Los
     * resultados se ordenan por nombre.
     *
     * @param em El EntityManager activo.
     * @param rol El {@link RolUsuario} a buscar.
     * @return Una lista (posiblemente vacía) de usuarios con el rol
     * especificado. Devuelve lista vacía si el rol es nulo o si ocurre un
     * error.
     */
    List<Usuario> findByRol(EntityManager em, RolUsuario rol);

    /**
     * Elimina un usuario por su ID. Busca la entidad y, si existe, la marca
     * para eliminar.
     * <p>
     * <b>Nota:</b> Esta operación debe ejecutarse dentro de una transacción
     * activa. ¡Precaución! Puede fallar si existen restricciones de clave
     * foránea (ej: un Promotor con Festivales asociados, un Cajero con Recargas
     * asociadas). Considerar la desactivación (cambiar estado a false) en lugar
     * del borrado físico.
     * </p>
     *
     * @param em El EntityManager activo y transaccional.
     * @param id El ID del usuario a eliminar.
     * @return {@code true} si la entidad fue encontrada y marcada para
     * eliminar, {@code false} si no se encontró.
     * @throws jakarta.persistence.PersistenceException si ocurre un error
     * durante la eliminación (ej: violación de FK).
     */
    boolean deleteById(EntityManager em, Integer id);

}
