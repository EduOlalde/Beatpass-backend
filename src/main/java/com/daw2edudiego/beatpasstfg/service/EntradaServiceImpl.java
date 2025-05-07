package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.Entrada;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
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
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de EntradaService.
 */
public class EntradaServiceImpl implements EntradaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaServiceImpl.class);

    private final EntradaRepository entradaRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;

    public EntradaServiceImpl() {
        this.entradaRepository = new EntradaRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    @Override
    public EntradaDTO crearEntrada(EntradaDTO entradaDTO, Integer idFestival, Integer idPromotor) {
        log.info("Service: Creando nuevo tipo de entrada para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (entradaDTO == null || idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("DTO, ID festival e ID promotor son requeridos.");
        }
        if (entradaDTO.getIdFestival() != null && !idFestival.equals(entradaDTO.getIdFestival())) {
            throw new IllegalArgumentException("El ID del festival en el DTO no coincide con el ID de la URL.");
        }
        validarDatosEntradaDTO(entradaDTO);

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

            Entrada nuevaEntrada = mapDtoToEntity(entradaDTO);
            nuevaEntrada.setFestival(festival);

            Entrada entradaGuardada = entradaRepository.save(em, nuevaEntrada);
            tx.commit();

            log.info("Nuevo tipo de entrada ID {} creado para festival ID {}", entradaGuardada.getIdEntrada(), idFestival);
            return mapEntityToDto(entradaGuardada);

        } catch (Exception e) {
            handleException(e, tx, "crear entrada para festival " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo entradas para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID festival e ID promotor son requeridos.");
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

            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);
            tx.commit();

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

    @Override
    public List<EntradaDTO> obtenerEntradasPublicasPorFestival(Integer idFestival) {
        log.debug("Service: Obteniendo entradas públicas para festival ID {}", idFestival);
        if (idFestival == null) {
            throw new IllegalArgumentException("ID de festival es requerido.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            if (festival.getEstado() != EstadoFestival.PUBLICADO) {
                log.warn("Intento de acceso a entradas de festival ID {} no publicado (Estado: {})", idFestival, festival.getEstado());
                throw new FestivalNoPublicadoException("El festival ID " + idFestival + " no está actualmente publicado.");
            }

            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);
            tx.commit();

            log.info("Encontrados {} tipos de entrada para el festival público ID {}", entradas.size(), idFestival);
            return entradas.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());

        } catch (FestivalNotFoundException | FestivalNoPublicadoException e) {
            rollbackTransaction(tx, "obtener entradas públicas festival " + idFestival);
            throw e;
        } catch (Exception e) {
            handleException(e, tx, "obtener entradas públicas festival " + idFestival);
            log.error("Error inesperado obteniendo entradas públicas para festival ID {}: {}", idFestival, e.getMessage());
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public EntradaDTO actualizarEntrada(Integer idEntrada, EntradaDTO entradaDTO, Integer idPromotor) {
        log.info("Service: Actualizando tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || entradaDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("ID entrada, DTO e ID promotor son requeridos.");
        }
        if (entradaDTO.getIdEntrada() != null && !idEntrada.equals(entradaDTO.getIdEntrada())) {
            throw new IllegalArgumentException("El ID de entrada en el DTO debe coincidir con el ID de la URL o ser nulo.");
        }
        validarDatosEntradaDTO(entradaDTO);

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            entrada.setTipo(entradaDTO.getTipo().trim());
            entrada.setDescripcion(entradaDTO.getDescripcion() != null ? entradaDTO.getDescripcion().trim() : null);
            entrada.setPrecio(entradaDTO.getPrecio());
            entrada.setStock(entradaDTO.getStock());

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

    @Override
    public void eliminarEntrada(Integer idEntrada, Integer idPromotor) {
        log.info("Service: Eliminando tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID entrada e ID promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            boolean eliminado = entradaRepository.deleteById(em, idEntrada);
            if (!eliminado) {
                log.warn("La entrada ID {} fue encontrada pero no pudo ser eliminada.", idEntrada);
                throw new EntradaNotFoundException("No se pudo eliminar la entrada ID " + idEntrada + ".");
            }
            tx.commit();

            log.info("Tipo de entrada ID {} eliminado exitosamente.", idEntrada);

        } catch (PersistenceException e) {
            handleException(e, tx, "eliminar entrada " + idEntrada);
            log.error("Error de persistencia eliminando entrada ID {}. ¿Ventas asociadas?", idEntrada);
            throw new RuntimeException("No se puede eliminar el tipo de entrada ID " + idEntrada + " porque tiene ventas asociadas.", e);
        } catch (Exception e) {
            handleException(e, tx, "eliminar entrada " + idEntrada);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor) {
        log.debug("Service: Obteniendo entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("IDs de entrada y promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Optional<Entrada> entradaOpt = entradaRepository.findById(em, idEntrada);

            if (entradaOpt.isEmpty()) {
                tx.commit();
                log.warn("Entrada no encontrada con ID {}", idEntrada);
                return Optional.empty();
            }

            Entrada entrada = entradaOpt.get();
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);
            tx.commit();

            return Optional.of(mapEntityToDto(entrada));

        } catch (Exception e) {
            handleException(e, tx, "obtener entrada por ID " + idEntrada);
            if (e instanceof EntradaNotFoundException || e instanceof SecurityException || e instanceof UsuarioNotFoundException) {
                log.warn("No se pudo obtener entrada ID {} para promotor ID {}: {}", idEntrada, idPromotor, e.getMessage());
                return Optional.empty();
            }
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Privados de Ayuda ---
    /**
     * Valida los campos básicos de un EntradaDTO.
     */
    private void validarDatosEntradaDTO(EntradaDTO dto) {
        if (dto.getTipo() == null || dto.getTipo().isBlank()
                || dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0
                || dto.getStock() == null || dto.getStock() < 0) {
            throw new IllegalArgumentException("Datos inválidos en DTO: tipo, precio (>=0) y stock (>=0) son obligatorios.");
        }
    }

    /**
     * Mapea DTO a entidad Entrada (sin asociar festival).
     */
    private Entrada mapDtoToEntity(EntradaDTO dto) {
        if (dto == null) {
            return null;
        }
        Entrada entity = new Entrada();
        // No mapeamos idEntrada ni idFestival aquí
        entity.setTipo(dto.getTipo().trim());
        entity.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        entity.setPrecio(dto.getPrecio());
        entity.setStock(dto.getStock());
        return entity;
    }

    /**
     * Mapea entidad Entrada a DTO.
     */
    private EntradaDTO mapEntityToDto(Entrada entrada) {
        if (entrada == null) {
            return null;
        }
        EntradaDTO dto = new EntradaDTO();
        dto.setIdEntrada(entrada.getIdEntrada());
        if (entrada.getFestival() != null) {
            dto.setIdFestival(entrada.getFestival().getIdFestival());
        }
        dto.setTipo(entrada.getTipo());
        dto.setDescripcion(entrada.getDescripcion());
        dto.setPrecio(entrada.getPrecio());
        dto.setStock(entrada.getStock());
        return dto;
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
     * Manejador genérico de excepciones.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    /**
     * Realiza rollback si la transacción está activa.
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
                || e instanceof EntradaAsignadaNotFoundException || e instanceof IllegalArgumentException || e instanceof SecurityException
                || e instanceof IllegalStateException || e instanceof PersistenceException || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }
}
