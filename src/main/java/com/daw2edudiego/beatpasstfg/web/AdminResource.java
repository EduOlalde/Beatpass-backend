package com.daw2edudiego.beatpasstfg.web; // O tu paquete web (asegúrate que coincide)

// Imports cambiados a javax.*
import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.exception.EmailExistenteException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;

import javax.servlet.RequestDispatcher; // javax
import javax.servlet.ServletException; // javax
import javax.servlet.http.HttpServletRequest; // javax
import javax.servlet.http.HttpServletResponse; // javax
import javax.servlet.http.HttpSession; // javax
import javax.ws.rs.*; // javax
import javax.ws.rs.core.Context; // javax
import javax.ws.rs.core.MediaType; // javax
import javax.ws.rs.core.Response; // javax
import javax.ws.rs.core.UriInfo; // javax
// javax.validation.Valid si usas Bean Validation con javax

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Recurso JAX-RS para manejar las peticiones web del panel de Administración.
 * Gestiona la creación y estado de usuarios Promotores. Requiere rol ADMIN
 * autenticado mediante Sesión HTTP. ¡CORREGIDO para usar javax.* y @Path!
 */
@Path("/admin") // Ruta base para las acciones de administración
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    private final UsuarioService usuarioService;

    @Context
    private UriInfo uriInfo;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    public AdminResource() {
        this.usuarioService = new UsuarioServiceImpl();
    }

    // --- Punto de entrada (Listado de Promotores) ---
    /**
     * Muestra el listado de promotores. GET /api/admin/promotores/listar
     */
    @GET
    @Path("/promotores/listar") // Ruta explícita para listar
    @Produces(MediaType.TEXT_HTML)
    public Response listarPromotores() throws ServletException, IOException {
        log.debug("GET /admin/promotores/listar recibido");
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol ADMIN

        log.debug("Listando usuarios con rol PROMOTOR para Admin ID: {}", idAdmin);
        List<UsuarioDTO> listaPromotores = usuarioService.obtenerUsuariosPorRol(RolUsuario.PROMOTOR);
        request.setAttribute("promotores", listaPromotores);
        request.setAttribute("idAdminAutenticado", idAdmin);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotores.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * Ruta alternativa para listar promotores (mapea /api/admin/promotores).
     * GET /api/admin/promotores
     */
    @GET
    @Path("/promotores") // Ruta alternativa
    @Produces(MediaType.TEXT_HTML)
    public Response listarPromotoresRoot() throws ServletException, IOException {
        // Simplemente llama al otro método o repite la lógica
        return listarPromotores();
    }

    /**
     * Ruta para un posible dashboard (si se implementa). GET
     * /api/admin/dashboard
     */
    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarDashboard() throws ServletException, IOException {
        log.debug("GET /admin/dashboard recibido");
        Integer idAdmin = verificarAccesoAdmin(request);
        // TODO: Implementar lógica del dashboard (obtener stats, etc.)
        // Por ahora, podemos redirigir a la lista de promotores
        log.info("Dashboard no implementado, redirigiendo a lista de promotores.");
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path("listarPromotores").build();
        return Response.seeOther(listUri).build();
        // O hacer forward a un dashboard.jsp si lo creas
        // request.setAttribute("stats", someStatsObject);
        // RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp");
        // dispatcher.forward(request, response);
        // return Response.ok().build();
    }

    // --- Crear Promotor ---
    /**
     * Muestra el formulario para crear un nuevo promotor. GET
     * /api/admin/promotores/crear
     */
    @GET
    @Path("/promotores/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrearPromotor() throws ServletException, IOException {
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

    /**
     * Procesa el envío del formulario para crear un nuevo promotor. POST
     * /api/admin/promotores/guardar
     */
    @POST
    @Path("/promotores/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarPromotor(
            // Usamos @FormParam para JAX-RS estándar (funciona con javax.ws.rs)
            @FormParam("nombre") String nombre,
            @FormParam("email") String email,
            @FormParam("password") String password) throws ServletException, IOException {

        log.info("POST /admin/promotores/guardar (creación) recibido para email: {}", email);
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

            log.info("Llamando a crearUsuario (Promotor) por Admin ID: {}", idAdmin);
            UsuarioDTO creado = usuarioService.crearUsuario(dto);
            String mensajeExito = "Promotor '" + creado.getNombre() + "' creado con éxito.";

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("mensaje", mensajeExito);
            }

            // Redirigir a la lista (usando la ruta explícita ahora)
            URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path("listarPromotores").build();
            log.debug("Redirigiendo a: {}", listUri);
            return Response.seeOther(listUri).build();

        } catch (EmailExistenteException | IllegalArgumentException e) {
            log.warn("Error de negocio/validación al crear promotor: {}", e.getMessage());
            request.setAttribute("error", e.getMessage());
            dto.setPassword(""); // Limpiar password
            request.setAttribute("promotor", dto);
            request.setAttribute("esNuevo", true);
            request.setAttribute("idAdminAutenticado", idAdmin);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotor-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();
        } catch (Exception e) {
            log.error("Error interno al crear promotor: {}", e.getMessage(), e);
            throw new WebApplicationException("Error interno al crear el promotor.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // --- Cambiar Estado Promotor ---
    /**
     * Cambia el estado (activo/inactivo) de un promotor. POST
     * /api/admin/promotores/cambiar-estado
     */
    @POST
    @Path("/promotores/cambiar-estado")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response cambiarEstadoPromotor(
            @FormParam("idPromotor") Integer idPromotor,
            @FormParam("nuevoEstado") Boolean nuevoEstado) {

        log.info("POST /admin/promotores/cambiar-estado para ID: {} a {}", idPromotor, nuevoEstado);
        Integer idAdmin = verificarAccesoAdmin(request);

        if (idPromotor == null || nuevoEstado == null) {
            log.warn("Faltan parámetros para cambiar estado.");
            throw new WebApplicationException("Faltan parámetros requeridos.", Response.Status.BAD_REQUEST);
        }

        String mensajeFlash = null;
        String errorFlash = null;

        try {
            log.info("Llamando a actualizarEstadoUsuario ID: {} a {} por Admin ID: {}", idPromotor, nuevoEstado, idAdmin);
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

        // Redirigir a la lista
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path("listarPromotores").build();
        log.debug("Redirigiendo a: {}", listUri);
        return Response.seeOther(listUri).build();
    }

    // --- Método Auxiliar de Seguridad (BASADO EN SESIÓN HTTP) ---
    /**
     * Verifica si hay un usuario autenticado en la sesión HTTP y si tiene el
     * rol ADMIN. Lanza WebApplicationException si la verificación falla.
     *
     * @param request La HttpServletRequest actual (obtenida vía @Context).
     * @return El ID del usuario administrador autenticado.
     * @throws WebApplicationException con estado 401 (Unauthorized) o 403
     * (Forbidden).
     */
    private Integer verificarAccesoAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            log.warn("Intento de acceso a recurso admin sin sesión activa.");
            throw new WebApplicationException("No hay sesión activa. Por favor, inicie sesión.", Response.Status.UNAUTHORIZED);
        }

        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (userId == null || userRole == null) {
            log.warn("Intento de acceso a recurso admin con sesión inválida (faltan atributos userId o userRole). Sesión ID: {}", session.getId());
            session.invalidate();
            throw new WebApplicationException("Sesión inválida. Por favor, inicie sesión de nuevo.", Response.Status.UNAUTHORIZED);
        }

        // Verificar si el rol es ADMIN
        if (!RolUsuario.ADMIN.name().equals(userRole)) {
            log.warn("Usuario ID {} con rol {} intentó acceder a recurso de admin.", userId, userRole);
            throw new WebApplicationException("Acceso denegado. Se requiere rol ADMIN.", Response.Status.FORBIDDEN);
        }

        log.debug("Acceso permitido para admin ID: {}", userId);
        return userId;
    }
}
