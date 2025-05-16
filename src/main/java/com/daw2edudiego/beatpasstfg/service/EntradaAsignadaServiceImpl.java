package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de EntradaAsignadaService.
 */
public class EntradaAsignadaServiceImpl implements EntradaAsignadaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaAsignadaServiceImpl.class);

    private final EntradaAsignadaRepository entradaAsignadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    private final EntradaRepository entradaRepository;
    private final AsistenteService asistenteService;
    private final EmailService emailService;

    public EntradaAsignadaServiceImpl() {
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.emailService = new EmailServiceImpl();
    }

    @Override
    public EntradaAsignadaDTO nominarEntrada(Integer idEntradaAsignada, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistente, Integer idPromotor) {
        log.info("Service - nominarEntrada (por ID): Iniciando para Entrada ID {}, Email Nom: {}, Promotor ID {}",
                idEntradaAsignada, emailAsistenteNominado, idPromotor);

        if (idEntradaAsignada == null || emailAsistenteNominado == null || emailAsistenteNominado.isBlank() || idPromotor == null) {
            log.error("Service - nominarEntrada (por ID): Parámetros inválidos. IDs de entrada, email de asistente nominado y promotor son requeridos.");
            throw new IllegalArgumentException("IDs de entrada, email de asistente nominado y promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        EntradaAsignada entradaPersistida = null;
        Asistente asistenteNominadoPersistido = null;
        EntradaAsignadaDTO entradaNominadaDTO = null;

        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Service - nominarEntrada (por ID): Buscando promotor ID {}", idPromotor);
            usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            log.debug("Service - nominarEntrada (por ID): Buscando entrada asignada ID {}", idEntradaAsignada);
            EntradaAsignada entradaAActualizar = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));

            log.debug("Service - nominarEntrada (por ID): Obteniendo festival de la entrada ID {}", idEntradaAsignada);
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAActualizar);
            log.debug("Service - nominarEntrada (por ID): Verificando propiedad del festival ID {} por promotor ID {}", festival.getIdFestival(), idPromotor);
            verificarPropiedadFestival(festival, idPromotor);

            if (entradaAActualizar.getAsistente() != null) {
                log.warn("Service - nominarEntrada (por ID): Intento de nominar entrada ID {} que ya está nominada a {}", idEntradaAsignada, entradaAActualizar.getAsistente().getEmail());
                throw new IllegalStateException("La entrada ID " + idEntradaAsignada + " ya está nominada al asistente: " + entradaAActualizar.getAsistente().getEmail());
            }
            if (entradaAActualizar.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                log.warn("Service - nominarEntrada (por ID): Intento de nominar entrada ID {} que no está ACTIVA. Estado actual: {}", idEntradaAsignada, entradaAActualizar.getEstado());
                throw new IllegalStateException("Solo se pueden nominar entradas que estén en estado ACTIVA. Estado actual: " + entradaAActualizar.getEstado());
            }

            log.debug("Service - nominarEntrada (por ID): Obteniendo o creando asistente con email {}", emailAsistenteNominado);
            // La creación/obtención de asistente se hace fuera de esta transacción si es necesario,
            // o se pasa el EntityManager si AsistenteService lo requiere para operar en la misma TX.
            // Por simplicidad, asumimos que asistenteService.obtenerOcrearAsistentePorEmail maneja su propia TX o es compatible.
            asistenteNominadoPersistido = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistente);

            log.debug("Service - nominarEntrada (por ID): Actualizando entrada ID {} con asistente ID {}", idEntradaAsignada, asistenteNominadoPersistido.getIdAsistente());
            entradaAActualizar.setAsistente(asistenteNominadoPersistido);
            entradaAActualizar.setFechaAsignacion(LocalDateTime.now());

            entradaPersistida = entradaAsignadaRepository.save(em, entradaAActualizar);
            entradaNominadaDTO = mapEntityToDto(entradaPersistida); // Mapear antes de commit para el email

            tx.commit();
            log.info("Service - nominarEntrada (por ID): COMMIT EXITOSO. Entrada ID {} nominada en BD. Asistente: {} ({}), Promotor: {}",
                    idEntradaAsignada, asistenteNominadoPersistido.getNombre(), asistenteNominadoPersistido.getEmail(), idPromotor);

            // --- Envío de Email al Nominado ---
            enviarEmailNominacionSiProcede(asistenteNominadoPersistido, entradaNominadaDTO, "nominarEntrada (por ID)");

            return entradaNominadaDTO;

        } catch (Exception e) {
            log.error("Service - nominarEntrada (por ID): Excepción general en el proceso de nominación para entrada ID {}. Error: {}", idEntradaAsignada, e.getMessage(), e);
            handleTransactionException(e, tx, "nominar entrada ID " + idEntradaAsignada);
            // Re-lanzar excepciones específicas para que el controlador las maneje
            if (e instanceof EntradaAsignadaNotFoundException || e instanceof UsuarioNotFoundException
                    || e instanceof SecurityException || e instanceof IllegalStateException
                    || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado durante la nominación de la entrada ID " + idEntradaAsignada + ": " + e.getMessage(), e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public EntradaAsignadaDTO nominarEntradaPorQr(String codigoQr, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistenteNominado) {
        log.info("Service - nominarEntradaPorQr: Iniciando para QR que empieza por '{}...', Email Nom: {}",
                (codigoQr != null && codigoQr.length() > 10) ? codigoQr.substring(0, 10) : codigoQr, emailAsistenteNominado);

        if (codigoQr == null || codigoQr.isBlank() || emailAsistenteNominado == null || emailAsistenteNominado.isBlank() || nombreAsistenteNominado == null || nombreAsistenteNominado.isBlank()) {
            log.error("Service - nominarEntradaPorQr: Parámetros inválidos. Código QR, email y nombre del asistente nominado son requeridos.");
            throw new IllegalArgumentException("Código QR, email y nombre del asistente nominado son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        EntradaAsignada entradaPersistida = null;
        Asistente asistenteNominadoPersistido = null;
        EntradaAsignadaDTO entradaNominadaDTO = null;

        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Service - nominarEntradaPorQr: Buscando entrada asignada por código QR: {}", codigoQr);
            EntradaAsignada entradaAActualizar = entradaAsignadaRepository.findByCodigoQr(em, codigoQr)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con código QR: " + codigoQr));

            Integer idEntradaAsignada = entradaAActualizar.getIdEntradaAsignada(); // Para logs

            if (entradaAActualizar.getAsistente() != null) {
                log.warn("Service - nominarEntradaPorQr: Intento de nominar entrada ID {} (QR: {}) que ya está nominada a {}",
                        idEntradaAsignada, codigoQr, entradaAActualizar.getAsistente().getEmail());
                throw new IllegalStateException("La entrada con QR " + codigoQr + " ya está nominada al asistente: " + entradaAActualizar.getAsistente().getEmail());
            }
            if (entradaAActualizar.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                log.warn("Service - nominarEntradaPorQr: Intento de nominar entrada ID {} (QR: {}) que no está ACTIVA. Estado actual: {}",
                        idEntradaAsignada, codigoQr, entradaAActualizar.getEstado());
                throw new IllegalStateException("Solo se pueden nominar entradas que estén en estado ACTIVA. Estado actual para QR " + codigoQr + ": " + entradaAActualizar.getEstado());
            }

            log.debug("Service - nominarEntradaPorQr: Obteniendo o creando asistente con email {}", emailAsistenteNominado);
            asistenteNominadoPersistido = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistenteNominado);

            log.debug("Service - nominarEntradaPorQr: Actualizando entrada ID {} (QR: {}) con asistente ID {}",
                    idEntradaAsignada, codigoQr, asistenteNominadoPersistido.getIdAsistente());
            entradaAActualizar.setAsistente(asistenteNominadoPersistido);
            entradaAActualizar.setFechaAsignacion(LocalDateTime.now());

            entradaPersistida = entradaAsignadaRepository.save(em, entradaAActualizar);
            entradaNominadaDTO = mapEntityToDto(entradaPersistida); // Mapear antes de commit para el email

            tx.commit();
            log.info("Service - nominarEntradaPorQr: COMMIT EXITOSO. Entrada ID {} (QR: {}) nominada en BD. Asistente: {} ({})",
                    idEntradaAsignada, codigoQr, asistenteNominadoPersistido.getNombre(), asistenteNominadoPersistido.getEmail());

            // --- Envío de Email al Nominado ---
            enviarEmailNominacionSiProcede(asistenteNominadoPersistido, entradaNominadaDTO, "nominarEntradaPorQr");

            return entradaNominadaDTO;

        } catch (Exception e) {
            log.error("Service - nominarEntradaPorQr: Excepción general en el proceso de nominación para entrada con QR {}. Error: {}", codigoQr, e.getMessage(), e);
            handleTransactionException(e, tx, "nominar entrada por QR " + codigoQr);
            if (e instanceof EntradaAsignadaNotFoundException || e instanceof IllegalStateException || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado durante la nominación de la entrada con QR " + codigoQr + ": " + e.getMessage(), e);
        } finally {
            closeEntityManager(em);
        }
    }

    private void enviarEmailNominacionSiProcede(Asistente asistente, EntradaAsignadaDTO entradaDTO, String metodoOrigen) {
        if (asistente != null && entradaDTO != null) {
            log.debug("Service - {}: Verificando condiciones para enviar email de nominación...", metodoOrigen);
            try {
                if (entradaDTO.getNombreFestival() == null || entradaDTO.getTipoEntradaOriginal() == null) {
                    log.error("Service - {}: Datos cruciales faltan en EntradaAsignadaDTO para el email. Festival: [{}], Tipo: [{}]. No se enviará email.",
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
                        metodoOrigen, entradaDTO.getIdEntradaAsignada(), asistente.getEmail(), emailEx.getMessage(), emailEx);
                // No relanzar la excepción para no afectar el flujo principal de nominación si el email falla
            }
        } else {
            log.warn("Service - {}: NO se intentará enviar email de nominación porque el asistente ({}) o entradaDTO ({}) es null.",
                    metodoOrigen, asistente != null, entradaDTO != null);
        }
    }

    @Override
    public List<EntradaAsignadaDTO> obtenerEntradasAsignadasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service - obtenerEntradasAsignadasPorFestival: Festival ID {}, Promotor ID {}", idFestival, idPromotor);
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

            List<EntradaAsignada> entradas = entradaAsignadaRepository.findByFestivalId(em, idFestival);

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
    public void cancelarEntrada(Integer idEntradaAsignada, Integer idPromotor) {
        log.info("Service - cancelarEntrada: Entrada ID {}, Promotor ID {}", idEntradaAsignada, idPromotor);
        if (idEntradaAsignada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada asignada e ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));

            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            verificarPropiedadFestival(festival, idPromotor);

            if (entradaAsignada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden cancelar entradas que estén en estado ACTIVA. Estado actual: " + entradaAsignada.getEstado());
            }

            entradaAsignada.setEstado(EstadoEntradaAsignada.CANCELADA);
            entradaAsignadaRepository.save(em, entradaAsignada);

            Entrada entradaOriginal = obtenerEntradaOriginal(entradaAsignada);
            em.lock(entradaOriginal, LockModeType.PESSIMISTIC_WRITE); // Bloqueo pesimista para actualizar stock
            int stockActual = entradaOriginal.getStock() != null ? entradaOriginal.getStock() : 0;
            entradaOriginal.setStock(stockActual + 1);
            entradaRepository.save(em, entradaOriginal);
            log.debug("Service - cancelarEntrada: Stock incrementado para Entrada Original ID {}. Nuevo stock: {}",
                    entradaOriginal.getIdEntrada(), entradaOriginal.getStock());

            tx.commit();
            log.info("Service - cancelarEntrada: Entrada ID {} cancelada y commit realizado. Promotor ID {}", idEntradaAsignada, idPromotor);

        } catch (Exception e) {
            log.error("Service - cancelarEntrada: Error para Entrada ID {}. Error: {}", idEntradaAsignada, e.getMessage(), e);
            handleTransactionException(e, tx, "cancelar entrada ID " + idEntradaAsignada);
            if (e instanceof EntradaAsignadaNotFoundException || e instanceof UsuarioNotFoundException
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
    public Optional<EntradaAsignadaDTO> obtenerEntradaAsignadaPorId(Integer idEntradaAsignada, Integer idPromotor) {
        log.debug("Service - obtenerEntradaAsignadaPorId: Entrada ID {}, Promotor ID {}", idEntradaAsignada, idPromotor);
        if (idEntradaAsignada == null || idPromotor == null) {
            throw new IllegalArgumentException("IDs de entrada asignada y promotor son requeridos.");
        }

        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();

            Optional<EntradaAsignada> entradaOpt = entradaAsignadaRepository.findById(em, idEntradaAsignada);

            if (entradaOpt.isEmpty()) {
                log.warn("Service - obtenerEntradaAsignadaPorId: Entrada asignada no encontrada con ID: {}", idEntradaAsignada);
                return Optional.empty();
            }

            EntradaAsignada entradaAsignada = entradaOpt.get();
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            // Verificar promotor antes de devolver datos sensibles
            usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));
            verificarPropiedadFestival(festival, idPromotor);

            return Optional.of(mapEntityToDto(entradaAsignada));

        } catch (Exception e) {
            // Loguear como warning, ya que puede ser un intento de acceso a una entrada no existente o sin permisos
            log.warn("Service - obtenerEntradaAsignadaPorId: No se pudo obtener entrada ID {} para promotor ID {}: {}", idEntradaAsignada, idPromotor, e.getMessage());
            if (e instanceof EntradaAsignadaNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException) {
                return Optional.empty(); // Devuelve vacío en casos esperados de "no encontrado" o "sin permiso"
            }
            throw mapServiceException(e); // Relanza otras excepciones inesperadas
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda ---
    private Festival obtenerFestivalDesdeEntradaAsignada(EntradaAsignada ea) {
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getEntrada() == null || ea.getCompraEntrada().getEntrada().getFestival() == null) {
            Integer eaId = (ea != null) ? ea.getIdEntradaAsignada() : null;
            String errorMsg = "Inconsistencia de datos para EntradaAsignada ID " + eaId + ": no se pudo obtener el festival asociado.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg); // Podría ser una DataIntegrityException personalizada
        }
        return ea.getCompraEntrada().getEntrada().getFestival();
    }

    private Entrada obtenerEntradaOriginal(EntradaAsignada ea) {
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getEntrada() == null) {
            Integer eaId = (ea != null) ? ea.getIdEntradaAsignada() : null;
            String errorMsg = "Inconsistencia de datos para EntradaAsignada ID " + eaId + ": no se pudo obtener la entrada original asociada.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        return ea.getCompraEntrada().getEntrada();
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
        if (e instanceof EntradaAsignadaNotFoundException || e instanceof UsuarioNotFoundException || e instanceof FestivalNotFoundException) {
            return (RuntimeException) e;
        }
        // Para cualquier otra excepción no esperada, envolverla
        return new RuntimeException("Error inesperado en el servicio de EntradaAsignada: " + e.getMessage(), e);
    }

    private EntradaAsignadaDTO mapEntityToDto(EntradaAsignada ea) {
        if (ea == null) {
            log.warn("mapEntityToDto recibió una EntradaAsignada nula.");
            return null; // O lanzar IllegalArgumentException si una entidad nula no es esperada aquí
        }
        EntradaAsignadaDTO dto = new EntradaAsignadaDTO();
        dto.setIdEntradaAsignada(ea.getIdEntradaAsignada());
        dto.setCodigoQr(ea.getCodigoQr());
        dto.setEstado(ea.getEstado());
        dto.setFechaAsignacion(ea.getFechaAsignacion()); // Puede ser null si aún no se ha asignado/nominado
        dto.setFechaUso(ea.getFechaUso());

        // Información de la compra y entrada original
        if (ea.getCompraEntrada() != null) {
            dto.setIdCompraEntrada(ea.getCompraEntrada().getIdCompraEntrada());
            if (ea.getCompraEntrada().getEntrada() != null) {
                Entrada entradaOriginal = ea.getCompraEntrada().getEntrada();
                dto.setIdEntradaOriginal(entradaOriginal.getIdEntrada());
                dto.setTipoEntradaOriginal(entradaOriginal.getTipo());
                if (entradaOriginal.getFestival() != null) {
                    dto.setIdFestival(entradaOriginal.getFestival().getIdFestival());
                    dto.setNombreFestival(entradaOriginal.getFestival().getNombre());
                } else {
                    log.warn("La entrada original ID {} (asociada a EntradaAsignada ID {}) no tiene un festival vinculado.", entradaOriginal.getIdEntrada(), ea.getIdEntradaAsignada());
                }
            } else {
                log.warn("La CompraEntrada ID {} (asociada a EntradaAsignada ID {}) no tiene una entrada original vinculada.", ea.getCompraEntrada().getIdCompraEntrada(), ea.getIdEntradaAsignada());
            }
        } else {
            log.warn("EntradaAsignada ID {} no tiene una CompraEntrada vinculada. Esto podría ser normal para entradas creadas manualmente (no compradas).", ea.getIdEntradaAsignada());
        }

        // Información del asistente nominado (si existe)
        if (ea.getAsistente() != null) {
            dto.setIdAsistente(ea.getAsistente().getIdAsistente());
            dto.setNombreAsistente(ea.getAsistente().getNombre());
            dto.setEmailAsistente(ea.getAsistente().getEmail());
        } else {
            // Es normal que el asistente sea null si la entrada aún no ha sido nominada
            log.trace("EntradaAsignada ID {} no tiene un asistente nominado en el momento del mapeo a DTO (mapEntityToDto).", ea.getIdEntradaAsignada());
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
                log.warn("No se pudo generar la imagen QR para el DTO de la entrada asignada ID {}", ea.getIdEntradaAsignada());
            }
        } else {
            log.warn("El código QR es nulo o vacío para la EntradaAsignada ID {} al intentar generar imagen para DTO.", ea.getIdEntradaAsignada());
        }
        log.trace("Mapeo de Entidad a DTO para EntradaAsignada ID {}: DTO resultante: {}", ea.getIdEntradaAsignada(), dto);
        return dto;
    }
}
