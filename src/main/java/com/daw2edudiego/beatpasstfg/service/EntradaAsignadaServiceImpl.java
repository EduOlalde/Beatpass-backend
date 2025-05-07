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

    public EntradaAsignadaServiceImpl() {
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.asistenteService = new AsistenteServiceImpl();
    }

    @Override
    public void nominarEntrada(Integer idEntradaAsignada, String emailAsistente, String nombreAsistente, String telefonoAsistente, Integer idPromotor) {
        log.info("Service: Iniciando nominación de entrada ID {} a asistente email {} por promotor ID {}",
                idEntradaAsignada, emailAsistente, idPromotor);

        if (idEntradaAsignada == null || emailAsistente == null || emailAsistente.isBlank() || idPromotor == null) {
            throw new IllegalArgumentException("IDs de entrada, email de asistente y promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));

            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            verificarPropiedadFestival(festival, idPromotor);

            if (entradaAsignada.getAsistente() != null) {
                throw new IllegalStateException("La entrada ID " + idEntradaAsignada + " ya está nominada al asistente: " + entradaAsignada.getAsistente().getEmail());
            }
            if (entradaAsignada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden nominar entradas que estén en estado ACTIVA. Estado actual: " + entradaAsignada.getEstado());
            }

            // Usar AsistenteService para obtener/crear
            Asistente asistente = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistente, nombreAsistente, telefonoAsistente);

            entradaAsignada.setAsistente(asistente);
            entradaAsignada.setFechaAsignacion(LocalDateTime.now());
            entradaAsignadaRepository.save(em, entradaAsignada);

            tx.commit();
            log.info("Entrada ID {} nominada exitosamente al asistente ID {} (Email: {}) por promotor ID {}",
                    idEntradaAsignada, asistente.getIdAsistente(), asistente.getEmail(), idPromotor);

        } catch (Exception e) {
            handleException(e, tx, "nominar entrada ID " + idEntradaAsignada);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<EntradaAsignadaDTO> obtenerEntradasAsignadasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo entradas asignadas para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival e ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor);

            List<EntradaAsignada> entradas = entradaAsignadaRepository.findByFestivalId(em, idFestival);
            tx.commit();

            log.info("Encontradas {} entradas asignadas para el festival ID {}", entradas.size(), idFestival);
            return entradas.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            handleException(e, tx, "obtener entradas asignadas por festival ID " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public void cancelarEntrada(Integer idEntradaAsignada, Integer idPromotor) {
        log.info("Service: Iniciando cancelación de entrada ID {} por promotor ID {}", idEntradaAsignada, idPromotor);
        if (idEntradaAsignada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada asignada e ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario promotor = usuarioRepository.findById(em, idPromotor)
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
            em.lock(entradaOriginal, LockModeType.PESSIMISTIC_WRITE); // Bloquear para actualizar stock
            int stockActual = entradaOriginal.getStock() != null ? entradaOriginal.getStock() : 0;
            entradaOriginal.setStock(stockActual + 1);
            entradaRepository.save(em, entradaOriginal);
            log.info("Stock incrementado para Entrada ID {}. Nuevo stock: {}",
                    entradaOriginal.getIdEntrada(), entradaOriginal.getStock());

            tx.commit();
            log.info("Entrada ID {} cancelada exitosamente por promotor ID {}", idEntradaAsignada, idPromotor);

        } catch (Exception e) {
            handleException(e, tx, "cancelar entrada ID " + idEntradaAsignada);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<EntradaAsignadaDTO> obtenerEntradaAsignadaPorId(Integer idEntradaAsignada, Integer idPromotor) {
        log.debug("Service: Obteniendo entrada asignada ID {} por promotor ID {}", idEntradaAsignada, idPromotor);
        if (idEntradaAsignada == null || idPromotor == null) {
            throw new IllegalArgumentException("IDs de entrada asignada y promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Optional<EntradaAsignada> entradaOpt = entradaAsignadaRepository.findById(em, idEntradaAsignada);

            if (entradaOpt.isEmpty()) {
                tx.commit();
                log.warn("Entrada asignada no encontrada con ID: {}", idEntradaAsignada);
                return Optional.empty();
            }

            EntradaAsignada entradaAsignada = entradaOpt.get();
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            verificarPropiedadFestival(festival, idPromotor);
            tx.commit();

            return Optional.of(mapEntityToDto(entradaAsignada));

        } catch (Exception e) {
            handleException(e, tx, "obtener entrada asignada por ID " + idEntradaAsignada);
            if (e instanceof EntradaAsignadaNotFoundException || e instanceof SecurityException) {
                log.warn("No se pudo obtener entrada asignada ID {} para promotor ID {}: {}", idEntradaAsignada, idPromotor, e.getMessage());
                return Optional.empty();
            }
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda ---
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
     * Obtiene la Entrada original asociada a una EntradaAsignada.
     */
    private Entrada obtenerEntradaOriginal(EntradaAsignada ea) {
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getEntrada() == null) {
            Integer eaId = (ea != null) ? ea.getIdEntradaAsignada() : null;
            log.error("Inconsistencia de datos para EntradaAsignada ID {}: no se pudo obtener la entrada original asociada.", eaId);
            throw new IllegalStateException("Error interno: no se pudo determinar la entrada original de la entrada asignada ID " + eaId);
        }
        return ea.getCompraEntrada().getEntrada();
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
     * Realiza rollback de transacción si está activa.
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
     * Manejador genérico de excepciones.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
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
        if (e instanceof AsistenteNotFoundException || e instanceof EntradaNotFoundException || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException || e instanceof FestivalNoPublicadoException || e instanceof StockInsuficienteException
                || e instanceof EntradaAsignadaNotFoundException || e instanceof EntradaAsignadaNoNominadaException || e instanceof PulseraYaAsociadaException
                || e instanceof IllegalArgumentException || e instanceof SecurityException || e instanceof IllegalStateException
                || e instanceof PersistenceException || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }

    /**
     * Mapea entidad EntradaAsignada a DTO, incluyendo imagen QR.
     */
    private EntradaAsignadaDTO mapEntityToDto(EntradaAsignada ea) {
        if (ea == null) {
            return null;
        }
        EntradaAsignadaDTO dto = new EntradaAsignadaDTO();
        dto.setIdEntradaAsignada(ea.getIdEntradaAsignada());
        dto.setCodigoQr(ea.getCodigoQr());
        dto.setEstado(ea.getEstado());
        dto.setFechaAsignacion(ea.getFechaAsignacion());
        dto.setFechaUso(ea.getFechaUso());

        if (ea.getCompraEntrada() != null) {
            dto.setIdCompraEntrada(ea.getCompraEntrada().getIdCompraEntrada());
            if (ea.getCompraEntrada().getEntrada() != null) {
                Entrada entradaOriginal = ea.getCompraEntrada().getEntrada();
                dto.setIdEntradaOriginal(entradaOriginal.getIdEntrada());
                dto.setTipoEntradaOriginal(entradaOriginal.getTipo());
                if (entradaOriginal.getFestival() != null) {
                    dto.setIdFestival(entradaOriginal.getFestival().getIdFestival());
                    dto.setNombreFestival(entradaOriginal.getFestival().getNombre());
                }
            }
        }
        if (ea.getAsistente() != null) {
            dto.setIdAsistente(ea.getAsistente().getIdAsistente());
            dto.setNombreAsistente(ea.getAsistente().getNombre());
            dto.setEmailAsistente(ea.getAsistente().getEmail());
        }
        if (ea.getPulseraAsociada() != null) {
            dto.setIdPulseraAsociada(ea.getPulseraAsociada().getIdPulsera());
            dto.setCodigoUidPulsera(ea.getPulseraAsociada().getCodigoUid());
        }

        if (ea.getCodigoQr() != null && !ea.getCodigoQr().isBlank()) {
            int qrWidth = 100;
            int qrHeight = 100;
            String imageDataUrl = QRCodeUtil.generarQrComoBase64(ea.getCodigoQr(), qrWidth, qrHeight);
            if (imageDataUrl != null) {
                dto.setQrCodeImageDataUrl(imageDataUrl);
            } else {
                log.warn("No se pudo generar la imagen QR para la entrada asignada ID {}", ea.getIdEntradaAsignada());
            }
        }
        return dto;
    }
}
