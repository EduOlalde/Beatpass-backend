package com.beatpass.service;

import com.beatpass.dto.FestivalDTO;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.UsuarioNotFoundException;
import com.beatpass.mapper.FestivalMapper;
import com.beatpass.model.EstadoFestival;
import com.beatpass.model.Festival;
import com.beatpass.model.RolUsuario;
import com.beatpass.model.Usuario;
import com.beatpass.repository.FestivalRepository;
import com.beatpass.repository.UsuarioRepository;
import com.beatpass.util.PermissionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de festivales, refactorizada para
 * usar CDI y JTA.
 */
@ApplicationScoped
public class FestivalServiceImpl implements FestivalService {

    private static final Logger log = LoggerFactory.getLogger(FestivalServiceImpl.class);

    @Inject
    private FestivalRepository festivalRepository;
    @Inject
    private UsuarioRepository usuarioRepository;
    @Inject
    private PermissionService permissionService;
    @Inject
    private FestivalMapper festivalMapper;

    @Override
    @Transactional
    public FestivalDTO crearFestival(FestivalDTO festivalDTO, Integer idPromotor) {
        log.info("Service: Creando festival '{}' para promotor ID: {}", festivalDTO.getNombre(), idPromotor);
        if (festivalDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("FestivalDTO e idPromotor no pueden ser nulos.");
        }
        validarDatosBasicosFestivalDTO(festivalDTO);

        Usuario promotor = usuarioRepository.findById(idPromotor)
                .filter(u -> u.getRol() == RolUsuario.PROMOTOR)
                .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado o inválido con ID: " + idPromotor));

        Festival festival = festivalMapper.festivalDTOToFestival(festivalDTO);
        festival.setPromotor(promotor);
        festival.setEstado(EstadoFestival.BORRADOR);

        festival = festivalRepository.save(festival);
        log.info("Festival '{}' creado con ID: {}", festival.getNombre(), festival.getIdFestival());
        return festivalMapper.festivalToFestivalDTO(festival);
    }

    @Override
    public Optional<FestivalDTO> obtenerFestivalPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return festivalRepository.findById(id).map(festivalMapper::festivalToFestivalDTO);
    }

    @Override
    public Optional<FestivalDTO> obtenerFestivalPorId(Integer id, Integer idActor) {
        if (id == null || idActor == null) {
            return Optional.empty();
        }
        return festivalRepository.findById(id).map(festival -> {
            permissionService.verificarPermisoSobreFestival(id, idActor);
            return festivalMapper.festivalToFestivalDTO(festival);
        });
    }

    @Override
    @Transactional
    public FestivalDTO actualizarFestival(Integer id, FestivalDTO festivalDTO, Integer idUsuarioActualizador) {
        log.info("Service: Actualizando festival ID: {} por Usuario ID: {}", id, idUsuarioActualizador);
        if (id == null || festivalDTO == null || idUsuarioActualizador == null) {
            throw new IllegalArgumentException("ID de festival, DTO y ID de usuario son requeridos.");
        }
        validarDatosBasicosFestivalDTO(festivalDTO);

        permissionService.verificarPermisoSobreFestival(id, idUsuarioActualizador);
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + id));

        festivalMapper.updateFestivalFromDto(festivalDTO, festival);
        festival = festivalRepository.save(festival);
        log.info("Festival ID: {} actualizado correctamente.", id);
        return festivalMapper.festivalToFestivalDTO(festival);
    }

    @Override
    @Transactional
    public void eliminarFestival(Integer id, Integer idUsuarioEliminador) {
        log.info("Service: Eliminando festival ID: {} por Usuario ID: {}", id, idUsuarioEliminador);
        if (id == null || idUsuarioEliminador == null) {
            throw new IllegalArgumentException("ID de festival y ID de usuario son requeridos.");
        }

        permissionService.verificarPermisoSobreFestival(id, idUsuarioEliminador);
        if (!festivalRepository.deleteById(id)) {
            throw new FestivalNotFoundException("No se pudo eliminar, festival no encontrado con ID: " + id);
        }
        log.info("Festival ID: {} eliminado.", id);
    }

    @Override
    public List<FestivalDTO> buscarFestivalesPublicados(LocalDate fechaDesde, LocalDate fechaHasta) {
        // La lógica de la consulta ahora reside en el repositorio
        List<Festival> festivales = festivalRepository.findActivosEntreFechas(fechaDesde, fechaHasta);
        return festivalMapper.toFestivalDTOList(festivales);
    }

    @Override
    public List<FestivalDTO> obtenerFestivalesPorPromotor(Integer idPromotor) {
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor es requerido.");
        }
        List<Festival> festivales = festivalRepository.findByPromotorId(idPromotor);
        return festivalMapper.toFestivalDTOList(festivales);
    }

    @Override
    @Transactional
    public FestivalDTO cambiarEstadoFestival(Integer idFestival, EstadoFestival nuevoEstado, Integer idActor) {
        log.info("Service: Cambiando estado a {} para festival ID: {} por Actor ID: {}", nuevoEstado, idFestival, idActor);
        if (idFestival == null || nuevoEstado == null || idActor == null) {
            throw new IllegalArgumentException("ID de festival, nuevo estado y ID de actor son requeridos.");
        }

        Usuario actor = usuarioRepository.findById(idActor)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor));
        if (actor.getRol() != RolUsuario.ADMIN) {
            throw new SecurityException("Solo los administradores pueden cambiar el estado de un festival.");
        }

        Festival festival = festivalRepository.findById(idFestival)
                .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

        validarTransicionEstado(festival.getEstado(), nuevoEstado);
        if (festival.getEstado() == nuevoEstado) {
            return festivalMapper.festivalToFestivalDTO(festival);
        }

        festival.setEstado(nuevoEstado);
        festival = festivalRepository.save(festival);
        log.info("Estado de festival ID: {} cambiado a {}", idFestival, nuevoEstado);
        return festivalMapper.festivalToFestivalDTO(festival);
    }

    @Override
    public List<FestivalDTO> obtenerTodosLosFestivales() {
        return festivalMapper.toFestivalDTOList(festivalRepository.findAll());
    }

    @Override
    public List<FestivalDTO> obtenerFestivalesPorEstado(EstadoFestival estado) {
        List<Festival> festivales = (estado == null)
                ? festivalRepository.findAll()
                : festivalRepository.findByEstado(estado);
        return festivalMapper.toFestivalDTOList(festivales);
    }

    // --- MÉTODOS PRIVADOS ---
    /**
     * Valida los campos básicos y obligatorios de un FestivalDTO.
     *
     * @param dto El DTO a validar.
     * @throws IllegalArgumentException si el nombre está vacío o las fechas son
     * nulas o inválidas (fecha de fin anterior a la de inicio).
     */
    private void validarDatosBasicosFestivalDTO(FestivalDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank() || dto.getFechaInicio() == null || dto.getFechaFin() == null || dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException("Nombre y fechas válidas (inicio <= fin) son obligatorios.");
        }
    }

    /**
     * Valida si una transición de estado de un festival es permitida según las
     * reglas de negocio.
     *
     * @param estadoActual El estado actual del festival.
     * @param nuevoEstado El estado al que se desea cambiar.
     * @throws IllegalStateException si la transición de estado no es válida.
     */
    private void validarTransicionEstado(EstadoFestival estadoActual, EstadoFestival nuevoEstado) {
        if (estadoActual == nuevoEstado) {
            return;
        }
        switch (estadoActual) {
            case BORRADOR:
                if (nuevoEstado != EstadoFestival.PUBLICADO && nuevoEstado != EstadoFestival.CANCELADO) {
                    throw new IllegalStateException("Desde BORRADOR solo se puede pasar a PUBLICADO o CANCELADO.");
                }
                break;
            case PUBLICADO:
                if (nuevoEstado != EstadoFestival.CANCELADO && nuevoEstado != EstadoFestival.FINALIZADO) {
                    throw new IllegalStateException("Desde PUBLICADO solo se puede pasar a CANCELADO o FINALIZADO.");
                }
                break;
            case CANCELADO:
            case FINALIZADO:
                throw new IllegalStateException("No se puede cambiar el estado de un festival CANCELADO o FINALIZADO.");
            default:
                throw new IllegalStateException("Estado actual desconocido: " + estadoActual);
        }
    }
}
