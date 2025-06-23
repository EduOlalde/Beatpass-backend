package com.beatpass.service;

import com.beatpass.dto.UsuarioCreacionDTO;
import com.beatpass.dto.UsuarioDTO;
import com.beatpass.exception.EmailExistenteException;
import com.beatpass.exception.PasswordIncorrectoException;
import com.beatpass.exception.UsuarioNotFoundException;
import com.beatpass.model.RolUsuario;
import com.beatpass.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Define la lógica de negocio para la gestión de Usuarios.
 */
public interface UsuarioService {

    /**
     * Crea un nuevo usuario (ADMIN, PROMOTOR o CAJERO). Verifica unicidad de
     * email y hashea contraseña. Estado inicial activo.
     *
     * @param usuarioCreacionDTO DTO con datos de creación.
     * @return El UsuarioDTO creado.
     * @throws EmailExistenteException si el email ya existe.
     * @throws IllegalArgumentException si los datos son inválidos.
     */
    UsuarioDTO crearUsuario(UsuarioCreacionDTO usuarioCreacionDTO);

    /**
     * Obtiene la información pública (DTO) de un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Optional con UsuarioDTO si existe.
     */
    Optional<UsuarioDTO> obtenerUsuarioPorId(Integer id);

    /**
     * Obtiene la información pública (DTO) de un usuario por su email.
     *
     * @param email Email del usuario.
     * @return Optional con UsuarioDTO si existe.
     */
    Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email);

    /**
     * Obtiene la entidad Usuario completa (incluyendo hash de pass) por email.
     * ¡Usar con precaución! Solo para procesos internos como autenticación.
     *
     * @param email Email del usuario.
     * @return Optional con la entidad Usuario completa si existe.
     */
    Optional<Usuario> obtenerEntidadUsuarioPorEmailParaAuth(String email);

    /**
     * Obtiene los usuarios con un rol específico.
     *
     * @param rol RolUsuario a filtrar.
     * @return Lista de UsuarioDTO.
     */
    List<UsuarioDTO> obtenerUsuariosPorRol(RolUsuario rol);

    /**
     * Actualiza el estado de activación de un usuario (activo/inactivo). Es
     * transaccional.
     *
     * @param id ID del usuario.
     * @param nuevoEstado true para activar, false para desactivar.
     * @return El UsuarioDTO actualizado.
     * @throws UsuarioNotFoundException si el usuario no existe.
     * @throws IllegalArgumentException si el ID es nulo.
     */
    UsuarioDTO actualizarEstadoUsuario(Integer id, boolean nuevoEstado);

    /**
     * Elimina un usuario por su ID. ¡Precaución con FKs! Es transaccional.
     *
     * @param id ID del usuario a eliminar.
     * @throws UsuarioNotFoundException si el usuario no existe.
     * @throws IllegalArgumentException si el ID es nulo.
     * @throws RuntimeException si falla por FKs u otro error.
     */
    void eliminarUsuario(Integer id);

    /**
     * Permite a un usuario cambiar su propia contraseña verificando la antigua.
     * Marca cambioPasswordRequerido como false. Es transaccional.
     *
     * @param userId ID del usuario.
     * @param passwordAntigua Contraseña actual en texto plano.
     * @param passwordNueva Nueva contraseña en texto plano (>= 8 chars, !=
     * antigua).
     * @throws UsuarioNotFoundException si el usuario no existe.
     * @throws PasswordIncorrectoException si la contraseña antigua no coincide.
     * @throws IllegalArgumentException si las contraseñas son inválidas.
     */
    void cambiarPassword(Integer userId, String passwordAntigua, String passwordNueva);

    /**
     * Cambia la contraseña sin verificar la antigua (para flujo obligatorio
     * inicial). Marca cambioPasswordRequerido como false. Es transaccional.
     *
     * @param userId ID del usuario.
     * @param passwordNueva Nueva contraseña en texto plano (>= 8 chars).
     * @throws UsuarioNotFoundException si el usuario no existe.
     * @throws IllegalArgumentException si los datos son inválidos.
     */
    void cambiarPasswordYMarcarActualizada(Integer userId, String passwordNueva);

    /**
     * Actualiza el nombre de un usuario. Para uso del Admin principalmente. Es
     * transaccional.
     *
     * @param id ID del usuario.
     * @param nuevoNombre Nuevo nombre (no vacío).
     * @return El UsuarioDTO actualizado.
     * @throws UsuarioNotFoundException si el usuario no existe.
     * @throws IllegalArgumentException si los datos son inválidos.
     */
    UsuarioDTO actualizarNombreUsuario(Integer id, String nuevoNombre);

}
