package com.daw2edudiego.beatpasstfg.web;

import com.daw2edudiego.beatpasstfg.dto.*;
import com.daw2edudiego.beatpasstfg.exception.*;
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
 * <li>Entradas: Listar por festival, nominar a asistentes, cancelar, asociar
 * pulseras.</li>
 * <li>Asistentes: Listar los asociados a sus festivales.</li>
 * <li>Pulseras NFC: Listar las asociadas a sus festivales.</li>
 * <li>Cambio de Contraseña: Gestionar el cambio de contraseña obligatorio
 * inicial.</li>
 * </ul>
 * La autenticación y autorización se basa en la validación de una sesión HTTP
 * existente y la verificación de que el usuario autenticado en sesión tenga el
 * rol PROMOTOR. Las respuestas son principalmente HTML, realizando forwards a
 * archivos JSP ubicados en {@code /WEB-INF/jsp/promotor/}. Utiliza el patrón
 * Post-Redirect-Get (PRG) con mensajes flash en sesión para operaciones POST.
 * </p>
 *
 * @see FestivalService
 * @see TipoEntradaService
 * @see EntradaService
 * @see VentaService
 * @see AsistenteService
 * @see PulseraNFCService
 * @see UsuarioService
 * @see CompraService
 * @author Eduardo Olalde
 */
@Path("/promotor")
public class PromotorResource {

    private static final Logger log = LoggerFactory.getLogger(PromotorResource.class);

    // Services (Manual Dependency Injection)
    private final FestivalService festivalService;
    private final UsuarioService usuarioService;
    private final TipoEntradaService tipoEntradaService;
    private final EntradaService entradaService;
    private final VentaService ventaService;
    private final AsistenteService asistenteService;
    private final PulseraNFCService pulseraNFCService;
    private final CompraService compraService;

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
        this.tipoEntradaService = new TipoEntradaServiceImpl();
        this.entradaService = new EntradaServiceImpl();
        this.ventaService = new VentaServiceImpl();
        this.asistenteService = new AsistenteServiceImpl();
        this.pulseraNFCService = new PulseraNFCServiceImpl();
        this.compraService = new CompraServiceImpl();
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
     * @param idFestivalParam ID del festival a visualizar, obtenido del path.
     * @return Una respuesta JAX-RS OK si el forward tiene éxito.
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
        final Integer idFestival = idFestivalParam; // Usar variable final para claridad

        if (idFestival == null || idFestival <= 0) { // Añadir validación > 0
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            log.debug("Buscando festival con ID: {}", idFestival);
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> idPromotor.equals(f.getIdPromotor())) // Asegurar comparación correcta de Integer
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            log.debug("Obteniendo tipos de entrada para festival ID: {}", idFestival);
            List<TipoEntradaDTO> listaTiposEntrada = tipoEntradaService.obtenerTipoEntradasPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("tiposEntrada", listaTiposEntrada);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/festival-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) { // NotFound podría venir de los servicios
            throw e; // Dejar que JAX-RS maneje 403 o 404
        } catch (Exception e) {
            log.error("Error al mostrar detalles para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del festival.", e);
        }
    }

    /**
     * Endpoint GET para mostrar el formulario de edición de los datos básicos
     * de un festival existente perteneciente al promotor autenticado. Carga
     * solo los datos necesarios para el formulario y realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-festival-editar.jsp}. Requiere rol
     * PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestivalParam ID del festival a editar, obtenido del path.
     * @return Una respuesta JAX-RS OK si el forward tiene éxito.
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

        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            log.debug("Buscando festival con ID: {} para editar", idFestival);
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> idPromotor.equals(f.getIdPromotor()))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            request.setAttribute("festival", festival);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            request.setAttribute("esNuevo", false); // Indicar modo edición
            mostrarMensajeFlash(request); // Mostrar errores de intento previo si los hubo

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-festival-editar.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al mostrar formulario de edición para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar datos del festival para editar.", e);
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
        Integer idPromotor = verificarAccesoPromotor(request);

        boolean esNuevo = (idStr == null || idStr.isBlank() || "0".equals(idStr));
        Integer idFestival = null;
        FestivalDTO dto = new FestivalDTO(); // Para mapear datos y posible reenvío al form en error
        String errorMessage = null;
        FestivalDTO festivalGuardado = null; // Para obtener ID si es nuevo

        try {
            // Intentar parsear ID si es actualización
            if (!esNuevo) {
                try {
                    idFestival = Integer.parseInt(idStr);
                    if (idFestival <= 0) {
                        throw new NumberFormatException(); // ID debe ser positivo
                    }
                    dto.setIdFestival(idFestival); // Establecer ID en el DTO si es actualización
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("ID de festival inválido para actualización: " + idStr);
                }
            }

            // Validar y poblar DTO con datos del formulario
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre del festival es obligatorio.");
            }
            dto.setNombre(nombre); // Se setean en el DTO para repoblar el form en caso de error
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
                try {
                    dto.setAforo(Integer.parseInt(aforoStr));
                    if (dto.getAforo() <= 0) {
                        throw new IllegalArgumentException("El aforo debe ser un número positivo.");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Formato numérico inválido para Aforo.");
                }
            }
            // El ID del promotor se pasa directamente al servicio, no se necesita en el DTO para la lógica del servicio
            dto.setIdPromotor(idPromotor); // Añadir promotor al DTO para repoblar form

            // Llamar al servicio correspondiente
            String mensajeExito;
            if (esNuevo) {
                log.debug("Llamando a festivalService.crearFestival para promotor {}", idPromotor);
                festivalGuardado = festivalService.crearFestival(dto, idPromotor);
                idFestival = festivalGuardado.getIdFestival(); // Obtener el ID del nuevo festival
                mensajeExito = "Solicitud de festival '" + festivalGuardado.getNombre() + "' creada correctamente (estado BORRADOR).";
            } else {
                log.debug("Llamando a festivalService.actualizarFestival ID {} para promotor {}", idFestival, idPromotor);
                // El servicio actualizarFestival verifica la propiedad y NO cambia el estado
                festivalService.actualizarFestival(idFestival, dto, idPromotor);
                // idFestival ya tiene el valor correcto
                mensajeExito = "Festival '" + dto.getNombre() + "' actualizado correctamente.";
            }

            // Éxito: Mensaje flash y redirección a la página de VISTA del festival
            setFlashMessage(request, "mensaje", mensajeExito);
            if (idFestival == null) {
                log.error("No se pudo obtener idFestival para redirigir después de guardar. Redirigiendo a lista general.");
                URI fallbackUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build();
                return Response.seeOther(fallbackUri).build();
            }
            // CORRECCIÓN: Usar UriBuilder para construir la URI y resolver el template 'id' con el valor de idFestival
            URI viewUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class) // Añade /api/promotor
                    .path(PromotorResource.class, "mostrarDetallesFestival") // Añade /festivales/ver/{id}
                    .resolveTemplate("id", idFestival) // Reemplaza {id} con el valor de idFestival
                    .build();
            log.debug("Redirección post-guardado a: {}", viewUri);
            return Response.seeOther(viewUri).build(); // 303 See Other

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
     * @param idFestival ID del festival al que se añade la entrada, obtenido
     * del path.
     * @param tipo Nombre del tipo de entrada (ej: "General", obligatorio).
     * @param descripcion Descripción (opcional).
     * @param precioStr Precio (String, obligatorio, se parseará a BigDecimal).
     * @param stockStr Stock inicial (String, obligatorio, se parseará a
     * Integer).
     * @param requiereNominacion
     * @return Una respuesta de redirección (303) a la vista de detalle del
     * festival.
     * @throws BadRequestException Si el ID del festival no es válido.
     */
    @POST
    @Path("/festivales/{idFestival}/tiposEntrada")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response guardarTipoEntrada(
            @PathParam("idFestival") Integer idFestival,
            @FormParam("tipo") String tipo,
            @FormParam("descripcion") String descripcion,
            @FormParam("precio") String precioStr,
            @FormParam("stock") String stockStr,
            @FormParam("requiereNominacion") Boolean requiereNominacion) {

        log.info("POST /promotor/festivales/{}/tiposEntrada recibido", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID festival inválido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        TipoEntradaDTO dto = new TipoEntradaDTO();
        dto.setIdFestival(idFestival);

        try {
            // Validar y poblar DTO
            if (tipo == null || tipo.isBlank()) {
                throw new IllegalArgumentException("El nombre del tipo de entrada es obligatorio.");
            }
            dto.setTipo(tipo); // Poblar DTO para posible reenvío en error
            dto.setDescripcion(descripcion);
            if (precioStr == null || precioStr.isBlank()) {
                throw new IllegalArgumentException("El precio es obligatorio.");
            }
            if (stockStr == null || stockStr.isBlank()) {
                throw new IllegalArgumentException("El stock es obligatorio.");
            }
            dto.setRequiereNominacion(requiereNominacion != null && requiereNominacion);

            // Parsear precio y stock
            try {
                dto.setPrecio(new BigDecimal(precioStr.replace(',', '.'))); // Permitir coma decimal
                if (dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("El precio no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato numérico inválido para Precio.");
            }
            try {
                dto.setStock(Integer.parseInt(stockStr));
                if (dto.getStock() < 0) {
                    throw new IllegalArgumentException("El stock no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato numérico inválido para Stock.");
            }

            // Llamar al servicio para crear la entrada (el servicio verifica propiedad del festival)
            TipoEntradaDTO tipoEntradaCreado = tipoEntradaService.crearTipoEntrada(dto, idFestival, idPromotor);
            mensajeFlash = "Tipo de entrada '" + tipoEntradaCreado.getTipo() + "' añadido correctamente.";

        } catch (IllegalArgumentException | SecurityException | FestivalNotFoundException e) {
            errorFlash = "Error al añadir tipo de entrada: " + e.getMessage();
            log.warn("Error de validación/negocio al crear tipo de entrada para festival {}: {}", idFestival, errorFlash);
            // DTO ya está poblado para repintar el formulario
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al añadir el tipo de entrada.";
            log.error("Error interno al crear tipo de entrada para festival {}: {}", idFestival, e.getMessage(), e);
            // DTO ya está poblado para repintar el formulario
        }

        // Guardar mensaje/error en sesión
        HttpSession session = request.getSession(); // Obtener o crear sesión
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

        // Redirigir siempre a la página de VISTA del festival
        URI detailUri = uriInfo.getBaseUriBuilder()
                .path(PromotorResource.class)
                .path(PromotorResource.class, "mostrarDetallesFestival")
                .resolveTemplate("id", idFestival)
                .build();
        return Response.seeOther(detailUri).build(); // 303 See Other
    }

    /**
     * Endpoint GET para mostrar el formulario de edición de un tipo de entrada
     * específico. Realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-entrada-detalle.jsp}. Requiere rol
     * PROMOTOR en sesión y ser dueño del festival asociado.
     *
     * @param idTipoEntrada ID del tipo de entrada a editar, obtenido del path.
     * @return Una respuesta JAX-RS OK si el forward tiene éxito.
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
    @Path("/tiposEntrada/{idTipoEntrada}/editar")
    @Produces(MediaType.TEXT_HTML)
    public Response mostrarFormularioEditarEntrada(@PathParam("idTipoEntrada") Integer idTipoEntrada) throws ServletException, IOException {
        log.debug("GET /promotor/tiposEntrada/{}/editar recibido", idTipoEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idTipoEntrada == null || idTipoEntrada <= 0) {
            throw new BadRequestException("ID de entrada no válido.");
        }

        try {
            // Obtener el DTO de la entrada, el servicio verifica propiedad
            TipoEntradaDTO tipoEntradaDTO = tipoEntradaService.obtenerTipoEntradaPorId(idTipoEntrada, idPromotor)
                    .orElseThrow(() -> new NotFoundException("Tipo de entrada no encontrado o no tiene permiso."));

            request.setAttribute("tipoEntrada", tipoEntradaDTO);
            request.setAttribute("idPromotorAutenticado", idPromotor); // Para consistencia
            mostrarMensajeFlash(request); // Por si hay errores de un intento previo

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-entrada-detalle.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (NotFoundException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al mostrar formulario de edición para entrada ID {}: {}", idTipoEntrada, e.getMessage(), e);
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
     * @param idTipoEntrada ID del tipo de entrada a actualizar, obtenido del
     * path.
     * @param tipo Nuevo nombre del tipo (obligatorio).
     * @param descripcion Nueva descripción (opcional).
     * @param precioStr Nuevo precio (String, obligatorio).
     * @param stockStr Nuevo stock (String, obligatorio).
     * @param requiereNominacion
     * @return Una respuesta de redirección (303) a la vista de detalle del
     * festival o al formulario de edición de entrada si hay error.
     * @throws BadRequestException Si el ID no es válido.
     * @throws ServletException Si ocurre un error durante el forward en caso de
     * error.
     * @throws IOException Si ocurre un error de E/S durante el forward en caso
     * de error.
     */
    @POST
    @Path("/tiposEntrada/{idTipoEntrada}/actualizar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response actualizarEntrada(
            @PathParam("idTipoEntrada") Integer idTipoEntrada,
            @FormParam("tipo") String tipo,
            @FormParam("descripcion") String descripcion,
            @FormParam("precio") String precioStr,
            @FormParam("stock") String stockStr,
            @FormParam("requiereNominacion") Boolean requiereNominacion) throws ServletException, IOException {

        log.info("POST /promotor/tiposEntrada/{}/actualizar recibido", idTipoEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idTipoEntrada == null || idTipoEntrada <= 0) {
            throw new BadRequestException("ID de tipo de entrada no válido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir
        TipoEntradaDTO dto = new TipoEntradaDTO(); // Para posible reenvío al form en error
        dto.setIdTipoEntrada(idTipoEntrada); // Guardar el ID en el DTO para repoblar form

        try {
            // Validar y poblar DTO
            if (tipo == null || tipo.isBlank()) {
                throw new IllegalArgumentException("El nombre del tipo de entrada es obligatorio.");
            }
            dto.setTipo(tipo); // Poblar DTO para repoblar form
            dto.setDescripcion(descripcion);
            if (precioStr == null || precioStr.isBlank()) {
                throw new IllegalArgumentException("El precio es obligatorio.");
            }
            if (stockStr == null || stockStr.isBlank()) {
                throw new IllegalArgumentException("El stock es obligatorio.");
            }
            dto.setRequiereNominacion(requiereNominacion != null && requiereNominacion);
            // Parsear y validar precio
            try {
                dto.setPrecio(new BigDecimal(precioStr.replace(',', '.')));
                if (dto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("El precio no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato numérico inválido para Precio.");
            }
            // Parsear y validar stock
            try {
                dto.setStock(Integer.parseInt(stockStr));
                if (dto.getStock() < 0) {
                    throw new IllegalArgumentException("El stock no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato numérico inválido para Stock.");
            }

            // Llamar al servicio para actualizar (el servicio verifica propiedad)
            TipoEntradaDTO actualizada = tipoEntradaService.actualizarTipoEntrada(idTipoEntrada, dto, idPromotor);
            mensajeFlash = "Tipo de entrada '" + actualizada.getTipo() + "' actualizado con éxito.";
            idFestival = actualizada.getIdFestival(); // Obtener ID del festival para redirigir

        } catch (IllegalArgumentException | SecurityException | TipoEntradaNotFoundException e) {
            errorFlash = "Error al actualizar: " + e.getMessage();
            log.warn("Error de validación/negocio al actualizar tipo de entrada ID {}: {}", idTipoEntrada, errorFlash);
            // DTO ya está poblado para repintar form
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al actualizar el tipo de entrada.";
            log.error("Error interno al actualizar tipo de entrada ID {}: {}", idTipoEntrada, e.getMessage(), e);
            // DTO ya está poblado para repintar form
        }

        // Decidir a dónde redirigir
        if (errorFlash != null) {
            // Si hubo error, redirigir de vuelta al formulario de edición de la ENTRADA
            setFlashMessage(request, "error", errorFlash);
            // Reconstruir la URI para el método GET de edición de esta entrada
            URI editUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarFormularioEditarEntrada") // Método GET para editar ENTRADA
                    .resolveTemplate("idTipoEntrada", idTipoEntrada) // Pasar el ID de la ENTRADA que falló
                    .build();
            log.debug("Redirección post-actualización (fallida) a: {}", editUri);
            return Response.seeOther(editUri).build(); // 303 See Other
        } else {
            // Si tuvo éxito, redirigir a la página de VISTA del FESTIVAL
            setFlashMessage(request, "mensaje", mensajeFlash);
            if (idFestival == null) {
                log.error("No se pudo obtener idFestival para redirigir después de actualizar tipo de entrada ID {}. Redirigiendo a lista general.", idTipoEntrada);
                URI fallbackUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build();
                return Response.seeOther(fallbackUri).build();
            }
            // CORRECCIÓN: Construir URI para ver detalles del FESTIVAL
            URI detailUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarDetallesFestival") // VISTA del FESTIVAL
                    .resolveTemplate("id", idFestival) // Usar "id" como nombre de template y pasar idFestival
                    .build();
            log.debug("Redirección post-actualización (exitosa) a: {}", detailUri);
            return Response.seeOther(detailUri).build(); // 303 See Other
        }
    } // Fin de actualizarTipoEntrada

    /**
     * Endpoint POST para eliminar un tipo de entrada específico. Redirige a la
     * vista de detalle del festival. Requiere rol PROMOTOR en sesión y ser
     * dueño del festival asociado.
     *
     * @param idTipoEntrada ID del tipo de entrada a eliminar, obtenido del
     * path.
     * @return Una respuesta de redirección (303) a la vista de detalle del
     * festival.
     * @throws BadRequestException Si el ID no es válido.
     */
    @POST
    @Path("/tiposEntrada/{idTipoEntrada}/eliminar")
    public Response eliminarEntrada(@PathParam("idTipoEntrada") Integer idTipoEntrada) {
        log.info("POST /promotor/tiposEntrada/{}/eliminar recibido", idTipoEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idTipoEntrada == null || idTipoEntrada <= 0) {
            throw new BadRequestException("ID de tipo de entrada no válido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir

        try {
            // Obtener el ID del festival ANTES de intentar eliminar el tipo de entrada
            Optional<TipoEntradaDTO> optDto = tipoEntradaService.obtenerTipoEntradaPorId(idTipoEntrada, idPromotor);
            if (optDto.isPresent()) {
                idFestival = optDto.get().getIdFestival();
                // Llamar al servicio para eliminar (el servicio verifica propiedad)
                tipoEntradaService.eliminarTipoEntrada(idTipoEntrada, idPromotor);
                mensajeFlash = "Tipo de entrada ID " + idTipoEntrada + " eliminado correctamente.";
            } else {
                errorFlash = "Tipo de entrada no encontrado o no tiene permiso para eliminarlo.";
                log.warn("Intento de eliminar entrada ID {} fallido (no encontrada o sin permiso) por promotor {}", idTipoEntrada, idPromotor);
            }
        } catch (TipoEntradaNotFoundException | UsuarioNotFoundException e) { // UsuarioNotFound no debería ocurrir aquí
            errorFlash = e.getMessage();
            log.warn("Error al eliminar tipo de entrada ID {}: {}", idTipoEntrada, errorFlash);
        } catch (SecurityException e) {
            errorFlash = e.getMessage(); // "No tiene permiso..."
            log.warn("Error de seguridad al eliminar tipo de entrada ID {}: {}", idTipoEntrada, errorFlash);
        } catch (RuntimeException e) { // Captura errores como violación de FK (IllegalStateException/PersistenceException del servicio)
            errorFlash = "No se pudo eliminar el tipo de entrada (posiblemente tiene ventas asociadas).";
            log.error("Error runtime al eliminar tipo de entrada ID {}: {}", idTipoEntrada, e.getMessage(), e);
        } catch (Exception e) { // Captura genérica para otros errores
            errorFlash = "Error interno inesperado al eliminar.";
            log.error("Error interno al eliminar tipo de entrada ID {}: {}", idTipoEntrada, e.getMessage(), e);
        }

        // Guardar mensaje/error y redirigir
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        // Redirigir a la página de VISTA del festival si obtuvimos su ID, sino a la lista general
        URI redirectUri;
        if (idFestival != null) {
            redirectUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarDetallesFestival") // VISTA del FESTIVAL
                    .resolveTemplate("id", idFestival) // Usar template "id"
                    .build();
        } else {
            // Fallback si no se pudo obtener el idFestival (ej, si la entrada no existía)
            redirectUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build();
        }
        log.debug("Redirección post-eliminación tipo de entrada a: {}", redirectUri);
        return Response.seeOther(redirectUri).build(); // 303 See Other
    }

    // --- Endpoints para Gestión de Entradas ---
    /**
     * Endpoint GET para listar las entradas (vendidas/generadas) de un festival
     * específico perteneciente al promotor autenticado. Realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-entradas.jsp}. Requiere rol
     * PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestival ID del festival cuyas entradas se listarán, obtenido
     * del path.
     * @return Una respuesta JAX-RS OK si el forward tiene éxito.
     * @throws BadRequestException Si el ID no es válido.
     * @throws NotFoundException Si el festival no se encuentra.
     * @throws ForbiddenException Si el promotor no es dueño del festival.
     * @throws InternalServerErrorException Si ocurre un error interno al cargar
     * datos.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/{idFestival}/entradas")
    @Produces(MediaType.TEXT_HTML)
    public Response listarEntradas(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/{}/entradas recibido", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID festival inválido.");
        }
        try {
            // Obtener festival y verificar propiedad
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> idPromotor.equals(f.getIdPromotor()))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            // Obtener lista de entradas 
            List<EntradaDTO> listaEntradas = entradaService.obtenerEntradasPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("entradas", listaEntradas);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-entradas.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar entradas para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar las entradas.", e);
        }
    }

    /**
     * Endpoint POST para nominar una entrada a un asistente. Recibe el email,
     * nombre y teléfono (opcional) del asistente desde el formulario. El
     * servicio buscará o creará el asistente por email. Redirige a la lista de
     * entradas del festival. Requiere rol PROMOTOR en sesión y ser dueño del
     * festival asociado a la entrada.
     *
     * @param idTipoEntrada ID de la entrada a nominar, obtenido del path.
     * @param emailAsistente Email del asistente (obligatorio).
     * @param nombreAsistente Nombre del asistente (obligatorio si el asistente
     * es nuevo).
     * @param telefonoAsistente Teléfono del asistente (opcional).
     * @return Una respuesta de redirección (303) a la lista de entradas del
     * festival.
     * @throws BadRequestException Si faltan parámetros obligatorios (ID
     * entrada, email).
     */
    @POST
    @Path("/entradas/{idTipoEntrada}/nominar")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response nominarEntrada(
            @PathParam("idTipoEntrada") Integer idTipoEntrada,
            @FormParam("emailAsistente") String emailAsistente,
            @FormParam("nombreAsistente") String nombreAsistente,
            @FormParam("telefonoAsistente") String telefonoAsistente) {

        log.info("POST /promotor/entradas/{}/nominar recibido para asistente email {}", idTipoEntrada, emailAsistente);
        Integer idPromotor = verificarAccesoPromotor(request);

        if (idTipoEntrada == null || idTipoEntrada <= 0 || emailAsistente == null || emailAsistente.isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos o son inválidos (idTipoEntrada, emailAsistente).");
        }
        // El servicio validará si el nombre es necesario

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir

        try {
            // Llamar al servicio para nominar (el servicio verifica propiedad y estado)
            entradaService.nominarEntrada(idTipoEntrada, emailAsistente, nombreAsistente, telefonoAsistente, idPromotor);
            mensajeFlash = "Entrada ID " + idTipoEntrada + " nominada correctamente al asistente con email " + emailAsistente + ".";

            // Obtener el ID del festival para la redirección
            Optional<EntradaDTO> optDto = entradaService.obtenerEntradaPorId(idTipoEntrada, idPromotor);
            if (optDto.isPresent()) {
                idFestival = optDto.get().getIdFestival();
            } else {
                log.warn("No se pudo obtener entrada ID {} después de nominarla para determinar el festival.", idTipoEntrada);
                // errorFlash podría establecerse aquí si la redirección es crítica
            }

        } catch (EntradaNotFoundException | UsuarioNotFoundException | IllegalArgumentException e) {
            errorFlash = e.getMessage(); // Errores de datos o no encontrado
            log.warn("Error al nominar entrada ID {}: {}", idTipoEntrada, errorFlash);
        } catch (SecurityException | IllegalStateException e) {
            errorFlash = "No se pudo nominar la entrada: " + e.getMessage(); // Errores de permiso o estado
            log.warn("Error de negocio/seguridad al nominar entrada ID {}: {}", idTipoEntrada, errorFlash);
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al nominar la entrada.";
            log.error("Error interno al nominar entrada ID {}: {}", idTipoEntrada, e.getMessage(), e);
        }

        // Guardar mensaje/error y redirigir
        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        // Redirigir a la lista de entradas del festival si tenemos el ID, sino a la lista general de festivales
        URI redirectUri;
        if (idFestival != null) {
            redirectUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "listarEntradas")
                    .resolveTemplate("idFestival", idFestival)
                    .build();
        } else {
            redirectUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build(); // Fallback
        }
        log.debug("Redirección post-nominación a: {}", redirectUri);
        return Response.seeOther(redirectUri).build(); // 303 See Other
    }

    /**
     * Endpoint POST para cancelar una entrada. El servicio se encarga de
     * verificar permisos, estado de la entrada y de restaurar el stock del tipo
     * de entrada original. Redirige a la lista de entradas del festival.
     * Requiere rol PROMOTOR en sesión y ser dueño del festival asociado.
     *
     * @param idTipoEntrada ID de la entrada a cancelar, obtenido del path.
     * @return Una respuesta de redirección (303) a la lista de entradas.
     * @throws BadRequestException Si falta el ID o no es válido.
     */
    @POST
    @Path("/entradas/{idTipoEntrada}/cancelar")
    public Response cancelarEntrada(@PathParam("idTipoEntrada") Integer idTipoEntrada) {
        log.info("POST /promotor/entradas/{}/cancelar recibido", idTipoEntrada);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idTipoEntrada == null || idTipoEntrada <= 0) {
            throw new BadRequestException("ID entrada inválido.");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir

        try {
            // Obtener ID del festival ANTES de cancelar, para poder redirigir incluso si falla
            Optional<EntradaDTO> optDto = entradaService.obtenerEntradaPorId(idTipoEntrada, idPromotor);
            if (optDto.isPresent()) {
                idFestival = optDto.get().getIdFestival();
                // Llamar al servicio para cancelar (verifica propiedad y estado)
                entradaService.cancelarEntrada(idTipoEntrada, idPromotor);
                mensajeFlash = "Entrada ID " + idTipoEntrada + " cancelada correctamente (stock restaurado).";
            } else {
                errorFlash = "Entrada no encontrada o no tiene permiso para cancelarla.";
                log.warn("Intento de cancelar entrada ID {} fallido (no encontrada o sin permiso) por promotor {}", idTipoEntrada, idPromotor);
            }
        } catch (EntradaNotFoundException | UsuarioNotFoundException e) {
            errorFlash = e.getMessage();
            log.warn("Error al cancelar entrada ID {}: {}", idTipoEntrada, e.getMessage());
        } catch (SecurityException | IllegalStateException e) {
            errorFlash = "No se pudo cancelar la entrada: " + e.getMessage();
            log.warn("Error negocio/seguridad al cancelar entrada ID {}: {}", idTipoEntrada, e.getMessage());
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al cancelar la entrada.";
            log.error("Error interno al cancelar entrada ID {}: {}", idTipoEntrada, e.getMessage(), e);
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        URI redirectUri;
        if (idFestival != null) {
            redirectUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "listarEntradas")
                    .resolveTemplate("idFestival", idFestival)
                    .build();
        } else {
            redirectUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build(); // Fallback
        }
        log.debug("Redirección post-cancelación a: {}", redirectUri);
        return Response.seeOther(redirectUri).build(); // 303 See Other
    }

    /**
     * Endpoint POST para asociar una pulsera NFC a una entrada. Recibe el UID
     * de la pulsera desde el formulario. Redirige a la lista de entradas del
     * festival. Requiere rol PROMOTOR en sesión y ser dueño del festival
     * asociado.
     *
     * @param idTipoEntrada ID de la entrada a la que asociar la pulsera,
     * obtenido del path.
     * @param codigoUid UID de la pulsera a asociar, obtenido del formulario.
     * @return Una respuesta de redirección (303) a la lista de entradas.
     * @throws BadRequestException Si faltan parámetros o son inválidos.
     */
    @POST
    @Path("/entradas/{idTipoEntrada}/asociar-pulsera")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response asociarPulseraPromotor(
            @PathParam("idTipoEntrada") Integer idTipoEntrada,
            @FormParam("codigoUid") String codigoUid) {

        log.info("POST /promotor/entradas/{}/asociar-pulsera con UID: {}", idTipoEntrada, codigoUid);
        Integer idPromotor = verificarAccesoPromotor(request);

        if (idTipoEntrada == null || idTipoEntrada <= 0 || codigoUid == null || codigoUid.isBlank()) {
            throw new BadRequestException("Faltan parámetros requeridos o son inválidos (idTipoEntrada, codigoUid).");
        }

        String mensajeFlash = null;
        String errorFlash = null;
        Integer idFestival = null; // Para redirigir

        try {
            // Obtener ID del festival ANTES de asociar, para la redirección
            Optional<EntradaDTO> optDto = entradaService.obtenerEntradaPorId(idTipoEntrada, idPromotor);
            if (optDto.isPresent()) {
                idFestival = optDto.get().getIdFestival();
                // Llamar al servicio para asociar la pulsera (verifica propiedad y estado)
                pulseraNFCService.asociarPulseraEntrada(codigoUid, idTipoEntrada, idPromotor);
                mensajeFlash = "Pulsera con UID '" + codigoUid + "' asociada correctamente a la entrada ID " + idTipoEntrada + ".";
            } else {
                errorFlash = "Entrada no encontrada o no tiene permiso.";
                log.warn("Intento de asociar pulsera a entrada ID {} fallido (no encontrada o sin permiso) por promotor {}", idTipoEntrada, idPromotor);
            }
        } catch (EntradaNotFoundException | UsuarioNotFoundException | IllegalArgumentException e) {
            errorFlash = e.getMessage();
            log.warn("Error al asociar pulsera a entrada ID {}: {}", idTipoEntrada, errorFlash);
        } catch (SecurityException | IllegalStateException | PulseraYaAsociadaException | EntradaNoNominadaException e) {
            errorFlash = "No se pudo asociar la pulsera: " + e.getMessage();
            log.warn("Error negocio/seguridad al asociar pulsera a entrada ID {}: {}", idTipoEntrada, errorFlash);
        } catch (Exception e) {
            errorFlash = "Error interno inesperado al asociar la pulsera.";
            log.error("Error interno al asociar pulsera a entrada ID {}: {}", idTipoEntrada, e.getMessage(), e);
        }

        setFlashMessage(request, mensajeFlash != null ? "mensaje" : "error", mensajeFlash != null ? mensajeFlash : errorFlash);

        URI redirectUri;
        if (idFestival != null) {
            redirectUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "listarEntradas")
                    .resolveTemplate("idFestival", idFestival)
                    .build();
        } else {
            redirectUri = uriInfo.getBaseUriBuilder().path(PromotorResource.class).path("festivales").build(); // Fallback
        }
        log.debug("Redirección post-asociar pulsera a: {}", redirectUri);
        return Response.seeOther(redirectUri).build(); // 303 See Other
    }

    // --- Endpoints para Consulta de Asistentes y Pulseras por Festival ---
    /**
     * Endpoint GET para mostrar la lista de asistentes únicos con entradas para
     * un festival específico del promotor autenticado. Realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-festival-asistentes.jsp}. Requiere
     * rol PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestival ID del festival cuyos asistentes se listarán, obtenido
     * del path.
     * @return Una respuesta JAX-RS OK si el forward tiene éxito.
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
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            // Obtener datos del festival y verificar propiedad
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> idPromotor.equals(f.getIdPromotor()))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            // Obtener lista de asistentes DTOs para este festival (el servicio verifica permisos)
            List<AsistenteDTO> listaAsistentes = asistenteService.obtenerAsistentesPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("asistentes", listaAsistentes);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-festival-asistentes.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar asistentes para festival ID {}: {}", idFestival, e.getMessage(), e);
            throw new InternalServerErrorException("Error al cargar los asistentes del festival.", e);
        }
    }

    /**
     * Endpoint GET para mostrar la lista de compras realizadas para un festival
     * específico del promotor autenticado. Realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-festival-compras.jsp}. Requiere rol
     * PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestival ID del festival cuyas compras se listarán, obtenido del
     * path.
     * @return Una respuesta JAX-RS OK si el forward tiene éxito, o redirección
     * si falla la carga.
     * @throws BadRequestException Si el ID no es válido.
     * @throws ForbiddenException Si el promotor no es dueño del festival.
     * @throws ServletException Si ocurre un error durante el forward del JSP.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    @GET
    @Path("/festivales/{idFestival}/compras")
    @Produces(MediaType.TEXT_HTML)
    public Response listarComprasPorFestival(@PathParam("idFestival") Integer idFestival) throws ServletException, IOException {
        log.debug("GET /promotor/festivales/{}/compras recibido", idFestival);
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            // Obtener datos del festival y verificar propiedad
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> idPromotor.equals(f.getIdPromotor()))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            // Obtener lista de compras DTOs para este festival (servicio verifica permiso)
            List<CompraDTO> listaCompras = compraService.obtenerComprasPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("compras", listaCompras);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-festival-compras.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar compras para festival ID {}: {}", idFestival, e.getMessage(), e);
            setFlashMessage(request, "error", "Error al cargar las compras del festival.");
            URI detailUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarDetallesFestival")
                    .resolveTemplate("id", idFestival)
                    .build();
            return Response.seeOther(detailUri).build(); // Redirigir con error
        }
    }

    // --- Endpoints para Cambio de Contraseña Obligatorio ---
    /**
     * Endpoint GET para mostrar el formulario de cambio de contraseña
     * obligatorio. Se accede típicamente después de un login exitoso si el flag
     * 'cambioPasswordRequerido' del usuario es true. Realiza forward al JSP
     * {@code /WEB-INF/jsp/cambiar-password-obligatorio.jsp}. Requiere sesión
     * activa.
     *
     * @return Una respuesta JAX-RS OK si el forward tiene éxito, o redirección
     * si falla.
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
        if (session == null || session.getAttribute("userId") == null) {
            log.warn("Intento de acceso a mostrar-cambio-password sin sesión válida.");
            try {
                URI loginUri = new URI(request.getContextPath() + "/login?error=session_required");
                return Response.seeOther(loginUri).build();
            } catch (URISyntaxException e) {
                log.error("Error creando URI para login", e);
                return Response.serverError().entity("Error interno").build();
            }
        }
        Integer userId = (Integer) session.getAttribute("userId");
        log.debug("Mostrando formulario de cambio de contraseña obligatorio para userId: {}", userId);

        mostrarMensajeFlash(request); // Mostrar errores de intento previo

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
                redirectUri = new URI(request.getContextPath() + "/login?error=session_expired");
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

    /**
     * Endpoint GET para mostrar la lista de pulseras NFC asociadas a un
     * festival específico del promotor autenticado. Realiza forward al JSP
     * {@code /WEB-INF/jsp/promotor/promotor-festival-pulseras.jsp}. Requiere
     * rol PROMOTOR en sesión y ser dueño del festival.
     *
     * @param idFestival ID del festival cuyas pulseras se listarán, obtenido
     * del path.
     * @return Una respuesta JAX-RS OK si el forward tiene éxito, o redirección
     * si falla la carga.
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
        Integer idPromotor = verificarAccesoPromotor(request);
        if (idFestival == null || idFestival <= 0) {
            throw new BadRequestException("ID de festival no válido.");
        }

        try {
            // Obtener datos del festival y verificar propiedad
            FestivalDTO festival = festivalService.obtenerFestivalPorId(idFestival)
                    .filter(f -> idPromotor.equals(f.getIdPromotor()))
                    .orElseThrow(() -> new ForbiddenException("Festival no encontrado o no pertenece a este promotor."));

            // Obtener lista de pulseras DTOs para este festival (servicio verifica permiso)
            List<PulseraNFCDTO> listaPulseras = pulseraNFCService.obtenerPulserasPorFestival(idFestival, idPromotor);

            request.setAttribute("festival", festival);
            request.setAttribute("pulseras", listaPulseras);
            request.setAttribute("idPromotorAutenticado", idPromotor);
            mostrarMensajeFlash(request);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/promotor/promotor-festival-pulseras.jsp");
            dispatcher.forward(request, response);
            return Response.ok().build();

        } catch (ForbiddenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar pulseras para festival ID {}: {}", idFestival, e.getMessage(), e);
            setFlashMessage(request, "error", "Error al cargar las pulseras del festival.");
            URI detailUri = uriInfo.getBaseUriBuilder()
                    .path(PromotorResource.class)
                    .path(PromotorResource.class, "mostrarDetallesFestival")
                    .resolveTemplate("id", idFestival)
                    .build();
            return Response.seeOther(detailUri).build();
        }
    }

    // --- Métodos Auxiliares ---
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
        request.setAttribute("esNuevo", !esEdicion);
        request.setAttribute("idPromotorAutenticado", idPromotor);
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

    /**
     * Redirige de vuelta al formulario de cambio de contraseña obligatorio.
     * Usado cuando falla la validación o el guardado.
     *
     * @return Una respuesta de redirección (303).
     */
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

    /**
     * Determina la URL relativa del dashboard al que redirigir después de un
     * login o cambio de contraseña exitoso, basado en el rol del usuario.
     *
     * @param roleName El nombre del rol del usuario (ej: "PROMOTOR", "ADMIN").
     * @return La URL relativa del dashboard correspondiente (ej:
     * "/api/promotor/festivales").
     */
    private String determineDashboardUrlFromRole(String roleName) {
        if (RolUsuario.PROMOTOR.name().equalsIgnoreCase(roleName)) {
            return "/api/promotor/festivales";
        }
        // Añadir otros roles si es necesario
        // if (RolUsuario.ADMIN.name().equalsIgnoreCase(roleName)) { return "/api/admin/dashboard"; }
        log.warn("Rol inesperado '{}' al determinar URL de dashboard desde PromotorResource.", roleName);
        return "/login?error=unexpected_role"; // Fallback a login
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
            HttpSession session = request.getSession();
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
        HttpSession session = request.getSession(false);
        if (session != null) {
            String mensaje = (String) session.getAttribute("mensaje");
            if (mensaje != null) {
                request.setAttribute("mensajeExito", mensaje);
                session.removeAttribute("mensaje");
                log.trace("Mensaje flash de éxito movido de sesión a request.");
            }
            String error = (String) session.getAttribute("error");
            if (error != null) {
                request.setAttribute("error", error);
                session.removeAttribute("error");
                log.trace("Mensaje flash de error movido de sesión a request.");
            }
            String passwordError = (String) session.getAttribute("passwordChangeError");
            if (passwordError != null) {
                request.setAttribute("error", passwordError); // Usar clave genérica 'error'
                session.removeAttribute("passwordChangeError");
                log.trace("Mensaje flash 'passwordChangeError' movido de sesión a request.");
            }
            TipoEntradaDTO entradaConError = (TipoEntradaDTO) session.getAttribute("nuevaEntradaConError");
            if (entradaConError != null) {
                request.setAttribute("nuevaEntrada", entradaConError);
                session.removeAttribute("nuevaEntradaConError");
                log.trace("Atributo flash 'nuevaEntradaConError' movido de sesión a request.");
            }
            Boolean errorEntrada = (Boolean) session.getAttribute("errorEntrada");
            if (errorEntrada != null && errorEntrada) {
                request.setAttribute("errorEntrada", true);
                session.removeAttribute("errorEntrada");
                log.trace("Flag flash 'errorEntrada' movido de sesión a request.");
            }
        }
    }
}
