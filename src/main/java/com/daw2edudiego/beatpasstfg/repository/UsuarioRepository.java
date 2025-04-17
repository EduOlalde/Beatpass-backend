/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Define las operaciones de acceso a datos para la entidad Usuario.
 */
public interface UsuarioRepository {

    /**
     * Guarda (crea o actualiza) un usuario.
     * Asume transacción externa activa.
     * @param em EntityManager activo.
     * @param usuario El usuario a guardar.
     * @return El usuario guardado.
     */
    Usuario save(EntityManager em, Usuario usuario);

    /**
     * Busca un usuario por su ID.
     * @param em EntityManager activo.
     * @param id El ID del usuario.
     * @return Optional con el usuario si se encuentra.
     */
    Optional<Usuario> findById(EntityManager em, Integer id);

    /**
     * Busca un usuario por su email.
     * @param em EntityManager activo.
     * @param email El email a buscar.
     * @return Optional con el usuario si se encuentra.
     */
    Optional<Usuario> findByEmail(EntityManager em, String email);

    /**
     * Busca todos los usuarios (puede necesitar paginación en una app real).
     * @param em EntityManager activo.
     * @return Lista de todos los usuarios.
     */
    List<Usuario> findAll(EntityManager em);

     /**
     * Busca todos los usuarios con un rol específico.
     * @param em EntityManager activo.
     * @param rol El rol a buscar.
     * @return Lista de usuarios con ese rol.
     */
    List<Usuario> findByRol(EntityManager em, RolUsuario rol);

    /**
     * Elimina un usuario por su ID.
     * Asume transacción externa activa.
     * @param em EntityManager activo.
     * @param id El ID del usuario a eliminar.
     * @return true si se encontró y marcó para eliminar, false si no.
     */
    boolean deleteById(EntityManager em, Integer id);

}
