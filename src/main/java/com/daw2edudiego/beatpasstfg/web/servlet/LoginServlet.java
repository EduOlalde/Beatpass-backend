/*
 * Servlet que maneja el proceso de inicio de sesión para los paneles web
 * de Administración y Promotor. Utiliza Sesiones HTTP.
 * Verifica credenciales, estado de la cuenta y si se requiere cambio de contraseña
 * (SOLO para PROMOTORES).
 * ACTUALIZADO: Cambio de contraseña obligatorio solo para PROMOTOR.
 */
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
 * Servlet para manejar el inicio de sesión (POST /login) y mostrar el
 * formulario (GET /login). Utiliza HttpSession para gestionar el estado de
 * autenticación de los usuarios que acceden a los paneles web (Admin/Promotor).
 * Incluye lógica para redirigir a cambio de contraseña si es necesario (solo
 * promotores).
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
        // ... (sin cambios) ...
        log.debug("GET /login: Mostrando página de login.");
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.debug("Invalidando sesión existente ({}) al acceder a GET /login.", session.getId());
            session.invalidate();
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/login.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String contextPath = request.getContextPath();

        log.info("POST /login: Intento de login para email: {}", email);

        request.removeAttribute("error");
        HttpSession currentSession = request.getSession(false);
        if (currentSession != null) {
            currentSession.removeAttribute("passwordChangeError");
        }

        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            log.warn("Login fallido: Email o contraseña vacíos.");
            setErrorAndForwardToLogin(request, response, "Email y contraseña son obligatorios.");
            return;
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioService.obtenerEntidadUsuarioPorEmailParaAuth(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                log.debug("Usuario encontrado para login web: ID={}, Email={}, Rol={}, Estado={}, CambioPwdReq={}",
                        usuario.getIdUsuario(), email, usuario.getRol(), usuario.getEstado(), usuario.getCambioPasswordRequerido());

                if (!Boolean.TRUE.equals(usuario.getEstado())) {
                    log.warn("Login fallido: Cuenta inactiva para email {}", email);
                    setErrorAndForwardToLogin(request, response, "La cuenta de usuario está inactiva.");
                    return;
                }

                if (PasswordUtil.checkPassword(password, usuario.getPassword())) {
                    log.info("Login web exitoso para usuario ID: {}, Email: {}", usuario.getIdUsuario(), email);

                    HttpSession session = request.getSession(true);
                    session.setAttribute("userId", usuario.getIdUsuario());
                    session.setAttribute("userRole", usuario.getRol().name());
                    session.setAttribute("userName", usuario.getNombre());
                    session.setMaxInactiveInterval(30 * 60);
                    log.debug("Sesión HTTP creada/actualizada (ID: {}) para usuario ID: {}. MaxInactiveInterval: {}s",
                            session.getId(), usuario.getIdUsuario(), session.getMaxInactiveInterval());

                    // --- *** VERIFICACIÓN: CAMBIO DE CONTRASEÑA OBLIGATORIO (SOLO PROMOTOR) *** ---
                    // Comprobar si el flag está a true Y si el rol es PROMOTOR
                    if (Boolean.TRUE.equals(usuario.getCambioPasswordRequerido()) && usuario.getRol() == RolUsuario.PROMOTOR) {
                        log.info("Usuario PROMOTOR ID: {} requiere cambio de contraseña obligatorio. Redirigiendo a /api/promotor/mostrar-cambio-password...", usuario.getIdUsuario());
                        response.sendRedirect(contextPath + "/api/promotor/mostrar-cambio-password");
                        return; // Detener aquí
                    } else {
                        // Si no requiere cambio (o es ADMIN aunque lo requiera), redirigir al dashboard normal
                        log.debug("Usuario ID: {} (Rol: {}) no requiere cambio de contraseña obligatorio o es Admin. Redirigiendo al dashboard...", usuario.getIdUsuario(), usuario.getRol());
                        String targetUrl;
                        if (usuario.getRol() == RolUsuario.ADMIN) {
                            log.debug("Redirigiendo ADMIN a /api/admin/promotores/listar...");
                            targetUrl = contextPath + "/api/admin/promotores/listar";
                        } else if (usuario.getRol() == RolUsuario.PROMOTOR) {
                            // Este caso ahora solo se alcanza si cambioPasswordRequerido es false
                            log.debug("Redirigiendo PROMOTOR a /api/promotor/festivales...");
                            targetUrl = contextPath + "/api/promotor/festivales";
                        } else {
                            log.error("Rol desconocido o no permitido ({}) encontrado durante login web para usuario ID {}", usuario.getRol(), usuario.getIdUsuario());
                            session.invalidate();
                            setErrorAndForwardToLogin(request, response, "Rol de usuario no válido para acceder a los paneles.");
                            return;
                        }
                        response.sendRedirect(targetUrl);
                        return;
                    }
                    // --- *** FIN VERIFICACIÓN *** ---

                } else {
                    log.warn("Login fallido: Contraseña incorrecta para email {}", email);
                    setErrorAndForwardToLogin(request, response, "Email o contraseña incorrectos.");
                }
            } else {
                log.warn("Login fallido: Usuario no encontrado con email {}", email);
                setErrorAndForwardToLogin(request, response, "Email o contraseña incorrectos.");
            }

        } catch (Exception e) {
            log.error("Error inesperado durante el proceso de login web para email {}: {}", email, e.getMessage(), e);
            setErrorAndForwardToLogin(request, response, "Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde.");
        }
    }

    private void setErrorAndForwardToLogin(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {
        // ... (sin cambios) ...
        request.setAttribute("error", errorMessage);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/login.jsp");
        dispatcher.forward(request, response);
    }

}
