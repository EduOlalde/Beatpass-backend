package com.beatpass.service;

import com.beatpass.dto.AsistenteDTO;
import com.beatpass.exception.AsistenteNotFoundException;
import com.beatpass.mapper.AsistenteMapper;
import com.beatpass.model.Asistente;
import com.beatpass.repository.AsistenteRepository;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación del servicio para la gestión de Asistentes. Gestiona la
 * creación, consulta y actualización de los datos de los asistentes.
 */
public class AsistenteServiceImpl extends AbstractService implements AsistenteService {

    private static final Logger log = LoggerFactory.getLogger(AsistenteServiceImpl.class);

    private final AsistenteRepository asistenteRepository;
    private final AsistenteMapper asistenteMapper;

    @Inject
    public AsistenteServiceImpl(AsistenteRepository asistenteRepository) {
        this.asistenteRepository = asistenteRepository;
        this.asistenteMapper = AsistenteMapper.INSTANCE;
    }

    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentes() {
        log.debug("Service: Obteniendo todos los asistentes.");
        return executeRead(em -> {
            List<Asistente> asistentes = asistenteRepository.findAll(em);
            log.info("Encontrados {} asistentes en total.", asistentes.size());
            // Nota: Esta vista general no carga la información detallada de festivales para mantenerla ligera.
            return asistenteMapper.toAsistenteDTOList(asistentes);
        }, "obtenerTodosLosAsistentes");
    }

    @Override
    public Optional<AsistenteDTO> obtenerAsistentePorId(Integer idAsistente) {
        if (idAsistente == null) {
            return Optional.empty();
        }
        return executeRead(em
                -> asistenteRepository.findById(em, idAsistente).map(asistenteMapper::asistenteToAsistenteDTO),
                "obtenerAsistentePorId " + idAsistente
        );
    }

    @Override
    public Asistente obtenerOcrearAsistentePorEmail(String email, String nombre, String telefono) {
        log.info("Service: Obteniendo o creando asistente por email: {}", email);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio para obtener o crear un asistente.");
        }

        return executeTransactional(em -> {
            Optional<Asistente> existenteOpt = asistenteRepository.findByEmail(em, email);
            if (existenteOpt.isPresent()) {
                return existenteOpt.get();
            }

            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre es obligatorio al crear un nuevo asistente.");
            }
            Asistente nuevoAsistente = new Asistente();
            nuevoAsistente.setEmail(email.trim().toLowerCase());
            nuevoAsistente.setNombre(nombre.trim());
            nuevoAsistente.setTelefono(telefono != null ? telefono.trim() : null);
            return asistenteRepository.save(em, nuevoAsistente);
        }, "obtenerOcrearAsistentePorEmail " + email);
    }

    @Override
    public List<AsistenteDTO> buscarAsistentes(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return obtenerTodosLosAsistentes();
        }
        return executeRead(em -> {
            String jpql = "SELECT a FROM Asistente a WHERE lower(a.nombre) LIKE :term OR lower(a.email) LIKE :term ORDER BY a.nombre";
            TypedQuery<Asistente> query = em.createQuery(jpql, Asistente.class);
            query.setParameter("term", "%" + searchTerm.toLowerCase() + "%");
            List<Asistente> asistentes = query.getResultList();
            return asistenteMapper.toAsistenteDTOList(asistentes);
        }, "buscarAsistentes " + searchTerm);
    }

    @Override
    public AsistenteDTO actualizarAsistente(Integer idAsistente, AsistenteDTO asistenteDTO) {
        log.info("Service: Actualizando asistente ID {}", idAsistente);
        if (idAsistente == null || asistenteDTO == null) {
            throw new IllegalArgumentException("ID y DTO del asistente son requeridos para actualizar.");
        }

        return executeTransactional(em -> {
            Asistente asistente = asistenteRepository.findById(em, idAsistente)
                    .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

            asistenteMapper.updateAsistenteFromDto(asistenteDTO, asistente);
            asistente = asistenteRepository.save(em, asistente);
            return asistenteMapper.asistenteToAsistenteDTO(asistente);
        }, "actualizarAsistente " + idAsistente);
    }

    @Override
    public List<AsistenteDTO> obtenerAsistentesPorFestival(Integer idFestival, Integer idActor) {
        log.debug("Service: Obteniendo asistentes para festival ID {} por actor ID {}", idFestival, idActor);
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID de festival e ID de actor son requeridos.");
        }

        return executeRead(em -> {
            verificarPermisoSobreFestival(em, idFestival, idActor);

            // 1. Ejecutar la consulta optimizada que devuelve datos planos
            List<Object[]> resultados = asistenteRepository.findAsistenteDetailsByFestivalId(em, idFestival);

            // 2. Procesar y agrupar los resultados para construir los DTOs finales
            Map<Integer, AsistenteDTO> asistentesMap = new LinkedHashMap<>();
            for (Object[] row : resultados) {
                Integer idAsistente = (Integer) row[0];

                // Crea el AsistenteDTO solo la primera vez que se encuentra su ID
                AsistenteDTO dto = asistentesMap.computeIfAbsent(idAsistente, k -> {
                    AsistenteDTO nuevoDto = new AsistenteDTO();
                    nuevoDto.setIdAsistente((Integer) row[0]);
                    nuevoDto.setNombre((String) row[1]);
                    nuevoDto.setEmail((String) row[2]);
                    nuevoDto.setTelefono((String) row[3]);
                    nuevoDto.setFechaCreacion((LocalDateTime) row[4]);
                    nuevoDto.setFestivalPulseraInfo(new LinkedHashMap<>());
                    return nuevoDto;
                });

                // Añade la información del festival y la pulsera al mapa del asistente
                String nombreFestival = (String) row[5];
                String codigoUidPulsera = (String) row[6];
                dto.getFestivalPulseraInfo().put(nombreFestival, codigoUidPulsera);
            }

            log.info("Encontrados {} asistentes únicos para el festival ID {}", asistentesMap.size(), idFestival);
            return new ArrayList<>(asistentesMap.values());
        }, "obtenerAsistentesPorFestival (optimizado) " + idFestival);
    }

    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentesConFiltro(String searchTerm) {
        // Este método mantiene una implementación más simple por ahora,
        // ya que la optimización principal se enfoca en la vista por festival.
        return buscarAsistentes(searchTerm);
    }
}
