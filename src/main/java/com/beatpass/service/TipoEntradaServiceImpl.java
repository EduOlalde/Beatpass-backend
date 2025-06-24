package com.beatpass.service;

import com.beatpass.dto.TipoEntradaDTO;
import com.beatpass.exception.FestivalNoPublicadoException;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.TipoEntradaNotFoundException;
import com.beatpass.mapper.TipoEntradaMapper;
import com.beatpass.model.EstadoFestival;
import com.beatpass.model.Festival;
import com.beatpass.model.TipoEntrada;
import com.beatpass.repository.FestivalRepository;
import com.beatpass.repository.TipoEntradaRepository;
import com.beatpass.repository.UsuarioRepository;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de Tipos de Entrada.
 */
public class TipoEntradaServiceImpl extends AbstractService implements TipoEntradaService {

    private static final Logger log = LoggerFactory.getLogger(TipoEntradaServiceImpl.class);

    private final TipoEntradaRepository tipoEntradaRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoEntradaMapper tipoEntradaMapper;

    @Inject
    public TipoEntradaServiceImpl(TipoEntradaRepository tipoEntradaRepository, FestivalRepository festivalRepository, UsuarioRepository usuarioRepository) {
        this.tipoEntradaRepository = tipoEntradaRepository;
        this.festivalRepository = festivalRepository;
        this.usuarioRepository = usuarioRepository;
        this.tipoEntradaMapper = TipoEntradaMapper.INSTANCE;
    }

    @Override
    public TipoEntradaDTO crearTipoEntrada(TipoEntradaDTO tipoEntradaDTO, Integer idFestival, Integer idActor) {
        log.info("Service: Creando nuevo tipo de entrada para festival ID {} por actor ID {}", idFestival, idActor);
        if (tipoEntradaDTO == null || idFestival == null || idActor == null) {
            throw new IllegalArgumentException("DTO, ID festival e ID actor son requeridos.");
        }
        validarDatosEntradaDTO(tipoEntradaDTO);

        return executeTransactional(em -> {
            verificarPermisoSobreFestival(em, idFestival, idActor);

            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            TipoEntrada nuevaEntrada = tipoEntradaMapper.tipoEntradaDTOToTipoEntrada(tipoEntradaDTO);
            nuevaEntrada.setFestival(festival);

            TipoEntrada entradaGuardada = tipoEntradaRepository.save(em, nuevaEntrada);
            log.info("Nuevo tipo de entrada ID {} creado para festival ID {}", entradaGuardada.getIdTipoEntrada(), idFestival);
            return tipoEntradaMapper.tipoEntradaToTipoEntradaDTO(entradaGuardada);
        }, "crearTipoEntrada for festival " + idFestival);
    }

    @Override
    public List<TipoEntradaDTO> obtenerTipoEntradasPorFestival(Integer idFestival, Integer idActor) {
        log.debug("Service: Obteniendo tipos de entrada para festival ID {} por actor ID {}", idFestival, idActor);
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID festival e ID actor son requeridos.");
        }

        return executeRead(em -> {
            verificarPermisoSobreFestival(em, idFestival, idActor);

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
    public TipoEntradaDTO actualizarTipoEntrada(Integer idEntrada, TipoEntradaDTO tipoEntradaDTO, Integer idActor) {
        log.info("Service: Actualizando tipo de entrada ID {} por actor ID {}", idEntrada, idActor);
        if (idEntrada == null || tipoEntradaDTO == null || idActor == null) {
            throw new IllegalArgumentException("ID entrada, DTO e ID actor son requeridos.");
        }
        validarDatosEntradaDTO(tipoEntradaDTO);

        return executeTransactional(em -> {
            TipoEntrada entrada = tipoEntradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            verificarPermisoSobreFestival(em, entrada.getFestival().getIdFestival(), idActor);

            tipoEntradaMapper.updateTipoEntradaFromDto(tipoEntradaDTO, entrada);

            TipoEntrada entradaActualizada = tipoEntradaRepository.save(em, entrada);
            log.info("Tipo de entrada ID {} actualizado exitosamente.", idEntrada);
            return tipoEntradaMapper.tipoEntradaToTipoEntradaDTO(entradaActualizada);
        }, "actualizarTipoEntrada " + idEntrada);
    }

    @Override
    public void eliminarTipoEntrada(Integer idEntrada, Integer idActor) {
        log.info("Service: Eliminando tipo de entrada ID {} por actor ID {}", idEntrada, idActor);
        if (idEntrada == null || idActor == null) {
            throw new IllegalArgumentException("ID entrada e ID actor son requeridos.");
        }

        executeTransactional(em -> {
            TipoEntrada entrada = tipoEntradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            verificarPermisoSobreFestival(em, entrada.getFestival().getIdFestival(), idActor);

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
    public Optional<TipoEntradaDTO> obtenerTipoEntradaPorId(Integer idEntrada, Integer idActor) {
        log.debug("Service: Obteniendo entrada ID {} por actor ID {}", idEntrada, idActor);
        if (idEntrada == null || idActor == null) {
            throw new IllegalArgumentException("IDs de entrada y actor son requeridos.");
        }

        return executeRead(em -> {
            Optional<TipoEntrada> entradaOpt = tipoEntradaRepository.findById(em, idEntrada);

            if (entradaOpt.isEmpty()) {
                return Optional.empty();
            }

            TipoEntrada entrada = entradaOpt.get();
            verificarPermisoSobreFestival(em, entrada.getFestival().getIdFestival(), idActor);
            return Optional.of(tipoEntradaMapper.tipoEntradaToTipoEntradaDTO(entrada));
        }, "obtenerTipoEntradaPorId " + idEntrada);
    }

    private void validarDatosEntradaDTO(TipoEntradaDTO dto) {
        if (dto.getTipo() == null || dto.getTipo().isBlank()
                || dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0
                || dto.getStock() == null || dto.getStock() < 0) {
            throw new IllegalArgumentException("Datos inválidos en DTO: tipo, precio (>=0) y stock (>=0) son obligatorios.");
        }
    }
}
