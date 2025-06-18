package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepository;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepositoryImpl;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepository;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepositoryImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de FestivalService.
 */
public class FestivalServiceImpl extends AbstractService implements FestivalService {

    private static final Logger log = LoggerFactory.getLogger(FestivalServiceImpl.class);

    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;

    public FestivalServiceImpl() {
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    @Override
    public FestivalDTO crearFestival(FestivalDTO festivalDTO, Integer idPromotor) {
        log.info("Service: Iniciando creación de festival '{}' para promotor ID: {}",
                festivalDTO != null ? festivalDTO.getNombre() : "null", idPromotor);

        if (festivalDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("FestivalDTO e idPromotor no pueden ser nulos.");
        }
        validarDatosBasicosFestivalDTO(festivalDTO);

        return executeTransactional(em -> {
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .filter(u -> u.getRol() == RolUsuario.PROMOTOR)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado o inválido con ID: " + idPromotor));

            Festival festival = mapDtoToEntity(festivalDTO);
            festival.setPromotor(promotor);
            if (festival.getEstado() == null) {
                festival.setEstado(EstadoFestival.BORRADOR);
            }

            festival = festivalRepository.save(em, festival);
            log.info("Service: Festival '{}' creado exitosamente con ID: {}", festival.getNombre(), festival.getIdFestival());
            return mapEntityToDto(festival);
        }, "crearFestival");
    }

    @Override
    public Optional<FestivalDTO> obtenerFestivalPorId(Integer id) {
        log.debug("Service: Buscando festival por ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        return executeRead(em -> {
            return festivalRepository.findById(em, id).map(this::mapEntityToDto);
        }, "obtenerFestivalPorId " + id);
    }

    @Override
    public FestivalDTO actualizarFestival(Integer id, FestivalDTO festivalDTO, Integer idUsuarioActualizador) {
        log.info("Service: Iniciando actualización de festival ID: {} por Usuario ID: {}", id, idUsuarioActualizador);

        if (id == null || festivalDTO == null || idUsuarioActualizador == null) {
            throw new IllegalArgumentException("ID festival, DTO y ID usuario son requeridos.");
        }
        validarDatosBasicosFestivalDTO(festivalDTO);

        return executeTransactional(em -> {
            Usuario actualizador = usuarioRepository.findById(em, idUsuarioActualizador)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actualizador no encontrado ID: " + idUsuarioActualizador));

            Festival festival = festivalRepository.findById(em, id)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + id));

            verificarPermisoModificacion(festival, actualizador);

            festival.setNombre(festivalDTO.getNombre().trim());
            festival.setDescripcion(festivalDTO.getDescripcion() != null ? festivalDTO.getDescripcion().trim() : null);
            festival.setFechaInicio(festivalDTO.getFechaInicio());
            festival.setFechaFin(festivalDTO.getFechaFin());
            festival.setUbicacion(festivalDTO.getUbicacion() != null ? festivalDTO.getUbicacion().trim() : null);
            festival.setAforo(festivalDTO.getAforo());
            festival.setImagenUrl(festivalDTO.getImagenUrl() != null ? festivalDTO.getImagenUrl().trim() : null);
            // El estado NO se actualiza aquí

            festival = festivalRepository.save(em, festival);
            log.info("Service: Festival ID: {} actualizado correctamente por Usuario ID: {}", id, idUsuarioActualizador);
            return mapEntityToDto(festival);
        }, "actualizarFestival " + id);
    }

    @Override
    public void eliminarFestival(Integer id, Integer idUsuarioEliminador) {
        log.info("Service: Iniciando eliminación de festival ID: {} por Usuario ID: {}", id, idUsuarioEliminador);
        if (id == null || idUsuarioEliminador == null) {
            throw new IllegalArgumentException("ID festival e ID usuario son requeridos.");
        }

        executeTransactional(em -> {
            Usuario eliminador = usuarioRepository.findById(em, idUsuarioEliminador)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario eliminador no encontrado ID: " + idUsuarioEliminador));

            Festival festival = festivalRepository.findById(em, id)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + id));

            verificarPermisoModificacion(festival, eliminador);

            boolean eliminado = festivalRepository.deleteById(em, id);
            if (!eliminado) {
                log.warn("deleteById devolvió false después de encontrar el festival ID {}, posible inconsistencia.", id);
                throw new RuntimeException("No se pudo completar la eliminación del festival ID: " + id);
            }
            log.info("Service: Festival ID: {} eliminado correctamente por Usuario ID: {}", id, idUsuarioEliminador);
            return null;
        }, "eliminarFestival " + id);
    }

    @Override
    public List<FestivalDTO> buscarFestivalesPublicados(LocalDate fechaDesde, LocalDate fechaHasta) {
        log.debug("Service: Buscando festivales publicados entre {} y {}", fechaDesde, fechaHasta);
        return executeRead(em -> {
            List<Festival> festivales = festivalRepository.findActivosEntreFechas(em, fechaDesde, fechaHasta);
            log.info("Service: Encontrados {} festivales publicados entre {} y {}.", festivales.size(), fechaDesde, fechaHasta);
            return festivales.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        }, "buscarFestivalesPublicados");
    }

    @Override
    public List<FestivalDTO> obtenerFestivalesPorPromotor(Integer idPromotor) {
        log.debug("Service: Obteniendo festivales para promotor ID: {}", idPromotor);
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor es requerido.");
        }
        return executeRead(em -> {
            List<Festival> festivales = festivalRepository.findByPromotorId(em, idPromotor);
            log.info("Service: Encontrados {} festivales para promotor ID: {}", festivales.size(), idPromotor);
            return festivales.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        }, "obtenerFestivalesPorPromotor " + idPromotor);
    }

    @Override
    public FestivalDTO cambiarEstadoFestival(Integer idFestival, EstadoFestival nuevoEstado, Integer idActor) {
        log.info("Service: Iniciando cambio de estado a {} para festival ID: {} por Actor ID: {}", nuevoEstado, idFestival, idActor);
        if (idFestival == null || nuevoEstado == null || idActor == null) {
            throw new IllegalArgumentException("IDs de festival, nuevo estado y actor son requeridos.");
        }

        return executeTransactional(em -> {
            Usuario actor = usuarioRepository.findById(em, idActor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor));
            if (actor.getRol() != RolUsuario.ADMIN) {
                log.warn("Service: Intento de cambiar estado de festival por usuario no ADMIN (ID: {}, Rol: {})", idActor, actor.getRol());
                throw new SecurityException("Solo los administradores pueden cambiar el estado de un festival.");
            }

            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            validarTransicionEstado(festival.getEstado(), nuevoEstado);

            if (festival.getEstado() == nuevoEstado) {
                log.info("Service: El festival ID {} ya está en estado {}. No se realiza cambio.", idFestival, nuevoEstado);
                return mapEntityToDto(festival);
            }

            festival.setEstado(nuevoEstado);
            festival = festivalRepository.save(em, festival);
            log.info("Service: Estado de festival ID: {} cambiado a {} correctamente por Admin ID: {}", idFestival, nuevoEstado, idActor);
            return mapEntityToDto(festival);
        }, "cambiarEstadoFestival " + idFestival + " to " + nuevoEstado);
    }

    @Override
    public List<FestivalDTO> obtenerTodosLosFestivales() {
        log.debug("Service: Obteniendo todos los festivales (Admin).");
        return executeRead(em -> {
            List<Festival> festivales = festivalRepository.findAll(em);
            log.info("Service: Encontrados {} festivales en total.", festivales.size());
            return festivales.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        }, "obtenerTodosLosFestivales");
    }

    @Override
    public List<FestivalDTO> obtenerFestivalesPorEstado(EstadoFestival estado) {
        log.debug("Service: Obteniendo festivales por estado: {} (Admin).", estado);
        return executeRead(em -> {
            List<Festival> festivales;
            if (estado == null) {
                festivales = festivalRepository.findAll(em);
            } else {
                festivales = festivalRepository.findByEstado(em, estado);
            }
            log.info("Service: Encontrados {} festivales para estado {}.", festivales.size(), estado == null ? "TODOS" : estado);
            return festivales.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        }, "obtenerFestivalesPorEstado " + (estado != null ? estado.name() : "ALL"));
    }

    // --- Métodos Privados de Ayuda ---
    /**
     * Valida datos básicos del DTO de Festival.
     */
    private void validarDatosBasicosFestivalDTO(FestivalDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()
                || dto.getFechaInicio() == null || dto.getFechaFin() == null
                || dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException("Nombre y fechas válidas (inicio <= fin) son obligatorios.");
        }
    }

    /**
     * Verifica si el usuario tiene permiso para modificar/eliminar el festival.
     */
    private void verificarPermisoModificacion(Festival festival, Usuario usuario) {
        if (festival == null || usuario == null) {
            throw new IllegalArgumentException("Festival y Usuario no pueden ser nulos.");
        }
        boolean esAdmin = usuario.getRol() == RolUsuario.ADMIN;
        boolean esPromotorDueño = festival.getPromotor() != null
                && festival.getPromotor().getIdUsuario().equals(usuario.getIdUsuario());
        if (!(esAdmin || esPromotorDueño)) {
            log.warn("Intento no autorizado de modificar/eliminar festival ID {} por usuario ID {}",
                    festival.getIdFestival(), usuario.getIdUsuario());
            throw new SecurityException("El usuario no tiene permiso para modificar o eliminar este festival.");
        }
    }

    /**
     * Valida si una transición de estado es permitida.
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

    /**
     * Mapea entidad Festival a DTO.
     */
    private FestivalDTO mapEntityToDto(Festival f) {
        if (f == null) {
            return null;
        }
        FestivalDTO dto = new FestivalDTO();
        dto.setIdFestival(f.getIdFestival());
        dto.setNombre(f.getNombre());
        dto.setDescripcion(f.getDescripcion());
        dto.setFechaInicio(f.getFechaInicio());
        dto.setFechaFin(f.getFechaFin());
        dto.setUbicacion(f.getUbicacion());
        dto.setAforo(f.getAforo());
        dto.setImagenUrl(f.getImagenUrl());
        dto.setEstado(f.getEstado());
        if (f.getPromotor() != null) {
            dto.setIdPromotor(f.getPromotor().getIdUsuario());
            dto.setNombrePromotor(f.getPromotor().getNombre());
        }
        return dto;
    }

    /**
     * Mapea DTO a entidad Festival (sin ID ni promotor).
     */
    private Festival mapDtoToEntity(FestivalDTO dto) {
        if (dto == null) {
            return null;
        }
        Festival entity = new Festival();
        entity.setNombre(dto.getNombre().trim());
        entity.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        entity.setFechaInicio(dto.getFechaInicio());
        entity.setFechaFin(dto.getFechaFin());
        entity.setUbicacion(dto.getUbicacion() != null ? dto.getUbicacion().trim() : null);
        entity.setAforo(dto.getAforo());
        entity.setImagenUrl(dto.getImagenUrl() != null ? dto.getImagenUrl().trim() : null);
        entity.setEstado(dto.getEstado());
        return entity;
    }

}
