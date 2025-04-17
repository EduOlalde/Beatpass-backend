/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.FestivalDTO; // Necesitaremos crear este DTO
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException; // Excepción personalizada
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.Usuario; // Asumimos que existe la entidad Usuario
import com.daw2edudiego.beatpasstfg.repository.FestivalRepository;
import com.daw2edudiego.beatpasstfg.repository.FestivalRepositoryImpl; // Implementación concreta
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepository; // Necesario para buscar el promotor
import com.daw2edudiego.beatpasstfg.repository.UsuarioRepositoryImpl; // Implementación concreta
import com.daw2edudiego.beatpasstfg.util.JPAUtil; // Utilidad para manejar EM y EMF

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de FestivalService. Gestiona transacciones.
 */
public class FestivalServiceImpl implements FestivalService {

    private static final Logger log = LoggerFactory.getLogger(FestivalServiceImpl.class);

    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository; // Para buscar el promotor

    public FestivalServiceImpl() {
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    @Override
    public FestivalDTO crearFestival(FestivalDTO festivalDTO, Integer idPromotor) {
        log.info("Iniciando creación de festival para promotor ID: {}", idPromotor);
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Buscando promotor con ID: {}", idPromotor);
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                 .orElseThrow(() -> {
                      log.warn("Intento de crear festival por promotor no existente ID: {}", idPromotor);
                      return new IllegalArgumentException("Promotor no encontrado con ID: " + idPromotor);
                 });

            // Validar rol del promotor (si es necesario)
            if(promotor.getRol() != com.daw2edudiego.beatpasstfg.model.RolUsuario.PROMOTOR){
                 log.warn("Usuario ID {} no es un promotor, intento de crear festival denegado.", idPromotor);
                 throw new SecurityException("El usuario especificado no es un promotor.");
            }

            log.debug("Mapeando DTO a entidad Festival");
            Festival festival = mapDtoToEntity(festivalDTO);
            festival.setPromotor(promotor);
            festival.setEstado(EstadoFestival.BORRADOR); // Estado inicial

            log.debug("Guardando entidad Festival");
            festival = festivalRepository.save(em, festival);

            tx.commit();
            log.info("Festival creado exitosamente con ID: {}", festival.getIdFestival());

            return mapEntityToDto(festival);

        } catch (Exception e) {
            log.error("Error durante la creación del festival para promotor ID {}: {}", idPromotor, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                log.warn("Realizando rollback de la transacción de creación de festival.");
                tx.rollback();
            }
            // Relanzar como RuntimeException o una excepción de servicio específica
            throw new RuntimeException("Error creando festival: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                 log.trace("EntityManager cerrado para crearFestival.");
            }
        }
    }

    @Override
    public Optional<FestivalDTO> obtenerFestivalPorId(Integer id) {
        log.debug("Buscando festival por ID: {}", id);
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            // Operación de solo lectura, no requiere transacción explícita aquí
            Optional<Festival> festivalOpt = festivalRepository.findById(em, id);
            log.info("Resultado de búsqueda para festival ID {}: {}", id, festivalOpt.isPresent() ? "Encontrado" : "No encontrado");
            return festivalOpt.map(this::mapEntityToDto);
        } catch (Exception e) {
             log.error("Error al obtener festival por ID {}: {}", id, e.getMessage(), e);
             // Devolver Optional vacío en caso de error inesperado
             return Optional.empty();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                log.trace("EntityManager cerrado para obtenerFestivalPorId.");
            }
        }
    }

    @Override
    public FestivalDTO actualizarFestival(Integer id, FestivalDTO festivalDTO, Integer idPromotor) {
        log.info("Iniciando actualización de festival ID: {} por promotor ID: {}", id, idPromotor);
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Buscando festival a actualizar con ID: {}", id);
            Festival festival = festivalRepository.findById(em, id)
                .orElseThrow(() -> {
                     log.warn("Intento de actualizar festival no existente ID: {}", id);
                     return new FestivalNotFoundException("Festival no encontrado con ID: " + id);
                });

            log.debug("Verificando propiedad del festival para promotor ID: {}", idPromotor);
            if (!festival.getPromotor().getIdUsuario().equals(idPromotor)) {
                 log.warn("Intento no autorizado de actualizar festival ID {} por promotor ID {}", id, idPromotor);
                throw new SecurityException("El usuario no tiene permiso para modificar este festival.");
            }

            log.debug("Actualizando datos de la entidad Festival desde DTO");
            // Actualizar campos permitidos
            festival.setNombre(festivalDTO.getNombre());
            festival.setDescripcion(festivalDTO.getDescripcion());
            festival.setFechaInicio(festivalDTO.getFechaInicio());
            festival.setFechaFin(festivalDTO.getFechaFin());
            festival.setUbicacion(festivalDTO.getUbicacion());
            festival.setAforo(festivalDTO.getAforo());
            festival.setImagenUrl(festivalDTO.getImagenUrl());
            // El estado se cambia con otro método

            log.debug("Guardando (merge) entidad Festival actualizada");
            festival = festivalRepository.save(em, festival);

            tx.commit();
            log.info("Festival ID: {} actualizado correctamente por promotor ID: {}", id, idPromotor);

            return mapEntityToDto(festival);

        } catch (Exception e) {
            log.error("Error durante la actualización del festival ID {}: {}", id, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                 log.warn("Realizando rollback de la transacción de actualización de festival.");
                tx.rollback();
            }
             // Relanzar excepciones específicas o genéricas
             if (e instanceof FestivalNotFoundException || e instanceof SecurityException) {
                 throw e;
             }
            throw new RuntimeException("Error actualizando festival: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                 log.trace("EntityManager cerrado para actualizarFestival.");
            }
        }
    }

     @Override
    public void eliminarFestival(Integer id, Integer idPromotor) {
        log.info("Iniciando eliminación de festival ID: {} por promotor ID: {}", id, idPromotor);
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Buscando festival a eliminar con ID: {}", id);
            Festival festival = festivalRepository.findById(em, id)
                .orElseThrow(() -> {
                     log.warn("Intento de eliminar festival no existente ID: {}", id);
                     return new FestivalNotFoundException("Festival no encontrado con ID: " + id);
                });

            log.debug("Verificando propiedad del festival para promotor ID: {}", idPromotor);
            if (!festival.getPromotor().getIdUsuario().equals(idPromotor)) {
                 log.warn("Intento no autorizado de eliminar festival ID {} por promotor ID {}", id, idPromotor);
                throw new SecurityException("El usuario no tiene permiso para eliminar este festival.");
            }

            log.debug("Eliminando festival con ID: {}", id);
            // El repositorio solo marca para eliminar, la acción ocurre en el commit
            boolean eliminado = festivalRepository.deleteById(em, id);
            if (!eliminado) {
                 // Esto no debería pasar si findById funcionó, pero por si acaso
                 throw new FestivalNotFoundException("Festival no encontrado al intentar eliminar ID: " + id);
            }

            tx.commit();
            log.info("Festival ID: {} eliminado correctamente por promotor ID: {}", id, idPromotor);

        } catch (Exception e) {
            log.error("Error durante la eliminación del festival ID {}: {}", id, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                 log.warn("Realizando rollback de la transacción de eliminación de festival.");
                tx.rollback();
            }
             if (e instanceof FestivalNotFoundException || e instanceof SecurityException) {
                 throw e;
             }
             // Capturar PersistenceException específicamente si hay problemas de FK
             // javax.persistence.PersistenceException o jakarta.persistence.PersistenceException
             // if (e instanceof jakarta.persistence.PersistenceException) { ... }
            throw new RuntimeException("Error eliminando festival: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                 log.trace("EntityManager cerrado para eliminarFestival.");
            }
        }
    }


    @Override
    public List<FestivalDTO> buscarFestivalesPublicados(LocalDate fechaDesde, LocalDate fechaHasta) {
        log.debug("Buscando festivales publicados entre {} y {}", fechaDesde, fechaHasta);
         EntityManager em = null;
         try {
             em = JPAUtil.createEntityManager();
             List<Festival> festivales = festivalRepository.findActivosEntreFechas(em, fechaDesde, fechaHasta);
             log.info("Encontrados {} festivales publicados entre {} y {}", festivales.size(), fechaDesde, fechaHasta);
             return festivales.stream()
                              .map(this::mapEntityToDto)
                              .collect(Collectors.toList());
         } catch (Exception e) {
             log.error("Error buscando festivales publicados: {}", e.getMessage(), e);
             return List.of(); // Devolver lista vacía en caso de error
         } finally {
             if (em != null && em.isOpen()) {
                 em.close();
                 log.trace("EntityManager cerrado para buscarFestivalesPublicados.");
             }
         }
    }

    @Override
    public List<FestivalDTO> obtenerFestivalesPorPromotor(Integer idPromotor) {
        log.debug("Obteniendo festivales para promotor ID: {}", idPromotor);
         EntityManager em = null;
         try {
             em = JPAUtil.createEntityManager();
             // Validar que el promotor existe podría ser un paso previo opcional
             List<Festival> festivales = festivalRepository.findByPromotorId(em, idPromotor);
             log.info("Encontrados {} festivales para promotor ID: {}", festivales.size(), idPromotor);
             return festivales.stream()
                              .map(this::mapEntityToDto)
                              .collect(Collectors.toList());
          } catch (Exception e) {
             log.error("Error obteniendo festivales para promotor ID {}: {}", idPromotor, e.getMessage(), e);
             return List.of();
         } finally {
             if (em != null && em.isOpen()) {
                 em.close();
                 log.trace("EntityManager cerrado para obtenerFestivalesPorPromotor.");
             }
         }
    }

     @Override
    public FestivalDTO cambiarEstadoFestival(Integer idFestival, EstadoFestival nuevoEstado, Integer idPromotor) {
        log.info("Iniciando cambio de estado a {} para festival ID: {} por promotor ID: {}", nuevoEstado, idFestival, idPromotor);
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            log.debug("Buscando festival a cambiar estado con ID: {}", idFestival);
            Festival festival = festivalRepository.findById(em, idFestival)
                .orElseThrow(() -> {
                    log.warn("Intento de cambiar estado a festival no existente ID: {}", idFestival);
                    return new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival);
                });

            log.debug("Verificando propiedad del festival para promotor ID: {}", idPromotor);
            if (!festival.getPromotor().getIdUsuario().equals(idPromotor)) {
                log.warn("Intento no autorizado de cambiar estado a festival ID {} por promotor ID {}", idFestival, idPromotor);
                throw new SecurityException("El usuario no tiene permiso para cambiar el estado de este festival.");
            }

            // Lógica de negocio adicional podría ir aquí (ej: no publicar si falta info)
            log.debug("Cambiando estado de festival {} a {}", idFestival, nuevoEstado);
            festival.setEstado(nuevoEstado);

            festival = festivalRepository.save(em, festival); // merge

            tx.commit();
            log.info("Estado de festival ID: {} cambiado a {} correctamente por promotor ID: {}", idFestival, nuevoEstado, idPromotor);

            return mapEntityToDto(festival);

        } catch (Exception e) {
            log.error("Error durante el cambio de estado del festival ID {}: {}", idFestival, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                 log.warn("Realizando rollback de la transacción de cambio de estado de festival.");
                tx.rollback();
            }
             if (e instanceof FestivalNotFoundException || e instanceof SecurityException) {
                 throw e;
             }
            throw new RuntimeException("Error cambiando estado del festival: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                 log.trace("EntityManager cerrado para cambiarEstadoFestival.");
            }
        }
    }


    // --- Métodos Privados de Mapeo ---

    private FestivalDTO mapEntityToDto(Festival f) {
        if (f == null) return null;
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
        if (dto == null) return null;
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