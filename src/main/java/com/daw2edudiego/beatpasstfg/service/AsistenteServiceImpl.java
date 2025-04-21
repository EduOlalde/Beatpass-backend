package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.AsistenteDTO;
import com.daw2edudiego.beatpasstfg.exception.AsistenteNotFoundException; // Importar
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
import jakarta.persistence.TypedQuery; // Para búsqueda por término
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de AsistenteService.
 */
public class AsistenteServiceImpl implements AsistenteService {

    private static final Logger log = LoggerFactory.getLogger(AsistenteServiceImpl.class);

    private final AsistenteRepository asistenteRepository;
    private final FestivalRepository festivalRepository; // <-- Añadido

    public AsistenteServiceImpl() {
        this.asistenteRepository = new AsistenteRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl(); // <-- Instanciar
    }

    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentes() {
        log.debug("Service: Obteniendo todos los asistentes.");
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Necesitamos un método findAll en el repositorio
            // Por ahora, simulamos con una query aquí (mejor en repo)
            TypedQuery<Asistente> query = em.createQuery("SELECT a FROM Asistente a ORDER BY a.nombre", Asistente.class);
            List<Asistente> asistentes = query.getResultList();
            log.info("Encontrados {} asistentes.", asistentes.size());
            return asistentes.stream().map(this::mapEntityToDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo todos los asistentes: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<AsistenteDTO> obtenerAsistentePorId(Integer idAsistente) {
        log.debug("Service: Obteniendo asistente por ID: {}", idAsistente);
        if (idAsistente == null) {
            return Optional.empty();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            return asistenteRepository.findById(em, idAsistente).map(this::mapEntityToDto);
        } catch (Exception e) {
            log.error("Error obteniendo asistente por ID {}: {}", idAsistente, e.getMessage(), e);
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Asistente obtenerOcrearAsistentePorEmail(String email, String nombre, String telefono) {
        log.info("Service: Obteniendo o creando asistente por email: {}", email);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio para obtener o crear un asistente.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            // Intentar buscar por email primero (sin transacción activa necesaria)
            Optional<Asistente> existenteOpt = asistenteRepository.findByEmail(em, email);

            if (existenteOpt.isPresent()) {
                log.debug("Asistente encontrado con email {}: ID {}", email, existenteOpt.get().getIdAsistente());
                return existenteOpt.get(); // Devolver asistente existente
            } else {
                // No existe, intentar crear uno nuevo
                log.info("Asistente con email {} no encontrado, creando uno nuevo.", email);
                if (nombre == null || nombre.isBlank()) {
                    throw new IllegalArgumentException("El nombre es obligatorio al crear un nuevo asistente.");
                }

                tx = em.getTransaction(); // Necesitamos transacción para persistir
                tx.begin();

                Asistente nuevoAsistente = new Asistente();
                nuevoAsistente.setEmail(email);
                nuevoAsistente.setNombre(nombre);
                nuevoAsistente.setTelefono(telefono); // Puede ser null

                nuevoAsistente = asistenteRepository.save(em, nuevoAsistente); // Persistir

                tx.commit();
                log.info("Nuevo asistente creado con ID {} para email {}", nuevoAsistente.getIdAsistente(), email);
                return nuevoAsistente;
            }
        } catch (Exception e) {
            log.error("Error obteniendo o creando asistente por email {}: {}", email, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    log.error("Error en rollback: {}", rbEx.getMessage());
                }
            }
            // Relanzar o mapear excepción
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new RuntimeException("Error inesperado al obtener o crear asistente: " + e.getMessage(), e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<AsistenteDTO> buscarAsistentes(String searchTerm) {
        log.debug("Service: Buscando asistentes con término: '{}'", searchTerm);
        if (searchTerm == null || searchTerm.isBlank()) {
            return obtenerTodosLosAsistentes(); // O devolver lista vacía
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            String jpql = "SELECT a FROM Asistente a WHERE lower(a.nombre) LIKE :term OR lower(a.email) LIKE :term ORDER BY a.nombre";
            TypedQuery<Asistente> query = em.createQuery(jpql, Asistente.class);
            query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
            List<Asistente> asistentes = query.getResultList();
            log.info("Encontrados {} asistentes para el término '{}'", asistentes.size(), searchTerm);
            return asistentes.stream().map(this::mapEntityToDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error buscando asistentes con término '{}': {}", searchTerm, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public AsistenteDTO actualizarAsistente(Integer idAsistente, AsistenteDTO asistenteDTO) {
        log.info("Service: Actualizando asistente ID {}", idAsistente);
        if (idAsistente == null || asistenteDTO == null) {
            throw new IllegalArgumentException("ID de asistente y DTO son requeridos.");
        }
        // Validar datos básicos del DTO (ej: nombre no vacío)
        if (asistenteDTO.getNombre() == null || asistenteDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del asistente no puede estar vacío.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Buscar el asistente existente
            Asistente asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

            // Actualizar solo los campos permitidos (nombre, teléfono)
            // NO actualizar email (suele ser identificador único) ni fechaCreacion
            asistente.setNombre(asistenteDTO.getNombre());
            asistente.setTelefono(asistenteDTO.getTelefono());
            // fechaModificacion se actualiza automáticamente por la BD

            // Guardar (merge)
            asistente = asistenteRepository.save(em, asistente);

            tx.commit();
            log.info("Asistente ID {} actualizado correctamente.", idAsistente);
            return mapEntityToDto(asistente);

        } catch (Exception e) {
            log.error("Error actualizando asistente ID {}: {}", idAsistente, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    log.error("Error rollback: {}", rbEx.getMessage());
                }
            }
            if (e instanceof AsistenteNotFoundException || e instanceof IllegalArgumentException || e instanceof PersistenceException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Error inesperado actualizando asistente: " + e.getMessage(), e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<AsistenteDTO> obtenerAsistentesPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo asistentes para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival y ID de promotor son requeridos.");
        }

        EntityManager em = null;
        // No se necesita tx para lectura, pero la usamos por consistencia y posible lazy loading futuro
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar que el festival existe y pertenece al promotor
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
                log.warn("Intento no autorizado por promotor ID {} para ver asistentes de festival ID {}", idPromotor, idFestival);
                throw new SecurityException("No tiene permiso para ver los asistentes de este festival.");
            }
            log.debug("Permiso verificado para promotor {} sobre festival {}", idPromotor, idFestival);

            // 2. Obtener los asistentes usando el método del repositorio
            List<Asistente> asistentes = asistenteRepository.findAsistentesByFestivalId(em, idFestival);

            tx.commit(); // Commit (aunque sea lectura)

            // 3. Mapear a DTOs
            return asistentes.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error obteniendo asistentes para festival ID {}: {}", idFestival, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    log.error("Error rollback: {}", rbEx.getMessage());
                }
            }
            if (e instanceof FestivalNotFoundException || e instanceof SecurityException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Error inesperado obteniendo asistentes: " + e.getMessage(), e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Helpers ---
    private AsistenteDTO mapEntityToDto(Asistente a) {
        if (a == null) {
            return null;
        }
        AsistenteDTO dto = new AsistenteDTO();
        dto.setIdAsistente(a.getIdAsistente());
        dto.setNombre(a.getNombre());
        dto.setEmail(a.getEmail());
        dto.setTelefono(a.getTelefono());
        dto.setFechaCreacion(a.getFechaCreacion());
        return dto;
    }

    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
