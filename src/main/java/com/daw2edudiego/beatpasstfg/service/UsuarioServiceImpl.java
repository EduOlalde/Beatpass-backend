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
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepository;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import com.daw2edudiego.beatpasstfg.util.PasswordUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de UsuarioService. Gestiona transacciones y hashing.
 */
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl() {
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    @Override
    public UsuarioDTO crearUsuario(UsuarioCreacionDTO ucDTO) {
        log.info("Iniciando creación de usuario con email: {}", ucDTO.getEmail());
        EntityManager em = null;
        EntityTransaction tx = null;

        // Validación básica de entrada
        if (ucDTO == null || ucDTO.getEmail() == null || ucDTO.getEmail().isBlank()
                || ucDTO.getPassword() == null || ucDTO.getPassword().isEmpty()
                || ucDTO.getNombre() == null || ucDTO.getNombre().isBlank() || ucDTO.getRol() == null) {
            log.warn("Intento de crear usuario con datos inválidos/incompletos.");
            throw new IllegalArgumentException("Datos de usuario incompletos o inválidos.");
        }

        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Verificando si el email {} ya existe.", ucDTO.getEmail());
            Optional<Usuario> existenteOpt = usuarioRepository.findByEmail(em, ucDTO.getEmail());
            if (existenteOpt.isPresent()) {
                log.warn("Intento de crear usuario con email existente: {}", ucDTO.getEmail());
                throw new EmailExistenteException("El email '" + ucDTO.getEmail() + "' ya está registrado.");
            }

            log.debug("Mapeando DTO a entidad Usuario");
            Usuario usuario = new Usuario();
            usuario.setNombre(ucDTO.getNombre());
            usuario.setEmail(ucDTO.getEmail());
            usuario.setRol(ucDTO.getRol());
            usuario.setEstado(true); // Por defecto, activo al crear

            log.debug("Hasheando contraseña para el usuario {}", ucDTO.getEmail());
            String hashedPassword = PasswordUtil.hashPassword(ucDTO.getPassword());
            usuario.setPassword(hashedPassword);

            log.debug("Guardando entidad Usuario");
            usuario = usuarioRepository.save(em, usuario);

            tx.commit();
            log.info("Usuario creado exitosamente con ID: {} y email: {}", usuario.getIdUsuario(), usuario.getEmail());

            return mapEntityToDto(usuario);

        } catch (Exception e) {
            log.error("Error durante la creación del usuario con email {}: {}", ucDTO.getEmail(), e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Realizando rollback de la transacción de creación de usuario.");
                tx.rollback();
            }
            if (e instanceof EmailExistenteException || e instanceof IllegalArgumentException) {
                throw e; // Relanzar excepciones de validación/negocio
            }
            throw new RuntimeException("Error creando usuario: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para crearUsuario.");
            }
        }
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Integer id) {
        log.debug("Buscando usuario por ID: {}", id);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(em, id);
            log.info("Resultado de búsqueda para usuario ID {}: {}", id, usuarioOpt.isPresent() ? "Encontrado" : "No encontrado");
            return usuarioOpt.map(this::mapEntityToDto);
        } catch (Exception e) {
            log.error("Error al obtener usuario por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para obtenerUsuarioPorId.");
            }
        }
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(em, email);
            log.info("Resultado de búsqueda para usuario email {}: {}", email, usuarioOpt.isPresent() ? "Encontrado" : "No encontrado");
            return usuarioOpt.map(this::mapEntityToDto);
        } catch (Exception e) {
            log.error("Error al obtener usuario por email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para obtenerUsuarioPorEmail.");
            }
        }
    }

    @Override
    public List<UsuarioDTO> obtenerUsuariosPorRol(RolUsuario rol) {
        log.debug("Obteniendo usuarios con rol: {}", rol);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Usuario> usuarios = usuarioRepository.findByRol(em, rol);
            log.info("Encontrados {} usuarios con rol {}", usuarios.size(), rol);
            return usuarios.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo usuarios por rol {}: {}", rol, e.getMessage(), e);
            return List.of();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para obtenerUsuariosPorRol.");
            }
        }
    }

    @Override
    public UsuarioDTO actualizarEstadoUsuario(Integer id, boolean nuevoEstado) {
        log.info("Iniciando actualización de estado a {} para usuario ID: {}", nuevoEstado, id);
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Buscando usuario a actualizar estado con ID: {}", id);
            Usuario usuario = usuarioRepository.findById(em, id)
                    .orElseThrow(() -> {
                        log.warn("Intento de actualizar estado a usuario no existente ID: {}", id);
                        return new UsuarioNotFoundException("Usuario no encontrado con ID: " + id);
                    });

            log.debug("Actualizando estado de usuario ID {} a {}", id, nuevoEstado);
            usuario.setEstado(nuevoEstado);

            usuario = usuarioRepository.save(em, usuario); // merge

            tx.commit();
            log.info("Estado de usuario ID: {} actualizado a {} correctamente.", id, nuevoEstado);

            return mapEntityToDto(usuario);

        } catch (Exception e) {
            log.error("Error durante la actualización de estado del usuario ID {}: {}", id, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Realizando rollback de la transacción de actualización de estado de usuario.");
                tx.rollback();
            }
            if (e instanceof UsuarioNotFoundException) {
                throw e;
            }
            throw new RuntimeException("Error actualizando estado de usuario: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para actualizarEstadoUsuario.");
            }
        }
    }

    @Override
    public void eliminarUsuario(Integer id) {
        log.info("Iniciando eliminación de usuario ID: {}", id);
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // No es necesario buscar primero, el repositorio lo hará.
            // Pero la validación de si se puede borrar (ej: promotor sin festivales)
            // debería hacerse aquí o capturar la excepción del repositorio.
            log.debug("Intentando eliminar usuario con ID: {}", id);
            boolean eliminado = usuarioRepository.deleteById(em, id);

            if (!eliminado) {
                // Podría ser porque no se encontró o por una restricción de FK
                // El repositorio ya loggea si no se encuentra. Si falla por FK,
                // lanzará una PersistenceException que se captura abajo.
                // Lanzamos NotFound si el repo devuelve false y no hubo otra excepción.
                log.warn("No se pudo eliminar usuario ID {}, no encontrado o error previo.", id);
                throw new UsuarioNotFoundException("Usuario no encontrado o no se pudo eliminar ID: " + id);
            }

            tx.commit();
            log.info("Usuario ID: {} eliminado (o marcado para eliminación) correctamente.", id);

        } catch (Exception e) { // Capturar PersistenceException específicamente sería mejor
            log.error("Error durante la eliminación del usuario ID {}: {}", id, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Realizando rollback de la transacción de eliminación de usuario.");
                tx.rollback();
            }
            if (e instanceof UsuarioNotFoundException) {
                throw e;
            }
            // Podría ser una ConstraintViolationException si hay FKs
            throw new RuntimeException("Error eliminando usuario: " + e.getMessage() + ". Verifique si tiene datos asociados (ej: festivales).", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para eliminarUsuario.");
            }
        }
    }

    // --- Métodos Privados de Mapeo ---
    private UsuarioDTO mapEntityToDto(Usuario u) {
        if (u == null) {
            return null;
        }
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(u.getIdUsuario());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setRol(u.getRol());
        dto.setEstado(u.getEstado());
        dto.setFechaCreacion(u.getFechaCreacion());
        dto.setFechaModificacion(u.getFechaModificacion());
        return dto;
    }

    // No necesitamos mapear DTO a Entidad para lectura normalmente,
    // pero sí para creación (hecho en crearUsuario).
}
