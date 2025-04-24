package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import com.daw2edudiego.beatpasstfg.util.QRCodeUtil; // Importar la utilidad QR
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType; // Para bloqueo pesimista
import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de la interfaz {@link EntradaAsignadaService}. Gestiona la
 * lógica de negocio para las entradas asignadas, incluyendo nominación,
 * cancelación y consulta, asegurando la integridad de los datos y los permisos.
 * <p>
 * Al mapear entidades a DTOs, genera la imagen del código QR como URL de datos
 * Base64 utilizando {@link QRCodeUtil}.
 * </p>
 *
 * @see EntradaAsignadaService
 * @see EntradaAsignadaRepository
 * @see QRCodeUtil
 * @author Eduardo Olalde
 */
public class EntradaAsignadaServiceImpl implements EntradaAsignadaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaAsignadaServiceImpl.class);

    // Inyección manual de dependencias de repositorios y otros servicios
    private final EntradaAsignadaRepository entradaAsignadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FestivalRepository festivalRepository; // Necesario para verificar propiedad
    private final EntradaRepository entradaRepository;   // Necesario para actualizar stock
    private final AsistenteService asistenteService;     // Necesario para obtener/crear asistente

    /**
     * Constructor que inicializa los repositorios y servicios necesarios.
     */
    public EntradaAsignadaServiceImpl() {
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl(); // Instanciar
        this.entradaRepository = new EntradaRepositoryImpl();   // Instanciar
        this.asistenteService = new AsistenteServiceImpl();   // Instanciar
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nominarEntrada(Integer idEntradaAsignada, String emailAsistente, String nombreAsistente, String telefonoAsistente, Integer idPromotor) {
        log.info("Service: Iniciando nominación de entrada ID {} a asistente email {} por promotor ID {}",
                idEntradaAsignada, emailAsistente, idPromotor);

        // Validación inicial de parámetros obligatorios
        if (idEntradaAsignada == null) {
            throw new IllegalArgumentException("ID de entrada asignada es requerido.");
        }
        if (emailAsistente == null || emailAsistente.isBlank()) {
            throw new IllegalArgumentException("Email de asistente es requerido.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("ID de promotor es requerido.");
        }
        // El nombre se validará en asistenteService si es necesario crear

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin(); // Iniciar transacción principal

            // 1. Verificar existencia del promotor (aunque la verificación de propiedad lo haría implícitamente)
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // 2. Buscar la Entrada Asignada a nominar
            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));

            // 3. Obtener el Festival asociado y verificar la propiedad del promotor
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada); // Lanza excepción si hay inconsistencia
            verificarPropiedadFestival(festival, idPromotor); // Lanza SecurityException si no es dueño

            // 4. Verificar el estado de la entrada asignada
            if (entradaAsignada.getAsistente() != null) {
                // Ya está nominada a alguien
                throw new IllegalStateException("La entrada ID " + idEntradaAsignada + " ya está nominada al asistente: " + entradaAsignada.getAsistente().getEmail());
            }
            if (entradaAsignada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                // No está activa (podría estar usada o cancelada)
                throw new IllegalStateException("Solo se pueden nominar entradas que estén en estado ACTIVA. Estado actual: " + entradaAsignada.getEstado());
            }

            // 5. Obtener o crear el Asistente usando AsistenteService
            // Esta llamada puede iniciar su propia transacción interna si necesita crear,
            // pero se ejecuta dentro del contexto de nuestra transacción principal.
            Asistente asistente = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistente, nombreAsistente, telefonoAsistente);

            // 6. Realizar la asignación en la entidad EntradaAsignada
            entradaAsignada.setAsistente(asistente);
            entradaAsignada.setFechaAsignacion(LocalDateTime.now());

            // 7. Guardar los cambios en EntradaAsignada (merge)
            entradaAsignadaRepository.save(em, entradaAsignada);

            tx.commit(); // Confirmar la transacción principal
            log.info("Entrada ID {} nominada exitosamente al asistente ID {} (Email: {}) por promotor ID {}",
                    idEntradaAsignada, asistente.getIdAsistente(), asistente.getEmail(), idPromotor);

        } catch (Exception e) {
            // Manejo de excepciones y rollback
            handleException(e, tx, "nominar entrada ID " + idEntradaAsignada);
            // Relanzar excepción mapeada para que la capa superior la maneje
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntradaAsignadaDTO> obtenerEntradasAsignadasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo entradas asignadas para festival ID {} por promotor ID {}", idFestival, idPromotor);
        // Validación de argumentos
        if (idFestival == null) {
            throw new IllegalArgumentException("ID de festival es requerido.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("ID de promotor es requerido.");
        }

        EntityManager em = null;
        // No se necesita transacción para lectura, pero la mantenemos por consistencia
        // y por si el mapeo a DTO accede a relaciones LAZY.
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar existencia del promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // 2. Verificar existencia del festival y propiedad
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor); // Lanza SecurityException si no es dueño

            // 3. Obtener las entradas asignadas del repositorio
            List<EntradaAsignada> entradas = entradaAsignadaRepository.findByFestivalId(em, idFestival);

            tx.commit(); // Commit (aunque sea lectura)

            // 4. Mapear a DTOs y devolver
            log.info("Encontradas {} entradas asignadas para el festival ID {}", entradas.size(), idFestival);
            return entradas.stream()
                    .map(this::mapEntityToDto) // Usar el helper de mapeo
                    .collect(Collectors.toList());
        } catch (Exception e) {
            handleException(e, tx, "obtener entradas asignadas por festival ID " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelarEntrada(Integer idEntradaAsignada, Integer idPromotor) {
        log.info("Service: Iniciando cancelación de entrada ID {} por promotor ID {}", idEntradaAsignada, idPromotor);
        // Validación de argumentos
        if (idEntradaAsignada == null) {
            throw new IllegalArgumentException("ID de entrada asignada es requerido.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("ID de promotor es requerido.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin(); // Iniciar transacción

            // 1. Verificar existencia del promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // 2. Buscar la Entrada Asignada a cancelar
            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findById(em, idEntradaAsignada)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("Entrada asignada no encontrada con ID: " + idEntradaAsignada));

            // 3. Obtener el Festival asociado y verificar la propiedad del promotor
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            verificarPropiedadFestival(festival, idPromotor);

            // 4. Verificar que la entrada esté en estado ACTIVA
            if (entradaAsignada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden cancelar entradas que estén en estado ACTIVA. Estado actual: " + entradaAsignada.getEstado());
            }

            // 5. Cambiar el estado de la EntradaAsignada a CANCELADA
            entradaAsignada.setEstado(EstadoEntradaAsignada.CANCELADA);
            // Opcionalmente, desvincular asistente si estaba nominada
            // entradaAsignada.setAsistente(null);
            // entradaAsignada.setFechaAsignacion(null);
            entradaAsignadaRepository.save(em, entradaAsignada); // Guardar el cambio de estado

            // 6. Incrementar el stock de la Entrada original asociada
            Entrada entradaOriginal = obtenerEntradaOriginal(entradaAsignada);
            // Bloquear la fila de Entrada para evitar problemas de concurrencia al actualizar stock
            em.lock(entradaOriginal, LockModeType.PESSIMISTIC_WRITE);
            int stockActual = entradaOriginal.getStock() != null ? entradaOriginal.getStock() : 0;
            entradaOriginal.setStock(stockActual + 1);
            entradaRepository.save(em, entradaOriginal); // Guardar el stock actualizado
            log.info("Stock incrementado para Entrada ID {} (original de cancelada {}). Nuevo stock: {}",
                    entradaOriginal.getIdEntrada(), idEntradaAsignada, entradaOriginal.getStock());

            tx.commit(); // Confirmar transacción
            log.info("Entrada ID {} cancelada exitosamente por promotor ID {}", idEntradaAsignada, idPromotor);

        } catch (Exception e) {
            handleException(e, tx, "cancelar entrada ID " + idEntradaAsignada);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<EntradaAsignadaDTO> obtenerEntradaAsignadaPorId(Integer idEntradaAsignada, Integer idPromotor) {
        log.debug("Service: Obteniendo entrada asignada ID {} por promotor ID {}", idEntradaAsignada, idPromotor);
        // Validación de argumentos
        if (idEntradaAsignada == null) {
            throw new IllegalArgumentException("ID de entrada asignada es requerido.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("ID de promotor es requerido.");
        }

        EntityManager em = null;
        EntityTransaction tx = null; // Opcional para lectura, pero usado para verificar propiedad
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Buscar la entrada asignada
            Optional<EntradaAsignada> entradaOpt = entradaAsignadaRepository.findById(em, idEntradaAsignada);

            if (entradaOpt.isEmpty()) {
                tx.commit(); // O rollback, no importa mucho en lectura sin cambios
                log.warn("Entrada asignada no encontrada con ID: {}", idEntradaAsignada);
                return Optional.empty(); // No encontrada
            }

            EntradaAsignada entradaAsignada = entradaOpt.get();

            // 2. Verificar propiedad del festival asociado
            Festival festival = obtenerFestivalDesdeEntradaAsignada(entradaAsignada);
            // Esta llamada lanzará SecurityException si no tiene permiso
            verificarPropiedadFestival(festival, idPromotor);

            tx.commit(); // Confirmar transacción de lectura

            // 3. Mapear a DTO y devolver
            return Optional.of(mapEntityToDto(entradaAsignada));

        } catch (Exception e) {
            handleException(e, tx, "obtener entrada asignada por ID " + idEntradaAsignada);
            // Si la excepción fue por no encontrarla o por seguridad, devolvemos vacío
            if (e instanceof EntradaAsignadaNotFoundException || e instanceof SecurityException) {
                log.warn("No se pudo obtener entrada asignada ID {} para promotor ID {}: {}", idEntradaAsignada, idPromotor, e.getMessage());
                return Optional.empty();
            }
            // Para otros errores, relanzamos
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos de Ayuda (Helpers) Internos ---
    /**
     * Obtiene la entidad Festival asociada a una EntradaAsignada, navegando a
     * través de las relaciones. Lanza IllegalStateException si hay
     * inconsistencias en los datos que impiden encontrar el festival.
     *
     * @param ea La EntradaAsignada. No debe ser {@code null}.
     * @return El Festival asociado. Nunca {@code null}.
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
     * Obtiene la entidad Entrada original asociada a una EntradaAsignada. Lanza
     * IllegalStateException si hay inconsistencias.
     *
     * @param ea La EntradaAsignada. No debe ser {@code null}.
     * @return La Entrada original. Nunca {@code null}.
     * @throws IllegalStateException si no se puede determinar la entrada
     * original.
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
     * Verifica que el promotor dado sea el propietario del festival. Lanza
     * SecurityException si no lo es.
     *
     * @param festival El festival a verificar. No debe ser {@code null}.
     * @param idPromotor El ID del promotor que se espera sea el propietario. No
     * debe ser {@code null}.
     * @throws SecurityException si el promotor no es el propietario.
     * @throws IllegalArgumentException si festival o idPromotor son nulos, o si
     * el festival no tiene promotor asociado.
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        // Reutilizado de AsistenteServiceImpl - Podría ir a una clase de utilidad de seguridad
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
     * Realiza rollback de una transacción si está activa y loggea el error.
     *
     * @param tx La transacción.
     * @param action Descripción de la acción que falló.
     */
    private void rollbackTransaction(EntityTransaction tx, String action) {
        // Reutilizado de AsistenteServiceImpl - Podría ir a una clase de utilidad JPA/Transacción
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
     * Manejador genérico de excepciones para métodos de servicio. Loggea el
     * error y realiza rollback si hay transacción activa.
     *
     * @param e La excepción capturada.
     * @param tx La transacción activa (puede ser null).
     * @param action Descripción de la acción que falló.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        // Reutilizado de AsistenteServiceImpl
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    /**
     * Cierra el EntityManager si está abierto.
     *
     * @param em El EntityManager a cerrar.
     */
    private void closeEntityManager(EntityManager em) {
        // Reutilizado de AsistenteServiceImpl
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    /**
     * Mapea excepciones técnicas a excepciones de negocio o RuntimeException.
     *
     * @param e La excepción original.
     * @return La excepción mapeada o una RuntimeException genérica.
     */
    private RuntimeException mapException(Exception e) {
        // Reutilizado de AsistenteServiceImpl - Podría ir a una clase de utilidad de excepciones
        // Asegurarse de incluir todas las excepciones personalizadas relevantes
        if (e instanceof AsistenteNotFoundException
                || e instanceof EntradaNotFoundException
                || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException
                || e instanceof FestivalNoPublicadoException
                || e instanceof StockInsuficienteException
                || e instanceof EntradaAsignadaNotFoundException // Añadida
                || e instanceof EntradaAsignadaNoNominadaException // Añadida
                || e instanceof PulseraYaAsociadaException // Añadida
                || e instanceof IllegalArgumentException
                || e instanceof SecurityException
                || e instanceof IllegalStateException
                || e instanceof PersistenceException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }

    /**
     * Mapea una entidad EntradaAsignada a su correspondiente
     * EntradaAsignadaDTO. Incluye información del asistente, tipo de entrada
     * original, festival y la URL de datos de la imagen QR.
     *
     * @param ea La entidad EntradaAsignada. No debe ser {@code null}.
     * @return El EntradaAsignadaDTO mapeado.
     */
    private EntradaAsignadaDTO mapEntityToDto(EntradaAsignada ea) {
        if (ea == null) {
            // Devolver null o un DTO vacío según preferencia. Devolver null aquí.
            return null;
        }
        EntradaAsignadaDTO dto = new EntradaAsignadaDTO();
        dto.setIdEntradaAsignada(ea.getIdEntradaAsignada());
        dto.setCodigoQr(ea.getCodigoQr()); // Mantener el contenido textual
        dto.setEstado(ea.getEstado());
        dto.setFechaAsignacion(ea.getFechaAsignacion());
        dto.setFechaUso(ea.getFechaUso());

        // Mapear información relacionada (con cuidado de nulos)
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
        if (ea.getPulseraAsociada() != null) {
            dto.setIdPulseraAsociada(ea.getPulseraAsociada().getIdPulsera());
            dto.setCodigoUidPulsera(ea.getPulseraAsociada().getCodigoUid());
        }

        // *** Generar y establecer la imagen QR en Base64 ***
        if (ea.getCodigoQr() != null && !ea.getCodigoQr().isBlank()) {
            // Definir tamaño deseado para la imagen QR
            int qrWidth = 100; // Ancho en píxeles
            int qrHeight = 100; // Alto en píxeles
            String imageDataUrl = QRCodeUtil.generarQrComoBase64(ea.getCodigoQr(), qrWidth, qrHeight);
            if (imageDataUrl != null) {
                dto.setQrCodeImageDataUrl(imageDataUrl);
            } else {
                // Si falla la generación, el campo quedará null (JsonInclude lo omitirá)
                log.warn("No se pudo generar la imagen QR para la entrada asignada ID {}", ea.getIdEntradaAsignada());
            }
        }

        return dto;
    }
}
