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
import jakarta.persistence.PersistenceException; // Importar para manejo específico
import java.util.Collections; // Importar para devolver listas vacías seguras
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de la interfaz {@link UsuarioService}. Gestiona la lógica de
 * negocio para las operaciones sobre Usuarios, incluyendo la creación,
 * consulta, actualización de estado, eliminación y cambio de contraseña. Maneja
 * las transacciones JPA y utiliza {@link UsuarioRepository} para el acceso a
 * datos y {@link PasswordUtil} para el hashing de contraseñas.
 *
 * @see UsuarioService
 * @see UsuarioRepository
 * @see PasswordUtil
 * @author Eduardo Olalde
 */
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor que inicializa el repositorio de usuarios.
     */
    public UsuarioServiceImpl() {
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsuarioDTO crearUsuario(UsuarioCreacionDTO ucDTO) {
        log.info("Service: Iniciando creación de usuario con email: {}", ucDTO != null ? ucDTO.getEmail() : "null");

        // Validación robusta de entrada
        if (ucDTO == null || ucDTO.getEmail() == null || ucDTO.getEmail().isBlank()
                || ucDTO.getPassword() == null || ucDTO.getPassword().isEmpty()
                || ucDTO.getNombre() == null || ucDTO.getNombre().isBlank()
                || ucDTO.getRol() == null) {
            throw new IllegalArgumentException("Datos de usuario (nombre, email, password, rol) son requeridos y no pueden estar vacíos.");
        }
        // Podría añadirse validación de longitud/formato de contraseña aquí si es necesario
        if (ucDTO.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Verificar si el email ya existe
            if (usuarioRepository.findByEmail(em, ucDTO.getEmail()).isPresent()) {
                throw new EmailExistenteException("El email '" + ucDTO.getEmail() + "' ya está registrado.");
            }

            // Mapear DTO a entidad
            Usuario usuario = new Usuario();
            usuario.setNombre(ucDTO.getNombre().trim());
            usuario.setEmail(ucDTO.getEmail().trim().toLowerCase()); // Normalizar email
            usuario.setRol(ucDTO.getRol());
            usuario.setEstado(true); // Activo por defecto
            usuario.setCambioPasswordRequerido(true); // Requiere cambio al primer login

            // Hashear contraseña
            String hashedPassword = PasswordUtil.hashPassword(ucDTO.getPassword());
            usuario.setPassword(hashedPassword);

            // Guardar la nueva entidad
            usuario = usuarioRepository.save(em, usuario);

            tx.commit();
            log.info("Usuario creado exitosamente con ID: {} y email: {}", usuario.getIdUsuario(), usuario.getEmail());

            return mapEntityToDto(usuario);

        } catch (Exception e) {
            handleException(e, tx, "crear usuario con email " + (ucDTO != null ? ucDTO.getEmail() : "null"));
            throw mapException(e); // Relanzar excepción mapeada
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Integer id) {
        log.debug("Service: Buscando usuario (DTO) por ID: {}", id);
        if (id == null) {
            log.warn("Intento de obtener usuario con ID nulo.");
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email) {
        log.debug("Service: Buscando usuario (DTO) por email: {}", email);
        if (email == null || email.isBlank()) {
            log.warn("Intento de obtener usuario con email nulo o vacío.");
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Usuario> obtenerEntidadUsuarioPorEmailParaAuth(String email) {
        log.debug("Service: Buscando entidad completa de usuario por email para auth: {}", email);
        if (email == null || email.isBlank()) {
            log.warn("Intento de obtener entidad de usuario para auth con email nulo o vacío.");
            return Optional.empty();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Llama directamente al método del repositorio
            return usuarioRepository.findByEmail(em, email);
        } catch (Exception e) {
            log.error("Error al obtener entidad usuario por email para auth {}: {}", email, e.getMessage(), e);
            return Optional.empty(); // Devuelve vacío en caso de error
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UsuarioDTO> obtenerUsuariosPorRol(RolUsuario rol) {
        log.debug("Service: Obteniendo usuarios con rol: {}", rol);
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo para buscar usuarios.");
        }
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
            return Collections.emptyList(); // Devolver lista vacía segura
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsuarioDTO actualizarEstadoUsuario(Integer id, boolean nuevoEstado) {
        log.info("Service: Iniciando actualización de estado a {} para usuario ID: {}", nuevoEstado, id);
        if (id == null) {
            throw new IllegalArgumentException("ID de usuario es requerido para actualizar estado.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Buscar usuario
            Usuario usuario = usuarioRepository.findById(em, id)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

            // Si el estado no cambia, no hacer nada (optimización)
            if (usuario.getEstado().equals(nuevoEstado)) {
                log.info("El estado del usuario ID {} ya es {}. No se requiere actualización.", id, nuevoEstado);
                tx.commit(); // Commit aunque no haya cambio
                return mapEntityToDto(usuario);
            }

            // Actualizar estado y guardar
            log.debug("Actualizando estado de usuario ID {} a {}", id, nuevoEstado);
            usuario.setEstado(nuevoEstado);
            usuario = usuarioRepository.save(em, usuario); // merge

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void eliminarUsuario(Integer id) {
        log.info("Service: Iniciando eliminación de usuario ID: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("ID de usuario es requerido para eliminar.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Intentar eliminar usando el repositorio
            boolean eliminado = usuarioRepository.deleteById(em, id);

            // deleteById ya lanza UsuarioNotFoundException si no lo encuentra
            // y PersistenceException si hay violación de FK.
            // Si devuelve false sin excepción, es un caso raro.
            if (!eliminado) {
                // Esto no debería ocurrir si deleteById funciona como se espera
                log.warn("deleteById devolvió false para usuario ID {} sin lanzar excepción.", id);
                // Podríamos lanzar una excepción genérica o confiar en que deleteById lance la correcta
                throw new RuntimeException("No se pudo completar la eliminación del usuario ID: " + id);
            }

            tx.commit();
            log.info("Usuario ID: {} eliminado (o marcado para eliminar) correctamente.", id);

        } catch (PersistenceException e) {
            // Captura específica para violación de FK
            handleException(e, tx, "eliminar usuario ID " + id);
            log.error("Error de persistencia al eliminar usuario ID {}. Causa probable: restricciones de FK (festivales, recargas).", id);
            throw new RuntimeException("No se pudo eliminar el usuario ID " + id + " debido a datos asociados.", e);
        } catch (Exception e) {
            handleException(e, tx, "eliminar usuario ID " + id);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cambiarPassword(Integer userId, String passwordAntigua, String passwordNueva) {
        log.info("Service: Iniciando cambio de contraseña para usuario ID: {}", userId);

        // Validación de parámetros
        if (userId == null || passwordAntigua == null || passwordAntigua.isEmpty()
                || passwordNueva == null || passwordNueva.isEmpty()) {
            throw new IllegalArgumentException("ID de usuario, contraseña antigua y nueva son obligatorios.");
        }
        if (passwordNueva.length() < 8) { // Requisito mínimo
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

            // Buscar usuario
            Usuario usuario = usuarioRepository.findById(em, userId)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + userId));

            // Verificar contraseña antigua
            if (!PasswordUtil.checkPassword(passwordAntigua, usuario.getPassword())) {
                throw new PasswordIncorrectoException("La contraseña actual introducida es incorrecta.");
            }

            // Hashear nueva contraseña
            String nuevoHash = PasswordUtil.hashPassword(passwordNueva);

            // Actualizar contraseña y marcar cambio como realizado
            usuario.setPassword(nuevoHash);
            usuario.setCambioPasswordRequerido(false);

            // Guardar cambios
            usuarioRepository.save(em, usuario); // merge

            tx.commit();
            log.info("Contraseña cambiada exitosamente para usuario ID: {}", userId);

        } catch (Exception e) {
            handleException(e, tx, "cambiar contraseña para usuario ID " + userId);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cambiarPasswordYMarcarActualizada(Integer userId, String passwordNueva) {
        log.info("Service: Iniciando cambio de contraseña obligatorio para usuario ID: {}", userId);

        // Validación de parámetros
        if (userId == null || passwordNueva == null || passwordNueva.isEmpty()) {
            throw new IllegalArgumentException("ID de usuario y nueva contraseña son obligatorios.");
        }
        if (passwordNueva.length() < 8) { // Requisito mínimo
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Buscar usuario
            Usuario usuario = usuarioRepository.findById(em, userId)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + userId));

            // Hashear nueva contraseña
            String nuevoHash = PasswordUtil.hashPassword(passwordNueva);

            // Actualizar contraseña y marcar cambio como realizado
            usuario.setPassword(nuevoHash);
            usuario.setCambioPasswordRequerido(false);

            // Guardar cambios
            usuarioRepository.save(em, usuario); // merge

            tx.commit();
            log.info("Contraseña cambiada (obligatorio) exitosamente para usuario ID: {}", userId);

        } catch (Exception e) {
            handleException(e, tx, "cambiar contraseña obligatoria para usuario ID " + userId);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsuarioDTO actualizarNombreUsuario(Integer id, String nuevoNombre) {
        log.info("Service: Iniciando actualización de nombre a '{}' para usuario ID: {}", nuevoNombre, id);
        if (id == null || nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("ID de usuario y nuevo nombre son requeridos y el nombre no puede estar vacío.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Buscar usuario
            Usuario usuario = usuarioRepository.findById(em, id)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

            // Si el nombre no cambia, no hacer nada
            if (usuario.getNombre().equals(nuevoNombre.trim())) {
                log.info("El nombre del usuario ID {} ya es '{}'. No se requiere actualización.", id, nuevoNombre);
                tx.commit(); // Commit aunque no haya cambio
                return mapEntityToDto(usuario);
            }

            // Actualizar nombre y guardar
            log.debug("Actualizando nombre de usuario ID {} a '{}'", id, nuevoNombre.trim());
            usuario.setNombre(nuevoNombre.trim());
            usuario = usuarioRepository.save(em, usuario); // merge

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


    // --- Métodos Privados de Ayuda (Helpers) ---
    /**
     * Mapea una entidad Usuario a su correspondiente UsuarioDTO. Excluye la
     * contraseña.
     *
     * @param u La entidad Usuario.
     * @return El UsuarioDTO mapeado, o {@code null} si la entidad es
     * {@code null}.
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
        dto.setCambioPasswordRequerido(u.getCambioPasswordRequerido()); // Incluir este estado
        dto.setFechaCreacion(u.getFechaCreacion());
        dto.setFechaModificacion(u.getFechaModificacion());
        return dto;
    }

    /**
     * Manejador genérico de excepciones para métodos de servicio. Realiza
     * rollback si hay transacción activa y loggea el error.
     *
     * @param e La excepción capturada.
     * @param tx La transacción activa (puede ser null).
     * @param action Descripción de la acción que falló.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
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
     * Cierra el EntityManager si está abierto.
     *
     * @param em El EntityManager a cerrar.
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    /**
     * Mapea excepciones técnicas a excepciones de negocio o RuntimeException.
     *
     * @param e La excepción original.
     * @return La excepción mapeada o una RuntimeException genérica.
     */
    private RuntimeException mapException(Exception e) {
        // Asegurarse de incluir todas las excepciones personalizadas relevantes
        if (e instanceof EmailExistenteException
                || e instanceof PasswordIncorrectoException
                || e instanceof UsuarioNotFoundException
                // Añadir otras excepciones de negocio si las hubiera
                || e instanceof IllegalArgumentException
                || e instanceof SecurityException
                || e instanceof IllegalStateException
                || e instanceof PersistenceException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        // Para otras excepciones no esperadas, envolvemos en RuntimeException
        return new RuntimeException("Error inesperado en la capa de servicio Usuario: " + e.getMessage(), e);
    }
}
