package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.AsistenteDTO;
import com.daw2edudiego.beatpasstfg.exception.AsistenteNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.model.Asistente;
import com.daw2edudiego.beatpasstfg.model.EstadoEntrada;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.repository.AsistenteRepository;
import com.daw2edudiego.beatpasstfg.repository.AsistenteRepositoryImpl;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepository;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de AsistenteService.
 */
public class AsistenteServiceImpl implements AsistenteService {

    private static final Logger log = LoggerFactory.getLogger(AsistenteServiceImpl.class);

    private final AsistenteRepository asistenteRepository;
    private final FestivalRepository festivalRepository;

    public AsistenteServiceImpl() {
        this.asistenteRepository = new AsistenteRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
    }

    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentes() {
        log.debug("Service: Obteniendo todos los asistentes.");
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Asistente> asistentes = asistenteRepository.findAll(em);
            log.info("Encontrados {} asistentes en total.", asistentes.size());
            // CORRECCIÓN: Usar una variable final para el EntityManager en la lambda.
            final EntityManager finalEm = em;
            return asistentes.stream()
                    .map(a -> mapEntityToDto(a, finalEm))
                    .collect(Collectors.toList());
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
            // CORRECCIÓN: Usar una variable final para el EntityManager en la lambda.
            final EntityManager finalEm = em;
            return asistenteRepository.findById(em, idAsistente)
                    .map(a -> mapEntityToDto(a, finalEm));
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
            Optional<Asistente> existenteOpt = asistenteRepository.findByEmail(em, email);

            if (existenteOpt.isPresent()) {
                log.debug("Asistente encontrado con email {}", email);
                return existenteOpt.get();
            } else {
                log.info("Asistente con email {} no encontrado, creando uno nuevo.", email);
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
                log.info("Nuevo asistente creado con ID {}", nuevoAsistente.getIdAsistente());
                return nuevoAsistente;
            }
        } catch (Exception e) {
            handleException(e, tx, "obtener o crear asistente por email " + email);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<AsistenteDTO> buscarAsistentes(String searchTerm) {
        log.debug("Service: Buscando asistentes con término: '{}'", searchTerm);
        if (searchTerm == null || searchTerm.isBlank()) {
            return obtenerTodosLosAsistentes();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            String jpql = "SELECT a FROM Asistente a WHERE lower(a.nombre) LIKE :term OR lower(a.email) LIKE :term ORDER BY a.nombre";
            TypedQuery<Asistente> query = em.createQuery(jpql, Asistente.class);
            query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
            List<Asistente> asistentes = query.getResultList();
            log.info("Encontrados {} asistentes para el término '{}'", asistentes.size(), searchTerm);
            // CORRECCIÓN: Usar una variable final para el EntityManager en la lambda.
            final EntityManager finalEm = em;
            return asistentes.stream()
                    .map(a -> mapEntityToDto(a, finalEm))
                    .collect(Collectors.toList());
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
            throw new IllegalArgumentException("ID y DTO del asistente son requeridos para actualizar.");
        }
        if (asistenteDTO.getNombre() == null || asistenteDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del asistente no puede estar vacío.");
        }
        if (asistenteDTO.getEmail() != null) {
            log.warn("Se ignorará el intento de actualizar el email del asistente ID {}.", idAsistente);
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

            return mapEntityToDto(asistente, em);

        } catch (Exception e) {
            handleException(e, tx, "actualizar asistente ID " + idAsistente);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

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

            List<Asistente> asistentes = asistenteRepository.findAsistentesByFestivalId(em, idFestival);
            log.info("Encontrados {} asistentes base para el festival ID {}", asistentes.size(), idFestival);

            for (Asistente asistente : asistentes) {
                AsistenteDTO dto = new AsistenteDTO();
                dto.setIdAsistente(asistente.getIdAsistente());
                dto.setNombre(asistente.getNombre());
                dto.setEmail(asistente.getEmail());
                dto.setTelefono(asistente.getTelefono());
                dto.setFechaCreacion(asistente.getFechaCreacion());

                String jpqlPulsera = "SELECT p.codigoUid FROM PulseraNFC p JOIN p.entrada ea "
                        + "WHERE ea.asistente = :asistente AND ea.compraEntrada.tipoEntrada.festival = :festival "
                        + "ORDER BY p.idPulsera DESC";

                TypedQuery<String> queryPulsera = em.createQuery(jpqlPulsera, String.class);
                queryPulsera.setParameter("asistente", asistente);
                queryPulsera.setParameter("festival", festival);
                queryPulsera.setMaxResults(1);

                Map<String, String> festivalPulseraMap = new LinkedHashMap<>();
                try {
                    String codigoUid = queryPulsera.getSingleResult();
                    festivalPulseraMap.put(festival.getNombre(), codigoUid);
                } catch (NoResultException e) {
                    festivalPulseraMap.put(festival.getNombre(), null);
                }
                dto.setFestivalPulseraInfo(festivalPulseraMap);
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

    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentesConFiltro(String searchTerm) {
        log.debug("Service: Obteniendo todos los asistentes con filtro: '{}'", searchTerm);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Asistente> asistentes;
            if (searchTerm == null || searchTerm.isBlank()) {
                asistentes = asistenteRepository.findAll(em);
            } else {
                TypedQuery<Asistente> query = em.createQuery(
                        "SELECT a FROM Asistente a WHERE lower(a.nombre) LIKE :term OR lower(a.email) LIKE :term ORDER BY a.nombre", Asistente.class);
                query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
                asistentes = query.getResultList();
            }

            log.info("Encontrados {} asistentes para el término '{}'", asistentes.size(), searchTerm);
            // CORRECCIÓN: Usar una variable final para el EntityManager en la lambda.
            final EntityManager finalEm = em;
            return asistentes.stream()
                    .map(a -> mapEntityToDto(a, finalEm))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error buscando asistentes con término '{}': {}", searchTerm, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda ---
    private AsistenteDTO mapEntityToDto(Asistente a, EntityManager em) {
        if (a == null) {
            return null;
        }
        AsistenteDTO dto = new AsistenteDTO();
        dto.setIdAsistente(a.getIdAsistente());
        dto.setNombre(a.getNombre());
        dto.setEmail(a.getEmail());
        dto.setTelefono(a.getTelefono());
        dto.setFechaCreacion(a.getFechaCreacion());

        Map<String, String> festivalPulseraMap = new LinkedHashMap<>();
        if (em != null && a.getIdAsistente() != null) {
            try {
                String jpql = "SELECT e.festival.nombre, p.codigoUid "
                        + "FROM Entrada ea JOIN ea.compraEntrada ce JOIN ce.tipoEntrada e LEFT JOIN ea.pulseraAsociada p "
                        + "WHERE ea.asistente = :asistente AND ea.estado = :estadoActiva "
                        + "ORDER BY e.festival.nombre";
                TypedQuery<Tuple> query = em.createQuery(jpql, Tuple.class);
                query.setParameter("asistente", a);
                query.setParameter("estadoActiva", EstadoEntrada.ACTIVA);
                List<Tuple> results = query.getResultList();
                for (Tuple tuple : results) {
                    festivalPulseraMap.put(tuple.get(0, String.class), tuple.get(1, String.class));
                }
            } catch (Exception e) {
                log.error("Error al obtener mapa festival-pulsera para asistente ID {}: {}", a.getIdAsistente(), e.getMessage());
            }
        }
        dto.setFestivalPulseraInfo(festivalPulseraMap);
        return dto;
    }

    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

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

    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    private RuntimeException mapException(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }

    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null || idPromotor == null) {
            throw new IllegalArgumentException("Festival e ID Promotor no pueden ser nulos.");
        }
        if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
    }
}
