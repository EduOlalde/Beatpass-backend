package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraRequestDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraResponseDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.Asistente;
// import com.daw2edudiego.beatpasstfg.model.EntradaAsignada; // Ya no se usa directamente aquí
// import com.daw2edudiego.beatpasstfg.model.EstadoEntradaAsignada; // Ya no se usa directamente aquí
import com.daw2edudiego.beatpasstfg.service.*;
// import com.daw2edudiego.beatpasstfg.repository.EntradaAsignadaRepository; // Ya no se usa directamente aquí
// import com.daw2edudiego.beatpasstfg.repository.EntradaAsignadaRepositoryImpl; // Ya no se usa directamente aquí
// import com.daw2edudiego.beatpasstfg.util.JPAUtil; // Ya no se usa directamente aquí

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
// import jakarta.persistence.EntityManager; // Ya no se usa directamente aquí
// import jakarta.persistence.EntityTransaction; // Ya no se usa directamente aquí
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import java.time.LocalDateTime; // Ya no se usa directamente aquí
import java.util.Map;

/**
 * Recurso JAX-RS para endpoints públicos de venta y nominación
 * (/api/public/venta). Incluye inicio de pago y confirmación con Stripe.
 */
@Path("/public/venta")
@Produces(MediaType.APPLICATION_JSON)
public class PublicVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PublicVentaResource.class);

    private final VentaService ventaService;
    private final AsistenteService asistenteService; // Se mantiene si es necesario para otros endpoints o lógica aquí
    // private final EntradaAsignadaRepository entradaAsignadaRepository; // Ya no es necesario
    private final EntradaAsignadaService entradaAsignadaService; // Ahora se usa el servicio

    public PublicVentaResource() {
        this.ventaService = new VentaServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        // this.entradaAsignadaRepository = new EntradaAsignadaRepositoryImpl(); // Ya no es necesario
        this.entradaAsignadaService = new EntradaAsignadaServiceImpl(); // Inyectar el servicio
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
            log.warn("Error de validación o negocio en POST /iniciar-pago: {}", e.getMessage());
            return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error interno en POST /iniciar-pago: {}", e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al iniciar pago.");
        }
    }

    /**
     * Confirma la compra después de un pago exitoso con Stripe.
     *
     * @param idFestival ID del festival (contexto, aunque podría obtenerse de
     * la entrada).
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
            @FormParam("idFestival") Integer idFestival, // Considerar si este ID es realmente necesario si la entrada ya lo tiene
            @FormParam("idEntrada") Integer idEntrada,
            @FormParam("cantidad") Integer cantidad,
            @FormParam("emailAsistente") String emailAsistente,
            @FormParam("nombreAsistente") String nombreAsistente,
            @FormParam("telefonoAsistente") String telefonoAsistente,
            @FormParam("paymentIntentId") String paymentIntentId) {

        log.info("POST /public/venta/confirmar-compra - Entrada: {}, Cant: {}, Email: {}, PI: {}",
                idEntrada, cantidad, emailAsistente, paymentIntentId);

        if (idEntrada == null || cantidad == null || cantidad <= 0 || emailAsistente == null || emailAsistente.isBlank()
                || nombreAsistente == null || nombreAsistente.isBlank() || paymentIntentId == null || paymentIntentId.isBlank()) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Faltan datos obligatorios (entrada, cantidad>0, email, nombre, paymentIntentId).");
        }

        try {
            // Obtener o crear el asistente. AsistenteService maneja su propia lógica de persistencia.
            Asistente asistente = asistenteService.obtenerOcrearAsistentePorEmail(emailAsistente, nombreAsistente, telefonoAsistente);

            // Llamar al servicio de venta para confirmar.
            CompraDTO compraConfirmada = ventaService.confirmarVentaConPago(
                    asistente.getIdAsistente(), idEntrada, cantidad, paymentIntentId);

            log.info("Compra confirmada. Compra ID: {}, PI: {}", compraConfirmada.getIdCompra(), paymentIntentId);
            return Response.ok(compraConfirmada).build();

        } catch (AsistenteNotFoundException | EntradaNotFoundException | FestivalNotFoundException e) {
            log.warn("Recurso no encontrado al confirmar compra PI {}: {}", paymentIntentId, e.getMessage());
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
     * Nomina una entrada usando su código QR. La lógica de nominación y envío
     * de email se delega a EntradaAsignadaService.
     *
     * @param codigoQr Contenido del QR.
     * @param emailNominado Email del asistente a nominar.
     * @param nombreNominado Nombre del asistente a nominar.
     * @param telefonoNominado Teléfono (opcional).
     * @return 200 OK con mensaje y EntradaAsignadaDTO, o error 4xx/5xx.
     */
    @POST
    @Path("/nominar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response nominarEntrada(
            @FormParam("codigoQr") String codigoQr,
            @FormParam("emailNominado") String emailNominado,
            @FormParam("nombreNominado") String nombreNominado,
            @FormParam("telefonoNominado") String telefonoNominado) {

        String qrLog = (codigoQr != null && codigoQr.length() > 10) ? codigoQr.substring(0, 10) + "..." : codigoQr;
        log.info("POST /public/venta/nominar - QR: {}, Email Nom: {}", qrLog, emailNominado);

        if (codigoQr == null || codigoQr.isBlank() || emailNominado == null || emailNominado.isBlank()
                || nombreNominado == null || nombreNominado.isBlank()) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Código QR, email y nombre del nominado son obligatorios.");
        }

        try {
            // Llamar al servicio para realizar la nominación y enviar el email
            EntradaAsignadaDTO entradaNominadaDTO = entradaAsignadaService.nominarEntradaPorQr(
                    codigoQr, emailNominado, nombreNominado, telefonoNominado);

            log.info("Entrada (QR: {}) nominada exitosamente a {} ({}) a través del servicio.",
                    qrLog, entradaNominadaDTO.getNombreAsistente(), entradaNominadaDTO.getEmailAsistente());

            // Devolver el DTO de la entrada nominada junto con un mensaje.
            // El cliente puede usar el DTO para mostrar detalles si es necesario.
            Map<String, Object> successResponse = Map.of(
                    "mensaje", "Entrada nominada correctamente a " + entradaNominadaDTO.getEmailAsistente(),
                    "entrada", entradaNominadaDTO
            );
            return Response.ok(successResponse).build();

        } catch (EntradaAsignadaNotFoundException e) {
            log.warn("Nominación fallida para QR {}: {}", qrLog, e.getMessage());
            return crearRespuestaError(Response.Status.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Nominación fallida para QR {}: {}", qrLog, e.getMessage());
            return crearRespuestaError(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) { // Captura cualquier otra excepción del servicio
            log.error("Error interno al procesar la nominación para QR {}: {}", qrLog, e.getMessage(), e);
            return crearRespuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al procesar la nominación.");
        }
        // Ya no se maneja EntityManager ni Transaction directamente aquí.
    }

    // --- Métodos Auxiliares ---
    private Response crearRespuestaError(Response.Status status, String mensaje) {
        return Response.status(status).entity(Map.of("error", mensaje)).build();
    }

    // Los métodos rollbackTransaction, handleException, y closeEntityManager ya no son necesarios aquí
    // si toda la lógica de persistencia está en la capa de servicio.
}
