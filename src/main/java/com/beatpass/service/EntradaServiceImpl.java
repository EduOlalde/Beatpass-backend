package com.beatpass.service;

import com.beatpass.dto.EntradaDTO;
import com.beatpass.exception.EntradaNotFoundException;
import com.beatpass.exception.FestivalNotFoundException;
import com.beatpass.exception.UsuarioNotFoundException;
import com.beatpass.mapper.EntradaMapper;
import com.beatpass.model.*;
import com.beatpass.repository.EntradaRepository;
import com.beatpass.repository.FestivalRepository;
import com.beatpass.repository.TipoEntradaRepository;
import com.beatpass.repository.UsuarioRepository;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de entradas individuales.
 */
public class EntradaServiceImpl extends AbstractService implements EntradaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaServiceImpl.class);

    private final EntradaRepository entradaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final AsistenteService asistenteService;
    private final EmailService emailService;
    private final EntradaMapper entradaMapper;

    @Inject
    public EntradaServiceImpl(EntradaRepository entradaRepository, UsuarioRepository usuarioRepository, FestivalRepository festivalRepository, TipoEntradaRepository tipoEntradaRepository, AsistenteService asistenteService, EmailService emailService) {
        this.entradaRepository = entradaRepository;
        this.usuarioRepository = usuarioRepository;
        this.festivalRepository = festivalRepository;
        this.tipoEntradaRepository = tipoEntradaRepository;
        this.asistenteService = asistenteService;
        this.emailService = emailService;
        this.entradaMapper = EntradaMapper.INSTANCE;
    }

    @Override
    public EntradaDTO nominarEntrada(Integer idEntrada, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistente, Integer idPromotor) {
        log.info("Service: Nominando entrada ID {} para asistente {} por Promotor ID {}", idEntrada, emailAsistenteNominado, idPromotor);
        if (idEntrada == null || emailAsistenteNominado == null || emailAsistenteNominado.isBlank() || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada, email de asistente y ID de promotor son requeridos.");
        }

        EntradaDTO entradaNominadaDTO = executeTransactional(em -> {
            Entrada entradaAActualizar = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con ID: " + idEntrada));

            Festival festival = obtenerFestivalDesdeEntrada(entradaAActualizar);
            verificarPermisoSobreFestival(em, festival.getIdFestival(), idPromotor);

            if (entradaAActualizar.getAsistente() != null) {
                throw new IllegalStateException("La entrada ID " + idEntrada + " ya está nominada.");
            }
            if (entradaAActualizar.getEstado() != EstadoEntrada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden nominar entradas en estado ACTIVA.");
            }

            Asistente asistenteNominado = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistente);

            entradaAActualizar.setAsistente(asistenteNominado);
            entradaAActualizar.setFechaAsignacion(LocalDateTime.now());

            return entradaMapper.entradaToEntradaDTO(entradaRepository.save(em, entradaAActualizar));
        }, "nominarEntrada (por ID) " + idEntrada);

        enviarEmailNominacionSiProcede(asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistente), entradaNominadaDTO, "nominarEntrada (por ID)");

        return entradaNominadaDTO;
    }

    @Override
    public EntradaDTO nominarEntradaPorQr(String codigoQr, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistenteNominado) {
        log.info("Service: Nominando entrada por QR para asistente {}", emailAsistenteNominado);
        if (codigoQr == null || codigoQr.isBlank() || emailAsistenteNominado == null || emailAsistenteNominado.isBlank() || nombreAsistenteNominado == null || nombreAsistenteNominado.isBlank()) {
            throw new IllegalArgumentException("Código QR, email y nombre del asistente son requeridos.");
        }

        EntradaDTO entradaNominadaDTO = executeTransactional(em -> {
            Entrada entradaAActualizar = entradaRepository.findByCodigoQr(em, codigoQr)
                    .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con código QR proporcionado."));

            if (entradaAActualizar.getAsistente() != null) {
                throw new IllegalStateException("La entrada ya está nominada.");
            }
            if (entradaAActualizar.getEstado() != EstadoEntrada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden nominar entradas en estado ACTIVA.");
            }

            Asistente asistenteNominado = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistenteNominado);
            entradaAActualizar.setAsistente(asistenteNominado);
            entradaAActualizar.setFechaAsignacion(LocalDateTime.now());

            return entradaMapper.entradaToEntradaDTO(entradaRepository.save(em, entradaAActualizar));
        }, "nominarEntradaPorQr " + codigoQr);

        enviarEmailNominacionSiProcede(asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistenteNominado), entradaNominadaDTO, "nominarEntradaPorQr");

        return entradaNominadaDTO;
    }

    @Override
    public List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor) {
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival e ID de promotor son requeridos.");
        }
        return executeRead(em -> {
            verificarPermisoSobreFestival(em, idFestival, idPromotor);
            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);
            log.info("Encontradas {} entradas para festival ID {}", entradas.size(), idFestival);
            return entradaMapper.toEntradaDTOList(entradas);
        }, "obtenerEntradasPorFestival " + idFestival);
    }

    @Override
    public void cancelarEntrada(Integer idEntrada, Integer idPromotor) {
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada e ID de promotor son requeridos.");
        }

        executeTransactional(em -> {
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con ID: " + idEntrada));

            verificarPermisoSobreFestival(em, obtenerFestivalDesdeEntrada(entrada).getIdFestival(), idPromotor);

            if (entrada.getEstado() != EstadoEntrada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden cancelar entradas en estado ACTIVA.");
            }

            entrada.setEstado(EstadoEntrada.CANCELADA);
            entradaRepository.save(em, entrada);

            TipoEntrada tipoEntrada = obtenerTipoEntradaDesdeEntrada(entrada);
            em.lock(tipoEntrada, LockModeType.PESSIMISTIC_WRITE);
            tipoEntrada.setStock(tipoEntrada.getStock() + 1);
            tipoEntradaRepository.save(em, tipoEntrada);
            log.info("Stock incrementado para TipoEntrada ID {}. Nuevo stock: {}", tipoEntrada.getIdTipoEntrada(), tipoEntrada.getStock());
            return null;
        }, "cancelarEntrada " + idEntrada);
    }

    @Override
    public Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor) {
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("IDs de entrada y promotor son requeridos.");
        }
        return executeRead(em -> {
            Optional<Entrada> entradaOpt = entradaRepository.findById(em, idEntrada);
            if (entradaOpt.isEmpty()) {
                return Optional.empty();
            }
            Entrada entrada = entradaOpt.get();
            verificarPermisoSobreFestival(em, obtenerFestivalDesdeEntrada(entrada).getIdFestival(), idPromotor);
            return Optional.of(entradaMapper.entradaToEntradaDTO(entrada));
        }, "obtenerEntradaPorId " + idEntrada);
    }

    @Override
    public Optional<EntradaDTO> obtenerParaNominacionPublicaPorQr(String codigoQr) {
        if (codigoQr == null || codigoQr.isBlank()) {
            return Optional.empty();
        }
        return executeRead(em
                -> entradaRepository.findByCodigoQr(em, codigoQr).map(entradaMapper::entradaToEntradaDTO),
                "obtenerParaNominacionPublicaPorQr " + codigoQr
        );
    }

    private void enviarEmailNominacionSiProcede(Asistente asistente, EntradaDTO entradaDTO, String metodoOrigen) {
        if (asistente == null || entradaDTO == null) {
            log.warn("Service - {}: No se enviará email de nominación por datos nulos.", metodoOrigen);
            return;
        }
        try {
            log.info("Service - {}: Enviando email de nominación a {}", metodoOrigen, asistente.getEmail());
            emailService.enviarEmailEntradaNominada(asistente.getEmail(), asistente.getNombre(), entradaDTO);
        } catch (Exception e) {
            log.error("Service - {}: Falló el envío de email para entrada ID {} a {}: {}", metodoOrigen, entradaDTO.getIdEntrada(), asistente.getEmail(), e.getMessage(), e);
        }
    }

    private Festival obtenerFestivalDesdeEntrada(Entrada entrada) {
        return Optional.ofNullable(entrada)
                .map(Entrada::getCompraEntrada)
                .map(CompraEntrada::getTipoEntrada)
                .map(TipoEntrada::getFestival)
                .orElseThrow(() -> new IllegalStateException("Inconsistencia de datos: no se pudo obtener el festival desde la entrada ID " + (entrada != null ? entrada.getIdEntrada() : "null")));
    }

    private TipoEntrada obtenerTipoEntradaDesdeEntrada(Entrada entrada) {
        return Optional.ofNullable(entrada)
                .map(Entrada::getCompraEntrada)
                .map(CompraEntrada::getTipoEntrada)
                .orElseThrow(() -> new IllegalStateException("Inconsistencia de datos: no se pudo obtener el tipo de entrada desde la entrada ID " + (entrada != null ? entrada.getIdEntrada() : "null")));
    }
}
