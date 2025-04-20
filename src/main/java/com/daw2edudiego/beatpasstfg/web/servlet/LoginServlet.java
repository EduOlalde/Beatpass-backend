package com.daw2edudiego.beatpasstfg.web.servlet; // O tu paquete web

import com.daw2edudiego.beatpasstfg.model.RolUsuario;
import com.daw2edudiego.beatpasstfg.model.Usuario; // Necesitamos la entidad completa aquí
import com.daw2edudiego.beatpasstfg.service.UsuarioService;
import com.daw2edudiego.beatpasstfg.service.UsuarioServiceImpl;
import com.daw2edudiego.beatpasstfg.util.PasswordUtil; // Para verificar contraseña

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
 * Servlet para manejar el proceso de inicio de sesión de Administradores y
 * Promotores. Utiliza Sesiones HTTP para mantener el estado del usuario.
 * ¡Versión con lógica de autenticación real llamando al servicio!
 */
@WebServlet("/login") // Mapea este servlet a la URL /login
public class LoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
    private static final long serialVersionUID = 1L;

    private final UsuarioService usuarioService;

    public LoginServlet() {
        // Instanciación manual (o inyección si usaras CDI/Spring)
        // Asegúrate de que UsuarioServiceImpl implementa el método obtenerEntidadUsuarioPorEmailParaAuth
        this.usuarioService = new UsuarioServiceImpl();
    }

    /**
     * Muestra la página de login.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("GET /login: Mostrando página de login.");
        // Invalidar sesión existente si el usuario llega a /login vía GET (opcional pero bueno)
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.debug("Invalidando sesión existente al acceder a GET /login.");
            session.invalidate();
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/login.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Procesa el envío del formulario de login.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String contextPath = request.getContextPath();

        log.info("POST /login: Intento de login para email: {}", email);

        // Validación básica
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            log.warn("Login fallido: Email o contraseña vacíos.");
            setErrorAndForwardToLogin(request, response, "Email y contraseña son obligatorios.");
            return;
        }

        // --- Lógica de Autenticación ---
        try {
            // 1. Obtener la entidad Usuario completa desde el servicio usando el método real
            Optional<Usuario> usuarioOpt = usuarioService.obtenerEntidadUsuarioPorEmailParaAuth(email);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                log.debug("Usuario encontrado para email {}: ID={}, Rol={}, Estado={}", email, usuario.getIdUsuario(), usuario.getRol(), usuario.getEstado());

                // 2. Verificar estado de la cuenta
                if (!Boolean.TRUE.equals(usuario.getEstado())) {
                    log.warn("Login fallido: Cuenta inactiva para email {}", email);
                    setErrorAndForwardToLogin(request, response, "La cuenta está inactiva.");
                    return;
                }

                // 3. Verificar contraseña
                if (PasswordUtil.checkPassword(password, usuario.getPassword())) {
                    // ¡Autenticación exitosa!
                    log.info("Login exitoso para usuario ID: {}, Email: {}, Rol: {}", usuario.getIdUsuario(), email, usuario.getRol());

                    // Crear o obtener la sesión HTTP (true = crear si no existe)
                    HttpSession session = request.getSession(true);

                    // Guardar información del usuario en la sesión
                    session.setAttribute("userId", usuario.getIdUsuario());
                    session.setAttribute("userRole", usuario.getRol().name()); // Guardamos el nombre del Enum
                    session.setAttribute("userName", usuario.getNombre()); // Opcional, para mostrar

                    // Establecer tiempo máximo de inactividad (ej: 30 minutos)
                    session.setMaxInactiveInterval(30 * 60);
                    log.debug("Sesión creada/actualizada para usuario ID: {}. MaxInactiveInterval: {}s", usuario.getIdUsuario(), session.getMaxInactiveInterval());

                    // Redirigir al panel correspondiente según el rol
                    String targetUrl;
                    if (usuario.getRol() == RolUsuario.ADMIN) {
                        log.debug("Redirigiendo ADMIN al listado de promotores...");
                        // CAMBIADO: Apuntar a la ruta que lista promotores en AdminResource
                        targetUrl = contextPath + "/api/admin/promotores/listar";
                        // O si prefieres la ruta base del recurso admin que también lista:
                        // targetUrl = contextPath + "/api/admin/promotores";
                    } else if (usuario.getRol() == RolUsuario.PROMOTOR) {
                        log.debug("Redirigiendo PROMOTOR a su panel...");
                        targetUrl = contextPath + "/api/promotor/festivales";
                    } else {
                        log.error("Rol desconocido o no permitido encontrado durante login: {}", usuario.getRol());
                        session.invalidate();
                        setErrorAndForwardToLogin(request, response, "Rol de usuario no válido para la aplicación.");
                        return;
                    }
                    response.sendRedirect(targetUrl); // Redirigir

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
            log.error("Error inesperado durante el proceso de login para email {}: {}", email, e.getMessage(), e);
            setErrorAndForwardToLogin(request, response, "Ocurrió un error inesperado. Inténtalo de nuevo más tarde.");
        }
    }

    /**
     * Método auxiliar para establecer un mensaje de error y reenviar a la
     * página de login.
     */
    private void setErrorAndForwardToLogin(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/login.jsp");
        dispatcher.forward(request, response);
    }

    // El método simulado obtenerEntidadUsuarioPorEmail ya NO es necesario y se ha eliminado.
}
