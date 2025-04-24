package com.daw2edudiego.beatpasstfg.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Servlet responsable de gestionar el cierre de sesión (logout) de los usuarios
 * de los paneles web (Admin/Promotor).
 * <p>
 * Invalida la sesión HTTP actual del usuario, eliminando así su estado de
 * autenticación, y luego redirige al usuario a la página de inicio de sesión
 * ({@code /login}).
 * </p>
 * <p>
 * Responde tanto a peticiones GET como POST a la URL {@code /logout}. Permitir
 * POST es una buena práctica para prevenir ataques CSRF si el logout se inicia
 * desde un enlace simple.
 * </p>
 *
 * @see HttpSession#invalidate()
 * @author Eduardo Olalde
 */
@WebServlet("/logout") // Mapea este servlet a la URL /logout del contexto de la aplicación
public class LogoutServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LogoutServlet.class);
    private static final long serialVersionUID = 1L; // Identificador de versión para serialización

    /**
     * Maneja las peticiones GET a /logout llamando a {@link #handleLogout}.
     *
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }

    /**
     * Maneja las peticiones POST a /logout llamando a {@link #handleLogout}.
     *
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }

    /**
     * Lógica central para el cierre de sesión. Obtiene la sesión actual (si
     * existe), la invalida y redirige a la página de login.
     *
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws IOException Si ocurre un error durante la redirección.
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.info("Procesando petición de logout.");
        HttpSession session = request.getSession(false); // Obtener sesión sin crearla si no existe

        if (session != null) {
            // Obtener ID de usuario para logging antes de invalidar
            String userId = (session.getAttribute("userId") != null)
                    ? session.getAttribute("userId").toString() : "desconocido";
            log.debug("Invalidando sesión HTTP (ID: {}) para usuario ID: {}", session.getId(), userId);
            session.invalidate(); // Invalidar la sesión (elimina todos los atributos)
        } else {
            log.debug("No había sesión HTTP activa para invalidar durante el logout.");
        }

        // Construir la URL de redirección a la página de login de forma segura
        String contextPath = request.getContextPath();
        String loginUrl = contextPath + "/login"; // Asume que login.jsp o LoginServlet está en /login
        // Opcional: añadir parámetro para mostrar mensaje en login.jsp
        // loginUrl += "?logout=true";

        try {
            // Realizar la redirección
            log.debug("Redirigiendo a la página de login: {}", loginUrl);
            response.sendRedirect(loginUrl);
        } catch (IOException e) {
            log.error("Error al redirigir a la página de login después del logout: {}", e.getMessage(), e);
            // Si la redirección falla, intentar enviar un error HTTP genérico
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al procesar logout.");
            }
            // Relanzar IOException para que el contenedor la maneje si es necesario
            throw e;
        }
    }
}
