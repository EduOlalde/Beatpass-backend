package com.daw2edudiego.beatpasstfg.web; // O tu paquete web

// Imports actualizados a jakarta.*
import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.exception.EmailExistenteException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.service.FestivalService;
import com.daw2edudiego.beatpasstfg.service.FestivalServiceImpl;
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
// jakarta.validation.Valid si usas Bean Validation

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional; // Necesario para Optional
import java.util.stream.Collectors;

/**
 * Recurso JAX-RS para el panel de Administración. Gestiona Promotores y
 * Festivales (creación y visualización por promotor). Requiere rol ADMIN
 * autenticado mediante Sesión HTTP. ¡ACTUALIZADO para ver festivales de un
 * promotor!
 */
@Path("/admin")
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    private final UsuarioService usuarioService;
    private final FestivalService festivalService;

    @Context
    private UriInfo uriInfo;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    public AdminResource() {
        this.usuarioService = new UsuarioServiceImpl();
        this.festivalService = new FestivalServiceImpl();
    }

    // --- Métodos de Gestión de Promotores ---
    @GET
    @Path("/promotores/listar")
    @Produces(MediaType.TEXT_HTML)
    public Response listarPromotores() throws ServletException, IOException {
        // ... (sin cambios) ...
        log.debug("GET /admin/promotores/listar recibido");
        Integer idAdmin = verificarAccesoAdmin(request);
        log.debug("Listando usuarios con rol PROMOTOR para Admin ID: {}", idAdmin);
        List<UsuarioDTO> listaPromotores = usuarioService.obtenerUsuariosPorRol(RolUsuario.PROMOTOR);
        request.setAttribute("promotores", listaPromotores);
        request.setAttribute("idAdminAutenticado", idAdmin);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotores.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    @GET
    @Path("/promotores")
    @Produces(MediaType.TEXT_HTML)
    public Response listarPromotoresRoot() throws ServletException, IOException {
        return listarPromotores();
    }

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarDashboard() throws ServletException, IOException {
        // ... (sin cambios) ...
        log.debug("GET /admin/dashboard recibido");
        verificarAccesoAdmin(request);
        log.info("Dashboard no implementado, redirigiendo a lista de promotores.");
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarPromotores").build();
        return Response.seeOther(listUri).build();
    }

    @GET
    @Path("/promotores/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrearPromotor() throws ServletException, IOException {
        // ... (sin cambios) ...
        log.debug("GET /admin/promotores/crear recibido");
        Integer idAdmin = verificarAccesoAdmin(request);
        log.debug("Mostrando formulario de creación de promotor por Admin ID: {}", idAdmin);
        request.setAttribute("promotor", new UsuarioCreacionDTO());
        request.setAttribute("esNuevo", true);
        request.setAttribute("idAdminAutenticado", idAdmin);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotor-detalle.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    @POST
    @Path("/promotores/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarPromotor(
            @FormParam("nombre") String nombre,
            @FormParam("email") String email,
            @FormParam("password") String password) throws ServletException, IOException {
        // ... (sin cambios) ...
        log.info("POST /admin/promotores/guardar (CREACIÓN) recibido para email: {}", email);
        Integer idAdmin = verificarAccesoAdmin(request);
        UsuarioCreacionDTO dto = new UsuarioCreacionDTO();
        dto.setNombre(nombre);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setRol(RolUsuario.PROMOTOR);
        try {
            if (nombre == null || nombre.isBlank() || email == null || email.isBlank() || password == null || password.isEmpty()) {
                throw new IllegalArgumentException("Nombre, email y contraseña son obligatorios.");
            }
            if (password.length() < 8) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
            }
            log.info("Llamando a usuarioService.crearUsuario (Promotor) por Admin ID: {}", idAdmin);
            UsuarioDTO creado = usuarioService.crearUsuario(dto);
            String mensajeExito = "Promotor '" + creado.getNombre() + "' creado con éxito.";
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("mensaje", mensajeExito);
            }
            URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarPromotores").build();
            log.debug("Redirigiendo a: {}", listUri);
            return Response.seeOther(listUri).build();
        } catch (EmailExistenteException e) {
            log.warn("Error de negocio al crear promotor (email existente): {}", e.getMessage());
            forwardToPromotorFormWithError(dto, idAdmin, e.getMessage());
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear promotor (argumento): {}", e.getMessage());
            forwardToPromotorFormWithError(dto, idAdmin, e.getMessage());
            return Response.ok().build();
        } catch (Exception e) {
            log.error("Error interno inesperado al crear promotor: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Error interno al crear el promotor.", e);
        }
    }

    @POST
    @Path("/promotores/cambiar-estado")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response cambiarEstadoPromotor(
            @FormParam("idPromotor") Integer idPromotor,
            @FormParam("nuevoEstado") Boolean nuevoEstado) {
        // ... (sin cambios) ...
        log.info("POST /admin/promotores/cambiar-estado para ID: {} a {}", idPromotor, nuevoEstado);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idPromotor == null || nuevoEstado == null) {
            throw new BadRequestException("Faltan parámetros requeridos (idPromotor, nuevoEstado).");
        }
        String mensajeFlash = null;
        String errorFlash = null;
        try {
            log.info("Llamando a usuarioService.actualizarEstadoUsuario ID: {} a {} por Admin ID: {}", idPromotor, nuevoEstado, idAdmin);
            UsuarioDTO actualizado = usuarioService.actualizarEstadoUsuario(idPromotor, nuevoEstado);
            mensajeFlash = "Estado del promotor '" + actualizado.getNombre() + "' actualizado a " + (nuevoEstado ? "ACTIVO" : "INACTIVO") + ".";
        } catch (UsuarioNotFoundException e) {
            log.warn("Promotor ID {} no encontrado para cambiar estado.", idPromotor);
            errorFlash = e.getMessage();
        } catch (Exception e) {
            log.error("Error interno al cambiar estado del promotor ID {}: {}", idPromotor, e.getMessage(), e);
            errorFlash = "Error interno al cambiar el estado.";
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (mensajeFlash != null) {
                session.setAttribute("mensaje", mensajeFlash);
            }
            if (errorFlash != null) {
                session.setAttribute("error", errorFlash);
            }
        }
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarPromotores").build();
        log.debug("Redirigiendo a: {}", listUri);
        return Response.seeOther(listUri).build();
    }

    // --- Métodos para Gestión de Festivales por Admin ---
    @GET
    @Path("/festivales/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrearFestival() throws ServletException, IOException {
        // ... (sin cambios) ...
        log.debug("GET /admin/festivales/crear recibido");
        Integer idAdmin = verificarAccesoAdmin(request);
        List<UsuarioDTO> promotoresActivos = usuarioService.obtenerUsuariosPorRol(RolUsuario.PROMOTOR)
                .stream()
                .filter(UsuarioDTO::getEstado)
                .collect(Collectors.toList());
        log.debug("Mostrando formulario de creación de festival por Admin ID: {}", idAdmin);
        request.setAttribute("festival", new FestivalDTO());
        request.setAttribute("promotores", promotoresActivos);
        request.setAttribute("esNuevo", true);
        request.setAttribute("idAdminAutenticado", idAdmin);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-festival-detalle.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    @POST
    @Path("/festivales/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarFestivalAdmin(
            @FormParam("nombre") String nombre,
            @FormParam("descripcion") String descripcion,
            @FormParam("fechaInicio") String fechaInicioStr,
            @FormParam("fechaFin") String fechaFinStr,
            @FormParam("ubicacion") String ubicacion,
            @FormParam("aforo") String aforoStr,
            @FormParam("imagenUrl") String imagenUrl,
            @FormParam("idPromotorSeleccionado") Integer idPromotorSeleccionado
    ) throws ServletException, IOException {
        // ... (lógica try/catch separada sin cambios) ...
        log.info("POST /admin/festivales/guardar (CREACIÓN Admin) recibido para promotor ID: {}", idPromotorSeleccionado);
        Integer idAdmin = verificarAccesoAdmin(request);
        FestivalDTO dto = new FestivalDTO();
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setUbicacion(ubicacion);
        dto.setImagenUrl(imagenUrl);
        try {
            if (idPromotorSeleccionado == null) {
                throw new IllegalArgumentException("Debe seleccionar un promotor.");
            }
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("Nombre obligatorio");
            }
            dto.setFechaInicio(LocalDate.parse(fechaInicioStr));
            dto.setFechaFin(LocalDate.parse(fechaFinStr));
            if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
                throw new IllegalArgumentException("Fechas incoherentes");
            }
            if (aforoStr != null && !aforoStr.isBlank()) {
                dto.setAforo(Integer.parseInt(aforoStr));
            }
            dto.setEstado(EstadoFestival.BORRADOR);
        } catch (NumberFormatException e) {
            log.warn("Error de formato numérico al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Formato de número inválido (ej: en Aforo).");
            return Response.ok().build();
        } catch (DateTimeParseException e) {
            log.warn("Error de formato de fecha al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Formato de fecha inválido (use yyyy-MM-dd).");
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Datos inválidos: " + e.getMessage());
            return Response.ok().build();
        }
        try {
            log.info("Llamando a festivalService.crearFestival para promotor ID: {} por Admin ID: {}", idPromotorSeleccionado, idAdmin);
            FestivalDTO creado = festivalService.crearFestival(dto, idPromotorSeleccionado);
            String mensajeExito = "Festival '" + creado.getNombre() + "' creado y asignado al promotor ID " + idPromotorSeleccionado + " con éxito.";
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("mensaje", mensajeExito);
            }
            URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarPromotores").build();
            log.debug("Redirigiendo a: {}", listUri);
            return Response.seeOther(listUri).build();
        } catch (IllegalArgumentException e) {
            log.warn("Error de negocio al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, e.getMessage());
            return Response.ok().build();
        } catch (Exception e) {
            log.error("Error interno inesperado al crear festival por admin: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Error interno al crear el festival.", e);
        }
    }

    // --- NUEVO MÉTODO: Ver Festivales de un Promotor Específico ---
    /**
     * Muestra la lista de festivales pertenecientes a un promotor específico.
     * GET /api/admin/promotores/{id}/festivales
     */
    @GET
    @Path("/promotores/{idPromotor}/festivales")
    @Produces(MediaType.TEXT_HTML)
    public Response listarFestivalesDePromotor(@PathParam("idPromotor") Integer idPromotor)
            throws ServletException, IOException {

        log.debug("GET /admin/promotores/{}/festivales recibido", idPromotor);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica que el admin esté logueado

        try {
            // Obtener info del promotor para mostrar su nombre
            UsuarioDTO promotor = usuarioService.obtenerUsuarioPorId(idPromotor)
                    .orElseThrow(() -> new WebApplicationException("Promotor no encontrado con ID: " + idPromotor, Response.Status.NOT_FOUND));

            // Verificar que el usuario encontrado sea realmente un promotor (por si acaso)
            if (promotor.getRol() != RolUsuario.PROMOTOR) {
                log.warn("Se intentó ver festivales de un usuario que no es promotor. ID: {}", idPromotor);
                throw new WebApplicationException("El ID proporcionado no corresponde a un promotor.", Response.Status.BAD_REQUEST);
            }

            log.debug("Listando festivales para Promotor ID: {} (visto por Admin ID: {})", idPromotor, idAdmin);
            List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);

            // Pasar datos a la nueva JSP
            request.setAttribute("promotor", promotor); // Info del promotor
            request.setAttribute("festivales", listaFestivales); // Lista de sus festivales
            request.setAttribute("idAdminAutenticado", idAdmin);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotor-festivales.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (WebApplicationException wae) {
            throw wae; // Relanzar excepciones JAX-RS
        } catch (Exception e) {
            log.error("Error al listar festivales del promotor ID {}: {}", idPromotor, e.getMessage(), e);
            throw new ServletException("Error al obtener los festivales del promotor.", e);
        }
    }

    // --- Método Auxiliar de Seguridad ---
    private Integer verificarAccesoAdmin(HttpServletRequest request) {
        // ... (sin cambios) ...
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new NotAuthorizedException("No hay sesión activa.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        if (userId == null || userRole == null) {
            session.invalidate();
            throw new NotAuthorizedException("Sesión inválida.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        if (!RolUsuario.ADMIN.name().equals(userRole)) {
            throw new ForbiddenException("Acceso denegado. Se requiere rol ADMIN.");
        }
        log.debug("Acceso permitido para admin ID: {}", userId);
        return userId;
    }

    // --- Métodos Auxiliares para Forward con Error ---
    private void forwardToPromotorFormWithError(UsuarioCreacionDTO dto, Integer idAdmin, String errorMessage)
            throws ServletException, IOException {
        // ... (sin cambios) ...
        request.setAttribute("error", errorMessage);
        dto.setPassword("");
        request.setAttribute("promotor", dto);
        request.setAttribute("esNuevo", true);
        request.setAttribute("idAdminAutenticado", idAdmin);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotor-detalle.jsp");
        dispatcher.forward(request, response);
    }

    private void forwardToFestivalFormWithError(FestivalDTO dto, Integer idAdmin, String errorMessage)
            throws ServletException, IOException {
        // ... (sin cambios) ...
        request.setAttribute("error", errorMessage);
        request.setAttribute("festival", dto);
        request.setAttribute("esNuevo", true);
        List<UsuarioDTO> promotoresActivos = usuarioService.obtenerUsuariosPorRol(RolUsuario.PROMOTOR)
                .stream().filter(UsuarioDTO::getEstado).collect(Collectors.toList());
        request.setAttribute("promotores", promotoresActivos);
        request.setAttribute("idAdminAutenticado", idAdmin);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-festival-detalle.jsp");
        dispatcher.forward(request, response);
    }

}
