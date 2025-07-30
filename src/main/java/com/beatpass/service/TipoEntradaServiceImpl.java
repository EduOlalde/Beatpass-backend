package com.beatpass.service;

import com.beatpass.dto.TipoEntradaDTO;
import com.beatpass.dto.TipoEntradaUpdateDTO;
import com.beatpass.exception.FestivalNoPublicadoException;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.TipoEntradaNotFoundException;
import com.beatpass.mapper.TipoEntradaMapper;
import com.beatpass.model.EstadoFestival;
import com.beatpass.model.Festival;
import com.beatpass.model.TipoEntrada;
import com.beatpass.repository.FestivalRepository;
import com.beatpass.repository.TipoEntradaRepository;
import com.beatpass.util.PermissionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de Tipos de Entrada,
 * refactorizada para usar CDI y JTA.
 */
@ApplicationScoped
public class TipoEntradaServiceImpl implements TipoEntradaService {

    private static final Logger log = LoggerFactory.getLogger(TipoEntradaServiceImpl.class);

    @Inject
    private TipoEntradaRepository tipoEntradaRepository;
    @Inject
    private FestivalRepository festivalRepository;
    @Inject
    private PermissionService permissionService;
    @Inject
    private TipoEntradaMapper tipoEntradaMapper;

    @Override
    @Transactional
    public TipoEntradaDTO crearTipoEntrada(TipoEntradaDTO tipoEntradaDTO, Integer idFestival, Integer idActor) {
        log.info("Service: Creando nuevo tipo de entrada para festival ID {} por actor ID {}", idFestival, idActor);
        if (tipoEntradaDTO == null || idFestival == null || idActor == null) {
            throw new IllegalArgumentException("DTO, ID festival e ID actor son requeridos.");
        }
        validarDatosEntradaDTO(tipoEntradaDTO);

        permissionService.verificarPermisoSobreFestival(idFestival, idActor);

        Festival festival = festivalRepository.findById(idFestival)
                .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

        TipoEntrada nuevaEntrada = tipoEntradaMapper.tipoEntradaDTOToTipoEntrada(tipoEntradaDTO);
        nuevaEntrada.setFestival(festival);

        TipoEntrada entradaGuardada = tipoEntradaRepository.save(nuevaEntrada);
        log.info("Nuevo tipo de entrada ID {} creado para festival ID {}", entradaGuardada.getIdTipoEntrada(), idFestival);
        return tipoEntradaMapper.tipoEntradaToTipoEntradaDTO(entradaGuardada);
    }

    @Override
    public List<TipoEntradaDTO> obtenerTipoEntradasPorFestival(Integer idFestival, Integer idActor) {
        log.debug("Service: Obteniendo tipos de entrada para festival ID {} por actor ID {}", idFestival, idActor);
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID festival e ID actor son requeridos.");
        }

        permissionService.verificarPermisoSobreFestival(idFestival, idActor);

        List<TipoEntrada> tiposEntrada = tipoEntradaRepository.findByFestivalId(idFestival);
        log.info("Encontrados {} tipos de entrada para el festival ID {} (Actor {})", tiposEntrada.size(), idFestival, idActor);
        return tipoEntradaMapper.toTipoEntradaDTOList(tiposEntrada);
    }

    @Override
    public List<TipoEntradaDTO> obtenerTiposEntradaPublicasPorFestival(Integer idFestival) {
        log.debug("Service: Obteniendo tipos de entrada públicas para festival ID {}", idFestival);
        if (idFestival == null) {
            throw new IllegalArgumentException("ID de festival es requerido.");
        }

        Festival festival = festivalRepository.findById(idFestival)
                .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

        if (festival.getEstado() != EstadoFestival.PUBLICADO) {
            log.warn("Intento de acceso a tipos de entrada de festival ID {} no publicado (Estado: {})", idFestival, festival.getEstado());
            throw new FestivalNoPublicadoException("El festival ID " + idFestival + " no está actualmente publicado.");
        }

        List<TipoEntrada> tiposEntrada = tipoEntradaRepository.findByFestivalId(idFestival);
        log.info("Encontrados {} tipos de entrada para el festival público ID {}", tiposEntrada.size(), idFestival);
        return tipoEntradaMapper.toTipoEntradaDTOList(tiposEntrada);
    }

    @Override
    @Transactional
    public TipoEntradaDTO actualizarTipoEntrada(Integer idEntrada, TipoEntradaUpdateDTO tipoEntradaUpdateDTO, Integer idActor) {
        log.info("Service: Actualizando tipo de entrada ID {} por actor ID {}", idEntrada, idActor);
        if (idEntrada == null || tipoEntradaUpdateDTO == null || idActor == null) {
            throw new IllegalArgumentException("ID entrada, DTO e ID actor son requeridos.");
        }

        TipoEntrada entrada = tipoEntradaRepository.findById(idEntrada)
                .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

        permissionService.verificarPermisoSobreFestival(entrada.getFestival().getIdFestival(), idActor);

        // Manually map fields from the update DTO to the entity
        entrada.setTipo(tipoEntradaUpdateDTO.getTipo());
        entrada.setDescripcion(tipoEntradaUpdateDTO.getDescripcion());
        entrada.setPrecio(tipoEntradaUpdateDTO.getPrecio());
        entrada.setStock(tipoEntradaUpdateDTO.getStock());
        entrada.setRequiereNominacion(tipoEntradaUpdateDTO.getRequiereNominacion());

        TipoEntrada entradaActualizada = tipoEntradaRepository.save(entrada);
        log.info("Tipo de entrada ID {} actualizado exitosamente.", idEntrada);
        return tipoEntradaMapper.tipoEntradaToTipoEntradaDTO(entradaActualizada);
    }

    @Override
    @Transactional
    public void eliminarTipoEntrada(Integer idEntrada, Integer idActor) {
        log.info("Service: Eliminando tipo de entrada ID {} por actor ID {}", idEntrada, idActor);
        if (idEntrada == null || idActor == null) {
            throw new IllegalArgumentException("ID entrada e ID actor son requeridos.");
        }

        TipoEntrada entrada = tipoEntradaRepository.findById(idEntrada)
                .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

        permissionService.verificarPermisoSobreFestival(entrada.getFestival().getIdFestival(), idActor);

        boolean eliminado = tipoEntradaRepository.deleteById(idEntrada);
        if (!eliminado) {
            log.warn("La entrada ID {} fue encontrada pero no pudo ser eliminada.", idEntrada);
            throw new TipoEntradaNotFoundException("No se pudo eliminar la entrada ID " + idEntrada + ".");
        }
        log.info("Tipo de entrada ID {} eliminado exitosamente.", idEntrada);
    }

    @Override
    public Optional<TipoEntradaDTO> obtenerTipoEntradaPorId(Integer idEntrada, Integer idActor) {
        log.debug("Service: Obteniendo entrada ID {} por actor ID {}", idEntrada, idActor);
        if (idEntrada == null || idActor == null) {
            throw new IllegalArgumentException("IDs de entrada y actor son requeridos.");
        }

        return tipoEntradaRepository.findById(idEntrada)
                .map(entrada -> {
                    permissionService.verificarPermisoSobreFestival(entrada.getFestival().getIdFestival(), idActor);
                    return tipoEntradaMapper.tipoEntradaToTipoEntradaDTO(entrada);
                });
    }

    // --- MÉTODO PRIVADO ORIGINAL PRESERVADO ---
    private void validarDatosEntradaDTO(TipoEntradaDTO dto) {
        if (dto.getTipo() == null || dto.getTipo().isBlank()
                || dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0
                || dto.getStock() == null || dto.getStock() < 0) {
            throw new IllegalArgumentException("Datos inválidos en DTO: tipo, precio (>=0) y stock (>=0) son obligatorios.");
        }
    }
}
