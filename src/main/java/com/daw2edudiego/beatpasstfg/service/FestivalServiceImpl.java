/*
 * Implementación de la interfaz FestivalService.
 * Contiene la lógica de negocio para operaciones relacionadas con Festivales,
 * gestiona las transacciones JPA y utiliza los repositorios para el acceso a datos.
 * ACTUALIZADO: Solo ADMIN puede cambiar estado de festival.
 */
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
import com.daw2edudiego.beatpasstfg.util.JPAUtil; // Utilidad para EntityManager

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
 * Implementación de FestivalService. Gestiona la lógica de negocio y las
 * transacciones para Festivales.
 */
public class FestivalServiceImpl implements FestivalService {

    private static final Logger log = LoggerFactory.getLogger(FestivalServiceImpl.class);

    // Inyección de dependencias (manual en este caso)
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;

    // Constructor donde se instancian los repositorios
    public FestivalServiceImpl() {
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    // --- Métodos crearFestival, obtenerFestivalPorId, actualizarFestival (sin cambios relevantes aquí) ---
    @Override
    public FestivalDTO crearFestival(FestivalDTO festivalDTO, Integer idPromotor) {
        log.info("Service: Iniciando creación de festival '{}' para promotor ID: {}", festivalDTO.getNombre(), idPromotor);
        // Validación básica de entrada
        if (festivalDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("FestivalDTO e idPromotor no pueden ser nulos.");
        }
        if (festivalDTO.getNombre() == null || festivalDTO.getNombre().isBlank()
                || festivalDTO.getFechaInicio() == null || festivalDTO.getFechaFin() == null
                || festivalDTO.getFechaFin().isBefore(festivalDTO.getFechaInicio())) {
            throw new IllegalArgumentException("Datos básicos del festival (nombre, fechas válidas) son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager(); // Obtener EntityManager
            tx = em.getTransaction(); // Iniciar transacción
            tx.begin();

            log.debug("Service: Buscando promotor con ID: {}", idPromotor);
            // Buscar la entidad Usuario del promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .filter(u -> u.getRol() == RolUsuario.PROMOTOR) // Asegurar que es un promotor
                    .orElseThrow(() -> {
                        log.warn("Service: Promotor no encontrado o no válido con ID: {}", idPromotor);
                        return new UsuarioNotFoundException("Promotor no encontrado o inválido con ID: " + idPromotor);
                    });

            log.debug("Service: Mapeando DTO a entidad Festival");
            Festival festival = mapDtoToEntity(festivalDTO); // Mapear DTO a Entidad
            festival.setPromotor(promotor); // Establecer la relación con el promotor encontrado

            // Asegurar estado inicial BORRADOR si no se especifica
            if (festival.getEstado() == null) {
                festival.setEstado(EstadoFestival.BORRADOR);
                log.debug("Service: Estableciendo estado por defecto a BORRADOR.");
            }

            log.debug("Service: Guardando entidad Festival en BD");
            festival = festivalRepository.save(em, festival); // Persistir la nueva entidad

            tx.commit(); // Confirmar transacción
            log.info("Service: Festival '{}' creado exitosamente con ID: {}", festival.getNombre(), festival.getIdFestival());

            return mapEntityToDto(festival); // Devolver DTO de la entidad creada

        } catch (Exception e) {
            log.error("Service: Error durante la creación del festival para promotor ID {}: {}", idPromotor, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Service: Realizando rollback de la transacción de creación de festival.");
                tx.rollback(); // Revertir cambios en caso de error
            }
            // Relanzar excepciones específicas o una genérica
            if (e instanceof UsuarioNotFoundException || e instanceof IllegalArgumentException || e instanceof PersistenceException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado creando festival: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Siempre cerrar el EntityManager
                log.trace("Service: EntityManager cerrado para crearFestival.");
            }
        }
    }

    @Override
    public Optional<FestivalDTO> obtenerFestivalPorId(Integer id) {
        log.debug("Service: Buscando festival por ID: {}", id);
        if (id == null) {
            return Optional.empty();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Usar el repositorio para buscar
            Optional<Festival> festivalOpt = festivalRepository.findById(em, id);
            log.info("Service: Resultado de búsqueda para festival ID {}: {}", id, festivalOpt.isPresent() ? "Encontrado" : "No encontrado");
            // Mapear a DTO si se encuentra
            return festivalOpt.map(this::mapEntityToDto);
        } catch (Exception e) {
            log.error("Service: Error al obtener festival por ID {}: {}", id, e.getMessage(), e);
            return Optional.empty(); // Devolver vacío en caso de error
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("Service: EntityManager cerrado para obtenerFestivalPorId.");
            }
        }
    }

    @Override
    public FestivalDTO actualizarFestival(Integer id, FestivalDTO festivalDTO, Integer idUsuarioActualizador) {
        log.info("Service: Iniciando actualización de festival ID: {} por Usuario ID: {}", id, idUsuarioActualizador);
        // Validaciones
        if (id == null || festivalDTO == null || idUsuarioActualizador == null) {
            throw new IllegalArgumentException("ID de festival, DTO y ID de usuario son requeridos.");
        }
        if (festivalDTO.getNombre() == null || festivalDTO.getNombre().isBlank()
                || festivalDTO.getFechaInicio() == null || festivalDTO.getFechaFin() == null
                || festivalDTO.getFechaFin().isBefore(festivalDTO.getFechaInicio())) {
            throw new IllegalArgumentException("Datos básicos del festival (nombre, fechas válidas) son requeridos para actualizar.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Service: Buscando usuario actualizador ID: {}", idUsuarioActualizador);
            Usuario actualizador = usuarioRepository.findById(em, idUsuarioActualizador)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actualizador no encontrado ID: " + idUsuarioActualizador));

            log.debug("Service: Buscando festival a actualizar con ID: {}", id);
            Festival festival = festivalRepository.findById(em, id)
                    .orElseThrow(() -> {
                        log.warn("Service: Intento de actualizar festival no existente ID: {}", id);
                        return new FestivalNotFoundException("Festival no encontrado con ID: " + id);
                    });

            // Verificar Permiso: O es Admin o es el Promotor dueño
            if (!(actualizador.getRol() == RolUsuario.ADMIN || festival.getPromotor().getIdUsuario().equals(idUsuarioActualizador))) {
                log.warn("Service: Intento no autorizado de actualizar festival ID {} por usuario ID {}", id, idUsuarioActualizador);
                throw new SecurityException("El usuario no tiene permiso para modificar este festival.");
            }
            log.debug("Service: Permiso verificado para usuario ID {} sobre festival ID {}", idUsuarioActualizador, id);

            log.debug("Service: Actualizando datos de la entidad Festival desde DTO (estado NO se modifica aquí)");
            // Actualizar solo los campos permitidos (NO cambiar estado aquí)
            festival.setNombre(festivalDTO.getNombre());
            festival.setDescripcion(festivalDTO.getDescripcion());
            festival.setFechaInicio(festivalDTO.getFechaInicio());
            festival.setFechaFin(festivalDTO.getFechaFin());
            festival.setUbicacion(festivalDTO.getUbicacion());
            festival.setAforo(festivalDTO.getAforo());
            festival.setImagenUrl(festivalDTO.getImagenUrl());
            // El estado se gestiona con cambiarEstadoFestival() por un ADMIN

            log.debug("Service: Guardando (merge) entidad Festival actualizada");
            festival = festivalRepository.save(em, festival); // Llama a merge

            tx.commit();
            log.info("Service: Festival ID: {} actualizado correctamente por Usuario ID: {}", id, idUsuarioActualizador);

            return mapEntityToDto(festival);

        } catch (Exception e) {
            log.error("Service: Error durante la actualización del festival ID {}: {}", id, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Service: Realizando rollback de la transacción de actualización de festival.");
                tx.rollback();
            }
            // Relanzar excepciones conocidas
            if (e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException || e instanceof IllegalArgumentException || e instanceof PersistenceException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado actualizando festival: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("Service: EntityManager cerrado para actualizarFestival.");
            }
        }
    }

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

            log.debug("Service: Buscando usuario eliminador ID: {}", idUsuarioEliminador);
            Usuario eliminador = usuarioRepository.findById(em, idUsuarioEliminador)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario eliminador no encontrado ID: " + idUsuarioEliminador));

            log.debug("Service: Buscando festival a eliminar con ID: {}", id);
            Festival festival = festivalRepository.findById(em, id)
                    .orElseThrow(() -> {
                        log.warn("Service: Intento de eliminar festival no existente ID: {}", id);
                        return new FestivalNotFoundException("Festival no encontrado con ID: " + id);
                    });

            // Verificar Permiso: O es Admin o es el Promotor dueño
            if (!(eliminador.getRol() == RolUsuario.ADMIN || festival.getPromotor().getIdUsuario().equals(idUsuarioEliminador))) {
                log.warn("Service: Intento no autorizado de eliminar festival ID {} por usuario ID {}", id, idUsuarioEliminador);
                throw new SecurityException("El usuario no tiene permiso para eliminar este festival.");
            }
            log.debug("Service: Permiso verificado para usuario ID {} sobre festival ID {}", idUsuarioEliminador, id);

            log.debug("Service: Marcando festival ID {} para eliminación.", id);
            em.remove(festival);

            tx.commit();
            log.info("Service: Festival ID: {} eliminado correctamente por Usuario ID: {}", id, idUsuarioEliminador);

        } catch (PersistenceException e) {
            log.error("Service: Error de persistencia al eliminar festival ID {}: {}. Causa probable: Datos asociados (entradas, etc.).", id, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Service: Rollback por error de persistencia en eliminación.");
                tx.rollback();
            }
            throw new RuntimeException("No se pudo eliminar el festival ID " + id + " debido a dependencias existentes.", e);
        } catch (Exception e) {
            log.error("Service: Error durante la eliminación del festival ID {}: {}", id, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Service: Realizando rollback de la transacción de eliminación de festival.");
                tx.rollback();
            }
            if (e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException || e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado eliminando festival: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("Service: EntityManager cerrado para eliminarFestival.");
            }
        }
    }

    @Override
    public List<FestivalDTO> buscarFestivalesPublicados(LocalDate fechaDesde, LocalDate fechaHasta) {
        log.debug("Service: Buscando festivales publicados entre {} y {}", fechaDesde, fechaHasta);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Festival> festivales = festivalRepository.findActivosEntreFechas(em, fechaDesde, fechaHasta);
            log.info("Service: Encontrados {} festivales publicados entre {} y {}", festivales.size(), fechaDesde, fechaHasta);
            return festivales.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Service: Error buscando festivales publicados: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("Service: EntityManager cerrado para buscarFestivalesPublicados.");
            }
        }
    }

    @Override
    public List<FestivalDTO> obtenerFestivalesPorPromotor(Integer idPromotor) {
        log.debug("Service: Obteniendo festivales para promotor ID: {}", idPromotor);
        if (idPromotor == null) {
            return Collections.emptyList();
        }
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Festival> festivales = festivalRepository.findByPromotorId(em, idPromotor);
            log.info("Service: Encontrados {} festivales para promotor ID: {}", festivales.size(), idPromotor);
            return festivales.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Service: Error obteniendo festivales para promotor ID {}: {}", idPromotor, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("Service: EntityManager cerrado para obtenerFestivalesPorPromotor.");
            }
        }
    }

    /**
     * Cambia el estado de un festival. SOLO el ADMIN puede hacerlo.
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

            log.debug("Service: Buscando actor con ID: {}", idActor);
            Usuario actor = usuarioRepository.findById(em, idActor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario actor no encontrado con ID: " + idActor));

            // *** CAMBIO AQUÍ: Verificar que el actor sea ADMIN ***
            if (actor.getRol() != RolUsuario.ADMIN) {
                log.warn("Service: Intento de cambiar estado de festival por usuario no ADMIN (ID: {}, Rol: {})", idActor, actor.getRol());
                throw new SecurityException("Solo los administradores pueden cambiar el estado de un festival.");
            }
            // *******************************************************

            log.debug("Service: Buscando festival a cambiar estado con ID: {}", idFestival);
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> {
                        log.warn("Service: Intento de cambiar estado a festival no existente ID: {}", idFestival);
                        return new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival);
                    });

            // --- Lógica de Negocio Adicional (Opcional, específica para Admin) ---
            // El admin puede cambiar a PUBLICADO (si está en BORRADOR)
            // o a CANCELADO/FINALIZADO (si está PUBLICADO).
            // Podríamos añadir más reglas si fuera necesario.
            if (nuevoEstado == EstadoFestival.PUBLICADO && festival.getEstado() != EstadoFestival.BORRADOR) {
                log.warn("Service: Admin ID {} intentó publicar festival ID {} que no está en BORRADOR (estado actual: {})", idActor, idFestival, festival.getEstado());
                throw new IllegalStateException("Solo se pueden publicar festivales que están en estado BORRADOR.");
            }
            if ((nuevoEstado == EstadoFestival.CANCELADO || nuevoEstado == EstadoFestival.FINALIZADO) && festival.getEstado() != EstadoFestival.PUBLICADO) {
                log.warn("Service: Admin ID {} intentó cancelar/finalizar festival ID {} que no está PUBLICADO (estado actual: {})", idActor, idFestival, festival.getEstado());
                throw new IllegalStateException("Solo se pueden cancelar o finalizar festivales que están PUBLICADOS.");
            }
            // Podríamos impedir volver a BORRADOR una vez publicado/cancelado/finalizado por el admin.
            if (nuevoEstado == EstadoFestival.BORRADOR && festival.getEstado() != EstadoFestival.BORRADOR) {
                log.warn("Service: Admin ID {} intentó devolver festival ID {} a BORRADOR (estado actual: {})", idActor, idFestival, festival.getEstado());
                throw new IllegalStateException("No se puede devolver un festival a BORRADOR una vez procesado.");
            }

            if (festival.getEstado() == nuevoEstado) {
                log.info("Service: El festival ID {} ya está en estado {}. No se realiza cambio.", idFestival, nuevoEstado);
                tx.commit(); // Hacer commit aunque no haya cambio para cerrar transacción
                return mapEntityToDto(festival); // Devolver estado actual
            }
            // --- Fin Lógica de Negocio ---

            log.debug("Service: Actualizando estado de festival {} a {}", idFestival, nuevoEstado);
            festival.setEstado(nuevoEstado);

            festival = festivalRepository.save(em, festival); // Guardar (merge) el cambio

            tx.commit();
            log.info("Service: Estado de festival ID: {} cambiado a {} correctamente por Admin ID: {}", idFestival, nuevoEstado, idActor);

            return mapEntityToDto(festival);

        } catch (Exception e) {
            log.error("Service: Error durante el cambio de estado del festival ID {}: {}", idFestival, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Service: Realizando rollback de la transacción de cambio de estado de festival.");
                tx.rollback();
            }
            // Relanzar excepciones conocidas
            if (e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException || e instanceof IllegalStateException || e instanceof IllegalArgumentException || e instanceof PersistenceException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado cambiando estado del festival: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("Service: EntityManager cerrado para cambiarEstadoFestival.");
            }
        }
    }

    @Override
    public List<FestivalDTO> obtenerTodosLosFestivales() {
        log.debug("Service: Obteniendo todos los festivales (Admin).");
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            List<Festival> festivales = festivalRepository.findAll(em);
            log.info("Service: Encontrados {} festivales en total.", festivales.size());
            return festivales.stream()
                    .map(this::mapEntityToDto) // Mapear a DTO (incluye nombre promotor)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Service: Error obteniendo todos los festivales: {}", e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("Service: EntityManager cerrado para obtenerTodosLosFestivales.");
            }
        }
    }

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
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("Service: EntityManager cerrado para obtenerFestivalesPorEstado.");
            }
        }
    }

    // --- Métodos Privados de Mapeo ---
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
            dto.setNombrePromotor(f.getPromotor().getNombre());
        }
        return dto;
    }

    private Festival mapDtoToEntity(FestivalDTO dto) {
        if (dto == null) {
            return null;
        }
        Festival entity = new Festival();
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setFechaInicio(dto.getFechaInicio());
        entity.setFechaFin(dto.getFechaFin());
        entity.setUbicacion(dto.getUbicacion());
        entity.setAforo(dto.getAforo());
        entity.setImagenUrl(dto.getImagenUrl());
        entity.setEstado(dto.getEstado());
        return entity;
    }
}
