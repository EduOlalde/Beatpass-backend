package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.Asistente;
import com.daw2edudiego.beatpasstfg.model.EntradaAsignada;
import com.daw2edudiego.beatpasstfg.model.EstadoEntradaAsignada;
import com.daw2edudiego.beatpasstfg.service.*;
import com.daw2edudiego.beatpasstfg.repository.EntradaAsignadaRepository;
import com.daw2edudiego.beatpasstfg.repository.EntradaAsignadaRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;
import com.daw2edudiego.beatpasstfg.util.QRCodeUtil;

// Imports JAX-RS
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Clases estándar Java
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Recurso JAX-RS que expone endpoints RESTful públicos para operaciones de
 * venta y nominación de entradas, destinados a ser consumidos por las páginas
 * web de venta de entradas de los festivales.
 * <p>
 * Estos endpoints están diseñados para ser accesibles sin autenticación JWT
 * compleja (excluidos por el
 * {@link com.daw2edudiego.beatpasstfg.security.AuthenticationFilter}). Sin
 * embargo, en un entorno de producción real, deberían implementarse medidas de
 * seguridad adicionales (CSRF, rate limiting, captchas, etc.).
 * </p>
 *
 * @see VentaService
 * @see AsistenteService
 * @see EntradaAsignadaService
 * @author Eduardo Olalde
 */
@Path("/public/venta") // Nueva ruta base pública
@Produces(MediaType.APPLICATION_JSON)
public class PublicVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PublicVentaResource.class);

    // Inyección manual de dependencias
    private final VentaService ventaService;
    private final AsistenteService asistenteService;
    private final EntradaAsignadaRepository entradaAsignadaRepository;
    private final EntradaAsignadaService entradaAsignadaService;

    /**
     * Constructor que inicializa los servicios y repositorios necesarios.
     */
    public PublicVentaResource() {
        this.ventaService = new VentaServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        this.entradaAsignadaService = new EntradaAsignadaServiceImpl();
    }

    /**
     * Endpoint POST público para realizar la compra de entradas. Recibe los
     * datos del asistente y la compra deseada. Internamente, obtiene o crea el
     * asistente y luego registra la venta.
     *
     * @param idFestival ID del festival (puede ser útil para validaciones
     * futuras).
     * @param idEntrada ID del tipo de entrada a comprar.
     * @param cantidad Número de entradas a comprar.
     * @param emailAsistente Email del comprador.
     * @param nombreAsistente Nombre del comprador.
     * @param telefonoAsistente Teléfono del comprador (opcional).
     * @return Respuesta HTTP (ver Javadoc de VentaService).
     */
    @POST
    @Path("/comprar") // Ruta: /api/public/venta/comprar
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response comprarEntradas(
            @FormParam("idFestival") Integer idFestival,
            @FormParam("idEntrada") Integer idEntrada,
            @FormParam("cantidad") Integer cantidad,
            @FormParam("emailAsistente") String emailAsistente,
            @FormParam("nombreAsistente") String nombreAsistente,
            @FormParam("telefonoAsistente") String telefonoAsistente) {

        log.info("POST /public/venta/comprar - Festival: {}, Entrada: {}, Cant: {}, Email: {}",
                idFestival, idEntrada, cantidad, emailAsistente);

        // Validación básica
        if (idFestival == null || idEntrada == null || cantidad == null || cantidad <= 0
                || emailAsistente == null || emailAsistente.isBlank()
                || nombreAsistente == null || nombreAsistente.isBlank()) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Faltan datos obligatorios (festival, entrada, cantidad > 0, email, nombre).");
        }

        try {
            // 1. Obtener o crear asistente
            Asistente asistente = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistente, nombreAsistente, telefonoAsistente);
            log.debug("Asistente obtenido/creado ID: {}", asistente.getIdAsistente());

            // 2. Registrar la venta
            ventaService.registrarVenta(asistente.getIdAsistente(), idEntrada, cantidad);
            log.info("Venta registrada exitosamente para asistente ID {}", asistente.getIdAsistente());

            // 3. Recuperar entradas generadas
            List<EntradaAsignadaDTO> entradasGeneradasDTOs = obtenerEntradasRecienCompradas(asistente.getIdAsistente(), idEntrada, cantidad);

            return Response.ok(entradasGeneradasDTOs).build();

        } catch (AsistenteNotFoundException | EntradaNotFoundException | FestivalNotFoundException e) {
            return crearRespuestaError(Response.Status.NOT_FOUND, e.getMessage());
        } catch (FestivalNoPublicadoException | StockInsuficienteException | IllegalArgumentException e) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error interno en POST /public/venta/comprar: {}", e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al procesar la compra.");
        }
    }

    /**
     * Endpoint POST público para nominar una entrada usando su código QR.
     *
     * @param codigoQr Contenido del código QR de la entrada a nominar.
     * @param emailNominado Email del asistente al que se nomina.
     * @param nombreNominado Nombre del asistente al que se nomina.
     * @param telefonoNominado Teléfono del asistente (opcional).
     * @return Respuesta HTTP (ver Javadoc de EntradaAsignadaService o
     * SimuladorResource).
     */
    @POST
    @Path("/nominar") // Ruta: /api/public/venta/nominar
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response nominarEntrada(
            @FormParam("codigoQr") String codigoQr,
            @FormParam("emailNominado") String emailNominado,
            @FormParam("nombreNominado") String nombreNominado,
            @FormParam("telefonoNominado") String telefonoNominado) {

        String qrLog = (codigoQr != null) ? codigoQr.substring(0, Math.min(20, codigoQr.length())) + "..." : "null";
        log.info("POST /public/venta/nominar - QR: {}, Email Nom: {}", qrLog, emailNominado);

        // Validación
        if (codigoQr == null || codigoQr.isBlank()
                || emailNominado == null || emailNominado.isBlank()
                || nombreNominado == null || nombreNominado.isBlank()) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Código QR, email y nombre del nominado son obligatorios.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Buscar Entrada por QR
            Optional<EntradaAsignada> entradaOpt = entradaAsignadaRepository.findByCodigoQr(em, codigoQr);
            if (entradaOpt.isEmpty()) {
                throw new EntradaAsignadaNotFoundException("No se encontró ninguna entrada con el código QR proporcionado.");
            }
            EntradaAsignada entradaAsignada = entradaOpt.get();
            log.debug("Encontrada EntradaAsignada ID {} para QR", entradaAsignada.getIdEntradaAsignada());

            // 2. Validar estado
            if (entradaAsignada.getAsistente() != null) {
                throw new IllegalStateException("Esta entrada ya está nominada al asistente: " + entradaAsignada.getAsistente().getEmail());
            }
            if (entradaAsignada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden nominar entradas que estén en estado ACTIVA. Estado actual: " + entradaAsignada.getEstado());
            }

            // 3. Obtener/Crear Asistente nominado
            Asistente nominado = asistenteService.obtenerOcrearAsistentePorEmail(emailNominado, nombreNominado, telefonoNominado);
            log.debug("Asistente nominado obtenido/creado ID: {}", nominado.getIdAsistente());

            // 4. Nominar
            entradaAsignada.setAsistente(nominado);
            entradaAsignada.setFechaAsignacion(LocalDateTime.now());

            // 5. Guardar
            entradaAsignadaRepository.save(em, entradaAsignada);

            tx.commit();
            log.info("Entrada ID {} (QR: {}) nominada exitosamente a asistente ID {}",
                    entradaAsignada.getIdEntradaAsignada(), qrLog, nominado.getIdAsistente());

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("mensaje", "Entrada nominada correctamente a " + nominado.getEmail());
            return Response.ok(successResponse).build();

        } catch (EntradaAsignadaNotFoundException e) {
            rollbackTransaction(tx, "nominar por QR");
            return crearRespuestaError(Response.Status.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            rollbackTransaction(tx, "nominar por QR");
            return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            handleException(e, tx, "nominar por QR");
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al procesar la nominación.");
        } finally {
            closeEntityManager(em);
        }
    }

    // --- Métodos Auxiliares (Copiados de SimuladorResource) ---
    /**
     * Método auxiliar para obtener las entradas recién compradas. NOTA:
     * Solución temporal y potencialmente ineficiente.
     */
    private List<EntradaAsignadaDTO> obtenerEntradasRecienCompradas(Integer idAsistente, Integer idEntrada, int cantidad) {
        EntityManager em = null;
        try {
            em = JPAUtil.createEntityManager();
            String jpql = "SELECT ea FROM EntradaAsignada ea "
                    + "JOIN ea.compraEntrada ce "
                    + "JOIN ce.compra c "
                    + "WHERE c.asistente.idAsistente = :asistenteId "
                    + "AND ce.entrada.idEntrada = :entradaId "
                    + "ORDER BY ea.idEntradaAsignada DESC";

            List<EntradaAsignada> entradas = em.createQuery(jpql, EntradaAsignada.class)
                    .setParameter("asistenteId", idAsistente)
                    .setParameter("entradaId", idEntrada)
                    .setMaxResults(cantidad)
                    .getResultList();

            if (entradas.size() != cantidad) {
                log.warn("Se esperaban {} entradas generadas para asistente {} y entrada {}, pero se encontraron {}.",
                        cantidad, idAsistente, idEntrada, entradas.size());
            }

            // Replicando lógica de mapeo temporalmente:
            return entradas.stream()
                    .map(ea -> {
                        EntradaAsignadaDTO dto = new EntradaAsignadaDTO();
                        dto.setIdEntradaAsignada(ea.getIdEntradaAsignada());
                        dto.setCodigoQr(ea.getCodigoQr());
                        dto.setEstado(ea.getEstado());
                        dto.setFechaAsignacion(ea.getFechaAsignacion());
                        if (ea.getCodigoQr() != null && !ea.getCodigoQr().isBlank()) {
                            dto.setQrCodeImageDataUrl(QRCodeUtil.generarQrComoBase64(ea.getCodigoQr(), 100, 100));
                        }
                        if (ea.getCompraEntrada() != null && ea.getCompraEntrada().getEntrada() != null) {
                            dto.setTipoEntradaOriginal(ea.getCompraEntrada().getEntrada().getTipo());
                        }
                        // Añadir datos del asistente si está nominado (aunque aquí no debería estarlo aún)
                        if (ea.getAsistente() != null) {
                            dto.setIdAsistente(ea.getAsistente().getIdAsistente());
                            dto.setNombreAsistente(ea.getAsistente().getNombre());
                            dto.setEmailAsistente(ea.getAsistente().getEmail());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error recuperando entradas recién compradas para asistente {} y entrada {}: {}", idAsistente, idEntrada, e.getMessage(), e);
            return List.of(); // Devolver lista vacía en caso de error
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * Crea una respuesta de error JAX-RS estándar.
     */
    private Response crearRespuestaError(Response.Status status, String mensaje) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", mensaje);
        return Response.status(status).entity(errorResponse).build();
    }

    /**
     * Realiza rollback de una transacción si está activa.
     */
    private void rollbackTransaction(EntityTransaction tx, String action) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Rollback de transacción de {} realizado debido a error.", action);
            } catch (Exception rbEx) {
                log.error("Error crítico durante el rollback de {}: {}", action, rbEx.getMessage(), rbEx);
            }
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
     * Cierra el EntityManager.
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                em.close();
            } catch (Exception e) {
                log.error("Error al cerrar EntityManager: {}", e.getMessage(), e);
            }
        }
    }
}
