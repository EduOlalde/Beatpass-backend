package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.TipoEntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.TipoEntrada;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario; // Import RolUsuario
import com.daw2edudiego.beatpasstfg.model.Usuario; // Import Usuario
import com.daw2edudiego.beatpasstfg.repository.TipoEntradaRepositoryImpl;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepository;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepositoryImpl;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepository;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepositoryImpl;
import com.daw2edudiego.beatpasstfg.mapper.TipoEntradaMapper;

import jakarta.persistence.EntityManager; // Import EntityManager
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daw2edudiego.beatpasstfg.repository.TipoEntradaRepository;

/**
 * Implementación de TipoEntradaService.
 */
public class TipoEntradaServiceImpl extends AbstractService implements TipoEntradaService {

    private static final Logger log = LoggerFactory.getLogger(TipoEntradaServiceImpl.class);

    private final TipoEntradaRepository tipoEntradaRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoEntradaMapper tipoEntradaMapper;

    public TipoEntradaServiceImpl() {
        this.tipoEntradaRepository = new TipoEntradaRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.tipoEntradaMapper = TipoEntradaMapper.INSTANCE;
    }

    @Override
    public TipoEntradaDTO crearTipoEntrada(TipoEntradaDTO tipoEntradaDTO, Integer idFestival, Integer idActor) { // Renamed idPromotor to idActor
        log.info("Service: Creando nuevo tipo de entrada para festival ID {} por actor ID {}", idFestival, idActor);
        if (tipoEntradaDTO == null || idFestival == null || idActor == null) {
            throw new IllegalArgumentException("DTO, ID festival e ID actor son requeridos.");
        }
        if (tipoEntradaDTO.getIdFestival() != null && !idFestival.equals(tipoEntradaDTO.getIdFestival())) {
            throw new IllegalArgumentException("El ID del festival en el DTO no coincide con el ID de la URL.");
        }
        validarDatosEntradaDTO(tipoEntradaDTO);

        return executeTransactional(em -> {
            // Only Promoters can create new ticket types for their festivals
            Usuario actor = usuarioRepository.findById(em, idActor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor));
            if (actor.getRol() != RolUsuario.PROMOTOR) {
                throw new SecurityException("Solo los promotores pueden crear tipos de entrada.");
            }

            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(em, festival, idActor); // Pass em

            TipoEntrada nuevaEntrada = tipoEntradaMapper.tipoEntradaDTOToTipoEntrada(tipoEntradaDTO);
            nuevaEntrada.setFestival(festival);

            TipoEntrada entradaGuardada = tipoEntradaRepository.save(em, nuevaEntrada);
            log.info("Nuevo tipo de entrada ID {} creado para festival ID {}", entradaGuardada.getIdTipoEntrada(), idFestival);
            return tipoEntradaMapper.tipoEntradaToTipoEntradaDTO(entradaGuardada);
        }, "crearTipoEntrada for festival " + idFestival);
    }

    @Override
    public List<TipoEntradaDTO> obtenerTipoEntradasPorFestival(Integer idFestival, Integer idActor) { // Renamed idPromotor to idActor
        log.debug("Service: Obteniendo tipos de entrada para festival ID {} por actor ID {}", idFestival, idActor);
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID festival e ID actor son requeridos.");
        }

        return executeRead(em -> {
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(em, festival, idActor); // Pass em

            List<TipoEntrada> tiposEntrada = tipoEntradaRepository.findByFestivalId(em, idFestival);
            log.info("Encontrados {} tipos de entrada para el festival ID {} (Actor {})", tiposEntrada.size(), idFestival, idActor);
            return tipoEntradaMapper.toTipoEntradaDTOList(tiposEntrada);
        }, "obtenerTipoEntradasPorFestival " + idFestival);
    }

    @Override
    public List<TipoEntradaDTO> obtenerTiposEntradaPublicasPorFestival(Integer idFestival) {
        log.debug("Service: Obteniendo tipos de entrada públicas para festival ID {}", idFestival);
        if (idFestival == null) {
            throw new IllegalArgumentException("ID de festival es requerido.");
        }

        return executeRead(em -> {
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                log.warn("Intento de acceso a tipos de entrada de festival ID {} no publicado (Estado: {})", idFestival, festival.getEstado());
                throw new FestivalNoPublicadoException("El festival ID " + idFestival + " no está actualmente publicado.");
            }

            List<TipoEntrada> tiposEntrada = tipoEntradaRepository.findByFestivalId(em, idFestival);
            log.info("Encontrados {} tipos de entrada para el festival público ID {}", tiposEntrada.size(), idFestival);
            return tipoEntradaMapper.toTipoEntradaDTOList(tiposEntrada);
        }, "obtenerTiposEntradaPublicasPorFestival " + idFestival);
    }

    @Override
    public TipoEntradaDTO actualizarTipoEntrada(Integer idEntrada, TipoEntradaDTO tipoEntradaDTO, Integer idActor) { // Renamed idPromotor to idActor
        log.info("Service: Actualizando tipo de entrada ID {} por actor ID {}", idEntrada, idActor);
        if (idEntrada == null || tipoEntradaDTO == null || idActor == null) {
            throw new IllegalArgumentException("ID entrada, DTO e ID actor son requeridos.");
        }
        if (tipoEntradaDTO.getIdTipoEntrada() != null && !idEntrada.equals(tipoEntradaDTO.getIdTipoEntrada())) {
            throw new IllegalArgumentException("El ID de entrada en el DTO debe coincidir con el ID de la URL o ser nulo.");
        }
        validarDatosEntradaDTO(tipoEntradaDTO);

        return executeTransactional(em -> {
            TipoEntrada entrada = tipoEntradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            verificarPropiedadFestival(em, entrada.getFestival(), idActor); // Pass em

            tipoEntradaMapper.updateTipoEntradaFromDto(tipoEntradaDTO, entrada);

            TipoEntrada entradaActualizada = tipoEntradaRepository.save(em, entrada);
            log.info("Tipo de entrada ID {} actualizado exitosamente.", idEntrada);
            return tipoEntradaMapper.tipoEntradaToTipoEntradaDTO(entradaActualizada);
        }, "actualizarTipoEntrada " + idEntrada);
    }

    @Override
    public void eliminarTipoEntrada(Integer idEntrada, Integer idActor) { // Renamed idPromotor to idActor
        log.info("Service: Eliminando tipo de entrada ID {} por actor ID {}", idEntrada, idActor);
        if (idEntrada == null || idActor == null) {
            throw new IllegalArgumentException("ID entrada e ID actor son requeridos.");
        }

        executeTransactional(em -> {
            TipoEntrada entrada = tipoEntradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            verificarPropiedadFestival(em, entrada.getFestival(), idActor); // Pass em

            boolean eliminado = tipoEntradaRepository.deleteById(em, idEntrada);
            if (!eliminado) {
                log.warn("La entrada ID {} fue encontrada pero no pudo ser eliminada.", idEntrada);
                throw new TipoEntradaNotFoundException("No se pudo eliminar la entrada ID " + idEntrada + ".");
            }
            log.info("Tipo de entrada ID {} eliminado exitosamente.", idEntrada);
            return null;
        }, "eliminarTipoEntrada " + idEntrada);
    }

    @Override
    public Optional<TipoEntradaDTO> obtenerTipoEntradaPorId(Integer idEntrada, Integer idActor) { // Renamed idPromotor to idActor
        log.debug("Service: Obteniendo entrada ID {} por actor ID {}", idEntrada, idActor);
        if (idEntrada == null || idActor == null) {
            throw new IllegalArgumentException("IDs de entrada y actor son requeridos.");
        }

        return executeRead(em -> {
            Optional<TipoEntrada> entradaOpt = tipoEntradaRepository.findById(em, idEntrada);

            if (entradaOpt.isEmpty()) {
                log.warn("Entrada no encontrada con ID {}", idEntrada);
                return Optional.empty();
            }

            TipoEntrada entrada = entradaOpt.get();
            verificarPropiedadFestival(em, entrada.getFestival(), idActor); // Pass em
            return Optional.of(tipoEntradaMapper.tipoEntradaToTipoEntradaDTO(entrada));
        }, "obtenerTipoEntradaPorId " + idEntrada);
    }

    // --- Métodos Privados de Ayuda ---
    /**
     * Valida los campos básicos de un TipoEntradaDTO.
     */
    private void validarDatosEntradaDTO(TipoEntradaDTO dto) {
        if (dto.getTipo() == null || dto.getTipo().isBlank()
                || dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0
                || dto.getStock() == null || dto.getStock() < 0) {
            throw new IllegalArgumentException("Datos inválidos en DTO: tipo, precio (>=0) y stock (>=0) son obligatorios.");
        }
    }

    /**
     * Mapea DTO a entidad TipoEntrada (sin asociar festival).
     */
    private TipoEntrada mapDtoToEntity(TipoEntradaDTO dto) {
        if (dto == null) {
            return null;
        }
        TipoEntrada entity = new TipoEntrada();
        entity.setTipo(dto.getTipo().trim());
        entity.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        entity.setPrecio(dto.getPrecio());
        entity.setStock(dto.getStock());
        if (dto.getRequiereNominacion() != null) {
            entity.setRequiereNominacion(dto.getRequiereNominacion());
        }
        return entity;
    }

    /**
     * Mapea entidad TipoEntrada a DTO.
     */
    private TipoEntradaDTO mapEntityToDto(TipoEntrada entrada) {
        if (entrada == null) {
            return null;
        }
        TipoEntradaDTO dto = new TipoEntradaDTO();
        dto.setIdTipoEntrada(entrada.getIdTipoEntrada());
        if (entrada.getFestival() != null) {
            dto.setIdFestival(entrada.getFestival().getIdFestival());
        }
        dto.setTipo(entrada.getTipo());
        dto.setDescripcion(entrada.getDescripcion());
        dto.setPrecio(entrada.getPrecio());
        dto.setStock(entrada.getStock());
        dto.setRequiereNominacion(entrada.getRequiereNominacion());

        return dto;
    }

    /**
     * Verifica que el usuario actor (ADMIN o PROMOTOR) tenga permiso sobre el
     * festival. Si es PROMOTOR, debe ser el propietario del festival. Si es
     * ADMIN, siempre tiene acceso.
     */
    private void verificarPropiedadFestival(EntityManager em, Festival festival, Integer idActor) { // Added EntityManager parameter
        if (festival == null) {
            throw new FestivalNotFoundException("El festival asociado no puede ser nulo.");
        }
        if (idActor == null) {
            throw new IllegalArgumentException("El ID del usuario actor no puede ser nulo.");
        }

        // Fetch the acting user (actor) to check their role.
        Usuario actor = em.find(Usuario.class, idActor); // Use em.find directly here for simplicity and direct management

        if (actor == null) {
            throw new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor);
        }

        boolean isActorAdmin = (actor.getRol() != null && actor.getRol() == RolUsuario.ADMIN); // Added null check for safety
        boolean isActorPromotorOwner = (festival.getPromotor() != null && festival.getPromotor().getIdUsuario().equals(idActor));

        if (!(isActorAdmin || isActorPromotorOwner)) {
            log.warn("Intento de acceso no autorizado por usuario ID {} (Rol: {}) al festival ID {} (Prop. por Promotor ID {})",
                    idActor, (actor.getRol() != null ? actor.getRol() : "NULL_ROL"), festival.getIdFestival(), festival.getPromotor() != null ? festival.getPromotor().getIdUsuario() : "N/A");
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
    }
}
