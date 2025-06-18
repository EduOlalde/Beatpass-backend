package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.QRCodeUtil;
import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de EntradaService.
 */
public class EntradaServiceImpl extends AbstractService implements EntradaService {

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

        // Use executeTransactional for the entire nomination process
        EntradaDTO entradaNominadaDTO = executeTransactional(em -> {
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
            Asistente asistenteNominadoPersistido = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistente);

            log.debug("Service - nominarEntrada (por ID): Actualizando entrada ID {} con asistente ID {}", idEntrada, asistenteNominadoPersistido.getIdAsistente());
            entradaAActualizar.setAsistente(asistenteNominadoPersistido);
            entradaAActualizar.setFechaAsignacion(LocalDateTime.now());

            Entrada entradaPersistida = entradaRepository.save(em, entradaAActualizar);
            return mapEntityToDto(entradaPersistida);
        }, "nominarEntrada (por ID) " + idEntrada);

        // --- Envío de Email al Nominado ---
        enviarEmailNominacionSiProcede(asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistente), entradaNominadaDTO, "nominarEntrada (por ID)");

        return entradaNominadaDTO;
    }

    @Override
    public EntradaDTO nominarEntradaPorQr(String codigoQr, String emailAsistenteNominado, String nombreAsistenteNominado, String telefonoAsistenteNominado) {
        log.info("Service - nominarEntradaPorQr: Iniciando para QR que empieza por '{}...', Email Nom: {}",
                (codigoQr != null && codigoQr.length() > 10) ? codigoQr.substring(0, 10) : codigoQr, emailAsistenteNominado);

        if (codigoQr == null || codigoQr.isBlank() || emailAsistenteNominado == null || emailAsistenteNominado.isBlank() || nombreAsistenteNominado == null || nombreAsistenteNominado.isBlank()) {
            log.error("Service - nominarEntradaPorQr: Parámetros inválidos. Código QR, email y nombre del asistente nominado son requeridos.");
            throw new IllegalArgumentException("Código QR, email y nombre del asistente nominado son requeridos.");
        }

        EntradaDTO entradaNominadaDTO = executeTransactional(em -> {
            log.debug("Service - nominarEntradaPorQr: Buscando entrada por código QR: {}", codigoQr);
            Entrada entradaAActualizar = entradaRepository.findByCodigoQr(em, codigoQr)
                    .orElseThrow(() -> new EntradaNotFoundException("Entrada no encontrada con código QR: " + codigoQr));

            Integer idEntrada = entradaAActualizar.getIdEntrada();

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
            Asistente asistenteNominadoPersistido = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistenteNominado);

            log.debug("Service - nominarEntradaPorQr: Actualizando entrada ID {} (QR: {}) con asistente ID {}",
                    idEntrada, codigoQr, asistenteNominadoPersistido.getIdAsistente());
            entradaAActualizar.setAsistente(asistenteNominadoPersistido);
            entradaAActualizar.setFechaAsignacion(LocalDateTime.now());

            Entrada entradaPersistida = entradaRepository.save(em, entradaAActualizar);
            return mapEntityToDto(entradaPersistida);
        }, "nominarEntradaPorQr " + codigoQr);

        // --- Envío de Email al Nominado ---
        enviarEmailNominacionSiProcede(asistenteService.obtenerOcrearAsistentePorEmail(emailAsistenteNominado, nombreAsistenteNominado, telefonoAsistenteNominado), entradaNominadaDTO, "nominarEntradaPorQr");

        return entradaNominadaDTO;
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

        return executeRead(em -> {
            usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor);

            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);

            log.info("Service - obtenerEntradasAsignadasPorFestival: Encontradas {} entradas para festival ID {}", entradas.size(), idFestival);
            return entradas.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        }, "obtenerEntradasPorFestival " + idFestival);
    }

    @Override
    public void cancelarEntrada(Integer idEntrada, Integer idPromotor) {
        log.info("Service - cancelarEntrada: Entrada ID {}, Promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada e ID de promotor son requeridos.");
        }

        executeTransactional(em -> {
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
            return null;
        }, "cancelarEntrada " + idEntrada);
    }

    @Override
    public Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor) {
        log.debug("Service - obtenerEntradaPorId: Entrada ID {}, Promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("IDs de entrada y promotor son requeridos.");
        }

        return executeRead(em -> {
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
        }, "obtenerEntradaPorId " + idEntrada);
    }

    @Override
    public Optional<EntradaDTO> obtenerParaNominacionPublicaPorQr(String codigoQr) {
        log.debug("Service - obtenerParaNominacionPublicaPorQr: Buscando entrada por QR: {}", codigoQr);
        if (codigoQr == null || codigoQr.isBlank()) {
            log.warn("Service - obtenerParaNominacionPublicaPorQr: Código QR nulo o vacío.");
            return Optional.empty();
        }

        return executeRead(em -> {
            Optional<Entrada> entradaOpt = entradaRepository.findByCodigoQr(em, codigoQr);

            if (entradaOpt.isEmpty()) {
                log.warn("Service - obtenerParaNominacionPublicaPorQr: Entrada no encontrada con QR: {}", codigoQr);
                return Optional.empty();
            }

            return entradaOpt.map(this::mapEntityToDto);
        }, "obtenerParaNominacionPublicaPorQr " + codigoQr);
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
        if (idPromotor == null) {
            throw new UsuarioNotFoundException("El ID del promotor no puede ser nulo para la verificación de propiedad.");
        }
        if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            log.warn("Intento de acceso no autorizado por promotor ID {} al festival ID {} (propiedad de promotor ID {})",
                    idPromotor, festival.getIdFestival(), festival.getPromotor() != null ? festival.getPromotor().getIdUsuario() : "DESCONOCIDO");
            throw new SecurityException("El usuario no tiene permiso para acceder o modificar los recursos de este festival.");
        }
    }

    // The closeEntityManager, rollbackTransaction, handleTransactionException, mapServiceException methods are now in AbstractService.
    // Remove them from here.
    private EntradaDTO mapEntityToDto(Entrada ea) {
        if (ea == null) {
            log.warn("mapEntityToDto recibió una Entrada nula.");
            return null;
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
