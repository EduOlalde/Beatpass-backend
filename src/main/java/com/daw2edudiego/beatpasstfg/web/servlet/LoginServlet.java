package com.daw2edudiego.beatpasstfg.web.servlet;

import com.daw2edudiego.beatpasstfg.model.RolUsuario; // Enum Roles
import com.daw2edudiego.beatpasstfg.model.Usuario; // Entidad Usuario (necesaria completa)
import com.daw2edudiego.beatpasstfg.service.UsuarioService; // Servicio de usuario
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;
import com.daw2edudiego.beatpasstfg.util.PasswordUtil; // Para verificar contraseña

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet; // Anotación para mapeo URL
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // Para manejar sesiones HTTP
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet responsable de gestionar el proceso de inicio de sesión para los
 * paneles web de Administración ({@link RolUsuario#ADMIN}) y Promotor
 * ({@link RolUsuario#PROMOTOR}).
 * <p>
 * Maneja las peticiones GET a {@code /login} mostrando el formulario de inicio
 * de sesión ({@code login.jsp}) y las peticiones POST a {@code /login} para
 * validar las credenciales (email y contraseña) proporcionadas.
 * </p>
 * <p>
 * Utiliza {@link HttpSession} para mantener el estado de autenticación del
 * usuario. Si el login es exitoso, crea una sesión y almacena información clave
 * del usuario (ID, rol, nombre). Implementa una verificación adicional para
 * usuarios con rol PROMOTOR: si el flag {@code cambioPasswordRequerido} está
 * activo, redirige al usuario a una página específica para forzar el cambio de
 * contraseña antes de permitir el acceso al panel principal del promotor. Los
 * usuarios ADMIN son redirigidos directamente a su panel, ignorando el flag de
 * cambio de contraseña.
 * </p>
 *
 * @see UsuarioService
 * @see PasswordUtil
 * @see HttpSession
 * @author Eduardo Olalde
 */
@WebServlet("/login") // Mapea este servlet a la URL /login del contexto de la aplicación
public class LoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
    private static final long serialVersionUID = 1L; // Identificador de versión para serialización

    // Instancia del servicio de usuario (inyección manual)
    private final UsuarioService usuarioService;

    /**
     * Constructor que inicializa el servicio de usuario.
     */
    public LoginServlet() {
        this.usuarioService = new UsuarioServiceImpl();
    }

    /**
     * Maneja las peticiones GET a /login. Invalida cualquier sesión existente y
     * muestra el formulario de login (login.jsp).
     *
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("GET /login: Mostrando página de login.");
        // Invalidar sesión previa si existe, para asegurar un login limpio
        HttpSession session = request.getSession(false); // No crear nueva sesión
        if (session != null) {
            log.debug("Invalidando sesión existente ({}) al acceder a GET /login.", session.getId());
            session.invalidate();
        }
        // Reenviar la petición al archivo JSP que contiene el formulario HTML
        RequestDispatcher dispatcher = request.getRequestDispatcher("/login.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Maneja las peticiones POST a /login. Valida las credenciales (email,
     * password), verifica el estado del usuario y la contraseña. Si es válido,
     * crea una sesión HTTP, almacena datos del usuario y redirige al panel
     * correspondiente (Admin o Promotor). Implementa la lógica de redirección
     * forzada para cambio de contraseña obligatorio solo para el rol PROMOTOR.
     *
     * @param request La petición HTTP, contiene los parámetros del formulario.
     * @param response La respuesta HTTP, usada para redireccionar.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener credenciales del formulario
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String contextPath = request.getContextPath(); // Obtener la ruta base de la aplicación

        log.info("POST /login: Intento de login para email: {}", email);

        // Limpiar errores previos de la request o sesión
        request.removeAttribute("error");
        HttpSession currentSession = request.getSession(false);
        if (currentSession != null) {
            currentSession.removeAttribute("passwordChangeError"); // Limpiar error específico de cambio pass
        }

        // Validación básica de entrada
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            log.warn("Login fallido: Email o contraseña vacíos.");
            setErrorAndForwardToLogin(request, response, "Email y contraseña son obligatorios.");
            return; // Detener procesamiento
        }

        try {
            // Buscar la entidad Usuario completa (incluye hash y flags)
            Optional<Usuario> usuarioOpt = usuarioService.obtenerEntidadUsuarioPorEmailParaAuth(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                log.debug("Usuario encontrado para login web: ID={}, Email={}, Rol={}, Estado={}, CambioPwdReq={}",
                        usuario.getIdUsuario(), email, usuario.getRol(), usuario.getEstado(), usuario.getCambioPasswordRequerido());

                // Verificar si la cuenta está activa
                if (!Boolean.TRUE.equals(usuario.getEstado())) {
                    log.warn("Login fallido: Cuenta inactiva para email {}", email);
                    setErrorAndForwardToLogin(request, response, "La cuenta de usuario está inactiva.");
                    return;
                }

                // Verificar la contraseña
                if (PasswordUtil.checkPassword(password, usuario.getPassword())) {
                    // ¡Autenticación exitosa!
                    log.info("Login web exitoso para usuario ID: {}, Email: {}", usuario.getIdUsuario(), email);

                    // Crear o obtener sesión HTTP y almacenar datos del usuario
                    HttpSession session = request.getSession(true); // Crear sesión si no existe
                    session.setAttribute("userId", usuario.getIdUsuario());
                    session.setAttribute("userRole", usuario.getRol().name()); // Guardar nombre del Enum
                    session.setAttribute("userName", usuario.getNombre());
                    session.setMaxInactiveInterval(30 * 60); // Establecer timeout de sesión (ej: 30 minutos)
                    log.debug("Sesión HTTP creada/actualizada (ID: {}) para usuario ID: {}. MaxInactiveInterval: {}s",
                            session.getId(), usuario.getIdUsuario(), session.getMaxInactiveInterval());

                    // --- VERIFICACIÓN: CAMBIO DE CONTRASEÑA OBLIGATORIO (SOLO PROMOTOR) ---
                    if (Boolean.TRUE.equals(usuario.getCambioPasswordRequerido()) && usuario.getRol() == RolUsuario.PROMOTOR) {
                        // Si requiere cambio Y es Promotor, redirigir a la página de cambio
                        log.info("Usuario PROMOTOR ID: {} requiere cambio de contraseña obligatorio. Redirigiendo a /api/promotor/mostrar-cambio-password...", usuario.getIdUsuario());
                        // Usar la ruta del recurso JAX-RS que muestra el formulario de cambio
                        response.sendRedirect(contextPath + "/api/promotor/mostrar-cambio-password");
                        return; // Detener aquí, no continuar al dashboard normal
                    } else {
                        // Si no requiere cambio, o si es ADMIN (ignoramos el flag para ADMIN), redirigir al dashboard normal
                        log.debug("Usuario ID: {} (Rol: {}) no requiere cambio de contraseña obligatorio o es Admin. Redirigiendo al dashboard...", usuario.getIdUsuario(), usuario.getRol());
                        String targetUrl;
                        // Determinar URL del dashboard según el rol
                        if (usuario.getRol() == RolUsuario.ADMIN) {
                            log.debug("Redirigiendo ADMIN a /api/admin/promotores/listar...");
                            // Usar la ruta del recurso JAX-RS del panel de admin
                            targetUrl = contextPath + "/api/admin/promotores/listar"; // O /api/admin/dashboard
                        } else if (usuario.getRol() == RolUsuario.PROMOTOR) {
                            // Este caso ahora solo se alcanza si cambioPasswordRequerido es false
                            log.debug("Redirigiendo PROMOTOR a /api/promotor/festivales...");
                            // Usar la ruta del recurso JAX-RS del panel de promotor
                            targetUrl = contextPath + "/api/promotor/festivales";
                        } else {
                            // Rol desconocido o no permitido para paneles web
                            log.error("Rol desconocido o no permitido ({}) encontrado durante login web para usuario ID {}", usuario.getRol(), usuario.getIdUsuario());
                            session.invalidate(); // Invalidar sesión creada
                            setErrorAndForwardToLogin(request, response, "Rol de usuario no válido para acceder a los paneles.");
                            return;
                        }
                        // Realizar la redirección al dashboard correspondiente
                        response.sendRedirect(targetUrl);
                        return;
                    }
                    // --- FIN VERIFICACIÓN ---

                } else {
                    // Contraseña incorrecta
                    log.warn("Login fallido: Contraseña incorrecta para email {}", email);
                    setErrorAndForwardToLogin(request, response, "Email o contraseña incorrectos.");
                }
            } else {
                // Usuario no encontrado
                log.warn("Login fallido: Usuario no encontrado con email {}", email);
                setErrorAndForwardToLogin(request, response, "Email o contraseña incorrectos.");
            }

        } catch (Exception e) {
            // Capturar cualquier error inesperado durante el proceso
            log.error("Error inesperado durante el proceso de login web para email {}: {}", email, e.getMessage(), e);
            setErrorAndForwardToLogin(request, response, "Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde.");
        }
    }

    /**
     * Método auxiliar para establecer un mensaje de error como atributo de la
     * petición y reenviar (forward) de vuelta al formulario de login
     * (login.jsp).
     *
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @param errorMessage El mensaje de error a mostrar en el JSP.
     * @throws ServletException Si ocurre un error durante el forward.
     * @throws IOException Si ocurre un error de E/S durante el forward.
     */
    private void setErrorAndForwardToLogin(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage); // Establecer atributo para el JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/login.jsp");
        dispatcher.forward(request, response); // Reenviar al JSP de login
    }

}
