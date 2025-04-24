package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.Entrada;
import com.daw2edudiego.beatpasstfg.model.Festival;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de la interfaz {@link EntradaService}. Gestiona la lógica de
 * negocio para los tipos de entrada, coordinando las operaciones del
 * repositorio y manejando las transacciones JPA.
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
        log.info("Service: Creando tipo de entrada '{}' para festival ID {} por promotor ID {}",
                entradaDTO != null ? entradaDTO.getTipo() : "null", idFestival, idPromotor);

        // Validación de argumentos y DTO
        if (entradaDTO == null) {
            throw new IllegalArgumentException("El DTO de entrada no puede ser nulo.");
        }
        if (idFestival == null) {
            throw new IllegalArgumentException("El ID del festival es requerido.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor es requerido.");
        }
        // Asegurar consistencia del ID de festival
        if (entradaDTO.getIdFestival() != null && !idFestival.equals(entradaDTO.getIdFestival())) {
            log.warn("Inconsistencia: ID de festival en DTO ({}) no coincide con ID de ruta ({})", entradaDTO.getIdFestival(), idFestival);
            // Podríamos lanzar excepción o simplemente usar el idFestival de la ruta
            // throw new IllegalArgumentException("El ID del festival en el DTO no coincide con el de la ruta.");
        } else if (entradaDTO.getIdFestival() == null) {
            entradaDTO.setIdFestival(idFestival); // Asegurar que el DTO tenga el ID correcto
        }
        validarDatosEntradaDTO(entradaDTO); // Validar contenido del DTO

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin(); // Iniciar transacción

            // 1. Verificar existencia del promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // 2. Verificar existencia del festival y propiedad
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor); // Lanza SecurityException si no es dueño

            // 3. Mapear DTO a entidad y asociar festival
            Entrada nuevaEntrada = mapDtoToEntity(entradaDTO);
            nuevaEntrada.setFestival(festival); // Asociar la entidad Festival gestionada

            // 4. Guardar la nueva entrada
            nuevaEntrada = entradaRepository.save(em, nuevaEntrada);

            tx.commit(); // Confirmar transacción
            log.info("Tipo de entrada '{}' creada con ID {} para festival ID {}", nuevaEntrada.getTipo(), nuevaEntrada.getIdEntrada(), idFestival);
            return mapEntityToDto(nuevaEntrada); // Devolver DTO de la entidad creada

        } catch (Exception e) {
            handleException(e, tx, "crear tipo de entrada");
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
        log.debug("Service: Obteniendo tipos de entrada para festival ID {} por promotor ID {}", idFestival, idPromotor);
        // Validación de argumentos
        if (idFestival == null) {
            throw new IllegalArgumentException("ID de festival es requerido.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("ID de promotor es requerido.");
        }

        EntityManager em = null;
        EntityTransaction tx = null; // Opcional para lectura, usado para verificación de propiedad
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Verificar promotor (opcional aquí, verificarPropiedadFestival lo hace)
            // Usuario promotor = usuarioRepository.findById(em, idPromotor)
            //        .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado: " + idPromotor));
            // 2. Verificar festival y propiedad
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor);

            // 3. Obtener entradas del repositorio
            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);

            tx.commit(); // Commit lectura

            // 4. Mapear y devolver
            log.info("Encontrados {} tipos de entrada para el festival ID {}", entradas.size(), idFestival);
            return entradas.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            handleException(e, tx, "obtener tipos de entrada por festival ID " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntradaDTO actualizarEntrada(Integer idEntrada, EntradaDTO entradaDTO, Integer idPromotor) {
        log.info("Service: Actualizando tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        // Validación de argumentos y DTO
        if (idEntrada == null) {
            throw new IllegalArgumentException("ID de entrada es requerido.");
        }
        if (entradaDTO == null) {
            throw new IllegalArgumentException("DTO de entrada es requerido.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("ID de promotor es requerido.");
        }
        validarDatosEntradaDTO(entradaDTO); // Validar contenido del DTO

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin(); // Iniciar transacción

            // 1. Verificar promotor (opcional aquí)
            // Usuario promotor = usuarioRepository.findById(em, idPromotor)
            //        .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado: " + idPromotor));
            // 2. Buscar la entrada a actualizar
            // Considerar bloqueo pesimista si hay alta concurrencia en la actualización de stock
            // Entrada entrada = em.find(Entrada.class, idEntrada, LockModeType.PESSIMISTIC_WRITE);
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // 3. Verificar propiedad del festival asociado
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            // 4. Actualizar los campos de la entidad desde el DTO
            entrada.setTipo(entradaDTO.getTipo().trim());
            entrada.setDescripcion(entradaDTO.getDescripcion() != null ? entradaDTO.getDescripcion().trim() : null);
            entrada.setPrecio(entradaDTO.getPrecio());
            entrada.setStock(entradaDTO.getStock());
            // No se actualiza el festival asociado

            // 5. Guardar (merge) la entidad actualizada
            entrada = entradaRepository.save(em, entrada);

            tx.commit(); // Confirmar transacción
            log.info("Tipo de entrada ID {} actualizado correctamente.", idEntrada);
            return mapEntityToDto(entrada); // Devolver DTO actualizado

        } catch (Exception e) {
            handleException(e, tx, "actualizar tipo de entrada ID " + idEntrada);
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
        // Validación de argumentos
        if (idEntrada == null) {
            throw new IllegalArgumentException("ID de entrada es requerido.");
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

            // 1. Verificar promotor (opcional aquí)
            // Usuario promotor = usuarioRepository.findById(em, idPromotor)
            //        .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado: " + idPromotor));
            // 2. Buscar la entrada a eliminar
            // Es importante buscarla dentro de la transacción para poder eliminarla
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // 3. Verificar propiedad del festival asociado
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            // 4. Intentar eliminar usando el método del repositorio
            // Este método ya busca y llama a em.remove()
            boolean eliminado = entradaRepository.deleteById(em, idEntrada);

            // Si deleteById no lanzó excepción pero devolvió false (no debería pasar si findById funcionó), loggear
            if (!eliminado) {
                log.warn("deleteById devolvió false después de encontrar la entrada ID {}, posible inconsistencia.", idEntrada);
                // Podríamos lanzar una excepción aquí si se considera un estado inválido
                // throw new IllegalStateException("Error inesperado al eliminar entrada.");
            }

            tx.commit(); // Confirmar transacción
            log.info("Tipo de entrada ID {} eliminado (o marcado para eliminar) correctamente.", idEntrada);

        } catch (PersistenceException e) {
            // Capturar específicamente errores de persistencia (como violación de FK)
            handleException(e, tx, "eliminar tipo de entrada ID " + idEntrada);
            log.error("Error de persistencia al eliminar entrada ID {}. Probablemente existen ventas asociadas.", idEntrada);
            // Mapear a una excepción más significativa para el usuario/frontend
            throw new IllegalStateException("No se puede eliminar el tipo de entrada ID " + idEntrada + " porque tiene ventas asociadas.", e);
        } catch (Exception e) {
            handleException(e, tx, "eliminar tipo de entrada ID " + idEntrada);
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
        log.debug("Service: Obteniendo tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        // Validación de argumentos
        if (idEntrada == null) {
            throw new IllegalArgumentException("ID de entrada es requerido.");
        }
        if (idPromotor == null) {
            throw new IllegalArgumentException("ID de promotor es requerido.");
        }

        EntityManager em = null;
        EntityTransaction tx = null; // Opcional para lectura, usado para verificar propiedad
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Buscar la entrada
            Optional<Entrada> entradaOpt = entradaRepository.findById(em, idEntrada);

            if (entradaOpt.isEmpty()) {
                tx.commit(); // O rollback
                log.warn("Tipo de entrada no encontrado con ID: {}", idEntrada);
                return Optional.empty(); // No encontrada
            }

            Entrada entrada = entradaOpt.get();

            // 2. Verificar propiedad del festival asociado
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            tx.commit(); // Confirmar transacción de lectura

            // 3. Mapear a DTO y devolver
            return Optional.of(mapEntityToDto(entrada));

        } catch (Exception e) {
            handleException(e, tx, "obtener tipo de entrada por ID " + idEntrada);
            // Si la excepción fue por no encontrarla o por seguridad, devolvemos vacío
            if (e instanceof EntradaNotFoundException || e instanceof SecurityException) {
                log.warn("No se pudo obtener tipo entrada ID {} para promotor ID {}: {}", idEntrada, idPromotor, e.getMessage());
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
     * Valida los campos obligatorios y formatos básicos de un EntradaDTO.
     *
     * @param dto El DTO a validar.
     * @throws IllegalArgumentException si algún dato es inválido.
     */
    private void validarDatosEntradaDTO(EntradaDTO dto) {
        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de entrada es obligatorio.");
        }
        if (dto.getTipo().length() > 50) {
            throw new IllegalArgumentException("El tipo de entrada no puede exceder los 50 caracteres.");
        }
        if (dto.getPrecio() == null) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }
        if (dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        // Podríamos añadir validación de dígitos aquí si es necesario
        if (dto.getStock() == null) {
            throw new IllegalArgumentException("El stock es obligatorio.");
        }
        if (dto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        // Validación de descripción (opcional, podría tener límite de longitud)
        // if (dto.getDescripcion() != null && dto.getDescripcion().length() > MAX_DESC_LENGTH) { ... }
    }

    /**
     * Mapea una entidad Entrada a su correspondiente EntradaDTO.
     *
     * @param e La entidad Entrada.
     * @return El EntradaDTO mapeado, o null si la entidad es null.
     */
    private EntradaDTO mapEntityToDto(Entrada e) {
        if (e == null) {
            return null;
        }
        EntradaDTO dto = new EntradaDTO();
        dto.setIdEntrada(e.getIdEntrada());
        dto.setTipo(e.getTipo());
        dto.setDescripcion(e.getDescripcion());
        dto.setPrecio(e.getPrecio());
        dto.setStock(e.getStock());
        if (e.getFestival() != null) {
            dto.setIdFestival(e.getFestival().getIdFestival());
        }
        return dto;
    }

    /**
     * Mapea un EntradaDTO a una nueva entidad Entrada. No establece el ID ni el
     * Festival asociado (deben hacerse por separado).
     *
     * @param dto El EntradaDTO.
     * @return La nueva entidad Entrada, o null si el DTO es null.
     */
    private Entrada mapDtoToEntity(EntradaDTO dto) {
        if (dto == null) {
            return null;
        }
        Entrada e = new Entrada();
        // No mapeamos idEntrada (se genera o ya existe)
        // No mapeamos idFestival (se asocia la entidad Festival)
        e.setTipo(dto.getTipo().trim());
        e.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        e.setPrecio(dto.getPrecio());
        e.setStock(dto.getStock());
        return e;
    }

    /**
     * Verifica que el promotor dado sea el propietario del festival.
     * (Reutilizado de otros servicios - podría ir a una clase de utilidad).
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
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
        if (e instanceof AsistenteNotFoundException
                || e instanceof EntradaNotFoundException
                || // Añadida
                e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException
                || e instanceof FestivalNoPublicadoException
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
