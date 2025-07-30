package com.beatpass.service;

import com.beatpass.dto.EntradaDTO;
import com.beatpass.exception.EntradaNotFoundException;
import com.beatpass.mapper.EntradaMapper;
import com.beatpass.model.*;
import com.beatpass.repository.EntradaRepository;
import com.beatpass.repository.TipoEntradaRepository;
import com.beatpass.util.PermissionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de entradas individuales,
 * refactorizada para usar CDI y JTA.
 */
@ApplicationScoped
public class EntradaServiceImpl implements EntradaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaServiceImpl.class);

    @Inject
    private EntradaRepository entradaRepository;
    @Inject
    private TipoEntradaRepository tipoEntradaRepository;
    @Inject
    private AsistenteService asistenteService;
    @Inject
    private EmailService emailService;
    @Inject
    private PermissionService permissionService;
    @Inject
    private EntradaMapper entradaMapper;

    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;

    @Override
    @Transactional
    public EntradaDTO nominarEntrada(Integer idEntrada, String emailAsistente, String nombreAsistente, String telefonoAsistente, Integer idPromotor) {
        log.info("Service: Nominando entrada ID {} para asistente {} por Promotor ID {}", idEntrada, emailAsistente, idPromotor);
        if (idEntrada == null || emailAsistente == null || emailAsistente.isBlank() || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada, email de asistente y ID de promotor son requeridos.");
        }

        Entrada entradaAActualizar = entradaRepository.findById(idEntrada)
                .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con ID: " + idEntrada));

        Festival festival = obtenerFestivalDesdeEntrada(entradaAActualizar);
        permissionService.verificarPermisoSobreFestival(festival.getIdFestival(), idPromotor);

        if (entradaAActualizar.getAsistente() != null) {
            throw new IllegalStateException("La entrada ID " + idEntrada + " ya está nominada.");
        }
        if (entradaAActualizar.getEstado() != EstadoEntrada.ACTIVA) {
            throw new IllegalStateException("Solo se pueden nominar entradas en estado ACTIVA.");
        }

        Asistente asistenteNominado = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistente, nombreAsistente, telefonoAsistente);
        entradaAActualizar.setAsistente(asistenteNominado);
        entradaAActualizar.setFechaAsignacion(LocalDateTime.now());

        EntradaDTO entradaNominadaDTO = entradaMapper.entradaToEntradaDTO(entradaRepository.save(entradaAActualizar));

        enviarEmailNominacionSiProcede(asistenteNominado, entradaNominadaDTO, "nominarEntrada (por ID)");

        return entradaNominadaDTO;
    }

    @Override
    @Transactional
    public EntradaDTO nominarEntradaPorQr(String codigoQr, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistenteNominado) {
        log.info("Service: Nominando entrada por QR para asistente {}", emailAsistenteNominado);
        if (codigoQr == null || codigoQr.isBlank() || emailAsistenteNominado == null || emailAsistenteNominado.isBlank() || nombreAsistenteNominado == null || nombreAsistenteNominado.isBlank()) {
            throw new IllegalArgumentException("Código QR, email y nombre del asistente son requeridos.");
        }

        Entrada entradaAActualizar = entradaRepository.findByCodigoQr(codigoQr)
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

        EntradaDTO entradaNominadaDTO = entradaMapper.entradaToEntradaDTO(entradaRepository.save(entradaAActualizar));

        enviarEmailNominacionSiProcede(asistenteNominado, entradaNominadaDTO, "nominarEntradaPorQr");

        return entradaNominadaDTO;
    }

    @Override
    public List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor) {
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival e ID de promotor son requeridos.");
        }
        permissionService.verificarPermisoSobreFestival(idFestival, idPromotor);
        List<Entrada> entradas = entradaRepository.findByFestivalId(idFestival);
        log.info("Encontradas {} entradas para festival ID {}", entradas.size(), idFestival);
        return entradaMapper.toEntradaDTOList(entradas);
    }

    @Override
    @Transactional
    public void cancelarEntrada(Integer idEntrada, Integer idPromotor) {
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada e ID de promotor son requeridos.");
        }

        Entrada entrada = entradaRepository.findById(idEntrada)
                .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con ID: " + idEntrada));

        permissionService.verificarPermisoSobreFestival(obtenerFestivalDesdeEntrada(entrada).getIdFestival(), idPromotor);

        if (entrada.getEstado() != EstadoEntrada.ACTIVA) {
            throw new IllegalStateException("Solo se pueden cancelar entradas en estado ACTIVA.");
        }

        entrada.setEstado(EstadoEntrada.CANCELADA);
        entradaRepository.save(entrada);

        TipoEntrada tipoEntrada = obtenerTipoEntradaDesdeEntrada(entrada);
        em.lock(tipoEntrada, LockModeType.PESSIMISTIC_WRITE); // Lógica de bloqueo pesimista preservada
        tipoEntrada.setStock(tipoEntrada.getStock() + 1);
        tipoEntradaRepository.save(tipoEntrada);

        log.info("Stock incrementado para TipoEntrada ID {}. Nuevo stock: {}", tipoEntrada.getIdTipoEntrada(), tipoEntrada.getStock());
    }

    @Override
    public Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor) {
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("IDs de entrada y promotor son requeridos.");
        }

        Optional<Entrada> entradaOpt = entradaRepository.findById(idEntrada);
        if (entradaOpt.isEmpty()) {
            return Optional.empty();
        }
        Entrada entrada = entradaOpt.get();
        permissionService.verificarPermisoSobreFestival(obtenerFestivalDesdeEntrada(entrada).getIdFestival(), idPromotor);
        return Optional.of(entradaMapper.entradaToEntradaDTO(entrada));
    }

    @Override
    public Optional<EntradaDTO> obtenerParaNominacionPublicaPorQr(String codigoQr) {
        if (codigoQr == null || codigoQr.isBlank()) {
            return Optional.empty();
        }
        return entradaRepository.findByCodigoQr(codigoQr)
                .map(entradaMapper::entradaToEntradaDTO);
    }

    // --- MÉTODOS PRIVADOS  ---
    /**
     * Envía un email de notificación al asistente recién nominado, si procede.
     * Captura y registra cualquier excepción para no interrumpir el flujo
     * principal.
     *
     * @param asistente El asistente al que se le enviará el correo.
     * @param entradaDTO El DTO de la entrada nominada.
     * @param metodoOrigen El nombre del método que invoca el envío para
     * trazabilidad en logs.
     */
    private void enviarEmailNominacionSiProcede(Asistente asistente, EntradaDTO entradaDTO, String metodoOrigen) {
        if (asistente == null || entradaDTO == null) {
            log.warn("Service - {}: No se enviará email de nominación por datos nulos.", metodoOrigen);
            return;
        }
        try {
            log.info("Service - {}: Enviando email de nominación a {}", metodoOrigen, asistente.getEmail());
            // Asumiendo que la firma de este método fue actualizada para no requerir el EntityManager
            emailService.enviarEmailEntradaNominada(asistente.getEmail(), asistente.getNombre(), entradaDTO);
        } catch (Exception e) {
            log.error("Service - {}: Falló el envío de email para entrada ID {} a {}: {}", metodoOrigen, entradaDTO.getIdEntrada(), asistente.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Obtiene la entidad Festival a partir de una entidad Entrada, navegando a
     * través de las relaciones.
     *
     * @param entrada La entrada de la cual obtener el festival.
     * @return La entidad Festival asociada.
     * @throws IllegalStateException si no se puede resolver el festival debido
     * a inconsistencias en los datos.
     */
    private Festival obtenerFestivalDesdeEntrada(Entrada entrada) {
        return Optional.ofNullable(entrada)
                .map(Entrada::getCompraEntrada)
                .map(CompraEntrada::getTipoEntrada)
                .map(TipoEntrada::getFestival)
                .orElseThrow(() -> new IllegalStateException("Inconsistencia de datos: no se pudo obtener el festival desde la entrada ID " + (entrada != null ? entrada.getIdEntrada() : "null")));
    }

    /**
     * Obtiene la entidad TipoEntrada a partir de una entidad Entrada, navegando
     * a través de las relaciones.
     *
     * @param entrada La entrada de la cual obtener el tipo de entrada.
     * @return La entidad TipoEntrada asociada.
     * @throws IllegalStateException si no se puede resolver el tipo de entrada.
     */
    private TipoEntrada obtenerTipoEntradaDesdeEntrada(Entrada entrada) {
        return Optional.ofNullable(entrada)
                .map(Entrada::getCompraEntrada)
                .map(CompraEntrada::getTipoEntrada)
                .orElseThrow(() -> new IllegalStateException("Inconsistencia de datos: no se pudo obtener el tipo de entrada desde la entrada ID " + (entrada != null ? entrada.getIdEntrada() : "null")));
    }
}
