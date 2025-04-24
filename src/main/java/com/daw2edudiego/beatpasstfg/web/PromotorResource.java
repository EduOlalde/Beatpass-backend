package com.daw2edudiego.beatpasstfg.web;

// DTOs, Excepciones, Modelo, Servicios
import com.daw2edudiego.beatpasstfg.dto.*; // Import all DTOs
import com.daw2edudiego.beatpasstfg.exception.*; // Import all exceptions
import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.service.*; // Import all services

// Jakarta EE Servlets y JAX-RS
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

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Clases estándar Java
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Recurso JAX-RS que define los endpoints para el panel web del Promotor,
 * accesible bajo la ruta base {@code /api/promotor}.
 * <p>
 * Proporciona funcionalidades para que los usuarios con rol
 * {@link RolUsuario#PROMOTOR} gestionen sus propios recursos:
 * <ul>
 * <li>Festivales: Listar los propios, ver detalles, mostrar formulario de
 * edición, crear solicitud, guardar cambios.</li>
 * <li>Tipos de Entrada: Añadir, editar, eliminar (asociados a sus
 * festivales).</li>
 * <li>Entradas Asignadas: Listar por festival, nominar a asistentes, cancelar,
 * asociar pulseras.</li>
 * <li>Asistentes: Listar los asociados a sus festivales.</li>
 * <li>Pulseras NFC: Listar las asociadas a sus festivales.</li>
 * <li>Cambio de Contraseña: Gestionar el cambio de contraseña obligatorio
 * inicial.</li>
 * <li>Simulación de Venta: Endpoint temporal para probar el servicio de
 * ventas.</li>
 * </ul>
 * La autenticación y autorización se basa en la validación de una sesión HTTP
 * existente y la verificación de que el usuario autenticado en sesión tenga el
 * rol PROMOTOR. Las respuestas son principalmente HTML, realizando forwards a
 * archivos JSP ubicados en {@code /WEB-INF/jsp/promotor/}. Utiliza el patrón
 * Post-Redirect-Get (PRG) con mensajes flash en sesión para operaciones POST.
 * </p>
 *
 * @see FestivalService
 * @see EntradaService
 * @see EntradaAsignadaService
 * @see VentaService
 * @see AsistenteService
 * @see PulseraNFCService
 * @see UsuarioService
 * @author Eduardo Olalde
 */
@Path("/promotor")
public class PromotorResource {

    private static final Logger log = LoggerFactory.getLogger(PromotorResource.class);

    // Services (Manual Dependency Injection)
    private final FestivalService festivalService;
    private final UsuarioService usuarioService;
    private final EntradaService entradaService;
    private final EntradaAsignadaService entradaAsignadaService;
    private final VentaService ventaService;
    private final AsistenteService asistenteService;
    private final PulseraNFCService pulseraNFCService;

    // JAX-RS Context Injection
    @Context
    private UriInfo uriInfo;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    /**
     * Constructor que inicializa las instancias de los servicios necesarios.
     */
    public PromotorResource() {
        this.festivalService = new FestivalServiceImpl();
        this.usuarioService = new UsuarioServiceImpl();
        this.entradaService = new EntradaServiceImpl();
        this.entradaAsignadaService = new EntradaAsignadaServiceImpl();
        this.ventaService = new VentaServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.pulseraNFCService = new PulseraNFCServiceImpl();
    }

    // --- Endpoints para Gestión de Festivales del Promotor ---
    /**
     * Endpoint GET para listar los festivales propios del promotor autenticado.
     * Realiza forward al JSP {@code /WEB-INF/jsp/promotor/mis-festivales.jsp}.
     * Requiere rol PROMOTOR en sesión.
     *
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene
     * éxito).
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     * @throws NotAuthorizedException Si no hay sesión activa.
     * @throws ForbiddenException Si el usuario en sesión no es PROMOTOR.
     */
    @GET
    @Path("/festivales")
    @Produces(MediaType.TEXT_HTML)
    public Response listarFestivales() throws ServletException, IOException {
        log.debug("GET /promotor/festivales (listar) recibido");
        Integer idPromotor = verificarAccesoPromotor(request); // Verifica sesión y rol

        log.debug("Listando festivales para Promotor ID: {}", idPromotor);
        List<FestivalDTO> listaFestivales = festivalService.obtenerFestivalesPorPromotor(idPromotor);

        request.setAttribute("festivales", listaFestivales);
        request.setAttribute("idPromotorAutenticado", idPromotor);
        mostrarMensajeFlash(request); // Muestra mensajes de éxito/error de acciones previas

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/mis-festivales.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * Endpoint GET para mostrar el formulario de creación de un nuevo festival
     * (solicitud) por el promotor autenticado. Realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-festival-editar.jsp}. Requiere rol
     * PROMOTOR en sesión.
     *
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene
     * éxito).
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     * @throws NotAuthorizedException Si no hay sesión activa.
     * @throws ForbiddenException Si el usuario en sesión no es PROMOTOR.
     */
    @GET
    @Path("/festivales/crear")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCrear() throws ServletException, IOException {
        log.debug("GET /promotor/festivales/crear recibido");
        Integer idPromotor = verificarAccesoPromotor(request);
        log.debug("Mostrando formulario de creación para Promotor ID: {}", idPromotor);

        request.setAttribute("festival", new FestivalDTO()); // DTO vacío
        request.setAttribute("idPromotorAutenticado", idPromotor);
        request.setAttribute("esNuevo", true); // Indicador para el JSP

        // Forward al JSP de edición (que también sirve para crear)
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-festival-editar.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * Endpoint GET para mostrar los detalles (vista de solo lectura) de un
     * festival específico perteneciente al promotor autenticado. Carga los
     * datos del festival, los tipos de entrada asociados y realiza forward al
     * JSP {@code /WEB-INF/jsp/promotor/festival-detalle.jsp}. Requiere rol
     * PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestivalParam ID del festival a visualizar.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene
     * éxito).
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el festival no se encuentra.
     * @throws ForbiddenException Si el promotor no es dueño del festival.
     * @throws InternalServerErrorException Si ocurre un error interno al cargar
     * datos.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/ver/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarDetallesFestival(@PathParam("id") Integer idFestivalParam) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/ver/{} (Detalles) recibido", idFestivalParam);
        Integer idPromotor = verificarAccesoPromotor(request);
        final Integer idFestival = idFestivalParam;

        if (idFestival == null) {
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            // Obtener festival y verificar propiedad
            log.debug("Buscando festival con ID: {}", idFestival);
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor)) // Filtrar por promotor dueño
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            // Obtener tipos de entrada para este festival
            log.debug("Obteniendo tipos de entrada para festival ID: {}", idFestival);
            List<EntradaDTO> listaEntradas = entradaService.obtenerEntradasPorFestival(idFestival, idPromotor);

            // Pasar datos al JSP de VISTA
            request.setAttribute("festival", festival);
            request.setAttribute("tiposEntrada", listaEntradas);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);

            // Forward al JSP de VISTA
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/festival-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException | ForbiddenException e) {
            throw e; // Dejar que JAX-RS maneje 404 o 403
        } catch (Exception e) {
            log.error("Error al mostrar detalles para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del festival", e);
        }
    }

    /**
     * Endpoint GET para mostrar el formulario de edición de los datos básicos
     * de un festival existente perteneciente al promotor autenticado. Carga
     * solo los datos necesarios para el formulario y realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-festival-editar.jsp}. Requiere rol
     * PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestivalParam ID del festival a editar.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene
     * éxito).
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el festival no se encuentra.
     * @throws ForbiddenException Si el promotor no es dueño del festival.
     * @throws InternalServerErrorException Si ocurre un error interno al cargar
     * datos.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/editar/{id}") // Nueva ruta para edición
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditarFestival(@PathParam("id") Integer idFestivalParam) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/editar/{} recibido", idFestivalParam);
        Integer idPromotor = verificarAccesoPromotor(request);
        final Integer idFestival = idFestivalParam;

        if (idFestival == null) {
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            // Obtener festival y verificar propiedad
            log.debug("Buscando festival con ID: {} para editar", idFestival);
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            // Pasar datos al JSP de EDICIÓN
            request.setAttribute("festival", festival);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            request.setAttribute("esNuevo", false); // Indicar modo edición
            mostrarMensajeFlash(request); // Mostrar errores de intento previo si los hubo

            // Forward al JSP de EDICIÓN
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-festival-editar.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException | ForbiddenException e) {
            throw e; // JAX-RS maneja estas
        } catch (Exception e) {
            log.error("Error al mostrar formulario de edición para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del festival para editar", e);
        }
    }

    /**
     * Endpoint POST para guardar un festival (crear uno nuevo o actualizar uno
     * existente). Recibe datos del formulario. Si es nuevo, lo crea en estado
     * BORRADOR. Si es existente, actualiza sus datos (excepto el estado y el
     * promotor). Redirige a la página de VISTA del festival si tiene éxito, o
     * vuelve a mostrar el formulario de EDICIÓN con errores si falla. Requiere
     * rol PROMOTOR en sesión.
     *
     * @param idStr ID del festival (String, vacío o "0" si es nuevo).
     * @param nombre Nombre del festival (obligatorio).
     * @param descripcion Descripción (opcional).
     * @param fechaInicioStr Fecha inicio (YYYY-MM-DD, obligatorio).
     * @param fechaFinStr Fecha fin (YYYY-MM-DD, obligatorio, no anterior a
     * inicio).
     * @param ubicacion Ubicación (opcional).
     * @param aforoStr Aforo (opcional, debe ser número positivo si se indica).
     * @param imagenUrl URL de imagen (opcional).
     * @return Una respuesta de redirección (303) a la vista si éxito, o una
     * respuesta OK (200) mostrando el formulario de edición con error si falla.
     * @throws ServletException Si ocurre un error durante el forward en caso de
     * error.
     * @throws IOException Si ocurre un error de E/S durante el forward en caso
     * de error.
     */
    @POST
    @Path("/festivales/guardar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarFestival(
            @FormParam("idFestival") String idStr, @FormParam("nombre") String nombre,
            @FormParam("descripcion") String descripcion, @FormParam("fechaInicio") String fechaInicioStr,
            @FormParam("fechaFin") String fechaFinStr, @FormParam("ubicacion") String ubicacion,
            @FormParam("aforo") String aforoStr, @FormParam("imagenUrl") String imagenUrl
    ) throws ServletException, IOException {

        log.info("POST /promotor/festivales/guardar recibido");
        Integer idPromotor = verificarAccesoPromotor(request); // Verifica sesión y rol

        boolean esNuevo = (idStr == null || idStr.isEmpty() || "0".equals(idStr));
        Integer idFestival = null;
        FestivalDTO dto = new FestivalDTO(); // Para mapear datos y posible reenvío
        String errorMessage = null;
        FestivalDTO festivalGuardado = null; // Para obtener ID si es nuevo

        try {
            // Parsear ID si es actualización
            if (!esNuevo) {
                idFestival = Integer.parseInt(idStr); // Puede lanzar NumberFormatException
                dto.setIdFestival(idFestival);
            }

            // Validar y poblar DTO
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre del festival es obligatorio.");
            }
            dto.setNombre(nombre);
            dto.setDescripcion(descripcion);
            dto.setUbicacion(ubicacion);
            dto.setImagenUrl(imagenUrl);
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
            // El ID del promotor se pasa al servicio, no es necesario en el DTO para la lógica del servicio

            // Llamar al servicio correspondiente
            String mensajeExito;
            if (esNuevo) {
                // El servicio establecerá estado BORRADOR
                festivalGuardado = festivalService.crearFestival(dto, idPromotor);
                idFestival = festivalGuardado.getIdFestival(); // Obtener el ID del nuevo festival
                mensajeExito = "Solicitud de festival '" + festivalGuardado.getNombre() + "' creada correctamente (estado BORRADOR).";
            } else {
                // El servicio actualizarFestival verifica la propiedad y NO cambia el estado
                festivalService.actualizarFestival(idFestival, dto, idPromotor);
                festivalGuardado = dto; // Usar DTO para mensaje
                mensajeExito = "Festival '" + dto.getNombre() + "' actualizado correctamente.";
            }

            // Éxito: Mensaje flash y redirección a la página de VISTA del festival
            setFlashMessage(request, "mensaje", mensajeExito);
            URI viewUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarDetallesFestival") // Redirigir a la VISTA
                    .resolveTemplate("id", idFestival) // Usar el ID (nuevo o existente)
                    .build();
            return Response.seeOther(viewUri).build(); // 303 See Other

        } catch (NumberFormatException e) {
            errorMessage = "Formato numérico inválido (ID o Aforo).";
            log.warn("Error guardando festival (promotor {}): {}", idPromotor, errorMessage);
        } catch (DateTimeParseException e) {
            errorMessage = "Formato de fecha inválido (use yyyy-MM-dd).";
            log.warn("Error guardando festival (promotor {}): {}", idPromotor, errorMessage);
        } catch (IllegalArgumentException | FestivalNotFoundException | SecurityException | IllegalStateException e) {
            // Errores de validación, negocio o permisos
            errorMessage = e.getMessage();
            log.warn("Error guardando festival (promotor {}): {}", idPromotor, errorMessage);
        } catch (Exception e) {
            // Errores inesperados
            errorMessage = "Error interno inesperado al guardar el festival.";
            log.error("Error interno guardando festival (promotor {}): {}", idPromotor, e.getMessage(), e);
        }

        // Si hubo error, volver a mostrar el formulario de EDICIÓN con el error
        // Repoblar DTO con los datos que se intentaron guardar
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setUbicacion(ubicacion);
        dto.setImagenUrl(imagenUrl);
        // Intentar parsear de nuevo para repoblar, ignorando errores aquí
        try {
            dto.setFechaInicio(LocalDate.parse(fechaInicioStr));
        } catch (Exception ignored) {
        }
        try {
            dto.setFechaFin(LocalDate.parse(fechaFinStr));
        } catch (Exception ignored) {
        }
        try {
            dto.setAforo(Integer.parseInt(aforoStr));
        } catch (Exception ignored) {
        }
        dto.setIdPromotor(idPromotor); // Establecer ID promotor para contexto si es necesario

        // Forward al JSP de EDICIÓN
        forwardToPromotorFestivalEditFormWithError(dto, idPromotor, !esNuevo, errorMessage);
        return Response.ok().build(); // 200 OK (mostrando form de edición con error)
    }

    // --- Endpoints para Gestión de Tipos de Entrada por el Promotor ---
    /**
     * Endpoint POST para añadir un nuevo tipo de entrada a un festival
     * existente del promotor autenticado. Recibe datos del formulario. Redirige
     * a la vista de detalle del festival. Requiere rol PROMOTOR en sesión y ser
     * dueño del festival.
     *
     * @param idFestival ID del festival al que se añade la entrada.
     * @param tipo Nombre del tipo de entrada (ej: "General", obligatorio).
     * @param descripcion Descripción (opcional).
     * @param precioStr Precio (String, obligatorio, se parseará a BigDecimal).
     * @param stockStr Stock inicial (String, obligatorio, se parseará a
     * Integer).
     * @return Una respuesta de redirección (303) a la vista de detalle del
     * festival.
     * @throws BadRequestException Si el ID del festival no es válido.
     */
    @POST
    @Path("/festivales/{idFestival}/entradas")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarEntrada(
            @PathParam("idFestival") Integer idFestival,
            @FormParam("tipo") String tipo,
            @FormParam("descripcion") String descripcion,
            @FormParam("precio") String precioStr,
            @FormParam("stock") String stockStr) {

        log.info("POST /promotor/festivales/{}/entradas recibido", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request); // Verifica sesión y rol
        if (idFestival == null) {
            throw new BadRequestException("ID festival inválido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        EntradaDTO dto = new EntradaDTO(); // Para posible reenvío si hay error

        try {
            // Validar y poblar DTO
            if (tipo == null || tipo.isBlank()) {
                throw new IllegalArgumentException("El nombre del tipo de entrada es obligatorio.");
            }
            if (precioStr == null || precioStr.isBlank()) {
                throw new IllegalArgumentException("El precio es obligatorio.");
            }
            if (stockStr == null || stockStr.isBlank()) {
                throw new IllegalArgumentException("El stock es obligatorio.");
            }

            dto.setIdFestival(idFestival); // Asegurar que el DTO tiene el ID correcto
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
            // Parsear precio y stock
            dto.setPrecio(new BigDecimal(precioStr.replace(',', '.'))); // Permitir coma decimal
            dto.setStock(Integer.parseInt(stockStr));
            // Validar valores numéricos
            if (dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo.");
            }
            if (dto.getStock() < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo.");
            }

            // Llamar al servicio para crear la entrada (el servicio verifica propiedad del festival)
            EntradaDTO entradaCreada = entradaService.crearEntrada(dto, idFestival, idPromotor);
            mensajeFlash = "Tipo de entrada '" + entradaCreada.getTipo() + "' añadido correctamente.";

        } catch (NumberFormatException e) {
            errorFlash = "Formato numérico inválido para precio o stock.";
            log.warn("Error de formato numérico al crear entrada para festival {}: {}", idFestival, e.getMessage());
            // Poblar DTO con datos erróneos para repintar form
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
        } catch (IllegalArgumentException | SecurityException | FestivalNotFoundException e) {
            errorFlash = "Error al añadir entrada: " + e.getMessage();
            log.warn("Error de validación/negocio al crear entrada para festival {}: {}", idFestival, errorFlash);
            // Poblar DTO con datos erróneos para repintar form
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
            try {
                dto.setPrecio(new BigDecimal(precioStr.replace(',', '.')));
            } catch (Exception ignored) {
            }
            try {
                dto.setStock(Integer.parseInt(stockStr));
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al añadir el tipo de entrada.";
            log.error("Error interno al crear entrada para festival {}: {}", idFestival, e.getMessage(), e);
        }

        // Guardar mensaje/error en sesión
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (mensajeFlash != null) {
                session.setAttribute("mensaje", mensajeFlash);
            }
            if (errorFlash != null) {
                session.setAttribute("error", errorFlash);
                // Guardar el DTO con errores para repintar el formulario de añadir entrada
                session.setAttribute("nuevaEntradaConError", dto);
                // Marcar que hubo error en la entrada para diferenciarlo del error de festival
                session.setAttribute("errorEntrada", true);
            }
        }

        // Redirigir siempre a la página de VISTA del festival
        URI detailUri = uriInfo.getBaseUriBuilder()
                .path(PromotorResource.class) // Clase actual
                .path(PromotorResource.class, "mostrarDetallesFestival") // Método GET de VISTA festival
                .resolveTemplate("id", idFestival) // Reemplazar {id}
                .build();
        return Response.seeOther(detailUri).build(); // 303 See Other
    }

    /**
     * Endpoint GET para mostrar el formulario de edición de un tipo de entrada
     * específico. Realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-entrada-detalle.jsp}. Requiere rol
     * PROMOTOR en sesión y ser dueño del festival asociado.
     *
     * @param idEntrada ID del tipo de entrada a editar.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene
     * éxito).
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si la entrada no se encuentra.
     * @throws ForbiddenException Si el promotor no es dueño del festival
     * asociado.
     * @throws InternalServerErrorException Si ocurre un error interno al cargar
     * datos.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/entradas/{idEntrada}/editar")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditarEntrada(@PathParam("idEntrada") Integer idEntrada) throws ServletException, IOException {
        log.debug("GET /promotor/entradas/{}/editar recibido", idEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntrada == null) {
            throw new BadRequestException("ID de entrada no válido.");
        }

        try {
            // Obtener el DTO de la entrada, el servicio verifica propiedad
            EntradaDTO entradaDTO = entradaService.obtenerEntradaPorId(idEntrada, idPromotor)
                    .orElseThrow(() -> new NotFoundException("Tipo de entrada no encontrado o no tiene permiso."));

            // Pasar datos al JSP de edición de entrada
            request.setAttribute("entrada", entradaDTO);
            request.setAttribute("idPromotorAutenticado", idPromotor); // Para consistencia
            mostrarMensajeFlash(request); // Por si hay errores de un intento previo

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-entrada-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException | ForbiddenException e) {
            throw e; // JAX-RS maneja estas
        } catch (Exception e) {
            log.error("Error al mostrar formulario de edición para entrada ID {}: {}", idEntrada, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del tipo de entrada.", e);
        }
    }

    /**
     * Endpoint POST para procesar la actualización de un tipo de entrada
     * existente. Recibe datos del formulario. Redirige a la vista de detalle
     * del festival si tiene éxito, o vuelve al formulario de edición de la
     * entrada si falla. Requiere rol PROMOTOR en sesión y ser dueño del
     * festival asociado.
     *
     * @param idEntrada ID del tipo de entrada a actualizar.
     * @param tipo Nuevo nombre del tipo (obligatorio).
     * @param descripcion Nueva descripción (opcional).
     * @param precioStr Nuevo precio (String, obligatorio).
     * @param stockStr Nuevo stock (String, obligatorio).
     * @return Una respuesta de redirección (303) a la vista de detalle del
     * festival o al formulario de edición de entrada si hay error.
     * @throws BadRequestException Si el ID no es válido.
     * @throws ServletException Si ocurre un error durante el forward en caso de
     * error.
     * @throws IOException Si ocurre un error de E/S durante el forward en caso
     * de error.
     */
    @POST
    @Path("/entradas/{idEntrada}/actualizar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response actualizarEntrada(
            @PathParam("idEntrada") Integer idEntrada,
            @FormParam("tipo") String tipo,
            @FormParam("descripcion") String descripcion,
            @FormParam("precio") String precioStr,
            @FormParam("stock") String stockStr) throws ServletException, IOException {

        log.info("POST /promotor/entradas/{}/actualizar recibido", idEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntrada == null) {
            throw new BadRequestException("ID de entrada no válido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir
        EntradaDTO dto = new EntradaDTO(); // Para posible reenvío al form

        try {
            // Validar y poblar DTO
            if (tipo == null || tipo.isBlank()) {
                throw new IllegalArgumentException("El nombre del tipo de entrada es obligatorio.");
            }
            if (precioStr == null || precioStr.isBlank()) {
                throw new IllegalArgumentException("El precio es obligatorio.");
            }
            if (stockStr == null || stockStr.isBlank()) {
                throw new IllegalArgumentException("El stock es obligatorio.");
            }

            dto.setIdEntrada(idEntrada); // ID de la entrada a actualizar
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
            dto.setPrecio(new BigDecimal(precioStr.replace(',', '.'))); // Parsear
            dto.setStock(Integer.parseInt(stockStr)); // Parsear
            if (dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo.");
            }
            if (dto.getStock() < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo.");
            }

            // Llamar al servicio para actualizar (el servicio verifica propiedad)
            EntradaDTO actualizada = entradaService.actualizarEntrada(idEntrada, dto, idPromotor);
            mensajeFlash = "Tipo de entrada '" + actualizada.getTipo() + "' actualizado con éxito.";
            idFestival = actualizada.getIdFestival(); // Obtener ID del festival para redirigir

        } catch (NumberFormatException e) {
            errorFlash = "Formato numérico inválido para precio o stock.";
            log.warn("Error de formato numérico al actualizar entrada ID {}: {}", idEntrada, e.getMessage());
            // Poblar DTO para reenvío
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
        } catch (IllegalArgumentException | SecurityException | EntradaNotFoundException e) {
            errorFlash = "Error al actualizar: " + e.getMessage();
            log.warn("Error de validación/negocio al actualizar entrada ID {}: {}", idEntrada, errorFlash);
            // Poblar DTO para reenvío
            dto.setTipo(tipo);
            dto.setDescripcion(descripcion);
            try {
                dto.setPrecio(new BigDecimal(precioStr.replace(',', '.')));
            } catch (Exception ignored) {
            }
            try {
                dto.setStock(Integer.parseInt(stockStr));
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al actualizar el tipo de entrada.";
            log.error("Error interno al actualizar entrada ID {}: {}", idEntrada, e.getMessage(), e);
        }

        // Si hubo error, redirigir de vuelta al formulario de edición de la entrada
        if (errorFlash != null) {
            setFlashMessage(request, "error", errorFlash);
            // Construir URI para el método GET de edición de esta entrada
            URI editUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarFormularioEditarEntrada")
                    .resolveTemplate("idEntrada", idEntrada)
                    .build();
            return Response.seeOther(editUri).build(); // 303 See Other
        }

        // Si tuvo éxito, redirigir a la página de VISTA del festival
        setFlashMessage(request, "mensaje", mensajeFlash);
        URI detailUri = uriInfo.getBaseUriBuilder()
                .path(PromotorResource.class)
                .path(PromotorResource.class, "mostrarDetallesFestival") // VISTA del festival
                .resolveTemplate("id", idFestival)
                .build();
        return Response.seeOther(detailUri).build(); // 303 See Other
    }

    /**
     * Endpoint POST para eliminar un tipo de entrada específico. Redirige a la
     * vista de detalle del festival. Requiere rol PROMOTOR en sesión y ser
     * dueño del festival asociado.
     *
     * @param idEntrada ID del tipo de entrada a eliminar.
     * @return Una respuesta de redirección (303) a la vista de detalle del
     * festival.
     * @throws BadRequestException Si el ID no es válido.
     */
    @POST
    @Path("/entradas/{idEntrada}/eliminar")
    public Response eliminarEntrada(@PathParam("idEntrada") Integer idEntrada) {
        log.info("POST /promotor/entradas/{}/eliminar recibido", idEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntrada == null) {
            throw new BadRequestException("ID de entrada no válido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir

        try {
            // Obtener el ID del festival ANTES de intentar eliminar la entrada
            Optional<EntradaDTO> optDto = entradaService.obtenerEntradaPorId(idEntrada, idPromotor);
            if (optDto.isPresent()) {
                idFestival = optDto.get().getIdFestival();
                // Llamar al servicio para eliminar (el servicio verifica propiedad)
                entradaService.eliminarEntrada(idEntrada, idPromotor);
                mensajeFlash = "Tipo de entrada ID " + idEntrada + " eliminado correctamente.";
            } else {
                // Si no se encontró la entrada o no tenía permiso para verla
                errorFlash = "Tipo de entrada no encontrado o no tiene permiso para eliminarlo.";
                log.warn("Intento de eliminar entrada ID {} fallido (no encontrada o sin permiso) por promotor {}", idEntrada, idPromotor);
            }
        } catch (EntradaNotFoundException | UsuarioNotFoundException e) { // UsuarioNotFound no debería ocurrir
            errorFlash = e.getMessage();
            log.warn("Error al eliminar entrada ID {}: {}", idEntrada, errorFlash);
        } catch (SecurityException e) {
            errorFlash = e.getMessage(); // "No tiene permiso..."
            log.warn("Error de seguridad al eliminar entrada ID {}: {}", idEntrada, errorFlash);
        } catch (RuntimeException e) { // Captura errores como violación de FK (IllegalStateException del servicio)
            errorFlash = "No se pudo eliminar el tipo de entrada (posiblemente tiene ventas asociadas): " + e.getMessage();
            log.error("Error runtime al eliminar entrada ID {}: {}", idEntrada, e.getMessage(), e);
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al eliminar.";
            log.error("Error interno al eliminar entrada ID {}: {}", idEntrada, e.getMessage(), e);
        }

        // Guardar mensaje/error y redirigir
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        // Redirigir a la página de VISTA del festival si obtuvimos su ID, sino a la lista general
        URI redirectUri;
        if (idFestival != null) {
            redirectUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarDetallesFestival") // VISTA del festival
                    .resolveTemplate("id", idFestival)
                    .build();
        } else {
            redirectUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build();
        }
        return Response.seeOther(redirectUri).build(); // 303 See Other
    }

    // --- Endpoints para Gestión de Entradas Asignadas (Nominación, Cancelación, Asociación Pulsera) ---
    /**
     * Endpoint GET para listar las entradas asignadas (vendidas/generadas) de
     * un festival específico perteneciente al promotor autenticado. Realiza
     * forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-entradas-asignadas.jsp}. Requiere
     * rol PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestival ID del festival cuyas entradas asignadas se listarán.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene
     * éxito).
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el festival no se encuentra.
     * @throws ForbiddenException Si el promotor no es dueño del festival.
     * @throws InternalServerErrorException Si ocurre un error interno al cargar
     * datos.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/{idFestival}/entradas-asignadas")
    @Produces(MediaType.TEXT_HTML)
    public Response listarEntradasAsignadas(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/{}/entradas-asignadas recibido", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null) {
            throw new BadRequestException("ID festival inválido.");
        }
        try {
            // Obtener festival y verificar propiedad
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            // Obtener lista de entradas asignadas
            List<EntradaAsignadaDTO> listaEntradas = entradaAsignadaService.obtenerEntradasAsignadasPorFestival(idFestival, idPromotor);

            // Pasar datos al JSP
            request.setAttribute("festival", festival);
            request.setAttribute("entradasAsignadas", listaEntradas);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);

            // Forward al JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-entradas-asignadas.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e; // Dejar que JAX-RS maneje 403/404
        } catch (Exception e) {
            log.error("Error al listar entradas asignadas para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar las entradas asignadas.", e);
        }
    }

    /**
     * Endpoint POST para nominar una entrada asignada a un asistente. Recibe el
     * email, nombre y teléfono (opcional) del asistente desde el formulario. El
     * servicio buscará o creará el asistente por email. Redirige a la lista de
     * entradas asignadas del festival. Requiere rol PROMOTOR en sesión y ser
     * dueño del festival asociado a la entrada.
     *
     * @param idEntradaAsignada ID de la entrada asignada a nominar.
     * @param emailAsistente Email del asistente (obligatorio).
     * @param nombreAsistente Nombre del asistente (obligatorio si el asistente
     * es nuevo).
     * @param telefonoAsistente Teléfono del asistente (opcional).
     * @return Una respuesta de redirección (303) a la lista de entradas
     * asignadas del festival.
     * @throws BadRequestException Si faltan parámetros obligatorios (ID
     * entrada, email).
     */
    @POST
    @Path("/entradas-asignadas/{idEntradaAsignada}/nominar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response nominarEntrada(
            @PathParam("idEntradaAsignada") Integer idEntradaAsignada,
            @FormParam("emailAsistente") String emailAsistente,
            @FormParam("nombreAsistente") String nombreAsistente,
            @FormParam("telefonoAsistente") String telefonoAsistente) {

        log.info("POST /promotor/entradas-asignadas/{}/nominar recibido para asistente email {}", idEntradaAsignada, emailAsistente);
        Integer idPromotor = verificarAccesoPromotor(request);

        if (idEntradaAsignada == null || emailAsistente == null || emailAsistente.isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos (idEntradaAsignada, emailAsistente).");
        }
        // El servicio validará si el nombre es necesario

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir

        try {
            // Llamar al servicio para nominar (el servicio verifica propiedad y estado)
            entradaAsignadaService.nominarEntrada(idEntradaAsignada, emailAsistente, nombreAsistente, telefonoAsistente, idPromotor);
            mensajeFlash = "Entrada ID " + idEntradaAsignada + " nominada correctamente al asistente con email " + emailAsistente + ".";

            // Obtener el ID del festival para la redirección
            Optional<EntradaAsignadaDTO> optDto = entradaAsignadaService.obtenerEntradaAsignadaPorId(idEntradaAsignada, idPromotor);
            idFestival = optDto.map(EntradaAsignadaDTO::getIdFestival).orElse(null);
            if (idFestival == null && mensajeFlash != null) {
                log.warn("No se pudo obtener ID de festival de entrada {} tras nominar.", idEntradaAsignada);
            }

        } catch (EntradaAsignadaNotFoundException | UsuarioNotFoundException | IllegalArgumentException e) {
            errorFlash = e.getMessage(); // Errores de datos o no encontrado
            log.warn("Error al nominar entrada ID {}: {}", idEntradaAsignada, errorFlash);
        } catch (SecurityException | IllegalStateException e) {
            errorFlash = "No se pudo nominar la entrada: " + e.getMessage(); // Errores de permiso o estado
            log.warn("Error de negocio/seguridad al nominar entrada ID {}: {}", idEntradaAsignada, errorFlash);
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al nominar la entrada.";
            log.error("Error interno al nominar entrada ID {}: {}", idEntradaAsignada, e.getMessage(), e);
        }

        // Guardar mensaje/error y redirigir
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        // Redirigir a la lista de entradas asignadas del festival si tenemos el ID, sino a la lista general de festivales
        URI redirectUri;
        if (idFestival != null) {
            redirectUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "listarEntradasAsignadas")
                    .resolveTemplate("idFestival", idFestival)
                    .build();
        } else {
            redirectUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build(); // Fallback
        }
        return Response.seeOther(redirectUri).build(); // 303 See Other
    }

    /**
     * Endpoint POST para cancelar una entrada asignada. El servicio se encarga
     * de verificar permisos, estado de la entrada y de restaurar el stock del
     * tipo de entrada original. Redirige a la lista de entradas asignadas del
     * festival. Requiere rol PROMOTOR en sesión y ser dueño del festival
     * asociado.
     *
     * @param idEntradaAsignada ID de la entrada asignada a cancelar.
     * @return Una respuesta de redirección (303) a la lista de entradas
     * asignadas.
     * @throws BadRequestException Si falta el ID.
     */
    @POST
    @Path("/entradas-asignadas/{idEntradaAsignada}/cancelar")
    public Response cancelarEntrada(@PathParam("idEntradaAsignada") Integer idEntradaAsignada) {
        log.info("POST /promotor/entradas-asignadas/{}/cancelar recibido", idEntradaAsignada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idEntradaAsignada == null) {
            throw new BadRequestException("Falta idEntradaAsignada.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir

        try {
            // Obtener ID del festival ANTES de cancelar, por si falla la cancelación pero necesitamos redirigir
            Optional<EntradaAsignadaDTO> optDto = entradaAsignadaService.obtenerEntradaAsignadaPorId(idEntradaAsignada, idPromotor);
            if (optDto.isPresent()) {
                idFestival = optDto.get().getIdFestival();
                // Llamar al servicio para cancelar (verifica propiedad y estado)
                entradaAsignadaService.cancelarEntrada(idEntradaAsignada, idPromotor);
                mensajeFlash = "Entrada ID " + idEntradaAsignada + " cancelada correctamente (stock restaurado).";
            } else {
                // Si no se encontró la entrada o no tenía permiso para verla
                errorFlash = "Entrada no encontrada o no tiene permiso para cancelarla.";
                log.warn("Intento de cancelar entrada ID {} fallido (no encontrada o sin permiso) por promotor {}", idEntradaAsignada, idPromotor);
            }
        } catch (EntradaAsignadaNotFoundException | UsuarioNotFoundException e) {
            errorFlash = e.getMessage();
            log.warn("Error al cancelar entrada ID {}: {}", idEntradaAsignada, e.getMessage());
        } catch (SecurityException | IllegalStateException e) {
            errorFlash = "No se pudo cancelar la entrada: " + e.getMessage();
            log.warn("Error negocio/seguridad al cancelar entrada ID {}: {}", idEntradaAsignada, e.getMessage());
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al cancelar la entrada.";
            log.error("Error interno al cancelar entrada ID {}: {}", idEntradaAsignada, e.getMessage(), e);
        }

        // Guardar mensaje/error y redirigir
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        // Redirigir a la lista de entradas asignadas si tenemos ID festival, sino a lista general
        URI redirectUri;
        if (idFestival != null) {
            redirectUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "listarEntradasAsignadas")
                    .resolveTemplate("idFestival", idFestival)
                    .build();
        } else {
            redirectUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build(); // Fallback
        }
        return Response.seeOther(redirectUri).build(); // 303 See Other
    }

    /**
     * Endpoint POST para asociar una pulsera NFC a una entrada asignada. Recibe
     * el UID de la pulsera desde el formulario. Redirige a la lista de entradas
     * asignadas del festival. Requiere rol PROMOTOR en sesión y ser dueño del
     * festival asociado.
     *
     * @param idEntradaAsignada ID de la entrada asignada a la que asociar la
     * pulsera.
     * @param codigoUid UID de la pulsera a asociar.
     * @return Una respuesta de redirección (303) a la lista de entradas
     * asignadas.
     * @throws BadRequestException Si faltan parámetros.
     */
    @POST
    @Path("/entradas-asignadas/{idEntradaAsignada}/asociar-pulsera")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response asociarPulseraPromotor(
            @PathParam("idEntradaAsignada") Integer idEntradaAsignada,
            @FormParam("codigoUid") String codigoUid) {

        log.info("POST /promotor/entradas-asignadas/{}/asociar-pulsera con UID: {}", idEntradaAsignada, codigoUid);
        Integer idPromotor = verificarAccesoPromotor(request); // Verifica sesión y rol

        if (idEntradaAsignada == null || codigoUid == null || codigoUid.isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos (idEntradaAsignada, codigoUid).");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir

        try {
            // Obtener ID del festival ANTES de asociar, para la redirección
            Optional<EntradaAsignadaDTO> optDto = entradaAsignadaService.obtenerEntradaAsignadaPorId(idEntradaAsignada, idPromotor);
            if (optDto.isPresent()) {
                idFestival = optDto.get().getIdFestival();
                // Llamar al servicio para asociar la pulsera (verifica propiedad y estado)
                pulseraNFCService.asociarPulseraEntrada(codigoUid, idEntradaAsignada, idPromotor);
                mensajeFlash = "Pulsera con UID '" + codigoUid + "' asociada correctamente a la entrada ID " + idEntradaAsignada + ".";
            } else {
                errorFlash = "Entrada no encontrada o no tiene permiso.";
                log.warn("Intento de asociar pulsera a entrada ID {} fallido (no encontrada o sin permiso) por promotor {}", idEntradaAsignada, idPromotor);
            }
        } catch (EntradaAsignadaNotFoundException | UsuarioNotFoundException | IllegalArgumentException e) {
            errorFlash = e.getMessage();
            log.warn("Error al asociar pulsera a entrada ID {}: {}", idEntradaAsignada, errorFlash);
        } catch (SecurityException | IllegalStateException | PulseraYaAsociadaException | EntradaAsignadaNoNominadaException e) {
            errorFlash = "No se pudo asociar la pulsera: " + e.getMessage();
            log.warn("Error negocio/seguridad al asociar pulsera a entrada ID {}: {}", idEntradaAsignada, errorFlash);
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al asociar la pulsera.";
            log.error("Error interno al asociar pulsera a entrada ID {}: {}", idEntradaAsignada, e.getMessage(), e);
        }

        // Guardar mensaje/error y redirigir
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        // Redirigir a la lista de entradas asignadas si tenemos ID festival, sino a lista general
        URI redirectUri;
        if (idFestival != null) {
            redirectUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "listarEntradasAsignadas")
                    .resolveTemplate("idFestival", idFestival)
                    .build();
        } else {
            redirectUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build(); // Fallback
        }
        return Response.seeOther(redirectUri).build(); // 303 See Other
    }

    // --- Endpoint para Listar Asistentes por Festival ---
    /**
     * Endpoint GET para mostrar la lista de asistentes únicos con entradas para
     * un festival específico del promotor autenticado. Realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-festival-asistentes.jsp}. Requiere
     * rol PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestival ID del festival cuyos asistentes se listarán.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene
     * éxito).
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el festival no se encuentra.
     * @throws ForbiddenException Si el promotor no es dueño del festival.
     * @throws InternalServerErrorException Si ocurre un error interno al cargar
     * datos.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/{idFestival}/asistentes")
    @Produces(MediaType.TEXT_HTML)
    public Response listarAsistentesPorFestival(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/{}/asistentes recibido", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request); // Verifica sesión y rol
        if (idFestival == null) {
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            // Obtener datos del festival y verificar propiedad
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            // Obtener lista de asistentes DTOs para este festival (el servicio verifica permisos)
            List<AsistenteDTO> listaAsistentes = asistenteService.obtenerAsistentesPorFestival(idFestival, idPromotor);

            // Pasar datos al JSP
            request.setAttribute("festival", festival);
            request.setAttribute("asistentes", listaAsistentes);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);

            // Forward al JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-festival-asistentes.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e; // Dejar que JAX-RS maneje 403/404
        } catch (Exception e) {
            log.error("Error al listar asistentes para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar los asistentes del festival.", e);
        }
    }

    // --- Endpoint para Simular Venta ---
    /**
     * Endpoint POST para simular el registro de una venta de entradas. Útil
     * para pruebas durante el desarrollo para generar entradas asignadas.
     * Espera idEntrada, idAsistente y cantidad como parámetros de formulario.
     * Redirige a la vista de detalle del festival. Requiere rol PROMOTOR en
     * sesión y ser dueño del festival asociado a la entrada.
     *
     * @param idFestival ID del festival (usado para validación y redirección).
     * @param idEntrada ID del tipo de entrada a "vender".
     * @param idAsistente ID del asistente al que se "vende".
     * @param cantidad Número de entradas a "vender".
     * @return Una respuesta de redirección (303) a la vista de detalle del
     * festival.
     * @throws BadRequestException Si faltan parámetros o la cantidad es
     * inválida.
     */
    @POST
    @Path("/festivales/{idFestival}/simular-venta")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response simularVenta(
            @PathParam("idFestival") Integer idFestival,
            @FormParam("idEntrada") Integer idEntrada,
            @FormParam("idAsistente") Integer idAsistente,
            @FormParam("cantidad") Integer cantidad) {

        log.info("POST /promotor/festivales/{}/simular-venta recibido -> Entrada ID: {}, Asistente ID: {}, Cantidad: {}",
                idFestival, idEntrada, idAsistente, cantidad);
        Integer idPromotor = verificarAccesoPromotor(request); // Verifica sesión y rol

        // Validaciones básicas
        if (idFestival == null || idEntrada == null || idAsistente == null || cantidad == null) {
            throw new BadRequestException("Faltan parámetros requeridos (idFestival, idEntrada, idAsistente, cantidad).");
        }
        if (cantidad <= 0) {
            throw new BadRequestException("La cantidad debe ser positiva.");
        }

        String mensajeFlash = null;
        String errorFlash = null;

        try {
            // Antes de llamar a ventaService, verificar que el promotor es dueño del festival
            Optional<FestivalDTO> festivalOpt = festivalService.obtenerFestivalPorId(idFestival);
            if (festivalOpt.isEmpty() || !festivalOpt.get().getIdPromotor().equals(idPromotor)) {
                throw new SecurityException("No tiene permiso para operar sobre este festival.");
            }

            // Llamar al servicio de venta
            ventaService.registrarVenta(idAsistente, idEntrada, cantidad);
            mensajeFlash = cantidad + " entrada(s) del tipo ID " + idEntrada + " generada(s) exitosamente para el asistente ID " + idAsistente + ".";

        } catch (AsistenteNotFoundException | EntradaNotFoundException | FestivalNotFoundException
                | FestivalNoPublicadoException | StockInsuficienteException | IllegalArgumentException | SecurityException e) {
            // Errores de negocio o validación esperados
            log.warn("Error al simular venta para festival {}: {}", idFestival, e.getMessage());
            errorFlash = "Error al simular venta: " + e.getMessage();
        } catch (Exception e) {
            // Errores inesperados
            log.error("Error interno inesperado al simular venta para festival {}: {}", idFestival, e.getMessage(), e);
            errorFlash = "Error interno inesperado durante la simulación de venta.";
        }

        // Guardar mensaje/error en sesión
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        // Redirigir siempre a la página de VISTA del festival donde se hizo la simulación
        URI detailUri = uriInfo.getBaseUriBuilder()
                .path(PromotorResource.class) // Clase actual
                .path(PromotorResource.class, "mostrarDetallesFestival") // Método GET de VISTA
                .resolveTemplate("id", idFestival) // Reemplazar {id}
                .build();
        log.debug("Redirigiendo a: {}", detailUri);
        return Response.seeOther(detailUri).build(); // 303 See Other
    }

    // --- Endpoints para Cambio de Contraseña Obligatorio ---
    /**
     * Endpoint GET para mostrar el formulario de cambio de contraseña
     * obligatorio. Se accede típicamente después de un login exitoso si el flag
     * 'cambioPasswordRequerido' del usuario es true. Realiza forward al JSP
     * {@code /WEB-INF/jsp/cambiar-password-obligatorio.jsp}. Requiere sesión
     * activa (no necesariamente rol PROMOTOR, podría ser cualquier rol).
     *
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene
     * éxito).
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     * @throws NotAuthorizedException Si no hay sesión activa o es inválida.
     */
    @GET
    @Path("/mostrar-cambio-password")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioCambioPassword() throws ServletException, IOException {
        log.debug("GET /promotor/mostrar-cambio-password recibido");
        HttpSession session = request.getSession(false);
        // Verificar que hay sesión y userId (no necesita verificar rol aquí)
        if (session == null || session.getAttribute("userId") == null) {
            log.warn("Intento de acceso a mostrar-cambio-password sin sesión válida.");
            // Redirigir al login si no hay sesión
            try {
                // Construir URL de login relativa al contexto de la aplicación
                URI loginUri = new URI(request.getContextPath() + "/login.jsp?error=session_required");
                return Response.seeOther(loginUri).build();
            } catch (URISyntaxException e) {
                log.error("Error creando URI para login", e);
                return Response.serverError().entity("Error interno").build();
            }
        }
        Integer userId = (Integer) session.getAttribute("userId");
        log.debug("Mostrando formulario de cambio de contraseña obligatorio para userId: {}", userId);

        mostrarMensajeFlash(request); // Mostrar errores de intento previo si los hubo

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/cambiar-password-obligatorio.jsp");
        dispatcher.forward(request, response);
        return Response.ok().build();
    }

    /**
     * Endpoint POST para procesar el cambio de contraseña obligatorio. Recibe
     * la nueva contraseña y su confirmación desde el formulario. Llama al
     * servicio para actualizar la contraseña y quitar el flag 'requerido'.
     * Redirige al dashboard correspondiente al rol del usuario si tiene éxito,
     * o de vuelta al formulario de cambio si falla. Requiere sesión activa.
     *
     * @param newPassword Nueva contraseña introducida (mínimo 8 caracteres).
     * @param confirmPassword Confirmación de la nueva contraseña (debe
     * coincidir).
     * @return Una respuesta de redirección (303) al dashboard o al formulario
     * de cambio.
     */
    @POST
    @Path("/cambiar-password-obligatorio")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response procesarCambioPasswordObligatorio(
            @FormParam("newPassword") String newPassword,
            @FormParam("confirmPassword") String confirmPassword) {

        HttpSession session = request.getSession(false);
        String errorMessage = null;
        URI redirectUri = null; // URI para redirección

        // Verificar sesión y obtener userId y rol
        if (session == null || session.getAttribute("userId") == null || session.getAttribute("userRole") == null) {
            log.error("Intento de cambiar contraseña obligatorio sin sesión o datos de usuario válidos.");
            try {
                redirectUri = new URI(request.getContextPath() + "/login.jsp?error=session_expired");
                return Response.seeOther(redirectUri).build();
            } catch (URISyntaxException e) {
                return Response.serverError().entity("Error interno").build();
            }
        }
        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        log.info("POST /promotor/cambiar-password-obligatorio para userId: {}", userId);

        // Validar contraseñas
        if (newPassword == null || newPassword.isEmpty() || !newPassword.equals(confirmPassword)) {
            errorMessage = "Las contraseñas no coinciden o están vacías.";
            log.warn("Error validación contraseña obligatoria (userId: {}): {}", userId, errorMessage);
            session.setAttribute("passwordChangeError", errorMessage); // Usar clave específica
            return redirectBackToChangePasswordForm();
        }
        if (newPassword.length() < 8) { // Requisito mínimo
            errorMessage = "La nueva contraseña debe tener al menos 8 caracteres.";
            log.warn("Error complejidad contraseña obligatoria (userId: {}): {}", userId, errorMessage);
            session.setAttribute("passwordChangeError", errorMessage);
            return redirectBackToChangePasswordForm();
        }

        try {
            // Llamar al servicio para cambiar contraseña y quitar flag
            log.debug("Llamando a usuarioService.cambiarPasswordYMarcarActualizada para userId: {}", userId);
            usuarioService.cambiarPasswordYMarcarActualizada(userId, newPassword);
            log.info("Contraseña obligatoria cambiada y flag actualizado para userId: {}", userId);

            // Limpiar error de sesión si existía y poner mensaje de éxito
            session.removeAttribute("passwordChangeError");
            session.setAttribute("mensaje", "Contraseña actualizada correctamente. ¡Bienvenido!");

            // Redirigir al dashboard apropiado según el rol guardado en sesión
            String dashboardUrl = determineDashboardUrlFromRole(userRole);
            log.debug("Redirigiendo a dashboard: {}", dashboardUrl);
            redirectUri = new URI(request.getContextPath() + dashboardUrl);
            return Response.seeOther(redirectUri).build(); // 303 See Other

        } catch (Exception e) { // Captura UsuarioNotFound, IllegalArgument o RuntimeException del servicio
            log.error("Error al actualizar contraseña obligatoria para userId {}: {}", userId, e.getMessage(), e);
            session.setAttribute("passwordChangeError", "Error al guardar la nueva contraseña: " + e.getMessage());
            return redirectBackToChangePasswordForm();
        }
    }

    // --- Endpoint para Listar Pulseras por Festival ---
    /**
     * Endpoint GET para mostrar la lista de pulseras NFC asociadas a un
     * festival específico del promotor autenticado. Realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-festival-pulseras.jsp}. Requiere
     * rol PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestival ID del festival cuyas pulseras se listarán.
     * @return Una respuesta JAX-RS (implícitamente OK si el forward tiene
     * éxito).
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el festival no se encuentra.
     * @throws ForbiddenException Si el promotor no es dueño del festival.
     * @throws InternalServerErrorException Si ocurre un error interno al cargar
     * datos.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/{idFestival}/pulseras")
    @Produces(MediaType.TEXT_HTML)
    public Response listarPulserasPorFestivalPromotor(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/{}/pulseras recibido", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request); // Verifica sesión y rol
        if (idFestival == null) {
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            // Obtener datos del festival y verificar propiedad
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> f.getIdPromotor().equals(idPromotor))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            // Obtener lista de pulseras DTOs para este festival (servicio verifica permiso)
            List<PulseraNFCDTO> listaPulseras = pulseraNFCService.obtenerPulserasPorFestival(idFestival, idPromotor);

            // Pasar datos al JSP
            request.setAttribute("festival", festival);
            request.setAttribute("pulseras", listaPulseras);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);

            // Forward al JSP de pulseras del festival para promotor
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-festival-pulseras.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e; // Dejar que JAX-RS maneje 403/404
        } catch (Exception e) {
            log.error("Error al listar pulseras para festival ID {}: {}", idFestival, e.getMessage(), e);
            // Redirigir a la página de VISTA del festival con un mensaje de error
            setFlashMessage(request, "error", "Error al cargar las pulseras del festival.");
            URI detailUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarDetallesFestival") // VISTA festival
                    .resolveTemplate("id", idFestival)
                    .build();
            return Response.seeOther(detailUri).build();
        }
    }

    // --- Métodos Auxiliares (Seguridad, Forward, Mensajes Flash) ---
    /**
     * Realiza un forward al JSP del formulario de edición de festival del
     * promotor, pasando un mensaje de error y los datos del DTO que causaron el
     * error. Usado cuando falla la operación de guardado.
     *
     * @param dto DTO con los datos introducidos que causaron el error.
     * @param idPromotor ID del promotor autenticado.
     * @param esEdicion {@code true} si era una edición, {@code false} si era
     * creación.
     * @param errorMessage Mensaje de error a mostrar en el formulario.
     * @throws ServletException Si ocurre un error durante el forward.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    private void forwardToPromotorFestivalEditFormWithError(FestivalDTO dto, Integer idPromotor, boolean esEdicion, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.setAttribute("festival", dto);
        request.setAttribute("esNuevo", !esEdicion); // Establecer modo correcto
        request.setAttribute("idPromotorAutenticado", idPromotor);
        // Forward al JSP de EDICIÓN
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-festival-editar.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Verifica si existe una sesión HTTP activa y si el usuario autenticado en
     * ella tiene el rol PROMOTOR.
     *
     * @param request La petición HTTP actual.
     * @return El ID del usuario promotor autenticado.
     * @throws NotAuthorizedException Si no hay sesión activa o es inválida.
     * @throws ForbiddenException Si el usuario en sesión no tiene rol PROMOTOR.
     */
    private Integer verificarAccesoPromotor(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // No crear sesión si no existe
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
        return userId; // Devolver ID del promotor autenticado
    }

    /**
     * Redirige de vuelta al formulario de cambio de contraseña obligatorio.
     * Usado cuando falla la validación o el guardado.
     *
     * @return Una respuesta de redirección (303).
     */
    private Response redirectBackToChangePasswordForm() {
        try {
            // Construye la URI para el método GET que muestra el formulario
            URI formUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class) // Clase actual
                    .path(PromotorResource.class, "mostrarFormularioCambioPassword") // Método GET
                    .build();
            log.debug("Redirigiendo de vuelta al formulario de cambio de contraseña: {}", formUri);
            return Response.seeOther(formUri).build(); // 303 See Other
        } catch (Exception e) {
            // Error construyendo la URI (poco probable)
            log.error("Error al crear URI para redirección a cambiar password obligatorio: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno al intentar redirigir al formulario.")
                    .build();
        }
    }

    /**
     * Determina la URL relativa del dashboard al que redirigir después de un
     * login o cambio de contraseña exitoso, basado en el rol del usuario.
     *
     * @param roleName El nombre del rol del usuario (ej: "PROMOTOR", "ADMIN").
     * @return La URL relativa del dashboard correspondiente (ej:
     * "/api/promotor/festivales").
     */
    private String determineDashboardUrlFromRole(String roleName) {
        // Este método podría vivir en una clase de utilidad o configuración si se usa en más sitios
        if (RolUsuario.PROMOTOR.name().equalsIgnoreCase(roleName)) {
            // Devuelve la ruta relativa al contexto para el dashboard del promotor
            return "/api/promotor/festivales";
        }
        // Añadir otros roles si fuera necesario
        // if (RolUsuario.ADMIN.name().equalsIgnoreCase(roleName)) { return "/api/admin/dashboard"; }
        log.warn("Rol inesperado '{}' al determinar URL de dashboard desde PromotorResource.", roleName);
        // Fallback a la página de login con error si el rol no es reconocido
        return "/login.jsp?error=unexpected_role";
    }

    /**
     * Establece un mensaje flash (de éxito o error) en la sesión HTTP.
     *
     * @param request La petición actual.
     * @param type "mensaje" para éxito, "error" para error.
     * @param message El mensaje a guardar. Si es null, no se guarda nada.
     */
    private void setFlashMessage(HttpServletRequest request, String type, String message) {
        if (message != null) {
            HttpSession session = request.getSession(); // Obtener o crear sesión
            session.setAttribute(type, message);
            log.trace("Mensaje flash '{}' guardado en sesión con clave '{}'", message, type);
        }
    }

    /**
     * Comprueba si existen mensajes flash ("mensaje", "error",
     * "passwordChangeError", "nuevaEntradaConError", "errorEntrada") en la
     * sesión y, si es así, los mueve a atributos de la request y los elimina de
     * la sesión.
     *
     * @param request La petición actual.
     */
    private void mostrarMensajeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // No crear sesión si no existe
        if (session != null) {
            // Mover mensaje de éxito
            String mensaje = (String) session.getAttribute("mensaje");
            if (mensaje != null) {
                request.setAttribute("mensajeExito", mensaje); // Usar clave consistente en JSP
                session.removeAttribute("mensaje");
                log.trace("Mensaje flash de éxito movido de sesión a request.");
            }
            // Mover mensaje de error genérico
            String error = (String) session.getAttribute("error");
            if (error != null) {
                request.setAttribute("error", error); // Usar clave consistente en JSP
                session.removeAttribute("error");
                log.trace("Mensaje flash de error movido de sesión a request.");
            }
            // Mover error específico de cambio de contraseña
            String passwordError = (String) session.getAttribute("passwordChangeError");
            if (passwordError != null) {
                request.setAttribute("error", passwordError); // Usar clave genérica 'error' en request
                session.removeAttribute("passwordChangeError");
                log.trace("Mensaje flash 'passwordChangeError' movido de sesión a request.");
            }
            // Mover DTO de entrada con error (para repintar form de añadir entrada)
            EntradaDTO entradaConError = (EntradaDTO) session.getAttribute("nuevaEntradaConError");
            if (entradaConError != null) {
                request.setAttribute("nuevaEntrada", entradaConError); // Usar clave consistente en JSP
                session.removeAttribute("nuevaEntradaConError");
                log.trace("Atributo flash 'nuevaEntradaConError' movido de sesión a request.");
            }
            // Mover flag de error de entrada
            Boolean errorEntrada = (Boolean) session.getAttribute("errorEntrada");
            if (errorEntrada != null && errorEntrada) {
                request.setAttribute("errorEntrada", true); // Pasar flag a la request
                session.removeAttribute("errorEntrada");
                log.trace("Flag flash 'errorEntrada' movido de sesión a request.");
            }
        }
    }
}
