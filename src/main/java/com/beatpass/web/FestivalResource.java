package com.beatpass.web;

import com.beatpass.dto.TipoEntradaDTO;
import com.beatpass.dto.FestivalDTO;
import com.beatpass.model.EstadoFestival;
import com.beatpass.model.RolUsuario;
import com.beatpass.service.TipoEntradaServiceImpl;
import com.beatpass.service.FestivalService;
import com.beatpass.service.FestivalServiceImpl;

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
import com.beatpass.service.TipoEntradaService;

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

    public FestivalResource() {
        this.festivalService = new FestivalServiceImpl();
        this.tipoEntradaService = new TipoEntradaServiceImpl();
    }

    /**
     * Endpoint público GET para obtener tipos de entrada de un festival (solo
     * si está PUBLICADO).
     *
     * @param id ID del festival.
     * @return 200 OK con lista de TipoEntradaDTO, 400 Bad Request, 404 Not
     * Found, 500 Error.
     */
    @GET
    @Path("/{id}/tipos-entrada")
    public Response obtenerTiposEntradaPublico(@PathParam("id") Integer id) {
        log.info("GET /festivales/{}/tipos-entrada (Público)", id);
        if (id == null) {
            throw new BadRequestException("ID de festival inválido.");
        }
        List<TipoEntradaDTO> tiposEntrada = tipoEntradaService.obtenerTiposEntradaPublicasPorFestival(id);
        log.info("Devolviendo {} tipos de entrada para festival público ID {}", tiposEntrada.size(), id);
        return Response.ok(tiposEntrada).build();
    }

    /**
     * Crea un nuevo festival. Requiere rol PROMOTOR. Estado inicial BORRADOR.
     *
     * @param festivalDTO DTO con datos del festival.
     * @return 201 Created con DTO creado, 400 Bad Request, 401/403 Error, 500
     * Error.
     */
    @POST
    public Response crearFestival(@Valid FestivalDTO festivalDTO) {
        log.info("POST /festivales");
        Integer idPromotorAutenticado = obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario.PROMOTOR);

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

    /**
     * Obtiene detalles de un festival por ID. Acceso público.
     *
     * @param id ID del festival.
     * @return 200 OK con FestivalDTO, 400 Bad Request, 404 Not Found, 500
     * Error.
     */
    @GET
    @Path("/{id}")
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

    /**
     * Actualiza un festival existente. Requiere rol PROMOTOR propietario.
     *
     * @param id ID del festival a actualizar.
     * @param festivalDTO DTO con los nuevos datos.
     * @return 200 OK con DTO actualizado, 400/401/403/404 Error, 500 Error.
     */
    @PUT
    @Path("/{id}")
    public Response actualizarFestival(@PathParam("id") Integer id, @Valid FestivalDTO festivalDTO) {
        log.info("PUT /festivales/{}", id);
        Integer idPromotorAutenticado = obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario.PROMOTOR);

        if (id == null) {
            throw new BadRequestException("ID de festival inválido.");
        }
        // Validaciones previas al servicio
        if (festivalDTO == null || festivalDTO.getNombre() == null || festivalDTO.getNombre().isBlank()
                || festivalDTO.getFechaInicio() == null || festivalDTO.getFechaFin() == null
                || festivalDTO.getFechaFin().isBefore(festivalDTO.getFechaInicio())) {
            throw new BadRequestException("Nombre y fechas válidas (inicio <= fin) son obligatorios.");
        }
        FestivalDTO festivalActualizado = festivalService.actualizarFestival(id, festivalDTO, idPromotorAutenticado);
        log.info("Festival ID {} actualizado por promotor ID {}.", id, idPromotorAutenticado);
        return Response.ok(festivalActualizado).build();
    }

    /**
     * Elimina un festival existente. Requiere rol PROMOTOR propietario.
     *
     * @param id ID del festival a eliminar.
     * @return 204 No Content, 400/401/403/404 Error, 409 Conflict (FKs), 500
     * Error.
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarFestival(@PathParam("id") Integer id) {
        log.info("DELETE /festivales/{}", id);
        // Las excepciones de autenticación/autorización ya se propagan.
        // Se obtiene el ID del usuario autenticado, sin forzar un rol específico aquí.
        // La validación de si es ADMIN o PROMOTOR dueño se delega al servicio.
        Integer idUsuarioAutenticado = obtenerIdUsuarioAutenticado(); // <-- CAMBIO: Obtener ID sin verificar rol específico

        if (id == null) {
            throw new BadRequestException("ID de festival inválido.");
        }
        // Llamada al servicio, que ya contiene la lógica de autorización (ADMIN o PROMOTOR dueño)
        festivalService.eliminarFestival(id, idUsuarioAutenticado); // <-- Se pasa el ID del usuario autenticado
        log.info("Festival ID {} eliminado por usuario ID {}.", id, idUsuarioAutenticado); // Log actualizado
        return Response.noContent().build();
    }

    /**
     * Busca festivales PUBLICADOS dentro de un rango de fechas. Acceso público.
     *
     * @param fechaDesdeStr Fecha inicio (YYYY-MM-DD, opcional, default hoy).
     * @param fechaHastaStr Fecha fin (YYYY-MM-DD, opcional, default hoy + 1
     * año).
     * @return 200 OK con lista de FestivalDTO, 400 Bad Request (fechas), 500
     * Error.
     */
    @GET
    @Path("/publicados")
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

    /**
     * Obtiene la lista de festivales propios del promotor autenticado. Requiere
     * rol PROMOTOR.
     *
     * @return 200 OK con lista de FestivalDTO, 401/403 Error, 500 Error.
     */
    @GET
    @Path("/mis-festivales")
    public Response obtenerMisFestivales() {
        log.info("GET /festivales/mis-festivales");
        Integer idPromotorAutenticado = obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario.PROMOTOR);

        List<FestivalDTO> festivales = festivalService.obtenerFestivalesPorPromotor(idPromotorAutenticado);
        log.info("Devolviendo {} festivales para promotor ID {}", festivales.size(), idPromotorAutenticado);
        return Response.ok(festivales).build();
    }

    /**
     * Cambia el estado de un festival propio. Requiere rol PROMOTOR y ser
     * dueño. ¡ADVERTENCIA! La lógica actual del servicio permite al promotor
     * cambiar a cualquier estado.
     *
     * @param id ID del festival.
     * @param nuevoEstadoStr Nuevo estado (String).
     * @return 200 OK con DTO actualizado, 400/401/403/404 Error, 409 Conflict
     * (transición), 500 Error.
     */
    @PUT
    @Path("/{id}/estado")
    public Response cambiarEstadoFestival(
            @PathParam("id") Integer id,
            @QueryParam("nuevoEstado") String nuevoEstadoStr) {
        log.warn("PUT /festivales/{}/estado por promotor (¡Permite cualquier estado!). Nuevo: {}", id, nuevoEstadoStr);
        Integer idPromotorAutenticado = obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario.PROMOTOR);

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
            throw new BadRequestException("Valor 'nuevoEstado' inválido. Posibles: BORRADOR, PUBLICADO, CANCELADO, FINALIZADO."); // Incluir todos los posibles estados.
        }

        FestivalDTO festivalActualizado = festivalService.cambiarEstadoFestival(id, nuevoEstado, idPromotorAutenticado);
        log.info("Estado festival ID {} cambiado a {} por promotor ID {}.", id, nuevoEstado, idPromotorAutenticado);
        return Response.ok(festivalActualizado).build();
    }

    // --- Métodos Auxiliares de Seguridad (Simulados/Placeholder) ---
    /**
     * Obtiene ID de usuario autenticado desde SecurityContext. Lanza
     * excepciones JAX-RS.
     */
    private Integer obtenerIdUsuarioAutenticado() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw new NotAuthorizedException("No autenticado.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        try {
            return Integer.parseInt(securityContext.getUserPrincipal().getName());
        } catch (NumberFormatException e) {
            throw new InternalServerErrorException("Error procesando identidad del usuario.");
        }
    }

    /**
     * Verifica si el usuario autenticado tiene el rol especificado.
     */
    private boolean esRol(RolUsuario rol) {
        if (securityContext == null) {
            return false;
        }
        return securityContext.isUserInRole(rol.name());
    }

    /**
     * Obtiene ID y verifica rol. Lanza excepciones JAX-RS si falla.
     */
    private Integer obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario rolRequerido) {
        Integer userId = obtenerIdUsuarioAutenticado();
        if (!esRol(rolRequerido)) {
            log.warn("Usuario ID {} no tiene el rol requerido {}", userId, rolRequerido);
            throw new ForbiddenException("Acceso denegado. Rol requerido: " + rolRequerido);
        }
        log.trace("Usuario ID {} verificado con rol {}", userId, rolRequerido);
        return userId;
    }
}
