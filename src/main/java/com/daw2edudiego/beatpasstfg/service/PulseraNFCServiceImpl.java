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
 * transacciones y permisos.
 *
 * @see PulseraNFCService
 * @see PulseraNFCRepository
 * @see RecargaRepository
 * @see ConsumoRepository
 * @author Eduardo Olalde
 */
public class PulseraNFCServiceImpl implements PulseraNFCService {

    private static final Logger log = LoggerFactory.getLogger(PulseraNFCServiceImpl.class);

    // Inyección manual de dependencias (en un entorno con CDI/Spring se usaría @Inject/@Autowired)
    private final PulseraNFCRepository pulseraNFCRepository;
    private final EntradaAsignadaRepository entradaAsignadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    // private final EntradaRepository entradaRepository; // No se usa directamente aquí
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
        // this.entradaRepository = new EntradaRepositoryImpl(); // No necesario
        this.recargaRepository = new RecargaRepositoryImpl();
        this.consumoRepository = new ConsumoRepositoryImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PulseraNFCDTO asociarPulseraEntrada(String codigoUid, Integer idEntradaAsignada, Integer idActor) {
        log.info("Service: Asociando pulsera UID {} a entrada ID {} por actor ID {}", codigoUid, idEntradaAsignada, idActor);

        // Validación de parámetros de entrada
        if (codigoUid == null || codigoUid.isBlank() || idEntradaAsignada == null || idActor == null) {
            throw new IllegalArgumentException("UID de pulsera, ID de entrada asignada y ID de actor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar Actor y permisos básicos de asociación
            Usuario actor = verificarActorPermitido(em, idActor, RolUsuario.ADMIN, RolUsuario.PROMOTOR, RolUsuario.CAJERO);

            // 2. Buscar Entrada Asignada y validar su estado
            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));
            validarEstadoEntradaParaAsociacion(entradaAsignada);

            // 3. Verificar si la Entrada ya tiene una Pulsera asociada
            if (pulseraNFCRepository.findByEntradaAsignadaId(em, idEntradaAsignada).isPresent()) {
                throw new IllegalStateException("La entrada ID " + idEntradaAsignada + " ya tiene una pulsera asociada.");
            }

            // 4. Buscar o Crear Pulsera por UID
            PulseraNFC pulsera = obtenerOCrearPulsera(em, codigoUid);
            validarEstadoPulseraParaAsociacion(pulsera, entradaAsignada);

            // 5. Verificar Permiso específico del Promotor si aplica
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada); // Obtener festival
            if (actor.getRol() == RolUsuario.PROMOTOR) {
                verificarPropiedadFestival(festival, idActor);
            }

            // 6. Establecer la asociación bidireccional y guardar
            pulsera.setEntradaAsignada(entradaAsignada);
            // entradaAsignada.setPulseraAsociada(pulsera); // Gestionado por @OneToOne(mappedBy...) en EntradaAsignada
            pulsera = pulseraNFCRepository.save(em, pulsera); // Guarda o actualiza la pulsera

            tx.commit();
            log.info("Pulsera UID {} (ID: {}) asociada correctamente a Entrada ID {}",
                    pulsera.getCodigoUid(), pulsera.getIdPulsera(), idEntradaAsignada);

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
        // No requiere transacción para lectura, pero puede ser útil para verificar permisos si implica joins
        try {
            em = JPAUtil.createEntityManager();

            // Buscar pulsera
            Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findById(em, idPulsera);
            if (pulseraOpt.isEmpty()) {
                return Optional.empty(); // No encontrada
            }
            PulseraNFC pulsera = pulseraOpt.get();

            // Verificar permisos
            verificarPermisoLecturaPulsera(em, pulsera, idActor);

            // Mapear y devolver
            return Optional.of(mapEntityToDto(pulsera));

        } catch (Exception e) {
            // No hacer rollback ya que es lectura
            log.error("Error obteniendo pulsera ID {}: {}", idPulsera, e.getMessage(), e);
            // Si es SecurityException, devolver vacío silenciosamente para el usuario
            if (e instanceof SecurityException) {
                return Optional.empty();
            }
            // Para otros errores, podríamos relanzar o devolver vacío
            // throw mapException(e); // Opcional
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

            // Buscar pulsera por UID
            Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findByCodigoUid(em, codigoUid);
            if (pulseraOpt.isEmpty()) {
                return Optional.empty(); // No encontrada
            }
            PulseraNFC pulsera = pulseraOpt.get();

            // Verificar permisos
            verificarPermisoLecturaPulsera(em, pulsera, idActor);

            // Mapear y devolver
            return Optional.of(mapEntityToDto(pulsera));

        } catch (Exception e) {
            log.error("Error obteniendo pulsera UID {}: {}", codigoUid, e.getMessage(), e);
            if (e instanceof SecurityException) {
                return Optional.empty();
            }
            // throw mapException(e); // Opcional
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
        // Devuelve el saldo o CERO si es nulo
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

            // Verificar Actor y permisos para listar pulseras del festival
            Usuario actor = usuarioRepository.findById(em, idActor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado: " + idActor));
            verificarPermisoListadoPulserasFestival(em, idFestival, actor);

            // Obtener pulseras del repositorio
            List<PulseraNFC> pulseras = pulseraNFCRepository.findByFestivalId(em, idFestival);

            // Mapear y devolver
            return pulseras.stream().map(this::mapEntityToDto).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error obteniendo pulseras para festival ID {}: {}", idFestival, e.getMessage(), e);
            // No hacer rollback en lectura
            // No relanzar SecurityException para no exponer detalles internos
            if (e instanceof SecurityException || e instanceof FestivalNotFoundException) {
                return Collections.emptyList();
            }
            // Para otros errores, devolver lista vacía
            // throw mapException(e); // Opcional
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PulseraNFCDTO registrarRecarga(String codigoUid, BigDecimal monto, String metodoPago, Integer idUsuarioCajero) {
        log.info("Service: Registrando recarga de {} ({}) en pulsera UID {} por cajero ID {}", monto, metodoPago, codigoUid, idUsuarioCajero);
        // Validación de parámetros
        if (codigoUid == null || codigoUid.isBlank() || idUsuarioCajero == null) {
            throw new IllegalArgumentException("UID pulsera y ID cajero son requeridos.");
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

            // 1. Verificar Cajero y permisos
            Usuario cajero = verificarActorPermitido(em, idUsuarioCajero, RolUsuario.ADMIN, RolUsuario.PROMOTOR, RolUsuario.CAJERO);

            // 2. Buscar Pulsera y bloquearla para escritura
            PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(em, codigoUid)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
            em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

            // 3. Verificar estado activo de la pulsera
            if (!Boolean.TRUE.equals(pulsera.getActiva())) {
                throw new IllegalStateException("La pulsera UID " + codigoUid + " no está activa.");
            }

            // 4. Crear y guardar el registro de Recarga
            Recarga recarga = new Recarga();
            recarga.setPulseraNFC(pulsera);
            recarga.setMonto(monto);
            recarga.setMetodoPago(metodoPago != null ? metodoPago.trim() : null);
            recarga.setUsuarioCajero(cajero);
            recargaRepository.save(em, recarga);
            log.debug("Recarga ID {} registrada.", recarga.getIdRecarga());

            // 5. Actualizar saldo de la pulsera
            BigDecimal saldoActual = pulsera.getSaldo() != null ? pulsera.getSaldo() : BigDecimal.ZERO;
            pulsera.setSaldo(saldoActual.add(monto));
            pulsera = pulseraNFCRepository.save(em, pulsera); // Guardar pulsera actualizada

            tx.commit();
            log.info("Recarga de {} realizada en pulsera UID {}. Nuevo saldo: {}", monto, codigoUid, pulsera.getSaldo());

            return mapEntityToDto(pulsera);

        } catch (Exception e) {
            handleException(e, tx, "registrando recarga para UID " + codigoUid);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PulseraNFCDTO registrarConsumo(String codigoUid, BigDecimal monto, String descripcion, Integer idFestival, Integer idPuntoVenta, Integer idActor) {
        log.info("Service: Registrando consumo de {} ('{}') en pulsera UID {} para festival ID {} por actor ID {}",
                monto, descripcion, codigoUid, idFestival, idActor);

        // Validación de parámetros
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
            // Podríamos añadir verificación de si el festival está PUBLICADO/activo

            // 3. Buscar Pulsera y bloquearla para escritura
            PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(em, codigoUid)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
            em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

            // 4. Verificar estado activo de la pulsera
            if (!Boolean.TRUE.equals(pulsera.getActiva())) {
                throw new IllegalStateException("La pulsera UID " + codigoUid + " no está activa.");
            }

            // 5. Verificar Saldo Suficiente
            BigDecimal saldoActual = pulsera.getSaldo() != null ? pulsera.getSaldo() : BigDecimal.ZERO;
            if (saldoActual.compareTo(monto) < 0) {
                log.warn("Saldo insuficiente en pulsera UID {}. Saldo: {}, Monto: {}", codigoUid, saldoActual, monto);
                throw new SaldoInsuficienteException("Saldo insuficiente (" + saldoActual + ") para realizar el consumo de " + monto + ".");
            }

            // 6. Crear y guardar el registro de Consumo
            Consumo consumo = new Consumo();
            consumo.setPulseraNFC(pulsera);
            consumo.setMonto(monto);
            consumo.setDescripcion(descripcion.trim());
            consumo.setFestival(festival); // Asociar al festival donde ocurre
            consumo.setIdPuntoVenta(idPuntoVenta); // Opcional
            consumoRepository.save(em, consumo);
            log.debug("Consumo ID {} registrado.", consumo.getIdConsumo());

            // 7. Actualizar saldo de la pulsera
            pulsera.setSaldo(saldoActual.subtract(monto));
            pulsera = pulseraNFCRepository.save(em, pulsera); // Guardar pulsera actualizada

            tx.commit();
            log.info("Consumo de {} registrado en pulsera UID {}. Nuevo saldo: {}", monto, codigoUid, pulsera.getSaldo());

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
     * Obtiene una PulseraNFC por su UID. Si no existe, crea una nueva instancia
     * (sin persistirla aún).
     *
     * @param em EntityManager activo.
     * @param codigoUid UID de la pulsera.
     * @return La entidad PulseraNFC (existente o nueva).
     */
    private PulseraNFC obtenerOCrearPulsera(EntityManager em, String codigoUid) {
        Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findByCodigoUid(em, codigoUid);
        if (pulseraOpt.isPresent()) {
            log.debug("Pulsera encontrada con UID {}", codigoUid);
            return pulseraOpt.get();
        } else {
            log.debug("Pulsera con UID {} no encontrada. Preparando nueva instancia.", codigoUid);
            PulseraNFC nuevaPulsera = new PulseraNFC();
            nuevaPulsera.setCodigoUid(codigoUid);
            nuevaPulsera.setSaldo(BigDecimal.ZERO);
            nuevaPulsera.setActiva(true);
            return nuevaPulsera;
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
            return; // Es una pulsera nueva, no hay estado que validar
        }
        if (!Boolean.TRUE.equals(pulsera.getActiva())) {
            throw new IllegalStateException("La pulsera con UID " + pulsera.getCodigoUid() + " no está activa.");
        }
        // Verificar si ya está asociada a OTRA entrada ACTIVA del MISMO festival
        if (pulsera.getEntradaAsignada() != null
                && pulsera.getEntradaAsignada().getEstado() == EstadoEntradaAsignada.ACTIVA
                && !pulsera.getEntradaAsignada().equals(nuevaEntrada)) { // Asegurarse que no es la misma entrada

            Festival festivalPulseraActual = obtenerFestivalDesdeEntradaAsignada(pulsera.getEntradaAsignada());
            Festival festivalEntradaNueva = obtenerFestivalDesdeEntradaAsignada(nuevaEntrada);

            if (festivalPulseraActual != null && festivalPulseraActual.equals(festivalEntradaNueva)) {
                throw new PulseraYaAsociadaException("La pulsera UID " + pulsera.getCodigoUid()
                        + " ya está asociada a otra entrada activa (ID: "
                        + pulsera.getEntradaAsignada().getIdEntradaAsignada() + ") para este festival.");
            }
            // Si está asociada a entrada de otro festival o entrada no activa, se permite reasociar
            log.warn("Pulsera UID {} estaba asociada a entrada ID {}, pero se reasociará a entrada ID {}",
                    pulsera.getCodigoUid(), pulsera.getEntradaAsignada().getIdEntradaAsignada(), nuevaEntrada.getIdEntradaAsignada());
        }
    }

    /**
     * Verifica si un usuario tiene permiso para leer la información de una
     * pulsera.
     *
     * @param em EntityManager activo.
     * @param pulsera La pulsera cuya información se quiere leer.
     * @param idActor ID del usuario solicitante.
     * @throws UsuarioNotFoundException Si el actor no existe.
     * @throws SecurityException Si el actor no tiene permisos.
     */
    private void verificarPermisoLecturaPulsera(EntityManager em, PulseraNFC pulsera, Integer idActor) {
        Usuario actor = usuarioRepository.findById(em, idActor)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado: " + idActor));

        // ADMIN y CAJERO pueden ver cualquiera
        if (actor.getRol() == RolUsuario.ADMIN || actor.getRol() == RolUsuario.CAJERO) {
            return;
        }

        // PROMOTOR solo puede ver las asociadas a sus festivales
        if (actor.getRol() == RolUsuario.PROMOTOR) {
            if (pulsera.getEntradaAsignada() == null) {
                // Si no está asociada, el promotor no puede verla directamente
                throw new SecurityException("Promotor no puede ver pulseras no asociadas a una entrada.");
            }
            try {
                Festival festival = obtenerFestivalDesdeEntradaAsignada(pulsera.getEntradaAsignada());
                verificarPropiedadFestival(festival, idActor); // Lanza SecurityException si no es dueño
            } catch (IllegalStateException | SecurityException e) {
                // Si no se puede determinar el festival o no es dueño
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
        // ADMIN puede listar todas
        if (actor.getRol() == RolUsuario.ADMIN) {
            // Opcional: verificar que el festival existe si queremos lanzar FestivalNotFoundException
            // festivalRepository.findById(em, idFestival).orElseThrow(() -> new FestivalNotFoundException(...));
            return;
        }
        // PROMOTOR puede listar las de sus festivales
        if (actor.getRol() == RolUsuario.PROMOTOR) {
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado: " + idFestival));
            verificarPropiedadFestival(festival, actor.getIdUsuario()); // Lanza SecurityException si no es dueño
            return;
        }
        // Otros roles (CAJERO) no pueden listar
        throw new SecurityException("Rol no autorizado para listar todas las pulseras de un festival.");
    }

    /**
     * Obtiene la entidad Festival asociada a una EntradaAsignada. Maneja
     * posibles inconsistencias en la cadena de relaciones.
     *
     * @param ea La EntradaAsignada.
     * @return El Festival asociado.
     * @throws IllegalStateException si no se puede determinar el festival.
     */
    private Festival obtenerFestivalDesdeEntradaAsignada(EntradaAsignada ea) {
        // Reutilizado de EntradaAsignadaServiceImpl - Podría ir a una clase de utilidad
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getEntrada() == null || ea.getCompraEntrada().getEntrada().getFestival() == null) {
            Integer eaId = (ea != null) ? ea.getIdEntradaAsignada() : null;
            log.error("Inconsistencia de datos para EntradaAsignada ID {}: no se pudo obtener el festival asociado.", eaId);
            throw new IllegalStateException("Error interno: no se pudo determinar el festival de la entrada ID " + eaId);
        }
        return ea.getCompraEntrada().getEntrada().getFestival();
    }

    /**
     * Verifica que el promotor dado sea el propietario del festival.
     * (Reutilizado de otros servicios).
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        // Reutilizado de otros servicios
        if (festival == null) {
            throw new IllegalArgumentException("El festival no puede ser nulo para verificar propiedad.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor no puede ser nulo para verificar propiedad.");
        }
        if (festival.getPromotor() == null || festival.getPromotor().getIdUsuario() == null) {
            log.error("Inconsistencia de datos: Festival ID {} no tiene un promotor asociado.", festival.getIdFestival());
            throw new IllegalStateException("El festival no tiene un promotor asociado.");
        }
        if (!festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            log.warn("Intento de acceso no autorizado por promotor ID {} al festival ID {} (propiedad de promotor ID {})",
                    idPromotor, festival.getIdFestival(), festival.getPromotor().getIdUsuario());
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
        log.trace("Verificación de propiedad exitosa para promotor ID {} sobre festival ID {}", idPromotor, festival.getIdFestival());
    }

    /**
     * Manejador genérico de excepciones para métodos de servicio. (Reutilizado
     * de otros servicios).
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
     * Cierra el EntityManager si está abierto. (Reutilizado de otros
     * servicios).
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    /**
     * Mapea excepciones técnicas a excepciones de negocio o RuntimeException.
     * (Reutilizado de otros servicios).
     */
    private RuntimeException mapException(Exception e) {
        // Asegurarse de incluir todas las excepciones personalizadas relevantes
        if (e instanceof PulseraNFCNotFoundException || e instanceof EntradaAsignadaNotFoundException
                || e instanceof EntradaAsignadaNoNominadaException || e instanceof PulseraYaAsociadaException
                || e instanceof AsistenteNotFoundException || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException || e instanceof FestivalNoPublicadoException
                || e instanceof StockInsuficienteException || e instanceof SaldoInsuficienteException
                || e instanceof IllegalArgumentException
                || e instanceof SecurityException || e instanceof IllegalStateException
                || e instanceof PersistenceException
                || e instanceof RuntimeException) { // Incluir RuntimeException genéricas
            return (RuntimeException) e;
        }
        // Para otras excepciones no esperadas, envolvemos en RuntimeException
        return new RuntimeException("Error inesperado en la capa de servicio PulseraNFC: " + e.getMessage(), e);
    }

    /**
     * Mapea una entidad PulseraNFC a su correspondiente PulseraNFCDTO. Incluye
     * información básica de la entrada y asistente asociados si existen.
     *
     * @param p La entidad PulseraNFC.
     * @return El PulseraNFCDTO mapeado, o {@code null} si la entidad es
     * {@code null}.
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

        // Incluir datos asociados si existen
        if (p.getEntradaAsignada() != null) {
            EntradaAsignada ea = p.getEntradaAsignada();
            dto.setIdEntradaAsignada(ea.getIdEntradaAsignada());
            // dto.setQrEntradaAsignada(ea.getCodigoQr()); // Opcional: enviar QR?

            if (ea.getAsistente() != null) {
                Asistente as = ea.getAsistente();
                dto.setIdAsistente(as.getIdAsistente());
                dto.setNombreAsistente(as.getNombre());
                dto.setEmailAsistente(as.getEmail());
            }
            // Incluir info del festival
            try {
                Festival f = obtenerFestivalDesdeEntradaAsignada(ea);
                if (f != null) {
                    dto.setIdFestival(f.getIdFestival());
                    dto.setNombreFestival(f.getNombre());
                }
            } catch (IllegalStateException ex) {
                log.warn("No se pudo obtener info del festival para pulsera ID {} al mapear a DTO.", p.getIdPulsera());
            }
        }
        return dto;
    }
}
