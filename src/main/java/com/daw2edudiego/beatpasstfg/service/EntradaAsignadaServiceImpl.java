package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType; // Importar para bloqueo
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Importar Optional
import java.util.stream.Collectors;

/**
 * Implementación de EntradaAsignadaService. ACTUALIZADO: Añadido método
 * cancelarEntrada y obtenerEntradaAsignadaPorId.
 */
public class EntradaAsignadaServiceImpl implements EntradaAsignadaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaAsignadaServiceImpl.class);

    // Inyección de dependencias
    private final EntradaAsignadaRepository entradaAsignadaRepository;
    // private final AsistenteRepository asistenteRepository; // Ya no se usa directamente aquí
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository;
    private final EntradaRepository entradaRepository;
    private final AsistenteService asistenteService; // <-- Añadido AsistenteService

    public EntradaAsignadaServiceImpl() {
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        // this.asistenteRepository = new AsistenteRepositoryImpl(); // Ya no necesario
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.entradaRepository = new EntradaRepositoryImpl();
        this.asistenteService = new AsistenteServiceImpl(); // <-- Instanciar
    }

    @Override
    public void nominarEntrada(Integer idEntradaAsignada, String emailAsistente, String nombreAsistente, String telefonoAsistente, Integer idPromotor) {
        log.info("Service: Nominando entrada ID {} a asistente email {} por promotor ID {}",
                idEntradaAsignada, emailAsistente, idPromotor);

        if (idEntradaAsignada == null || emailAsistente == null || emailAsistente.isBlank() || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada asignada, email de asistente y ID de promotor son requeridos.");
        }
        // El nombre solo es obligatorio si se va a crear el asistente (lo valida el AsistenteService)

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Buscar promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // 2. Buscar Entrada Asignada
            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));

            // 3. Verificar propiedad del festival
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            verificarPropiedadFestival(festival, idPromotor);

            // 4. Verificar estado de la entrada
            if (entradaAsignada.getAsistente() != null) {
                throw new IllegalStateException("La entrada ya está nominada al asistente: " + entradaAsignada.getAsistente().getEmail());
            }
            if (entradaAsignada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden nominar entradas que estén en estado ACTIVA.");
            }

            // 5. Obtener o Crear Asistente por Email (usa su propia transacción interna si crea)
            // Pasamos el EntityManager actual por si acaso, aunque el servicio podría obtener el suyo.
            // Nota: obtenerOcrear maneja la transacción para la creación si es necesario.
            // Si solo busca, no necesita tx activa aquí. Si crea, usa su propia tx.
            // Para asegurar consistencia, podríamos envolver todo en una única tx grande,
            // pero la implementación actual de obtenerOcrear ya lo hace robusto.
            Asistente asistente = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistente, nombreAsistente, telefonoAsistente);
            // Si llegamos aquí, el asistente existe o se ha creado correctamente.

            // 6. Realizar la asignación
            entradaAsignada.setAsistente(asistente);
            entradaAsignada.setFechaAsignacion(LocalDateTime.now());

            // 7. Guardar la entrada actualizada
            entradaAsignadaRepository.save(em, entradaAsignada);

            tx.commit();
            log.info("Entrada ID {} nominada exitosamente al asistente ID {} (Email: {})",
                    idEntradaAsignada, asistente.getIdAsistente(), asistente.getEmail());

        } catch (Exception e) {
            handleException(e, tx, "nominando entrada");
            throw mapException(e); // Relanzar excepción mapeada
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<EntradaAsignadaDTO> obtenerEntradasAsignadasPorFestival(Integer idFestival, Integer idPromotor) {
        // ... (código sin cambios relevantes, solo refactorizado con helpers) ...
        log.debug("Service: Obteniendo entradas asignadas para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival y ID de promotor son requeridos.");
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
            return entradas.stream().map(this::mapEntityToDto).collect(Collectors.toList());
        } catch (Exception e) {
            handleException(e, tx, "obteniendo entradas asignadas");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public void cancelarEntrada(Integer idEntradaAsignada, Integer idPromotor) {
        log.info("Service: Cancelando entrada ID {} por promotor ID {}", idEntradaAsignada, idPromotor);
        if (idEntradaAsignada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada asignada y ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Buscar promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // 2. Buscar Entrada Asignada
            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));

            // 3. Verificar propiedad del festival asociado
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            verificarPropiedadFestival(festival, idPromotor);

            // 4. Verificar estado de la entrada (solo se cancelan ACTIVAS)
            if (entradaAsignada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                log.warn("Intento de cancelar entrada ID {} que no está ACTIVA (estado: {})",
                        idEntradaAsignada, entradaAsignada.getEstado());
                throw new IllegalStateException("Solo se pueden cancelar entradas que estén en estado ACTIVA.");
            }

            // 5. Cambiar estado a CANCELADA
            entradaAsignada.setEstado(EstadoEntradaAsignada.CANCELADA);
            // Opcional: limpiar asistente y fecha de asignación? Depende del negocio.
            // entradaAsignada.setAsistente(null);
            // entradaAsignada.setFechaAsignacion(null);
            entradaAsignadaRepository.save(em, entradaAsignada); // Guardar cambio de estado

            // 6. Incrementar Stock de la Entrada Original (con bloqueo)
            Entrada entradaOriginal = entradaAsignada.getCompraEntrada().getEntrada();
            if (entradaOriginal != null) {
                // Bloquear la entrada original para evitar concurrencia
                em.lock(entradaOriginal, LockModeType.PESSIMISTIC_WRITE);
                int stockActual = entradaOriginal.getStock();
                entradaOriginal.setStock(stockActual + 1); // Incrementar stock
                entradaRepository.save(em, entradaOriginal); // Guardar stock actualizado
                log.info("Stock incrementado para Entrada ID {} (original de la cancelada {}). Nuevo stock: {}",
                        entradaOriginal.getIdEntrada(), idEntradaAsignada, entradaOriginal.getStock());
            } else {
                // Esto sería un error de integridad grave
                log.error("No se pudo encontrar la Entrada original asociada a CompraEntrada ID {} para actualizar stock.",
                        entradaAsignada.getCompraEntrada().getIdCompraEntrada());
                // Podríamos lanzar excepción o solo loguear dependiendo de la criticidad
            }

            // 7. Commit
            tx.commit();
            log.info("Entrada ID {} cancelada exitosamente por promotor ID {}", idEntradaAsignada, idPromotor);

        } catch (Exception e) {
            handleException(e, tx, "cancelando entrada");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<EntradaAsignadaDTO> obtenerEntradaAsignadaPorId(Integer idEntradaAsignada, Integer idPromotor) {
        log.debug("Service: Obteniendo entrada asignada ID {} por promotor ID {}", idEntradaAsignada, idPromotor);
        if (idEntradaAsignada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada asignada y ID de promotor son requeridos.");
        }
        EntityManager em = null;
        EntityTransaction tx = null; // Opcional para lectura
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));

            // Verificar propiedad
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            verificarPropiedadFestival(festival, idPromotor);

            tx.commit();
            return Optional.of(mapEntityToDto(entradaAsignada));

        } catch (Exception e) {
            handleException(e, tx, "obteniendo entrada asignada por ID");
            // Si no se encuentra o no tiene permiso, devolvemos Optional vacío o relanzamos
            if (e instanceof EntradaAsignadaNotFoundException || e instanceof SecurityException) {
                log.warn("No se pudo obtener entrada asignada ID {} para promotor ID {}: {}", idEntradaAsignada, idPromotor, e.getMessage());
                return Optional.empty(); // O throw e;
            }
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Helpers Internos ---
    /**
     * Obtiene el festival asociado a una EntradaAsignada, manejando nulos
     * intermedios.
     */
    private Festival obtenerFestivalDesdeEntradaAsignada(EntradaAsignada ea) {
        if (ea == null || ea.getCompraEntrada() == null || ea.getCompraEntrada().getEntrada() == null || ea.getCompraEntrada().getEntrada().getFestival() == null) {
            log.error("Inconsistencia de datos para EntradaAsignada ID {}: no se pudo obtener el festival asociado.", ea != null ? ea.getIdEntradaAsignada() : "null");
            throw new IllegalStateException("Error interno: no se pudo determinar el festival de la entrada.");
        }
        return ea.getCompraEntrada().getEntrada().getFestival();
    }

    /**
     * Verifica si un promotor es dueño de un festival. Lanza SecurityException
     * si no.
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            log.warn("Intento no autorizado por promotor ID {} sobre festival ID {}", idPromotor, festival.getIdFestival());
            throw new SecurityException("No tiene permiso para realizar acciones sobre este festival.");
        }
    }

    /**
     * Maneja excepciones comunes en métodos transaccionales.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error {} : {}", action, e.getMessage(), e);
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
     * Cierra el EntityManager si está abierto.
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    /**
     * Mapea excepciones conocidas o devuelve una RuntimeException genérica.
     */
    private RuntimeException mapException(Exception e) {
        if (e instanceof AsistenteNotFoundException || e instanceof EntradaNotFoundException
                || e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException
                || e instanceof FestivalNoPublicadoException || e instanceof StockInsuficienteException
                || e instanceof IllegalArgumentException || e instanceof SecurityException
                || e instanceof IllegalStateException || e instanceof PersistenceException
                || e instanceof EntradaAsignadaNotFoundException) { // Añadir nueva excepción
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en servicio: " + e.getMessage(), e);
    }

    /**
     * Mapea Entidad EntradaAsignada a EntradaAsignadaDTO
     */
    private EntradaAsignadaDTO mapEntityToDto(EntradaAsignada ea) {
        // ... (código de mapeo sin cambios) ...
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
            Asistente asistente = ea.getAsistente();
            dto.setIdAsistente(asistente.getIdAsistente());
            dto.setNombreAsistente(asistente.getNombre());
            dto.setEmailAsistente(asistente.getEmail());
        }
        return dto;
    }
}
