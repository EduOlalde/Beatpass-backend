/*
 * Recurso JAX-RS para las funcionalidades del panel de Administración.
 * Gestiona operaciones sobre Promotores y Festivales que requieren rol ADMIN.
 * La autenticación y autorización para este panel se basa en Sesión HTTP.
 * ACTUALIZADO: Añadido endpoint GET para ver festivales de un promotor específico.
 */
package com.daw2edudiego.beatpasstfg.web;

// Imports DTOs, Excepciones, Modelo, Servicios
import com.daw2edudiego.beatpasstfg.dto.AsistenteDTO;
import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.exception.AsistenteNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.EmailExistenteException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.service.AsistenteService;
import com.daw2edudiego.beatpasstfg.service.AsistenteServiceImpl;
import com.daw2edudiego.beatpasstfg.service.FestivalService;
import com.daw2edudiego.beatpasstfg.service.FestivalServiceImpl;
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;

// Imports Jakarta EE para Servlets y JAX-RS
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*; // Anotaciones JAX-RS
import jakarta.ws.rs.core.Context; // Para inyectar contexto HTTP/Seguridad
import jakarta.ws.rs.core.MediaType; // Tipos MIME
import jakarta.ws.rs.core.Response; // Respuesta HTTP
import jakarta.ws.rs.core.UriInfo; // Información sobre la URI de la petición
// jakarta.validation.Valid; // Si se usara Bean Validation

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Clases estándar de Java
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Recurso JAX-RS (@Path) para el panel de Administración (/api/admin). Define
 * endpoints para gestionar Promotores (crear, listar, cambiar estado) y
 * Festivales (crear por admin, listar todos, confirmar, cambiar estado, listar
 * por promotor). La seguridad de estos endpoints se basa en verificar la sesión
 * HTTP y el rol ADMIN almacenado en ella.
 */
@Path("/admin")
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    // Inyección de dependencias (manual)
    private final UsuarioService usuarioService;
    private final FestivalService festivalService;
    private final AsistenteService asistenteService; // Nuevo servicio

    // Inyección de contexto JAX-RS
    @Context
    private UriInfo uriInfo;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    // Constructor: instancia los servicios
    public AdminResource() {
        this.usuarioService = new UsuarioServiceImpl();
        this.festivalService = new FestivalServiceImpl();
        this.asistenteService = new AsistenteServiceImpl(); // Instanciar
    }

    // --- Endpoints para Gestión de Promotores por Admin ---
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
        mostrarMensajeFlash(request);
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

    // *** NUEVO MÉTODO ***
    /**
     * GET /api/admin/promotores/{idPromotor}/festivales Muestra la lista de
     * festivales para un promotor específico (vista de admin). Devuelve HTML
     * (forward a JSP). Requiere rol ADMIN en sesión.
     */
    @GET
    @Path("/promotores/{idPromotor}/festivales")
    @Produces(MediaType.TEXT_HTML)
    public Response listarFestivalesDePromotor(@PathParam("idPromotor") Integer idPromotor) throws ServletException, IOException {
        log.debug("GET /admin/promotores/{}/festivales recibido", idPromotor);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol ADMIN

        if (idPromotor == null) {
            throw new BadRequestException("ID de Promotor no proporcionado.");
        }

        try {
            // Obtener datos del promotor para mostrar su nombre
            UsuarioDTO promotor = usuarioService.obtenerUsuarioPorId(idPromotor)
                    .filter(u -> u.getRol() == RolUsuario.PROMOTOR) // Asegurarse que es promotor
                    .orElseThrow(() -> new NotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // Obtener festivales de ese promotor
            List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);

            // Pasar datos al JSP
            request.setAttribute("promotor", promotor); // Datos del promotor
            request.setAttribute("festivales", listaFestivales); // Lista de sus festivales
            request.setAttribute("idAdminAutenticado", idAdmin);
            mostrarMensajeFlash(request); // Por si hay mensajes de acciones previas

            // Forward al JSP correspondiente
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotor-festivales.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException e) {
            // Si el promotor no se encuentra, lanzar 404
            log.warn("Promotor ID {} no encontrado al intentar listar sus festivales.", idPromotor);
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener festivales para promotor ID {}: {}", idPromotor, e.getMessage(), e);
            throw new InternalServerErrorException("Error interno al cargar los festivales del promotor.", e);
        }
    }
    // *** FIN NUEVO MÉTODO ***

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarDashboard() throws ServletException, IOException {
        // ... (sin cambios) ...
        log.debug("GET /admin/dashboard recibido");
        verificarAccesoAdmin(request);
        log.info("Dashboard de Admin no implementado, redirigiendo a lista de promotores.");
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

    // --- Endpoints para Gestión de Festivales por Admin ---
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
        // ... (sin cambios) ...
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
                throw new IllegalArgumentException("Nombre obligatorio.");
            }
            dto.setFechaInicio(LocalDate.parse(fechaInicioStr));
            dto.setFechaFin(LocalDate.parse(fechaFinStr));
            if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
                throw new IllegalArgumentException("Fechas incoherentes");
            }
            if (aforoStr != null && !aforoStr.isBlank()) {
                dto.setAforo(Integer.parseInt(aforoStr));
                if (dto.getAforo() <= 0) {
                    throw new IllegalArgumentException("El aforo debe ser un número positivo.");
                }
            }
            dto.setEstado(EstadoFestival.BORRADOR);

            log.info("Llamando a festivalService.crearFestival para promotor ID: {} por Admin ID: {}", idPromotorSeleccionado, idAdmin);
            FestivalDTO creado = festivalService.crearFestival(dto, idPromotorSeleccionado);
            String mensajeExito = "Festival '" + creado.getNombre() + "' creado y asignado al promotor ID " + idPromotorSeleccionado + " con éxito (estado BORRADOR).";
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("mensaje", mensajeExito);
            }
            URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarTodosFestivales").build();
            log.debug("Redirigiendo a: {}", listUri);
            return Response.seeOther(listUri).build();

        } catch (NumberFormatException e) {
            log.warn("Error de formato numérico al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Formato de número inválido (ej: en Aforo).");
            return Response.ok().build();
        } catch (DateTimeParseException e) {
            log.warn("Error de formato de fecha al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Formato de fecha inválido (use YYYY-MM-DD).");
            return Response.ok().build();
        } catch (IllegalArgumentException | UsuarioNotFoundException e) {
            log.warn("Error de validación/negocio al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Datos inválidos: " + e.getMessage());
            return Response.ok().build();
        } catch (Exception e) {
            log.error("Error interno inesperado al crear festival por admin: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Error interno al crear el festival.", e);
        }
    }

    @GET
    @Path("/festivales/listar-todos")
    @Produces(MediaType.TEXT_HTML)
    public Response listarTodosFestivales(@QueryParam("estado") String estadoFilter) throws ServletException, IOException {
        // ... (sin cambios) ...
        log.debug("GET /admin/festivales/listar-todos recibido. Filtro estado: {}", estadoFilter);
        Integer idAdmin = verificarAccesoAdmin(request);

        List<FestivalDTO> listaFestivales;
        EstadoFestival estadoEnum = null;
        String errorFiltro = null;

        try {
            if (estadoFilter != null && !estadoFilter.isBlank()) {
                estadoEnum = EstadoFestival.valueOf(estadoFilter.toUpperCase());
                log.debug("Filtrando festivales por estado: {}", estadoEnum);
            }
            listaFestivales = festivalService.obtenerFestivalesPorEstado(estadoEnum);

        } catch (IllegalArgumentException e) {
            log.warn("Estado de filtro inválido recibido: {}", estadoFilter);
            errorFiltro = "Estado de filtro inválido: '" + estadoFilter + "'. Mostrando todos.";
            listaFestivales = festivalService.obtenerTodosLosFestivales();
        } catch (Exception e) {
            log.error("Error obteniendo lista de festivales para admin: {}", e.getMessage(), e);
            request.setAttribute("error", "Error crítico al cargar la lista de festivales.");
            listaFestivales = List.of();
        }

        request.setAttribute("festivales", listaFestivales);
        request.setAttribute("idAdminAutenticado", idAdmin);
        request.setAttribute("estadoFiltro", estadoFilter);
        request.setAttribute("estadosPosibles", EstadoFestival.values());
        if (errorFiltro != null) {
            request.setAttribute("error", errorFiltro);
        }
        mostrarMensajeFlash(request);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-festivales.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    @POST
    @Path("/festivales/confirmar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response confirmarFestival(@FormParam("idFestival") Integer idFestival) {
        // ... (sin cambios) ...
        log.info("POST /admin/festivales/confirmar para ID: {}", idFestival);
        Integer idAdmin = verificarAccesoAdmin(request);

        if (idFestival == null) {
            throw new BadRequestException("Falta el parámetro idFestival.");
        }

        String mensajeFlash = null;
        String errorFlash = null;

        try {
            log.info("Llamando a festivalService.cambiarEstadoFestival ID {} a PUBLICADO por Admin ID {}", idFestival, idAdmin);
            FestivalDTO confirmado = festivalService.cambiarEstadoFestival(idFestival, EstadoFestival.PUBLICADO, idAdmin);
            mensajeFlash = "Festival '" + confirmado.getNombre() + "' confirmado y publicado con éxito.";

        } catch (FestivalNotFoundException | UsuarioNotFoundException e) {
            log.warn("Error al confirmar festival ID {}: {}", idFestival, e.getMessage());
            errorFlash = e.getMessage();
        } catch (SecurityException | IllegalStateException e) {
            log.warn("Error de negocio/seguridad al confirmar festival ID {}: {}", idFestival, e.getMessage());
            errorFlash = "No se pudo confirmar el festival: " + e.getMessage();
        } catch (Exception e) {
            log.error("Error interno al confirmar festival ID {}: {}", idFestival, e.getMessage(), e);
            errorFlash = "Error interno al confirmar el festival.";
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

        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarTodosFestivales").build();
        log.debug("Redirigiendo a: {}", listUri);
        return Response.seeOther(listUri).build();
    }

    @POST
    @Path("/festivales/cambiar-estado")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response cambiarEstadoFestivalAdmin(
            @FormParam("idFestival") Integer idFestival,
            @FormParam("nuevoEstado") String nuevoEstadoStr) {
        // ... (sin cambios) ...
        log.info("POST /admin/festivales/cambiar-estado para ID: {} a {}", idFestival, nuevoEstadoStr);
        Integer idAdmin = verificarAccesoAdmin(request);

        if (idFestival == null || nuevoEstadoStr == null || nuevoEstadoStr.isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos (idFestival, nuevoEstado).");
        }

        EstadoFestival nuevoEstado;
        try {
            nuevoEstado = EstadoFestival.valueOf(nuevoEstadoStr.toUpperCase());
            if (nuevoEstado == EstadoFestival.BORRADOR || nuevoEstado == EstadoFestival.PUBLICADO) {
                throw new IllegalArgumentException("Use la acción 'Confirmar' para publicar. No se puede volver a Borrador.");
            }
        } catch (IllegalArgumentException e) {
            log.warn("Valor inválido para 'nuevoEstado' en POST /admin/festivales/cambiar-estado: {}", nuevoEstadoStr);
            throw new BadRequestException("Valor de 'nuevoEstado' inválido. Use CANCELADO o FINALIZADO.");
        }

        String mensajeFlash = null;
        String errorFlash = null;

        try {
            log.info("Llamando a festivalService.cambiarEstadoFestival ID {} a {} por Admin ID {}", idFestival, nuevoEstado, idAdmin);
            FestivalDTO actualizado = festivalService.cambiarEstadoFestival(idFestival, nuevoEstado, idAdmin);
            mensajeFlash = "Estado del festival '" + actualizado.getNombre() + "' cambiado a " + nuevoEstado + " con éxito.";

        } catch (FestivalNotFoundException | UsuarioNotFoundException e) {
            log.warn("Error al cambiar estado de festival ID {}: {}", idFestival, e.getMessage());
            errorFlash = e.getMessage();
        } catch (SecurityException | IllegalStateException e) {
            log.warn("Error de negocio/seguridad al cambiar estado de festival ID {}: {}", idFestival, e.getMessage());
            errorFlash = "No se pudo cambiar el estado: " + e.getMessage();
        } catch (Exception e) {
            log.error("Error interno al cambiar estado de festival ID {}: {}", idFestival, e.getMessage(), e);
            errorFlash = "Error interno al cambiar el estado del festival.";
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

        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarTodosFestivales").build();
        log.debug("Redirigiendo a: {}", listUri);
        return Response.seeOther(listUri).build();
    }

    // --- *** NUEVOS ENDPOINTS PARA GESTIÓN DE ASISTENTES POR ADMIN *** ---
    /**
     * GET /api/admin/asistentes Lista todos los asistentes registrados.
     * Devuelve HTML (forward a JSP). Requiere rol ADMIN en sesión.
     */
    @GET
    @Path("/asistentes")
    @Produces(MediaType.TEXT_HTML)
    public Response listarAsistentes(@QueryParam("buscar") String searchTerm) throws ServletException, IOException {
        log.debug("GET /admin/asistentes recibido. Término búsqueda: '{}'", searchTerm);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol ADMIN

        List<AsistenteDTO> listaAsistentes;
        try {
            if (searchTerm != null && !searchTerm.isBlank()) {
                log.debug("Buscando asistentes con término: {}", searchTerm);
                listaAsistentes = asistenteService.buscarAsistentes(searchTerm);
            } else {
                log.debug("Obteniendo todos los asistentes.");
                listaAsistentes = asistenteService.obtenerTodosLosAsistentes();
            }
        } catch (Exception e) {
            log.error("Error obteniendo lista de asistentes para admin: {}", e.getMessage(), e);
            request.setAttribute("error", "Error crítico al cargar la lista de asistentes.");
            listaAsistentes = Collections.emptyList(); // Lista vacía en caso de error
        }

        // Pasar datos al JSP
        request.setAttribute("asistentes", listaAsistentes);
        request.setAttribute("idAdminAutenticado", idAdmin);
        request.setAttribute("searchTerm", searchTerm); // Para repoblar campo búsqueda
        mostrarMensajeFlash(request); // Por si hay mensajes de acciones futuras

        // Forward al nuevo JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-asistentes.jsp"); // Crear este JSP
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * GET /api/admin/asistentes/{idAsistente} Muestra la página de detalles de
     * un asistente específico. Devuelve HTML (forward a JSP). Requiere rol
     * ADMIN en sesión.
     */
    @GET
    @Path("/asistentes/{idAsistente}")
    @Produces(MediaType.TEXT_HTML)
    public Response verDetalleAsistente(@PathParam("idAsistente") Integer idAsistente) throws ServletException, IOException {
        log.debug("GET /admin/asistentes/{} recibido", idAsistente);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol ADMIN

        if (idAsistente == null) {
            throw new BadRequestException("ID de Asistente no proporcionado.");
        }

        try {
            // Obtener el DTO del asistente
            AsistenteDTO asistente = asistenteService.obtenerAsistentePorId(idAsistente)
                    .orElseThrow(() -> new NotFoundException("Asistente no encontrado con ID: " + idAsistente));

            // Pasar datos al JSP
            request.setAttribute("asistente", asistente);
            request.setAttribute("idAdminAutenticado", idAdmin);
            mostrarMensajeFlash(request); // Por si hay mensajes

            // Forward al nuevo JSP de detalle
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-asistente-detalle.jsp"); // Crear este JSP
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException e) {
            // Si el asistente no se encuentra, JAX-RS manejará NotFoundException (404)
            log.warn("Asistente ID {} no encontrado.", idAsistente);
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener detalles del asistente ID {}: {}", idAsistente, e.getMessage(), e);
            throw new InternalServerErrorException("Error interno al cargar los detalles del asistente.", e);
        }
    }

    /**
     * GET /api/admin/asistentes/{idAsistente}/editar Muestra el formulario para
     * editar un asistente existente. Devuelve HTML (forward a JSP). Requiere
     * rol ADMIN.
     */
    @GET
    @Path("/asistentes/{idAsistente}/editar")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditarAsistente(@PathParam("idAsistente") Integer idAsistente) throws ServletException, IOException {
        log.debug("GET /admin/asistentes/{}/editar recibido", idAsistente);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no válido.");
        }

        try {
            AsistenteDTO asistente = asistenteService.obtenerAsistentePorId(idAsistente)
                    .orElseThrow(() -> new NotFoundException("Asistente no encontrado con ID: " + idAsistente));

            request.setAttribute("asistente", asistente);
            request.setAttribute("idAdminAutenticado", idAdmin);
            request.setAttribute("editMode", true); // Indicador para el JSP
            mostrarMensajeFlash(request);

            // Reutilizamos el JSP de detalle, que ahora será editable
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-asistente-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al mostrar formulario editar asistente ID {}: {}", idAsistente, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del asistente.", e);
        }
    }

    /**
     * POST /api/admin/asistentes/{idAsistente}/actualizar Procesa la
     * actualización de un asistente existente. Espera datos de formulario.
     * Requiere rol ADMIN. Redirige a la lista de asistentes.
     */
    @POST
    @Path("/asistentes/{idAsistente}/actualizar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response actualizarAsistente(
            @PathParam("idAsistente") Integer idAsistente,
            @FormParam("nombre") String nombre,
            @FormParam("telefono") String telefono) throws ServletException, IOException { // No se actualiza email

        log.info("POST /admin/asistentes/{}/actualizar recibido", idAsistente);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no válido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        AsistenteDTO dto = new AsistenteDTO(); // Para posible reenvío al form

        try {
            // Validar y poblar DTO (solo campos editables)
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("Nombre obligatorio.");
            }
            dto.setIdAsistente(idAsistente); // Necesario para el servicio
            dto.setNombre(nombre);
            dto.setTelefono(telefono); // Teléfono es opcional

            // Llamar al servicio para actualizar
            AsistenteDTO actualizado = asistenteService.actualizarAsistente(idAsistente, dto);
            mensajeFlash = "Asistente '" + actualizado.getNombre() + "' (ID: " + idAsistente + ") actualizado con éxito.";

        } catch (IllegalArgumentException | AsistenteNotFoundException e) {
            errorFlash = "Error: " + e.getMessage();
            log.warn("Error al actualizar asistente ID {}: {}", idAsistente, errorFlash);
            // Poblar DTO para rellenar form (necesitaríamos el email original)
            dto.setNombre(nombre);
            dto.setTelefono(telefono);
            // Podríamos volver a buscar el asistente para obtener el email y otros datos
            // o pasar el DTO con error en sesión y que el GET /editar lo recoja.
            // Por simplicidad ahora, solo ponemos mensaje de error.
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al actualizar.";
            log.error("Error interno al actualizar asistente ID {}: {}", idAsistente, e.getMessage(), e);
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

        // Redirigir a la lista de asistentes
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarAsistentes").build();
        return Response.seeOther(listUri).build();
    }

    // --- *** FIN ENDPOINTS ASISTENTES *** ---
    // --- Método Auxiliar de Seguridad ---
    private Integer verificarAccesoAdmin(HttpServletRequest request) {
        // ... (sin cambios) ...
        HttpSession session = request.getSession(false);
        log.debug("Verificando acceso Admin. ¿Sesión existe?: {}", (session != null));
        if (session == null) {
            log.warn("Intento de acceso a recurso Admin sin sesión activa.");
            throw new NotAuthorizedException("No hay sesión activa. Por favor, inicie sesión.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        log.debug("Atributos de sesión encontrados: userId={}, userRole={}", userId, userRole);
        if (userId == null || userRole == null) {
            log.warn("Intento de acceso a recurso Admin con sesión inválida (faltan atributos userId o userRole). Sesión ID: {}", session.getId());
            session.invalidate();
            throw new NotAuthorizedException("Sesión inválida. Por favor, inicie sesión de nuevo.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        if (!RolUsuario.ADMIN.name().equals(userRole)) {
            log.warn("Usuario ID {} con rol {} intentó acceder a recurso de Admin.", userId, userRole);
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

    private void mostrarMensajeFlash(HttpServletRequest request) {
        // ... (sin cambios) ...
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (session.getAttribute("mensaje") != null) {
                request.setAttribute("mensajeExito", session.getAttribute("mensaje"));
                session.removeAttribute("mensaje");
            }
            if (session.getAttribute("error") != null) {
                request.setAttribute("error", session.getAttribute("error"));
                session.removeAttribute("error");
            }
        }
    }
}
