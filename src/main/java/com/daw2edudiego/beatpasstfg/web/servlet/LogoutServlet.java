/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
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
 * Servlet para manejar el cierre de sesión (logout). Invalida la sesión HTTP
 * actual y redirige a la página de login.
 */
@WebServlet("/logout") // Mapea este servlet a la URL /logout
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
        // Es buena práctica permitir logout por POST también para evitar CSRF si se hace con link
        handleLogout(request, response);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.info("Procesando petición de logout.");
        HttpSession session = request.getSession(false); // Obtener sesión sin crearla

        if (session != null) {
            String userId = (session.getAttribute("userId") != null) ? session.getAttribute("userId").toString() : "desconocido";
            log.debug("Invalidando sesión para usuario ID: {}", userId);
            session.invalidate(); // Invalidar la sesión
        } else {
            log.debug("No había sesión activa para invalidar.");
        }

        // Guardar un mensaje para mostrar en la página de login (opcional, usar request attribute porque la sesión ya no existe)
        // request.setAttribute("mensajeLogout", "Has cerrado sesión correctamente.");
        // NOTA: Los atributos de request no sobreviven a una redirección.
        // Para mostrar mensaje tras redirect, se podría pasar un parámetro en la URL:
        // response.sendRedirect(request.getContextPath() + "/login?logout=true");
        // Y en login.jsp comprobar ese parámetro.
        // Redirigir a la página de login
        log.debug("Redirigiendo a la página de login.");
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
