/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de UsuarioRepository usando JPA EntityManager. Transacciones
 * gestionadas externamente (Servicio).
 */
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private static final Logger log = LoggerFactory.getLogger(UsuarioRepositoryImpl.class);

    @Override
    public Usuario save(EntityManager em, Usuario usuario) {
        log.debug("Intentando guardar usuario con email: {}", usuario.getEmail());
        if (usuario.getIdUsuario() == null) {
            em.persist(usuario);
            log.info("Usuario nuevo persistido con email: {}", usuario.getEmail());
            return usuario;
        } else {
            Usuario mergedUsuario = em.merge(usuario);
            log.info("Usuario actualizado con ID: {}", mergedUsuario.getIdUsuario());
            return mergedUsuario;
        }
    }

    @Override
    public Optional<Usuario> findById(EntityManager em, Integer id) {
        log.debug("Buscando usuario con ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        Usuario usuario = em.find(Usuario.class, id);
        if (usuario != null) {
            log.debug("Usuario encontrado con ID: {}", id);
        } else {
            log.debug("Usuario NO encontrado con ID: {}", id);
        }
        return Optional.ofNullable(usuario);
    }

    @Override
    public Optional<Usuario> findByEmail(EntityManager em, String email) {
        log.debug("Buscando usuario con email: {}", email);
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.email = :emailParam", Usuario.class);
            query.setParameter("emailParam", email);
            Usuario usuario = query.getSingleResult(); // Lanza NoResultException si no se encuentra
            log.debug("Usuario encontrado con email: {}", email);
            return Optional.of(usuario);
        } catch (NoResultException e) {
            log.debug("Usuario NO encontrado con email: {}", email);
            return Optional.empty(); // Email no encontrado
        } catch (Exception e) {
            log.error("Error buscando usuario por email {}: {}", email, e.getMessage(), e);
            return Optional.empty(); // Otro tipo de error
        }
    }

    @Override
    public List<Usuario> findAll(EntityManager em) {
        log.debug("Buscando todos los usuarios.");
        try {
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u ORDER BY u.nombre", Usuario.class);
            List<Usuario> usuarios = query.getResultList();
            log.debug("Encontrados {} usuarios.", usuarios.size());
            return usuarios;
        } catch (Exception e) {
            log.error("Error al buscar todos los usuarios: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Usuario> findByRol(EntityManager em, RolUsuario rol) {
        log.debug("Buscando usuarios con rol: {}", rol);
        if (rol == null) {
            return Collections.emptyList();
        }
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.rol = :rolParam ORDER BY u.nombre", Usuario.class);
            query.setParameter("rolParam", rol);
            List<Usuario> usuarios = query.getResultList();
            log.debug("Encontrados {} usuarios con rol {}.", usuarios.size(), rol);
            return usuarios;
        } catch (Exception e) {
            log.error("Error al buscar usuarios por rol {}: {}", rol, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean deleteById(EntityManager em, Integer id) {
        log.debug("Intentando eliminar usuario con ID: {}", id);
        Optional<Usuario> usuarioOpt = findById(em, id);
        if (usuarioOpt.isPresent()) {
            // Considerar restricciones: ¿Se puede borrar un promotor si tiene festivales?
            // La FK en Festival está como ON DELETE RESTRICT, así que esto fallará si
            // el promotor tiene festivales. Deberías manejar esa excepción (PersistenceException)
            // en la capa de servicio o desactivar al usuario en lugar de borrarlo.
            try {
                em.remove(usuarioOpt.get());
                log.info("Usuario con ID: {} marcado para eliminación.", id);
                return true;
            } catch (Exception e) { // Captura específica de PersistenceException sería mejor
                log.error("Error al intentar marcar para eliminar usuario ID {}: {}", id, e.getMessage());
                // Podría ser por restricciones de FK
                return false; // O relanzar una excepción específica
            }
        } else {
            log.warn("No se pudo eliminar. Usuario no encontrado con ID: {}", id);
            return false;
        }
    }
}
