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
    private final EntradaAsignadaRepository entradaAsignadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    private final RecargaRepository recargaRepository;
    private final ConsumoRepository consumoRepository;

    public PulseraNFCServiceImpl() {
        this.pulseraNFCRepository = new PulseraNFCRepositoryImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.recargaRepository = new RecargaRepositoryImpl();
        this.consumoRepository = new ConsumoRepositoryImpl();
    }

    @Override
    public PulseraNFCDTO asociarPulseraEntrada(String codigoUid, Integer idEntradaAsignada, Integer idActor) {
        log.info("Service: Asociando pulsera UID {} a entrada ID {} por actor ID {}", codigoUid, idEntradaAsignada, idActor);
        if (codigoUid == null || codigoUid.isBlank() || idEntradaAsignada == null || idActor == null) {
            throw new IllegalArgumentException("UID pulsera, ID entrada y ID actor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario actor = verificarActorPermitido(em, idActor, RolUsuario.ADMIN, RolUsuario.PROMOTOR, RolUsuario.CAJERO);

            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));
            validarEstadoEntradaParaAsociacion(entradaAsignada);

            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);

            if (pulseraNFCRepository.findByEntradaAsignadaId(em, idEntradaAsignada).isPresent()) {
                throw new IllegalStateException("La entrada ID " + idEntradaAsignada + " ya tiene una pulsera asociada.");
            }

            Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findByCodigoUid(em, codigoUid);
            PulseraNFC pulsera;

            if (pulseraOpt.isPresent()) {
                pulsera = pulseraOpt.get();
                log.debug("Pulsera encontrada con UID {}. Verificando estado y festival.", codigoUid);
                em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);
                validarEstadoPulseraParaAsociacion(pulsera, entradaAsignada);

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
                log.debug("Pulsera con UID {} no encontrada. Creando nueva.", codigoUid);
                pulsera = new PulseraNFC();
                pulsera.setCodigoUid(codigoUid);
                pulsera.setSaldo(BigDecimal.ZERO);
                pulsera.setActiva(true);
                pulsera.setFestival(festival); // Asociar al festival de la entrada
            }

            if (actor.getRol() == RolUsuario.PROMOTOR) {
                verificarPropiedadFestival(festival, idActor);
            }

            if (pulsera.getEntradaAsignada() != null && !pulsera.getEntradaAsignada().equals(entradaAsignada)) {
                log.warn("Desasociando pulsera UID {} de entrada anterior ID {}", codigoUid, pulsera.getEntradaAsignada().getIdEntradaAsignada());
            }
            pulsera.setEntradaAsignada(entradaAsignada);
            pulsera = pulseraNFCRepository.save(em, pulsera);

            tx.commit();
            log.info("Pulsera UID {} (ID: {}) asociada correctamente a Entrada ID {} del Festival ID {}",
                    pulsera.getCodigoUid(), pulsera.getIdPulsera(), idEntradaAsignada, festival.getIdFestival());

            return mapEntityToDto(pulsera);

        } catch (Exception e) {
            handleException(e, tx, "asociando pulsera UID " + codigoUid + " a entrada ID " + idEntradaAsignada);
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
            verificarPermisoLecturaPulsera(em, pulsera, idActor); // Verifica permisos
            return Optional.of(mapEntityToDto(pulsera));
        } catch (Exception e) {
            log.error("Error obteniendo pulsera ID {}: {}", idPulsera, e.getMessage(), e);
            if (e instanceof SecurityException) {
                return Optional.empty(); // No tiene permisos
            }
            return Optional.empty(); // Otros errores
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

    // --- Métodos Privados de Ayuda ---
    /**
     * Verifica si un usuario existe y tiene uno de los roles permitidos.
     */
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

    /**
     * Valida si el estado de una EntradaAsignada permite la asociación de
     * pulsera.
     */
    private void validarEstadoEntradaParaAsociacion(EntradaAsignada entrada) {
        if (entrada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
            throw new IllegalStateException("La entrada ID " + entrada.getIdEntradaAsignada()
                    + " no está activa (Estado: " + entrada.getEstado() + ").");
        }
        if (entrada.getAsistente() == null) {
            throw new EntradaAsignadaNoNominadaException("La entrada ID " + entrada.getIdEntradaAsignada()
                    + " debe estar nominada a un asistente.");
        }
    }

    /**
     * Valida si el estado de una PulseraNFC existente permite asociarla a una
     * nueva Entrada.
     */
    private void validarEstadoPulseraParaAsociacion(PulseraNFC pulsera, EntradaAsignada nuevaEntrada) {
        if (pulsera.getIdPulsera() == null) {
            return; // Es nueva
        }
        if (!Boolean.TRUE.equals(pulsera.getActiva())) {
            throw new IllegalStateException("La pulsera con UID " + pulsera.getCodigoUid() + " no está activa.");
        }
        if (pulsera.getEntradaAsignada() != null
                && pulsera.getEntradaAsignada().getEstado() == EstadoEntradaAsignada.ACTIVA
                && !pulsera.getEntradaAsignada().equals(nuevaEntrada)) {
            Festival festivalPulseraActual = obtenerFestivalDesdeEntradaAsignada(pulsera.getEntradaAsignada());
            Festival festivalEntradaNueva = obtenerFestivalDesdeEntradaAsignada(nuevaEntrada);
            if (festivalPulseraActual != null && festivalPulseraActual.equals(festivalEntradaNueva)) {
                throw new PulseraYaAsociadaException("La pulsera UID " + pulsera.getCodigoUid()
                        + " ya está asociada a otra entrada activa (ID: "
                        + pulsera.getEntradaAsignada().getIdEntradaAsignada() + ") para este festival.");
            }
        }
    }

    /**
     * Obtiene el Festival asociado a una EntradaAsignada.
     */
    private Festival obtenerFestivalDesdeEntradaAsignada(EntradaAsignada ea) {
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getEntrada() == null || ea.getCompraEntrada().getEntrada().getFestival() == null) {
            Integer eaId = (ea != null) ? ea.getIdEntradaAsignada() : null;
            log.error("Inconsistencia de datos para EntradaAsignada ID {}: no se pudo obtener el festival asociado.", eaId);
            throw new IllegalStateException("Error interno: no se pudo determinar el festival de la entrada ID " + eaId);
        }
        return ea.getCompraEntrada().getEntrada().getFestival();
    }

    /**
     * Verifica que el promotor sea propietario del festival.
     */
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

    /**
     * Verifica permisos para leer información de una pulsera.
     */
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

    /**
     * Verifica permisos para listar las pulseras de un festival.
     */
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

    /**
     * Manejador genérico de excepciones.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    /**
     * Realiza rollback si la transacción está activa.
     */
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

    /**
     * Cierra el EntityManager.
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    /**
     * Mapea excepciones técnicas a de negocio o Runtime.
     */
    private RuntimeException mapException(Exception e) {
        if (e instanceof PulseraNFCNotFoundException || e instanceof EntradaAsignadaNotFoundException || e instanceof EntradaAsignadaNoNominadaException
                || e instanceof PulseraYaAsociadaException || e instanceof AsistenteNotFoundException || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException || e instanceof FestivalNoPublicadoException || e instanceof StockInsuficienteException
                || e instanceof SaldoInsuficienteException || e instanceof IllegalArgumentException || e instanceof SecurityException
                || e instanceof IllegalStateException || e instanceof PersistenceException || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio PulseraNFC: " + e.getMessage(), e);
    }

    /**
     * Mapea entidad PulseraNFC a DTO.
     */
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
        if (p.getEntradaAsignada() != null) {
            EntradaAsignada ea = p.getEntradaAsignada();
            dto.setIdEntradaAsignada(ea.getIdEntradaAsignada());
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
