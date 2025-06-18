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
import com.daw2edudiego.beatpasstfg.mapper.AsistenteMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de AsistenteService.
 */
public class AsistenteServiceImpl extends AbstractService implements AsistenteService {

    private static final Logger log = LoggerFactory.getLogger(AsistenteServiceImpl.class);

    private final AsistenteRepository asistenteRepository;
    private final FestivalRepository festivalRepository;
    private final AsistenteMapper asistenteMapper; //

    public AsistenteServiceImpl() {
        this.asistenteRepository = new AsistenteRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.asistenteMapper = AsistenteMapper.INSTANCE;
    }

    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentes() {
        log.debug("Service: Obteniendo todos los asistentes.");
        return executeRead(em -> {
            List<Asistente> asistentes = asistenteRepository.findAll(em);
            log.info("Encontrados {} asistentes en total.", asistentes.size());
            return asistentes.stream()
                    .map(a -> mapEntityToDtoWithPulseraInfo(a, em))
                    .collect(Collectors.toList());
        }, "obtenerTodosLosAsistentes");
    }

    @Override
    public Optional<AsistenteDTO> obtenerAsistentePorId(Integer idAsistente) {
        log.debug("Service: Obteniendo asistente por ID: {}", idAsistente);
        if (idAsistente == null) {
            return Optional.empty();
        }
        return executeRead(em -> {
            return asistenteRepository.findById(em, idAsistente)
                    .map(a -> mapEntityToDtoWithPulseraInfo(a, em));
        }, "obtenerAsistentePorId " + idAsistente);
    }

    @Override
    public Asistente obtenerOcrearAsistentePorEmail(String email, String nombre, String telefono) {
        log.info("Service: Obteniendo o creando asistente por email: {}", email);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio para obtener o crear un asistente.");
        }

        EntityManager emLookup = null;
        try {
            emLookup = JPAUtil.createEntityManager();
            Optional<Asistente> existenteOpt = asistenteRepository.findByEmail(emLookup, email);
            if (existenteOpt.isPresent()) {
                log.debug("Asistente encontrado con email {}", email);
                return existenteOpt.get();
            }
        } finally {
            if (emLookup != null && emLookup.isOpen()) {
                emLookup.close();
            }
        }

        log.info("Asistente con email {} no encontrado, creando uno nuevo.", email);
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio al crear un nuevo asistente.");
        }

        return executeTransactional(em -> {
            Asistente nuevoAsistente = new Asistente();
            nuevoAsistente.setEmail(email.trim().toLowerCase());
            nuevoAsistente.setNombre(nombre.trim());
            nuevoAsistente.setTelefono(telefono != null ? telefono.trim() : null);
            nuevoAsistente = asistenteRepository.save(em, nuevoAsistente);
            log.info("Nuevo asistente creado con ID {}", nuevoAsistente.getIdAsistente());
            return nuevoAsistente;
        }, "crearAsistentePorEmail " + email);
    }

    @Override
    public List<AsistenteDTO> buscarAsistentes(String searchTerm) {
        log.debug("Service: Buscando asistentes con término: '{}'", searchTerm);
        if (searchTerm == null || searchTerm.isBlank()) {
            return obtenerTodosLosAsistentes();
        }
        return executeRead(em -> {
            String jpql = "SELECT a FROM Asistente a WHERE lower(a.nombre) LIKE :term OR lower(a.email) LIKE :term ORDER BY a.nombre";
            TypedQuery<Asistente> query = em.createQuery(jpql, Asistente.class);
            query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
            List<Asistente> asistentes = query.getResultList();
            log.info("Encontrados {} asistentes para el término '{}'", asistentes.size(), searchTerm);
            return asistentes.stream()
                    .map(a -> mapEntityToDtoWithPulseraInfo(a, em))
                    .collect(Collectors.toList());
        }, "buscarAsistentes " + searchTerm);
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

        return executeTransactional(em -> {
            Asistente asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

            asistenteMapper.updateAsistenteFromDto(asistenteDTO, asistente);

            asistente = asistenteRepository.save(em, asistente);
            log.info("Asistente ID {} actualizado correctamente.", idAsistente);
            return mapEntityToDtoWithPulseraInfo(asistente, em);
        }, "actualizarAsistente " + idAsistente);
    }

    @Override
    public List<AsistenteDTO> obtenerAsistentesPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo asistentes para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival e ID de promotor son requeridos.");
        }

        return executeRead(em -> {
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor);

            List<Asistente> asistentes = asistenteRepository.findAsistentesByFestivalId(em, idFestival);
            log.info("Encontrados {} asistentes únicos para el festival ID {}", asistentes.size(), idFestival);

            List<AsistenteDTO> resultadoDTOs = new ArrayList<>();
            for (Asistente asistente : asistentes) {
                AsistenteDTO dto = asistenteMapper.asistenteToAsistenteDTO(asistente);

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
            return resultadoDTOs;
        }, "obtenerAsistentesPorFestival " + idFestival);
    }

    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentesConFiltro(String searchTerm) {
        log.debug("Service: Obteniendo todos los asistentes con filtro: '{}'", searchTerm);
        return executeRead(em -> {
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
            return asistentes.stream()
                    .map(a -> mapEntityToDtoWithPulseraInfo(a, em))
                    .collect(Collectors.toList());
        }, "obtenerTodosLosAsistentesConFiltro " + searchTerm);
    }

    // --- Métodos Privados de Ayuda ---
    /**
     * Método auxiliar para el mapeo de `festivalPulseraInfo`, que requiere una
     * consulta JPA adicional que MapStruct no puede generar automáticamente en
     * el mapeo directo de la entidad.
     */
    private AsistenteDTO mapEntityToDtoWithPulseraInfo(Asistente a, EntityManager em) {
        if (a == null) {
            return null;
        }
        AsistenteDTO dto = asistenteMapper.asistenteToAsistenteDTO(a);

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

    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null || idPromotor == null) {
            throw new IllegalArgumentException("Festival e ID Promotor no pueden ser nulos.");
        }
        if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
    }
}
