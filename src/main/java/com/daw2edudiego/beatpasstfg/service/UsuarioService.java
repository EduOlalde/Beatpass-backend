/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.exception.EmailExistenteException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio relacionada con los Usuarios (Admin/Promotor).
 */
public interface UsuarioService {

    /**
     * Crea un nuevo usuario (Admin o Promotor). Hashea la contraseña antes de
     * guardarla. Verifica que el email no exista previamente.
     *
     * @param usuarioCreacionDTO Datos del usuario a crear.
     * @return DTO del usuario creado.
     * @throws EmailExistenteException si el email ya está en uso.
     */
    UsuarioDTO crearUsuario(UsuarioCreacionDTO usuarioCreacionDTO);

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Optional con el DTO del usuario si existe.
     */
    Optional<UsuarioDTO> obtenerUsuarioPorId(Integer id);

    /**
     * Obtiene un usuario por su email.
     *
     * @param email Email del usuario.
     * @return Optional con el DTO del usuario si existe.
     */
    Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email);

    /**
     * Obtiene la entidad Usuario completa (incluyendo hash de contraseña y
     * estado) por email. ¡Usar con precaución! Diseñado principalmente para
     * procesos internos como la autenticación. NUNCA exponer la entidad
     * completa directamente en una API pública.
     *
     * @param email Email del usuario.
     * @return Optional con la entidad Usuario si existe.
     */
    Optional<Usuario> obtenerEntidadUsuarioPorEmailParaAuth(String email);

    /**
     * Obtiene todos los usuarios con un rol específico.
     *
     * @param rol Rol a buscar.
     * @return Lista de DTOs de usuarios.
     */
    List<UsuarioDTO> obtenerUsuariosPorRol(RolUsuario rol);

    /**
     * Actualiza el estado (activo/inactivo) de un usuario.
     *
     * @param id ID del usuario.
     * @param nuevoEstado true para activo, false para inactivo.
     * @return DTO del usuario actualizado.
     * @throws UsuarioNotFoundException si el usuario no existe.
     */
    UsuarioDTO actualizarEstadoUsuario(Integer id, boolean nuevoEstado);

    /**
     * Elimina un usuario (si las restricciones lo permiten). ¡Precaución!
     * Considerar desactivar en lugar de eliminar.
     *
     * @param id ID del usuario a eliminar.
     * @throws UsuarioNotFoundException si el usuario no existe.
     * @throws RuntimeException si no se puede eliminar por restricciones (ej:
     * promotor con festivales).
     */
    void eliminarUsuario(Integer id);

    // Podrían añadirse métodos para actualizar perfil, cambiar contraseña, etc.
}
