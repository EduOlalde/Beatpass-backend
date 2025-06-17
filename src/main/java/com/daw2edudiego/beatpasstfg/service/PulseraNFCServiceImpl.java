package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de PulseraNFCService.
 */
public class PulseraNFCServiceImpl implements PulseraNFCService {

    private static final Logger log = LoggerFactory.getLogger(PulseraNFCServiceImpl.class);

    private final PulseraNFCRepository pulseraNFCRepository;
    private final EntradaRepository entradaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    private final RecargaRepository recargaRepository;
    private final ConsumoRepository consumoRepository;

    public PulseraNFCServiceImpl() {
        this.pulseraNFCRepository = new PulseraNFCRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.recargaRepository = new RecargaRepositoryImpl();
        this.consumoRepository = new ConsumoRepositoryImpl();
    }

    @Override
    public PulseraNFCDTO asociarPulseraEntrada(String codigoUid, Integer idEntrada, Integer idActor) {
        log.info("Service: Asociando pulsera UID {} a entrada ID {} por actor ID {}", codigoUid, idEntrada, idActor);

        if (codigoUid == null || codigoUid.isBlank() || idEntrada == null || idActor == null) {
            throw new IllegalArgumentException("UID de pulsera, ID de entrada y ID de actor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario actor = verificarActorPermitido(em, idActor, RolUsuario.ADMIN, RolUsuario.PROMOTOR, RolUsuario.CAJERO);

            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con ID: " + idEntrada));

            // Validar si la entrada está nominada y activa
            validarEstadoEntradaParaAsociacion(entrada);

            Festival festival = obtenerFestivalDesdeEntradaAsignada(entrada);

            // Verificar que el promotor (si es el actor) sea dueño del festival
            if (actor.getRol() == RolUsuario.PROMOTOR) {
                verificarPropiedadFestival(festival, idActor);
            }

            // Verificar si la entrada ya tiene otra pulsera asociada
            Optional<PulseraNFC> pulseraExistenteParaEntrada = pulseraNFCRepository.findByEntradaId(em, idEntrada);
            if (pulseraExistenteParaEntrada.isPresent() && !pulseraExistenteParaEntrada.get().getCodigoUid().equals(codigoUid)) {
                throw new IllegalStateException("La entrada ID " + idEntrada + " ya tiene otra pulsera asociada (UID: " + pulseraExistenteParaEntrada.get().getCodigoUid() + ").");
            }

            Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findByCodigoUid(em, codigoUid);
            PulseraNFC pulsera;

            if (pulseraOpt.isPresent()) {
                pulsera = pulseraOpt.get();
                log.debug("Pulsera encontrada con UID {}. Verificando estado y festival.", codigoUid);
                em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE); // Bloquear para evitar condiciones de carrera
                validarEstadoPulseraParaAsociacion(pulsera, entrada);

                if (pulsera.getFestival() == null) {
                    log.warn("Inconsistencia: Pulsera existente ID {} (UID {}) no tiene festival asociado. Asociando al festival ID {}.",
                            pulsera.getIdPulsera(), codigoUid, festival.getIdFestival());
                    pulsera.setFestival(festival);
                } else if (!pulsera.getFestival().getIdFestival().equals(festival.getIdFestival())) {
                    log.error("Conflicto: Pulsera UID {} pertenece al festival ID {} pero se intenta asociar a entrada del festival ID {}",
                            codigoUid, pulsera.getFestival().getIdFestival(), festival.getIdFestival());
                    throw new SecurityException("Esta pulsera pertenece a otro festival y no puede ser asociada a esta entrada.");
                }
            } else {
                log.debug("Pulsera con UID {} no encontrada. Creando nueva instancia.", codigoUid);
                pulsera = new PulseraNFC();
                pulsera.setCodigoUid(codigoUid);
                pulsera.setSaldo(BigDecimal.ZERO);
                pulsera.setActiva(true);
                pulsera.setFestival(festival);
            }

            pulsera.setEntrada(entrada);
            pulsera.setFechaAsociacion(LocalDateTime.now());
            pulsera = pulseraNFCRepository.save(em, pulsera);

            // Actualizar Entrada: fecha_uso y estado
            entrada.setFechaUso(LocalDateTime.now());
            entrada.setEstado(EstadoEntrada.USADA);
            entradaRepository.save(em, entrada);
            log.info("Entrada ID {} marcada como USADA y fecha_uso actualizada a {}", entrada.getIdEntrada(), entrada.getFechaUso());

            tx.commit();
            log.info("Pulsera UID {} (ID: {}) asociada correctamente a Entrada ID {} del Festival ID {}. Fecha de uso de entrada actualizada.",
                    pulsera.getCodigoUid(), pulsera.getIdPulsera(), idEntrada, festival.getIdFestival());

            return mapEntityToDto(pulsera);

        } catch (Exception e) {
            handleException(e, tx, "asociando pulsera UID " + codigoUid + " a entrada ID " + idEntrada);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<PulseraNFCDTO> obtenerPulseraPorId(Integer idPulsera, Integer idActor) {
        log.debug("Service: Obteniendo pulsera ID {} por actor ID {}", idPulsera, idActor);
        if (idPulsera == null || idActor == null) {
            throw new IllegalArgumentException("ID pulsera y ID actor requeridos.");
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findById(em, idPulsera);
            if (pulseraOpt.isEmpty()) {
                return Optional.empty();
            }
            PulseraNFC pulsera = pulseraOpt.get();
            verificarPermisoLecturaPulsera(em, pulsera, idActor);
            return Optional.of(mapEntityToDto(pulsera));
        } catch (Exception e) {
            log.error("Error obteniendo pulsera ID {}: {}", idPulsera, e.getMessage(), e);
            if (e instanceof SecurityException) {
                return Optional.empty();
            }
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<PulseraNFCDTO> obtenerPulseraPorCodigoUid(String codigoUid, Integer idActor) {
        log.debug("Service: Obteniendo pulsera UID {} por actor ID {}", codigoUid, idActor);
        if (codigoUid == null || codigoUid.isBlank() || idActor == null) {
            throw new IllegalArgumentException("UID pulsera y ID actor requeridos.");
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findByCodigoUid(em, codigoUid);
            if (pulseraOpt.isEmpty()) {
                return Optional.empty();
            }
            PulseraNFC pulsera = pulseraOpt.get();
            verificarPermisoLecturaPulsera(em, pulsera, idActor);
            return Optional.of(mapEntityToDto(pulsera));
        } catch (Exception e) {
            log.error("Error obteniendo pulsera UID {}: {}", codigoUid, e.getMessage(), e);
            if (e instanceof SecurityException) {
                return Optional.empty();
            }
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public BigDecimal obtenerSaldo(Integer idPulsera, Integer idActor) {
        log.debug("Service: Obteniendo saldo pulsera ID {} por actor ID {}", idPulsera, idActor);
        PulseraNFCDTO dto = obtenerPulseraPorId(idPulsera, idActor)
                .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada o sin permiso de acceso: " + idPulsera));
        return dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO;
    }

    @Override
    public List<PulseraNFCDTO> obtenerPulserasPorFestival(Integer idFestival, Integer idActor) {
        log.debug("Service: Obteniendo pulseras para festival ID {} por actor ID {}", idFestival, idActor);
        if (idFestival == null || idActor == null) {
            throw new IllegalArgumentException("ID festival y ID actor requeridos.");
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            Usuario actor = usuarioRepository.findById(em, idActor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado: " + idActor));
            verificarPermisoListadoPulserasFestival(em, idFestival, actor);
            List<PulseraNFC> pulseras = pulseraNFCRepository.findByFestivalId(em, idFestival);
            return pulseras.stream().map(this::mapEntityToDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo pulseras para festival ID {}: {}", idFestival, e.getMessage(), e);
            if (e instanceof SecurityException || e instanceof FestivalNotFoundException) {
                return Collections.emptyList();
            }
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public PulseraNFCDTO registrarRecarga(String codigoUid, BigDecimal monto, String metodoPago, Integer idUsuarioCajero, Integer idFestival) {
        log.info("Service: Registrando recarga de {} ({}) en pulsera UID {} (Fest:{}) por cajero ID {}",
                monto, metodoPago, codigoUid, idFestival, idUsuarioCajero);

        if (codigoUid == null || codigoUid.isBlank() || idUsuarioCajero == null || idFestival == null) {
            throw new IllegalArgumentException("UID pulsera, ID cajero y ID festival son requeridos.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la recarga debe ser positivo.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario cajero = verificarActorPermitido(em, idUsuarioCajero, RolUsuario.ADMIN, RolUsuario.PROMOTOR, RolUsuario.CAJERO);

            PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(em, codigoUid)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
            em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

            if (pulsera.getFestival() == null || !pulsera.getFestival().getIdFestival().equals(idFestival)) {
                log.error("Intento de recargar pulsera UID {} (Fest:{}) en contexto de festival ID {}",
                        codigoUid, (pulsera.getFestival() != null ? pulsera.getFestival().getIdFestival() : "NULL"), idFestival);
                throw new SecurityException("La pulsera no pertenece al festival especificado (" + idFestival + ").");
            }
            if (cajero.getRol() == RolUsuario.PROMOTOR) {
                verificarPropiedadFestival(pulsera.getFestival(), idUsuarioCajero);
            }
            if (!Boolean.TRUE.equals(pulsera.getActiva())) {
                throw new IllegalStateException("La pulsera UID " + codigoUid + " no está activa.");
            }

            Recarga recarga = new Recarga();
            recarga.setPulseraNFC(pulsera);
            recarga.setMonto(monto);
            recarga.setMetodoPago(metodoPago != null ? metodoPago.trim() : null);
            recarga.setUsuarioCajero(cajero);
            recargaRepository.save(em, recarga);

            BigDecimal saldoActual = pulsera.getSaldo() != null ? pulsera.getSaldo() : BigDecimal.ZERO;
            pulsera.setSaldo(saldoActual.add(monto));
            pulsera = pulseraNFCRepository.save(em, pulsera);

            tx.commit();
            log.info("Recarga de {} realizada en pulsera UID {} (Fest:{}). Nuevo saldo: {}", monto, codigoUid, idFestival, pulsera.getSaldo());

            return mapEntityToDto(pulsera);

        } catch (Exception e) {
            handleException(e, tx, "registrando recarga para UID " + codigoUid);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public PulseraNFCDTO registrarConsumo(String codigoUid, BigDecimal monto, String descripcion, Integer idFestival, Integer idPuntoVenta, Integer idActor) {
        log.info("Service: Registrando consumo de {} ('{}') en pulsera UID {} para festival ID {} por actor ID {}",
                monto, descripcion, codigoUid, idFestival, idActor);

        if (codigoUid == null || codigoUid.isBlank() || idFestival == null || idActor == null) {
            throw new IllegalArgumentException("UID pulsera, ID festival y ID actor son requeridos.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del consumo debe ser positivo.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción del consumo es requerida.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario actor = verificarActorPermitido(em, idActor, RolUsuario.ADMIN, RolUsuario.PROMOTOR, RolUsuario.CAJERO);

            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado: " + idFestival));

            PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(em, codigoUid)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
            em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

            if (pulsera.getFestival() == null || !pulsera.getFestival().getIdFestival().equals(idFestival)) {
                log.error("Intento de registrar consumo para festival ID {} con pulsera UID {} que pertenece a festival ID {}",
                        idFestival, codigoUid, (pulsera.getFestival() != null ? pulsera.getFestival().getIdFestival() : "NULL"));
                throw new SecurityException("La pulsera no pertenece al festival especificado (" + idFestival + ").");
            }
            if (actor.getRol() == RolUsuario.PROMOTOR) {
                verificarPropiedadFestival(festival, idActor);
            }
            if (!Boolean.TRUE.equals(pulsera.getActiva())) {
                throw new IllegalStateException("La pulsera UID " + codigoUid + " no está activa.");
            }

            BigDecimal saldoActual = pulsera.getSaldo() != null ? pulsera.getSaldo() : BigDecimal.ZERO;
            if (saldoActual.compareTo(monto) < 0) {
                log.warn("Saldo insuficiente en pulsera UID {}. Saldo: {}, Monto: {}", codigoUid, saldoActual, monto);
                throw new SaldoInsuficienteException("Saldo insuficiente (" + saldoActual + ") para realizar el consumo de " + monto + ".");
            }

            Consumo consumo = new Consumo();
            consumo.setPulseraNFC(pulsera);
            consumo.setMonto(monto);
            consumo.setDescripcion(descripcion.trim());
            consumo.setFestival(festival);
            consumo.setIdPuntoVenta(idPuntoVenta);
            consumoRepository.save(em, consumo);

            pulsera.setSaldo(saldoActual.subtract(monto));
            pulsera = pulseraNFCRepository.save(em, pulsera);

            tx.commit();
            log.info("Consumo de {} registrado en pulsera UID {} (Fest:{}). Nuevo saldo: {}", monto, codigoUid, idFestival, pulsera.getSaldo());

            return mapEntityToDto(pulsera);

        } catch (Exception e) {
            handleException(e, tx, "registrando consumo para UID " + codigoUid);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public PulseraNFCDTO asociarPulseraViaQrEntrada(String codigoQrEntrada, String codigoUidPulsera, Integer idFestivalContexto)
            throws EntradaNotFoundException, PulseraNFCNotFoundException,
            PulseraYaAsociadaException, EntradaNoNominadaException,
            IllegalStateException, FestivalNotFoundException, SecurityException {

        log.info("Service: Iniciando asociación de pulsera UID {} a entrada con QR {} (Contexto Fest. ID: {})",
                codigoUidPulsera, (codigoQrEntrada != null ? codigoQrEntrada.substring(0, Math.min(15, codigoQrEntrada.length())) + "..." : "null"), idFestivalContexto);

        if (codigoQrEntrada == null || codigoQrEntrada.isBlank() || codigoUidPulsera == null || codigoUidPulsera.isBlank()) {
            throw new IllegalArgumentException("El código QR de la entrada y el UID de la pulsera son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Entrada entrada = entradaRepository.findByCodigoQr(em, codigoQrEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con el código QR proporcionado."));
            log.debug("Entrada Asignada ID {} encontrada para QR: {}", entrada.getIdEntrada(), codigoQrEntrada);

            validarEstadoEntradaParaAsociacion(entrada);

            Festival festivalDeLaEntrada = obtenerFestivalDesdeEntradaAsignada(entrada);
            log.debug("Festival ID {} determinado desde la entrada.", festivalDeLaEntrada.getIdFestival());

            if (idFestivalContexto != null && !festivalDeLaEntrada.getIdFestival().equals(idFestivalContexto)) {
                log.warn("Conflicto de festival: Entrada pertenece a Fest.ID {} pero el contexto es Fest.ID {}", festivalDeLaEntrada.getIdFestival(), idFestivalContexto);
                throw new SecurityException("La entrada no pertenece al festival indicado en el contexto de la operación.");
            }
            if (festivalDeLaEntrada.getEstado() != EstadoFestival.PUBLICADO) {
                log.warn("Intento de asociar pulsera para festival '{}' (ID:{}) que no está PUBLICADO. Estado actual: {}",
                        festivalDeLaEntrada.getNombre(), festivalDeLaEntrada.getIdFestival(), festivalDeLaEntrada.getEstado());
                throw new IllegalStateException("Solo se pueden asociar pulseras para festivales que están PUBLICADOS.");
            }

            Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findByCodigoUid(em, codigoUidPulsera);
            PulseraNFC pulsera;

            if (pulseraOpt.isPresent()) {
                pulsera = pulseraOpt.get();
                log.debug("Pulsera existente encontrada con UID {}. ID: {}. Verificando...", codigoUidPulsera, pulsera.getIdPulsera());
                em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

                validarEstadoPulseraParaAsociacion(pulsera, entrada);

                if (pulsera.getFestival() == null) {
                    log.warn("Pulsera existente UID {} no tiene festival asociado. Vinculando a festival ID {}.", codigoUidPulsera, festivalDeLaEntrada.getIdFestival());
                    pulsera.setFestival(festivalDeLaEntrada);
                } else if (!pulsera.getFestival().getIdFestival().equals(festivalDeLaEntrada.getIdFestival())) {
                    throw new SecurityException("La pulsera UID " + codigoUidPulsera + " pertenece a un festival diferente (ID: " + pulsera.getFestival().getIdFestival() + ").");
                }

            } else {
                log.debug("Pulsera con UID {} no encontrada. Creando nueva pulsera para el festival ID {}.", codigoUidPulsera, festivalDeLaEntrada.getIdFestival());
                pulsera = new PulseraNFC();
                pulsera.setCodigoUid(codigoUidPulsera);
                pulsera.setSaldo(BigDecimal.ZERO);
                pulsera.setActiva(true);
                pulsera.setFestival(festivalDeLaEntrada);
            }

            if (entrada.getPulseraAsociada() != null && !entrada.getPulseraAsociada().getCodigoUid().equals(codigoUidPulsera)) {
                log.warn("La entrada ID {} ya estaba asociada a la pulsera UID {}. Se re-asociará a la pulsera UID {}.",
                        entrada.getIdEntrada(), entrada.getPulseraAsociada().getCodigoUid(), codigoUidPulsera);
                PulseraNFC pulseraAntigua = entrada.getPulseraAsociada();
                pulseraAntigua.setEntrada(null);
                pulseraAntigua.setFechaAsociacion(null);
                pulseraNFCRepository.save(em, pulseraAntigua);
            }

            pulsera.setEntrada(entrada);
            pulsera.setFechaAsociacion(LocalDateTime.now());
            PulseraNFC pulseraGuardada = pulseraNFCRepository.save(em, pulsera);

            // Actualizar Entrada: fecha_uso y estado
            entrada.setFechaUso(LocalDateTime.now());
            entrada.setEstado(EstadoEntrada.USADA);
            entradaRepository.save(em, entrada);
            log.info("Entrada ID {} (QR: {}) marcada como USADA y fecha_uso actualizada a {}", entrada.getIdEntrada(), codigoQrEntrada, entrada.getFechaUso());

            tx.commit();
            log.info("Pulsera UID {} (ID: {}) asociada exitosamente a Entrada ID {} (QR: {}) del Festival ID {}. Fecha de uso de entrada actualizada.",
                    pulseraGuardada.getCodigoUid(), pulseraGuardada.getIdPulsera(),
                    entrada.getIdEntrada(), codigoQrEntrada,
                    festivalDeLaEntrada.getIdFestival());

            return mapEntityToDto(pulseraGuardada);

        } catch (Exception e) {
            handleException(e, tx, "asociar pulsera UID " + codigoUidPulsera + " a entrada con QR " + codigoQrEntrada);
            if (e instanceof EntradaNotFoundException || e instanceof PulseraNFCNotFoundException
                    || e instanceof PulseraYaAsociadaException || e instanceof EntradaNoNominadaException
                    || e instanceof IllegalStateException || e instanceof FestivalNotFoundException || e instanceof SecurityException) {
                throw e;
            }
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    private Usuario verificarActorPermitido(EntityManager em, Integer idUsuario, RolUsuario... rolesPermitidos) {
        Usuario actor = usuarioRepository.findById(em, idUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idUsuario));
        boolean permitido = false;
        for (RolUsuario rol : rolesPermitidos) {
            if (actor.getRol() == rol) {
                permitido = true;
                break;
            }
        }
        if (!permitido) {
            log.warn("Usuario ID {} con rol {} intentó realizar una acción no permitida.", idUsuario, actor.getRol());
            throw new SecurityException("Rol de usuario no autorizado para esta acción.");
        }
        return actor;
    }

    private void validarEstadoEntradaParaAsociacion(Entrada entrada) {
        if (entrada.getEstado() != EstadoEntrada.ACTIVA) {
            throw new IllegalStateException("La entrada ID " + entrada.getIdEntrada()
                    + " no está activa (Estado: " + entrada.getEstado() + "). No se puede asociar pulsera.");
        }
        TipoEntrada tipoOriginal = entrada.getCompraEntrada().getTipoEntrada();

        if (Boolean.TRUE.equals(tipoOriginal.getRequiereNominacion()) && entrada.getAsistente() == null) {
            throw new EntradaNoNominadaException("La entrada ID " + entrada.getIdEntrada()
                    + " debe estar nominada a un asistente antes de asociar una pulsera.");
        }
    }

    private void validarEstadoPulseraParaAsociacion(PulseraNFC pulsera, Entrada nuevaEntrada) {
        if (pulsera.getIdPulsera() == null) {
            return;
        }
        if (!Boolean.TRUE.equals(pulsera.getActiva())) {
            throw new IllegalStateException("La pulsera con UID " + pulsera.getCodigoUid() + " no está activa.");
        }
        // Verifica si la pulsera ya está asociada a OTRA entrada activa del MISMO festival
        if (pulsera.getEntrada() != null
                && !pulsera.getEntrada().getIdEntrada().equals(nuevaEntrada.getIdEntrada())
                && pulsera.getEntrada().getEstado() == EstadoEntrada.ACTIVA) {

            Festival festivalPulseraActual = obtenerFestivalDesdeEntradaAsignada(pulsera.getEntrada());
            Festival festivalEntradaNueva = obtenerFestivalDesdeEntradaAsignada(nuevaEntrada);

            if (festivalPulseraActual != null && festivalPulseraActual.equals(festivalEntradaNueva)) {
                throw new PulseraYaAsociadaException("La pulsera UID " + pulsera.getCodigoUid()
                        + " ya está asociada a otra entrada activa (ID: "
                        + pulsera.getEntrada().getIdEntrada() + ") para este festival.");
            }
        }
    }

    private Festival obtenerFestivalDesdeEntradaAsignada(Entrada ea) {
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getTipoEntrada() == null || ea.getCompraEntrada().getTipoEntrada().getFestival() == null) {
            Integer eaId = (ea != null) ? ea.getIdEntrada() : null;
            log.error("Inconsistencia de datos para EntradaAsignada ID {}: no se pudo obtener el festival asociado.", eaId);
            throw new IllegalStateException("Error interno: no se pudo determinar el festival de la entrada ID " + eaId);
        }
        return ea.getCompraEntrada().getTipoEntrada().getFestival();
    }

    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
            throw new IllegalArgumentException("El festival no puede ser nulo.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor no puede ser nulo.");
        }
        if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            log.warn("Intento de acceso no autorizado por promotor ID {} al festival ID {}",
                    idPromotor, festival.getIdFestival());
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
    }

    private void verificarPermisoLecturaPulsera(EntityManager em, PulseraNFC pulsera, Integer idActor) {
        Usuario actor = usuarioRepository.findById(em, idActor)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado: " + idActor));
        if (actor.getRol() == RolUsuario.ADMIN || actor.getRol() == RolUsuario.CAJERO) {
            return;
        }
        if (actor.getRol() == RolUsuario.PROMOTOR) {
            if (pulsera.getFestival() == null) {
                throw new SecurityException("Promotor no puede ver pulseras no asociadas a un festival.");
            }
            try {
                verificarPropiedadFestival(pulsera.getFestival(), idActor);
            } catch (IllegalStateException | SecurityException e) {
                throw new SecurityException("Promotor no tiene permiso para ver esta pulsera.", e);
            }
        } else {
            throw new SecurityException("Rol no autorizado para ver información de pulseras.");
        }
    }

    private void verificarPermisoListadoPulserasFestival(EntityManager em, Integer idFestival, Usuario actor) {
        if (actor.getRol() == RolUsuario.ADMIN) {
            return;
        }
        if (actor.getRol() == RolUsuario.PROMOTOR) {
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado: " + idFestival));
            verificarPropiedadFestival(festival, actor.getIdUsuario());
            return;
        }
        throw new SecurityException("Rol no autorizado para listar pulseras de un festival.");
    }

    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    private void rollbackTransaction(EntityTransaction tx, String action) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Rollback de transacción de {} realizado.", action);
            } catch (Exception rbEx) {
                log.error("Error durante el rollback de {}: {}", action, rbEx.getMessage(), rbEx);
            }
        }
    }

    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    private RuntimeException mapException(Exception e) {
        if (e instanceof PulseraNFCNotFoundException || e instanceof EntradaNotFoundException || e instanceof EntradaNoNominadaException
                || e instanceof PulseraYaAsociadaException || e instanceof AsistenteNotFoundException || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException || e instanceof FestivalNoPublicadoException || e instanceof StockInsuficienteException
                || e instanceof SaldoInsuficienteException || e instanceof IllegalArgumentException || e instanceof SecurityException
                || e instanceof IllegalStateException || e instanceof PersistenceException || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio PulseraNFC: " + e.getMessage(), e);
    }

    private PulseraNFCDTO mapEntityToDto(PulseraNFC p) {
        if (p == null) {
            return null;
        }
        PulseraNFCDTO dto = new PulseraNFCDTO();
        dto.setIdPulsera(p.getIdPulsera());
        dto.setCodigoUid(p.getCodigoUid());
        dto.setSaldo(p.getSaldo());
        dto.setActiva(p.getActiva());
        dto.setFechaAlta(p.getFechaAlta());
        dto.setUltimaModificacion(p.getUltimaModificacion());

        if (p.getFestival() != null) {
            dto.setIdFestival(p.getFestival().getIdFestival());
            dto.setNombreFestival(p.getFestival().getNombre());
        }
        if (p.getEntrada() != null) {
            Entrada ea = p.getEntrada();
            dto.setIdEntrada(ea.getIdEntrada());
            if (ea.getAsistente() != null) {
                Asistente as = ea.getAsistente();
                dto.setIdAsistente(as.getIdAsistente());
                dto.setNombreAsistente(as.getNombre());
                dto.setEmailAsistente(as.getEmail());
            }
        }
        return dto;
    }
}
