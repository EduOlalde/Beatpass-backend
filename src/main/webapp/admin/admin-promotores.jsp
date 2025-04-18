<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestionar Promotores - Beatpass Admin</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <style> body {
            font-family: 'Inter', sans-serif;
        } </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8">
            <%-- Incluir un menú de navegación de admin si lo tienes --%>
            <%-- <jsp:include page="../_admin_menu.jsp" /> --%>

            <h1 class="text-3xl font-bold mb-6 text-purple-700">Gestionar Promotores</h1>

            <%-- Mostrar mensajes flash --%>
            <c:if test="${not empty sessionScope.mensaje}">
                <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${sessionScope.mensaje}</span>
                </div>
                <% session.removeAttribute("mensaje"); %>
            </c:if>
            <c:if test="${not empty sessionScope.error}">
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${sessionScope.error}</span>
                </div>
                <% session.removeAttribute("error");%>
            </c:if>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${requestScope.error}</span>
                </div>
            </c:if>

            <div class="mb-4 text-right">
                <a href="${pageContext.request.contextPath}/api/admin/promotores/crear" <%-- Enlace al endpoint de creación --%>
                   class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded shadow">
                    + Añadir Nuevo Promotor
                </a>
            </div>

            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty promotores}">
                                <tr>
                                    <td colspan="5" class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">No hay promotores registrados.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="p" items="${promotores}">
                                    <tr>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${p.idUsuario}</td>
                                        <td class="px-6 py-4 whitespace-nowrap">
                                            <div class="text-sm font-medium text-gray-900">${p.nombre}</div>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${p.email}</td>
                                        <td class="px-6 py-4 whitespace-nowrap">
                                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${p.estado ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                                                ${p.estado ? 'Activo' : 'Inactivo'}
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                            <%-- Formulario para cambiar estado --%>
                                            <form action="${pageContext.request.contextPath}/api/admin/promotores/cambiar-estado" method="post" class="inline">
                                                <input type="hidden" name="idPromotor" value="${p.idUsuario}">
                                                <input type="hidden" name="nuevoEstado" value="${!p.estado}"> <%-- Envía el estado opuesto --%>
                                                <button type="submit" class="${p.estado ? 'text-yellow-600 hover:text-yellow-900' : 'text-green-600 hover:text-green-900'}">
                                                    ${p.estado ? 'Desactivar' : 'Activar'}
                                                </button>
                                            </form>
                                            <%-- Enlace para editar (si se implementa) --%>
                                            <%-- <a href="${pageContext.request.contextPath}/api/admin/promotores/editar/${p.idUsuario}" class="text-indigo-600 hover:text-indigo-900 ml-3">Editar</a> --%>
                                            <%-- Formulario para eliminar (si se implementa) --%>
                                            <%--
                                            <form action="${pageContext.request.contextPath}/api/admin/promotores/eliminar" method="post" class="inline" onsubmit="return confirm('¿Seguro que quieres eliminar a ${p.nombre}?');">
                                                 <input type="hidden" name="idPromotor" value="${p.idUsuario}">
                                                 <button type="submit" class="text-red-600 hover:text-red-900 ml-3">Eliminar</button>
                                             </form>
                                            --%>
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
