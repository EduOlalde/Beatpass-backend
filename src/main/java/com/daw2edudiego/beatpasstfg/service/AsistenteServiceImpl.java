package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.AsistenteDTO;
import com.daw2edudiego.beatpasstfg.exception.AsistenteNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.model.Asistente;
import com.daw2edudiego.beatpasstfg.model.EstadoEntradaAsignada;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.repository.AsistenteRepository;
import com.daw2edudiego.beatpasstfg.repository.AsistenteRepositoryImpl;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepository;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Tuple; // Importar Tuple
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap; // Usar LinkedHashMap para mantener orden
import java.util.List;
import java.util.Map; // Importar Map
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de la interfaz {@link AsistenteService}. Gestiona la lógica de
 * negocio para los asistentes, coordinando las operaciones del repositorio y
 * manejando las transacciones JPA. Incluye la obtención del mapa Festival ->
 * UID Pulsera asociada.
 *
 * @author Eduardo Olalde
 */
public class AsistenteServiceImpl implements AsistenteService {

    private static final Logger log = LoggerFactory.getLogger(AsistenteServiceImpl.class);

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
     * {@inheritDoc} Ahora recupera el mapa festival-pulsera para cada
     * asistente.
     */
    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentes() {
        log.debug("Service: Obteniendo todos los asistentes (con mapa festival-pulsera).");
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Asistente> asistentes = asistenteRepository.findAll(em); // Usa el método del repo original
            log.info("Encontrados {} asistentes en total.", asistentes.size());
            EntityManager finalEm = em; // Necesario para lambda
            return asistentes.stream()
                    .map(a -> mapEntityToDto(a, finalEm)) // Usar el método de mapeo actualizado
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo todos los asistentes: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc} Ahora recupera el mapa festival-pulsera para el asistente.
     */
    @Override
    public Optional<AsistenteDTO> obtenerAsistentePorId(Integer idAsistente) {
        log.debug("Service: Obteniendo asistente por ID (con mapa festival-pulsera): {}", idAsistente);
        if (idAsistente == null) {
            return Optional.empty();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            EntityManager finalEm = em; // Necesario para lambda
            return asistenteRepository.findById(em, idAsistente)
                    .map(a -> mapEntityToDto(a, finalEm)); // Usar el método de mapeo actualizado
        } catch (Exception e) {
            log.error("Error obteniendo asistente por ID {}: {}", idAsistente, e.getMessage(), e);
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asistente obtenerOcrearAsistentePorEmail(String email, String nombre, String telefono) {
        // (Sin cambios respecto a la versión original)
        log.info("Service: Obteniendo o creando asistente por email: {}", email);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio para obtener o crear un asistente.");
        }
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            Optional<Asistente> existenteOpt = asistenteRepository.findByEmail(em, email);
            if (existenteOpt.isPresent()) {
                return existenteOpt.get();
            } else {
                if (nombre == null || nombre.isBlank()) {
                    throw new IllegalArgumentException("El nombre es obligatorio al crear un nuevo asistente.");
                }
                tx = em.getTransaction();
                tx.begin();
                Asistente nuevoAsistente = new Asistente();
                nuevoAsistente.setEmail(email.trim().toLowerCase());
                nuevoAsistente.setNombre(nombre.trim());
                nuevoAsistente.setTelefono(telefono != null ? telefono.trim() : null);
                nuevoAsistente = asistenteRepository.save(em, nuevoAsistente);
                tx.commit();
                return nuevoAsistente;
            }
        } catch (Exception e) {
            handleException(e, tx, "obtener o crear asistente por email");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc} Ahora recupera el mapa festival-pulsera para cada asistente
     * encontrado.
     */
    @Override
    public List<AsistenteDTO> buscarAsistentes(String searchTerm) {
        log.debug("Service: Buscando asistentes con término (con mapa festival-pulsera): '{}'", searchTerm);
        if (searchTerm == null || searchTerm.isBlank()) {
            return obtenerTodosLosAsistentes();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Usamos la consulta del repositorio que proporcionaste
            String jpql = "SELECT a FROM Asistente a WHERE lower(a.nombre) LIKE :term OR lower(a.email) LIKE :term ORDER BY a.nombre";
            TypedQuery<Asistente> query = em.createQuery(jpql, Asistente.class);
            query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
            List<Asistente> asistentes = query.getResultList();
            log.info("Encontrados {} asistentes para el término '{}'", asistentes.size(), searchTerm);
            EntityManager finalEm = em; // Necesario para lambda
            return asistentes.stream()
                    .map(a -> mapEntityToDto(a, finalEm)) // Usar el método de mapeo actualizado
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error buscando asistentes con término '{}': {}", searchTerm, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc} Ahora recupera el mapa festival-pulsera para el asistente
     * actualizado.
     */
    @Override
    public AsistenteDTO actualizarAsistente(Integer idAsistente, AsistenteDTO asistenteDTO) {
        log.info("Service: Actualizando asistente ID {} (con mapa festival-pulsera)", idAsistente);
        if (idAsistente == null || asistenteDTO == null) {
            throw new IllegalArgumentException("ID y DTO del asistente son requeridos para actualizar.");
        }
        if (asistenteDTO.getNombre() == null || asistenteDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del asistente no puede estar vacío.");
        }
        if (asistenteDTO.getEmail() != null) {
            log.warn("Se intentó actualizar el email del asistente ID {}, lo cual no está permitido por este método.", idAsistente);
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Asistente asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

            asistente.setNombre(asistenteDTO.getNombre().trim());
            asistente.setTelefono(asistenteDTO.getTelefono() != null ? asistenteDTO.getTelefono().trim() : null);

            asistente = asistenteRepository.save(em, asistente);

            tx.commit();
            log.info("Asistente ID {} actualizado correctamente.", idAsistente);
            // Mapear la entidad actualizada a DTO, incluyendo el mapa festival-pulsera
            return mapEntityToDto(asistente, em); // Usar el método de mapeo actualizado

        } catch (Exception e) {
            handleException(e, tx, "actualizar asistente");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc} Recupera el UID de la pulsera asociada a cada asistente
     * *para este festival específico* y lo almacena en el mapa del DTO.
     */
    @Override
    public List<AsistenteDTO> obtenerAsistentesPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo asistentes para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival e ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        List<AsistenteDTO> resultadoDTOs = new ArrayList<>();

        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor);

            // Usamos el método del repositorio original
            List<Asistente> asistentes = asistenteRepository.findAsistentesByFestivalId(em, idFestival);
            log.info("Encontrados {} asistentes base para el festival ID {}", asistentes.size(), idFestival);

            for (Asistente asistente : asistentes) {
                // Mapeo básico inicial
                AsistenteDTO dto = new AsistenteDTO();
                dto.setIdAsistente(asistente.getIdAsistente());
                dto.setNombre(asistente.getNombre());
                dto.setEmail(asistente.getEmail());
                dto.setTelefono(asistente.getTelefono());
                dto.setFechaCreacion(asistente.getFechaCreacion());

                // Buscar el UID de la pulsera asociada a este asistente DENTRO de este festival
                String jpqlPulsera = "SELECT p.codigoUid "
                        + "FROM PulseraNFC p "
                        + "JOIN p.entradaAsignada ea "
                        + "WHERE ea.asistente = :asistente "
                        + "AND ea.compraEntrada.entrada.festival.idFestival = :idFestival "
                        // Considerar añadir: AND ea.estado = :estadoActiva AND p.activa = true
                        + "ORDER BY p.idPulsera DESC"; // Coger la última si hay varias

                TypedQuery<String> queryPulsera = em.createQuery(jpqlPulsera, String.class);
                queryPulsera.setParameter("asistente", asistente);
                queryPulsera.setParameter("idFestival", idFestival);
                // queryPulsera.setParameter("estadoActiva", EstadoEntradaAsignada.ACTIVA); // Si se añade filtro de estado
                queryPulsera.setMaxResults(1);

                Map<String, String> festivalPulseraMap = new LinkedHashMap<>(); // Crear el mapa
                try {
                    String codigoUid = queryPulsera.getSingleResult();
                    festivalPulseraMap.put(festival.getNombre(), codigoUid); // Añadir la entrada al mapa
                    log.trace("Pulsera UID {} encontrada para asistente ID {} en festival ID {}", codigoUid, asistente.getIdAsistente(), idFestival);
                } catch (NoResultException e) {
                    log.trace("No se encontró pulsera asociada para asistente ID {} en festival ID {}", asistente.getIdAsistente(), idFestival);
                    festivalPulseraMap.put(festival.getNombre(), null); // Añadir entrada con valor null
                } catch (Exception eQuery) {
                    log.error("Error buscando pulsera para asistente ID {} en festival ID {}: {}",
                            asistente.getIdAsistente(), idFestival, eQuery.getMessage());
                    festivalPulseraMap.put(festival.getNombre(), null); // Añadir entrada con valor null en caso de error
                }
                dto.setFestivalPulseraInfo(festivalPulseraMap); // Establecer el mapa en el DTO
                resultadoDTOs.add(dto);
            }

            tx.commit();
            return resultadoDTOs;

        } catch (Exception e) {
            handleException(e, tx, "obtener asistentes por festival ID " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos de Ayuda (Helpers) ---
    /**
     * Mapea una entidad Asistente a su correspondiente AsistenteDTO, incluyendo
     * la consulta para obtener el mapa de festivales y UIDs de pulseras
     * asociadas.
     *
     * @param a La entidad {@link Asistente} a mapear. No debe ser {@code null}.
     * @param em El {@link EntityManager} activo para realizar la consulta
     * adicional. No debe ser {@code null}.
     * @return El {@link AsistenteDTO} mapeado, incluyendo el mapa
     * {@code festivalPulseraInfo}. Retorna {@code null} si la entidad de
     * entrada es {@code null}. El mapa puede estar vacío si no se encuentran
     * festivales/pulseras o si ocurre un error durante la consulta.
     */
    private AsistenteDTO mapEntityToDto(Asistente a, EntityManager em) {
        if (a == null) {
            return null;
        }
        // Mapeo básico
        AsistenteDTO dto = new AsistenteDTO();
        dto.setIdAsistente(a.getIdAsistente());
        dto.setNombre(a.getNombre());
        dto.setEmail(a.getEmail());
        dto.setTelefono(a.getTelefono());
        dto.setFechaCreacion(a.getFechaCreacion());

        // *** Consulta para obtener mapa Festival -> Pulsera UID ***
        Map<String, String> festivalPulseraMap = new LinkedHashMap<>(); // Usar LinkedHashMap para mantener orden
        if (em != null && a.getIdAsistente() != null) { // Asegurarse de tener EM y ID
            try {
                // Consulta que devuelve pares (nombreFestival, codigoUidPulsera)
                // Usa LEFT JOIN para incluir festivales aunque el asistente no tenga pulsera asociada
                // Filtra por entradas ACTIVAS del asistente
                String jpql = "SELECT e.festival.nombre, p.codigoUid "
                        + "FROM EntradaAsignada ea "
                        + "JOIN ea.compraEntrada ce "
                        + "JOIN ce.entrada e "
                        + "LEFT JOIN ea.pulseraAsociada p " // LEFT JOIN para pulsera opcional
                        + "WHERE ea.asistente = :asistente "
                        + "AND ea.estado = :estadoActiva " // Considerar solo entradas activas
                        + "ORDER BY e.festival.nombre"; // Ordenar alfabéticamente por festival

                TypedQuery<Tuple> query = em.createQuery(jpql, Tuple.class);
                query.setParameter("asistente", a); // Pasar la entidad completa
                query.setParameter("estadoActiva", EstadoEntradaAsignada.ACTIVA);

                List<Tuple> results = query.getResultList();

                // Poblar el mapa
                for (Tuple tuple : results) {
                    String festivalNombre = tuple.get(0, String.class);
                    String pulseraUid = tuple.get(1, String.class); // Será null si no hay pulsera en el LEFT JOIN
                    // Si ya existe una entrada para este festival (poco probable si la lógica es correcta),
                    // esta implementación sobrescribirá con la última encontrada.
                    festivalPulseraMap.put(festivalNombre, pulseraUid);
                }
                log.trace("Mapa Festival-Pulsera obtenido para asistente ID {}: {}", a.getIdAsistente(), festivalPulseraMap);

            } catch (Exception e) {
                log.error("Error al obtener mapa festival-pulsera para asistente ID {}: {}", a.getIdAsistente(), e.getMessage());
                // Dejar el mapa vacío en caso de error en la consulta
                festivalPulseraMap = Collections.emptyMap();
            }
        } else {
            // Si no hay EntityManager o ID, devolver mapa vacío
            festivalPulseraMap = Collections.emptyMap();
        }
        dto.setFestivalPulseraInfo(festivalPulseraMap); // Establecer el mapa (posiblemente vacío)
        // *** Fin Consulta Mapa ***

        return dto;
    }

    // Métodos auxiliares
    
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
     * Manejador genérico de excepciones para métodos de servicio.
     *
     * @param e La excepción capturada.
     * @param tx La transacción activa (puede ser null).
     * @param action Descripción de la acción que falló.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    /**
     * Mapea excepciones técnicas a excepciones de negocio o RuntimeException.
     *
     * @param e La excepción original.
     * @return La excepción mapeada o una RuntimeException genérica.
     */
    private RuntimeException mapException(Exception e) {
        if (e instanceof AsistenteNotFoundException
                || e instanceof FestivalNotFoundException
                || e instanceof IllegalArgumentException
                || e instanceof SecurityException
                || e instanceof IllegalStateException
                || e instanceof PersistenceException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }

    /**
     * Verifica que el promotor dado sea el propietario del festival.
     *
     * @param festival El festival a verificar.
     * @param idPromotor El ID del promotor que se espera sea el propietario.
     * @throws SecurityException si el promotor no es el propietario.
     * @throws IllegalArgumentException si festival o idPromotor son nulos, o si
     * el festival no tiene promotor asociado.
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
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
