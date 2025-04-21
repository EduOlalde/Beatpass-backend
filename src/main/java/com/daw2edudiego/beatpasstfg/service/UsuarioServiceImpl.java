/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
        // ... (Implementación sin cambios) ...
        log.info("Iniciando creación de usuario con email: {}", ucDTO.getEmail());
        EntityManager em = null;
        EntityTransaction tx = null;
        if (ucDTO == null || ucDTO.getEmail() == null || ucDTO.getEmail().isBlank() || ucDTO.getPassword() == null || ucDTO.getPassword().isEmpty() || ucDTO.getNombre() == null || ucDTO.getNombre().isBlank() || ucDTO.getRol() == null) {
            log.warn("Intento de crear usuario con datos inválidos/incompletos.");
            throw new IllegalArgumentException("Datos de usuario incompletos o inválidos.");
        }
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            log.debug("Verificando si el email {} ya existe.", ucDTO.getEmail());
            Optional<Usuario> existenteOpt = usuarioRepository.findByEmail(em, ucDTO.getEmail()); // Usa el repo
            if (existenteOpt.isPresent()) {
                log.warn("Intento de crear usuario con email existente: {}", ucDTO.getEmail());
                throw new EmailExistenteException("El email '" + ucDTO.getEmail() + "' ya está registrado.");
            }
            log.debug("Mapeando DTO a entidad Usuario");
            Usuario usuario = new Usuario();
            usuario.setNombre(ucDTO.getNombre());
            usuario.setEmail(ucDTO.getEmail());
            usuario.setRol(ucDTO.getRol());
            usuario.setEstado(true);
            log.debug("Hasheando contraseña para el usuario {}", ucDTO.getEmail());
            String hashedPassword = PasswordUtil.hashPassword(ucDTO.getPassword());
            usuario.setPassword(hashedPassword);
            usuario.setCambioPasswordRequerido(true); 
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
                throw e;
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
        // ... (Implementación sin cambios, devuelve DTO) ...
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
        // ... (Implementación sin cambios, devuelve DTO) ...
        log.debug("Buscando usuario (DTO) por email: {}", email);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(em, email);
            log.info("Resultado de búsqueda DTO para usuario email {}: {}", email, usuarioOpt.isPresent() ? "Encontrado" : "No encontrado");
            return usuarioOpt.map(this::mapEntityToDto);
        } catch (Exception e) {
            log.error("Error al obtener usuario DTO por email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para obtenerUsuarioPorEmail.");
            }
        }
    }

    // --- NUEVO MÉTODO IMPLEMENTADO ---
    @Override
    public Optional<Usuario> obtenerEntidadUsuarioPorEmailParaAuth(String email) {
        log.debug("Buscando entidad completa de usuario por email para auth: {}", email);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Llama directamente al método del repositorio que devuelve la entidad
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(em, email);
            log.info("Resultado de búsqueda de entidad para auth email {}: {}", email, usuarioOpt.isPresent() ? "Encontrado" : "No encontrado");
            return usuarioOpt; // Devuelve el Optional<Usuario> directamente
        } catch (Exception e) {
            log.error("Error al obtener entidad usuario por email para auth {}: {}", email, e.getMessage(), e);
            return Optional.empty(); // Devuelve vacío en caso de error
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para obtenerEntidadUsuarioPorEmailParaAuth.");
            }
        }
    }
    // --- FIN NUEVO MÉTODO ---

    @Override
    public List<UsuarioDTO> obtenerUsuariosPorRol(RolUsuario rol) {
        // ... (Implementación sin cambios) ...
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
        // ... (Implementación sin cambios) ...
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
        // ... (Implementación sin cambios) ...
        log.info("Iniciando eliminación de usuario ID: {}", id);
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            log.debug("Intentando eliminar usuario con ID: {}", id);
            boolean eliminado = usuarioRepository.deleteById(em, id);
            if (!eliminado) {
                log.warn("No se pudo eliminar usuario ID {}, no encontrado o error previo.", id);
                throw new UsuarioNotFoundException("Usuario no encontrado o no se pudo eliminar ID: " + id);
            }
            tx.commit();
            log.info("Usuario ID: {} eliminado (o marcado para eliminación) correctamente.", id);
        } catch (Exception e) {
            log.error("Error durante la eliminación del usuario ID {}: {}", id, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Realizando rollback de la transacción de eliminación de usuario.");
                tx.rollback();
            }
            if (e instanceof UsuarioNotFoundException) {
                throw e;
            }
            throw new RuntimeException("Error eliminando usuario: " + e.getMessage() + ". Verifique si tiene datos asociados (ej: festivales).", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para eliminarUsuario.");
            }
        }
    }

    @Override
    public void cambiarPassword(Integer userId, String passwordAntigua, String passwordNueva) {
        log.info("Iniciando cambio de contraseña para usuario ID: {}", userId);
        // Validación básica de entrada
        if (userId == null || passwordAntigua == null || passwordAntigua.isEmpty() || passwordNueva == null || passwordNueva.isEmpty()) {
            throw new IllegalArgumentException("ID de usuario, contraseña antigua y nueva son obligatorios.");
        }
        if (passwordNueva.length() < 8) { // Misma validación que al crear
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        }
        if (passwordAntigua.equals(passwordNueva)) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la anterior.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Buscando usuario con ID: {}", userId);
            Usuario usuario = usuarioRepository.findById(em, userId)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + userId));

            log.debug("Verificando contraseña antigua para usuario ID: {}", userId);
            if (!PasswordUtil.checkPassword(passwordAntigua, usuario.getPassword())) {
                log.warn("Intento de cambio de contraseña fallido (contraseña antigua incorrecta) para usuario ID: {}", userId);
                throw new PasswordIncorrectoException("La contraseña actual introducida es incorrecta.");
            }

            log.debug("Hasheando nueva contraseña para usuario ID: {}", userId);
            String nuevoHash = PasswordUtil.hashPassword(passwordNueva);

            // Actualizar contraseña y marcar cambio como realizado
            usuario.setPassword(nuevoHash);
            usuario.setCambioPasswordRequerido(false); // <-- Marcar como ya cambiado

            log.debug("Guardando cambios de contraseña y estado para usuario ID: {}", userId);
            usuarioRepository.save(em, usuario); // merge

            tx.commit();
            log.info("Contraseña cambiada exitosamente para usuario ID: {}", userId);

        } catch (Exception e) {
            log.error("Error durante el cambio de contraseña para usuario ID {}: {}", userId, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Realizando rollback de la transacción de cambio de contraseña.");
                tx.rollback();
            }
            // Relanzar excepciones específicas o una genérica
            if (e instanceof UsuarioNotFoundException || e instanceof PasswordIncorrectoException || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Error al cambiar la contraseña: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para cambiarPassword.");
            }
        }
    }
    
    /**
     * Cambia la contraseña de un usuario SIN verificar la contraseña antigua
     * y marca el flag 'cambioPasswordRequerido' a false.
     * Usado para el flujo de cambio obligatorio en el primer login.
     *
     * @param userId        ID del usuario a modificar.
     * @param passwordNueva La nueva contraseña en texto plano.
     * @throws UsuarioNotFoundException Si el usuario no se encuentra.
     * @throws IllegalArgumentException Si la nueva contraseña es inválida.
     * @throws RuntimeException Si ocurre un error durante la transacción.
     */
    @Override 
    public void cambiarPasswordYMarcarActualizada(Integer userId, String passwordNueva) {
        log.info("Iniciando cambio de contraseña obligatorio para usuario ID: {}", userId);
        // Validación básica de entrada
        if (userId == null || passwordNueva == null || passwordNueva.isEmpty()) {
            throw new IllegalArgumentException("ID de usuario y nueva contraseña son obligatorios.");
        }
        // Puedes añadir aquí la misma validación de complejidad que usas en otros sitios
        // if (passwordNueva.length() < 8) {
        //     throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        // }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Buscando usuario con ID: {}", userId);
            Usuario usuario = usuarioRepository.findById(em, userId)
                    .orElseThrow(() -> {
                         log.warn("Intento de cambio obligatorio a usuario no existente ID: {}", userId);
                         return new UsuarioNotFoundException("Usuario no encontrado con ID: " + userId);
                     });

            log.debug("Hasheando nueva contraseña para usuario ID: {}", userId);
            String nuevoHash = PasswordUtil.hashPassword(passwordNueva);

            // Actualizar contraseña y marcar cambio como realizado
            usuario.setPassword(nuevoHash);
            usuario.setCambioPasswordRequerido(false); // <-- Marcar como ya cambiado

            log.debug("Guardando cambios de contraseña y estado para usuario ID: {}", userId);
            usuarioRepository.save(em, usuario); // merge

            tx.commit();
            log.info("Contraseña cambiada (obligatorio) exitosamente para usuario ID: {}", userId);

        } catch (Exception e) {
            log.error("Error durante el cambio de contraseña obligatorio para usuario ID {}: {}", userId, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Realizando rollback de la transacción de cambio de contraseña obligatorio.");
                tx.rollback();
            }
            // Relanzar excepciones específicas o una genérica
            if (e instanceof UsuarioNotFoundException || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Error al cambiar la contraseña obligatoria: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para cambiarPasswordYMarcarActualizada.");
            }
        }
    }

    // --- Método Privado de Mapeo (sin cambios) ---
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
}
