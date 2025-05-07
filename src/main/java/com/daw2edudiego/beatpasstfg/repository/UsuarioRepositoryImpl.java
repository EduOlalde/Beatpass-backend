package com.daw2edudiego.beatpasstfg.repository;

import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de UsuarioRepository usando JPA EntityManager.
 */
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private static final Logger log = LoggerFactory.getLogger(UsuarioRepositoryImpl.class);

    @Override
    public Usuario save(EntityManager em, Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("La entidad Usuario no puede ser nula.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email del Usuario no puede ser nulo ni vacío.");
        }

        log.debug("Intentando guardar usuario con ID: {} y email: {}", usuario.getIdUsuario(), usuario.getEmail());
        try {
            if (usuario.getIdUsuario() == null) {
                log.trace("Persistiendo nuevo Usuario...");
                em.persist(usuario);
                log.info("Nuevo Usuario persistido con ID: {}", usuario.getIdUsuario());
                return usuario;
            } else {
                log.trace("Actualizando Usuario con ID: {}", usuario.getIdUsuario());
                Usuario mergedUsuario = em.merge(usuario);
                log.info("Usuario actualizado con ID: {}", mergedUsuario.getIdUsuario());
                return mergedUsuario;
            }
        } catch (PersistenceException e) {
            log.error("Error de persistencia al guardar Usuario (ID: {}, Email: {}): {}",
                    usuario.getIdUsuario(), usuario.getEmail(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al guardar Usuario (ID: {}, Email: {}): {}",
                    usuario.getIdUsuario(), usuario.getEmail(), e.getMessage(), e);
            throw new PersistenceException("Error inesperado al guardar Usuario", e);
        }
    }

    @Override
    public Optional<Usuario> findById(EntityManager em, Integer id) {
        log.debug("Buscando usuario con ID: {}", id);
        if (id == null) {
            log.warn("Intento de buscar Usuario con ID nulo.");
            return Optional.empty();
        }
        try {
            Usuario usuario = em.find(Usuario.class, id);
            return Optional.ofNullable(usuario);
        } catch (IllegalArgumentException e) {
            log.error("Argumento ilegal al buscar Usuario por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado buscando usuario con ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Usuario> findByEmail(EntityManager em, String email) {
        log.debug("Buscando usuario con email: {}", email);
        if (email == null || email.isBlank()) {
            log.warn("Intento de buscar Usuario con email nulo o vacío.");
            return Optional.empty();
        }
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.email = :emailParam", Usuario.class);
            query.setParameter("emailParam", email);
            Usuario usuario = query.getSingleResult();
            return Optional.of(usuario);
        } catch (NoResultException e) {
            log.trace("Usuario NO encontrado con email: {}", email);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error buscando usuario por email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
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
            log.warn("Intento de buscar usuarios con rol nulo.");
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
        if (id == null) {
            log.warn("Intento de eliminar Usuario con ID nulo.");
            return false;
        }
        Optional<Usuario> usuarioOpt = findById(em, id);
        if (usuarioOpt.isPresent()) {
            try {
                em.remove(usuarioOpt.get());
                log.info("Usuario con ID: {} marcado para eliminación.", id);
                return true;
            } catch (PersistenceException e) {
                log.error("Error de persistencia al eliminar Usuario ID {}: {}. Causa probable: restricciones de FK.", id, e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Error inesperado al eliminar Usuario ID {}: {}", id, e.getMessage(), e);
                throw new PersistenceException("Error inesperado al eliminar Usuario", e);
            }
        } else {
            log.warn("No se pudo eliminar. Usuario no encontrado con ID: {}", id);
            return false;
        }
    }
}
