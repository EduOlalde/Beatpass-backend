<%-- 
    Document   : admin-admins
    Created on : 28 abr 2025, 0:08:46
    Author     : Eduardo Olalde
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestionar Administradores - Beatpass Admin</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Menú con la página activa correcta --%>
            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="admins"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">Gestionar Administradores</h2>

            <%-- Botón Añadir apunta a la creación general de usuarios --%>
            <div class="flex justify-end mb-4 space-x-3">
                <a href="${pageContext.request.contextPath}/api/admin/usuarios/crear"
                   class="btn btn-primary">
                    + Añadir Nuevo Usuario
                </a>
            </div>

            <%-- Mensajes flash --%>
            <c:if test="${not empty requestScope.mensajeExito}">
                <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Éxito</p> <p>${requestScope.mensajeExito}</p>
                </div>
            </c:if>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error</p> <p>${requestScope.error}</p>
                </div>
            </c:if>

            <%-- Tabla de Admins --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <%-- Usar la variable genérica "usuarios" --%>
                        <c:choose>
                            <c:when test="${empty usuarios}">
                                <tr> <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 italic">No hay administradores registrados.</td> </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="u" items="${usuarios}"> <%-- Variable genérica u --%>
                                    <tr>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${u.idUsuario}</td>
                                        <td class="px-6 py-4 whitespace-nowrap">
                                            <div class="text-sm font-medium text-gray-900">${u.nombre}</div>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${u.email}</td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center">
                                            <span class="badge ${u.estado ? 'badge-activa' : 'badge-inactiva'}">
                                                ${u.estado ? 'Activo' : 'Inactivo'}
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-2">
                                            <%-- Enlace Editar generalizado --%>
                                            <a href="${pageContext.request.contextPath}/api/admin/usuarios/${u.idUsuario}/editar" class="action-link action-link-edit" title="Editar datos de ${u.nombre}">Editar</a>
                                            <%-- Formulario Cambiar Estado generalizado --%>
                                            <%-- Evitar que un admin se desactive a sí mismo (opcional, lógica en backend sería mejor) --%>
                                            <c:if test="${u.idUsuario != sessionScope.userId}"> <%-- Asume que el ID del admin logueado está en sesión --%>
                                                <form action="${pageContext.request.contextPath}/api/admin/usuarios/cambiar-estado" method="post" class="inline">
                                                    <input type="hidden" name="idUsuario" value="${u.idUsuario}">
                                                    <input type="hidden" name="nuevoEstado" value="${!u.estado}">
                                                    <button type="submit" class="action-button ${u.estado ? 'action-button-deactivate' : 'action-button-activate'}" title="${u.estado ? 'Desactivar cuenta' : 'Activar cuenta'}">
                                                        ${u.estado ? 'Desactivar' : 'Activar'}
                                                    </button>
                                                </form>
                                            </c:if>
                                            <c:if test="${u.idUsuario == sessionScope.userId}">
                                                <span class="text-xs text-gray-400 italic">(Tú mismo)</span>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
