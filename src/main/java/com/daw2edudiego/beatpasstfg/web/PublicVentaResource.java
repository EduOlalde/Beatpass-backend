package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.CompraDTO;
import com.daw2edudiego.beatpasstfg.dto.ConfirmarCompraRequestDTO;
import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraRequestDTO;
import com.daw2edudiego.beatpasstfg.dto.IniciarCompraResponseDTO;
import com.daw2edudiego.beatpasstfg.dto.NominacionRequestDTO;
import com.daw2edudiego.beatpasstfg.service.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/public/venta")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicVentaResource {

    private static final Logger log = LoggerFactory.getLogger(PublicVentaResource.class);

    private final VentaService ventaService;
    private final EntradaService entradaService;

    @Context
    private UriInfo uriInfo;

    public PublicVentaResource() {
        this.ventaService = new VentaServiceImpl();
        this.entradaService = new EntradaServiceImpl();
    }

    /**
     * Endpoint POST para nominar una entrada públicamente. Recibe DTO JSON. No
     * requiere autenticación, ya que es público.
     *
     * @param codigoQr Código QR de la entrada.
     * @param nominacionRequest DTO con email, nombre y teléfono del asistente,
     * y confirmación de email.
     * @return 200 OK con la EntradaDTO nominada.
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

    @POST
    @Path("/confirmar-compra")
    public Response confirmarCompraConPago(
            @Valid ConfirmarCompraRequestDTO confirmarCompraRequest) {

        log.info("POST /public/venta/confirmar-compra - Entrada: {}, Cant: {}, Email Comprador: {}, PI: {}",
                confirmarCompraRequest.getIdEntrada(), confirmarCompraRequest.getCantidad(), confirmarCompraRequest.getEmailComprador(), confirmarCompraRequest.getPaymentIntentId());

        if (confirmarCompraRequest.getIdEntrada() == null || confirmarCompraRequest.getCantidad() == null || confirmarCompraRequest.getCantidad() <= 0
                || confirmarCompraRequest.getEmailComprador() == null || confirmarCompraRequest.getEmailComprador().isBlank()
                || confirmarCompraRequest.getNombreComprador() == null || confirmarCompraRequest.getNombreComprador().isBlank()
                || confirmarCompraRequest.getPaymentIntentId() == null || confirmarCompraRequest.getPaymentIntentId().isBlank()) {
            throw new BadRequestException("Faltan datos obligatorios (entrada, cantidad>0, email, nombre, paymentIntentId).");
        }

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
}
