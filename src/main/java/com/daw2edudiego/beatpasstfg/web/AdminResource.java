package com.daw2edudiego.beatpasstfg.web;

// Imports DTOs, Excepciones, Modelo, Servicios
import com.daw2edudiego.beatpasstfg.dto.AsistenteDTO;
import com.daw2edudiego.beatpasstfg.dto.FestivalDTO;
import com.daw2edudiego.beatpasstfg.dto.PulseraNFCDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioCreacionDTO;
import com.daw2edudiego.beatpasstfg.dto.UsuarioDTO;
import com.daw2edudiego.beatpasstfg.exception.AsistenteNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.EmailExistenteException;
import com.daw2edudiego.beatpasstfg.exception.FestivalNotFoundException;
import com.daw2edudiego.beatpasstfg.exception.UsuarioNotFoundException;
import com.daw2edudiego.beatpasstfg.model.EstadoFestival;
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.*; // Importar todos los servicios

// Imports Jakarta EE para Servlets y JAX-RS
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*; // Anotaciones JAX-RS (@Path, @GET, @POST, etc.)
import jakarta.ws.rs.core.Context; // Para inyectar contexto HTTP/Seguridad
import jakarta.ws.rs.core.MediaType; // Tipos MIME (APPLICATION_FORM_URLENCODED, TEXT_HTML)
import jakarta.ws.rs.core.Response; // Respuesta HTTP (ok, seeOther, status)
import jakarta.ws.rs.core.UriInfo; // Información sobre la URI de la petición

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
 * Recurso JAX-RS que define los endpoints para el panel de Administración,
 * accesible bajo la ruta base {@code /api/admin}.
 * <p>
 * Proporciona funcionalidades para que los usuarios con rol
 * {@link RolUsuario#ADMIN} gestionen:
 * <ul>
 * <li>Usuarios Promotores (listar, crear, cambiar estado).</li>
 * <li>Festivales (listar todos, listar por promotor, crear, confirmar/publicar,
 * cambiar estado a cancelado/finalizado).</li>
 * <li>Asistentes (listar, ver detalle, editar).</li>
 * <li>Pulseras NFC (listar por festival).</li>
 * </ul>
 * La autenticación y autorización para acceder a estos endpoints se basa en la
 * validación de una sesión HTTP existente y la verificación de que el usuario
 * autenticado en sesión tenga el rol ADMIN. Las respuestas son principalmente
 * HTML, realizando forwards a archivos JSP ubicados en {@code /WEB-INF/jsp/admin/}.
 * </p>
 *
 * @see UsuarioService
 * @see FestivalService
 * @see AsistenteService
 * @see PulseraNFCService
 * @author Eduardo Olalde
 */
@Path("/admin")
public class AdminResource {

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

    // Inyección manual de dependencias de servicios
    private final UsuarioService usuarioService;
    private final FestivalService festivalService;
    private final AsistenteService asistenteService;
    private final PulseraNFCService pulseraNFCService;

    // Inyección de contexto JAX-RS para acceder a información de la petición HTTP
    @Context
    private UriInfo uriInfo; // Información sobre la URI (para construir URIs de redirección)
    @Context
    private HttpServletRequest request; // Petición HTTP (para acceder a sesión, atributos, dispatcher)
    @Context
    private HttpServletResponse response; // Respuesta HTTP (usada por RequestDispatcher)

    /**
     * Constructor que inicializa las instancias de los servicios necesarios.
     */
    public AdminResource() {
        this.usuarioService = new UsuarioServiceImpl();
        this.festivalService = new FestivalServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.pulseraNFCService = new PulseraNFCServiceImpl();
    }

    // --- Endpoints para Gestión de Promotores por Admin ---
    /**
     * Endpoint GET para listar todos los usuarios con rol PROMOTOR.
     * Realiza forward a un JSP para mostrar la lista.
     * Requiere rol ADMIN en sesión.
     *
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene éxito).
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     * @throws NotAuthorizedException Si no hay sesión activa.
     * @throws ForbiddenException Si el usuario en sesión no es ADMIN.
     */
    @GET
    @Path("/promotores/listar")
    @Produces(MediaType.TEXT_HTML)
    public Response listarPromotores() throws ServletException, IOException {
        log.debug("GET /admin/promotores/listar recibido");
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol

        log.debug("Listando usuarios con rol PROMOTOR para Admin ID: {}", idAdmin);
        List<UsuarioDTO> listaPromotores = usuarioService.obtenerUsuariosPorRol(RolUsuario.PROMOTOR);

        request.setAttribute("promotores", listaPromotores);
        request.setAttribute("idAdminAutenticado", idAdmin); // Para usar en el JSP
        mostrarMensajeFlash(request); // Muestra mensajes de éxito/error de acciones previas

        // Delega la generación de la respuesta HTML al JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotores.jsp");
        dispatcher.forward(request, response);

        // El forward ya maneja la respuesta, devolvemos OK para JAX-RS
        return Response.ok().build();
    }

    /**
     * Endpoint GET alternativo para listar promotores (acceso desde /admin/promotores).
     * Simplemente llama a {@link #listarPromotores()}.
     *
     * @return Respuesta de {@link #listarPromotores()}.
     * @throws ServletException Ver {@link #listarPromotores()}.
     * @throws IOException Ver {@link #listarPromotores()}.
     */
    @GET
    @Path("/promotores")
    @Produces(MediaType.TEXT_HTML)
    public Response listarPromotoresRoot() throws ServletException, IOException {
        return listarPromotores();
    }

    /**
     * Endpoint GET para listar los festivales asociados a un promotor específico.
     * Útil para que el administrador vea los eventos de un promotor concreto.
     * Realiza forward a un JSP. Requiere rol ADMIN en sesión.
     *
     * @param idPromotor El ID del promotor cuyos festivales se quieren listar.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene éxito).
     * @throws BadRequestException Si {@code idPromotor} no es válido o no se proporciona.
     * @throws NotFoundException Si no se encuentra un promotor con el ID dado.
     * @throws InternalServerErrorException Si ocurre un error interno al cargar los datos.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
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
            // Obtener datos del promotor para mostrar su nombre en la vista
            UsuarioDTO promotor = usuarioService.obtenerUsuarioPorId(idPromotor)
                    .filter(u -> u.getRol() == RolUsuario.PROMOTOR) // Asegurarse que es promotor
                    .orElseThrow(() -> new NotFoundException("Promotor no encontrado con ID: " + idPromotor));

            // Obtener la lista de festivales para ese promotor
            List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);

            // Pasar datos al JSP a través de atributos de la request
            request.setAttribute("promotor", promotor);
            request.setAttribute("festivales", listaFestivales);
            request.setAttribute("idAdminAutenticado", idAdmin);
            mostrarMensajeFlash(request);

            // Forward al JSP que mostrará la lista
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotor-festivales.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException e) {
            log.warn("Promotor ID {} no encontrado al intentar listar sus festivales.", idPromotor);
            throw e; // JAX-RS mapeará esto a 404 Not Found
        } catch (Exception e) {
            log.error("Error al obtener festivales para promotor ID {}: {}", idPromotor, e.getMessage(), e);
            throw new InternalServerErrorException("Error interno al cargar los festivales del promotor.", e);
        }
    }

    /**
     * Endpoint GET para el dashboard principal del administrador.
     * Actualmente redirige a la lista de promotores.
     *
     * @return Una respuesta de redirección (303 See Other).
     */
    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarDashboard() {
        log.debug("GET /admin/dashboard recibido");
        verificarAccesoAdmin(request); // Solo verifica acceso
        log.info("Dashboard de Admin no implementado, redirigiendo a lista de promotores.");
        // Construye la URI para el método listarPromotores dentro de esta misma clase
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarPromotores").build();
        // Devuelve una respuesta 303 See Other para redirigir el navegador
        return Response.seeOther(listUri).build();
    }

    /**
     * Endpoint GET para mostrar el formulario de creación de un nuevo promotor.
     * Realiza forward a un JSP. Requiere rol ADMIN en sesión.
     *
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene éxito).
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/promotores/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrearPromotor() throws ServletException, IOException {
        log.debug("GET /admin/promotores/crear recibido");
        Integer idAdmin = verificarAccesoAdmin(request);
        log.debug("Mostrando formulario de creación de promotor por Admin ID: {}", idAdmin);

        // Prepara un DTO vacío para el formulario y lo pasa al JSP
        request.setAttribute("promotor", new UsuarioCreacionDTO());
        request.setAttribute("esNuevo", true); // Indicador para el JSP
        request.setAttribute("idAdminAutenticado", idAdmin);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotor-detalle.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * Endpoint POST para procesar la creación de un nuevo promotor.
     * Recibe los datos del formulario, llama al servicio para crear el usuario,
     * y redirige a la lista de promotores en caso de éxito, o vuelve a mostrar
     * el formulario con errores en caso de fallo.
     * Requiere rol ADMIN en sesión.
     *
     * @param nombre Nombre del nuevo promotor.
     * @param email Email del nuevo promotor (debe ser único).
     * @param password Contraseña inicial para el promotor (texto plano).
     * @return Una respuesta de redirección (303) a la lista si éxito, o una
     * respuesta OK (200) mostrando el formulario con error si falla.
     * @throws ServletException Si ocurre un error durante el forward en caso de error.
     * @throws IOException Si ocurre un error de E/S durante el forward en caso de error.
     * @throws InternalServerErrorException Si ocurre un error interno inesperado.
     */
    @POST
    @Path("/promotores/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarPromotor(
            @FormParam("nombre") String nombre,
            @FormParam("email") String email,
            @FormParam("password") String password) throws ServletException, IOException {

        log.info("POST /admin/promotores/guardar (CREACIÓN) recibido para email: {}", email);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol

        // Crear DTO con los datos del formulario
        UsuarioCreacionDTO dto = new UsuarioCreacionDTO();
        dto.setNombre(nombre);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setRol(RolUsuario.PROMOTOR); // Rol fijo para este endpoint

        try {
            // Validaciones básicas (podrían estar en el DTO con Bean Validation)
            if (nombre == null || nombre.isBlank() || email == null || email.isBlank() || password == null || password.isEmpty()) {
                throw new IllegalArgumentException("Nombre, email y contraseña son obligatorios.");
            }
            if (password.length() < 8) { // Ejemplo de validación de longitud
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
            }

            // Llamar al servicio para crear el usuario
            log.info("Llamando a usuarioService.crearUsuario (Promotor) por Admin ID: {}", idAdmin);
            UsuarioDTO creado = usuarioService.crearUsuario(dto);

            // Guardar mensaje de éxito en sesión para mostrarlo después de redirigir (Flash message)
            String mensajeExito = "Promotor '" + creado.getNombre() + "' creado con éxito.";
            setFlashMessage(request, "mensaje", mensajeExito);

            // Redirigir a la lista de promotores
            URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarPromotores").build();
            log.debug("Redirigiendo a: {}", listUri);
            return Response.seeOther(listUri).build(); // 303 See Other

        } catch (EmailExistenteException e) {
            log.warn("Error de negocio al crear promotor (email existente): {}", e.getMessage());
            // Volver a mostrar el formulario con el error
            forwardToPromotorFormWithError(dto, idAdmin, e.getMessage());
            return Response.ok().build(); // 200 OK (mostrando el form con error)
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear promotor: {}", e.getMessage());
            // Volver a mostrar el formulario con el error
            forwardToPromotorFormWithError(dto, idAdmin, e.getMessage());
            return Response.ok().build(); // 200 OK
        } catch (Exception e) {
            log.error("Error interno inesperado al crear promotor: {}", e.getMessage(), e);
            // Error grave, devolver 500 Internal Server Error
            throw new InternalServerErrorException("Error interno al crear el promotor.", e);
        }
    }

    /**
     * Endpoint POST para cambiar el estado (activo/inactivo) de un promotor.
     * Recibe el ID del promotor y el nuevo estado deseado.
     * Redirige a la lista de promotores. Requiere rol ADMIN en sesión.
     *
     * @param idPromotor ID del promotor a modificar.
     * @param nuevoEstado {@code true} para activar, {@code false} para desactivar.
     * @return Una respuesta de redirección (303) a la lista de promotores.
     * @throws BadRequestException Si faltan parámetros.
     */
    @POST
    @Path("/promotores/cambiar-estado")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response cambiarEstadoPromotor(
            @FormParam("idPromotor") Integer idPromotor,
            @FormParam("nuevoEstado") Boolean nuevoEstado) {

        log.info("POST /admin/promotores/cambiar-estado para ID: {} a {}", idPromotor, nuevoEstado);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol

        if (idPromotor == null || nuevoEstado == null) {
            throw new BadRequestException("Faltan parámetros requeridos (idPromotor, nuevoEstado).");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        try {
            // Llamar al servicio para actualizar el estado
            log.info("Llamando a usuarioService.actualizarEstadoUsuario ID: {} a {} por Admin ID: {}", idPromotor, nuevoEstado, idAdmin);
            UsuarioDTO actualizado = usuarioService.actualizarEstadoUsuario(idPromotor, nuevoEstado);
            mensajeFlash = "Estado del promotor '" + actualizado.getNombre() + "' actualizado a " + (nuevoEstado ? "ACTIVO" : "INACTIVO") + ".";
        } catch (UsuarioNotFoundException e) {
            log.warn("Promotor ID {} no encontrado para cambiar estado.", idPromotor);
            errorFlash = e.getMessage(); // Mensaje específico de "no encontrado"
        } catch (Exception e) {
            log.error("Error interno al cambiar estado del promotor ID {}: {}", idPromotor, e.getMessage(), e);
            errorFlash = "Error interno al cambiar el estado."; // Mensaje genérico
        }

        // Guardar mensaje (éxito o error) en sesión para mostrar después de redirigir
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        // Redirigir siempre a la lista de promotores
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarPromotores").build();
        log.debug("Redirigiendo a: {}", listUri);
        return Response.seeOther(listUri).build(); // 303 See Other
    }

    // --- Endpoints para Gestión de Festivales por Admin ---
    /**
     * Endpoint GET para mostrar el formulario de creación de un nuevo festival por el Admin.
     * Carga la lista de promotores activos para poder asignarle uno.
     * Realiza forward a un JSP. Requiere rol ADMIN en sesión.
     *
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene éxito).
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrearFestival() throws ServletException, IOException {
        log.debug("GET /admin/festivales/crear recibido");
        Integer idAdmin = verificarAccesoAdmin(request);

        // Obtener lista de promotores activos para el desplegable
        List<UsuarioDTO> promotoresActivos = usuarioService.obtenerUsuariosPorRol(RolUsuario.PROMOTOR)
                .stream()
                .filter(UsuarioDTO::getEstado) // Filtrar solo los activos
                .collect(Collectors.toList());

        log.debug("Mostrando formulario de creación de festival por Admin ID: {}", idAdmin);
        request.setAttribute("festival", new FestivalDTO()); // DTO vacío para el formulario
        request.setAttribute("promotores", promotoresActivos); // Lista para el select
        request.setAttribute("esNuevo", true); // Indicador para el JSP
        request.setAttribute("idAdminAutenticado", idAdmin);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-festival-detalle.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * Endpoint POST para procesar la creación de un nuevo festival por el Admin.
     * Recibe los datos del formulario, incluyendo el ID del promotor al que se asigna.
     * Llama al servicio para crear el festival (que lo creará en estado BORRADOR).
     * Redirige a la lista de todos los festivales en caso de éxito.
     * Requiere rol ADMIN en sesión.
     *
     * @param nombre Nombre del festival.
     * @param descripcion Descripción del festival.
     * @param fechaInicioStr Fecha de inicio (formato yyyy-MM-dd).
     * @param fechaFinStr Fecha de fin (formato yyyy-MM-dd).
     * @param ubicacion Ubicación del festival.
     * @param aforoStr Aforo (como String, se parseará a Integer).
     * @param imagenUrl URL de la imagen del cartel.
     * @param idPromotorSeleccionado ID del promotor al que se asigna el festival.
     * @return Una respuesta de redirección (303) a la lista si éxito, o una
     * respuesta OK (200) mostrando el formulario con error si falla.
     * @throws ServletException Si ocurre un error durante el forward en caso de error.
     * @throws IOException Si ocurre un error de E/S durante el forward en caso de error.
     * @throws InternalServerErrorException Si ocurre un error interno inesperado.
     */
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

        log.info("POST /admin/festivales/guardar (CREACIÓN Admin) recibido para promotor ID: {}", idPromotorSeleccionado);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol

        // Crear DTO y poblar con datos del form
        FestivalDTO dto = new FestivalDTO();
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setUbicacion(ubicacion);
        dto.setImagenUrl(imagenUrl);
        // Establecer el ID del promotor en el DTO también (aunque el servicio use el parámetro)
        dto.setIdPromotor(idPromotorSeleccionado);

        try {
            // Validaciones y parseo
            if (idPromotorSeleccionado == null) {
                throw new IllegalArgumentException("Debe seleccionar un promotor.");
            }
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre del festival es obligatorio.");
            }
            if (fechaInicioStr == null || fechaInicioStr.isBlank() || fechaFinStr == null || fechaFinStr.isBlank()) {
                 throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
            }
            dto.setFechaInicio(LocalDate.parse(fechaInicioStr)); // Puede lanzar DateTimeParseException
            dto.setFechaFin(LocalDate.parse(fechaFinStr));     // Puede lanzar DateTimeParseException
            if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
                throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
            }
            if (aforoStr != null && !aforoStr.isBlank()) {
                dto.setAforo(Integer.parseInt(aforoStr)); // Puede lanzar NumberFormatException
                if (dto.getAforo() <= 0) {
                    throw new IllegalArgumentException("El aforo debe ser un número positivo.");
                }
            }
            // El servicio establecerá el estado BORRADOR por defecto
            // dto.setEstado(EstadoFestival.BORRADOR);

            // Llamar al servicio para crear el festival
            log.info("Llamando a festivalService.crearFestival para promotor ID: {} por Admin ID: {}", idPromotorSeleccionado, idAdmin);
            FestivalDTO creado = festivalService.crearFestival(dto, idPromotorSeleccionado);

            // Mensaje flash y redirección
            String mensajeExito = "Festival '" + creado.getNombre() + "' creado y asignado al promotor (ID "
                    + idPromotorSeleccionado + ") con éxito (estado BORRADOR).";
            setFlashMessage(request, "mensaje", mensajeExito);
            URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarTodosFestivales").build();
            log.debug("Redirigiendo a: {}", listUri);
            return Response.seeOther(listUri).build(); // 303 See Other

        } catch (NumberFormatException e) {
            log.warn("Error de formato numérico al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Formato de número inválido (ej: en Aforo).");
            return Response.ok().build(); // 200 OK (mostrar form con error)
        } catch (DateTimeParseException e) {
            log.warn("Error de formato de fecha al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Formato de fecha inválido (use yyyy-MM-dd).");
            return Response.ok().build(); // 200 OK
        } catch (IllegalArgumentException | UsuarioNotFoundException e) {
            log.warn("Error de validación/negocio al crear festival por admin: {}", e.getMessage());
            forwardToFestivalFormWithError(dto, idAdmin, "Datos inválidos: " + e.getMessage());
            return Response.ok().build(); // 200 OK
        } catch (Exception e) {
            log.error("Error interno inesperado al crear festival por admin: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Error interno al crear el festival.", e);
        }
    }

    /**
     * Endpoint GET para listar todos los festivales registrados en el sistema.
     * Permite filtrar opcionalmente por estado usando un parámetro query.
     * Realiza forward a un JSP. Requiere rol ADMIN en sesión.
     *
     * @param estadoFilter String opcional representando el estado (ej: "PUBLICADO", "BORRADOR").
     * Si es inválido o nulo/vacío, se muestran todos.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene éxito).
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/listar-todos")
    @Produces(MediaType.TEXT_HTML)
    public Response listarTodosFestivales(@QueryParam("estado") String estadoFilter) throws ServletException, IOException {
        log.debug("GET /admin/festivales/listar-todos recibido. Filtro estado: '{}'", estadoFilter);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol

        List<FestivalDTO> listaFestivales;
        EstadoFestival estadoEnum = null;
        String errorFiltro = null;

        try {
            // Intentar parsear el estado si se proporciona
            if (estadoFilter != null && !estadoFilter.isBlank()) {
                estadoEnum = EstadoFestival.valueOf(estadoFilter.toUpperCase());
                log.debug("Filtrando festivales por estado: {}", estadoEnum);
            }
            // Llamar al servicio (que maneja estado null para obtener todos)
            listaFestivales = festivalService.obtenerFestivalesPorEstado(estadoEnum);

        } catch (IllegalArgumentException e) {
            // Captura el error si valueOf falla (estado inválido)
            log.warn("Estado de filtro inválido recibido: '{}'", estadoFilter);
            errorFiltro = "Estado de filtro inválido: '" + estadoFilter + "'. Mostrando todos.";
            listaFestivales = festivalService.obtenerTodosLosFestivales(); // Mostrar todos como fallback
        } catch (Exception e) {
            log.error("Error obteniendo lista de festivales para admin: {}", e.getMessage(), e);
            request.setAttribute("error", "Error crítico al cargar la lista de festivales.");
            listaFestivales = Collections.emptyList(); // Lista vacía en caso de error grave
        }

        // Pasar datos al JSP
        request.setAttribute("festivales", listaFestivales);
        request.setAttribute("idAdminAutenticado", idAdmin);
        request.setAttribute("estadoFiltro", estadoFilter); // Para mantener selección en el form
        request.setAttribute("estadosPosibles", EstadoFestival.values()); // Para el desplegable de filtro
        if (errorFiltro != null) {
            request.setAttribute("error", errorFiltro); // Mostrar error de filtro inválido
        }
        mostrarMensajeFlash(request); // Mostrar mensajes de acciones previas

        // Forward al JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-festivales.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * Endpoint POST para confirmar (publicar) un festival que está en estado BORRADOR.
     * Cambia el estado del festival a PUBLICADO.
     * Redirige a la lista de todos los festivales. Requiere rol ADMIN en sesión.
     *
     * @param idFestival ID del festival a confirmar.
     * @return Una respuesta de redirección (303) a la lista de festivales.
     * @throws BadRequestException Si falta el ID del festival.
     */
    @POST
    @Path("/festivales/confirmar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response confirmarFestival(@FormParam("idFestival") Integer idFestival) {
        log.info("POST /admin/festivales/confirmar para ID: {}", idFestival);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol

        if (idFestival == null) {
            throw new BadRequestException("Falta el parámetro idFestival.");
        }

        String mensajeFlash = null;
        String errorFlash = null;

        try {
            // Llamar al servicio para cambiar el estado a PUBLICADO
            log.info("Llamando a festivalService.cambiarEstadoFestival ID {} a PUBLICADO por Admin ID {}", idFestival, idAdmin);
            FestivalDTO confirmado = festivalService.cambiarEstadoFestival(idFestival, EstadoFestival.PUBLICADO, idAdmin);
            mensajeFlash = "Festival '" + confirmado.getNombre() + "' confirmado y publicado con éxito.";

        } catch (FestivalNotFoundException | UsuarioNotFoundException e) {
            log.warn("Error al confirmar festival ID {}: {}", idFestival, e.getMessage());
            errorFlash = e.getMessage(); // Error: no encontrado
        } catch (SecurityException | IllegalStateException e) {
            log.warn("Error de negocio/seguridad al confirmar festival ID {}: {}", idFestival, e.getMessage());
            errorFlash = "No se pudo confirmar el festival: " + e.getMessage(); // Error: estado inválido o sin permiso
        } catch (Exception e) {
            log.error("Error interno al confirmar festival ID {}: {}", idFestival, e.getMessage(), e);
            errorFlash = "Error interno al confirmar el festival."; // Error genérico
        }

        // Guardar mensaje flash y redirigir
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarTodosFestivales").build();
        log.debug("Redirigiendo a: {}", listUri);
        return Response.seeOther(listUri).build(); // 303 See Other
    }

    /**
     * Endpoint POST para cambiar el estado de un festival a CANCELADO o FINALIZADO.
     * Solo permite cambiar desde el estado PUBLICADO.
     * Redirige a la lista de todos los festivales. Requiere rol ADMIN en sesión.
     *
     * @param idFestival ID del festival a modificar.
     * @param nuevoEstadoStr String representando el nuevo estado ("CANCELADO" o "FINALIZADO").
     * @return Una respuesta de redirección (303) a la lista de festivales.
     * @throws BadRequestException Si faltan parámetros o el nuevo estado es inválido.
     */
    @POST
    @Path("/festivales/cambiar-estado")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response cambiarEstadoFestivalAdmin(
            @FormParam("idFestival") Integer idFestival,
            @FormParam("nuevoEstado") String nuevoEstadoStr) {

        log.info("POST /admin/festivales/cambiar-estado para ID: {} a {}", idFestival, nuevoEstadoStr);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol

        if (idFestival == null || nuevoEstadoStr == null || nuevoEstadoStr.isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos (idFestival, nuevoEstado).");
        }

        EstadoFestival nuevoEstado;
        try {
            // Convertir string a Enum y validar que sea CANCELADO o FINALIZADO
            nuevoEstado = EstadoFestival.valueOf(nuevoEstadoStr.toUpperCase());
            if (nuevoEstado != EstadoFestival.CANCELADO && nuevoEstado != EstadoFestival.FINALIZADO) {
                 throw new IllegalArgumentException("Solo se permite cambiar a CANCELADO o FINALIZADO desde esta acción.");
            }
        } catch (IllegalArgumentException e) {
            log.warn("Valor inválido para 'nuevoEstado' en POST /admin/festivales/cambiar-estado: {}", nuevoEstadoStr);
            throw new BadRequestException("Valor de 'nuevoEstado' inválido. Use CANCELADO o FINALIZADO.");
        }

        String mensajeFlash = null;
        String errorFlash = null;

        try {
            // Llamar al servicio para cambiar el estado
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

        // Guardar mensaje flash y redirigir
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarTodosFestivales").build();
        log.debug("Redirigiendo a: {}", listUri);
        return Response.seeOther(listUri).build(); // 303 See Other
    }

    // --- Endpoints para Gestión de Asistentes por Admin ---
    /**
     * Endpoint GET para listar todos los asistentes registrados o buscar por término.
     * Realiza forward a un JSP. Requiere rol ADMIN en sesión.
     *
     * @param searchTerm Término opcional para buscar por nombre o email.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene éxito).
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/asistentes")
    @Produces(MediaType.TEXT_HTML)
    public Response listarAsistentes(@QueryParam("buscar") String searchTerm) throws ServletException, IOException {
        log.debug("GET /admin/asistentes recibido. Término búsqueda: '{}'", searchTerm);
        Integer idAdmin = verificarAccesoAdmin(request); // Verifica sesión y rol

        List<AsistenteDTO> listaAsistentes;
        try {
            // Llama al servicio de búsqueda (que maneja término nulo/vacío)
            listaAsistentes = asistenteService.buscarAsistentes(searchTerm);
        } catch (Exception e) {
            log.error("Error obteniendo lista de asistentes para admin: {}", e.getMessage(), e);
            request.setAttribute("error", "Error crítico al cargar la lista de asistentes.");
            listaAsistentes = Collections.emptyList(); // Lista vacía en caso de error
        }

        // Pasar datos al JSP
        request.setAttribute("asistentes", listaAsistentes);
        request.setAttribute("idAdminAutenticado", idAdmin);
        request.setAttribute("searchTerm", searchTerm); // Para repoblar campo búsqueda
        mostrarMensajeFlash(request);

        // Forward al JSP de asistentes
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-asistentes.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * Endpoint GET para mostrar los detalles de un asistente específico.
     * Realiza forward a un JSP. Requiere rol ADMIN en sesión.
     *
     * @param idAsistente ID del asistente a visualizar.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene éxito).
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el asistente no se encuentra.
     * @throws InternalServerErrorException Si ocurre un error interno.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/asistentes/{idAsistente}")
    @Produces(MediaType.TEXT_HTML)
    public Response verDetalleAsistente(@PathParam("idAsistente") Integer idAsistente) throws ServletException, IOException {
        log.debug("GET /admin/asistentes/{} recibido", idAsistente);
        Integer idAdmin = verificarAccesoAdmin(request);

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
            request.setAttribute("editMode", false); // Modo solo lectura
            mostrarMensajeFlash(request);

            // Forward al JSP de detalle
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-asistente-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException e) {
            log.warn("Asistente ID {} no encontrado.", idAsistente);
            throw e; // Dejar que JAX-RS maneje el 404
        } catch (Exception e) {
            log.error("Error al obtener detalles del asistente ID {}: {}", idAsistente, e.getMessage(), e);
            throw new InternalServerErrorException("Error interno al cargar los detalles del asistente.", e);
        }
    }

    /**
     * Endpoint GET para mostrar el formulario de edición de un asistente existente.
     * Realiza forward a un JSP. Requiere rol ADMIN en sesión.
     *
     * @param idAsistente ID del asistente a editar.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene éxito).
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el asistente no se encuentra.
     * @throws InternalServerErrorException Si ocurre un error interno.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
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
            // Obtener datos actuales del asistente
            AsistenteDTO asistente = asistenteService.obtenerAsistentePorId(idAsistente)
                    .orElseThrow(() -> new NotFoundException("Asistente no encontrado con ID: " + idAsistente));

            // Pasar datos al JSP (el mismo que el de detalle, pero en modo edición)
            request.setAttribute("asistente", asistente);
            request.setAttribute("idAdminAutenticado", idAdmin);
            request.setAttribute("editMode", true); // Habilitar campos de edición
            mostrarMensajeFlash(request); // Mostrar errores de validación previos si los hubo

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-asistente-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException e) {
            throw e; // Dejar que JAX-RS maneje el 404
        } catch (Exception e) {
            log.error("Error al mostrar formulario editar asistente ID {}: {}", idAsistente, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del asistente para editar.", e);
        }
    }

    /**
     * Endpoint POST para procesar la actualización de un asistente existente.
     * Recibe los datos del formulario (nombre, teléfono). El email no se modifica.
     * Redirige a la lista de asistentes. Requiere rol ADMIN en sesión.
     *
     * @param idAsistente ID del asistente a actualizar.
     * @param nombre Nuevo nombre para el asistente.
     * @param telefono Nuevo teléfono para el asistente (opcional).
     * @return Una respuesta de redirección (303) a la lista de asistentes.
     * @throws BadRequestException Si el ID no es válido.
     * @throws ServletException Si ocurre un error durante el forward en caso de error de validación.
     * @throws IOException Si ocurre un error de E/S durante el forward en caso de error de validación.
     */
    @POST
    @Path("/asistentes/{idAsistente}/actualizar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response actualizarAsistente(
            @PathParam("idAsistente") Integer idAsistente,
            @FormParam("nombre") String nombre,
            @FormParam("telefono") String telefono) throws ServletException, IOException {

        log.info("POST /admin/asistentes/{}/actualizar recibido", idAsistente);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idAsistente == null) {
            throw new BadRequestException("ID Asistente no válido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        AsistenteDTO dtoParaForm = new AsistenteDTO(); // Para posible reenvío al form

        try {
            // Validar y poblar DTO (solo campos editables: nombre, telefono)
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre del asistente es obligatorio.");
            }
            // Crear DTO solo con los datos a actualizar
            AsistenteDTO dtoActualizar = new AsistenteDTO();
            dtoActualizar.setNombre(nombre);
            dtoActualizar.setTelefono(telefono); // El servicio maneja si es null/vacío

            // Llamar al servicio para actualizar
            AsistenteDTO actualizado = asistenteService.actualizarAsistente(idAsistente, dtoActualizar);
            mensajeFlash = "Asistente '" + actualizado.getNombre() + "' (ID: " + idAsistente + ") actualizado con éxito.";

        } catch (IllegalArgumentException | AsistenteNotFoundException e) {
            errorFlash = "Error al actualizar: " + e.getMessage();
            log.warn("Error al actualizar asistente ID {}: {}", idAsistente, errorFlash);
            // Si hay error de validación/no encontrado, preparamos para volver al form
            // Necesitaríamos el email original para mostrarlo
            Optional<AsistenteDTO> originalOpt = asistenteService.obtenerAsistentePorId(idAsistente); // Volver a buscar
            if (originalOpt.isPresent()) {
                dtoParaForm = originalOpt.get(); // Usar datos originales
                dtoParaForm.setNombre(nombre); // Sobrescribir con los datos del intento fallido
                dtoParaForm.setTelefono(telefono);
                forwardToAsistenteFormWithError(dtoParaForm, idAdmin, errorFlash);
                return Response.ok().build(); // 200 OK (mostrando form con error)
            } else {
                // Si no se encontró ni siquiera para mostrar el error, redirigir con error general
                 setFlashMessage(request, "error", "Asistente no encontrado (ID: " + idAsistente + ").");
                 URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarAsistentes").build();
                 return Response.seeOther(listUri).build();
            }
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al actualizar.";
            log.error("Error interno al actualizar asistente ID {}: {}", idAsistente, e.getMessage(), e);
            setFlashMessage(request, "error", errorFlash);
            URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarAsistentes").build();
            return Response.seeOther(listUri).build(); // Redirigir con error
        }

        // Si la actualización fue exitosa, guardar mensaje y redirigir
        setFlashMessage(request, "mensaje", mensajeFlash);
        URI listUri = uriInfo.getBaseUriBuilder().path(AdminResource.class).path(AdminResource.class, "listarAsistentes").build();
        return Response.seeOther(listUri).build();
    }

    // --- Endpoints para Gestión de Pulseras NFC por Admin ---
    /**
     * Endpoint GET para listar las pulseras NFC asociadas a un festival específico.
     * Realiza forward a un JSP. Requiere rol ADMIN en sesión.
     *
     * @param idFestival ID del festival cuyas pulseras se quieren listar.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene éxito).
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el festival no se encuentra.
     * @throws InternalServerErrorException Si ocurre un error interno.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/{idFestival}/pulseras")
    @Produces(MediaType.TEXT_HTML)
    public Response listarPulserasPorFestivalAdmin(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /admin/festivales/{}/pulseras recibido", idFestival);
        Integer idAdmin = verificarAccesoAdmin(request);
        if (idFestival == null) {
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            // Obtener datos del festival para el título de la página
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .orElseThrow(() -> new NotFoundException("Festival no encontrado con ID: " + idFestival));

            // Obtener lista de pulseras DTOs para este festival (el servicio verifica permisos)
            List<PulseraNFCDTO> listaPulseras = pulseraNFCService.obtenerPulserasPorFestival(idFestival, idAdmin);

            // Pasar datos al JSP
            request.setAttribute("festival", festival);
            request.setAttribute("pulseras", listaPulseras);
            request.setAttribute("idAdminAutenticado", idAdmin);
            mostrarMensajeFlash(request);

            // Forward al JSP de pulseras del festival para admin
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-festival-pulseras.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException e) {
            throw e; // Dejar que JAX-RS maneje el 404
        } catch (Exception e) {
            log.error("Error al listar pulseras para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar las pulseras del festival.", e);
        }
    }

    // --- Método Auxiliar de Seguridad ---
    /**
     * Verifica si existe una sesión HTTP activa y si el usuario autenticado
     * en ella tiene el rol ADMIN.
     *
     * @param request La petición HTTP actual.
     * @return El ID del usuario administrador autenticado.
     * @throws NotAuthorizedException Si no hay sesión activa o es inválida.
     * @throws ForbiddenException Si el usuario en sesión no tiene rol ADMIN.
     */
    private Integer verificarAccesoAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // No crear sesión si no existe
        log.trace("Verificando acceso Admin. ¿Sesión existe?: {}", (session != null));
        if (session == null) {
            log.warn("Intento de acceso a recurso Admin sin sesión activa.");
            // Usamos una excepción estándar JAX-RS para 401
            throw new NotAuthorizedException("No hay sesión activa. Por favor, inicie sesión.", Response.status(Response.Status.UNAUTHORIZED).build());
        }

        // Obtener datos de usuario de la sesión
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        log.trace("Atributos de sesión encontrados: userId={}, userRole={}", userId, userRole);

        // Validar datos de sesión
        if (userId == null || userRole == null) {
            log.warn("Intento de acceso a recurso Admin con sesión inválida (faltan atributos userId o userRole). Sesión ID: {}", session.getId());
            session.invalidate(); // Invalidar sesión corrupta
            throw new NotAuthorizedException("Sesión inválida. Por favor, inicie sesión de nuevo.", Response.status(Response.Status.UNAUTHORIZED).build());
        }

        // Verificar rol ADMIN
        if (!RolUsuario.ADMIN.name().equals(userRole)) {
            log.warn("Usuario ID {} con rol {} intentó acceder a recurso de Admin.", userId, userRole);
            // Usamos una excepción estándar JAX-RS para 403
            throw new ForbiddenException("Acceso denegado. Se requiere rol ADMIN.");
        }

        log.debug("Acceso permitido para admin ID: {}", userId);
        return userId; // Devolver ID del admin autenticado
    }

    // --- Métodos Auxiliares para Forward con Error y Mensajes Flash ---
    /**
     * Realiza un forward a la vista de detalle/creación de promotor, pasando
     * un mensaje de error y los datos introducidos por el usuario (excepto contraseña).
     *
     * @param dto DTO con los datos introducidos que causaron el error.
     * @param idAdmin ID del administrador autenticado.
     * @param errorMessage Mensaje de error a mostrar en el formulario.
     * @throws ServletException Si ocurre un error durante el forward.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    private void forwardToPromotorFormWithError(UsuarioCreacionDTO dto, Integer idAdmin, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        dto.setPassword(""); // Limpiar contraseña antes de reenviar
        request.setAttribute("promotor", dto);
        request.setAttribute("esNuevo", true); // Indicar que es el formulario de creación
        request.setAttribute("idAdminAutenticado", idAdmin);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-promotor-detalle.jsp");
        dispatcher.forward(request, response);
    }

     /**
     * Realiza un forward a la vista de detalle/creación de festival, pasando
     * un mensaje de error y los datos introducidos por el usuario.
     * Recarga la lista de promotores activos para el desplegable.
     *
     * @param dto DTO con los datos introducidos que causaron el error.
     * @param idAdmin ID del administrador autenticado.
     * @param errorMessage Mensaje de error a mostrar en el formulario.
     * @throws ServletException Si ocurre un error durante el forward.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    private void forwardToFestivalFormWithError(FestivalDTO dto, Integer idAdmin, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.setAttribute("festival", dto);
        request.setAttribute("esNuevo", true); // Indicar que es el formulario de creación
        // Recargar lista de promotores para el select
        List<UsuarioDTO> promotoresActivos = usuarioService.obtenerUsuariosPorRol(RolUsuario.PROMOTOR)
                .stream().filter(UsuarioDTO::getEstado).collect(Collectors.toList());
        request.setAttribute("promotores", promotoresActivos);
        request.setAttribute("idAdminAutenticado", idAdmin);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-festival-detalle.jsp");
        dispatcher.forward(request, response);
    }

     /**
     * Realiza un forward a la vista de detalle de asistente, pasando
     * un mensaje de error y los datos introducidos por el usuario.
     *
     * @param dto DTO con los datos introducidos que causaron el error.
     * @param idAdmin ID del administrador autenticado.
     * @param errorMessage Mensaje de error a mostrar en el formulario.
     * @throws ServletException Si ocurre un error durante el forward.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    private void forwardToAsistenteFormWithError(AsistenteDTO dto, Integer idAdmin, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.setAttribute("asistente", dto);
        request.setAttribute("editMode", true); // Indicar que estábamos en modo edición
        request.setAttribute("idAdminAutenticado", idAdmin);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/admin/admin-asistente-detalle.jsp");
        dispatcher.forward(request, response);
    }


    /**
     * Establece un mensaje flash (de éxito o error) en la sesión HTTP.
     *
     * @param request La petición actual.
     * @param type "mensaje" para éxito, "error" para error.
     * @param message El mensaje a guardar.
     */
    private void setFlashMessage(HttpServletRequest request, String type, String message) {
        if (message != null) {
            HttpSession session = request.getSession(); // Obtener o crear sesión
            session.setAttribute(type, message);
            log.trace("Mensaje flash '{}' guardado en sesión con clave '{}'", message, type);
        }
    }

    /**
     * Comprueba si existen mensajes flash ("mensaje" o "error") en la sesión y,
     * si es así, los mueve a atributos de la request y los elimina de la sesión.
     * Esto permite mostrarlos una sola vez en el JSP después de una redirección.
     *
     * @param request La petición actual.
     */
    private void mostrarMensajeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // No crear sesión si no existe
        if (session != null) {
            if (session.getAttribute("mensaje") != null) {
                request.setAttribute("mensajeExito", session.getAttribute("mensaje"));
                session.removeAttribute("mensaje");
                log.trace("Mensaje flash de éxito movido de sesión a request.");
            }
            if (session.getAttribute("error") != null) {
                request.setAttribute("error", session.getAttribute("error"));
                session.removeAttribute("error");
                log.trace("Mensaje flash de error movido de sesión a request.");
            }
        }
    }
}
