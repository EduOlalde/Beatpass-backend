package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.EntradaDTO;
import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNoPublicadoException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.EntradaService;
import com.daw2edudiego.beatpasstfg.service.EntradaServiceImpl;
import com.daw2edudiego.beatpasstfg.service.FestivalService;
import com.daw2edudiego.beatpasstfg.service.FestivalServiceImpl;

import jakarta.validation.Valid; // Para validar DTOs si se usa Bean Validation
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
 * Recurso JAX-RS que expone endpoints RESTful para la gestión de Festivales.
 * Define operaciones CRUD básicas, búsqueda y cambio de estado, interactuando
 * con la capa de servicio {@link FestivalService}. La seguridad de los
 * endpoints se basa en la información obtenida del {@link SecurityContext}
 * inyectado (probablemente poblado por un filtro JWT). Produce y consume
 * principalmente {@link MediaType#APPLICATION_JSON}.
 *
 * @see FestivalService
 * @see FestivalDTO
 * @see SecurityContext
 * @author Eduardo Olalde
 */
@Path("/festivales") // Ruta base para todos los endpoints de este recurso
@Produces(MediaType.APPLICATION_JSON) // Por defecto, las respuestas serán JSON
@Consumes(MediaType.APPLICATION_JSON) // Por defecto, las peticiones con cuerpo esperan JSON
public class FestivalResource {

    private static final Logger log = LoggerFactory.getLogger(FestivalResource.class);

    // Inyección manual de dependencias (o usar @Inject con CDI/Spring)
    private final FestivalService festivalService;
    private final EntradaService entradaService;

    // Inyección de contexto JAX-RS
    @Context
    private UriInfo uriInfo; // Para construir URIs (ej: para cabecera Location)
    @Context
    private SecurityContext securityContext; // Para obtener información del usuario autenticado

    /**
     * Constructor que inicializa el servicio de festivales.
     */
    public FestivalResource() {
        this.festivalService = new FestivalServiceImpl();
        this.entradaService = new EntradaServiceImpl();
    }

    /**
     * Endpoint público GET para obtener los tipos de entrada disponibles para
     * un festival específico. Solo devuelve entradas de festivales PUBLICADOS.
     *
     * @param id El ID del festival.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con una lista (posiblemente vacía) de DTOs de tipos de
     * entrada.</li>
     * <li><b>400 Bad Request:</b> si el ID es inválido.</li>
     * <li><b>404 Not Found:</b> si el festival no existe o no está
     * publicado.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @GET
    @Path("/{id}/entradas") // Ruta para obtener entradas de un festival
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerTiposEntradaPublico(@PathParam("id") Integer id) {
        log.info("GET /festivales/{}/entradas (Público) recibido", id);
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"ID de festival inválido.\"}").build();
        }

        try {
            // Llamar al NUEVO método del servicio que no requiere promotor
            List<EntradaDTO> entradas = entradaService.obtenerEntradasPublicasPorFestival(id);

            log.info("Devolviendo {} tipos de entrada para festival público ID {}", entradas.size(), id);
            return Response.ok(entradas).build(); // 200 OK

        } catch (FestivalNotFoundException | FestivalNoPublicadoException e) {
            // Si el festival no existe o no está publicado, devolver 404
            log.warn("No se pueden obtener entradas públicas para festival ID {}: {}", id, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (IllegalArgumentException e) {
            // Si el ID era inválido (aunque ya lo chequeamos)
            log.warn("Argumento inválido al obtener entradas públicas para festival ID {}: {}", id, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            // Otros errores inesperados
            log.error("Error interno al obtener tipos de entrada públicos para festival ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error interno al obtener tipos de entrada.\"}")
                    .build();
        }
    }

    /**
     * Endpoint para crear un nuevo festival. Requiere que el usuario
     * autenticado tenga el rol PROMOTOR. El festival se crea inicialmente en
     * estado BORRADOR.
     *
     * @param festivalDTO DTO con los datos del festival a crear. Se espera que
     * contenga al menos nombre y fechas válidas. La validación con
     * {@code @Valid} depende de la configuración de Bean Validation.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>201 Created:</b> con la cabecera 'Location' apuntando al nuevo
     * recurso y el DTO del festival creado en el cuerpo.</li>
     * <li><b>400 Bad Request:</b> si los datos del DTO son inválidos o
     * faltan.</li>
     * <li><b>401 Unauthorized:</b> si el usuario no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si el usuario autenticado no es PROMOTOR.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @POST
    public Response crearFestival(@Valid FestivalDTO festivalDTO) {
        log.info("POST /festivales recibido");
        Integer idPromotorAutenticado;
        try {
            // Obtener ID y verificar rol PROMOTOR desde el contexto de seguridad
            idPromotorAutenticado = obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario.PROMOTOR);
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse(); // Devolver respuesta 401 o 403 generada por el helper
        }

        try {
            // Validaciones básicas adicionales (complementarias a @Valid)
            if (festivalDTO == null || festivalDTO.getNombre() == null || festivalDTO.getNombre().isBlank()
                    || festivalDTO.getFechaInicio() == null || festivalDTO.getFechaFin() == null
                    || festivalDTO.getFechaFin().isBefore(festivalDTO.getFechaInicio())) {
                log.warn("Intento de crear festival con datos básicos inválidos.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Nombre y fechas válidas (inicio <= fin) son obligatorios.\"}")
                        .build();
            }

            // Llamar al servicio para crear el festival
            FestivalDTO festivalCreado = festivalService.crearFestival(festivalDTO, idPromotorAutenticado);

            // Construir la URI del nuevo recurso creado
            URI location = uriInfo.getAbsolutePathBuilder().path(festivalCreado.getIdFestival().toString()).build();
            log.info("Festival creado con ID: {}, Location: {}", festivalCreado.getIdFestival(), location);

            // Devolver respuesta 201 Created
            return Response.created(location).entity(festivalCreado).build();

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear festival: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        } catch (Exception e) { // Captura otras excepciones del servicio (ej: UsuarioNotFound, PersistenceException)
            log.error("Error interno al crear festival: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al crear el festival.\"}").build();
        }
    }

    /**
     * Endpoint para obtener los detalles de un festival específico por su ID.
     * Accesible públicamente (no requiere autenticación específica en este
     * ejemplo).
     *
     * @param id El ID del festival a obtener.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con el DTO del festival en el cuerpo si se
     * encuentra.</li>
     * <li><b>400 Bad Request:</b> si el ID es inválido.</li>
     * <li><b>404 Not Found:</b> si no se encuentra un festival con ese ID.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @GET
    @Path("/{id}")
    public Response obtenerFestival(@PathParam("id") Integer id) {
        log.info("GET /festivales/{} recibido", id);
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"ID de festival inválido.\"}").build();
        }
        try {
            Optional<FestivalDTO> festivalOpt = festivalService.obtenerFestivalPorId(id);

            return festivalOpt
                    .map(dto -> {
                        log.debug("Festival ID {} encontrado.", id);
                        return Response.ok(dto).build(); // 200 OK con DTO
                    })
                    .orElseGet(() -> {
                        log.warn("Festival ID {} no encontrado.", id);
                        return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"Festival no encontrado.\"}").build(); // 404 Not Found
                    });
        } catch (Exception e) {
            log.error("Error interno al obtener festival ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al obtener el festival.\"}").build();
        }
    }

    /**
     * Endpoint para actualizar un festival existente. Requiere que el usuario
     * autenticado sea el PROMOTOR propietario del festival.
     *
     * @param id El ID del festival a actualizar.
     * @param festivalDTO DTO con los nuevos datos para el festival.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con el DTO del festival actualizado en el cuerpo.</li>
     * <li><b>400 Bad Request:</b> si el ID o los datos del DTO son inválidos o
     * faltan.</li>
     * <li><b>401 Unauthorized:</b> si el usuario no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si el usuario autenticado no es el PROMOTOR
     * dueño.</li>
     * <li><b>404 Not Found:</b> si no se encuentra un festival con ese ID.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @PUT
    @Path("/{id}")
    public Response actualizarFestival(@PathParam("id") Integer id, @Valid FestivalDTO festivalDTO) {
        log.info("PUT /festivales/{} recibido", id);
        Integer idPromotorAutenticado;
        try {
            idPromotorAutenticado = obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario.PROMOTOR);
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        }

        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"ID de festival inválido.\"}").build();
        }

        try {
            // Validaciones básicas del DTO
            if (festivalDTO == null || festivalDTO.getNombre() == null || festivalDTO.getNombre().isBlank()
                    || festivalDTO.getFechaInicio() == null || festivalDTO.getFechaFin() == null
                    || festivalDTO.getFechaFin().isBefore(festivalDTO.getFechaInicio())) {
                log.warn("Intento de actualizar festival ID {} con datos básicos inválidos.", id);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Nombre y fechas válidas (inicio <= fin) son obligatorios para actualizar.\"}")
                        .build();
            }

            // Llamar al servicio para actualizar (el servicio verifica propiedad)
            FestivalDTO festivalActualizado = festivalService.actualizarFestival(id, festivalDTO, idPromotorAutenticado);
            log.info("Festival ID {} actualizado por promotor ID {}.", id, idPromotorAutenticado);
            return Response.ok(festivalActualizado).build(); // 200 OK con DTO actualizado

        } catch (FestivalNotFoundException e) {
            log.warn("Festival ID {} no encontrado para actualizar.", id);
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 404
        } catch (SecurityException e) {
            log.warn("Intento no autorizado de actualizar festival ID {} por usuario {}", id, idPromotorAutenticado);
            return Response.status(Response.Status.FORBIDDEN).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 403
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al actualizar festival ID {}: {}", id, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 400
        } catch (Exception e) {
            log.error("Error interno al actualizar festival ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al actualizar el festival.\"}").build(); // 500
        }
    }

    /**
     * Endpoint para eliminar un festival existente. Requiere que el usuario
     * autenticado sea el PROMOTOR propietario del festival.
     *
     * @param id El ID del festival a eliminar.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>204 No Content:</b> si la eliminación tiene éxito.</li>
     * <li><b>400 Bad Request:</b> si el ID es inválido.</li>
     * <li><b>401 Unauthorized:</b> si el usuario no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si el usuario autenticado no es el PROMOTOR
     * dueño.</li>
     * <li><b>404 Not Found:</b> si no se encuentra un festival con ese ID.</li>
     * <li><b>409 Conflict:</b> si no se puede eliminar debido a dependencias
     * (ej: entradas vendidas).</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @DELETE
    @Path("/{id}")
    public Response eliminarFestival(@PathParam("id") Integer id) {
        log.info("DELETE /festivales/{} recibido", id);
        Integer idPromotorAutenticado;
        try {
            idPromotorAutenticado = obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario.PROMOTOR);
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        }

        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"ID de festival inválido.\"}").build();
        }

        try {
            // Llamar al servicio para eliminar (el servicio verifica propiedad)
            festivalService.eliminarFestival(id, idPromotorAutenticado);
            log.info("Festival ID {} eliminado por promotor ID {}.", id, idPromotorAutenticado);
            return Response.noContent().build(); // 204 No Content

        } catch (FestivalNotFoundException e) {
            log.warn("Festival ID {} no encontrado para eliminar.", id);
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 404
        } catch (SecurityException e) {
            log.warn("Intento no autorizado de eliminar festival ID {} por usuario {}", id, idPromotorAutenticado);
            return Response.status(Response.Status.FORBIDDEN).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 403
        } catch (RuntimeException e) { // Captura errores como violación de FK (mapeado a IllegalStateException o similar por el servicio)
            log.error("Error al eliminar festival ID {}: {}", id, e.getMessage());
            // Devolver 409 Conflict si no se puede borrar por dependencias
            return Response.status(Response.Status.CONFLICT).entity("{\"error\": \"No se pudo eliminar el festival: " + e.getMessage() + "\"}").build();
        } catch (Exception e) { // Captura genérica para otros errores
            log.error("Error interno al eliminar festival ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al eliminar el festival.\"}").build(); // 500
        }
    }

    /**
     * Endpoint público para buscar festivales que están PUBLICADOS y activos
     * dentro de un rango de fechas especificado. Si no se proporcionan fechas,
     * busca desde hoy hasta un año en el futuro.
     *
     * @param fechaDesdeStr Fecha de inicio del rango (formato YYYY-MM-DD).
     * Opcional.
     * @param fechaHastaStr Fecha de fin del rango (formato YYYY-MM-DD).
     * Opcional.
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con una lista (posiblemente vacía) de DTOs de
     * festivales en el cuerpo.</li>
     * <li><b>400 Bad Request:</b> si el formato de fecha es inválido o
     * fechaHasta es anterior a fechaDesde.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @GET
    @Path("/publicados")
    public Response buscarFestivalesPublicados(
            @QueryParam("fechaDesde") String fechaDesdeStr,
            @QueryParam("fechaHasta") String fechaHastaStr) {
        log.info("GET /festivales/publicados recibido con fechas: Desde='{}', Hasta='{}'", fechaDesdeStr, fechaHastaStr);
        LocalDate fechaDesde;
        LocalDate fechaHasta;
        try {
            // Usar fecha actual si no se proporciona fechaDesde
            fechaDesde = (fechaDesdeStr != null && !fechaDesdeStr.isBlank()) ? LocalDate.parse(fechaDesdeStr) : LocalDate.now();
            // Usar fechaDesde + 1 año si no se proporciona fechaHasta
            fechaHasta = (fechaHastaStr != null && !fechaHastaStr.isBlank()) ? LocalDate.parse(fechaHastaStr) : fechaDesde.plusYears(1);

            // Validar que fechaHasta no sea anterior a fechaDesde
            if (fechaHasta.isBefore(fechaDesde)) {
                log.warn("Fechas inválidas en GET /festivales/publicados: fechaHasta ({}) < fechaDesde ({})", fechaHasta, fechaDesde);
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"La fecha 'hasta' no puede ser anterior a la fecha 'desde'.\"}").build();
            }
        } catch (DateTimeParseException e) {
            log.warn("Formato de fecha inválido en GET /festivales/publicados: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Formato de fecha inválido. Use YYYY-MM-DD.\"}").build();
        }

        try {
            // Llamar al servicio para buscar
            List<FestivalDTO> festivales = festivalService.buscarFestivalesPublicados(fechaDesde, fechaHasta);
            log.info("Devolviendo {} festivales publicados entre {} y {}.", festivales.size(), fechaDesde, fechaHasta);
            return Response.ok(festivales).build(); // 200 OK con la lista
        } catch (Exception e) {
            log.error("Error interno buscando festivales publicados: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al buscar festivales.\"}").build();
        }
    }

    /**
     * Endpoint para que un promotor autenticado obtenga la lista de sus propios
     * festivales. Requiere rol PROMOTOR.
     *
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con una lista (posiblemente vacía) de DTOs de sus
     * festivales.</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si no tiene rol PROMOTOR.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @GET
    @Path("/mis-festivales")
    public Response obtenerMisFestivales() {
        log.info("GET /festivales/mis-festivales recibido");
        Integer idPromotorAutenticado;
        try {
            idPromotorAutenticado = obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario.PROMOTOR);
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        }

        try {
            List<FestivalDTO> festivales = festivalService.obtenerFestivalesPorPromotor(idPromotorAutenticado);
            log.info("Devolviendo {} festivales para el promotor ID {}", festivales.size(), idPromotorAutenticado);
            return Response.ok(festivales).build();
        } catch (Exception e) {
            log.error("Error interno obteniendo mis-festivales para promotor ID {}: {}", idPromotorAutenticado, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al obtener sus festivales.\"}").build();
        }
    }

    /**
     * Endpoint para que un promotor cambie el estado de uno de sus propios
     * festivales. **NOTA:** La lógica actual del servicio
     * {@link FestivalService#cambiarEstadoFestival(Integer, EstadoFestival, Integer)}
     * permite al promotor cambiar a cualquier estado, lo cual podría no ser lo
     * deseado. Generalmente, un promotor solo debería poder mover a BORRADOR o
     * solicitar publicación. Se mantiene la llamada al servicio como está, pero
     * se advierte de esta posible inconsistencia. Requiere rol PROMOTOR y ser
     * dueño del festival.
     *
     * @param id El ID del festival a modificar.
     * @param nuevoEstadoStr String representando el nuevo estado deseado (ej:
     * "BORRADOR").
     * @return Respuesta HTTP:
     * <ul>
     * <li><b>200 OK:</b> con el DTO del festival actualizado.</li>
     * <li><b>400 Bad Request:</b> si el ID o el nuevo estado son
     * inválidos.</li>
     * <li><b>401 Unauthorized:</b> si no está autenticado.</li>
     * <li><b>403 Forbidden:</b> si no es PROMOTOR o no es dueño.</li>
     * <li><b>404 Not Found:</b> si el festival no existe.</li>
     * <li><b>409 Conflict:</b> si la transición de estado no es válida.</li>
     * <li><b>500 Internal Server Error:</b> si ocurre un error interno.</li>
     * </ul>
     */
    @PUT
    @Path("/{id}/estado")
    public Response cambiarEstadoFestival(
            @PathParam("id") Integer id,
            @QueryParam("nuevoEstado") String nuevoEstadoStr) {
        log.info("PUT /festivales/{}/estado recibido con nuevoEstado={}", id, nuevoEstadoStr);
        Integer idPromotorAutenticado;
        try {
            idPromotorAutenticado = obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario.PROMOTOR);
        } catch (NotAuthorizedException | ForbiddenException e) {
            return e.getResponse();
        }

        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"ID de festival inválido.\"}").build();
        }

        EstadoFestival nuevoEstado;
        try {
            if (nuevoEstadoStr == null || nuevoEstadoStr.isBlank()) {
                throw new IllegalArgumentException("El parámetro 'nuevoEstado' es obligatorio.");
            }
            // Convertir String a Enum
            nuevoEstado = EstadoFestival.valueOf(nuevoEstadoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Valor inválido para 'nuevoEstado' en PUT /festivales/{}/estado: {}", id, nuevoEstadoStr);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Valor de 'nuevoEstado' inválido. Valores posibles: BORRADOR, PUBLICADO, CANCELADO, FINALIZADO.\"}")
                    .build();
        }

        try {
            // Llamar al servicio (el servicio valida propiedad y lógica de transición si la tuviera)
            // ¡Advertencia! El servicio actual permite al promotor cambiar a cualquier estado.
            FestivalDTO festivalActualizado = festivalService.cambiarEstadoFestival(id, nuevoEstado, idPromotorAutenticado);
            log.info("Estado de festival ID {} cambiado a {} por promotor ID {}.", id, nuevoEstado, idPromotorAutenticado);
            return Response.ok(festivalActualizado).build(); // 200 OK

        } catch (FestivalNotFoundException e) {
            log.warn("Festival ID {} no encontrado para cambiar estado.", id);
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 404
        } catch (SecurityException e) {
            log.warn("Intento no autorizado de cambiar estado a festival ID {} por usuario {}", id, idPromotorAutenticado);
            return Response.status(Response.Status.FORBIDDEN).entity("{\"error\": \"" + e.getMessage() + "\"}").build(); // 403
        } catch (IllegalStateException e) { // Captura errores de lógica de transición del servicio
            log.warn("Transición de estado inválida para festival ID {}: {}", id, e.getMessage());
            return Response.status(Response.Status.CONFLICT).entity("{\"error\": \"Cambio de estado no permitido: " + e.getMessage() + "\"}").build(); // 409 Conflict
        } catch (Exception e) {
            log.error("Error interno al cambiar estado de festival ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"Error interno al cambiar estado del festival.\"}").build(); // 500
        }
    }

    // --- Métodos Auxiliares de Seguridad (Simulados/Placeholder) ---
    /**
     * Obtiene el ID del usuario autenticado desde el SecurityContext inyectado.
     * Asume que el nombre del Principal es el ID del usuario parseable a
     * Integer.
     * <b>¡Requiere implementación real basada en el mecanismo de
     * autenticación!</b>
     *
     * @return El ID del usuario autenticado.
     * @throws NotAuthorizedException Si no hay contexto de seguridad o
     * principal.
     * @throws InternalServerErrorException Si el ID del principal no es un
     * número válido.
     */
    private Integer obtenerIdUsuarioAutenticado() {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            log.warn("Intento de acceso sin SecurityContext o UserPrincipal válidos.");
            throw new NotAuthorizedException("No autenticado.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        try {
            return Integer.parseInt(securityContext.getUserPrincipal().getName());
        } catch (NumberFormatException e) {
            log.error("Error al parsear ID de usuario desde SecurityContext Principal: {}", securityContext.getUserPrincipal().getName(), e);
            throw new InternalServerErrorException("Error al procesar identidad del usuario.");
        }
        // Código simulado eliminado
    }

    /**
     * Verifica si el usuario autenticado tiene el rol especificado usando el
     * SecurityContext inyectado.
     * <b>¡Requiere implementación real basada en el mecanismo de
     * autenticación!</b>
     *
     * @param rol El rol requerido.
     * @return {@code true} si el usuario tiene el rol, {@code false} en caso
     * contrario.
     * @throws NotAuthorizedException Si no hay contexto de seguridad.
     */
    private boolean esRol(RolUsuario rol) {
        if (securityContext == null) {
            log.warn("SecurityContext es null. No se puede verificar el rol.");
            // Devolver false en lugar de lanzar excepción aquí, la verificación se hace en el método combinado
            return false;
            // throw new NotAuthorizedException("No autenticado.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        // Lógica real usando el SecurityContext inyectado
        boolean tieneRol = securityContext.isUserInRole(rol.name());
        log.trace("Verificación de rol {}: {}", rol.name(), tieneRol);
        return tieneRol;
        // Código simulado eliminado
    }

    /**
     * Combina la obtención del ID del usuario autenticado y la verificación de
     * que tiene el rol requerido. Lanza excepciones JAX-RS apropiadas si la
     * verificación falla.
     *
     * @param rolRequerido El rol que debe tener el usuario.
     * @return El ID del usuario autenticado si la verificación es exitosa.
     * @throws NotAuthorizedException Si el usuario no está autenticado.
     * @throws ForbiddenException Si el usuario no tiene el rol requerido.
     * @throws InternalServerErrorException Si hay un error al procesar la
     * identidad.
     */
    private Integer obtenerIdUsuarioAutenticadoYVerificarRol(RolUsuario rolRequerido) {
        Integer userId = obtenerIdUsuarioAutenticado(); // Lanza NotAuthorizedException o InternalServerErrorException si falla
        if (!esRol(rolRequerido)) { // esRol ahora devuelve false si no hay contexto
            log.warn("Usuario ID {} no tiene el rol requerido {}", userId, rolRequerido);
            throw new ForbiddenException("Acceso denegado. Rol requerido: " + rolRequerido);
        }
        log.trace("Usuario ID {} verificado con rol {}", userId, rolRequerido);
        return userId;
    }
}
