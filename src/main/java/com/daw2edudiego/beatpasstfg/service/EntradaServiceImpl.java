package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.Entrada;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival; // Importar EstadoFestival
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.repository.EntradaRepository;
import com.daw2edudiego.beatpasstfg.repository.EntradaRepositoryImpl;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepository;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepositoryImpl;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepository;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
// import jakarta.persistence.LockModeType; // Descomentar si se usa bloqueo pesimista
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.Collections; // Importar Collections
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de la interfaz {@link EntradaService}. Gestiona la lógica de
 * negocio para los tipos de entrada, coordinando repositorios y transacciones.
 * Añadido método para obtener entradas públicas.
 *
 * @author Eduardo Olalde
 */
public class EntradaServiceImpl implements EntradaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaServiceImpl.class);

    // Inyección manual de dependencias
    private final EntradaRepository entradaRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor que inicializa los repositorios necesarios.
     */
    public EntradaServiceImpl() {
        this.entradaRepository = new EntradaRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntradaDTO crearEntrada(EntradaDTO entradaDTO, Integer idFestival, Integer idPromotor) {
        log.info("Service: Creando nuevo tipo de entrada para festival ID {} por promotor ID {}", idFestival, idPromotor);
        // Validaciones iniciales
        if (entradaDTO == null || idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("DTO de entrada, ID de festival e ID de promotor son requeridos.");
        }
        if (!idFestival.equals(entradaDTO.getIdFestival())) {
            throw new IllegalArgumentException("El ID del festival en el DTO (" + entradaDTO.getIdFestival() + ") no coincide con el ID del festival en la URL (" + idFestival + ").");
        }
        // Validar campos obligatorios del DTO (aunque Bean Validation podría hacerlo)
        if (entradaDTO.getTipo() == null || entradaDTO.getTipo().isBlank()
                || entradaDTO.getPrecio() == null || entradaDTO.getPrecio().compareTo(BigDecimal.ZERO) < 0
                || entradaDTO.getStock() == null || entradaDTO.getStock() < 0) {
            throw new IllegalArgumentException("Datos inválidos en el DTO: tipo, precio (>=0) y stock (>=0) son obligatorios.");
        }

        EntityManager em = null;
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

            // 3. Crear la entidad Entrada desde el DTO
            Entrada nuevaEntrada = new Entrada();
            nuevaEntrada.setFestival(festival); // Asociar al festival encontrado
            nuevaEntrada.setTipo(entradaDTO.getTipo().trim());
            nuevaEntrada.setDescripcion(entradaDTO.getDescripcion() != null ? entradaDTO.getDescripcion().trim() : null);
            nuevaEntrada.setPrecio(entradaDTO.getPrecio());
            nuevaEntrada.setStock(entradaDTO.getStock());
            // fechaCreacion y fechaModificacion se manejan automáticamente

            // 4. Persistir la nueva entrada
            Entrada entradaGuardada = entradaRepository.save(em, nuevaEntrada);

            tx.commit();
            log.info("Nuevo tipo de entrada ID {} creado exitosamente para festival ID {}", entradaGuardada.getIdEntrada(), idFestival);
            return mapEntityToDto(entradaGuardada);

        } catch (Exception e) {
            handleException(e, tx, "crear entrada para festival " + idFestival);
            throw mapException(e); // Relanzar excepción mapeada
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo entradas para festival ID {} por promotor ID {}", idFestival, idPromotor);
        // Validación de argumentos
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival e ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null; // Usar transacción para verificar propiedad
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

            // 3. Obtener las entradas del repositorio
            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);

            tx.commit(); // Confirmar transacción de lectura

            // 4. Mapear a DTOs y devolver
            log.info("Encontrados {} tipos de entrada para el festival ID {} (Promotor {})", entradas.size(), idFestival, idPromotor);
            return entradas.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            handleException(e, tx, "obtener entradas por festival " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- IMPLEMENTACIÓN NUEVO MÉTODO PÚBLICO ---
    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntradaDTO> obtenerEntradasPublicasPorFestival(Integer idFestival) {
        log.debug("Service: Obteniendo entradas públicas para festival ID {}", idFestival);
        if (idFestival == null) {
            throw new IllegalArgumentException("ID de festival es requerido.");
        }

        EntityManager em = null;
        // No se necesita transacción explícita para lectura simple,
        // pero la usamos para mantener consistencia en el manejo del EM.
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Buscar el festival
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            // 2. Verificar que esté PUBLICADO
            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                log.warn("Intento de acceso a entradas de festival ID {} no publicado (Estado: {})", idFestival, festival.getEstado());
                // Lanzar excepción específica para este caso
                throw new FestivalNoPublicadoException("El festival ID " + idFestival + " no está actualmente publicado.");
            }

            // 3. Obtener las entradas del repositorio
            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);

            tx.commit(); // Confirmar transacción de lectura

            // 4. Mapear a DTOs y devolver
            log.info("Encontrados {} tipos de entrada para el festival público ID {}", entradas.size(), idFestival);
            return entradas.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());

        } catch (FestivalNotFoundException | FestivalNoPublicadoException e) {
            // Relanzar excepciones esperadas después de rollback (si hubo tx)
            rollbackTransaction(tx, "obtener entradas públicas festival " + idFestival);
            throw e;
        } catch (Exception e) {
            // Manejar otros errores
            handleException(e, tx, "obtener entradas públicas festival " + idFestival);
            // Devolver lista vacía o relanzar según política de errores
            // Aquí optamos por devolver lista vacía en caso de error inesperado en lectura pública
            log.error("Error inesperado obteniendo entradas públicas para festival ID {}: {}", idFestival, e.getMessage());
            return Collections.emptyList(); // O relanzar: throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }
    // --- FIN IMPLEMENTACIÓN NUEVO MÉTODO ---

    /**
     * {@inheritDoc}
     */
    @Override
    public EntradaDTO actualizarEntrada(Integer idEntrada, EntradaDTO entradaDTO, Integer idPromotor) {
        log.info("Service: Actualizando tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        // Validaciones
        if (idEntrada == null || entradaDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada, DTO de entrada e ID de promotor son requeridos.");
        }
        if (!idEntrada.equals(entradaDTO.getIdEntrada())) {
            // Permitir que idEntrada en DTO sea null o coincida
            if (entradaDTO.getIdEntrada() != null) {
                throw new IllegalArgumentException("El ID de entrada en el DTO (" + entradaDTO.getIdEntrada() + ") debe coincidir con el ID en la URL (" + idEntrada + ") o ser nulo.");
            }
        }
        // Validar campos obligatorios del DTO
        if (entradaDTO.getTipo() == null || entradaDTO.getTipo().isBlank()
                || entradaDTO.getPrecio() == null || entradaDTO.getPrecio().compareTo(BigDecimal.ZERO) < 0
                || entradaDTO.getStock() == null || entradaDTO.getStock() < 0) {
            throw new IllegalArgumentException("Datos inválidos en el DTO: tipo, precio (>=0) y stock (>=0) son obligatorios.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // 2. Buscar la entrada a actualizar
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // 3. Verificar propiedad del festival asociado
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            // 4. Actualizar los campos de la entidad con los datos del DTO
            // No se permite cambiar el festival asociado (entradaDTO.getIdFestival() se ignora)
            entrada.setTipo(entradaDTO.getTipo().trim());
            entrada.setDescripcion(entradaDTO.getDescripcion() != null ? entradaDTO.getDescripcion().trim() : null);
            entrada.setPrecio(entradaDTO.getPrecio());
            entrada.setStock(entradaDTO.getStock());
            // fechaModificacion se actualiza automáticamente por la BD

            // 5. Guardar (merge) la entidad actualizada
            Entrada entradaActualizada = entradaRepository.save(em, entrada);

            tx.commit();
            log.info("Tipo de entrada ID {} actualizado exitosamente.", idEntrada);
            return mapEntityToDto(entradaActualizada);

        } catch (Exception e) {
            handleException(e, tx, "actualizar entrada " + idEntrada);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eliminarEntrada(Integer idEntrada, Integer idPromotor) {
        log.info("Service: Eliminando tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        // Validaciones
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada e ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // 2. Buscar la entrada a eliminar
            // Necesitamos la entidad completa para verificar la propiedad del festival
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // 3. Verificar propiedad del festival asociado
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            // 4. Intentar eliminar usando el repositorio
            // El repositorio buscará de nuevo y llamará a em.remove()
            // Esto lanzará PersistenceException si hay FKs (CompraEntrada)
            boolean eliminado = entradaRepository.deleteById(em, idEntrada);

            if (!eliminado) {
                // Esto no debería ocurrir si la encontramos en el paso 2, pero por si acaso
                log.warn("La entrada ID {} fue encontrada pero no pudo ser eliminada (estado inesperado).", idEntrada);
                throw new EntradaNotFoundException("No se pudo eliminar la entrada ID " + idEntrada + " (posiblemente ya eliminada).");
            }

            tx.commit();
            log.info("Tipo de entrada ID {} eliminado exitosamente.", idEntrada);

        } catch (PersistenceException e) {
            handleException(e, tx, "eliminar entrada " + idEntrada);
            // Podríamos lanzar una excepción más específica si es por FK
            log.error("Error de persistencia eliminando entrada ID {}. Probablemente existen compras asociadas.", idEntrada);
            throw new RuntimeException("No se puede eliminar el tipo de entrada ID " + idEntrada + " porque tiene ventas asociadas.", e);
        } catch (Exception e) {
            handleException(e, tx, "eliminar entrada " + idEntrada);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor) {
        log.debug("Service: Obteniendo entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        // Validaciones
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada e ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null; // Usar transacción para verificar propiedad
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // 2. Buscar la entrada
            Optional<Entrada> entradaOpt = entradaRepository.findById(em, idEntrada);

            if (entradaOpt.isEmpty()) {
                tx.commit(); // O rollback
                log.warn("Entrada no encontrada con ID {}", idEntrada);
                return Optional.empty(); // Devolver vacío si no existe
            }

            Entrada entrada = entradaOpt.get();

            // 3. Verificar propiedad del festival
            verificarPropiedadFestival(entrada.getFestival(), idPromotor); // Lanza SecurityException si no es dueño

            tx.commit(); // Confirmar lectura

            // 4. Mapear a DTO y devolver
            return Optional.of(mapEntityToDto(entrada));

        } catch (UsuarioNotFoundException | EntradaNotFoundException e) {
            // Si no se encuentra el promotor o la entrada, devolver vacío
            handleException(e, tx, "obtener entrada por ID " + idEntrada);
            return Optional.empty();
        } catch (SecurityException e) {
            // Si no tiene permisos, devolver vacío
            handleException(e, tx, "obtener entrada por ID " + idEntrada + " (permiso denegado)");
            return Optional.empty();
        } catch (Exception e) {
            handleException(e, tx, "obtener entrada por ID " + idEntrada);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos de Ayuda (Helpers) Internos ---
    /**
     * Mapea una entidad Entrada a su correspondiente EntradaDTO.
     *
     * @param entrada La entidad Entrada.
     * @return El EntradaDTO mapeado, o null si la entidad es null.
     */
    private EntradaDTO mapEntityToDto(Entrada entrada) {
        if (entrada == null) {
            return null;
        }
        EntradaDTO dto = new EntradaDTO();
        dto.setIdEntrada(entrada.getIdEntrada());
        // Asegurarse de obtener el ID del festival desde la entidad asociada
        if (entrada.getFestival() != null) {
            dto.setIdFestival(entrada.getFestival().getIdFestival());
        }
        dto.setTipo(entrada.getTipo());
        dto.setDescripcion(entrada.getDescripcion());
        dto.setPrecio(entrada.getPrecio());
        dto.setStock(entrada.getStock());
        // No incluir fechas de creación/modificación en este DTO por ahora
        return dto;
    }

    /**
     * Verifica que el promotor dado sea el propietario del festival. Lanza
     * SecurityException si no lo es. (Reutilizado de otros servicios).
     *
     * @param festival El festival a verificar. No debe ser {@code null}.
     * @param idPromotor El ID del promotor que se espera sea el propietario. No
     * debe ser {@code null}.
     * @throws SecurityException si el promotor no es el propietario.
     * @throws IllegalArgumentException si festival o idPromotor son nulos, o si
     * el festival no tiene promotor asociado.
     * @throws FestivalNotFoundException si el festival es nulo (aunque se
     * verifica antes).
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
            // Esta verificación es redundante si se llama después de buscar el festival,
            // pero es una salvaguarda.
            throw new FestivalNotFoundException("El festival asociado no puede ser nulo.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor no puede ser nulo para verificar propiedad.");
        }
        if (festival.getPromotor() == null || festival.getPromotor().getIdUsuario() == null) {
            log.error("Inconsistencia de datos: Festival ID {} no tiene un promotor asociado.", festival.getIdFestival());
            throw new IllegalStateException("El festival ID " + festival.getIdFestival() + " no tiene un promotor asociado.");
        }
        if (!festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            log.warn("Intento de acceso no autorizado por promotor ID {} al festival ID {} (propiedad de promotor ID {})",
                    idPromotor, festival.getIdFestival(), festival.getPromotor().getIdUsuario());
            throw new SecurityException("El usuario no tiene permiso para acceder a los recursos de este festival.");
        }
        log.trace("Verificación de propiedad exitosa para promotor ID {} sobre festival ID {}", idPromotor, festival.getIdFestival());
    }

    /**
     * Manejador genérico de excepciones para métodos de servicio. Loggea el
     * error y realiza rollback si hay transacción activa. (Reutilizado de otros
     * servicios).
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    /**
     * Realiza rollback de una transacción si está activa y loggea el error.
     * (Reutilizado de otros servicios).
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
        if (e instanceof AsistenteNotFoundException
                || e instanceof EntradaNotFoundException // Añadida
                || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException
                || e instanceof FestivalNoPublicadoException // Añadida
                || e instanceof StockInsuficienteException
                || e instanceof EntradaAsignadaNotFoundException
                || e instanceof IllegalArgumentException
                || e instanceof SecurityException
                || e instanceof IllegalStateException
                || e instanceof PersistenceException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }
}
