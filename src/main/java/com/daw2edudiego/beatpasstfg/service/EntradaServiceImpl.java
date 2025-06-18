package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daw2edudiego.beatpasstfg.mapper.EntradaMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private final EntradaMapper entradaMapper;

    public EntradaServiceImpl() {
        this.entradaRepository = new EntradaRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.tipoEntradaRepository = new TipoEntradaRepositoryImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.emailService = new EmailServiceImpl();
        this.entradaMapper = EntradaMapper.INSTANCE;
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
            return entradaMapper.entradaToEntradaDTO(entradaPersistida);
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
            return entradaMapper.entradaToEntradaDTO(entradaPersistida);
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
            return entradaMapper.toEntradaDTOList(entradas);
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

            return Optional.of(entradaMapper.entradaToEntradaDTO(entrada));
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

            return entradaOpt.map(entradaMapper::entradaToEntradaDTO);
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
}
