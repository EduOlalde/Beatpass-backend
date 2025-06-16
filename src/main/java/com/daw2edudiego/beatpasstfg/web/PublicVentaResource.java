package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraRequestDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraResponseDTO;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.Asistente;
import com.daw2edudiego.beatpasstfg.model.EstadoEntradaAsignada;
import com.daw2edudiego.beatpasstfg.service.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Path("/public/venta")
@Produces(MediaType.APPLICATION_JSON)
public class PublicVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PublicVentaResource.class);

    private final VentaService ventaService;
    private final EntradaAsignadaService entradaAsignadaService;
    private final FestivalService festivalService;

    @Context
    private UriInfo uriInfo;
    @Context
    private HttpServletRequest servletRequest;
    @Context
    private HttpServletResponse servletResponse;

    public PublicVentaResource() {
        this.ventaService = new VentaServiceImpl();
        this.entradaAsignadaService = new EntradaAsignadaServiceImpl();
        this.festivalService = new FestivalServiceImpl();
    }

    @GET
    @Path("/nominar-entrada/{ticketCode}")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarPaginaNominacionPublica(@PathParam("ticketCode") String ticketCode)
            throws ServletException, IOException {
        log.info("GET /public/venta/nominar-entrada/{}", ticketCode);

        String errorMsg = servletRequest.getParameter("error");
        String successMsg = servletRequest.getParameter("successMessage");
        String nominatedEmail = servletRequest.getParameter("nominatedEmail");

        String nombreFestival = "el festival";
        boolean hideForm = false;

        if (successMsg != null) {
            servletRequest.setAttribute("successMessage", successMsg);
            servletRequest.setAttribute("nominatedEmail", nominatedEmail);
            servletRequest.setAttribute("hideForm", true);
            hideForm = true;
        }
        if (errorMsg != null) {
            servletRequest.setAttribute("error", errorMsg);
        }

        if (!hideForm && (ticketCode == null || ticketCode.isBlank())) {
            servletRequest.setAttribute("error", "El código de la entrada no puede estar vacío.");
            log.warn("El código de la entrada no puede estar vacío.");
        } else if (!hideForm) {
            try {
                Optional<EntradaAsignadaDTO> entradaOpt = entradaAsignadaService.obtenerParaNominacionPublicaPorQr(ticketCode);
                if (entradaOpt.isEmpty()) {
                    servletRequest.setAttribute("error", "La entrada con el código proporcionado no fue encontrada o no es válida.");
                } else {
                    EntradaAsignadaDTO entrada = entradaOpt.get();
                    if (entrada.getEstado() != EstadoEntradaAsignada.ACTIVA) {
                        servletRequest.setAttribute("error", "Esta entrada ya ha sido utilizada (" + entrada.getEstado() + ") o no está activa para nominación.");
                    } else if (entrada.getIdAsistente() != null) {
                        servletRequest.setAttribute("error", "Esta entrada ya ha sido nominada a " + (entrada.getEmailAsistente() != null ? entrada.getEmailAsistente() : "un asistente") + ".");
                    }
                    if (entrada.getIdFestival() != null) {
                        Optional<FestivalDTO> festivalOpt = festivalService.obtenerFestivalPorId(entrada.getIdFestival());
                        if (festivalOpt.isPresent()) {
                            nombreFestival = festivalOpt.get().getNombre();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error al validar la entrada para nominación pública (código {}): {}", ticketCode, e.getMessage(), e);
                servletRequest.setAttribute("error", "Ocurrió un error al verificar la entrada.");
            }
        }

        servletRequest.setAttribute("ticketCode", ticketCode);
        servletRequest.setAttribute("nombreFestival", nombreFestival);

        RequestDispatcher dispatcher = servletRequest.getRequestDispatcher("/WEB-INF/jsp/nominacion_publica.jsp");
        dispatcher.forward(servletRequest, servletResponse);
        return Response.ok().build();
    }

    @POST
    @Path("/nominar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response nominarEntrada(
            @FormParam("codigoQr") String codigoQr,
            @FormParam("emailNominado") String emailNominado,
            @FormParam("confirmEmailNominado") String confirmEmailNominado,
            @FormParam("nombreNominado") String nombreNominado,
            @FormParam("telefonoNominado") String telefonoNominado) throws ServletException, IOException {

        String qrLog = (codigoQr != null && codigoQr.length() > 10) ? codigoQr.substring(0, 10) + "..." : codigoQr;
        log.info("POST /public/venta/nominar - QR: {}, Email Nom: {}", qrLog, emailNominado);

        UriBuilder redirectUriBuilder = uriInfo.getBaseUriBuilder()
                .path(PublicVentaResource.class)
                .path(PublicVentaResource.class, "mostrarPaginaNominacionPublica");
        URI redirectUri;

        if (codigoQr == null || codigoQr.isBlank()
                || emailNominado == null || emailNominado.isBlank()
                || confirmEmailNominado == null || confirmEmailNominado.isBlank()
                || nombreNominado == null || nombreNominado.isBlank()) {

            log.warn("Datos de nominación incompletos o inválidos para QR: {}", qrLog);
            redirectUri = redirectUriBuilder.resolveTemplate("ticketCode", codigoQr_valid_for_url(codigoQr))
                    .queryParam("error", "Todos los campos marcados con * son obligatorios (Nombre, Email, Confirmar Email).")
                    .queryParam("nombreNominado", nombreNominado)
                    .queryParam("emailNominado", emailNominado)
                    .queryParam("confirmEmailNominado", confirmEmailNominado)
                    .queryParam("telefonoNominado", telefonoNominado)
                    .build();
            return Response.seeOther(redirectUri).build();
        }

        if (!emailNominado.equals(confirmEmailNominado)) {
            log.warn("Los emails no coinciden para la nominación del QR: {}", qrLog);
            redirectUri = redirectUriBuilder.resolveTemplate("ticketCode", codigoQr_valid_for_url(codigoQr))
                    .queryParam("error", "Los emails introducidos no coinciden.")
                    .queryParam("nombreNominado", nombreNominado)
                    .queryParam("emailNominado", emailNominado)
                    .queryParam("telefonoNominado", telefonoNominado)
                    .build();
            return Response.seeOther(redirectUri).build();
        }

        try {
            EntradaAsignadaDTO entradaNominadaDTO = entradaAsignadaService.nominarEntradaPorQr(
                    codigoQr, emailNominado, nombreNominado, telefonoNominado);

            log.info("Entrada (QR: {}) nominada exitosamente a {} ({})",
                    qrLog, entradaNominadaDTO.getNombreAsistente(), entradaNominadaDTO.getEmailAsistente());

            redirectUri = redirectUriBuilder.resolveTemplate("ticketCode", codigoQr_valid_for_url(codigoQr))
                    .queryParam("successMessage", "¡Entrada nominada con éxito a " + entradaNominadaDTO.getNombreAsistente() + "!")
                    .queryParam("nominatedEmail", entradaNominadaDTO.getEmailAsistente())
                    .build();
            return Response.seeOther(redirectUri).build();

        } catch (EntradaAsignadaNotFoundException e) {
            log.warn("Nominación fallida para QR {}: {}", qrLog, e.getMessage());
            redirectUri = redirectUriBuilder.resolveTemplate("ticketCode", codigoQr_valid_for_url(codigoQr))
                    .queryParam("error", e.getMessage())
                    .build();
            return Response.seeOther(redirectUri).build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Nominación fallida para QR {}: {}", qrLog, e.getMessage());
            redirectUri = redirectUriBuilder.resolveTemplate("ticketCode", codigoQr_valid_for_url(codigoQr))
                    .queryParam("error", e.getMessage())
                    .queryParam("nombreNominado", nombreNominado)
                    .queryParam("emailNominado", emailNominado)
                    .queryParam("telefonoNominado", telefonoNominado)
                    .build();
            return Response.seeOther(redirectUri).build();
        } catch (Exception e) {
            log.error("Error interno al procesar la nominación para QR {}: {}", qrLog, e.getMessage(), e);
            redirectUri = redirectUriBuilder.resolveTemplate("ticketCode", codigoQr_valid_for_url(codigoQr))
                    .queryParam("error", "Error interno al procesar la nominación. Inténtalo más tarde.")
                    .build();
            return Response.seeOther(redirectUri).build();
        }
    }

    /**
     * Helper function to ensure the QR code is valid for a URL, especially if
     * passed as null or blank.
     *
     * @param codigoQr The QR code.
     * @return The QR code, or "invalid" if null/blank.
     */
    private String codigoQr_valid_for_url(String codigoQr) {
        return (codigoQr == null || codigoQr.isBlank()) ? "invalid" : codigoQr;
    }

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

    @POST
    @Path("/confirmar-compra")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response confirmarCompraConPago(
            @FormParam("idEntrada") Integer idEntrada,
            @FormParam("cantidad") Integer cantidad,
            @FormParam("emailComprador") String emailComprador,
            @FormParam("nombreComprador") String nombreComprador,
            @FormParam("telefonoComprador") String telefonoComprador,
            @FormParam("paymentIntentId") String paymentIntentId) {

        log.info("POST /public/venta/confirmar-compra - Entrada: {}, Cant: {}, Email Comprador: {}, PI: {}",
                idEntrada, cantidad, emailComprador, paymentIntentId);

        if (idEntrada == null || cantidad == null || cantidad <= 0 || emailComprador == null || emailComprador.isBlank()
                || nombreComprador == null || nombreComprador.isBlank() || paymentIntentId == null || paymentIntentId.isBlank()) {
            return crearRespuestaError(Response.Status.BAD_REQUEST, "Faltan datos obligatorios (entrada, cantidad>0, email, nombre, paymentIntentId).");
        }

        try {
            CompraDTO compraConfirmada = ventaService.confirmarVentaConPago(
                    emailComprador, nombreComprador, telefonoComprador, idEntrada, cantidad, paymentIntentId);

            log.info("Compra confirmada. Compra ID: {}, PI: {}", compraConfirmada.getIdCompra(), paymentIntentId);
            return Response.ok(compraConfirmada).build();

        } catch (EntradaNotFoundException | FestivalNotFoundException e) {
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

    private Response crearRespuestaError(Response.Status status, String mensaje) {
        return Response.status(status).entity(Map.of("error", mensaje)).build();
    }
}
