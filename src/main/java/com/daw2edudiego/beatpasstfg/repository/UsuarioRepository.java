package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Usuario.
 */
public interface UsuarioRepository {

    /**
     * Guarda (crea o actualiza) un Usuario. Debe ejecutarse dentro de una
     * transacción activa.
     *
     * @param em El EntityManager activo y transaccional.
     * @param usuario El usuario a guardar.
     * @return La entidad Usuario guardada o actualizada.
     */
    Usuario save(EntityManager em, Usuario usuario);

    /**
     * Busca un Usuario por su ID.
     *
     * @param em El EntityManager activo.
     * @param id El ID a buscar.
     * @return Un Optional con el Usuario si se encuentra, o vacío.
     */
    Optional<Usuario> findById(EntityManager em, Integer id);

    /**
     * Busca un Usuario por su email (único).
     *
     * @param em El EntityManager activo.
     * @param email El email a buscar.
     * @return Un Optional con el Usuario si se encuentra, o vacío.
     */
    Optional<Usuario> findByEmail(EntityManager em, String email);

    /**
     * Busca todos los usuarios. Usar con precaución.
     *
     * @param em El EntityManager activo.
     * @return Una lista con todos los usuarios.
     */
    List<Usuario> findAll(EntityManager em);

    /**
     * Busca todos los usuarios con un rol específico.
     *
     * @param em El EntityManager activo.
     * @param rol El RolUsuario a buscar.
     * @return Una lista (posiblemente vacía) de usuarios.
     */
    List<Usuario> findByRol(EntityManager em, RolUsuario rol);

    /**
     * Elimina un usuario por su ID. Debe ejecutarse dentro de una transacción
     * activa. ¡Precaución con FKs!
     *
     * @param em El EntityManager activo y transaccional.
     * @param id El ID a eliminar.
     * @return true si se encontró y marcó para eliminar, false si no.
     */
    boolean deleteById(EntityManager em, Integer id);

}
