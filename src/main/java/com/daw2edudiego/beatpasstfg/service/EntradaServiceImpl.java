package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.*;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de EntradaService. ACTUALIZADO: Implementados
 * actualizarEntrada, eliminarEntrada, obtenerEntradaPorId.
 */
public class EntradaServiceImpl implements EntradaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaServiceImpl.class);

    // Dependencias
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
        // ... (Implementación sin cambios) ...
        log.info("Service: Creando tipo de entrada '{}' para festival ID {} por promotor ID {}",
                entradaDTO.getTipo(), idFestival, idPromotor);
        if (entradaDTO == null || idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("DTO, ID festival y ID promotor requeridos.");
        }
        if (!idFestival.equals(entradaDTO.getIdFestival())) {
            throw new IllegalArgumentException("ID festival DTO no coincide con ID ruta.");
        }
        validarDatosEntradaDTO(entradaDTO);
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            Usuario promotor = usuarioRepository.findById(em, idPromotor).orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado: " + idPromotor));
            Festival festival = festivalRepository.findById(em, idFestival).orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor);
            Entrada nuevaEntrada = mapDtoToEntity(entradaDTO);
            nuevaEntrada.setFestival(festival);
            nuevaEntrada = entradaRepository.save(em, nuevaEntrada);
            tx.commit();
            log.info("Tipo de entrada '{}' creada con ID {} para festival ID {}", nuevaEntrada.getTipo(), nuevaEntrada.getIdEntrada(), idFestival);
            return mapEntityToDto(nuevaEntrada);
        } catch (Exception e) {
            handleException(e, tx, "creando tipo entrada");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor) {
        // ... (Implementación sin cambios) ...
        log.debug("Service: Obteniendo tipos de entrada para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID festival y ID promotor requeridos.");
        }
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            Usuario promotor = usuarioRepository.findById(em, idPromotor).orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado: " + idPromotor));
            Festival festival = festivalRepository.findById(em, idFestival).orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado: " + idFestival));
            verificarPropiedadFestival(festival, idPromotor);
            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);
            tx.commit();
            return entradas.stream().map(this::mapEntityToDto).collect(Collectors.toList());
        } catch (Exception e) {
            handleException(e, tx, "obteniendo tipos entrada");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public EntradaDTO actualizarEntrada(Integer idEntrada, EntradaDTO entradaDTO, Integer idPromotor) {
        log.info("Service: Actualizando tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || entradaDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada, DTO y ID de promotor son requeridos.");
        }
        validarDatosEntradaDTO(entradaDTO); // Validar datos del DTO

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Verificar promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // Buscar la entrada a actualizar (con bloqueo si se espera concurrencia alta en stock)
            // Entrada entrada = em.find(Entrada.class, idEntrada, LockModeType.PESSIMISTIC_WRITE);
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // Verificar propiedad
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            // Actualizar campos
            entrada.setTipo(entradaDTO.getTipo());
            entrada.setDescripcion(entradaDTO.getDescripcion());
            entrada.setPrecio(entradaDTO.getPrecio());
            entrada.setStock(entradaDTO.getStock());

            // Guardar (merge)
            entrada = entradaRepository.save(em, entrada);

            tx.commit();
            log.info("Tipo de entrada ID {} actualizado correctamente.", idEntrada);
            return mapEntityToDto(entrada);

        } catch (Exception e) {
            handleException(e, tx, "actualizando tipo entrada");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public void eliminarEntrada(Integer idEntrada, Integer idPromotor) {
        log.info("Service: Eliminando tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada y ID de promotor son requeridos.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Verificar promotor
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // Buscar la entrada a eliminar
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // Verificar propiedad
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            // Intentar eliminar (puede fallar por FK si hay CompraEntrada)
            boolean eliminado = entradaRepository.deleteById(em, idEntrada);
            if (!eliminado) { // Si no se encontró después de la verificación inicial (raro)
                throw new EntradaNotFoundException("No se pudo eliminar el tipo de entrada con ID: " + idEntrada);
            }

            tx.commit();
            log.info("Tipo de entrada ID {} eliminado correctamente.", idEntrada);

        } catch (PersistenceException e) {
            // Error específico si hay dependencias (ej: entradas vendidas)
            handleException(e, tx, "eliminando tipo entrada (PersistenceException)");
            throw new RuntimeException("No se puede eliminar el tipo de entrada ID " + idEntrada + " porque tiene ventas asociadas.", e);
        } catch (Exception e) {
            handleException(e, tx, "eliminando tipo entrada");
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    @Override
    public Optional<EntradaDTO> obtenerEntradaPorId(Integer idEntrada, Integer idPromotor) {
        log.debug("Service: Obteniendo tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);
        if (idEntrada == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada y ID de promotor son requeridos.");
        }
        EntityManager em = null;
        EntityTransaction tx = null; // Opcional para lectura
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // Verificar propiedad
            verificarPropiedadFestival(entrada.getFestival(), idPromotor);

            tx.commit();
            return Optional.of(mapEntityToDto(entrada));

        } catch (Exception e) {
            handleException(e, tx, "obteniendo tipo entrada por ID");
            if (e instanceof EntradaNotFoundException || e instanceof SecurityException) {
                log.warn("No se pudo obtener tipo entrada ID {} para promotor ID {}: {}", idEntrada, idPromotor, e.getMessage());
                return Optional.empty(); // Devolver vacío si no existe o no tiene permiso
            }
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos privados (sin cambios) ---
    private void validarDatosEntradaDTO(EntradaDTO dto) {
        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new IllegalArgumentException("Tipo obligatorio.");
        }
        if (dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Precio obligatorio y no negativo.");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new IllegalArgumentException("Stock obligatorio y no negativo.");
        }
    }

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

    private Entrada mapDtoToEntity(EntradaDTO dto) {
        if (dto == null) {
            return null;
        }
        Entrada e = new Entrada();
        e.setTipo(dto.getTipo());
        e.setDescripcion(dto.getDescripcion());
        e.setPrecio(dto.getPrecio());
        e.setStock(dto.getStock());
        return e;
    }

    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
            throw new IllegalStateException("Error interno: Festival asociado no encontrado.");
        }
        if (festival.getPromotor() == null || !festival.getPromotor().getIdUsuario().equals(idPromotor)) {
            throw new SecurityException("No tiene permiso para realizar acciones sobre este festival/entrada.");
        }
    }

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

    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    private RuntimeException mapException(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en servicio: " + e.getMessage(), e);
    }
}
