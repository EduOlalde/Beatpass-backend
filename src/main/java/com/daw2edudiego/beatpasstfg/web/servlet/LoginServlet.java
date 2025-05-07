package com.daw2edudiego.beatpasstfg.web.servlet;

import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario;
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;
import com.daw2edudiego.beatpasstfg.util.PasswordUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet para gestionar el login de usuarios ADMIN y PROMOTOR a los paneles
 * web. Maneja GET para mostrar el formulario y POST para validar credenciales,
 * crear sesión y gestionar el flujo de cambio de contraseña obligatorio para
 * PROMOTOR.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
    private static final long serialVersionUID = 1L;

    private final UsuarioService usuarioService;

    public LoginServlet() {
        this.usuarioService = new UsuarioServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("GET /login: Mostrando página de login.");
        invalidateSession(request); // Invalida sesión previa
        forwardToJsp(request, response, "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String contextPath = request.getContextPath();

        log.info("POST /login: Intento de login web para email: {}", email);
        clearPreviousErrors(request);

        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            log.warn("Login fallido: Email o contraseña vacíos.");
            setErrorAndForwardToLogin(request, response, "Email y contraseña son obligatorios.");
            return;
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioService.obtenerEntidadUsuarioPorEmailParaAuth(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                log.debug("Usuario encontrado: ID={}, Email={}, Rol={}, Estado={}, CambioPwdReq={}",
                        usuario.getIdUsuario(), email, usuario.getRol(), usuario.getEstado(), usuario.getCambioPasswordRequerido());

                if (!Boolean.TRUE.equals(usuario.getEstado())) {
                    log.warn("Login fallido: Cuenta inactiva para email {}", email);
                    setErrorAndForwardToLogin(request, response, "La cuenta de usuario está inactiva.");
                    return;
                }

                if (PasswordUtil.checkPassword(password, usuario.getPassword())) {
                    log.info("Login web exitoso para usuario ID: {}, Email: {}", usuario.getIdUsuario(), email);
                    createOrUpdateSession(request, usuario);

                    // Redirigir a cambio de contraseña si es PROMOTOR y lo requiere
                    if (Boolean.TRUE.equals(usuario.getCambioPasswordRequerido()) && usuario.getRol() == RolUsuario.PROMOTOR) {
                        log.info("PROMOTOR ID {} requiere cambio pass. Redirigiendo...", usuario.getIdUsuario());
                        // Usamos la ruta del recurso JAX-RS que muestra el form
                        response.sendRedirect(contextPath + "/api/promotor/mostrar-cambio-password");
                    } else {
                        // Redirigir al dashboard correspondiente
                        log.debug("Usuario ID {} (Rol {}) no requiere cambio o es Admin. Redirigiendo a dashboard...", usuario.getIdUsuario(), usuario.getRol());
                        String targetUrl = determineDashboardUrl(usuario.getRol(), contextPath);
                        if (targetUrl == null) { // Rol no válido para paneles
                            invalidateSession(request);
                            setErrorAndForwardToLogin(request, response, "Rol no válido para acceder a paneles.");
                        } else {
                            response.sendRedirect(targetUrl);
                        }
                    }
                } else {
                    log.warn("Login fallido: Contraseña incorrecta para email {}", email);
                    setErrorAndForwardToLogin(request, response, "Email o contraseña incorrectos.");
                }
            } else {
                log.warn("Login fallido: Usuario no encontrado con email {}", email);
                setErrorAndForwardToLogin(request, response, "Email o contraseña incorrectos.");
            }

        } catch (Exception e) {
            log.error("Error inesperado durante login web para email {}: {}", email, e.getMessage(), e);
            setErrorAndForwardToLogin(request, response, "Error inesperado. Inténtalo más tarde.");
        }
    }

    /**
     * Limpia atributos de error de la request y sesión.
     */
    private void clearPreviousErrors(HttpServletRequest request) {
        request.removeAttribute("error");
        HttpSession currentSession = request.getSession(false);
        if (currentSession != null) {
            currentSession.removeAttribute("passwordChangeError");
        }
    }

    /**
     * Crea o actualiza la sesión HTTP con datos del usuario.
     */
    private void createOrUpdateSession(HttpServletRequest request, Usuario usuario) {
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", usuario.getIdUsuario());
        session.setAttribute("userRole", usuario.getRol().name());
        session.setAttribute("userName", usuario.getNombre());
        session.setMaxInactiveInterval(30 * 60); // 30 minutos
        log.debug("Sesión HTTP creada/actualizada (ID: {}) para usuario ID: {}", session.getId(), usuario.getIdUsuario());
    }

    /**
     * Invalida la sesión actual si existe.
     */
    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Determina la URL del dashboard según el rol.
     */
    private String determineDashboardUrl(RolUsuario rol, String contextPath) {
        if (rol == RolUsuario.ADMIN) {
            // Punto de entrada al panel admin (podría ser /dashboard si existiera)
            return contextPath + "/api/admin/promotores/listar";
        } else if (rol == RolUsuario.PROMOTOR) {
            // Punto de entrada al panel promotor
            return contextPath + "/api/promotor/festivales";
        } else {
            log.error("Rol desconocido o no permitido ({}) para paneles web.", rol);
            return null; // Indica rol no válido
        }
    }

    /**
     * Establece mensaje de error y hace forward a login.jsp.
     */
    private void setErrorAndForwardToLogin(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        forwardToJsp(request, response, "/login.jsp");
    }

    /**
     * Realiza forward a un JSP.
     */
    private void forwardToJsp(HttpServletRequest request, HttpServletResponse response, String jspPath)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
        dispatcher.forward(request, response);
    }
}
