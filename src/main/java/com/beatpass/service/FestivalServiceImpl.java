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
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de festivales.
 */
public class FestivalServiceImpl extends AbstractService implements FestivalService {

    private static final Logger log = LoggerFactory.getLogger(FestivalServiceImpl.class);

    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalMapper festivalMapper;

    @Inject
    public FestivalServiceImpl(FestivalRepository festivalRepository, UsuarioRepository usuarioRepository) {
        this.festivalRepository = festivalRepository;
        this.usuarioRepository = usuarioRepository;
        this.festivalMapper = FestivalMapper.INSTANCE;
    }

    @Override
    public FestivalDTO crearFestival(FestivalDTO festivalDTO, Integer idPromotor) {
        log.info("Service: Creando festival '{}' para promotor ID: {}", festivalDTO.getNombre(), idPromotor);
        if (festivalDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("FestivalDTO e idPromotor no pueden ser nulos.");
        }
        validarDatosBasicosFestivalDTO(festivalDTO);

        return executeTransactional(em -> {
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .filter(u -> u.getRol() == RolUsuario.PROMOTOR)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado o inválido con ID: " + idPromotor));

            Festival festival = festivalMapper.festivalDTOToFestival(festivalDTO);
            festival.setPromotor(promotor);
            festival.setEstado(EstadoFestival.BORRADOR);

            festival = festivalRepository.save(em, festival);
            log.info("Festival '{}' creado con ID: {}", festival.getNombre(), festival.getIdFestival());
            return festivalMapper.festivalToFestivalDTO(festival);
        }, "crearFestival");
    }

    @Override
    public Optional<FestivalDTO> obtenerFestivalPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return executeRead(em
                -> festivalRepository.findById(em, id).map(festivalMapper::festivalToFestivalDTO),
                "obtenerFestivalPorId " + id
        );
    }

    @Override
    public FestivalDTO actualizarFestival(Integer id, FestivalDTO festivalDTO, Integer idUsuarioActualizador) {
        log.info("Service: Actualizando festival ID: {} por Usuario ID: {}", id, idUsuarioActualizador);
        if (id == null || festivalDTO == null || idUsuarioActualizador == null) {
            throw new IllegalArgumentException("ID de festival, DTO y ID de usuario son requeridos.");
        }
        validarDatosBasicosFestivalDTO(festivalDTO);

        return executeTransactional(em -> {
            verificarPermisoSobreFestival(em, id, idUsuarioActualizador);
            Festival festival = festivalRepository.findById(em, id)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + id));

            festivalMapper.updateFestivalFromDto(festivalDTO, festival);
            festival = festivalRepository.save(em, festival);
            log.info("Festival ID: {} actualizado correctamente.", id);
            return festivalMapper.festivalToFestivalDTO(festival);
        }, "actualizarFestival " + id);
    }

    @Override
    public void eliminarFestival(Integer id, Integer idUsuarioEliminador) {
        log.info("Service: Eliminando festival ID: {} por Usuario ID: {}", id, idUsuarioEliminador);
        if (id == null || idUsuarioEliminador == null) {
            throw new IllegalArgumentException("ID de festival y ID de usuario son requeridos.");
        }

        executeTransactional(em -> {
            verificarPermisoSobreFestival(em, id, idUsuarioEliminador);
            if (!festivalRepository.deleteById(em, id)) {
                throw new FestivalNotFoundException("No se pudo eliminar, festival no encontrado con ID: " + id);
            }
            log.info("Festival ID: {} eliminado.", id);
            return null;
        }, "eliminarFestival " + id);
    }

    @Override
    public List<FestivalDTO> buscarFestivalesPublicados(LocalDate fechaDesde, LocalDate fechaHasta) {
        return executeRead(em -> {
            List<Festival> festivales = festivalRepository.findActivosEntreFechas(em, fechaDesde, fechaHasta);
            return festivalMapper.toFestivalDTOList(festivales);
        }, "buscarFestivalesPublicados");
    }

    @Override
    public List<FestivalDTO> obtenerFestivalesPorPromotor(Integer idPromotor) {
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor es requerido.");
        }
        return executeRead(em -> {
            List<Festival> festivales = festivalRepository.findByPromotorId(em, idPromotor);
            return festivalMapper.toFestivalDTOList(festivales);
        }, "obtenerFestivalesPorPromotor " + idPromotor);
    }

    @Override
    public FestivalDTO cambiarEstadoFestival(Integer idFestival, EstadoFestival nuevoEstado, Integer idActor) {
        log.info("Service: Cambiando estado a {} para festival ID: {} por Actor ID: {}", nuevoEstado, idFestival, idActor);
        if (idFestival == null || nuevoEstado == null || idActor == null) {
            throw new IllegalArgumentException("ID de festival, nuevo estado y ID de actor son requeridos.");
        }

        return executeTransactional(em -> {
            Usuario actor = usuarioRepository.findById(em, idActor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor));
            if (actor.getRol() != RolUsuario.ADMIN) {
                throw new SecurityException("Solo los administradores pueden cambiar el estado de un festival.");
            }

            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            validarTransicionEstado(festival.getEstado(), nuevoEstado);
            if (festival.getEstado() == nuevoEstado) {
                return festivalMapper.festivalToFestivalDTO(festival);
            }

            festival.setEstado(nuevoEstado);
            festival = festivalRepository.save(em, festival);
            log.info("Estado de festival ID: {} cambiado a {}", idFestival, nuevoEstado);
            return festivalMapper.festivalToFestivalDTO(festival);
        }, "cambiarEstadoFestival " + idFestival + " to " + nuevoEstado);
    }

    @Override
    public List<FestivalDTO> obtenerTodosLosFestivales() {
        return executeRead(em
                -> festivalMapper.toFestivalDTOList(festivalRepository.findAll(em)),
                "obtenerTodosLosFestivales"
        );
    }

    @Override
    public List<FestivalDTO> obtenerFestivalesPorEstado(EstadoFestival estado) {
        return executeRead(em -> {
            List<Festival> festivales = (estado == null)
                    ? festivalRepository.findAll(em)
                    : festivalRepository.findByEstado(em, estado);
            return festivalMapper.toFestivalDTOList(festivales);
        }, "obtenerFestivalesPorEstado " + (estado != null ? estado.name() : "ALL"));
    }

    private void validarDatosBasicosFestivalDTO(FestivalDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank() || dto.getFechaInicio() == null || dto.getFechaFin() == null || dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException("Nombre y fechas válidas (inicio <= fin) son obligatorios.");
        }
    }

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
