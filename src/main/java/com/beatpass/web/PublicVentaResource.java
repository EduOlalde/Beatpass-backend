package com.beatpass.web;

import com.beatpass.dto.*;
import com.beatpass.service.EntradaService;
import com.beatpass.service.VentaService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Recurso JAX-RS para los endpoints de venta públicos. No requiere
 * autenticación.
 */
@Path("/public/venta")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class PublicVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PublicVentaResource.class);

    @Inject
    private VentaService ventaService;
    @Inject
    private EntradaService entradaService;

    @Context
    private UriInfo uriInfo;

    public PublicVentaResource() {
    }

    /**
     * Endpoint público para nominar una entrada a un asistente usando su código
     * QR.
     *
     * @param codigoQr El código QR único de la entrada.
     * @param nominacionRequest DTO con los datos del asistente a nominar.
     * @return Una respuesta HTTP 200 OK con los datos de la entrada ya
     * nominada.
     */
    @POST
    @Path("/nominar/{codigoQr}")
    public Response nominarEntrada(
            @PathParam("codigoQr") String codigoQr,
            @Valid NominacionRequestDTO nominacionRequest) {

        String qrLog = (codigoQr != null && codigoQr.length() > 10) ? codigoQr.substring(0, 10) + "..." : codigoQr;
        log.info("POST /public/venta/nominar/{} (JSON) - Email Nom: {}", qrLog, nominacionRequest.getEmailAsistente());

        if (!nominacionRequest.getEmailAsistente().equals(nominacionRequest.getConfirmEmailNominado())) {
            throw new BadRequestException("Los emails introducidos no coinciden.");
        }

        EntradaDTO entradaNominadaDTO = entradaService.nominarEntradaPorQr(
                codigoQr,
                nominacionRequest.getEmailAsistente(),
                nominacionRequest.getNombreAsistente(),
                nominacionRequest.getTelefonoAsistente());

        log.info("Entrada (QR: {}) nominada exitosamente a {} ({})",
                qrLog, entradaNominadaDTO.getNombreAsistente(), entradaNominadaDTO.getEmailAsistente());

        return Response.ok(entradaNominadaDTO).build();
    }

    /**
     * Inicia el proceso de pago con Stripe. Valida la entrada y la cantidad,
     * calcula el total y crea un PaymentIntent, devolviendo su client_secret.
     *
     * @param requestDTO DTO con el ID del tipo de entrada y la cantidad.
     * @return Una respuesta HTTP 200 OK con el client_secret para el frontend.
     */
    @POST
    @Path("/iniciar-pago")
    public Response iniciarPago(@Valid IniciarCompraRequestDTO requestDTO) {
        log.info("POST /public/venta/iniciar-pago - Entrada ID: {}, Cantidad: {}",
                requestDTO != null ? requestDTO.getIdEntrada() : "null",
                requestDTO != null ? requestDTO.getCantidad() : "null");

        if (requestDTO == null || requestDTO.getIdEntrada() == null || requestDTO.getCantidad() == null || requestDTO.getCantidad() <= 0) {
            throw new BadRequestException("Datos inválidos (idEntrada y cantidad > 0 son obligatorios).");
        }

        IniciarCompraResponseDTO responseDTO = ventaService.iniciarProcesoPago(
                requestDTO.getIdEntrada(), requestDTO.getCantidad());
        log.info("Proceso de pago iniciado. Devolviendo client_secret.");
        return Response.ok(responseDTO).build();
    }

    /**
     * Confirma una compra después de que el pago haya sido procesado con éxito
     * por Stripe en el frontend.
     *
     * @param confirmarCompraRequest DTO con los detalles del comprador, la
     * compra y el ID del PaymentIntent.
     * @return Una respuesta HTTP 200 OK con los detalles de la compra
     * confirmada.
     */
    @POST
    @Path("/confirmar-compra")
    public Response confirmarCompraConPago(
            @Valid ConfirmarCompraRequestDTO confirmarCompraRequest) {

        log.info("POST /public/venta/confirmar-compra - Entrada: {}, Cant: {}, Email Comprador: {}, PI: {}",
                confirmarCompraRequest.getIdEntrada(), confirmarCompraRequest.getCantidad(), confirmarCompraRequest.getEmailComprador(), confirmarCompraRequest.getPaymentIntentId());

        CompraDTO compraConfirmada = ventaService.confirmarVentaConPago(
                confirmarCompraRequest.getEmailComprador(),
                confirmarCompraRequest.getNombreComprador(),
                confirmarCompraRequest.getTelefonoComprador(),
                confirmarCompraRequest.getIdEntrada(),
                confirmarCompraRequest.getCantidad(),
                confirmarCompraRequest.getPaymentIntentId());

        log.info("Compra confirmada. Compra ID: {}, PI: {}", compraConfirmada.getIdCompra(), confirmarCompraRequest.getPaymentIntentId());
        return Response.ok(compraConfirmada).build();
    }

    /**
     * Endpoint público para obtener los detalles de una entrada usando su
     * código QR, usado principalmente para verificarla antes del proceso de
     * nominación.
     *
     * @param codigoQr El código QR de la entrada a consultar.
     * @return Una respuesta HTTP 200 OK con los datos de la entrada.
     * @throws NotFoundException si la entrada no es válida para la nominación
     * pública.
     */
    @GET
    @Path("/entrada-qr/{codigoQr}")
    public Response obtenerEntradaPorQr(@PathParam("codigoQr") String codigoQr) {
        String qrLog = (codigoQr != null && codigoQr.length() > 10) ? codigoQr.substring(0, 10) + "..." : codigoQr;
        log.info("GET /public/venta/entrada-qr/{} recibido", qrLog);

        if (codigoQr == null || codigoQr.isBlank()) {
            throw new BadRequestException("Código QR de entrada obligatorio.");
        }

        Optional<EntradaDTO> entradaOpt = entradaService.obtenerParaNominacionPublicaPorQr(codigoQr);

        return entradaOpt
                .map(dto -> Response.ok(dto).build())
                .orElseThrow(() -> {
                    log.warn("Entrada no encontrada con QR: {}", qrLog);
                    return new NotFoundException("Entrada no encontrada con el código QR proporcionado o no válida para nominación pública.");
                });
    }
}
