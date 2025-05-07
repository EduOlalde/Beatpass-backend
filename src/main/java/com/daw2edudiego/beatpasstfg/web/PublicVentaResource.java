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

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Recurso JAX-RS para endpoints públicos de venta y nominación
 * (/api/public/venta). Incluye inicio de pago y confirmación con Stripe.
 */
@Path("/public/venta")
@Produces(MediaType.APPLICATION_JSON)
public class PublicVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PublicVentaResource.class);

    private final VentaService ventaService;
    private final AsistenteService asistenteService;
    private final EntradaAsignadaRepository entradaAsignadaRepository; // Para /nominar
    private final EntradaAsignadaService entradaAsignadaService; // Para /nominar

    public PublicVentaResource() {
        this.ventaService = new VentaServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl();
        this.entradaAsignadaService = new EntradaAsignadaServiceImpl();
    }

    /**
     * Inicia el proceso de pago con Stripe.
     *
     * @param requestDTO DTO con idEntrada y cantidad.
     * @return 200 OK con IniciarCompraResponseDTO (client_secret), o error
     * 4xx/5xx.
     */
    @POST
    @Path("/iniciar-pago")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response iniciarPago(@Valid IniciarCompraRequestDTO requestDTO) {
        log.info("POST /public/venta/iniciar-pago - Entrada ID: {}, Cantidad: {}",
                requestDTO != null ? requestDTO.getIdEntrada() : "null",
                requestDTO != null ? requestDTO.getCantidad() : "null");

        if (requestDTO == null || requestDTO.getIdEntrada() == null || requestDTO.getCantidad() == null || requestDTO.getCantidad() <= 0) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Datos inválidos (idEntrada, cantidad > 0).");
        }

        try {
            IniciarCompraResponseDTO responseDTO = ventaService.iniciarProcesoPago(
                    requestDTO.getIdEntrada(), requestDTO.getCantidad());
            log.info("Proceso de pago iniciado. Devolviendo client_secret.");
            return Response.ok(responseDTO).build();
        } catch (EntradaNotFoundException | FestivalNoPublicadoException | IllegalArgumentException e) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error interno en POST /iniciar-pago: {}", e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al iniciar pago.");
        }
    }

    /**
     * Confirma la compra después de un pago exitoso con Stripe.
     *
     * @param idFestival ID del festival (contexto).
     * @param idEntrada ID del tipo de entrada.
     * @param cantidad Número de entradas.
     * @param emailAsistente Email del comprador.
     * @param nombreAsistente Nombre del comprador.
     * @param telefonoAsistente Teléfono (opcional).
     * @param paymentIntentId ID del PaymentIntent de Stripe ('pi_...')
     * 'succeeded'.
     * @return 200 OK con CompraDTO, o error 4xx/5xx.
     */
    @POST
    @Path("/confirmar-compra")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response confirmarCompraConPago(
            @FormParam("idFestival") Integer idFestival, @FormParam("idEntrada") Integer idEntrada,
            @FormParam("cantidad") Integer cantidad, @FormParam("emailAsistente") String emailAsistente,
            @FormParam("nombreAsistente") String nombreAsistente, @FormParam("telefonoAsistente") String telefonoAsistente,
            @FormParam("paymentIntentId") String paymentIntentId) {

        log.info("POST /public/venta/confirmar-compra - Entrada: {}, Cant: {}, Email: {}, PI: {}",
                idEntrada, cantidad, emailAsistente, paymentIntentId);

        if (idEntrada == null || cantidad == null || cantidad <= 0 || emailAsistente == null || emailAsistente.isBlank()
                || nombreAsistente == null || nombreAsistente.isBlank() || paymentIntentId == null || paymentIntentId.isBlank()) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Faltan datos obligatorios (entrada, cantidad>0, email, nombre, paymentIntentId).");
        }

        try {
            Asistente asistente = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistente, nombreAsistente, telefonoAsistente);
            CompraDTO compraConfirmada = ventaService.confirmarVentaConPago(
                    asistente.getIdAsistente(), idEntrada, cantidad, paymentIntentId);
            log.info("Compra confirmada. Compra ID: {}, PI: {}", compraConfirmada.getIdCompra(), paymentIntentId);
            return Response.ok(compraConfirmada).build();
        } catch (AsistenteNotFoundException | EntradaNotFoundException | FestivalNotFoundException e) {
            return crearRespuestaError(Response.Status.NOT_FOUND, e.getMessage());
        } catch (FestivalNoPublicadoException | StockInsuficienteException | IllegalArgumentException | PagoInvalidoException e) {
            log.warn("Error de negocio/pago al confirmar compra PI {}: {}", paymentIntentId, e.getMessage());
            return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error interno en POST /confirmar-compra PI {}: {}", paymentIntentId, e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al confirmar compra.");
        }
    }

    /**
     * Nomina una entrada usando su código QR. No interactúa con pago.
     *
     * @param codigoQr Contenido del QR.
     * @param emailNominado Email del asistente a nominar.
     * @param nombreNominado Nombre del asistente a nominar.
     * @param telefonoNominado Teléfono (opcional).
     * @return 200 OK con mensaje, o error 4xx/5xx.
     */
    @POST
    @Path("/nominar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response nominarEntrada(
            @FormParam("codigoQr") String codigoQr, @FormParam("emailNominado") String emailNominado,
            @FormParam("nombreNominado") String nombreNominado, @FormParam("telefonoNominado") String telefonoNominado) {

        String qrLog = (codigoQr != null) ? codigoQr.substring(0, Math.min(20, codigoQr.length())) + "..." : "null";
        log.info("POST /public/venta/nominar - QR: {}, Email Nom: {}", qrLog, emailNominado);

        if (codigoQr == null || codigoQr.isBlank() || emailNominado == null || emailNominado.isBlank()
                || nombreNominado == null || nombreNominado.isBlank()) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Código QR, email y nombre del nominado son obligatorios.");
        }

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            EntradaAsignada entradaAsignada = entradaAsignadaRepository.findByCodigoQr(em, codigoQr)
                    .orElseThrow(() -> new EntradaAsignadaNotFoundException("No se encontró entrada con el código QR proporcionado."));
            log.debug("Encontrada EntradaAsignada ID {} para QR", entradaAsignada.getIdEntradaAsignada());

            if (entradaAsignada.getAsistente() != null) {
                throw new IllegalStateException("Esta entrada ya está nominada a: " + entradaAsignada.getAsistente().getEmail());
            }
            if (entradaAsignada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                throw new IllegalStateException("Solo se pueden nominar entradas ACTIVAS. Estado actual: " + entradaAsignada.getEstado());
            }

            Asistente nominado = asistenteService.obtenerOcrearAsistentePorEmail(emailNominado, nombreNominado, telefonoNominado);
            log.debug("Asistente nominado obtenido/creado ID: {}", nominado.getIdAsistente());

            entradaAsignada.setAsistente(nominado);
            entradaAsignada.setFechaAsignacion(LocalDateTime.now());
            entradaAsignadaRepository.save(em, entradaAsignada);
            tx.commit();

            log.info("Entrada ID {} (QR: {}) nominada a asistente ID {}",
                    entradaAsignada.getIdEntradaAsignada(), qrLog, nominado.getIdAsistente());
            Map<String, String> successResponse = Map.of("mensaje", "Entrada nominada correctamente a " + nominado.getEmail());
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

    // --- Métodos Auxiliares ---
    private Response crearRespuestaError(Response.Status status, String mensaje) {
        return Response.status(status).entity(Map.of("error", mensaje)).build();
    }

    private void rollbackTransaction(EntityTransaction tx, String action) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                log.warn("Rollback de transacción de {} realizado.", action);
            } catch (Exception rbEx) {
                log.error("Error crítico durante el rollback de {}: {}", action, rbEx.getMessage(), rbEx);
            }
        }
    }

    private void handleException(Exception e, EntityTransaction tx, String action) {
        log.error("Error durante la acción '{}': {}", action, e.getMessage(), e);
        rollbackTransaction(tx, action);
    }

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
