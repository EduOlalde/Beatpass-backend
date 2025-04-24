package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepository;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepositoryImpl;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepository;
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de la interfaz {@link FestivalService}. Gestiona la lógica de
 * negocio para las operaciones relacionadas con Festivales, maneja las
 * transacciones JPA y utiliza los repositorios para el acceso a datos. Incluye
 * validaciones de negocio y comprobaciones de permisos.
 *
 * @see FestivalService
 * @see FestivalRepository
 * @see UsuarioRepository
 * @author Eduardo Olalde
 */
public class FestivalServiceImpl implements FestivalService {

    private static final Logger log = LoggerFactory.getLogger(FestivalServiceImpl.class);

    // Inyección manual de dependencias (en un entorno con CDI/Spring se usaría @Inject/@Autowired)
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor que inicializa los repositorios necesarios.
     */
    public FestivalServiceImpl() {
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FestivalDTO crearFestival(FestivalDTO festivalDTO, Integer idPromotor) {
        log.info("Service: Iniciando creación de festival '{}' para promotor ID: {}",
                festivalDTO != null ? festivalDTO.getNombre() : "null", idPromotor);

        // Validación de argumentos principales
        if (festivalDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("FestivalDTO e idPromotor no pueden ser nulos.");
        }
        // Validación de datos básicos del DTO
        validarDatosBasicosFestivalDTO(festivalDTO);

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Verificar que el usuario existe y es un PROMOTOR
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .filter(u -> u.getRol() == RolUsuario.PROMOTOR)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado o inválido con ID: " + idPromotor));

            // Mapear DTO a entidad y establecer relaciones
            Festival festival = mapDtoToEntity(festivalDTO);
            festival.setPromotor(promotor);

            // Asegurar estado inicial BORRADOR
            if (festival.getEstado() == null) {
                festival.setEstado(EstadoFestival.BORRADOR);
                log.debug("Service: Estableciendo estado por defecto a BORRADOR para nuevo festival.");
            }

            // Guardar la nueva entidad
            festival = festivalRepository.save(em, festival);

            tx.commit();
            log.info("Service: Festival '{}' creado exitosamente con ID: {}", festival.getNombre(), festival.getIdFestival());

            return mapEntityToDto(festival);

        } catch (Exception e) {
            handleException(e, tx, "crear festival");
            throw mapException(e); // Relanzar excepción mapeada
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FestivalDTO> obtenerFestivalPorId(Integer id) {
        log.debug("Service: Buscando festival por ID: {}", id);
        if (id == null) {
            log.warn("Intento de obtener festival con ID nulo.");
            return Optional.empty();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Buscar y mapear si se encuentra
            return festivalRepository.findById(em, id).map(this::mapEntityToDto);
        } catch (Exception e) {
            log.error("Service: Error al obtener festival por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty(); // Devolver vacío en caso de error
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FestivalDTO actualizarFestival(Integer id, FestivalDTO festivalDTO, Integer idUsuarioActualizador) {
        log.info("Service: Iniciando actualización de festival ID: {} por Usuario ID: {}", id, idUsuarioActualizador);

        // Validación de argumentos principales
        if (id == null || festivalDTO == null || idUsuarioActualizador == null) {
            throw new IllegalArgumentException("ID de festival, DTO y ID de usuario son requeridos.");
        }
        // Validación de datos básicos del DTO
        validarDatosBasicosFestivalDTO(festivalDTO);

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Verificar usuario actualizador
            Usuario actualizador = usuarioRepository.findById(em, idUsuarioActualizador)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actualizador no encontrado ID: " + idUsuarioActualizador));

            // Buscar festival a actualizar
            Festival festival = festivalRepository.findById(em, id)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + id));

            // Verificar Permiso (ADMIN o Promotor dueño)
            verificarPermisoModificacion(festival, actualizador);

            // Actualizar campos permitidos (el estado NO se cambia aquí)
            festival.setNombre(festivalDTO.getNombre().trim());
            festival.setDescripcion(festivalDTO.getDescripcion() != null ? festivalDTO.getDescripcion().trim() : null);
            festival.setFechaInicio(festivalDTO.getFechaInicio());
            festival.setFechaFin(festivalDTO.getFechaFin());
            festival.setUbicacion(festivalDTO.getUbicacion() != null ? festivalDTO.getUbicacion().trim() : null);
            festival.setAforo(festivalDTO.getAforo());
            festival.setImagenUrl(festivalDTO.getImagenUrl() != null ? festivalDTO.getImagenUrl().trim() : null);

            // Guardar (merge) la entidad actualizada
            festival = festivalRepository.save(em, festival);

            tx.commit();
            log.info("Service: Festival ID: {} actualizado correctamente por Usuario ID: {}", id, idUsuarioActualizador);

            return mapEntityToDto(festival);

        } catch (Exception e) {
            handleException(e, tx, "actualizar festival ID " + id);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eliminarFestival(Integer id, Integer idUsuarioEliminador) {
        log.info("Service: Iniciando eliminación de festival ID: {} por Usuario ID: {}", id, idUsuarioEliminador);
        if (id == null || idUsuarioEliminador == null) {
            throw new IllegalArgumentException("ID de festival y ID de usuario son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Verificar usuario eliminador
            Usuario eliminador = usuarioRepository.findById(em, idUsuarioEliminador)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario eliminador no encontrado ID: " + idUsuarioEliminador));

            // Buscar festival a eliminar
            Festival festival = festivalRepository.findById(em, id)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + id));

            // Verificar Permiso (ADMIN o Promotor dueño)
            verificarPermisoModificacion(festival, eliminador);

            // Intentar eliminar (puede fallar por FK)
            boolean eliminado = festivalRepository.deleteById(em, id);

            // Si deleteById no lanzó excepción pero devolvió false (raro), loggear
            if (!eliminado) {
                log.warn("deleteById devolvió false después de encontrar el festival ID {}, posible inconsistencia.", id);
            }

            tx.commit();
            log.info("Service: Festival ID: {} eliminado (o marcado para eliminar) correctamente por Usuario ID: {}", id, idUsuarioEliminador);

        } catch (PersistenceException e) {
            handleException(e, tx, "eliminar festival ID " + id);
            log.error("Error de persistencia al eliminar festival ID {}. Causa probable: Datos asociados (entradas, etc.).", id);
            // Mapear a una excepción más significativa
            throw new IllegalStateException("No se pudo eliminar el festival ID " + id + " debido a dependencias existentes.", e);
        } catch (Exception e) {
            handleException(e, tx, "eliminar festival ID " + id);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FestivalDTO> buscarFestivalesPublicados(LocalDate fechaDesde, LocalDate fechaHasta) {
        log.debug("Service: Buscando festivales publicados entre {} y {}", fechaDesde, fechaHasta);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // El repositorio maneja la lógica de fechas y estado PUBLICADO
            List<Festival> festivales = festivalRepository.findActivosEntreFechas(em, fechaDesde, fechaHasta);
            log.info("Service: Encontrados {} festivales publicados entre {} y {}", festivales.size(), fechaDesde, fechaHasta);
            return festivales.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Service: Error buscando festivales publicados: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FestivalDTO> obtenerFestivalesPorPromotor(Integer idPromotor) {
        log.debug("Service: Obteniendo festivales para promotor ID: {}", idPromotor);
        if (idPromotor == null) {
            throw new IllegalArgumentException("El ID del promotor es requerido.");
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Opcional: verificar que el promotor existe antes de buscar sus festivales
            // usuarioRepository.findById(em, idPromotor).orElseThrow(() -> new UsuarioNotFoundException(...));
            List<Festival> festivales = festivalRepository.findByPromotorId(em, idPromotor);
            log.info("Service: Encontrados {} festivales para promotor ID: {}", festivales.size(), idPromotor);
            return festivales.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Service: Error obteniendo festivales para promotor ID {}: {}", idPromotor, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FestivalDTO cambiarEstadoFestival(Integer idFestival, EstadoFestival nuevoEstado, Integer idActor) {
        log.info("Service: Iniciando cambio de estado a {} para festival ID: {} por Actor ID: {}", nuevoEstado, idFestival, idActor);
        if (idFestival == null || nuevoEstado == null || idActor == null) {
            throw new IllegalArgumentException("ID de festival, nuevo estado y ID de actor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Verificar Actor y Rol ADMIN
            Usuario actor = usuarioRepository.findById(em, idActor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor));
            if (actor.getRol() != RolUsuario.ADMIN) {
                log.warn("Service: Intento de cambiar estado de festival por usuario no ADMIN (ID: {}, Rol: {})", idActor, actor.getRol());
                throw new SecurityException("Solo los administradores pueden cambiar el estado de un festival.");
            }

            // Buscar festival
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            // Aplicar lógica de transición de estado
            validarTransicionEstado(festival.getEstado(), nuevoEstado);

            // Si el estado no cambia, no hacer nada más
            if (festival.getEstado() == nuevoEstado) {
                log.info("Service: El festival ID {} ya está en estado {}. No se realiza cambio.", idFestival, nuevoEstado);
                tx.commit(); // Hacer commit aunque no haya cambio
                return mapEntityToDto(festival);
            }

            // Actualizar estado y guardar
            log.debug("Service: Actualizando estado de festival {} a {}", idFestival, nuevoEstado);
            festival.setEstado(nuevoEstado);
            festival = festivalRepository.save(em, festival);

            tx.commit();
            log.info("Service: Estado de festival ID: {} cambiado a {} correctamente por Admin ID: {}", idFestival, nuevoEstado, idActor);

            return mapEntityToDto(festival);

        } catch (Exception e) {
            handleException(e, tx, "cambiar estado festival ID " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FestivalDTO> obtenerTodosLosFestivales() {
        log.debug("Service: Obteniendo todos los festivales (Admin).");
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Festival> festivales = festivalRepository.findAll(em);
            log.info("Service: Encontrados {} festivales en total.", festivales.size());
            return festivales.stream()
                    .map(this::mapEntityToDto) // Mapear a DTO
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Service: Error obteniendo todos los festivales: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FestivalDTO> obtenerFestivalesPorEstado(EstadoFestival estado) {
        log.debug("Service: Obteniendo festivales por estado: {} (Admin).", estado);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Festival> festivales;
            if (estado == null) {
                log.debug("Service: Estado es null, obteniendo todos los festivales.");
                festivales = festivalRepository.findAll(em);
            } else {
                festivales = festivalRepository.findByEstado(em, estado);
            }
            log.info("Service: Encontrados {} festivales para estado {}.", festivales.size(), estado == null ? "TODOS" : estado);
            return festivales.stream()
                    .map(this::mapEntityToDto) // Mapear a DTO
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Service: Error obteniendo festivales por estado {}: {}", estado, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda (Helpers) ---
    /**
     * Valida los datos básicos requeridos en un FestivalDTO.
     *
     * @param dto El DTO a validar.
     * @throws IllegalArgumentException Si falta algún dato o es inválido.
     */
    private void validarDatosBasicosFestivalDTO(FestivalDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del festival es obligatorio.");
        }
        if (dto.getFechaInicio() == null || dto.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        // Podrían añadirse más validaciones (ej: formato URL imagen, aforo positivo)
    }

    /**
     * Verifica si un usuario tiene permiso para modificar/eliminar un festival.
     * Permiso concedido si es ADMIN o el PROMOTOR propietario del festival.
     *
     * @param festival El festival a verificar.
     * @param usuario El usuario que intenta realizar la acción.
     * @throws SecurityException Si el usuario no tiene permisos.
     * @throws IllegalArgumentException Si festival o usuario son nulos.
     */
    private void verificarPermisoModificacion(Festival festival, Usuario usuario) {
        if (festival == null || usuario == null) {
            throw new IllegalArgumentException("Festival y Usuario no pueden ser nulos para verificar permiso.");
        }
        boolean esAdmin = usuario.getRol() == RolUsuario.ADMIN;
        boolean esPromotorDueño = festival.getPromotor() != null
                && festival.getPromotor().getIdUsuario().equals(usuario.getIdUsuario());

        if (!(esAdmin || esPromotorDueño)) {
            log.warn("Intento no autorizado de modificar/eliminar festival ID {} por usuario ID {}",
                    festival.getIdFestival(), usuario.getIdUsuario());
            throw new SecurityException("El usuario no tiene permiso para modificar o eliminar este festival.");
        }
        log.trace("Permiso verificado para usuario ID {} sobre festival ID {}",
                usuario.getIdUsuario(), festival.getIdFestival());
    }

    /**
     * Valida si una transición de estado de festival es permitida.
     *
     * @param estadoActual El estado actual del festival.
     * @param nuevoEstado El estado al que se desea transicionar.
     * @throws IllegalStateException Si la transición no es válida.
     */
    private void validarTransicionEstado(EstadoFestival estadoActual, EstadoFestival nuevoEstado) {
        if (estadoActual == nuevoEstado) {
            return; // No hay transición
        }
        switch (estadoActual) {
            case BORRADOR:
                if (nuevoEstado != EstadoFestival.PUBLICADO && nuevoEstado != EstadoFestival.CANCELADO) { // Permitir cancelar desde borrador
                    throw new IllegalStateException("Desde BORRADOR solo se puede pasar a PUBLICADO o CANCELADO.");
                }
                break;
            case PUBLICADO:
                if (nuevoEstado != EstadoFestival.CANCELADO && nuevoEstado != EstadoFestival.FINALIZADO) {
                    throw new IllegalStateException("Desde PUBLICADO solo se puede pasar a CANCELADO o FINALIZADO.");
                }
                break;
            case CANCELADO:
            case FINALIZADO:
                // No permitir salir de estos estados finales
                throw new IllegalStateException("No se puede cambiar el estado de un festival CANCELADO o FINALIZADO.");
            default:
                throw new IllegalStateException("Estado actual desconocido: " + estadoActual);
        }
        log.debug("Transición de estado de {} a {} validada.", estadoActual, nuevoEstado);
    }

    /**
     * Mapea una entidad Festival a su correspondiente FestivalDTO. Incluye el
     * ID y nombre del promotor.
     *
     * @param f La entidad Festival.
     * @return El FestivalDTO mapeado, o {@code null} si la entidad es
     * {@code null}.
     */
    private FestivalDTO mapEntityToDto(Festival f) {
        if (f == null) {
            return null;
        }
        FestivalDTO dto = new FestivalDTO();
        dto.setIdFestival(f.getIdFestival());
        dto.setNombre(f.getNombre());
        dto.setDescripcion(f.getDescripcion());
        dto.setFechaInicio(f.getFechaInicio());
        dto.setFechaFin(f.getFechaFin());
        dto.setUbicacion(f.getUbicacion());
        dto.setAforo(f.getAforo());
        dto.setImagenUrl(f.getImagenUrl());
        dto.setEstado(f.getEstado());
        if (f.getPromotor() != null) {
            dto.setIdPromotor(f.getPromotor().getIdUsuario());
            dto.setNombrePromotor(f.getPromotor().getNombre()); // Añadir nombre para conveniencia
        }
        return dto;
    }

    /**
     * Mapea un FestivalDTO a una nueva entidad Festival. No establece el ID ni
     * el promotor (deben hacerse por separado).
     *
     * @param dto El FestivalDTO.
     * @return La nueva entidad Festival, o {@code null} si el DTO es
     * {@code null}.
     */
    private Festival mapDtoToEntity(FestivalDTO dto) {
        if (dto == null) {
            return null;
        }
        Festival entity = new Festival();
        // No mapeamos idFestival (se genera o ya existe)
        // No mapeamos idPromotor (se asocia la entidad Usuario)
        entity.setNombre(dto.getNombre().trim());
        entity.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        entity.setFechaInicio(dto.getFechaInicio());
        entity.setFechaFin(dto.getFechaFin());
        entity.setUbicacion(dto.getUbicacion() != null ? dto.getUbicacion().trim() : null);
        entity.setAforo(dto.getAforo());
        entity.setImagenUrl(dto.getImagenUrl() != null ? dto.getImagenUrl().trim() : null);
        entity.setEstado(dto.getEstado()); // El estado inicial se maneja en crearFestival
        return entity;
    }

    /**
     * Manejador genérico de excepciones para métodos de servicio. Realiza
     * rollback si hay transacción activa y loggea el error.
     *
     * @param e La excepción capturada.
     * @param tx La transacción activa (puede ser null).
     * @param action Descripción de la acción que falló.
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
     * Cierra el EntityManager si está abierto.
     *
     * @param em El EntityManager a cerrar.
     */
    private void closeEntityManager(EntityManager em) {
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
        // Asegurarse de incluir todas las excepciones personalizadas relevantes
        if (e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException
                || e instanceof SecurityException
                || e instanceof IllegalStateException
                || e instanceof IllegalArgumentException
                || e instanceof PersistenceException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        // Para otras excepciones no esperadas, envolvemos en RuntimeException
        return new RuntimeException("Error inesperado en la capa de servicio Festival: " + e.getMessage(), e);
    }
}
