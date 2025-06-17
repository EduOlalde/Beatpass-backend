package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import com.daw2edudiego.beatpasstfg.util.QRCodeUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de EntradaService.
 */
public class EntradaServiceImpl implements EntradaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaServiceImpl.class);

    private final EntradaRepository entradaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final AsistenteService asistenteService;
    private final EmailService emailService;

    public EntradaServiceImpl() {
        this.entradaRepository = new EntradaRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.tipoEntradaRepository = new TipoEntradaRepositoryImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.emailService = new EmailServiceImpl();
    }

    @Override
    public EntradaDTO nominarEntrada(Integer idEntrada, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistente, Integer idPromotor) {
        log.info("Service - nominarEntrada (por ID): Iniciando para Entrada ID {}, Email Nom: {}, Promotor ID {}",
                idEntrada, emailAsistenteNominado, idPromotor);

        if (idEntrada == null || emailAsistenteNominado == null || emailAsistenteNominado.isBlank() || idPromotor == null) {
            log.error("Service - nominarEntrada (por ID): Parámetros inválidos. IDs de entrada, email de asistente nominado y promotor son requeridos.");
            throw new IllegalArgumentException("IDs de entrada, email de asistente nominado y promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        Entrada entradaPersistida = null;
        Asistente asistenteNominadoPersistido = null;
        EntradaDTO entradaNominadaDTO = null;

        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Service - nominarEntrada (por ID): Buscando promotor ID {}", idPromotor);
            usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            log.debug("Service - nominarEntrada (por ID): Buscando entrada ID {}", idEntrada);
            Entrada entradaAActualizar = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con ID: " + idEntrada));

            log.debug("Service - nominarEntrada (por ID): Obteniendo festival de la entrada ID {}", idEntrada);
            Festival festival = obtenerFestivalDesdeEntrada(entradaAActualizar);
            log.debug("Service - nominarEntrada (por ID): Verificando propiedad del festival ID {} por promotor ID {}", festival.getIdFestival(), idPromotor);
            verificarPropiedadFestival(festival, idPromotor);

            if (entradaAActualizar.getAsistente() != null) {
                log.warn("Service - nominarEntrada (por ID): Intento de nominar entrada ID {} que ya está nominada a {}", idEntrada, entradaAActualizar.getAsistente().getEmail());
                throw new IllegalStateException("La entrada ID " + idEntrada + " ya está nominada al asistente: " + entradaAActualizar.getAsistente().getEmail());
            }
            if (entradaAActualizar.getEstado() != EstadoEntrada.ACTIVA) {
                log.warn("Service - nominarEntrada (por ID): Intento de nominar entrada ID {} que no está ACTIVA. Estado actual: {}", idEntrada, entradaAActualizar.getEstado());
                throw new IllegalStateException("Solo se pueden nominar entradas que estén en estado ACTIVA. Estado actual: " + entradaAActualizar.getEstado());
            }

            log.debug("Service - nominarEntrada (por ID): Obteniendo o creando asistente con email {}", emailAsistenteNominado);
            asistenteNominadoPersistido = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistente);

            log.debug("Service - nominarEntrada (por ID): Actualizando entrada ID {} con asistente ID {}", idEntrada, asistenteNominadoPersistido.getIdAsistente());
            entradaAActualizar.setAsistente(asistenteNominadoPersistido);
            entradaAActualizar.setFechaAsignacion(LocalDateTime.now()); // <<-- CORREGIDO: Usamos LocalDateTime aquí porque el campo de la ENTIDAD es LocalDateTime

            entradaPersistida = entradaRepository.save(em, entradaAActualizar);
            entradaNominadaDTO = mapEntityToDto(entradaPersistida); // Mapear antes de commit para el email

            tx.commit();
            log.info("Service - nominarEntrada (por ID): COMMIT EXITOSO. Entrada ID {} nominada en BD. Asistente: {} ({}), Promotor: {}",
                    idEntrada, asistenteNominadoPersistido.getNombre(), asistenteNominadoPersistido.getEmail(), idPromotor);

            // --- Envío de Email al Nominado ---
            enviarEmailNominacionSiProcede(asistenteNominadoPersistido, entradaNominadaDTO, "nominarEntrada (por ID)");

            return entradaNominadaDTO;

        } catch (Exception e) {
            log.error("Service - nominarEntrada (por ID): Excepción general en el proceso de nominación para entrada ID {}. Error: {}", idEntrada, e.getMessage(), e);
            handleTransactionException(e, tx, "nominar entrada ID " + idEntrada);
            if (e instanceof EntradaNotFoundException || e instanceof UsuarioNotFoundException
                    || e instanceof SecurityException || e instanceof IllegalStateException
                    || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado durante la nominación de la entrada ID " + idEntrada + ": " + e.getMessage(), e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public EntradaDTO nominarEntradaPorQr(String codigoQr, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistenteNominado) {
        log.info("Service - nominarEntradaPorQr: Iniciando para QR que empieza por '{}...', Email Nom: {}",
                (codigoQr != null && codigoQr.length() > 10) ? codigoQr.substring(0, 10) : codigoQr, emailAsistenteNominado);

        if (codigoQr == null || codigoQr.isBlank() || emailAsistenteNominado == null || emailAsistenteNominado.isBlank() || nombreAsistenteNominado == null || nombreAsistenteNominado.isBlank()) {
            log.error("Service - nominarEntradaPorQr: Parámetros inválidos. Código QR, email y nombre del asistente nominado son requeridos.");
            throw new IllegalArgumentException("Código QR, email y nombre del asistente nominado son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        Entrada entradaPersistida = null;
        Asistente asistenteNominadoPersistido = null;
        EntradaDTO entradaNominadaDTO = null;

        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Service - nominarEntradaPorQr: Buscando entrada por código QR: {}", codigoQr);
            Entrada entradaAActualizar = entradaRepository.findByCodigoQr(em, codigoQr)
                    .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con código QR: " + codigoQr));

            Integer idEntrada = entradaAActualizar.getIdEntrada(); // Para logs

            if (entradaAActualizar.getAsistente() != null) {
                log.warn("Service - nominarEntradaPorQr: Intento de nominar entrada ID {} (QR: {}) que ya está nominada a {}",
                        idEntrada, codigoQr, entradaAActualizar.getAsistente().getEmail());
                throw new IllegalStateException("La entrada con QR " + codigoQr + " ya está nominada al asistente: " + entradaAActualizar.getAsistente().getEmail());
            }
            if (entradaAActualizar.getEstado() != EstadoEntrada.ACTIVA) {
                log.warn("Service - nominarEntradaPorQr: Intento de nominar entrada ID {} (QR: {}) que no está ACTIVA. Estado actual: {}",
                        idEntrada, codigoQr, entradaAActualizar.getEstado());
                throw new IllegalStateException("Solo se pueden nominar entradas que estén en estado ACTIVA. Estado actual para QR " + codigoQr + ": " + entradaAActualizar.getEstado());
            }

            log.debug("Service - nominarEntradaPorQr: Obteniendo o creando asistente con email {}", emailAsistenteNominado);
            asistenteNominadoPersistido = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistenteNominado);

            log.debug("Service - nominarEntradaPorQr: Actualizando entrada ID {} (QR: {}) con asistente ID {}",
                    idEntrada, codigoQr, asistenteNominadoPersistido.getIdAsistente());
            entradaAActualizar.setAsistente(asistenteNominadoPersistido);
            entradaAActualizar.setFechaAsignacion(LocalDateTime.now()); // <<-- CORREGIDO: Usamos LocalDateTime aquí porque el campo de la ENTIDAD es LocalDateTime

            entradaPersistida = entradaRepository.save(em, entradaAActualizar);
            entradaNominadaDTO = mapEntityToDto(entradaPersistida); // Mapear antes de commit para el email

            tx.commit();
            log.info("Service - nominarEntradaPorQr: COMMIT EXITOSO. Entrada ID {} (QR: {}) nominada en BD. Asistente: {} ({})",
                    idEntrada, codigoQr, asistenteNominadoPersistido.getNombre(), asistenteNominadoPersistido.getEmail());

            // --- Envío de Email al Nominado ---
            enviarEmailNominacionSiProcede(asistenteNominadoPersistido, entradaNominadaDTO, "nominarEntradaPorQr");

            return entradaNominadaDTO;

        } catch (Exception e) {
            log.error("Service - nominarEntradaPorQr: Excepción general en el proceso de nominación para entrada con QR {}. Error: {}", codigoQr, e.getMessage(), e);
            handleTransactionException(e, tx, "nominar entrada por QR " + codigoQr);
            if (e instanceof EntradaNotFoundException || e instanceof IllegalStateException || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado durante la nominación de la entrada con QR " + codigoQr + ": " + e.getMessage(), e);
        } finally {
            closeEntityManager(em);
        }
    }

    private void enviarEmailNominacionSiProcede(Asistente asistente, EntradaDTO entradaDTO, String metodoOrigen) {
        if (asistente != null && entradaDTO != null) {
            log.debug("Service - {}: Verificando condiciones para enviar email de nominación...", metodoOrigen);
            try {
                if (entradaDTO.getNombreFestival() == null || entradaDTO.getTipoEntradaOriginal() == null) {
                    log.error("Service - {}: Datos cruciales faltan en EntradaDTO para el email. Festival: [{}], Tipo: [{}]. No se enviará email.",
                            metodoOrigen, entradaDTO.getNombreFestival(), entradaDTO.getTipoEntradaOriginal());
                } else {
                    log.info("Service - {}: Llamando a emailService.enviarEmailEntradaNominada para {}", metodoOrigen, asistente.getEmail());
                    emailService.enviarEmailEntradaNominada(
                            asistente.getEmail(),
                            asistente.getNombre(),
                            entradaDTO
                    );
                    log.info("Service - {}: Llamada a emailService.enviarEmailEntradaNominada completada para {}.", metodoOrigen, asistente.getEmail());
                }
            } catch (Exception emailEx) {
                log.error("Service - {}: Excepción durante el intento de envío de email de nominación para entrada ID {} a {}: {}",
                        metodoOrigen, entradaDTO.getIdEntrada(), asistente.getEmail(), emailEx.getMessage(), emailEx);
                // No relanzar la excepción para no afectar el flujo principal de nominación si el email falla
            }
        } else {
            log.warn("Service - {}: NO se intentará enviar email de nominación porque el asistente ({}) o entradaDTO ({}) es null.",
                    metodoOrigen, asistente != null, entradaDTO != null);
        }
    }

    @Override
    public List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service - obtenerEntradasPorFestival: Festival ID {}, Promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival e ID de promotor son requeridos.");
        }

        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();

            usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor);

            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);

            log.info("Service - obtenerEntradasAsignadasPorFestival: Encontradas {} entradas para festival ID {}", entradas.size(), idFestival);
            return entradas.stream()
                    .map(this::mapEntityToDto) // Reutiliza el mapeador
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Service - obtenerEntradasAsignadasPorFestival: Error para festival ID {} y promotor ID {}: {}", idFestival, idPromotor, e.getMessage(), e);
            if (e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException) {
                throw e;
            }
            throw mapServiceException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public void cancelarEntrada(Integer idEntrada, Integer idPromotor) {
        log.info("Service - cancelarEntrada: Entrada ID {}, Promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada e ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con ID: " + idEntrada));

            Festival festival = obtenerFestivalDesdeEntrada(entrada);
            verificarPropiedadFestival(festival, idPromotor);

            if (entrada.getEstado() != EstadoEntrada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden cancelar entradas que estén en estado ACTIVA. Estado actual: " + entrada.getEstado());
            }

            entrada.setEstado(EstadoEntrada.CANCELADA);
            entradaRepository.save(em, entrada);

            TipoEntrada entradaOriginal = obtenerEntradaOriginal(entrada);
            em.lock(entradaOriginal, LockModeType.PESSIMISTIC_WRITE); // Bloqueo pesimista para actualizar stock
            int stockActual = entradaOriginal.getStock() != null ? entradaOriginal.getStock() : 0;
            entradaOriginal.setStock(stockActual + 1);
            tipoEntradaRepository.save(em, entradaOriginal);
            log.debug("Service - cancelarEntrada: Stock incrementado para Entrada Original ID {}. Nuevo stock: {}",
                    entradaOriginal.getIdTipoEntrada(), entradaOriginal.getStock());

            tx.commit();
            log.info("Service - cancelarEntrada: Entrada ID {} cancelada y commit realizado. Promotor ID {}", idEntrada, idPromotor);

        } catch (Exception e) {
            log.error("Service - cancelarEntrada: Error para Entrada ID {}. Error: {}", idEntrada, e.getMessage(), e);
            handleTransactionException(e, tx, "cancelar entrada ID " + idEntrada);
            if (e instanceof EntradaNotFoundException || e instanceof UsuarioNotFoundException
                    || e instanceof SecurityException || e instanceof IllegalStateException
                    || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw mapServiceException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor) {
        log.debug("Service - obtenerEntradaPorId: Entrada ID {}, Promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("IDs de entrada y promotor son requeridos.");
        }

        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();

            Optional<Entrada> entradaOpt = entradaRepository.findById(em, idEntrada);

            if (entradaOpt.isEmpty()) {
                log.warn("Service - obtenerEntradaPorId: Entrada no encontrada con ID: {}", idEntrada);
                return Optional.empty();
            }

            Entrada entrada = entradaOpt.get();
            Festival festival = obtenerFestivalDesdeEntrada(entrada);
            // Verificar promotor antes de devolver datos sensibles
            usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));
            verificarPropiedadFestival(festival, idPromotor);

            return Optional.of(mapEntityToDto(entrada));

        } catch (Exception e) {
            // Loguear como warning, ya que puede ser un intento de acceso a una entrada no existente o sin permisos
            log.warn("Service - obtenerEntradaPorId: No se pudo obtener entrada ID {} para promotor ID {}: {}", idEntrada, idPromotor, e.getMessage());
            if (e instanceof EntradaNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException) {
                return Optional.empty(); // Devuelve vacío en casos esperados de "no encontrado" o "sin permiso"
            }
            throw mapServiceException(e); // Relanza otras excepciones inesperadas
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<EntradaDTO> obtenerParaNominacionPublicaPorQr(String codigoQr) {
        log.debug("Service - obtenerParaNominacionPublicaPorQr: Buscando entrada por QR: {}", codigoQr);
        if (codigoQr == null || codigoQr.isBlank()) {
            log.warn("Service - obtenerParaNominacionPublicaPorQr: Código QR nulo o vacío.");
            return Optional.empty();
        }

        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            Optional<Entrada> entradaOpt = entradaRepository.findByCodigoQr(em, codigoQr);

            if (entradaOpt.isEmpty()) {
                log.warn("Service - obtenerParaNominacionPublicaPorQr: Entrada no encontrada con QR: {}", codigoQr);
                return Optional.empty();
            }

            return entradaOpt.map(this::mapEntityToDto);

        } catch (Exception e) {
            log.error("Service - obtenerParaNominacionPublicaPorQr: Error al buscar entrada por QR {}: {}", codigoQr, e.getMessage(), e);
            // No relanzar la excepción aquí, simplemente devolver Optional.empty()
            // para que el controlador maneje la no existencia.
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda ---
    private Festival obtenerFestivalDesdeEntrada(Entrada ea) {
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getTipoEntrada() == null || ea.getCompraEntrada().getTipoEntrada().getFestival() == null) {
            Integer eaId = (ea != null) ? ea.getIdEntrada() : null;
            String errorMsg = "Inconsistencia de datos para Entrada ID " + eaId + ": no se pudo obtener el festival asociado.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        return ea.getCompraEntrada().getTipoEntrada().getFestival();
    }

    private TipoEntrada obtenerEntradaOriginal(Entrada ea) {
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getTipoEntrada() == null) {
            Integer eaId = (ea != null) ? ea.getIdEntrada() : null;
            String errorMsg = "Inconsistencia de datos para Entrada ID " + eaId + ": no se pudo obtener la entrada original asociada.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        return ea.getCompraEntrada().getTipoEntrada();
    }

    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
            throw new FestivalNotFoundException("El festival asociado no puede ser nulo para la verificación de propiedad.");
        }
        if (idPromotor == null) { // Aunque ya validado en el método público, defensa en profundidad
            throw new UsuarioNotFoundException("El ID del promotor no puede ser nulo para la verificación de propiedad.");
        }
        if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            log.warn("Intento de acceso no autorizado por promotor ID {} al festival ID {} (propiedad de promotor ID {})",
                    idPromotor, festival.getIdFestival(), festival.getPromotor() != null ? festival.getPromotor().getIdUsuario() : "DESCONOCIDO");
            throw new SecurityException("El usuario no tiene permiso para acceder o modificar los recursos de este festival.");
        }
    }

    private void rollbackTransaction(EntityTransaction tx, String actionContext) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Rollback de transacción de '{}' realizado.", actionContext);
            } catch (Exception rbEx) {
                log.error("Error crítico durante el rollback de la transacción '{}': {}", actionContext, rbEx.getMessage(), rbEx);
            }
        }
    }

    private void handleTransactionException(Exception e, EntityTransaction tx, String actionContext) {
        // El log de error específico de la acción ya se hace en el bloque catch principal del método de servicio
        rollbackTransaction(tx, actionContext);
    }

    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                em.close();
            } catch (Exception e_close) {
                log.error("Error al cerrar EntityManager después de la acción: {}", e_close.getMessage(), e_close);
            }
        }
    }

    private RuntimeException mapServiceException(Exception e) {
        // Mapea excepciones JPA u otras a excepciones de negocio o Runtime si es necesario,
        // o simplemente las relanza si ya son del tipo adecuado.
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException || e instanceof SecurityException || e instanceof PersistenceException) {
            return (RuntimeException) e;
        }
        // Excepciones personalizadas que ya son RuntimeException
        if (e instanceof EntradaNotFoundException || e instanceof UsuarioNotFoundException || e instanceof FestivalNotFoundException) {
            return (RuntimeException) e;
        }
        // Para cualquier otra excepción no esperada, envolverla
        return new RuntimeException("Error inesperado en el servicio de Entrada: " + e.getMessage(), e);
    }

    private EntradaDTO mapEntityToDto(Entrada ea) {
        if (ea == null) {
            log.warn("mapEntityToDto recibió una Entrada nula.");
            return null; // O lanzar IllegalArgumentException si una entidad nula no es esperada aquí
        }
        EntradaDTO dto = new EntradaDTO();
        dto.setIdEntrada(ea.getIdEntrada());
        dto.setCodigoQr(ea.getCodigoQr());
        dto.setEstado(ea.getEstado());

        if (ea.getFechaAsignacion() != null) {
            dto.setFechaAsignacion(Date.from(ea.getFechaAsignacion().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (ea.getFechaUso() != null) {
            dto.setFechaUso(Date.from(ea.getFechaUso().atZone(ZoneId.systemDefault()).toInstant()));
        }

        // Información de la compra y entrada original
        if (ea.getCompraEntrada() != null) {
            dto.setIdCompraEntrada(ea.getCompraEntrada().getIdCompraEntrada());
            if (ea.getCompraEntrada().getTipoEntrada() != null) {
                TipoEntrada tipoEntradaOriginal = ea.getCompraEntrada().getTipoEntrada();
                dto.setIdEntradaOriginal(tipoEntradaOriginal.getIdTipoEntrada());
                dto.setTipoEntradaOriginal(tipoEntradaOriginal.getTipo());
                dto.setRequiereNominacion(tipoEntradaOriginal.getRequiereNominacion()); 
                if (tipoEntradaOriginal.getFestival() != null) {
                    dto.setIdFestival(tipoEntradaOriginal.getFestival().getIdFestival());
                    dto.setNombreFestival(tipoEntradaOriginal.getFestival().getNombre());
                } else {
                    log.warn("La entrada original ID {} (asociada a Entrada ID {}) no tiene un festival vinculado.", tipoEntradaOriginal.getIdTipoEntrada(), ea.getIdEntrada());
                }
            } else {
                log.warn("La CompraEntrada ID {} (asociada a Entrada ID {}) no tiene una entrada original vinculada.", ea.getCompraEntrada().getIdCompraEntrada(), ea.getIdEntrada());
            }
        } else {
            log.warn("Entrada ID {} no tiene una CompraEntrada vinculada. Esto podría ser normal para entradas creadas manualmente (no compradas).", ea.getIdEntrada());
        }

        // Información del asistente nominado (si existe)
        if (ea.getAsistente() != null) {
            dto.setIdAsistente(ea.getAsistente().getIdAsistente());
            dto.setNombreAsistente(ea.getAsistente().getNombre());
            dto.setEmailAsistente(ea.getAsistente().getEmail());
        } else {
            // Es normal que el asistente sea null si la entrada aún no ha sido nominada
            log.trace("Entrada ID {} no tiene un asistente nominado en el momento del mapeo a DTO (mapEntityToDto).", ea.getIdEntrada());
        }

        // Información de la pulsera (si está asociada)
        if (ea.getPulseraAsociada() != null) {
            dto.setIdPulseraAsociada(ea.getPulseraAsociada().getIdPulsera());
            dto.setCodigoUidPulsera(ea.getPulseraAsociada().getCodigoUid());
        }

        // Generar imagen QR para el DTO (si aplica y el código QR existe)
        if (ea.getCodigoQr() != null && !ea.getCodigoQr().isBlank()) {
            // Considerar si la generación de la imagen QR debe estar aquí o en una capa de presentación/utilidad específica para DTOs.
            // Por ahora, se mantiene como estaba.
            String imageDataUrl = QRCodeUtil.generarQrComoBase64(ea.getCodigoQr(), 100, 100);
            if (imageDataUrl != null) {
                dto.setQrCodeImageDataUrl(imageDataUrl);
            } else {
                log.warn("No se pudo generar la imagen QR para el DTO de la entrada ID {}", ea.getIdEntrada());
            }
        } else {
            log.warn("El código QR es nulo o vacío para la Entrada ID {} al intentar generar imagen para DTO.", ea.getIdEntrada());
        }
        log.trace("Mapeo de Entidad a DTO para Entrada ID {}: DTO resultante: {}", ea.getIdEntrada(), dto);
        return dto;
    }
}
