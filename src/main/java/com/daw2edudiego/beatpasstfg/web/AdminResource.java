package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.*;
import com.daw2edudiego.beatpasstfg.exception.*;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.*;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Recurso JAX-RS para el panel de Administración (/api/admin). Gestiona
 * Usuarios, Festivales, Asistentes y Pulseras. Requiere rol ADMIN en sesión
 * HTTP. Devuelve principalmente HTML (JSPs).
 */
@Path("/admin")
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    private final UsuarioService usuarioService;
    private final FestivalService festivalService;
    private final AsistenteService asistenteService;
    private final PulseraNFCService pulseraNFCService;

    @Context
    private UriInfo uriInfo;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    public AdminResource() {
        this.usuarioService = new UsuarioServiceImpl();
        this.festivalService = new FestivalServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.pulseraNFCService = new PulseraNFCServiceImpl();
    }

    // --- Gestión de Usuarios ---
    @GET
    @Path("/admins/listar")
    @Produces(MediaType.TEXT_HTML)
    public Response listarAdmins() throws ServletException, IOException {
        return listarUsuariosPorRol(RolUsuario.ADMIN, "admins");
    }

    @GET
    @Path("/promotores/listar")
    @Produces(MediaType.TEXT_HTML)
    public Response listarPromotores() throws ServletException, IOException {
        return listarUsuariosPorRol(RolUsuario.PROMOTOR, "promotores");
    }

    @GET
    @Path("/cajeros/listar")
    @Produces(MediaType.TEXT_HTML)
    public Response listarCajeros() throws ServletException, IOException {
        return listarUsuariosPorRol(RolUsuario.CAJERO, "cajeros");
    }

    private Response listarUsuariosPorRol(RolUsuario rol, String activePage) throws ServletException, IOException {
        log.debug("GET /admin/{}/listar", activePage);
        Integer idAdmin = verificarAccesoAdmin(request);
        List<UsuarioDTO> listaUsuarios = usuarioService.obtenerUsuariosPorRol(rol);

        request.setAttribute("usuarios", listaUsuarios);
        request.setAttribute("rolListado", rol.name());
        request.setAttribute("tituloPagina", "Gestionar " + activePage.substring(0, 1).toUpperCase() + activePage.substring(1));
        request.setAttribute("idAdminAutenticado", idAdmin);
        mostrarMensajeFlash(request);

        String jspPath = "/WEB-INF/jsp/admin/admin-" + activePage + ".jsp";
        forwardToJsp(jspPath);
        return Response.ok().build();
    }

    @GET
    @Path("/usuarios/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrearUsuario() throws ServletException, IOException {
        log.debug("GET /admin/usuarios/crear");
        Integer idAdmin = verificarAccesoAdmin(request);

        request.setAttribute("usuario", new UsuarioCreacionDTO());
        request.setAttribute("esNuevo", true);
        request.setAttribute("rolesPosibles", RolUsuario.values());
        request.setAttribute("idAdminAutenticado", idAdmin);

        forwardToJsp("/WEB-INF/jsp/admin/admin-usuario-detalle.jsp");
        return Response.ok().build();
    }

    @GET
    @Path("/usuarios/{idUsuario}/editar")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditarUsuario(@PathParam("idUsuario") Integer idUsuario) throws ServletException, IOException {
        log.debug("GET /admin/usuarios/{}/editar", idUsuario);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idUsuario == null) {
            throw new BadRequestException("ID Usuario no válido.");
        }

        try {
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorId(idUsuario)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + idUsuario));

            request.setAttribute("usuario", usuario);
            request.setAttribute("idAdminAutenticado", idAdmin);
            request.setAttribute("esNuevo", false);
            mostrarMensajeFlash(request);

            forwardToJsp("/WEB-INF/jsp/admin/admin-usuario-detalle.jsp");
            return Response.ok().build();

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al mostrar formulario editar usuario ID {}: {}", idUsuario, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del usuario para editar.", e);
        }
    }

    @POST
    @Path("/usuarios/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarUsuario(
            @FormParam("idUsuario") Integer idUsuario,
            @FormParam("nombre") String nombre,
            @FormParam("email") String email,
            @FormParam("password") String password,
            @FormParam("rol") String rolStr
    ) throws ServletException, IOException {

        Integer idAdmin = verificarAccesoAdmin(request);
        boolean esActualizacion = (idUsuario != null && idUsuario > 0);
        log.info("POST /admin/usuarios/guardar. Modo: {}", esActualizacion ? "ACTUALIZACIÓN (ID: " + idUsuario + ")" : "CREACIÓN");

        if (esActualizacion) {
            // Lógica de Actualización (solo nombre)
            String mensajeFlash = null;
            String errorFlash = null;
            UsuarioDTO dtoParaForm = null;
            URI redirectUri = getListUriForRole(RolUsuario.PROMOTOR); // Default redirect

            try {
                if (nombre == null || nombre.isBlank()) {
                    throw new IllegalArgumentException("El nombre del usuario es obligatorio.");
                }
                UsuarioDTO actualizado = usuarioService.actualizarNombreUsuario(idUsuario, nombre);
                mensajeFlash = "Usuario '" + actualizado.getNombre() + "' (ID: " + idUsuario + ") actualizado.";
                redirectUri = getListUriForRole(actualizado.getRol()); // Redirect a la lista correcta

            } catch (IllegalArgumentException | UsuarioNotFoundException e) {
                errorFlash = "Error al actualizar: " + e.getMessage();
                Optional<UsuarioDTO> originalOpt = usuarioService.obtenerUsuarioPorId(idUsuario);
                if (originalOpt.isPresent()) {
                    dtoParaForm = originalOpt.get();
                    dtoParaForm.setNombre(nombre); // Mantener el intento fallido
                    forwardToUsuarioEditFormWithError(dtoParaForm, idAdmin, errorFlash);
                    return Response.ok().build(); // Mostrar form con error
                } else {
                    errorFlash = "Usuario no encontrado (ID: " + idUsuario + ") al intentar mostrar error.";
                }
            } catch (Exception e) {
                errorFlash = "Error interno inesperado al actualizar.";
                log.error("Error interno al actualizar usuario ID {}: {}", idUsuario, e.getMessage(), e);
            }
            setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
            return Response.seeOther(redirectUri).build();

        } else {
            // Lógica de Creación
            UsuarioCreacionDTO dto = new UsuarioCreacionDTO();
            dto.setNombre(nombre);
            dto.setEmail(email);
            dto.setPassword(password);
            RolUsuario rol = null;

            try {
                if (rolStr == null || rolStr.isBlank()) {
                    throw new IllegalArgumentException("El rol es obligatorio.");
                }
                rol = RolUsuario.valueOf(rolStr.toUpperCase());
                dto.setRol(rol);
                if (nombre == null || nombre.isBlank() || email == null || email.isBlank() || password == null || password.isEmpty()) {
                    throw new IllegalArgumentException("Nombre, email y contraseña son obligatorios.");
                }
                if (password.length() < 8) {
                    throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
                }

                UsuarioDTO creado = usuarioService.crearUsuario(dto);
                setFlashMessage(request, "mensaje", "Usuario '" + creado.getNombre() + "' (Rol: " + rol + ") creado.");
                URI listUri = getListUriForRole(rol);
                return Response.seeOther(listUri).build();

            } catch (EmailExistenteException | IllegalArgumentException e) {
                log.warn("Error de negocio/validación al crear usuario: {}", e.getMessage());
                forwardToUsuarioCreateFormWithError(dto, idAdmin, e.getMessage());
                return Response.ok().build(); // Mostrar form con error
            } catch (Exception e) {
                log.error("Error interno inesperado al crear usuario: {}", e.getMessage(), e);
                throw new InternalServerErrorException("Error interno al crear el usuario.", e);
            }
        }
    }

    @POST
    @Path("/usuarios/cambiar-estado")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response cambiarEstadoUsuario(
            @FormParam("idUsuario") Integer idUsuario,
            @FormParam("nuevoEstado") Boolean nuevoEstado) {

        log.info("POST /admin/usuarios/cambiar-estado para ID: {} a {}", idUsuario, nuevoEstado);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idUsuario == null || nuevoEstado == null) {
            throw new BadRequestException("Faltan parámetros requeridos (idUsuario, nuevoEstado).");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        UsuarioDTO actualizado = null;

        try {
            actualizado = usuarioService.actualizarEstadoUsuario(idUsuario, nuevoEstado);
            mensajeFlash = "Estado del usuario '" + actualizado.getNombre() + "' actualizado a " + (nuevoEstado ? "ACTIVO" : "INACTIVO") + ".";
        } catch (UsuarioNotFoundException e) {
            errorFlash = e.getMessage();
        } catch (Exception e) {
            log.error("Error interno al cambiar estado del usuario ID {}: {}", idUsuario, e.getMessage(), e);
            errorFlash = "Error interno al cambiar el estado.";
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI listUri = (actualizado != null) ? getListUriForRole(actualizado.getRol()) : getListUriForRole(RolUsuario.PROMOTOR); // Fallback
        return Response.seeOther(listUri).build();
    }

    // --- Gestión de Festivales ---
    @GET
    @Path("/promotores/{idPromotor}/festivales")
    @Produces(MediaType.TEXT_HTML)
    public Response listarFestivalesDePromotor(@PathParam("idPromotor") Integer idPromotor) throws ServletException, IOException {
        log.debug("GET /admin/promotores/{}/festivales", idPromotor);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idPromotor == null) {
            throw new BadRequestException("ID de Promotor no proporcionado.");
        }

        try {
            UsuarioDTO promotor = usuarioService.obtenerUsuarioPorId(idPromotor)
                    .filter(u -> u.getRol() == RolUsuario.PROMOTOR)
                    .orElseThrow(() -> new NotFoundException("Promotor no encontrado con ID: " + idPromotor));
            List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);

            request.setAttribute("promotor", promotor);
            request.setAttribute("festivales", listaFestivales);
            request.setAttribute("idAdminAutenticado", idAdmin);
            mostrarMensajeFlash(request);

            forwardToJsp("/WEB-INF/jsp/admin/admin-promotor-festivales.jsp");
            return Response.ok().build();

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener festivales para promotor ID {}: {}", idPromotor, e.getMessage(), e);
            throw new InternalServerErrorException("Error interno al cargar los festivales del promotor.", e);
        }
    }

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarDashboard() {
        log.debug("GET /admin/dashboard");
        verificarAccesoAdmin(request);
        log.info("Dashboard Admin no implementado, redirigiendo a lista de promotores.");
        return Response.seeOther(getListUriForRole(RolUsuario.PROMOTOR)).build();
    }

    @GET
    @Path("/festivales/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrearFestival() throws ServletException, IOException {
        log.debug("GET /admin/festivales/crear");
        Integer idAdmin = verificarAccesoAdmin(request);

        List<UsuarioDTO> promotoresActivos = usuarioService.obtenerUsuariosPorRol(RolUsuario.PROMOTOR)
                .stream().filter(UsuarioDTO::getEstado).collect(Collectors.toList());

        request.setAttribute("festival", new FestivalDTO());
        request.setAttribute("promotores", promotoresActivos);
        request.setAttribute("esNuevo", true);
        request.setAttribute("idAdminAutenticado", idAdmin);

        forwardToJsp("/WEB-INF/jsp/admin/admin-festival-detalle.jsp");
        return Response.ok().build();
    }

    @POST
    @Path("/festivales/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarFestivalAdmin(
            @FormParam("nombre") String nombre, @FormParam("descripcion") String descripcion,
            @FormParam("fechaInicio") String fechaInicioStr, @FormParam("fechaFin") String fechaFinStr,
            @FormParam("ubicacion") String ubicacion, @FormParam("aforo") String aforoStr,
            @FormParam("imagenUrl") String imagenUrl, @FormParam("idPromotorSeleccionado") Integer idPromotorSeleccionado
    ) throws ServletException, IOException {

        log.info("POST /admin/festivales/guardar (CREACIÓN Admin) para promotor ID: {}", idPromotorSeleccionado);
        Integer idAdmin = verificarAccesoAdmin(request);

        FestivalDTO dto = new FestivalDTO();
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setUbicacion(ubicacion);
        dto.setImagenUrl(imagenUrl);
        dto.setIdPromotor(idPromotorSeleccionado);

        try {
            if (idPromotorSeleccionado == null) {
                throw new IllegalArgumentException("Debe seleccionar un promotor.");
            }
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("Nombre es obligatorio.");
            }
            if (fechaInicioStr == null || fechaFinStr == null || fechaInicioStr.isBlank() || fechaFinStr.isBlank()) {
                throw new IllegalArgumentException("Fechas de inicio y fin son obligatorias.");
            }
            dto.setFechaInicio(LocalDate.parse(fechaInicioStr));
            dto.setFechaFin(LocalDate.parse(fechaFinStr));
            if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
                throw new IllegalArgumentException("Fecha fin no puede ser anterior a fecha inicio.");
            }
            if (aforoStr != null && !aforoStr.isBlank()) {
                dto.setAforo(Integer.parseInt(aforoStr));
                if (dto.getAforo() <= 0) {
                    throw new IllegalArgumentException("Aforo debe ser positivo.");
                }
            }

            FestivalDTO creado = festivalService.crearFestival(dto, idPromotorSeleccionado);
            setFlashMessage(request, "mensaje", "Festival '" + creado.getNombre() + "' creado y asignado (estado BORRADOR).");
            URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarTodosFestivales").build();
            return Response.seeOther(listUri).build();

        } catch (NumberFormatException e) {
            log.warn("Error de formato numérico al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Formato de número inválido (ej: en Aforo).");
            return Response.ok().build();
        } catch (DateTimeParseException e) {
            log.warn("Error de formato de fecha al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Formato de fecha inválido (use WebView-MM-dd).");
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
        log.debug("GET /admin/festivales/listar-todos. Filtro estado: '{}'", estadoFilter);
        Integer idAdmin = verificarAccesoAdmin(request);

        List<FestivalDTO> listaFestivales;
        EstadoFestival estadoEnum = null;
        String errorFiltro = null;

        try {
            if (estadoFilter != null && !estadoFilter.isBlank()) {
                estadoEnum = EstadoFestival.valueOf(estadoFilter.toUpperCase());
            }
            listaFestivales = festivalService.obtenerFestivalesPorEstado(estadoEnum);
        } catch (IllegalArgumentException e) {
            errorFiltro = "Estado de filtro inválido: '" + estadoFilter + "'. Mostrando todos.";
            listaFestivales = festivalService.obtenerTodosLosFestivales();
        } catch (Exception e) {
            log.error("Error obteniendo lista de festivales para admin: {}", e.getMessage(), e);
            request.setAttribute("error", "Error crítico al cargar la lista.");
            listaFestivales = Collections.emptyList();
        }

        request.setAttribute("festivales", listaFestivales);
        request.setAttribute("idAdminAutenticado", idAdmin);
        request.setAttribute("estadoFiltro", estadoFilter);
        request.setAttribute("estadosPosibles", EstadoFestival.values());
        if (errorFiltro != null) {
            request.setAttribute("error", errorFiltro);
        }
        mostrarMensajeFlash(request);

        forwardToJsp("/WEB-INF/jsp/admin/admin-festivales.jsp");
        return Response.ok().build();
    }

    @POST
    @Path("/festivales/confirmar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response confirmarFestival(@FormParam("idFestival") Integer idFestival) {
        log.info("POST /admin/festivales/confirmar para ID: {}", idFestival);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idFestival == null) {
            throw new BadRequestException("Falta idFestival.");
        }

        String mensajeFlash = null;
        String errorFlash = null;

        try {
            FestivalDTO confirmado = festivalService.cambiarEstadoFestival(idFestival, EstadoFestival.PUBLICADO, idAdmin);
            mensajeFlash = "Festival '" + confirmado.getNombre() + "' confirmado y publicado.";
        } catch (FestivalNotFoundException | UsuarioNotFoundException | SecurityException | IllegalStateException e) {
            errorFlash = "No se pudo confirmar: " + e.getMessage();
        } catch (Exception e) {
            log.error("Error interno al confirmar festival ID {}: {}", idFestival, e.getMessage(), e);
            errorFlash = "Error interno al confirmar.";
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarTodosFestivales").build();
        return Response.seeOther(listUri).build();
    }

    @POST
    @Path("/festivales/cambiar-estado")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response cambiarEstadoFestivalAdmin(
            @FormParam("idFestival") Integer idFestival,
            @FormParam("nuevoEstado") String nuevoEstadoStr) {

        log.info("POST /admin/festivales/cambiar-estado para ID: {} a {}", idFestival, nuevoEstadoStr);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idFestival == null || nuevoEstadoStr == null || nuevoEstadoStr.isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos (idFestival, nuevoEstado).");
        }

        EstadoFestival nuevoEstado;
        try {
            nuevoEstado = EstadoFestival.valueOf(nuevoEstadoStr.toUpperCase());
            if (nuevoEstado != EstadoFestival.CANCELADO && nuevoEstado != EstadoFestival.FINALIZADO) {
                throw new IllegalArgumentException("Solo se permite cambiar a CANCELADO o FINALIZADO.");
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Valor de 'nuevoEstado' inválido. Use CANCELADO o FINALIZADO.");
        }

        String mensajeFlash = null;
        String errorFlash = null;

        try {
            FestivalDTO actualizado = festivalService.cambiarEstadoFestival(idFestival, nuevoEstado, idAdmin);
            mensajeFlash = "Estado del festival '" + actualizado.getNombre() + "' cambiado a " + nuevoEstado + ".";
        } catch (FestivalNotFoundException | UsuarioNotFoundException | SecurityException | IllegalStateException e) {
            errorFlash = "No se pudo cambiar el estado: " + e.getMessage();
        } catch (Exception e) {
            log.error("Error interno al cambiar estado de festival ID {}: {}", idFestival, e.getMessage(), e);
            errorFlash = "Error interno al cambiar estado.";
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarTodosFestivales").build();
        return Response.seeOther(listUri).build();
    }

    // --- Gestión de Asistentes ---
    @GET
    @Path("/asistentes")
    @Produces(MediaType.TEXT_HTML)
    public Response listarAsistentes(@QueryParam("buscar") String searchTerm) throws ServletException, IOException {
        log.debug("GET /admin/asistentes. Término búsqueda: '{}'", searchTerm);
        Integer idAdmin = verificarAccesoAdmin(request);
        List<AsistenteDTO> listaAsistentes;
        try {
            listaAsistentes = asistenteService.buscarAsistentes(searchTerm);
        } catch (Exception e) {
            log.error("Error obteniendo lista de asistentes: {}", e.getMessage(), e);
            request.setAttribute("error", "Error al cargar la lista.");
            listaAsistentes = Collections.emptyList();
        }
        request.setAttribute("asistentes", listaAsistentes);
        request.setAttribute("idAdminAutenticado", idAdmin);
        request.setAttribute("searchTerm", searchTerm);
        mostrarMensajeFlash(request);
        forwardToJsp("/WEB-INF/jsp/admin/admin-asistentes.jsp");
        return Response.ok().build();
    }

    @GET
    @Path("/asistentes/{idAsistente}")
    @Produces(MediaType.TEXT_HTML)
    public Response verDetalleAsistente(@PathParam("idAsistente") Integer idAsistente) throws ServletException, IOException {
        log.debug("GET /admin/asistentes/{}", idAsistente);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no proporcionado.");
        }

        try {
            AsistenteDTO asistente = asistenteService.obtenerAsistentePorId(idAsistente)
                    .orElseThrow(() -> new NotFoundException("Asistente no encontrado con ID: " + idAsistente));
            request.setAttribute("asistente", asistente);
            request.setAttribute("idAdminAutenticado", idAdmin);
            request.setAttribute("editMode", false);
            mostrarMensajeFlash(request);
            forwardToJsp("/WEB-INF/jsp/admin/admin-asistente-detalle.jsp");
            return Response.ok().build();
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al obtener detalles del asistente ID {}: {}", idAsistente, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar detalles del asistente.", e);
        }
    }

    @GET
    @Path("/asistentes/{idAsistente}/editar")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditarAsistente(@PathParam("idAsistente") Integer idAsistente) throws ServletException, IOException {
        log.debug("GET /admin/asistentes/{}/editar", idAsistente);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no válido.");
        }

        try {
            AsistenteDTO asistente = asistenteService.obtenerAsistentePorId(idAsistente)
                    .orElseThrow(() -> new NotFoundException("Asistente no encontrado con ID: " + idAsistente));
            request.setAttribute("asistente", asistente);
            request.setAttribute("idAdminAutenticado", idAdmin);
            request.setAttribute("editMode", true);
            mostrarMensajeFlash(request);
            forwardToJsp("/WEB-INF/jsp/admin/admin-asistente-detalle.jsp");
            return Response.ok().build();
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al mostrar formulario editar asistente ID {}: {}", idAsistente, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del asistente para editar.", e);
        }
    }

    @POST
    @Path("/asistentes/{idAsistente}/actualizar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response actualizarAsistente(
            @PathParam("idAsistente") Integer idAsistente,
            @FormParam("nombre") String nombre,
            @FormParam("telefono") String telefono) throws ServletException, IOException {

        log.info("POST /admin/asistentes/{}/actualizar", idAsistente);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no válido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        AsistenteDTO dtoParaForm = new AsistenteDTO(); // Para posible reenvío

        try {
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre del asistente es obligatorio.");
            }
            AsistenteDTO dtoActualizar = new AsistenteDTO();
            dtoActualizar.setNombre(nombre);
            dtoActualizar.setTelefono(telefono);

            AsistenteDTO actualizado = asistenteService.actualizarAsistente(idAsistente, dtoActualizar);
            mensajeFlash = "Asistente '" + actualizado.getNombre() + "' (ID: " + idAsistente + ") actualizado.";

        } catch (IllegalArgumentException | AsistenteNotFoundException e) {
            errorFlash = "Error al actualizar: " + e.getMessage();
            Optional<AsistenteDTO> originalOpt = asistenteService.obtenerAsistentePorId(idAsistente);
            if (originalOpt.isPresent()) {
                dtoParaForm = originalOpt.get();
                dtoParaForm.setNombre(nombre); // Mantener intento fallido
                dtoParaForm.setTelefono(telefono);
                forwardToAsistenteFormWithError(dtoParaForm, idAdmin, errorFlash);
                return Response.ok().build();
            } else {
                errorFlash = "Asistente no encontrado (ID: " + idAsistente + ") al intentar mostrar error.";
            }
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al actualizar.";
            log.error("Error interno al actualizar asistente ID {}: {}", idAsistente, e.getMessage(), e);
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarAsistentes").build();
        return Response.seeOther(listUri).build();
    }

    // --- Gestión de Pulseras NFC ---
    @GET
    @Path("/festivales/{idFestival}/pulseras")
    @Produces(MediaType.TEXT_HTML)
    public Response listarPulserasPorFestivalAdmin(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /admin/festivales/{}/pulseras", idFestival);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idFestival == null) {
            throw new BadRequestException("ID festival no válido.");
        }

        try {
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .orElseThrow(() -> new NotFoundException("Festival no encontrado con ID: " + idFestival));
            // Admin puede ver pulseras de cualquier festival
            List<PulseraNFCDTO> listaPulseras = pulseraNFCService.obtenerPulserasPorFestival(idFestival, idAdmin);

            request.setAttribute("festival", festival);
            request.setAttribute("pulseras", listaPulseras);
            request.setAttribute("idAdminAutenticado", idAdmin);
            mostrarMensajeFlash(request);

            forwardToJsp("/WEB-INF/jsp/admin/admin-festival-pulseras.jsp");
            return Response.ok().build();

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar pulseras para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar las pulseras del festival.", e);
        }
    }

    // --- Métodos Auxiliares ---
    /**
     * Verifica acceso ADMIN y devuelve su ID. Lanza excepciones JAX-RS si
     * falla.
     */
    private Integer verificarAccesoAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("Intento de acceso a recurso Admin sin sesión activa.");
            throw new NotAuthorizedException("No hay sesión activa.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        if (userId == null || userRole == null) {
            log.warn("Intento de acceso a recurso Admin con sesión inválida. Sesión ID: {}", session.getId());
            session.invalidate();
            throw new NotAuthorizedException("Sesión inválida.", Response.status(Response.Status.UNAUTHORIZED).build());
        }
        if (!RolUsuario.ADMIN.name().equals(userRole)) {
            log.warn("Usuario ID {} con rol {} intentó acceder a recurso Admin.", userId, userRole);
            throw new ForbiddenException("Acceso denegado. Se requiere rol ADMIN.");
        }
        log.debug("Acceso permitido para admin ID: {}", userId);
        return userId;
    }

    /**
     * Realiza forward a un JSP.
     */
    private void forwardToJsp(String jspPath) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
        dispatcher.forward(request, response);
    }

    /**
     * Guarda mensaje flash en sesión.
     */
    private void setFlashMessage(HttpServletRequest request, String type, String message) {
        if (message != null) {
            HttpSession session = request.getSession(); // Obtener o crear
            session.setAttribute(type, message);
            log.trace("Mensaje flash '{}' guardado en sesión con clave '{}'", message, type);
        }
    }

    /**
     * Mueve mensajes flash de sesión a request.
     */
    private void mostrarMensajeFlash(HttpServletRequest request) {
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

    /**
     * Obtiene la URI de la lista de usuarios según el rol.
     */
    private URI getListUriForRole(RolUsuario rol) {
        String methodName;
        switch (rol) {
            case ADMIN:
                methodName = "listarAdmins";
                break;
            case PROMOTOR:
                methodName = "listarPromotores";
                break;
            case CAJERO:
                methodName = "listarCajeros";
                break;
            default:
                methodName = "listarPromotores"; // Fallback
                log.warn("Rol inesperado {} al determinar URI. Usando lista promotores.", rol);
        }
        return uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, methodName).build();
    }

    /**
     * Forward a form de creación de usuario con error.
     */
    private void forwardToUsuarioCreateFormWithError(UsuarioCreacionDTO dto, Integer idAdmin, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        dto.setPassword(""); // Clear password
        request.setAttribute("usuario", dto);
        request.setAttribute("esNuevo", true);
        request.setAttribute("rolesPosibles", RolUsuario.values());
        request.setAttribute("idAdminAutenticado", idAdmin);
        forwardToJsp("/WEB-INF/jsp/admin/admin-usuario-detalle.jsp");
    }

    /**
     * Forward a form de edición de usuario con error.
     */
    private void forwardToUsuarioEditFormWithError(UsuarioDTO dto, Integer idAdmin, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.setAttribute("usuario", dto);
        request.setAttribute("esNuevo", false);
        request.setAttribute("idAdminAutenticado", idAdmin);
        forwardToJsp("/WEB-INF/jsp/admin/admin-usuario-detalle.jsp");
    }

    /**
     * Forward a form de creación/edición de festival con error.
     */
    private void forwardToFestivalFormWithError(FestivalDTO dto, Integer idAdmin, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.setAttribute("festival", dto);
        request.setAttribute("esNuevo", true); // Assume error occurred during creation
        List<UsuarioDTO> promotoresActivos = usuarioService.obtenerUsuariosPorRol(RolUsuario.PROMOTOR)
                .stream().filter(UsuarioDTO::getEstado).collect(Collectors.toList());
        request.setAttribute("promotores", promotoresActivos);
        request.setAttribute("idAdminAutenticado", idAdmin);
        forwardToJsp("/WEB-INF/jsp/admin/admin-festival-detalle.jsp");
    }

    /**
     * Forward a form de edición de asistente con error.
     */
    private void forwardToAsistenteFormWithError(AsistenteDTO dto, Integer idAdmin, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.setAttribute("asistente", dto);
        request.setAttribute("editMode", true); // Estamos en modo edición
        request.setAttribute("idAdminAutenticado", idAdmin);
        forwardToJsp("/WEB-INF/jsp/admin/admin-asistente-detalle.jsp");
    }

}
