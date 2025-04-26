package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.Asistente;
import com.daw2edudiego.beatpasstfg.model.Compra;
import com.daw2edudiego.beatpasstfg.model.CompraEntrada;
import com.daw2edudiego.beatpasstfg.model.Festival;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.repository.*;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la interfaz {@link CompraService}. Gestiona la lógica de
 * negocio para consultar compras, coordinando repositorios y transacciones.
 *
 * @author Eduardo Olalde
 */
public class CompraServiceImpl implements CompraService {

    private static final Logger log = LoggerFactory.getLogger(CompraServiceImpl.class);

    // Inyección manual de dependencias
    private final CompraRepository compraRepository;
    private final FestivalRepository festivalRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor que inicializa los repositorios necesarios.
     */
    public CompraServiceImpl() {
        this.compraRepository = new CompraRepositoryImpl();
        this.festivalRepository = new FestivalRepositoryImpl();
        this.usuarioRepository = new UsuarioRepositoryImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CompraDTO> obtenerComprasPorFestival(Integer idFestival, Integer idPromotor) {
        log.debug("Service: Obteniendo compras para festival ID {} por promotor ID {}", idFestival, idPromotor);
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

            // 3. Obtener las compras del repositorio
            List<Compra> compras = compraRepository.findByFestivalId(em, idFestival);

            tx.commit(); // Confirmar transacción de lectura

            // 4. Mapear a DTOs y devolver
            log.info("Encontradas {} compras para el festival ID {} (Promotor {})", compras.size(), idFestival, idPromotor);
            return compras.stream()
                    .map(this::mapCompraToDto) // Mapear cada compra a DTO
                    .collect(Collectors.toList());

        } catch (Exception e) {
            handleException(e, tx, "obtener compras por festival " + idFestival);
            throw mapException(e);
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos de Ayuda (Helpers) ---

    /**
     * Mapea una entidad Compra a su correspondiente CompraDTO.
     * Incluye información del asistente y un resumen de las entradas.
     *
     * @param compra La entidad Compra.
     * @return El CompraDTO mapeado, o {@code null} si la entidad es {@code null}.
     */
    private CompraDTO mapCompraToDto(Compra compra) {
        if (compra == null) {
            return null;
        }
        CompraDTO dto = new CompraDTO();
        dto.setIdCompra(compra.getIdCompra());
        dto.setFechaCompra(compra.getFechaCompra());
        dto.setTotal(compra.getTotal());

        // Mapear datos del asistente
        Asistente asistente = compra.getAsistente();
        if (asistente != null) {
            dto.setIdAsistente(asistente.getIdAsistente());
            dto.setNombreAsistente(asistente.getNombre());
            dto.setEmailAsistente(asistente.getEmail());
        }

        // Crear resumen de entradas (ej: "2 x General", "1 x VIP")
        List<String> resumen = new ArrayList<>();
        if (compra.getDetallesCompra() != null) {
            for (CompraEntrada detalle : compra.getDetallesCompra()) {
                String tipo = (detalle.getEntrada() != null) ? detalle.getEntrada().getTipo() : "Desconocido";
                resumen.add(detalle.getCantidad() + " x " + tipo);
            }
        }
        dto.setResumenEntradas(resumen);

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
     * @throws FestivalNotFoundException si el festival es nulo.
     */
    private void verificarPropiedadFestival(Festival festival, Integer idPromotor) {
        if (festival == null) {
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
     * Manejador genérico de excepciones para métodos de servicio.
     */
    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

    /**
     * Realiza rollback de una transacción si está activa.
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
     * Cierra el EntityManager si está abierto.
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    /**
     * Mapea excepciones técnicas a excepciones de negocio o RuntimeException.
     */
    private RuntimeException mapException(Exception e) {
        if (e instanceof FestivalNotFoundException
                || e instanceof UsuarioNotFoundException
                || e instanceof SecurityException
                || e instanceof IllegalArgumentException
                || e instanceof IllegalStateException
                || e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Error inesperado en la capa de servicio Compra: " + e.getMessage(), e);
    }
}
