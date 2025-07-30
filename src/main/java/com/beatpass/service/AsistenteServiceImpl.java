package com.beatpass.service;

import com.beatpass.dto.AsistenteDTO;
import com.beatpass.dto.AsistenteUpdateDTO;
import com.beatpass.exception.AsistenteNotFoundException;
import com.beatpass.mapper.AsistenteMapper;
import com.beatpass.model.Asistente;
import com.beatpass.repository.AsistenteRepository;
import com.beatpass.util.PermissionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación del servicio para la gestión de Asistentes, corregida para
 * mantener la lógica de consulta original mientras se adopta CDI y JTA.
 */
@ApplicationScoped
public class AsistenteServiceImpl implements AsistenteService {

    private static final Logger log = LoggerFactory.getLogger(AsistenteServiceImpl.class);

    // Inyectamos el EntityManager para mantener la lógica original de búsqueda
    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;

    @Inject
    private AsistenteRepository asistenteRepository;

    @Inject
    private AsistenteMapper asistenteMapper;

    @Inject
    private PermissionService permissionService;

    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentes() {
        log.debug("Service: Obteniendo todos los asistentes.");
        List<Asistente> asistentes = asistenteRepository.findAll();
        log.info("Encontrados {} asistentes en total.", asistentes.size());
        return asistenteMapper.toAsistenteDTOList(asistentes);
    }

    @Override
    public Optional<AsistenteDTO> obtenerAsistentePorId(Integer idAsistente) {
        if (idAsistente == null) {
            return Optional.empty();
        }
        log.debug("Service: Buscando asistente por ID: {}", idAsistente);
        return asistenteRepository.findById(idAsistente).map(asistenteMapper::asistenteToAsistenteDTO);
    }

    @Override
    @Transactional
    public Asistente obtenerOcrearAsistentePorEmail(String email, String nombre, String telefono) {
        log.info("Service: Obteniendo o creando asistente por email: {}", email);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio para obtener o crear un asistente.");
        }

        Optional<Asistente> existenteOpt = asistenteRepository.findByEmail(email);
        if (existenteOpt.isPresent()) {
            return existenteOpt.get();
        }

        log.debug("Asistente con email {} no encontrado, creando uno nuevo.", email);
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio al crear un nuevo asistente.");
        }
        Asistente nuevoAsistente = new Asistente();
        nuevoAsistente.setEmail(email.trim().toLowerCase());
        nuevoAsistente.setNombre(nombre.trim());
        nuevoAsistente.setTelefono(telefono != null ? telefono.trim() : null);
        return asistenteRepository.save(nuevoAsistente);
    }

    @Override
    public List<AsistenteDTO> buscarAsistentes(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return obtenerTodosLosAsistentes();
        }
        log.debug("Service: Buscando asistentes con el término: '{}'", searchTerm);
        List<Asistente> asistentes = asistenteRepository.searchByTerm(searchTerm);

        return asistenteMapper.toAsistenteDTOList(asistentes);
    }

    @Override
    @Transactional
    public AsistenteDTO actualizarAsistente(Integer idAsistente, AsistenteUpdateDTO asistenteUpdateDTO) {
        log.info("Service: Actualizando asistente ID {}", idAsistente);
        if (idAsistente == null || asistenteUpdateDTO == null) {
            throw new IllegalArgumentException("ID y DTO del asistente son requeridos para actualizar.");
        }

        Asistente asistente = asistenteRepository.findById(idAsistente)
                .orElseThrow(() -> new AsistenteNotFoundException("Asistente no encontrado con ID: " + idAsistente));

        asistente.setNombre(asistenteUpdateDTO.getNombre());
        asistente.setTelefono(asistenteUpdateDTO.getTelefono());

        asistente = asistenteRepository.save(asistente);
        return asistenteMapper.asistenteToAsistenteDTO(asistente);
    }

    @Override
    public List<AsistenteDTO> obtenerAsistentesPorFestival(Integer idFestival, Integer idActor) {
        log.debug("Service: Obteniendo asistentes para festival ID {} por actor ID {}", idFestival, idActor);
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID de festival e ID de actor son requeridos.");
        }

        permissionService.verificarPermisoSobreFestival(idFestival, idActor);

        List<Object[]> resultados = asistenteRepository.findAsistenteDetailsByFestivalId(idFestival);

        Map<Integer, AsistenteDTO> asistentesMap = new LinkedHashMap<>();
        for (Object[] row : resultados) {
            Integer idAsistente = (Integer) row[0];
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
            String nombreFestival = (String) row[5];
            String codigoUidPulsera = (String) row[6];
            dto.getFestivalPulseraInfo().put(nombreFestival, codigoUidPulsera);
        }

        log.info("Encontrados {} asistentes únicos para el festival ID {}", asistentesMap.size(), idFestival);
        return new ArrayList<>(asistentesMap.values());
    }

    @Override
    public List<AsistenteDTO> obtenerTodosLosAsistentesConFiltro(String searchTerm) {
        return buscarAsistentes(searchTerm);
    }
}
