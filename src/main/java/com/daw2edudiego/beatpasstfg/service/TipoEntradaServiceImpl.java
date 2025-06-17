package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.TipoEntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.TipoEntrada;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.repository.TipoEntradaRepositoryImpl;
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
import com.daw2edudiego.beatpasstfg.repository.TipoEntradaRepository;

/**
 * Implementación de TipoEntradaService.
 */
public class TipoEntradaServiceImpl implements TipoEntradaService {

    private static final Logger log = LoggerFactory.getLogger(TipoEntradaServiceImpl.class);

    private final TipoEntradaRepository tipoEntradaRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;

    public TipoEntradaServiceImpl() {
        this.tipoEntradaRepository = new TipoEntradaRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    @Override
    public TipoEntradaDTO crearTipoEntrada(TipoEntradaDTO tipoEntradaDTO, Integer idFestival, Integer idPromotor) {
        log.info("Service: Creando nuevo tipo de entrada para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (tipoEntradaDTO == null || idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("DTO, ID festival e ID promotor son requeridos.");
        }
        if (tipoEntradaDTO.getIdFestival() != null && !idFestival.equals(tipoEntradaDTO.getIdFestival())) {
            throw new IllegalArgumentException("El ID del festival en el DTO no coincide con el ID de la URL.");
        }
        validarDatosEntradaDTO(tipoEntradaDTO);

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

            TipoEntrada nuevaEntrada = mapDtoToEntity(tipoEntradaDTO);
            nuevaEntrada.setFestival(festival);

            TipoEntrada entradaGuardada = tipoEntradaRepository.save(em, nuevaEntrada);
            tx.commit();

            log.info("Nuevo tipo de entrada ID {} creado para festival ID {}", entradaGuardada.getIdTipoEntrada(), idFestival);
            return mapEntityToDto(entradaGuardada);

        } catch (Exception e) {
            handleException(e, tx, "crear entrada para festival " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<TipoEntradaDTO> obtenerTipoEntradasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo tipos de entrada para festival ID {} por promotor ID {}", idFestival, idPromotor);
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

            List<TipoEntrada> tiposEntrada = tipoEntradaRepository.findByFestivalId(em, idFestival);
            tx.commit();

            log.info("Encontrados {} tipos de entrada para el festival ID {} (Promotor {})", tiposEntrada.size(), idFestival, idPromotor);
            return tiposEntrada.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            handleException(e, tx, "obtener tipos de entrada por festival " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<TipoEntradaDTO> obtenerTiposEntradaPublicasPorFestival(Integer idFestival) {
        log.debug("Service: Obteniendo tipos de entrada públicas para festival ID {}", idFestival);
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
                log.warn("Intento de acceso a tipos de entrada de festival ID {} no publicado (Estado: {})", idFestival, festival.getEstado());
                throw new FestivalNoPublicadoException("El festival ID " + idFestival + " no está actualmente publicado.");
            }

            List<TipoEntrada> tiposEntrada = tipoEntradaRepository.findByFestivalId(em, idFestival);
            tx.commit();

            log.info("Encontrados {} tipos de entrada para el festival público ID {}", tiposEntrada.size(), idFestival);
            return tiposEntrada.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());

        } catch (FestivalNotFoundException | FestivalNoPublicadoException e) {
            rollbackTransaction(tx, "obtener tipos de entrada públicas festival " + idFestival);
            throw e;
        } catch (Exception e) {
            handleException(e, tx, "obtener tipos de entrada públicas festival " + idFestival);
            log.error("Error inesperado obteniendo tipos de entrada públicas para festival ID {}: {}", idFestival, e.getMessage());
            return Collections.emptyList();
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public TipoEntradaDTO actualizarTipoEntrada(Integer idEntrada, TipoEntradaDTO tipoEntradaDTO, Integer idPromotor) {
        log.info("Service: Actualizando tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || tipoEntradaDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("ID entrada, DTO e ID promotor son requeridos.");
        }
        if (tipoEntradaDTO.getIdTipoEntrada() != null && !idEntrada.equals(tipoEntradaDTO.getIdTipoEntrada())) {
            throw new IllegalArgumentException("El ID de entrada en el DTO debe coincidir con el ID de la URL o ser nulo.");
        }
        validarDatosEntradaDTO(tipoEntradaDTO);

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            TipoEntrada entrada = tipoEntradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            // Actualización de los campos
            entrada.setTipo(tipoEntradaDTO.getTipo().trim());
            entrada.setDescripcion(tipoEntradaDTO.getDescripcion() != null ? tipoEntradaDTO.getDescripcion().trim() : null);
            entrada.setPrecio(tipoEntradaDTO.getPrecio());
            entrada.setStock(tipoEntradaDTO.getStock());
            entrada.setRequiereNominacion(tipoEntradaDTO.getRequiereNominacion());

            TipoEntrada entradaActualizada = tipoEntradaRepository.save(em, entrada);
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
    public void eliminarTipoEntrada(Integer idEntrada, Integer idPromotor) {
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

            TipoEntrada entrada = tipoEntradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new TipoEntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            boolean eliminado = tipoEntradaRepository.deleteById(em, idEntrada);
            if (!eliminado) {
                log.warn("La entrada ID {} fue encontrada pero no pudo ser eliminada.", idEntrada);
                throw new TipoEntradaNotFoundException("No se pudo eliminar la entrada ID " + idEntrada + ".");
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
    public Optional<TipoEntradaDTO> obtenerTipoEntradaPorId(Integer idEntrada, Integer idPromotor) {
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

            Optional<TipoEntrada> entradaOpt = tipoEntradaRepository.findById(em, idEntrada);

            if (entradaOpt.isEmpty()) {
                tx.commit();
                log.warn("Entrada no encontrada con ID {}", idEntrada);
                return Optional.empty();
            }

            TipoEntrada entrada = entradaOpt.get();
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);
            tx.commit();

            return Optional.of(mapEntityToDto(entrada));

        } catch (Exception e) {
            handleException(e, tx, "obtener entrada por ID " + idEntrada);
            if (e instanceof TipoEntradaNotFoundException || e instanceof SecurityException || e instanceof UsuarioNotFoundException) {
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
     * Valida los campos básicos de un TipoEntradaDTO.
     */
    private void validarDatosEntradaDTO(TipoEntradaDTO dto) {
        if (dto.getTipo() == null || dto.getTipo().isBlank()
                || dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0
                || dto.getStock() == null || dto.getStock() < 0) {
            throw new IllegalArgumentException("Datos inválidos en DTO: tipo, precio (>=0) y stock (>=0) son obligatorios.");
        }
    }

    /**
     * Mapea DTO a entidad TipoEntrada (sin asociar festival).
     */
    private TipoEntrada mapDtoToEntity(TipoEntradaDTO dto) {
        if (dto == null) {
            return null;
        }
        TipoEntrada entity = new TipoEntrada();
        entity.setTipo(dto.getTipo().trim());
        entity.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        entity.setPrecio(dto.getPrecio());
        entity.setStock(dto.getStock());
        if (dto.getRequiereNominacion() != null) {
            entity.setRequiereNominacion(dto.getRequiereNominacion());
        }
        return entity;
    }

    /**
     * Mapea entidad TipoEntrada a DTO.
     */
    private TipoEntradaDTO mapEntityToDto(TipoEntrada entrada) {
        if (entrada == null) {
            return null;
        }
        TipoEntradaDTO dto = new TipoEntradaDTO();
        dto.setIdTipoEntrada(entrada.getIdTipoEntrada());
        if (entrada.getFestival() != null) {
            dto.setIdFestival(entrada.getFestival().getIdFestival());
        }
        dto.setTipo(entrada.getTipo());
        dto.setDescripcion(entrada.getDescripcion());
        dto.setPrecio(entrada.getPrecio());
        dto.setStock(entrada.getStock());
        dto.setRequiereNominacion(entrada.getRequiereNominacion());

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
        if (e instanceof AsistenteNotFoundException || e instanceof TipoEntradaNotFoundException || e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException || e instanceof FestivalNoPublicadoException || e instanceof StockInsuficienteException
                || e instanceof EntradaNotFoundException || e instanceof IllegalArgumentException || e instanceof SecurityException
                || e instanceof IllegalStateException || e instanceof PersistenceException || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio: " + e.getMessage(), e);
    }
}
