package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.AsistenteDTO;
import com.daw2edudiego.beatpasstfg.exception.AsistenteNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.model.Asistente;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.repository.AsistenteRepository;
import com.daw2edudiego.beatpasstfg.repository.AsistenteRepositoryImpl;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepository;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de la interfaz {@link AsistenteService}. Gestiona la lógica de
 * negocio para los asistentes, coordinando las operaciones del repositorio y
 * manejando las transacciones JPA.
 *
 * @author Eduardo Olalde
 */
public class AsistenteServiceImpl implements AsistenteService {

    private static final Logger log = LoggerFactory.getLogger(AsistenteServiceImpl.class);

    // Inyección manual de dependencias (en un entorno con CDI/Spring se usaría @Inject/@Autowired)
    private final AsistenteRepository asistenteRepository;
    private final FestivalRepository festivalRepository;

    /**
     * Constructor que inicializa los repositorios necesarios.
     */
    public AsistenteServiceImpl() {
        this.asistenteRepository = new AsistenteRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
    }

    /**
     * {@inheritDoc} Nota: Este método debería tener control de acceso (ej: solo
     * ADMIN). Actualmente no implementa paginación.
     */
    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentes() {
        log.debug("Service: Obteniendo todos los asistentes.");
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Utilizar el método findAll del repositorio si existe, sino la query directa
            List<Asistente> asistentes;
            if (asistenteRepository instanceof AsistenteRepositoryImpl) { // Asumiendo que tiene findAll
                asistentes = ((AsistenteRepositoryImpl) asistenteRepository).findAll(em);
            } else {
                // Fallback a query directa si findAll no está en la interfaz/impl
                TypedQuery<Asistente> query = em.createQuery("SELECT a FROM Asistente a ORDER BY a.nombre", Asistente.class);
                asistentes = query.getResultList();
            }

            log.info("Encontrados {} asistentes en total.", asistentes.size());
            // Mapear la lista de entidades a DTOs
            return asistentes.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo todos los asistentes: {}", e.getMessage(), e);
            return Collections.emptyList(); // Devolver lista vacía en caso de error
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AsistenteDTO> obtenerAsistentePorId(Integer idAsistente) {
        log.debug("Service: Obteniendo asistente por ID: {}", idAsistente);
        if (idAsistente == null) {
            log.warn("Intento de obtener asistente con ID nulo.");
            return Optional.empty();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Buscar en el repositorio y mapear si se encuentra
            return asistenteRepository.findById(em, idAsistente)
                    .map(this::mapEntityToDto);
        } catch (Exception e) {
            log.error("Error obteniendo asistente por ID {}: {}", idAsistente, e.getMessage(), e);
            return Optional.empty(); // Devolver vacío en caso de error
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asistente obtenerOcrearAsistentePorEmail(String email, String nombre, String telefono) {
        log.info("Service: Obteniendo o creando asistente por email: {}", email);
        // Validación inicial de argumentos
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio para obtener o crear un asistente.");
        }
        // Nota: La obligatoriedad del nombre se valida más adelante si se necesita crear

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            // 1. Intentar buscar por email (no requiere transacción activa)
            Optional<Asistente> existenteOpt = asistenteRepository.findByEmail(em, email);

            if (existenteOpt.isPresent()) {
                // 2a. Si existe, devolverlo
                Asistente existente = existenteOpt.get();
                log.debug("Asistente encontrado con email {}: ID {}", email, existente.getIdAsistente());
                return existente;
            } else {
                // 2b. Si no existe, proceder a crearlo (requiere transacción)
                log.info("Asistente con email {} no encontrado, se procederá a crear uno nuevo.", email);
                if (nombre == null || nombre.isBlank()) {
                    // Validar nombre solo si se va a crear
                    throw new IllegalArgumentException("El nombre es obligatorio al crear un nuevo asistente.");
                }

                tx = em.getTransaction(); // Iniciar transacción para la creación
                tx.begin();

                Asistente nuevoAsistente = new Asistente();
                nuevoAsistente.setEmail(email.trim().toLowerCase()); // Normalizar email
                nuevoAsistente.setNombre(nombre.trim());
                nuevoAsistente.setTelefono(telefono != null ? telefono.trim() : null); // Limpiar teléfono

                // Guardar el nuevo asistente usando el repositorio
                nuevoAsistente = asistenteRepository.save(em, nuevoAsistente);

                tx.commit(); // Confirmar la transacción
                log.info("Nuevo asistente creado con ID {} para email {}", nuevoAsistente.getIdAsistente(), email);
                return nuevoAsistente;
            }
        } catch (Exception e) {
            // Manejo de excepciones y rollback
            handleException(e, tx, "obtener o crear asistente por email");
            // Relanzar excepción mapeada o específica
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            // Podríamos mapear PersistenceException (ej: email duplicado por concurrencia) a una excepción más específica
            throw new RuntimeException("Error inesperado al obtener o crear asistente: " + e.getMessage(), e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AsistenteDTO> buscarAsistentes(String searchTerm) {
        log.debug("Service: Buscando asistentes con término: '{}'", searchTerm);
        // Si el término es vacío, podríamos devolver todos o ninguno, aquí devolvemos todos
        if (searchTerm == null || searchTerm.isBlank()) {
            log.debug("Término de búsqueda vacío, devolviendo todos los asistentes.");
            return obtenerTodosLosAsistentes();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Query JPQL para buscar por nombre o email (case-insensitive)
            String jpql = "SELECT a FROM Asistente a WHERE lower(a.nombre) LIKE :term OR lower(a.email) LIKE :term ORDER BY a.nombre";
            TypedQuery<Asistente> query = em.createQuery(jpql, Asistente.class);
            // Añadir wildcards y convertir a minúsculas para la búsqueda LIKE
            query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
            List<Asistente> asistentes = query.getResultList();
            log.info("Encontrados {} asistentes para el término '{}'", asistentes.size(), searchTerm);
            return asistentes.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error buscando asistentes con término '{}': {}", searchTerm, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AsistenteDTO actualizarAsistente(Integer idAsistente, AsistenteDTO asistenteDTO) {
        log.info("Service: Actualizando asistente ID {}", idAsistente);
        // Validación de argumentos
        if (idAsistente == null) {
            throw new IllegalArgumentException("El ID del asistente es requerido para actualizar.");
        }
        if (asistenteDTO == null) {
            throw new IllegalArgumentException("Los datos del asistente (DTO) son requeridos para actualizar.");
        }
        // Validar datos dentro del DTO
        if (asistenteDTO.getNombre() == null || asistenteDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del asistente no puede estar vacío.");
        }
        // No permitir actualizar email a través de este método
        if (asistenteDTO.getEmail() != null) {
            log.warn("Se intentó actualizar el email del asistente ID {}, lo cual no está permitido por este método.", idAsistente);
            // Opcionalmente, lanzar excepción o simplemente ignorar el campo email del DTO
            // throw new IllegalArgumentException("No se permite actualizar el email del asistente.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Buscar el asistente existente
            Asistente asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

            // 2. Actualizar los campos permitidos desde el DTO
            asistente.setNombre(asistenteDTO.getNombre().trim());
            asistente.setTelefono(asistenteDTO.getTelefono() != null ? asistenteDTO.getTelefono().trim() : null);
            // La fecha de modificación se actualiza automáticamente por la BD

            // 3. Guardar (merge) la entidad actualizada
            // save se encarga de llamar a merge si el ID no es nulo
            asistente = asistenteRepository.save(em, asistente);

            tx.commit(); // Confirmar transacción
            log.info("Asistente ID {} actualizado correctamente.", idAsistente);
            return mapEntityToDto(asistente); // Devolver DTO actualizado

        } catch (Exception e) {
            handleException(e, tx, "actualizar asistente");
            // Relanzar excepción mapeada o específica
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AsistenteDTO> obtenerAsistentesPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo asistentes para festival ID {} por promotor ID {}", idFestival, idPromotor);
        // Validación de argumentos
        if (idFestival == null) {
            throw new IllegalArgumentException("El ID del festival es requerido.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor es requerido.");
        }

        EntityManager em = null;
        // No se necesita transacción para esta lectura, pero la mantenemos por si
        // se accede a relaciones LAZY que requieran sesión activa.
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin(); // Iniciar transacción (principalmente para la verificación)

            // 1. Verificar que el festival existe y pertenece al promotor
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            // Verificar propiedad (lanza SecurityException si no coincide)
            verificarPropiedadFestival(festival, idPromotor);
            log.debug("Permiso verificado para promotor {} sobre festival {}", idPromotor, idFestival);

            // 2. Obtener los asistentes usando el método del repositorio
            List<Asistente> asistentes = asistenteRepository.findAsistentesByFestivalId(em, idFestival);

            tx.commit(); // Commit (aunque sea lectura)

            // 3. Mapear a DTOs y devolver
            log.info("Encontrados {} asistentes para el festival ID {}", asistentes.size(), idFestival);
            return asistentes.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // Manejo de excepciones y rollback si aplica
            handleException(e, tx, "obtener asistentes por festival");
            // Relanzar excepción mapeada o específica
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos de Ayuda (Helpers) ---
    /**
     * Mapea una entidad Asistente a su correspondiente AsistenteDTO.
     *
     * @param a La entidad Asistente.
     * @return El AsistenteDTO mapeado, o null si la entidad es null.
     */
    private AsistenteDTO mapEntityToDto(Asistente a) {
        if (a == null) {
            return null;
        }
        AsistenteDTO dto = new AsistenteDTO();
        dto.setIdAsistente(a.getIdAsistente());
        dto.setNombre(a.getNombre());
        dto.setEmail(a.getEmail());
        dto.setTelefono(a.getTelefono());
        dto.setFechaCreacion(a.getFechaCreacion()); // Incluir fecha creación
        return dto;
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
     * Realiza rollback de una transacción si está activa y loggea el error.
     *
     * @param tx La transacción.
     * @param action Descripción de la acción que falló.
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
     * Manejador genérico de excepciones para métodos de servicio. Loggea el
     * error y realiza rollback si hay transacción activa.
     *
     * @param e La excepción capturada.
     * @param tx La transacción activa (puede ser null).
     * @param action Descripción de la acción que falló.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action); // Intentar rollback
    }

    /**
     * Mapea excepciones técnicas a excepciones de negocio o RuntimeException.
     *
     * @param e La excepción original.
     * @return La excepción mapeada o una RuntimeException genérica.
     */
    private RuntimeException mapException(Exception e) {
        // Si ya es una de nuestras excepciones de negocio o RuntimeException, la relanzamos tal cual
        if (e instanceof AsistenteNotFoundException
                || e instanceof FestivalNotFoundException
                || e instanceof IllegalArgumentException
                || e instanceof SecurityException
                || e instanceof IllegalStateException
                || e instanceof PersistenceException
                || // Incluir PersistenceException
                e instanceof RuntimeException) { // Incluir RuntimeException genéricas
            return (RuntimeException) e;
        }
        // Para otras excepciones no esperadas, envolvemos en RuntimeException
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }

    /**
     * Verifica que el promotor dado sea el propietario del festival. Lanza
     * SecurityException si no lo es.
     *
     * @param festival El festival a verificar.
     * @param idPromotor El ID del promotor que se espera sea el propietario.
     * @throws SecurityException si el promotor no es el propietario.
     * @throws IllegalArgumentException si festival o idPromotor son nulos, o si
     * el festival no tiene promotor asociado.
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
            // Esto no debería ocurrir si se busca antes, pero por seguridad
            throw new IllegalArgumentException("El festival no puede ser nulo para verificar propiedad.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor no puede ser nulo para verificar propiedad.");
        }
        if (festival.getPromotor() == null || festival.getPromotor().getIdUsuario() == null) {
            log.error("Inconsistencia de datos: Festival ID {} no tiene un promotor asociado.", festival.getIdFestival());
            throw new IllegalStateException("El festival no tiene un promotor asociado.");
        }
        if (!festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            log.warn("Intento de acceso no autorizado por promotor ID {} al festival ID {} (propiedad de promotor ID {})",
                    idPromotor, festival.getIdFestival(), festival.getPromotor().getIdUsuario());
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
    }
}
