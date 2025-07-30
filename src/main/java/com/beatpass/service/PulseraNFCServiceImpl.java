package com.beatpass.service;

import com.beatpass.dto.PulseraNFCDTO;
import com.beatpass.exception.*;
import com.beatpass.mapper.PulseraNFCMapper;
import com.beatpass.model.*;
import com.beatpass.repository.*;
import com.beatpass.util.PermissionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de Pulseras NFC, refactorizada
 * para usar CDI y JTA. Gestiona la asociación con entradas, recargas de saldo y
 * consumos.
 */
@ApplicationScoped
public class PulseraNFCServiceImpl implements PulseraNFCService {

    private static final Logger log = LoggerFactory.getLogger(PulseraNFCServiceImpl.class);

    @Inject
    private PulseraNFCRepository pulseraNFCRepository;
    @Inject
    private EntradaRepository entradaRepository;
    @Inject
    private RecargaRepository recargaRepository;
    @Inject
    private ConsumoRepository consumoRepository;
    @Inject
    private PermissionService permissionService;
    @Inject
    private PulseraNFCMapper pulseraNFCMapper;

    @PersistenceContext(unitName = "beatpassPersistenceUnit")
    private EntityManager em;

    @Override
    @Transactional
    public PulseraNFCDTO asociarPulseraEntrada(String codigoUid, Integer idEntrada, Integer idActor) {
        log.info("Service: Asociando pulsera UID {} a entrada ID {} por actor ID {}", codigoUid, idEntrada, idActor);
        if (codigoUid == null || codigoUid.isBlank() || idEntrada == null || idActor == null) {
            throw new IllegalArgumentException("UID de pulsera, ID de entrada y ID de actor son requeridos.");
        }

        Entrada entrada = entradaRepository.findById(idEntrada)
                .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con ID: " + idEntrada));

        validarEstadoEntradaParaAsociacion(entrada);

        Festival festival = obtenerFestivalDesdeEntrada(entrada);
        permissionService.verificarPermisoSobreFestival(festival.getIdFestival(), idActor);

        return asociarPulsera(codigoUid, entrada, festival);
    }

    @Override
    public Optional<PulseraNFCDTO> obtenerPulseraPorId(Integer idPulsera, Integer idActor) {
        if (idPulsera == null || idActor == null) {
            return Optional.empty();
        }
        return pulseraNFCRepository.findById(idPulsera)
                .map(pulsera -> {
                    permissionService.verificarPermisoSobreFestival(pulsera.getFestival().getIdFestival(), idActor);
                    return pulseraNFCMapper.pulseraNFCToPulseraNFCDTO(pulsera);
                });
    }

    @Override
    public Optional<PulseraNFCDTO> obtenerPulseraPorCodigoUid(String codigoUid, Integer idActor) {
        if (codigoUid == null || codigoUid.isBlank() || idActor == null) {
            return Optional.empty();
        }
        return pulseraNFCRepository.findByCodigoUid(codigoUid)
                .map(pulsera -> {
                    permissionService.verificarPermisoSobreFestival(pulsera.getFestival().getIdFestival(), idActor);
                    return pulseraNFCMapper.pulseraNFCToPulseraNFCDTO(pulsera);
                });
    }

    @Override
    public BigDecimal obtenerSaldo(Integer idPulsera, Integer idActor) {
        return obtenerPulseraPorId(idPulsera, idActor)
                .map(PulseraNFCDTO::getSaldo)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public List<PulseraNFCDTO> obtenerPulserasPorFestival(Integer idFestival, Integer idActor) {
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID de festival y ID de actor requeridos.");
        }
        permissionService.verificarPermisoSobreFestival(idFestival, idActor);
        List<PulseraNFC> pulseras = pulseraNFCRepository.findByFestivalId(idFestival);
        return pulseraNFCMapper.toPulseraNFCDTOList(pulseras);
    }

    @Override
    @Transactional
    public PulseraNFCDTO registrarRecarga(String codigoUid, BigDecimal monto, String metodoPago, Integer idUsuarioCajero, Integer idFestival) {
        log.info("Service: Registrando recarga de {} en pulsera UID {} por cajero ID {}", monto, codigoUid, idUsuarioCajero);
        if (codigoUid == null || codigoUid.isBlank() || idUsuarioCajero == null || idFestival == null || monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Datos de recarga inválidos.");
        }

        permissionService.verificarPermisoSobreFestival(idFestival, idUsuarioCajero);

        PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(codigoUid)
                .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
        em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

        if (!idFestival.equals(pulsera.getFestival().getIdFestival())) {
            throw new SecurityException("La pulsera no pertenece al festival especificado.");
        }
        if (!Boolean.TRUE.equals(pulsera.getActiva())) {
            throw new IllegalStateException("La pulsera no está activa.");
        }

        Recarga recarga = new Recarga();
        recarga.setPulseraNFC(pulsera);
        recarga.setMonto(monto);
        recarga.setMetodoPago(metodoPago);
        recarga.setUsuarioCajero(em.getReference(Usuario.class, idUsuarioCajero));
        recargaRepository.save(recarga);

        pulsera.setSaldo(pulsera.getSaldo().add(monto));
        pulsera = pulseraNFCRepository.save(pulsera);
        return pulseraNFCMapper.pulseraNFCToPulseraNFCDTO(pulsera);
    }

    @Override
    @Transactional
    public PulseraNFCDTO registrarConsumo(String codigoUid, BigDecimal monto, String descripcion, Integer idFestival, Integer idPuntoVenta, Integer idActor) {
        log.info("Service: Registrando consumo de {} en pulsera UID {} por actor ID {}", monto, codigoUid, idActor);
        if (codigoUid == null || idFestival == null || idActor == null || monto == null || monto.compareTo(BigDecimal.ZERO) <= 0 || descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Datos de consumo inválidos.");
        }

        permissionService.verificarPermisoSobreFestival(idFestival, idActor);

        PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(codigoUid)
                .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
        em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

        if (!idFestival.equals(pulsera.getFestival().getIdFestival())) {
            throw new SecurityException("La pulsera no pertenece al festival especificado.");
        }
        if (!Boolean.TRUE.equals(pulsera.getActiva())) {
            throw new IllegalStateException("La pulsera no está activa.");
        }
        if (pulsera.getSaldo().compareTo(monto) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente (" + pulsera.getSaldo() + ") para realizar el consumo de " + monto + ".");
        }

        Consumo consumo = new Consumo();
        consumo.setPulseraNFC(pulsera);
        consumo.setMonto(monto);
        consumo.setDescripcion(descripcion);
        consumo.setFestival(em.getReference(Festival.class, idFestival));
        consumo.setIdPuntoVenta(idPuntoVenta);
        consumoRepository.save(consumo);

        pulsera.setSaldo(pulsera.getSaldo().subtract(monto));
        pulsera = pulseraNFCRepository.save(pulsera);
        return pulseraNFCMapper.pulseraNFCToPulseraNFCDTO(pulsera);
    }

    @Override
    @Transactional
    public PulseraNFCDTO asociarPulseraViaQrEntrada(String codigoQrEntrada, String codigoUidPulsera, Integer idFestivalContexto) {
        log.info("Service: Asociando pulsera UID {} a entrada con QR (contexto Fest. ID: {})", codigoUidPulsera, idFestivalContexto);
        if (codigoQrEntrada == null || codigoQrEntrada.isBlank() || codigoUidPulsera == null || codigoUidPulsera.isBlank()) {
            throw new IllegalArgumentException("El código QR de la entrada y el UID de la pulsera son requeridos.");
        }

        Entrada entrada = entradaRepository.findByCodigoQr(codigoQrEntrada)
                .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con el código QR proporcionado."));

        validarEstadoEntradaParaAsociacion(entrada);

        Festival festival = obtenerFestivalDesdeEntrada(entrada);

        if (idFestivalContexto != null && !festival.getIdFestival().equals(idFestivalContexto)) {
            throw new SecurityException("La entrada no pertenece al festival del contexto.");
        }
        if (festival.getEstado() != EstadoFestival.PUBLICADO) {
            throw new IllegalStateException("Solo se pueden asociar pulseras para festivales PUBLICADOS.");
        }

        return asociarPulsera(codigoUidPulsera, entrada, festival);
    }

    // --- MÉTODOS PRIVADOS ORIGINALES PRESERVADOS ---
    private PulseraNFCDTO asociarPulsera(String codigoUid, Entrada entrada, Festival festival) {
        Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findByCodigoUid(codigoUid);
        PulseraNFC pulsera;

        if (pulseraOpt.isPresent()) {
            pulsera = pulseraOpt.get();
            em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);
            validarEstadoPulseraParaAsociacion(pulsera, entrada);
            if (pulsera.getFestival() != null && !pulsera.getFestival().equals(festival)) {
                throw new SecurityException("La pulsera pertenece a un festival diferente.");
            }
        } else {
            pulsera = new PulseraNFC();
            pulsera.setCodigoUid(codigoUid);
            pulsera.setSaldo(BigDecimal.ZERO);
            pulsera.setActiva(true);
        }

        pulsera.setFestival(festival);
        pulsera.setEntrada(entrada);
        pulsera.setFechaAsociacion(LocalDateTime.now());
        pulsera = pulseraNFCRepository.save(pulsera);

        entrada.setFechaUso(LocalDateTime.now());
        entrada.setEstado(EstadoEntrada.USADA);
        entradaRepository.save(entrada);
        log.info("Entrada ID {} marcada como USADA. Pulsera ID {} asociada.", entrada.getIdEntrada(), pulsera.getIdPulsera());

        return pulseraNFCMapper.pulseraNFCToPulseraNFCDTO(pulsera);
    }

    private void validarEstadoEntradaParaAsociacion(Entrada entrada) {
        if (entrada.getEstado() != EstadoEntrada.ACTIVA) {
            throw new IllegalStateException("La entrada ID " + entrada.getIdEntrada() + " no está activa.");
        }
        if (Boolean.TRUE.equals(entrada.getCompraEntrada().getTipoEntrada().getRequiereNominacion()) && entrada.getAsistente() == null) {
            throw new EntradaNoNominadaException("La entrada ID " + entrada.getIdEntrada() + " debe estar nominada.");
        }
    }

    private void validarEstadoPulseraParaAsociacion(PulseraNFC pulsera, Entrada nuevaEntrada) {
        if (!Boolean.TRUE.equals(pulsera.getActiva())) {
            throw new IllegalStateException("La pulsera con UID " + pulsera.getCodigoUid() + " no está activa.");
        }
        if (pulsera.getEntrada() != null && !pulsera.getEntrada().getIdEntrada().equals(nuevaEntrada.getIdEntrada())) {
            throw new PulseraYaAsociadaException("La pulsera UID " + pulsera.getCodigoUid() + " ya está asociada a otra entrada.");
        }
    }

    private Festival obtenerFestivalDesdeEntrada(Entrada entrada) {
        return Optional.ofNullable(entrada)
                .map(Entrada::getCompraEntrada)
                .map(CompraEntrada::getTipoEntrada)
                .map(TipoEntrada::getFestival)
                .orElseThrow(() -> new IllegalStateException("Inconsistencia de datos: no se pudo obtener el festival desde la entrada ID " + (entrada != null ? entrada.getIdEntrada() : "null")));
    }
}
