/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario; // Necesario para comprobación de rol
import com.daw2edudiego.beatpasstfg.service.FestivalService;
import com.daw2edudiego.beatpasstfg.service.FestivalServiceImpl;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest; // Necesario para forward/attributes
import jakarta.servlet.http.HttpServletResponse; // Necesario para forward
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Recurso JAX-RS para manejar las peticiones web del panel de Promotor.
 * Reemplaza al PromotorServlet y utiliza SecurityContext. Interactúa con JSPs
 * mediante forward y redirect.
 */
@Path("/promotor/festivales") // Ruta base para las acciones del promotor sobre festivales
public class PromotorResource {

    private static final Logger log = LoggerFactory.getLogger(PromotorResource.class);

    // Instanciación manual (o inyección si usaras CDI)
    private final FestivalService festivalService;

    // Inyección de contextos JAX-RS y Servlet
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext securityContext;
    @Context
    private HttpServletRequest request; // Para setAttribute y forward
    @Context
    private HttpServletResponse response; // Para forward

    public PromotorResource() {
        this.festivalService = new FestivalServiceImpl();
    }

    // --- Métodos GET para mostrar JSPs ---
    /**
     * Muestra la lista de festivales del promotor autenticado. GET
     * /api/promotor/festivales ó /api/promotor/festivales/listar
     */
    @GET
    @Produces(MediaType.TEXT_HTML) // Indica que la respuesta final será HTML (generado por JSP)
    public Response listarFestivales() throws ServletException, IOException {
        log.debug("GET /listar recibido");
        Integer idPromotor = obtenerIdUsuarioAutenticadoYValidarRol(RolUsuario.PROMOTOR);
        // Si la validación falla, se lanza una excepción que debería ser manejada
        // por un ExceptionMapper o devolverá un error interno si no se captura aquí.
        // Por simplicidad, asumimos que obtenerIdUsuarioAutenticadoYValidarRol lanza WebApplicationException si falla.

        log.debug("Listando festivales para Promotor ID: {}", idPromotor);
        List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);
        request.setAttribute("festivales", listaFestivales); // Poner datos para la JSP
        request.setAttribute("idPromotorAutenticado", idPromotor); // Pasar ID por si la JSP lo necesita

        // Forward a la JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/promotor/mis-festivales.jsp");
        dispatcher.forward(request, response);

        // JAX-RS requiere devolver una Response, pero el forward ya ha manejado la respuesta.
        // Devolver null o una Response vacía puede ser apropiado aquí, aunque puede depender de la implementación de Jersey/Tomcat.
        // Devolver OK puede ser más seguro en algunos casos aunque el cuerpo ya se envió.
        return Response.ok().build(); // Indica que el manejo fue exitoso a nivel JAX-RS
    }

    /**
     * Muestra el formulario para crear un nuevo festival. GET
     * /api/promotor/festivales/crear
     */
    @GET
    @Path("/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrear() throws ServletException, IOException {
        log.debug("GET /crear recibido");
        Integer idPromotor = obtenerIdUsuarioAutenticadoYValidarRol(RolUsuario.PROMOTOR);

        log.debug("Mostrando formulario de creación para Promotor ID: {}", idPromotor);
        FestivalDTO festival = new FestivalDTO(); // DTO vacío
        request.setAttribute("festival", festival);
        request.setAttribute("idPromotorAutenticado", idPromotor);
        request.setAttribute("estadosPosibles", EstadoFestival.values()); // Aunque no se use al crear, por consistencia del form

        RequestDispatcher dispatcher = request.getRequestDispatcher("/promotor/festival-detalle.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * Muestra el formulario para ver/editar un festival existente. GET
     * /api/promotor/festivales/ver/{id}
     */
    @GET
    @Path("/ver/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditar(@PathParam("id") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /ver/{} recibido", idFestival);
        Integer idPromotor = obtenerIdUsuarioAutenticadoYValidarRol(RolUsuario.PROMOTOR);

        try {
            log.debug("Buscando festival con ID: {}", idFestival);
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .orElseThrow(() -> {
                        log.warn("Festival no encontrado para editar con ID: {}", idFestival);
                        return new WebApplicationException("Festival no encontrado", Response.Status.NOT_FOUND);
                    });

            // Verificar permiso
            if (!festival.getIdPromotor().equals(idPromotor)) {
                log.warn("Intento no autorizado de ver/editar festival ID {} por promotor ID {}", idFestival, idPromotor);
                throw new WebApplicationException("No tiene permiso para acceder a este festival", Response.Status.FORBIDDEN);
            }

            log.debug("Mostrando formulario para editar festival ID: {}", idFestival);
            request.setAttribute("festival", festival);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            request.setAttribute("estadosPosibles", EstadoFestival.values());

            RequestDispatcher dispatcher = request.getRequestDispatcher("/promotor/festival-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (WebApplicationException wae) {
            throw wae; // Relanzar excepciones JAX-RS
        } catch (Exception e) {
            log.error("Error al mostrar formulario de edición para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new ServletException("Error al cargar datos del festival", e);
        }
    }

    // --- Métodos POST para procesar formularios ---
    /**
     * Procesa el envío del formulario para guardar (crear o actualizar) un
     * festival. POST /api/promotor/festivales/guardar
     */
    @POST
    @Path("/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // Recibe datos de formulario
    public Response guardarFestival(
            @FormParam("idFestival") String idStr, // JAX-RS puede inyectar parámetros de formulario
            @FormParam("nombre") String nombre,
            @FormParam("descripcion") String descripcion,
            @FormParam("fechaInicio") String fechaInicioStr,
            @FormParam("fechaFin") String fechaFinStr,
            @FormParam("ubicacion") String ubicacion,
            @FormParam("aforo") String aforoStr,
            @FormParam("imagenUrl") String imagenUrl,
            @FormParam("estado") String estadoStr // Puede ser null si es creación
    // @FormParam("action") String action // No necesitamos 'action' aquí
    ) throws ServletException, IOException {

        log.info("POST /guardar recibido");
        Integer idPromotor = obtenerIdUsuarioAutenticadoYValidarRol(RolUsuario.PROMOTOR);

        FestivalDTO dto = new FestivalDTO();
        boolean esNuevo = (idStr == null || idStr.isEmpty() || "0".equals(idStr));
        Integer idFestival = null;

        // --- Validación y Población del DTO ---
        // (Similar a la lógica del Servlet, pero usando @FormParam)
        // Aquí iría la validación robusta... si falla, se debería
        // guardar el error y los datos en request y hacer forward de vuelta al JSP.
        // Por simplicidad, asumimos validación básica y continuamos.
        if (!esNuevo) {
            try {
                idFestival = Integer.parseInt(idStr);
                dto.setIdFestival(idFestival);
            } catch (NumberFormatException e) {
                throw new WebApplicationException("ID inválido", Response.Status.BAD_REQUEST);
            }
        }
        if (nombre == null || nombre.isBlank()) {
            throw new WebApplicationException("Nombre obligatorio", Response.Status.BAD_REQUEST);
        }
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setUbicacion(ubicacion);
        dto.setImagenUrl(imagenUrl);
        try {
            dto.setFechaInicio(LocalDate.parse(fechaInicioStr));
            dto.setFechaFin(LocalDate.parse(fechaFinStr));
            if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
                throw new IllegalArgumentException("Fechas incoherentes");
            }
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new WebApplicationException("Fechas inválidas: " + e.getMessage(), Response.Status.BAD_REQUEST);
        }
        try {
            if (aforoStr != null && !aforoStr.isBlank()) {
                dto.setAforo(Integer.parseInt(aforoStr));
            }
        } catch (NumberFormatException e) {
            throw new WebApplicationException("Aforo inválido", Response.Status.BAD_REQUEST);
        }
        try {
            if (!esNuevo && estadoStr != null && !estadoStr.isBlank()) {
                dto.setEstado(EstadoFestival.valueOf(estadoStr.toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Estado inválido", Response.Status.BAD_REQUEST);
        }
        // --- Fin Validación ---

        try {
            String mensajeExito;
            if (esNuevo) {
                log.info("Llamando a crearFestival para promotor ID: {}", idPromotor);
                FestivalDTO creado = festivalService.crearFestival(dto, idPromotor);
                mensajeExito = "Festival '" + creado.getNombre() + "' creado con éxito.";
            } else {
                log.info("Llamando a actualizar/cambiar estado de Festival ID: {} para promotor ID: {}", idFestival, idPromotor);
                // Si se envió un estado, cambiarlo primero (o ajustar la lógica del servicio)
                if (dto.getEstado() != null) {
                    festivalService.cambiarEstadoFestival(idFestival, dto.getEstado(), idPromotor);
                }
                festivalService.actualizarFestival(idFestival, dto, idPromotor);
                mensajeExito = "Festival '" + dto.getNombre() + "' actualizado con éxito.";
            }

            // Guardar mensaje flash en sesión (requiere manejo en JSP)
            request.getSession().setAttribute("mensaje", mensajeExito);

            // Redirigir a la lista usando JAX-RS Response y UriInfo
            URI listUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("listar").build();
            log.debug("Redirigiendo a: {}", listUri);
            return Response.seeOther(listUri).build(); // 303 See Other para PRG

        } catch (FestivalNotFoundException | SecurityException | IllegalArgumentException e) {
            log.warn("Error de negocio al guardar festival: {}", e.getMessage());
            // Para devolver al formulario con errores:
            request.setAttribute("error", e.getMessage());
            request.setAttribute("festival", dto); // Devolver DTO con datos introducidos
            request.setAttribute("idPromotorAutenticado", idPromotor);
            request.setAttribute("estadosPosibles", EstadoFestival.values());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/promotor/festival-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build(); // El forward ya manejó la respuesta
        } catch (WebApplicationException wae) {
            throw wae; // Relanzar excepciones de validación JAX-RS
        } catch (Exception e) {
            log.error("Error interno al guardar festival: {}", e.getMessage(), e);
            request.setAttribute("error", "Error interno al guardar: " + e.getMessage());
            request.setAttribute("festival", dto);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            request.setAttribute("estadosPosibles", EstadoFestival.values());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/promotor/festival-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();
        }
    }

    /**
     * Procesa la petición para eliminar un festival. POST
     * /api/promotor/festivales/eliminar (o podría ser DELETE
     * /api/promotor/festivales/{id}) Usamos POST aquí para que funcione con un
     * simple form HTML.
     */
    @POST
    @Path("/eliminar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response eliminarFestival(@FormParam("idFestival") Integer idFestival) {
        log.info("POST /eliminar recibido para ID: {}", idFestival);
        Integer idPromotor = obtenerIdUsuarioAutenticadoYValidarRol(RolUsuario.PROMOTOR);

        if (idFestival == null) {
            log.warn("Falta ID de festival para eliminar.");
            // Podríamos redirigir con error, pero lanzar excepción es más JAX-RS
            throw new WebApplicationException("Falta el ID del festival a eliminar.", Response.Status.BAD_REQUEST);
        }

        String mensajeFlash = null;
        String errorFlash = null;

        try {
            log.info("Llamando a eliminarFestival ID: {} para promotor ID: {}", idFestival, idPromotor);
            festivalService.eliminarFestival(idFestival, idPromotor);
            mensajeFlash = "Festival eliminado con éxito.";

        } catch (FestivalNotFoundException | SecurityException e) {
            log.warn("Error de negocio al eliminar festival ID {}: {}", idFestival, e.getMessage());
            errorFlash = e.getMessage();
        } catch (RuntimeException e) { // Error por restricciones de FK, etc.
            log.error("Error de persistencia al eliminar festival ID {}: {}", idFestival, e.getMessage());
            errorFlash = "No se pudo eliminar el festival (puede tener datos asociados): " + e.getMessage();
        } catch (Exception e) {
            log.error("Error interno al eliminar festival ID {}: {}", idFestival, e.getMessage(), e);
            errorFlash = "Error interno al eliminar el festival.";
        }

        // Guardar mensaje flash en sesión
        if (mensajeFlash != null) {
            request.getSession().setAttribute("mensaje", mensajeFlash);
        }
        if (errorFlash != null) {
            request.getSession().setAttribute("error", errorFlash);
        }

        // Redirigir siempre a la lista
        URI listUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("listar").build();
        log.debug("Redirigiendo a: {}", listUri);
        return Response.seeOther(listUri).build();
    }

    // --- Método Auxiliar de Seguridad ---
    /**
     * Obtiene el ID del usuario autenticado desde el SecurityContext y valida
     * su rol. Lanza WebApplicationException si no está autenticado o no tiene
     * el rol requerido.
     *
     * @param rolRequerido El rol que debe tener el usuario.
     * @return El ID del usuario autenticado.
     * @throws WebApplicationException con estado 401 (Unauthorized) o 403
     * (Forbidden).
     */
    private Integer obtenerIdUsuarioAutenticadoYValidarRol(RolUsuario rolRequerido) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            log.warn("Intento de acceso sin autenticación.");
            throw new WebApplicationException("Autenticación requerida.", Response.Status.UNAUTHORIZED);
        }
        if (!securityContext.isUserInRole(rolRequerido.name())) {
            log.warn("Usuario {} no tiene el rol requerido {}", securityContext.getUserPrincipal().getName(), rolRequerido);
            throw new WebApplicationException("Acceso denegado. Rol requerido: " + rolRequerido.name(), Response.Status.FORBIDDEN);
        }
        try {
            Integer userId = Integer.parseInt(securityContext.getUserPrincipal().getName());
            log.debug("Usuario autenticado ID: {} con rol {}", userId, rolRequerido);
            return userId;
        } catch (NumberFormatException e) {
            log.error("Error al parsear ID de usuario desde Principal: {}", securityContext.getUserPrincipal().getName());
            throw new WebApplicationException("Error interno de autenticación.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
