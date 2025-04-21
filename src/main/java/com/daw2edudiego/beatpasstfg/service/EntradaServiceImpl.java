package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.exception.EntradaNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.Entrada;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.repository.*; // Importar todos los repositorios necesarios
import com.daw2edudiego.beatpasstfg.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de EntradaService. Gestiona la lógica de negocio y las
 * transacciones para Tipos de Entrada.
 * @author Eduardo Olalde
 */
public class EntradaServiceImpl implements EntradaService {

    private static final Logger log = LoggerFactory.getLogger(EntradaServiceImpl.class);

    // Inyección de dependencias (manual)
    private final EntradaRepository entradaRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;

    // Constructor
    public EntradaServiceImpl() {
        this.entradaRepository = new EntradaRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl(); // Necesario para verificar promotor
    }

    @Override
    public EntradaDTO crearEntrada(EntradaDTO entradaDTO, Integer idFestival, Integer idPromotor) {
        log.info("Service: Creando tipo de entrada '{}' para festival ID {} por promotor ID {}",
                entradaDTO.getTipo(), idFestival, idPromotor);

        // Validaciones iniciales
        if (entradaDTO == null || idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("DTO de entrada, ID de festival y ID de promotor son requeridos.");
        }
        if (!idFestival.equals(entradaDTO.getIdFestival())) {
            throw new IllegalArgumentException("El ID del festival en el DTO (" + entradaDTO.getIdFestival() + ") no coincide con el ID del festival en la ruta (" + idFestival + ").");
        }
        validarDatosEntradaDTO(entradaDTO); // Validación de campos del DTO

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Verificar que el promotor existe
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // Verificar que el festival existe y pertenece al promotor
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            if (!festival.getPromotor().getIdUsuario().equals(idPromotor)) {
                log.warn("Intento no autorizado por promotor ID {} para crear entrada en festival ID {}", idPromotor, idFestival);
                throw new SecurityException("No tiene permiso para añadir entradas a este festival.");
            }

            // Mapear DTO a Entidad y establecer relación
            Entrada nuevaEntrada = mapDtoToEntity(entradaDTO);
            nuevaEntrada.setFestival(festival); // Asociar con el festival encontrado

            // Guardar la nueva entrada
            nuevaEntrada = entradaRepository.save(em, nuevaEntrada);

            tx.commit();
            log.info("Tipo de entrada '{}' creada con ID {} para festival ID {}", nuevaEntrada.getTipo(), nuevaEntrada.getIdEntrada(), idFestival);
            return mapEntityToDto(nuevaEntrada); // Devolver DTO de la entrada creada

        } catch (Exception e) {
            log.error("Error creando tipo de entrada para festival ID {}: {}", idFestival, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            // Relanzar excepciones conocidas o una genérica
            if (e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException || e instanceof IllegalArgumentException || e instanceof PersistenceException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado creando tipo de entrada: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<EntradaDTO> obtenerEntradasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo tipos de entrada para festival ID {} por promotor ID {}", idFestival, idPromotor);
        if (idFestival == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de festival y ID de promotor son requeridos.");
        }

        EntityManager em = null;
        // No se necesita transacción explícita para una consulta simple, pero la mantenemos por consistencia
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin(); // Iniciar transacción (aunque sea de lectura)

            // Verificar promotor y propiedad del festival
            Usuario promotor = usuarioRepository.findById(em, idPromotor)
                    .orElseThrow(() -> new UsuarioNotFoundException("Promotor no encontrado con ID: " + idPromotor));
            Festival festival = festivalRepository.findById(em, idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));
            if (!festival.getPromotor().getIdUsuario().equals(idPromotor)) {
                log.warn("Intento no autorizado por promotor ID {} para ver entradas de festival ID {}", idPromotor, idFestival);
                throw new SecurityException("No tiene permiso para ver las entradas de este festival.");
            }

            // Obtener las entradas usando el repositorio
            List<Entrada> entradas = entradaRepository.findByFestivalId(em, idFestival);

            tx.commit(); // Commit (aunque no haya cambios)

            // Mapear a DTOs
            return entradas.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error obteniendo tipos de entrada para festival ID {}: {}", idFestival, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                tx.rollback(); // Rollback en caso de error durante la verificación o consulta
            }
            // Relanzar excepciones conocidas o una genérica
            if (e instanceof FestivalNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException) {
                throw e;
            }
            // Devolver lista vacía o lanzar RuntimeException según se prefiera
            // return Collections.emptyList();
            throw new RuntimeException("Error inesperado obteniendo tipos de entrada: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public EntradaDTO actualizarEntrada(Integer idEntrada, EntradaDTO entradaDTO, Integer idPromotor) {
        log.info("Service: Actualizando tipo de entrada ID {} por promotor ID {}", idEntrada, idPromotor);

        if (idEntrada == null || entradaDTO == null || idPromotor == null) {
            throw new IllegalArgumentException("ID de entrada, DTO y ID de promotor son requeridos.");
        }
        // Podríamos verificar si entradaDTO.getIdEntrada() coincide con idEntrada si viene en el DTO
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

            // Buscar la entrada a actualizar
            Entrada entrada = entradaRepository.findById(em, idEntrada)
                    .orElseThrow(() -> new EntradaNotFoundException("Tipo de entrada no encontrado con ID: " + idEntrada));

            // Verificar propiedad (a través del festival asociado a la entrada)
            if (entrada.getFestival() == null || !entrada.getFestival().getPromotor().getIdUsuario().equals(idPromotor)) {
                log.warn("Intento no autorizado por promotor ID {} para actualizar entrada ID {}", idPromotor, idEntrada);
                throw new SecurityException("No tiene permiso para modificar este tipo de entrada.");
            }

            // Actualizar campos de la entidad desde el DTO
            entrada.setTipo(entradaDTO.getTipo());
            entrada.setDescripcion(entradaDTO.getDescripcion());
            entrada.setPrecio(entradaDTO.getPrecio());
            entrada.setStock(entradaDTO.getStock());
            // No actualizamos el festival asociado

            // Guardar (merge)
            entrada = entradaRepository.save(em, entrada);

            tx.commit();
            log.info("Tipo de entrada ID {} actualizado correctamente.", idEntrada);
            return mapEntityToDto(entrada);

        } catch (Exception e) {
            log.error("Error actualizando tipo de entrada ID {}: {}", idEntrada, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            if (e instanceof EntradaNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException || e instanceof IllegalArgumentException || e instanceof PersistenceException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado actualizando tipo de entrada: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
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
            if (entrada.getFestival() == null || !entrada.getFestival().getPromotor().getIdUsuario().equals(idPromotor)) {
                log.warn("Intento no autorizado por promotor ID {} para eliminar entrada ID {}", idPromotor, idEntrada);
                throw new SecurityException("No tiene permiso para eliminar este tipo de entrada.");
            }

            // Intentar eliminar
            // El repositorio lanzará PersistenceException si hay FKs (ej: CompraEntrada)
            boolean eliminado = entradaRepository.deleteById(em, idEntrada);
            if (!eliminado) {
                // Esto no debería ocurrir si findById funcionó, a menos que deleteById falle silenciosamente
                throw new EntradaNotFoundException("No se pudo eliminar el tipo de entrada con ID: " + idEntrada + " (posiblemente ya no existía).");
            }

            tx.commit();
            log.info("Tipo de entrada ID {} eliminado correctamente.", idEntrada);

        } catch (PersistenceException e) {
            log.error("Error de persistencia eliminando entrada ID {}: {}. Causa probable: entradas vendidas.", idEntrada, e.getMessage());
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            // Lanzar una excepción más descriptiva para el usuario
            throw new RuntimeException("No se puede eliminar el tipo de entrada ID " + idEntrada + " porque ya tiene ventas asociadas.", e);
        } catch (Exception e) {
            log.error("Error eliminando tipo de entrada ID {}: {}", idEntrada, e.getMessage(), e);
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            if (e instanceof EntradaNotFoundException || e instanceof UsuarioNotFoundException || e instanceof SecurityException) {
                throw e;
            }
            throw new RuntimeException("Error inesperado eliminando tipo de entrada: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // --- Métodos privados ---
    /**
     * Valida los campos obligatorios y restricciones del DTO. Lanza
     * IllegalArgumentException si algo es inválido.
     */
    private void validarDatosEntradaDTO(EntradaDTO dto) {
        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de entrada es obligatorio.");
        }
        if (dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio es obligatorio y no puede ser negativo.");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new IllegalArgumentException("El stock es obligatorio y no puede ser negativo.");
        }
        // Añadir más validaciones si es necesario (longitud, formato, etc.)
    }

    /**
     * Mapea Entidad Entrada a EntradaDTO
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
     * Mapea EntradaDTO a Entidad Entrada (sin ID ni Festival)
     */
    private Entrada mapDtoToEntity(EntradaDTO dto) {
        if (dto == null) {
            return null;
        }
        Entrada e = new Entrada();
        // ID se asigna al guardar o se usa para buscar en actualizar
        e.setTipo(dto.getTipo());
        e.setDescripcion(dto.getDescripcion());
        e.setPrecio(dto.getPrecio());
        e.setStock(dto.getStock());
        // El festival se asigna explícitamente en el método del servicio
        return e;
    }
}
