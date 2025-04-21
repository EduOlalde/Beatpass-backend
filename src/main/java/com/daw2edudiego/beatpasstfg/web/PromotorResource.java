/*
 * Recurso JAX-RS para el panel web del Promotor.
 * Gestiona la visualización y edición de los festivales propios del promotor.
 * La autenticación y autorización se basa en Sesión HTTP.
 * También incluye los endpoints para el cambio de contraseña obligatorio.
 * ACTUALIZADO: Promotor NO puede cambiar estado de festival. Eliminado endpoint PUT /estado.
 * Método guardarFestival ya no procesa el parámetro de estado.
 */
package com.daw2edudiego.beatpasstfg.web;

// Imports DTOs, Excepciones, Modelo, Servicios
import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
// import com.daw2edudiego.beatpasstfg.model.Usuario; // No se usa directamente aquí
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
import jakarta.ws.rs.core.Context; // Inyección de contexto
import jakarta.ws.rs.core.MediaType; // Tipos MIME
import jakarta.ws.rs.core.Response; // Respuesta HTTP
import jakarta.ws.rs.core.UriInfo; // Información de URI

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Clases estándar Java
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
// import java.util.Optional; // No se usa directamente aquí

/**
 * Recurso JAX-RS (@Path) para el panel web del Promotor (/api/promotor). Define
 * endpoints para que el promotor gestione sus festivales (listar, crear,
 * editar) y maneje el cambio de contraseña obligatorio. La seguridad se basa en
 * verificar la sesión HTTP y el rol PROMOTOR. Los métodos suelen hacer forward
 * a JSPs.
 */
@Path("/promotor") // Ruta base: /api/promotor
public class PromotorResource {

    private static final Logger log = LoggerFactory.getLogger(PromotorResource.class);

    // Inyección de dependencias (manual)
    private final FestivalService festivalService;
    private final UsuarioService usuarioService;

    // Inyección de contexto JAX-RS
    @Context
    private UriInfo uriInfo;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    // Constructor
    public PromotorResource() {
        this.festivalService = new FestivalServiceImpl();
        this.usuarioService = new UsuarioServiceImpl();
    }

    // --- Endpoints para Gestión de Festivales del Promotor ---
    @GET
    @Path("/festivales")
    @Produces(MediaType.TEXT_HTML)
    public Response listarFestivales() throws ServletException, IOException {
        // ... (sin cambios) ...
        log.debug("GET /promotor/festivales (listar) recibido");
        Integer idPromotor = verificarAccesoPromotor(request);

        log.debug("Listando festivales para Promotor ID: {}", idPromotor);
        List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);
        request.setAttribute("festivales", listaFestivales);
        request.setAttribute("idPromotorAutenticado", idPromotor);

        mostrarMensajeFlash(request); // Usar helper

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/mis-festivales.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    @GET
    @Path("/festivales/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrear() throws ServletException, IOException {
        // ... (sin cambios) ...
        log.debug("GET /promotor/festivales/crear recibido");
        Integer idPromotor = verificarAccesoPromotor(request);

        log.debug("Mostrando formulario de creación para Promotor ID: {}", idPromotor);
        FestivalDTO festival = new FestivalDTO();
        request.setAttribute("festival", festival);
        request.setAttribute("idPromotorAutenticado", idPromotor);
        request.setAttribute("esNuevo", true);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/festival-detalle.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    @GET
    @Path("/festivales/ver/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditar(@PathParam("id") Integer idFestivalParam) throws ServletException, IOException {
        // ... (sin cambios respecto a la versión anterior corregida) ...
        log.debug("GET /promotor/festivales/ver/{} recibido", idFestivalParam);
        Integer idPromotor = verificarAccesoPromotor(request);
        final Integer idFestival = idFestivalParam;

        try {
            log.debug("Buscando festival con ID: {}", idFestival);
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .orElseThrow(() -> new FestivalNotFoundException("Festival no encontrado con ID: " + idFestival));

            if (!festival.getIdPromotor().equals(idPromotor)) {
                log.warn("Intento no autorizado de ver/editar festival ID {} por promotor ID {}", idFestival, idPromotor);
                throw new ForbiddenException("No tiene permiso para acceder a este festival");
            }

            log.debug("Mostrando formulario para editar festival ID: {}", idFestival);
            request.setAttribute("festival", festival);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            request.setAttribute("esNuevo", false);
            // No pasar estadosPosibles ya que no puede cambiarlo
            // request.setAttribute("estadosPosibles", EstadoFestival.values());

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/festival-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al mostrar formulario de edición para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del festival", e);
        }
    }

    @POST
    @Path("/festivales/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarFestival(
            @FormParam("idFestival") String idStr, @FormParam("nombre") String nombre,
            @FormParam("descripcion") String descripcion, @FormParam("fechaInicio") String fechaInicioStr,
            @FormParam("fechaFin") String fechaFinStr, @FormParam("ubicacion") String ubicacion,
            @FormParam("aforo") String aforoStr, @FormParam("imagenUrl") String imagenUrl
    // *** CAMBIO: Eliminado @FormParam("estado") String estadoStr ***
    ) throws ServletException, IOException {

        log.info("POST /promotor/festivales/guardar recibido");
        Integer idPromotor = verificarAccesoPromotor(request);

        FestivalDTO dto = new FestivalDTO();
        boolean esNuevo = (idStr == null || idStr.isEmpty() || "0".equals(idStr));
        Integer idFestival = null;
        String errorMessage = null;

        try {
            // --- Validación y Población del DTO ---
            if (!esNuevo) {
                idFestival = Integer.parseInt(idStr);
                dto.setIdFestival(idFestival);
            }
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("Nombre obligatorio");
            }
            dto.setNombre(nombre);
            dto.setDescripcion(descripcion);
            dto.setUbicacion(ubicacion);
            dto.setImagenUrl(imagenUrl);
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
            // *** CAMBIO: No procesar ni establecer estado desde el form ***
            // El estado se establece a BORRADOR en crearFestival y solo lo cambia el Admin
            // --- Fin Validación ---

            // --- Lógica de Guardado ---
            String mensajeExito;
            final Integer idFestivalFinal = idFestival; // Para lambda
            if (esNuevo) {
                log.info("Llamando a crearFestival para promotor ID: {}", idPromotor);
                // El servicio pondrá estado BORRADOR por defecto
                FestivalDTO creado = festivalService.crearFestival(dto, idPromotor);
                mensajeExito = "Solicitud de festival '" + creado.getNombre() + "' creada con éxito (estado BORRADOR).";
            } else {
                log.info("Llamando a actualizarFestival ID: {} para promotor ID: {}", idFestivalFinal, idPromotor);
                // El servicio actualizará los datos, pero NO el estado si lo hace un promotor
                festivalService.actualizarFestival(idFestivalFinal, dto, idPromotor);
                mensajeExito = "Festival '" + dto.getNombre() + "' actualizado con éxito.";
                // *** CAMBIO: Eliminada la lógica para llamar a cambiarEstadoFestival ***
            }

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("mensaje", mensajeExito);
            }

            URI listUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build();
            log.debug("Guardado exitoso. Redirigiendo a: {}", listUri);
            return Response.seeOther(listUri).build();

        } catch (NumberFormatException e) {
            errorMessage = "Formato de número inválido (ej: en Aforo o ID).";
            log.warn("Error al guardar festival (promotor {}): {}", idPromotor, errorMessage);
        } catch (DateTimeParseException e) {
            errorMessage = "Formato de fecha inválido (use yyyy-MM-dd).";
            log.warn("Error al guardar festival (promotor {}): {}", idPromotor, errorMessage);
        } catch (IllegalArgumentException | FestivalNotFoundException | SecurityException | IllegalStateException e) {
            errorMessage = e.getMessage();
            log.warn("Error al guardar festival (promotor {}): {}", idPromotor, errorMessage);
        } catch (Exception e) {
            errorMessage = "Error interno inesperado al guardar.";
            log.error("Error interno al guardar festival (promotor {}): {}", idPromotor, e.getMessage(), e);
        }

        log.debug("Error detectado, haciendo forward de vuelta al formulario con mensaje: {}", errorMessage);
        forwardToPromotorFestivalFormWithError(dto, idPromotor, !esNuevo, errorMessage);
        return Response.ok().build();
    }

    // *** CAMBIO: Eliminado el endpoint PUT /festivales/{id}/estado ***
    // @PUT
    // @Path("/{id}/estado")
    // public Response cambiarEstadoFestival(...) { ... }
    // --- Endpoints para Cambio de Contraseña Obligatorio (sin cambios) ---
    @GET
    @Path("/mostrar-cambio-password")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCambioPassword() throws ServletException, IOException {
        // ... (sin cambios) ...
        log.debug("GET /promotor/mostrar-cambio-password recibido");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            log.warn("Intento de acceso a mostrar-cambio-password sin sesión válida.");
            try {
                return Response.seeOther(new URI(request.getContextPath() + "/login?error=session_required")).build();
            } catch (URISyntaxException e) {
                return Response.serverError().build();
            }
        }
        Integer userId = (Integer) session.getAttribute("userId");
        log.debug("Mostrando formulario de cambio de contraseña obligatorio para userId: {}", userId);

        mostrarMensajeFlash(request);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/cambiar-password-obligatorio.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    @POST
    @Path("/cambiar-password-obligatorio")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response procesarCambioPasswordObligatorio(
            @FormParam("newPassword") String newPassword,
            @FormParam("confirmPassword") String confirmPassword) {
        // ... (sin cambios) ...
        HttpSession session = request.getSession(false);
        String errorMessage = null;

        if (session == null || session.getAttribute("userId") == null) {
            log.error("Intento de cambiar contraseña obligatorio sin sesión o userId válido.");
            try {
                return Response.seeOther(new URI(request.getContextPath() + "/login?error=session_expired")).build();
            } catch (URISyntaxException e) {
                return Response.serverError().build();
            }
        }
        Integer userId = (Integer) session.getAttribute("userId");
        log.info("POST /promotor/cambiar-password-obligatorio para userId: {}", userId);

        if (newPassword == null || newPassword.isEmpty() || !newPassword.equals(confirmPassword)) {
            errorMessage = "Las contraseñas no coinciden o están vacías.";
            log.warn("Error validación contraseña obligatoria (userId: {}): {}", userId, errorMessage);
            session.setAttribute("passwordChangeError", errorMessage);
            return redirectBackToChangePasswordForm();
        }
        if (newPassword.length() < 8) {
            errorMessage = "La nueva contraseña debe tener al menos 8 caracteres.";
            log.warn("Error complejidad contraseña obligatoria (userId: {}): {}", userId, errorMessage);
            session.setAttribute("passwordChangeError", errorMessage);
            return redirectBackToChangePasswordForm();
        }

        try {
            log.debug("Llamando a usuarioService.cambiarPasswordYMarcarActualizada para userId: {}", userId);
            usuarioService.cambiarPasswordYMarcarActualizada(userId, newPassword);
            log.info("Contraseña obligatoria cambiada y flag actualizado para userId: {}", userId);

            session.removeAttribute("passwordChangeError");
            session.setAttribute("mensaje", "Contraseña actualizada correctamente. ¡Bienvenido!");

            String dashboardUrl = determineDashboardUrlFromRole(RolUsuario.PROMOTOR.name());
            log.debug("Redirigiendo a dashboard del promotor: {}", dashboardUrl);
            URI redirectUri = new URI(request.getContextPath() + dashboardUrl);
            return Response.seeOther(redirectUri).build();

        } catch (Exception e) {
            log.error("Error al actualizar contraseña obligatoria para userId {}: {}", userId, e.getMessage(), e);
            session.setAttribute("passwordChangeError", "Error al guardar: " + e.getMessage());
            return redirectBackToChangePasswordForm();
        }
    }

    private Response redirectBackToChangePasswordForm() {
        try {
            URI formUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarFormularioCambioPassword")
                    .build();
            log.debug("Redirigiendo de vuelta al formulario de cambio de contraseña: {}", formUri);
            return Response.seeOther(formUri).build();
        } catch (Exception e) {
            log.error("Error al crear URI para redirección a cambiar password obligatorio: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno al intentar redirigir al formulario.")
                    .build();
        }
    }

    private String determineDashboardUrlFromRole(String roleName) {
        if (RolUsuario.PROMOTOR.name().equalsIgnoreCase(roleName)) {
            return "/api/promotor/festivales";
        }
        log.warn("Rol inesperado '{}' al determinar URL de dashboard desde PromotorResource.", roleName);
        return "/login?error=unexpected_role";
    }

    // --- Método Auxiliar de Seguridad (Verifica Sesión y Rol PROMOTOR) ---
    private Integer verificarAccesoPromotor(HttpServletRequest request) {
        // ... (sin cambios) ...
        HttpSession session = request.getSession(false);
        log.trace("Verificando acceso Promotor. ¿Sesión existe?: {}", (session != null));
        if (session == null) {
            log.warn("Intento de acceso a recurso Promotor sin sesión activa.");
            throw new NotAuthorizedException("No hay sesión activa. Por favor, inicie sesión.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        log.trace("Atributos de sesión encontrados: userId={}, userRole={}", userId, userRole);
        if (userId == null || userRole == null) {
            log.warn("Intento de acceso a recurso Promotor con sesión inválida (faltan atributos). Sesión ID: {}", session.getId());
            session.invalidate();
            throw new NotAuthorizedException("Sesión inválida. Por favor, inicie sesión de nuevo.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        if (!RolUsuario.PROMOTOR.name().equals(userRole)) {
            log.warn("Usuario ID {} con rol {} intentó acceder a recurso de Promotor.", userId, userRole);
            throw new ForbiddenException("Acceso denegado. Se requiere rol PROMOTOR.");
        }
        log.debug("Acceso permitido para promotor ID: {}", userId);
        return userId;
    }

    // --- Método Auxiliar para Forward con Error (Formulario Festival Promotor) ---
    private void forwardToPromotorFestivalFormWithError(FestivalDTO dto, Integer idPromotor, boolean esEdicion, String errorMessage)
            throws ServletException, IOException {
        // ... (sin cambios, pero ya no pasará estadosPosibles) ...
        request.setAttribute("error", errorMessage);
        request.setAttribute("festival", dto);
        request.setAttribute("esNuevo", !esEdicion);
        request.setAttribute("idPromotorAutenticado", idPromotor);
        // Ya no es necesario pasar estadosPosibles si el promotor no puede cambiarlo
        // if (esEdicion) {
        //    request.setAttribute("estadosPosibles", EstadoFestival.values());
        // }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/festival-detalle.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Muestra mensajes flash de la sesión en el request actual.
     */
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
            if (session.getAttribute("passwordChangeError") != null) {
                request.setAttribute("error", session.getAttribute("passwordChangeError"));
                session.removeAttribute("passwordChangeError");
            }
        }
    }
}
