package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraRequestDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraResponseDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.Asistente;
import com.daw2edudiego.beatpasstfg.model.EntradaAsignada;
import com.daw2edudiego.beatpasstfg.model.EstadoEntradaAsignada;
import com.daw2edudiego.beatpasstfg.service.*;
import com.daw2edudiego.beatpasstfg.repository.EntradaAsignadaRepository;
import com.daw2edudiego.beatpasstfg.repository.EntradaAsignadaRepositoryImpl;
import com.daw2edudiego.beatpasstfg.util.JPAUtil;

// Imports JAX-RS
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.validation.Valid;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Clases estándar Java
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Recurso JAX-RS que expone endpoints RESTful públicos para operaciones de
 * venta y nominación de entradas. Ahora incluye la confirmación de pago.
 *
 * @see VentaService
 * @see AsistenteService
 * @see EntradaAsignadaService
 * @author Eduardo Olalde
 */
@Path("/public/venta")
@Produces(MediaType.APPLICATION_JSON)
public class PublicVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PublicVentaResource.class);

    // Servicios
    private final VentaService ventaService;
    private final AsistenteService asistenteService;
    private final EntradaAsignadaRepository entradaAsignadaRepository; // Mantenido para /nominar
    private final EntradaAsignadaService entradaAsignadaService; // Mantenido para /nominar

    /**
     * Constructor que inicializa los servicios y repositorios necesarios.
     */
    public PublicVentaResource() {
        this.ventaService = new VentaServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl(); // Para /nominar
        this.entradaAsignadaService = new EntradaAsignadaServiceImpl(); // Para /nominar
    }

    /**
     * Endpoint POST público para iniciar el proceso de pago. Recibe el ID de la
     * entrada y la cantidad, calcula el total, crea un PaymentIntent en Stripe
     * y devuelve el client_secret.
     *
     * @param requestDTO DTO que contiene idEntrada y cantidad.
     * @return Respuesta HTTP: 200 OK con el client_secret, o error 4xx/5xx.
     */
    @POST
    @Path("/iniciar-pago") // Nueva ruta para iniciar el pago
    @Consumes(MediaType.APPLICATION_JSON) // Espera JSON en el cuerpo
    public Response iniciarPago(@Valid IniciarCompraRequestDTO requestDTO) {

        log.info("POST /public/venta/iniciar-pago - Entrada ID: {}, Cantidad: {}",
                requestDTO != null ? requestDTO.getIdEntrada() : "null",
                requestDTO != null ? requestDTO.getCantidad() : "null");

        // Validación básica (complementaria a @Valid)
        if (requestDTO == null || requestDTO.getIdEntrada() == null || requestDTO.getCantidad() == null || requestDTO.getCantidad() <= 0) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Datos inválidos. Se requiere idEntrada y cantidad > 0.");
        }

        try {
            // Llamar al servicio para iniciar el proceso y obtener el DTO de respuesta
            IniciarCompraResponseDTO responseDTO = ventaService.iniciarProcesoPago(
                    requestDTO.getIdEntrada(),
                    requestDTO.getCantidad()
            );

            log.info("Proceso de pago iniciado. Devolviendo client_secret para Entrada ID {}", requestDTO.getIdEntrada());
            // Devolver 200 OK con el DTO que contiene el client_secret
            return Response.ok(responseDTO).build();

        } catch (EntradaNotFoundException | FestivalNoPublicadoException e) {
            // Errores conocidos que indican que la compra no puede proceder
            log.warn("No se pudo iniciar el pago para Entrada ID {}: {}", requestDTO.getIdEntrada(), e.getMessage());
            return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage()); // 400 Bad Request
        } catch (IllegalArgumentException e) {
            // Errores en los parámetros de entrada
            log.warn("Argumento inválido al iniciar pago para Entrada ID {}: {}", requestDTO.getIdEntrada(), e.getMessage());
            return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage()); // 400 Bad Request
        } catch (Exception e) {
            // Errores inesperados (Stripe API, cálculo, etc.)
            log.error("Error interno en POST /public/venta/iniciar-pago para Entrada ID {}: {}", requestDTO.getIdEntrada(), e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al iniciar el proceso de pago."); // 500 Internal Server Error
        }
    }

    /**
     * Endpoint POST público para confirmar la compra de entradas DESPUÉS de que
     * el pago haya sido procesado exitosamente en el frontend con Stripe.
     * Recibe el ID del PaymentIntent confirmado.
     *
     * @param idFestival ID del festival (puede ser útil para contexto).
     * @param idEntrada ID del tipo de entrada comprado.
     * @param cantidad Número de entradas compradas.
     * @param emailAsistente Email del comprador (para buscar/crear asistente).
     * @param nombreAsistente Nombre del comprador (para crear asistente si no
     * existe).
     * @param telefonoAsistente Teléfono del comprador (opcional).
     * @param paymentIntentId ID del PaymentIntent de Stripe ('pi_...'), debe
     * estar 'succeeded'.
     * @return Respuesta HTTP: 200 OK con DTO de la compra si éxito, o error
     * 4xx/5xx.
     */
    @POST
    @Path("/confirmar-compra")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response confirmarCompraConPago(
            @FormParam("idFestival") Integer idFestival, // Aún puede ser útil para contexto
            @FormParam("idEntrada") Integer idEntrada,
            @FormParam("cantidad") Integer cantidad,
            @FormParam("emailAsistente") String emailAsistente,
            @FormParam("nombreAsistente") String nombreAsistente,
            @FormParam("telefonoAsistente") String telefonoAsistente,
            @FormParam("paymentIntentId") String paymentIntentId) { // ID de Stripe

        log.info("POST /public/venta/confirmar-compra - Entrada: {}, Cant: {}, Email: {}, PI: {}",
                idEntrada, cantidad, emailAsistente, paymentIntentId);

        // Validación básica de parámetros obligatorios
        if (idEntrada == null || cantidad == null || cantidad <= 0
                || emailAsistente == null || emailAsistente.isBlank()
                || nombreAsistente == null || nombreAsistente.isBlank()
                || paymentIntentId == null || paymentIntentId.isBlank()) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Faltan datos obligatorios (entrada, cantidad > 0, email, nombre, paymentIntentId).");
        }

        try {
            // 1. Obtener o crear asistente (igual que antes)
            Asistente asistente = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistente, nombreAsistente, telefonoAsistente);
            log.debug("Asistente obtenido/creado ID: {}", asistente.getIdAsistente());

            // 2. Llamar al NUEVO método del servicio que verifica el pago y registra la venta
            CompraDTO compraConfirmada = ventaService.confirmarVentaConPago(
                    asistente.getIdAsistente(),
                    idEntrada,
                    cantidad,
                    paymentIntentId
            );

            log.info("Compra confirmada exitosamente con pago verificado. Compra ID: {}, PI: {}",
                    compraConfirmada.getIdCompra(), paymentIntentId);

            // Devolver 200 OK con el DTO de la compra confirmada
            return Response.ok(compraConfirmada).build();

        } catch (AsistenteNotFoundException | EntradaNotFoundException | FestivalNotFoundException e) {
            return crearRespuestaError(Response.Status.NOT_FOUND, e.getMessage());
        } catch (FestivalNoPublicadoException | StockInsuficienteException | IllegalArgumentException | VentaServiceImpl.PagoInvalidoException e) {
            if (e instanceof VentaServiceImpl.PagoInvalidoException) {
                log.warn("Error de pago al confirmar compra PI {}: {}", paymentIntentId, e.getMessage());
                return crearRespuestaError(Response.Status.BAD_REQUEST, "Error con el pago: " + e.getMessage());
            } else {
                log.warn("Error de validación/negocio al confirmar compra PI {}: {}", paymentIntentId, e.getMessage());
                return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error interno en POST /public/venta/confirmar-compra para PI {}: {}", paymentIntentId, e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al confirmar la compra.");
        }
    }

    /**
     * Endpoint POST público para nominar una entrada usando su código QR. (Se
     * mantiene igual, ya que no interactúa con el pago).
     *
     * @param codigoQr Contenido del código QR de la entrada a nominar.
     * @param emailNominado Email del asistente al que se nomina.
     * @param nombreNominado Nombre del asistente al que se nomina.
     * @param telefonoNominado Teléfono del asistente (opcional).
     * @return Respuesta HTTP.
     */
    @POST
    @Path("/nominar")
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

    // --- Métodos Auxiliares (Copiados y mantenidos) ---
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

    // --- Endpoint Obsoleto ---
    /**
     * Endpoint original para comprar entradas. Ahora obsoleto, usar
     * /confirmar-compra.
     *
     * @deprecated Usar
     * {@link #confirmarCompraConPago(Integer, Integer, Integer, String, String, String, String)}
     */
    @POST
    @Path("/comprar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Deprecated
    public Response comprarEntradasObsoleto(
            @FormParam("idFestival") Integer idFestival,
            @FormParam("idEntrada") Integer idEntrada,
            @FormParam("cantidad") Integer cantidad,
            @FormParam("emailAsistente") String emailAsistente,
            @FormParam("nombreAsistente") String nombreAsistente,
            @FormParam("telefonoAsistente") String telefonoAsistente) {
        log.warn("Llamada a endpoint obsoleto POST /public/venta/comprar");
        return crearRespuestaError(Response.Status.GONE, "Este endpoint está obsoleto. Use /api/public/venta/confirmar-compra después de procesar el pago.");
    }

}
