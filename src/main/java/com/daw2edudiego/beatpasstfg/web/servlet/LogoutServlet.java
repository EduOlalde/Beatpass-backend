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
 * Servlet para gestionar el cierre de sesión (logout) de los paneles web.
 * Invalida la sesión HTTP y redirige a la página de login.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LogoutServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleLogout(request, response);
    }

    /**
     * Lógica central para el logout: invalida sesión y redirige a /login.
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.info("Procesando petición de logout.");
        HttpSession session = request.getSession(false); // Obtener sin crear

        if (session != null) {
            String userId = (session.getAttribute("userId") != null)
                    ? session.getAttribute("userId").toString() : "desconocido";
            log.debug("Invalidando sesión HTTP (ID: {}) para usuario ID: {}", session.getId(), userId);
            session.invalidate(); // Invalida la sesión
        } else {
            log.debug("No había sesión HTTP activa para invalidar.");
        }

        String contextPath = request.getContextPath();
        String loginUrl = contextPath + "/login"; // URL de redirección

        try {
            log.debug("Redirigiendo a: {}", loginUrl);
            response.sendRedirect(loginUrl); // Redirige al usuario
        } catch (IOException e) {
            log.error("Error al redirigir a login tras logout: {}", e.getMessage(), e);
            if (!response.isCommitted()) {
                // Intentar enviar error si la respuesta no se ha enviado aún
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error procesando logout.");
            }
            throw e; // Relanzar para que el contenedor maneje si es necesario
        }
    }
}
