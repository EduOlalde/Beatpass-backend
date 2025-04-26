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
 * Implementación de la interfaz {@link PulseraNFCService}. Gestiona la lógica
 * de negocio para las pulseras NFC, incluyendo asociación, consultas, recargas
 * y consumos, asegurando la integridad de los datos, manejo de saldo,
 * transacciones y permisos. **Modificado para vincular PulseraNFC directamente
 * a Festival y verificarlo en operaciones POS.**
 *
 * @see PulseraNFCService
 * @see PulseraNFCRepository
 * @see RecargaRepository
 * @see ConsumoRepository
 * @author Eduardo Olalde
 */
public class PulseraNFCServiceImpl implements PulseraNFCService {

    private static final Logger log = LoggerFactory.getLogger(PulseraNFCServiceImpl.class);

    // Repositorios y Servicios
    private final PulseraNFCRepository pulseraNFCRepository;
    private final EntradaAsignadaRepository entradaAsignadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    private final RecargaRepository recargaRepository;
    private final ConsumoRepository consumoRepository;

    /**
     * Constructor que inicializa los repositorios necesarios.
     */
    public PulseraNFCServiceImpl() {
        this.pulseraNFCRepository = new PulseraNFCRepositoryImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.recargaRepository = new RecargaRepositoryImpl();
        this.consumoRepository = new ConsumoRepositoryImpl();
    }

    /**
     * {@inheritDoc} Modificado para asignar el Festival a la pulsera durante la
     * asociación. Añade validación para evitar asociar una pulsera existente a
     * un festival diferente.
     */
    @Override
    public PulseraNFCDTO asociarPulseraEntrada(String codigoUid, Integer idEntradaAsignada, Integer idActor) {
        log.info("Service: Asociando pulsera UID {} a entrada ID {} por actor ID {}", codigoUid, idEntradaAsignada, idActor);

        if (codigoUid == null || codigoUid.isBlank() || idEntradaAsignada == null || idActor == null) {
            throw new IllegalArgumentException("UID de pulsera, ID de entrada asignada y ID de actor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar Actor y permisos básicos
            Usuario actor = verificarActorPermitido(em, idActor, RolUsuario.ADMIN, RolUsuario.PROMOTOR, RolUsuario.CAJERO);

            // 2. Buscar Entrada Asignada y validar estado
            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));
            validarEstadoEntradaParaAsociacion(entradaAsignada); // Verifica ACTIVA y NOMINADA

            // 3. Obtener el Festival asociado a la Entrada (necesario para asignar a pulsera)
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);

            // 4. Verificar si la Entrada ya tiene una Pulsera asociada
            if (pulseraNFCRepository.findByEntradaAsignadaId(em, idEntradaAsignada).isPresent()) {
                throw new IllegalStateException("La entrada ID " + idEntradaAsignada + " ya tiene una pulsera asociada.");
            }

            // 5. Buscar o Crear Pulsera por UID
            Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findByCodigoUid(em, codigoUid);
            PulseraNFC pulsera;

            if (pulseraOpt.isPresent()) {
                // --- Pulsera Existente ---
                pulsera = pulseraOpt.get();
                log.debug("Pulsera encontrada con UID {}. Verificando estado y festival.", codigoUid);
                // Bloquear para escritura si vamos a modificarla
                em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

                // Validar estado de la pulsera existente
                validarEstadoPulseraParaAsociacion(pulsera, entradaAsignada);

                // *** VALIDACIÓN: Verificar si la pulsera pertenece al mismo festival ***
                if (pulsera.getFestival() == null) {
                    // Inconsistencia: Una pulsera existente debería tener un festival. Corregir.
                    log.warn("Inconsistencia: Pulsera existente ID {} (UID {}) no tiene festival asociado. Asociando al festival ID {}.",
                            pulsera.getIdPulsera(), codigoUid, festival.getIdFestival());
                    pulsera.setFestival(festival); // Asignar el festival correcto
                } else if (!pulsera.getFestival().getIdFestival().equals(festival.getIdFestival())) {
                    // La pulsera existe pero pertenece a OTRO festival. ¡ERROR!
                    log.error("Conflicto: Pulsera UID {} pertenece al festival ID {} pero se intenta asociar a entrada del festival ID {}",
                            codigoUid, pulsera.getFestival().getIdFestival(), festival.getIdFestival());
                    throw new SecurityException("Esta pulsera pertenece a otro festival y no puede ser asociada a esta entrada.");
                }
                // Si la pulsera existe y pertenece al mismo festival, se permite la asociación

            } else {
                // --- Pulsera Nueva ---
                log.debug("Pulsera con UID {} no encontrada. Creando nueva instancia.", codigoUid);
                pulsera = new PulseraNFC();
                pulsera.setCodigoUid(codigoUid);
                pulsera.setSaldo(BigDecimal.ZERO);
                pulsera.setActiva(true);
                // *** Asignar el festival a la nueva pulsera ***
                pulsera.setFestival(festival); // Se asocia al festival de la entrada
            }

            // 6. Verificar Permiso específico del Promotor si aplica
            if (actor.getRol() == RolUsuario.PROMOTOR) {
                verificarPropiedadFestival(festival, idActor);
            }

            // 7. Establecer la asociación bidireccional y guardar/actualizar pulsera
            if (pulsera.getEntradaAsignada() != null && !pulsera.getEntradaAsignada().equals(entradaAsignada)) {
                log.warn("Desasociando pulsera UID {} de entrada anterior ID {}", codigoUid, pulsera.getEntradaAsignada().getIdEntradaAsignada());
            }
            pulsera.setEntradaAsignada(entradaAsignada);
            // La referencia inversa en EntradaAsignada (mappedBy) no se gestiona aquí
            pulsera = pulseraNFCRepository.save(em, pulsera); // Persiste si es nueva, hace merge si existe

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

    /**
     * {@inheritDoc}
     */
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
            // Verificar permisos ANTES de mapear y devolver
            verificarPermisoLecturaPulsera(em, pulsera, idActor);
            return Optional.of(mapEntityToDto(pulsera));
        } catch (Exception e) {
            log.error("Error obteniendo pulsera ID {}: {}", idPulsera, e.getMessage(), e);
            // Si la excepción es por falta de permisos, devolver vacío
            if (e instanceof SecurityException) {
                return Optional.empty();
            }
            // Para otros errores, también devolvemos vacío para no exponer detalles
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
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
            // Verificar permisos ANTES de mapear y devolver
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

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal obtenerSaldo(Integer idPulsera, Integer idActor) {
        log.debug("Service: Obteniendo saldo pulsera ID {} por actor ID {}", idPulsera, idActor);
        // Reutiliza obtenerPulseraPorId que ya verifica permisos
        PulseraNFCDTO dto = obtenerPulseraPorId(idPulsera, idActor)
                .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada o sin permiso de acceso: " + idPulsera));
        return dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO;
    }

    /**
     * {@inheritDoc}
     */
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
            // Verificar permisos para listar pulseras de este festival
            verificarPermisoListadoPulserasFestival(em, idFestival, actor);
            // Usar el método del repositorio que busca directamente por id_festival
            List<PulseraNFC> pulseras = pulseraNFCRepository.findByFestivalId(em, idFestival);
            return pulseras.stream().map(this::mapEntityToDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo pulseras para festival ID {}: {}", idFestival, e.getMessage(), e);
            // Devolver lista vacía si no tiene permisos o no encuentra el festival
            if (e instanceof SecurityException || e instanceof FestivalNotFoundException) {
                return Collections.emptyList();
            }
            // Para otros errores, también devolver lista vacía
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc} Modificado para aceptar idFestival y verificarlo si el
     * actor es PROMOTOR.
     */
    @Override
    public PulseraNFCDTO registrarRecarga(String codigoUid, BigDecimal monto, String metodoPago, Integer idUsuarioCajero, Integer idFestival) {
        log.info("Service: Registrando recarga de {} ({}) en pulsera UID {} (Fest:{}) por cajero ID {}",
                monto, metodoPago, codigoUid, idFestival, idUsuarioCajero);

        // Validaciones de parámetros
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

            // 1. Verificar Cajero y permisos básicos
            Usuario cajero = verificarActorPermitido(em, idUsuarioCajero, RolUsuario.ADMIN, RolUsuario.PROMOTOR, RolUsuario.CAJERO);

            // 2. Buscar Pulsera y bloquearla
            PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(em, codigoUid)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
            em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

            // 3. Verificar que la pulsera pertenece al festival indicado
            if (pulsera.getFestival() == null || !pulsera.getFestival().getIdFestival().equals(idFestival)) {
                log.error("Intento de recargar pulsera UID {} (Fest:{}) en contexto de festival ID {}",
                        codigoUid, (pulsera.getFestival() != null ? pulsera.getFestival().getIdFestival() : "NULL"), idFestival);
                throw new SecurityException("La pulsera no pertenece al festival especificado (" + idFestival + ").");
            }

            // 4. Si el actor es PROMOTOR, verificar propiedad del festival
            // (Esta verificación es redundante si ya comprobamos que la pulsera pertenece al festival,
            // pero la mantenemos por si acaso la lógica de permisos cambia)
            if (cajero.getRol() == RolUsuario.PROMOTOR) {
                verificarPropiedadFestival(pulsera.getFestival(), idUsuarioCajero);
            }

            // 5. Verificar estado activo de la pulsera
            if (!Boolean.TRUE.equals(pulsera.getActiva())) {
                throw new IllegalStateException("La pulsera UID " + codigoUid + " no está activa.");
            }

            // 6. Crear y guardar Recarga
            Recarga recarga = new Recarga();
            recarga.setPulseraNFC(pulsera);
            recarga.setMonto(monto);
            recarga.setMetodoPago(metodoPago != null ? metodoPago.trim() : null);
            recarga.setUsuarioCajero(cajero);
            recargaRepository.save(em, recarga);

            // 7. Actualizar saldo
            BigDecimal saldoActual = pulsera.getSaldo() != null ? pulsera.getSaldo() : BigDecimal.ZERO;
            pulsera.setSaldo(saldoActual.add(monto));
            pulsera = pulseraNFCRepository.save(em, pulsera); // merge

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

    /**
     * {@inheritDoc} Añadida verificación para asegurar que el consumo se
     * registra en una pulsera perteneciente al festival indicado en la llamada.
     */
    @Override
    public PulseraNFCDTO registrarConsumo(String codigoUid, BigDecimal monto, String descripcion, Integer idFestival, Integer idPuntoVenta, Integer idActor) {
        log.info("Service: Registrando consumo de {} ('{}') en pulsera UID {} para festival ID {} por actor ID {}",
                monto, descripcion, codigoUid, idFestival, idActor);

        // Validaciones de parámetros
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

            // 1. Verificar Actor y permisos
            Usuario actor = verificarActorPermitido(em, idActor, RolUsuario.ADMIN, RolUsuario.PROMOTOR, RolUsuario.CAJERO);

            // 2. Buscar Festival (verificar que existe)
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado: " + idFestival));

            // 3. Buscar Pulsera y bloquearla
            PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(em, codigoUid)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
            em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

            // 4. Verificar que la pulsera pertenece al festival indicado
            if (pulsera.getFestival() == null || !pulsera.getFestival().getIdFestival().equals(idFestival)) {
                log.error("Intento de registrar consumo para festival ID {} con pulsera UID {} que pertenece a festival ID {}",
                        idFestival, codigoUid, (pulsera.getFestival() != null ? pulsera.getFestival().getIdFestival() : "NULL"));
                throw new SecurityException("La pulsera no pertenece al festival especificado (" + idFestival + ").");
            }

            // 5. Si el actor es PROMOTOR, verificar propiedad del festival (redundante pero seguro)
            if (actor.getRol() == RolUsuario.PROMOTOR) {
                verificarPropiedadFestival(festival, idActor);
            }

            // 6. Verificar estado activo de la pulsera
            if (!Boolean.TRUE.equals(pulsera.getActiva())) {
                throw new IllegalStateException("La pulsera UID " + codigoUid + " no está activa.");
            }

            // 7. Verificar Saldo Suficiente
            BigDecimal saldoActual = pulsera.getSaldo() != null ? pulsera.getSaldo() : BigDecimal.ZERO;
            if (saldoActual.compareTo(monto) < 0) {
                log.warn("Saldo insuficiente en pulsera UID {}. Saldo: {}, Monto: {}", codigoUid, saldoActual, monto);
                throw new SaldoInsuficienteException("Saldo insuficiente (" + saldoActual + ") para realizar el consumo de " + monto + ".");
            }

            // 8. Crear y guardar Consumo
            Consumo consumo = new Consumo();
            consumo.setPulseraNFC(pulsera);
            consumo.setMonto(monto);
            consumo.setDescripcion(descripcion.trim());
            consumo.setFestival(festival); // Asociar al festival donde ocurre
            consumo.setIdPuntoVenta(idPuntoVenta);
            consumoRepository.save(em, consumo);

            // 9. Actualizar saldo de la pulsera
            pulsera.setSaldo(saldoActual.subtract(monto));
            pulsera = pulseraNFCRepository.save(em, pulsera); // merge

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

    // --- Métodos Privados de Ayuda (Helpers) ---
    /**
     * Verifica si un usuario existe y tiene uno de los roles permitidos.
     *
     * @param em EntityManager activo.
     * @param idUsuario ID del usuario a verificar.
     * @param rolesPermitidos Roles que tienen permiso.
     * @return La entidad Usuario si existe y tiene un rol permitido.
     * @throws UsuarioNotFoundException Si el usuario no existe.
     * @throws SecurityException Si el usuario no tiene un rol permitido.
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
     * Valida si el estado de una EntradaAsignada permite la asociación de una
     * pulsera.
     *
     * @param entrada La EntradaAsignada a validar.
     * @throws IllegalStateException Si la entrada no está ACTIVA.
     * @throws EntradaAsignadaNoNominadaException Si la entrada no está
     * nominada.
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
     * nueva EntradaAsignada.
     *
     * @param pulsera La PulseraNFC existente.
     * @param nuevaEntrada La nueva EntradaAsignada a la que se quiere asociar.
     * @throws IllegalStateException Si la pulsera no está activa.
     * @throws PulseraYaAsociadaException Si la pulsera ya está asociada a otra
     * entrada activa del mismo festival.
     */
    private void validarEstadoPulseraParaAsociacion(PulseraNFC pulsera, EntradaAsignada nuevaEntrada) {
        if (pulsera.getIdPulsera() == null) {
            return; // Es nueva
        }
        if (!Boolean.TRUE.equals(pulsera.getActiva())) {
            throw new IllegalStateException("La pulsera con UID " + pulsera.getCodigoUid() + " no está activa.");
        }
        // Comprobar si está asociada a OTRA entrada ACTIVA del MISMO festival
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
            log.warn("Pulsera UID {} estaba asociada a entrada ID {}, pero se reasociará a entrada ID {}",
                    pulsera.getCodigoUid(), pulsera.getEntradaAsignada().getIdEntradaAsignada(), nuevaEntrada.getIdEntradaAsignada());
        }
    }

    /**
     * Obtiene la entidad Festival asociada a una EntradaAsignada.
     *
     * @param ea La EntradaAsignada.
     * @return El Festival asociado.
     * @throws IllegalStateException si no se puede determinar el festival.
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
     * Verifica que el promotor dado sea el propietario del festival.
     *
     * @param festival El festival a verificar.
     * @param idPromotor El ID del promotor.
     * @throws SecurityException si no es el propietario.
     * @throws IllegalArgumentException si los argumentos son nulos o
     * inconsistentes.
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
            throw new IllegalArgumentException("El festival no puede ser nulo.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor no puede ser nulo.");
        }
        if (festival.getPromotor() == null || festival.getPromotor().getIdUsuario() == null) {
            throw new IllegalStateException("El festival ID " + festival.getIdFestival() + " no tiene un promotor asociado.");
        }
        if (!festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
    }

    /**
     * Verifica si un usuario tiene permiso para leer la información de una
     * pulsera.
     *
     * @param em EntityManager activo.
     * @param pulsera La pulsera a verificar.
     * @param idActor ID del usuario solicitante.
     * @throws UsuarioNotFoundException Si el actor no existe.
     * @throws SecurityException Si el actor no tiene permisos.
     */
    private void verificarPermisoLecturaPulsera(EntityManager em, PulseraNFC pulsera, Integer idActor) {
        Usuario actor = usuarioRepository.findById(em, idActor)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado: " + idActor));
        if (actor.getRol() == RolUsuario.ADMIN || actor.getRol() == RolUsuario.CAJERO) {
            return; // ADMIN y CAJERO pueden ver todas
        }
        if (actor.getRol() == RolUsuario.PROMOTOR) {
            // El promotor solo puede ver pulseras de SUS festivales
            if (pulsera.getFestival() == null) {
                // Si la pulsera no tiene festival (no debería pasar), el promotor no puede verla
                throw new SecurityException("Promotor no puede ver pulseras no asociadas a un festival.");
            }
            try {
                // Verificar si el festival de la pulsera pertenece al promotor
                verificarPropiedadFestival(pulsera.getFestival(), idActor);
            } catch (IllegalStateException | SecurityException e) {
                throw new SecurityException("Promotor no tiene permiso para ver esta pulsera.", e);
            }
        } else {
            // Otros roles no tienen permiso
            throw new SecurityException("Rol no autorizado para ver información de pulseras.");
        }
    }

    /**
     * Verifica si un usuario tiene permiso para listar las pulseras de un
     * festival.
     *
     * @param em EntityManager activo.
     * @param idFestival ID del festival.
     * @param actor El usuario solicitante.
     * @throws FestivalNotFoundException Si el festival no existe.
     * @throws SecurityException Si el actor no tiene permisos.
     */
    private void verificarPermisoListadoPulserasFestival(EntityManager em, Integer idFestival, Usuario actor) {
        if (actor.getRol() == RolUsuario.ADMIN) {
            return; // ADMIN puede listar todas
        }
        if (actor.getRol() == RolUsuario.PROMOTOR) {
            // PROMOTOR solo puede listar las de sus festivales
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado: " + idFestival));
            verificarPropiedadFestival(festival, actor.getIdUsuario()); // Lanza SecurityException si no es dueño
            return;
        }
        // Otros roles no pueden listar
        throw new SecurityException("Rol no autorizado para listar pulseras de un festival.");
    }

    /**
     * Manejador de excepciones y rollback.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
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
        if (e instanceof PulseraNFCNotFoundException || e instanceof EntradaAsignadaNotFoundException
                || e instanceof EntradaAsignadaNoNominadaException || e instanceof PulseraYaAsociadaException
                || e instanceof AsistenteNotFoundException || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException || e instanceof FestivalNoPublicadoException
                || e instanceof StockInsuficienteException || e instanceof SaldoInsuficienteException
                || e instanceof IllegalArgumentException
                || e instanceof SecurityException || e instanceof IllegalStateException
                || e instanceof PersistenceException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio PulseraNFC: " + e.getMessage(), e);
    }

    /**
     * Mapea una entidad PulseraNFC a su DTO correspondiente. Incluye
     * información del festival directamente.
     *
     * @param p La entidad PulseraNFC.
     * @return El PulseraNFCDTO mapeado.
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

        // Mapear festival directamente
        if (p.getFestival() != null) {
            dto.setIdFestival(p.getFestival().getIdFestival());
            dto.setNombreFestival(p.getFestival().getNombre());
        }

        // Mapear entrada y asistente asociados si existen
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
