package com.beatpass.web;

import com.beatpass.dto.FestivalDTO;
import com.beatpass.dto.TipoEntradaDTO;
import com.beatpass.model.EstadoFestival;
import com.beatpass.service.FestivalService;
import com.beatpass.service.TipoEntradaService;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Recurso JAX-RS para la gestión de Festivales (/api/festivales). Expone
 * endpoints públicos y protegidos para CRUD, búsqueda y cambio de estado.
 */
@Path("/festivales")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FestivalResource {

    private static final Logger log = LoggerFactory.getLogger(FestivalResource.class);

    private final FestivalService festivalService;
    private final TipoEntradaService tipoEntradaService;

    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;

    @Inject
    public FestivalResource(FestivalService festivalService, TipoEntradaService tipoEntradaService) {
        this.festivalService = festivalService;
        this.tipoEntradaService = tipoEntradaService;
    }

    @GET
    @Path("/{id}/tipos-entrada")
    @PermitAll
    public Response obtenerTiposEntradaPublico(@PathParam("id") Integer id) {
        log.info("GET /festivales/{}/tipos-entrada (Público)", id);
        if (id == null) {
            throw new BadRequestException("ID de festival inválido.");
        }
        List<TipoEntradaDTO> tiposEntrada = tipoEntradaService.obtenerTiposEntradaPublicasPorFestival(id);
        log.info("Devolviendo {} tipos de entrada para festival público ID {}", tiposEntrada.size(), id);
        return Response.ok(tiposEntrada).build();
    }

    @POST
    @RolesAllowed("PROMOTOR")
    public Response crearFestival(@Valid FestivalDTO festivalDTO) {
        log.info("POST /festivales");
        Integer idPromotorAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (festivalDTO == null || festivalDTO.getNombre() == null || festivalDTO.getNombre().isBlank()
                || festivalDTO.getFechaInicio() == null || festivalDTO.getFechaFin() == null
                || festivalDTO.getFechaFin().isBefore(festivalDTO.getFechaInicio())) {
            throw new BadRequestException("Nombre y fechas válidas (inicio <= fin) son obligatorios.");
        }
        FestivalDTO festivalCreado = festivalService.crearFestival(festivalDTO, idPromotorAutenticado);
        URI location = uriInfo.getAbsolutePathBuilder().path(festivalCreado.getIdFestival().toString()).build();
        log.info("Festival creado con ID: {}, Location: {}", festivalCreado.getIdFestival(), location);
        return Response.created(location).entity(festivalCreado).build();
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public Response obtenerFestival(@PathParam("id") Integer id) {
        log.info("GET /festivales/{}", id);
        if (id == null) {
            throw new BadRequestException("ID de festival inválido.");
        }
        Optional<FestivalDTO> festivalOpt = festivalService.obtenerFestivalPorId(id);
        return festivalOpt
                .map(dto -> Response.ok(dto).build())
                .orElseThrow(() -> new NotFoundException("Festival no encontrado."));
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "PROMOTOR"})
    public Response actualizarFestival(@PathParam("id") Integer id, @Valid FestivalDTO festivalDTO) {
        log.info("PUT /festivales/{}", id);
        Integer idUsuarioActualizador = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (id == null) {
            throw new BadRequestException("ID de festival inválido.");
        }
        if (festivalDTO == null || festivalDTO.getNombre() == null || festivalDTO.getNombre().isBlank()
                || festivalDTO.getFechaInicio() == null || festivalDTO.getFechaFin() == null
                || festivalDTO.getFechaFin().isBefore(festivalDTO.getFechaInicio())) {
            throw new BadRequestException("Nombre y fechas válidas (inicio <= fin) son obligatorios.");
        }
        FestivalDTO festivalActualizado = festivalService.actualizarFestival(id, festivalDTO, idUsuarioActualizador);
        log.info("Festival ID {} actualizado por usuario ID {}.", id, idUsuarioActualizador);
        return Response.ok(festivalActualizado).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "PROMOTOR"})
    public Response eliminarFestival(@PathParam("id") Integer id) {
        log.info("DELETE /festivales/{}", id);
        Integer idUsuarioAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (id == null) {
            throw new BadRequestException("ID de festival inválido.");
        }
        festivalService.eliminarFestival(id, idUsuarioAutenticado);
        log.info("Festival ID {} eliminado por usuario ID {}.", id, idUsuarioAutenticado);
        return Response.noContent().build();
    }

    @GET
    @Path("/publicados")
    @PermitAll
    public Response buscarFestivalesPublicados(
            @QueryParam("fechaDesde") String fechaDesdeStr,
            @QueryParam("fechaHasta") String fechaHastaStr) {
        log.info("GET /festivales/publicados. Desde='{}', Hasta='{}'", fechaDesdeStr, fechaHastaStr);
        LocalDate fechaDesde;
        LocalDate fechaHasta;
        try {
            fechaDesde = (fechaDesdeStr != null && !fechaDesdeStr.isBlank()) ? LocalDate.parse(fechaDesdeStr) : LocalDate.now();
            fechaHasta = (fechaHastaStr != null && !fechaHastaStr.isBlank()) ? LocalDate.parse(fechaHastaStr) : fechaDesde.plusYears(1);
            if (fechaHasta.isBefore(fechaDesde)) {
                throw new BadRequestException("Fecha 'hasta' no puede ser anterior a 'desde'.");
            }
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Formato fecha inválido (YYYY-MM-DD).");
        }

        List<FestivalDTO> festivales = festivalService.buscarFestivalesPublicados(fechaDesde, fechaHasta);
        log.info("Devolviendo {} festivales publicados entre {} y {}.", festivales.size(), fechaDesde, fechaHasta);
        return Response.ok(festivales).build();
    }

    @GET
    @Path("/mis-festivales")
    @RolesAllowed("PROMOTOR")
    public Response obtenerMisFestivales() {
        log.info("GET /festivales/mis-festivales");
        Integer idPromotorAutenticado = Integer.parseInt(securityContext.getUserPrincipal().getName());

        List<FestivalDTO> festivales = festivalService.obtenerFestivalesPorPromotor(idPromotorAutenticado);
        log.info("Devolviendo {} festivales para promotor ID {}", festivales.size(), idPromotorAutenticado);
        return Response.ok(festivales).build();
    }

    @PUT
    @Path("/{id}/estado")
    @RolesAllowed({"ADMIN", "PROMOTOR"})
    public Response cambiarEstadoFestival(
            @PathParam("id") Integer id,
            @QueryParam("nuevoEstado") String nuevoEstadoStr) {
        log.warn("PUT /festivales/{}/estado por promotor (¡Permite cualquier estado!). Nuevo: {}", id, nuevoEstadoStr);
        Integer idUsuarioActor = Integer.parseInt(securityContext.getUserPrincipal().getName());

        if (id == null) {
            throw new BadRequestException("ID festival inválido.");
        }
        EstadoFestival nuevoEstado;
        try {
            if (nuevoEstadoStr == null || nuevoEstadoStr.isBlank()) {
                throw new IllegalArgumentException("Parámetro 'nuevoEstado' obligatorio.");
            }
            nuevoEstado = EstadoFestival.valueOf(nuevoEstadoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Valor 'nuevoEstado' inválido. Posibles: BORRADOR, PUBLICADO, CANCELADO, FINALIZADO.");
        }

        FestivalDTO festivalActualizado = festivalService.cambiarEstadoFestival(id, nuevoEstado, idUsuarioActor);
        log.info("Estado festival ID {} cambiado a {} por usuario ID {}.", id, nuevoEstado, idUsuarioActor);
        return Response.ok(festivalActualizado).build();
    }
}
