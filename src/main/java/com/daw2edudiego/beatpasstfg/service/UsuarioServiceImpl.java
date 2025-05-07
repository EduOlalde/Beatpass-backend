package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.exception.EmailExistenteException;
import com.daw2edudiego.beatpasstfg.exception.PasswordIncorrectoException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepository;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import com.daw2edudiego.beatpasstfg.util.PasswordUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de UsuarioService.
 */
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);
    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl() {
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    @Override
    public UsuarioDTO crearUsuario(UsuarioCreacionDTO ucDTO) {
        log.info("Service: Iniciando creación de usuario con email: {}", ucDTO != null ? ucDTO.getEmail() : "null");
        validarUsuarioCreacionDTO(ucDTO);

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            if (usuarioRepository.findByEmail(em, ucDTO.getEmail()).isPresent()) {
                throw new EmailExistenteException("El email '" + ucDTO.getEmail() + "' ya está registrado.");
            }

            Usuario usuario = new Usuario();
            usuario.setNombre(ucDTO.getNombre().trim());
            usuario.setEmail(ucDTO.getEmail().trim().toLowerCase());
            usuario.setRol(ucDTO.getRol());
            usuario.setEstado(true);
            usuario.setCambioPasswordRequerido(true);
            usuario.setPassword(PasswordUtil.hashPassword(ucDTO.getPassword()));

            usuario = usuarioRepository.save(em, usuario);
            tx.commit();

            log.info("Usuario creado exitosamente con ID: {} y email: {}", usuario.getIdUsuario(), usuario.getEmail());
            return mapEntityToDto(usuario);

        } catch (Exception e) {
            handleException(e, tx, "crear usuario con email " + (ucDTO != null ? ucDTO.getEmail() : "null"));
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Integer id) {
        log.debug("Service: Buscando usuario (DTO) por ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            return usuarioRepository.findById(em, id).map(this::mapEntityToDto);
        } catch (Exception e) {
            log.error("Error al obtener usuario (DTO) por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email) {
        log.debug("Service: Buscando usuario (DTO) por email: {}", email);
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            return usuarioRepository.findByEmail(em, email).map(this::mapEntityToDto);
        } catch (Exception e) {
            log.error("Error al obtener usuario (DTO) por email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<Usuario> obtenerEntidadUsuarioPorEmailParaAuth(String email) {
        log.debug("Service: Buscando entidad completa de usuario por email para auth: {}", email);
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            return usuarioRepository.findByEmail(em, email);
        } catch (Exception e) {
            log.error("Error al obtener entidad usuario por email para auth {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<UsuarioDTO> obtenerUsuariosPorRol(RolUsuario rol) {
        log.debug("Service: Obteniendo usuarios con rol: {}", rol);
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo.");
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Usuario> usuarios = usuarioRepository.findByRol(em, rol);
            log.info("Encontrados {} usuarios con rol {}", usuarios.size(), rol);
            return usuarios.stream().map(this::mapEntityToDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo usuarios por rol {}: {}", rol, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public UsuarioDTO actualizarEstadoUsuario(Integer id, boolean nuevoEstado) {
        log.info("Service: Iniciando actualización de estado a {} para usuario ID: {}", nuevoEstado, id);
        if (id == null) {
            throw new IllegalArgumentException("ID de usuario es requerido.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario usuario = usuarioRepository.findById(em, id)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

            if (usuario.getEstado().equals(nuevoEstado)) {
                log.info("El estado del usuario ID {} ya es {}. No se requiere actualización.", id, nuevoEstado);
                tx.commit();
                return mapEntityToDto(usuario);
            }

            usuario.setEstado(nuevoEstado);
            usuario = usuarioRepository.save(em, usuario);
            tx.commit();

            log.info("Estado de usuario ID: {} actualizado a {} correctamente.", id, nuevoEstado);
            return mapEntityToDto(usuario);

        } catch (Exception e) {
            handleException(e, tx, "actualizar estado usuario ID " + id);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public void eliminarUsuario(Integer id) {
        log.info("Service: Iniciando eliminación de usuario ID: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID de usuario es requerido.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            boolean eliminado = usuarioRepository.deleteById(em, id);
            if (!eliminado) {
                // deleteById ya lanza UsuarioNotFoundException si no lo encuentra
                log.warn("deleteById devolvió false para usuario ID {} sin lanzar excepción.", id);
                throw new RuntimeException("No se pudo completar la eliminación del usuario ID: " + id);
            }
            tx.commit();

            log.info("Usuario ID: {} eliminado correctamente.", id);

        } catch (PersistenceException e) {
            handleException(e, tx, "eliminar usuario ID " + id);
            log.error("Error de persistencia al eliminar usuario ID {}. Causa probable: FKs.", id);
            throw new RuntimeException("No se pudo eliminar el usuario ID " + id + " debido a datos asociados.", e);
        } catch (Exception e) {
            handleException(e, tx, "eliminar usuario ID " + id);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public void cambiarPassword(Integer userId, String passwordAntigua, String passwordNueva) {
        log.info("Service: Iniciando cambio de contraseña para usuario ID: {}", userId);
        validarCambioPassword(userId, passwordAntigua, passwordNueva);

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario usuario = usuarioRepository.findById(em, userId)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + userId));

            if (!PasswordUtil.checkPassword(passwordAntigua, usuario.getPassword())) {
                throw new PasswordIncorrectoException("La contraseña actual introducida es incorrecta.");
            }

            usuario.setPassword(PasswordUtil.hashPassword(passwordNueva));
            usuario.setCambioPasswordRequerido(false); // Marcar como actualizado
            usuarioRepository.save(em, usuario);
            tx.commit();

            log.info("Contraseña cambiada exitosamente para usuario ID: {}", userId);

        } catch (Exception e) {
            handleException(e, tx, "cambiar contraseña para usuario ID " + userId);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public void cambiarPasswordYMarcarActualizada(Integer userId, String passwordNueva) {
        log.info("Service: Iniciando cambio de contraseña obligatorio para usuario ID: {}", userId);
        validarPasswordNueva(userId, passwordNueva);

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario usuario = usuarioRepository.findById(em, userId)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + userId));

            usuario.setPassword(PasswordUtil.hashPassword(passwordNueva));
            usuario.setCambioPasswordRequerido(false);
            usuarioRepository.save(em, usuario);
            tx.commit();

            log.info("Contraseña cambiada (obligatorio) exitosamente para usuario ID: {}", userId);

        } catch (Exception e) {
            handleException(e, tx, "cambiar contraseña obligatoria para usuario ID " + userId);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public UsuarioDTO actualizarNombreUsuario(Integer id, String nuevoNombre) {
        log.info("Service: Iniciando actualización de nombre a '{}' para usuario ID: {}", nuevoNombre, id);
        if (id == null || nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("ID y nuevo nombre (no vacío) son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario usuario = usuarioRepository.findById(em, id)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

            if (usuario.getNombre().equals(nuevoNombre.trim())) {
                log.info("El nombre del usuario ID {} ya es '{}'. No se requiere actualización.", id, nuevoNombre);
                tx.commit();
                return mapEntityToDto(usuario);
            }

            usuario.setNombre(nuevoNombre.trim());
            usuario = usuarioRepository.save(em, usuario);
            tx.commit();

            log.info("Nombre de usuario ID: {} actualizado a '{}' correctamente.", id, nuevoNombre);
            return mapEntityToDto(usuario);

        } catch (Exception e) {
            handleException(e, tx, "actualizar nombre usuario ID " + id);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda ---
    /**
     * Valida los campos del DTO de creación.
     */
    private void validarUsuarioCreacionDTO(UsuarioCreacionDTO dto) {
        if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()
                || dto.getPassword() == null || dto.getPassword().isEmpty()
                || dto.getNombre() == null || dto.getNombre().isBlank() || dto.getRol() == null) {
            throw new IllegalArgumentException("Datos de usuario (nombre, email, password, rol) son requeridos.");
        }
        if (dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
    }

    /**
     * Valida los parámetros para el cambio de contraseña iniciado por el
     * usuario.
     */
    private void validarCambioPassword(Integer userId, String antigua, String nueva) {
        if (userId == null || antigua == null || antigua.isEmpty() || nueva == null || nueva.isEmpty()) {
            throw new IllegalArgumentException("ID usuario, contraseña antigua y nueva son obligatorios.");
        }
        if (nueva.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        }
        if (antigua.equals(nueva)) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la anterior.");
        }
    }

    /**
     * Valida los parámetros para el cambio de contraseña obligatorio.
     */
    private void validarPasswordNueva(Integer userId, String nueva) {
        if (userId == null || nueva == null || nueva.isEmpty()) {
            throw new IllegalArgumentException("ID usuario y nueva contraseña son obligatorios.");
        }
        if (nueva.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        }
    }

    /**
     * Mapea entidad Usuario a DTO.
     */
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
        dto.setCambioPasswordRequerido(u.getCambioPasswordRequerido());
        dto.setFechaCreacion(u.getFechaCreacion());
        dto.setFechaModificacion(u.getFechaModificacion());
        return dto;
    }

    /**
     * Manejador genérico de excepciones.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    /**
     * Realiza rollback si la transacción está activa.
     */
    private void rollbackTransaction(EntityTransaction tx, String action) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Rollback de transacción de {} realizado.", action);
            } catch (Exception rbEx) {
                log.error("Error durante el rollback de {}: {}", action, rbEx.getMessage(), rbEx);
            }
        }
    }

    /**
     * Cierra el EntityManager.
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    /**
     * Mapea excepciones técnicas a de negocio o Runtime.
     */
    private RuntimeException mapException(Exception e) {
        if (e instanceof EmailExistenteException || e instanceof PasswordIncorrectoException || e instanceof UsuarioNotFoundException
                || e instanceof IllegalArgumentException || e instanceof SecurityException || e instanceof IllegalStateException
                || e instanceof PersistenceException || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio Usuario: " + e.getMessage(), e);
    }
}
