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

    // Dependencias
    private final PulseraNFCRepository pulseraNFCRepository;
    private final EntradaAsignadaRepository entradaAsignadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    private final EntradaRepository entradaRepository;
    private final RecargaRepository recargaRepository;
    private final ConsumoRepository consumoRepository;

    public PulseraNFCServiceImpl() {
        this.pulseraNFCRepository = new PulseraNFCRepositoryImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.recargaRepository = new RecargaRepositoryImpl();
        this.consumoRepository = new ConsumoRepositoryImpl();
    }

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

            // 1. Verificar Actor (simplificado, asumimos ADMIN o PROMOTOR pueden asociar)
            Usuario actor = usuarioRepository.findById(em, idActor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor));
            // TODO: Definir roles específicos si es necesario (ej: CAJERO, ACCESO)
            boolean tienePermiso = (actor.getRol() == RolUsuario.ADMIN || actor.getRol() == RolUsuario.PROMOTOR);
            if (!tienePermiso) {
                throw new SecurityException("El usuario no tiene permiso para asociar pulseras.");
            }

            // 2. Buscar Entrada Asignada
            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));

            // 3. Validar Estado y Nominación de la Entrada
            if (entradaAsignada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                throw new IllegalStateException("La entrada ID " + idEntradaAsignada + " no está activa (Estado: " + entradaAsignada.getEstado() + ").");
            }
            if (entradaAsignada.getAsistente() == null) {
                throw new EntradaAsignadaNoNominadaException("La entrada ID " + idEntradaAsignada + " debe estar nominada a un asistente antes de asociar una pulsera.");
            }

            // 4. Verificar si la Entrada ya tiene una Pulsera asociada
            Optional<PulseraNFC> pulseraExistenteParaEntrada = pulseraNFCRepository.findByEntradaAsignadaId(em, idEntradaAsignada);
            if (pulseraExistenteParaEntrada.isPresent()) {
                throw new IllegalStateException("La entrada ID " + idEntradaAsignada + " ya tiene asociada la pulsera UID " + pulseraExistenteParaEntrada.get().getCodigoUid());
            }

            // 5. Buscar o Crear Pulsera por UID
            Optional<PulseraNFC> pulseraOpt = pulseraNFCRepository.findByCodigoUid(em, codigoUid);
            PulseraNFC pulsera;

            if (pulseraOpt.isPresent()) {
                // Pulsera encontrada - Verificar si está activa y si ya está asociada a OTRA entrada ACTIVA del MISMO festival
                pulsera = pulseraOpt.get();
                log.debug("Pulsera encontrada con UID {}. ID: {}", codigoUid, pulsera.getIdPulsera());
                if (!Boolean.TRUE.equals(pulsera.getActiva())) {
                    throw new IllegalStateException("La pulsera con UID " + codigoUid + " no está activa.");
                }
                // Verificar si ya está asociada a otra entrada activa del mismo festival
                if (pulsera.getEntradaAsignada() != null && pulsera.getEntradaAsignada().getEstado() == EstadoEntradaAsignada.ACTIVA) {
                    // Obtener festival de la pulsera ya asociada
                    Festival festivalPulseraActual = obtenerFestivalDesdeEntradaAsignada(pulsera.getEntradaAsignada());
                    // Obtener festival de la entrada que queremos asociar
                    Festival festivalEntradaNueva = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);

                    if (festivalPulseraActual != null && festivalEntradaNueva != null && festivalPulseraActual.equals(festivalEntradaNueva)) {
                        throw new PulseraYaAsociadaException("La pulsera UID " + codigoUid + " ya está asociada a otra entrada activa (ID: " + pulsera.getEntradaAsignada().getIdEntradaAsignada() + ") para este festival.");
                    }
                    // Si está asociada a una entrada de OTRO festival o una entrada NO ACTIVA, podríamos permitir reasociarla (depende reglas negocio)
                    log.warn("Pulsera UID {} estaba asociada a entrada ID {}, pero se reasociará a entrada ID {}", codigoUid, pulsera.getEntradaAsignada().getIdEntradaAsignada(), idEntradaAsignada);
                }

            } else {
                // Pulsera no encontrada - Crear una nueva
                log.debug("Pulsera con UID {} no encontrada. Creando nueva.", codigoUid);
                pulsera = new PulseraNFC();
                pulsera.setCodigoUid(codigoUid);
                pulsera.setSaldo(BigDecimal.ZERO); // Saldo inicial 0
                pulsera.setActiva(true); // Activa por defecto al crear
                // La fechaAlta se pone automáticamente por la BD
            }

            // 6. Verificar Permiso del Promotor (si el actor es promotor)
            // Necesitamos el festival al que pertenece la entrada
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            if (actor.getRol() == RolUsuario.PROMOTOR) {
                verificarPropiedadFestival(festival, idActor); // Reutilizamos helper
            }

            // 7. Establecer la asociación y guardar
            pulsera.setEntradaAsignada(entradaAsignada);
            pulsera = pulseraNFCRepository.save(em, pulsera); // Guarda o actualiza la pulsera

            tx.commit();
            log.info("Pulsera UID {} (ID: {}) asociada correctamente a Entrada ID {}",
                    pulsera.getCodigoUid(), pulsera.getIdPulsera(), idEntradaAsignada);

            return mapEntityToDto(pulsera); // Devolver DTO de la pulsera

        } catch (Exception e) {
            handleException(e, tx, "asociando pulsera a entrada");
            throw mapException(e); // Relanzar excepción mapeada
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
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin(); // Tx para posible verificación de permisos compleja
            Usuario actor = usuarioRepository.findById(em, idActor).orElseThrow(() -> new UsuarioNotFoundException("Actor no encontrado: " + idActor));
            PulseraNFC pulsera = pulseraNFCRepository.findById(em, idPulsera).orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada: " + idPulsera));

            // Verificar permisos (simplificado: admin puede ver todo, promotor solo las de sus festivales)
            if (actor.getRol() == RolUsuario.PROMOTOR) {
                if (pulsera.getEntradaAsignada() == null) {
                    throw new SecurityException("Promotor no puede ver pulseras no asociadas.");
                }
                Festival festival = obtenerFestivalDesdeEntradaAsignada(pulsera.getEntradaAsignada());
                verificarPropiedadFestival(festival, idActor);
            }
            tx.commit();
            return Optional.of(mapEntityToDto(pulsera));
        } catch (Exception e) {
            handleException(e, tx, "obteniendo pulsera por ID");
            if (e instanceof SecurityException || e instanceof PulseraNFCNotFoundException) {
                return Optional.empty();
            }
            throw mapException(e);
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
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            Usuario actor = usuarioRepository.findById(em, idActor).orElseThrow(() -> new UsuarioNotFoundException("Actor no encontrado: " + idActor));
            PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(em, codigoUid).orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada: " + codigoUid));

            // Verificar permisos (igual que en findById)
            if (actor.getRol() == RolUsuario.PROMOTOR) {
                if (pulsera.getEntradaAsignada() == null) {
                    throw new SecurityException("Promotor no puede ver pulseras no asociadas.");
                }
                Festival festival = obtenerFestivalDesdeEntradaAsignada(pulsera.getEntradaAsignada());
                verificarPropiedadFestival(festival, idActor);
            }
            tx.commit();
            return Optional.of(mapEntityToDto(pulsera));
        } catch (Exception e) {
            handleException(e, tx, "obteniendo pulsera por UID");
            if (e instanceof SecurityException || e instanceof PulseraNFCNotFoundException) {
                return Optional.empty();
            }
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public BigDecimal obtenerSaldo(Integer idPulsera, Integer idActor) {
        log.debug("Service: Obteniendo saldo pulsera ID {} por actor ID {}", idPulsera, idActor);
        // Usamos obtenerPulseraPorId que ya incluye la verificación de permisos
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
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            Usuario actor = usuarioRepository.findById(em, idActor).orElseThrow(() -> new UsuarioNotFoundException("Actor no encontrado: " + idActor));

            // Verificar permisos (Admin o Promotor dueño)
            if (actor.getRol() == RolUsuario.PROMOTOR) {
                Festival festival = festivalRepository.findById(em, idFestival).orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado: " + idFestival));
                verificarPropiedadFestival(festival, idActor);
            } else if (actor.getRol() != RolUsuario.ADMIN) {
                throw new SecurityException("Rol no autorizado para ver pulseras de festival.");
            }

            List<PulseraNFC> pulseras = pulseraNFCRepository.findByFestivalId(em, idFestival);
            tx.commit();
            return pulseras.stream().map(this::mapEntityToDto).collect(Collectors.toList());
        } catch (Exception e) {
            handleException(e, tx, "obteniendo pulseras por festival");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public PulseraNFCDTO registrarRecarga(String codigoUid, BigDecimal monto, String metodoPago, Integer idUsuarioCajero) {
        log.info("Service: Registrando recarga de {} ({}) en pulsera UID {} por cajero ID {}", monto, metodoPago, codigoUid, idUsuarioCajero);
        if (codigoUid == null || codigoUid.isBlank() || monto == null || monto.compareTo(BigDecimal.ZERO) <= 0 || idUsuarioCajero == null) {
            throw new IllegalArgumentException("UID pulsera, monto positivo y ID cajero son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar Cajero (simplificado)
            Usuario cajero = usuarioRepository.findById(em, idUsuarioCajero)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario cajero no encontrado: " + idUsuarioCajero));
            // TODO: Verificar rol específico de cajero si existe
            if (cajero.getRol() != RolUsuario.ADMIN && cajero.getRol() != RolUsuario.PROMOTOR) { // Ejemplo temporal
                throw new SecurityException("Usuario no autorizado para realizar recargas.");
            }

            // 2. Buscar Pulsera y bloquearla
            PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(em, codigoUid)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
            em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE); // Bloqueo para actualizar saldo

            // 3. Verificar si pulsera está activa
            if (!Boolean.TRUE.equals(pulsera.getActiva())) {
                throw new IllegalStateException("La pulsera UID " + codigoUid + " no está activa.");
            }

            // 4. Crear registro de Recarga
            Recarga recarga = new Recarga();
            recarga.setPulseraNFC(pulsera);
            recarga.setMonto(monto);
            recarga.setMetodoPago(metodoPago);
            recarga.setUsuarioCajero(cajero);
            // La fecha se establece automáticamente por la BD

            recargaRepository.save(em, recarga);
            log.debug("Recarga ID {} registrada.", recarga.getIdRecarga());

            // 5. Actualizar saldo de la pulsera
            BigDecimal saldoActual = pulsera.getSaldo() != null ? pulsera.getSaldo() : BigDecimal.ZERO;
            pulsera.setSaldo(saldoActual.add(monto));
            pulseraNFCRepository.save(em, pulsera); // Guardar pulsera con saldo actualizado

            tx.commit();
            log.info("Recarga de {} realizada en pulsera UID {}. Nuevo saldo: {}", monto, codigoUid, pulsera.getSaldo());

            return mapEntityToDto(pulsera);

        } catch (Exception e) {
            handleException(e, tx, "registrando recarga");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public PulseraNFCDTO registrarConsumo(String codigoUid, BigDecimal monto, String descripcion, Integer idFestival, Integer idPuntoVenta, Integer idActor) {
        log.info("Service: Registrando consumo de {} ('{}') en pulsera UID {} para festival ID {} por actor ID {}",
                monto, descripcion, codigoUid, idFestival, idActor);

        if (codigoUid == null || codigoUid.isBlank() || monto == null || monto.compareTo(BigDecimal.ZERO) <= 0 || idFestival == null || idActor == null) {
            throw new IllegalArgumentException("UID pulsera, monto positivo, ID festival y ID actor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar Actor (simplificado, ¿quién puede registrar consumos?)
            Usuario actor = usuarioRepository.findById(em, idActor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado: " + idActor));
            // TODO: Definir rol específico (PUNTO_VENTA?) y verificarlo
            if (actor.getRol() != RolUsuario.ADMIN && actor.getRol() != RolUsuario.PROMOTOR) { // Ejemplo temporal
                throw new SecurityException("Usuario no autorizado para registrar consumos.");
            }

            // 2. Buscar Festival
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado: " + idFestival));
            // Podríamos verificar si el festival está activo/publicado

            // 3. Buscar Pulsera y bloquearla
            PulseraNFC pulsera = pulseraNFCRepository.findByCodigoUid(em, codigoUid)
                    .orElseThrow(() -> new PulseraNFCNotFoundException("Pulsera no encontrada con UID: " + codigoUid));
            em.lock(pulsera, LockModeType.PESSIMISTIC_WRITE);

            // 4. Verificar si pulsera está activa
            if (!Boolean.TRUE.equals(pulsera.getActiva())) {
                throw new IllegalStateException("La pulsera UID " + codigoUid + " no está activa.");
            }

            // 5. Verificar Saldo Suficiente
            BigDecimal saldoActual = pulsera.getSaldo() != null ? pulsera.getSaldo() : BigDecimal.ZERO;
            if (saldoActual.compareTo(monto) < 0) {
                log.warn("Saldo insuficiente en pulsera UID {}. Saldo: {}, Monto: {}", codigoUid, saldoActual, monto);
                throw new SaldoInsuficienteException("Saldo insuficiente (" + saldoActual + "€) para realizar el consumo de " + monto + "€.");
            }

            // 6. Crear registro de Consumo
            Consumo consumo = new Consumo();
            consumo.setPulseraNFC(pulsera);
            consumo.setMonto(monto);
            consumo.setDescripcion(descripcion);
            consumo.setFestival(festival); // Asociar al festival donde ocurre
            consumo.setIdPuntoVenta(idPuntoVenta); // Opcional
            // La fecha se establece automáticamente

            consumoRepository.save(em, consumo);
            log.debug("Consumo ID {} registrado.", consumo.getIdConsumo());

            // 7. Actualizar saldo de la pulsera
            pulsera.setSaldo(saldoActual.subtract(monto));
            pulseraNFCRepository.save(em, pulsera);

            tx.commit();
            log.info("Consumo de {} registrado en pulsera UID {}. Nuevo saldo: {}", monto, codigoUid, pulsera.getSaldo());

            return mapEntityToDto(pulsera);

        } catch (Exception e) {
            handleException(e, tx, "registrando consumo");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Helpers Internos (reutilizados de otros servicios) ---
    private Festival obtenerFestivalDesdeEntradaAsignada(EntradaAsignada ea) {
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getEntrada() == null || ea.getCompraEntrada().getEntrada().getFestival() == null) {
            log.error("Inconsistencia de datos para EntradaAsignada ID {}: no se pudo obtener el festival asociado.", ea != null ? ea.getIdEntradaAsignada() : "null");
            throw new IllegalStateException("Error interno: no se pudo determinar el festival de la entrada.");
        }
        return ea.getCompraEntrada().getEntrada().getFestival();
    }

    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
            throw new IllegalStateException("Error interno: Festival asociado no encontrado.");
        }
        if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            throw new SecurityException("No tiene permiso para realizar acciones sobre este festival/pulsera.");
        }
    }

    private void handleException(Exception e, EntityTransaction tx, String action) {
        /* ... (como en otros servicios) ... */ }

    private void closeEntityManager(EntityManager em) {
        /* ... (como en otros servicios) ... */ }

    private RuntimeException mapException(Exception e) {
        /* ... (como en otros servicios, añadir nuevas excepciones NFC) ... */
        if (e instanceof PulseraNFCNotFoundException || e instanceof EntradaAsignadaNotFoundException
                || e instanceof EntradaAsignadaNoNominadaException || e instanceof PulseraYaAsociadaException
                || e instanceof AsistenteNotFoundException || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException || e instanceof FestivalNoPublicadoException
                || e instanceof StockInsuficienteException || e instanceof IllegalArgumentException
                || e instanceof SecurityException || e instanceof IllegalStateException
                || e instanceof PersistenceException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en servicio NFC: " + e.getMessage(), e);
    }

    // --- Helper de Mapeo PulseraNFC a PulseraNFCDTO ---
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

        if (p.getEntradaAsignada() != null) {
            EntradaAsignada ea = p.getEntradaAsignada();
            dto.setIdEntradaAsignada(ea.getIdEntradaAsignada());
            dto.setQrEntradaAsignada(ea.getCodigoQr()); // O solo una parte

            if (ea.getAsistente() != null) {
                Asistente as = ea.getAsistente();
                dto.setIdAsistente(as.getIdAsistente());
                dto.setNombreAsistente(as.getNombre());
                dto.setEmailAsistente(as.getEmail());
            }
            // Obtener info del festival
            if (ea.getCompraEntrada() != null && ea.getCompraEntrada().getEntrada() != null && ea.getCompraEntrada().getEntrada().getFestival() != null) {
                Festival f = ea.getCompraEntrada().getEntrada().getFestival();
                dto.setIdFestival(f.getIdFestival());
                dto.setNombreFestival(f.getNombre());
            }
        }
        return dto;
    }
}
