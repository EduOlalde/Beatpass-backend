<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestionar Asistentes - Beatpass Admin</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            body {
                font-family: 'Inter', sans-serif;
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Incluir Menú Admin Común --%>
            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="asistentes"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">Gestionar Asistentes</h2>

            <%-- Mensajes flash --%>
            <c:if test="${not empty requestScope.mensajeExito}">
                <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p>${requestScope.mensajeExito}</p>
                </div>
            </c:if>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error:</p> <p>${requestScope.error}</p>
                </div>
            </c:if>

            <%-- Formulario de Búsqueda --%>
            <div class="mb-4 bg-white p-4 rounded shadow-sm">
                <form action="${pageContext.request.contextPath}/api/admin/asistentes" method="get" class="flex items-end space-x-3">
                    <div>
                        <label for="searchTerm" class="block text-sm font-medium text-gray-700 mb-1">Buscar por Nombre o Email:</label>
                        <input type="search" id="searchTerm" name="buscar" value="${requestScope.searchTerm}" placeholder="Nombre o email..."
                               class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md shadow-sm">
                    </div>
                    <button type="submit" class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center text-sm bg-blue-600 hover:bg-blue-700 text-white">
                        Buscar
                    </button>
                    <a href="${pageContext.request.contextPath}/api/admin/asistentes" class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center text-sm bg-gray-200 hover:bg-gray-300 text-gray-800">Limpiar</a>
                </form>
            </div>

            <%-- TODO: Añadir botón para Crear Asistente --%>
            <%-- <div class="mb-4 flex justify-end"> <a href="..." class="...">+ Añadir Asistente</a> </div> --%>

            <%-- Tabla de Asistentes --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Teléfono</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha Registro</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty asistentes}">
                                <tr>
                                    <td colspan="6" class="px-6 py-4 text-center text-sm text-gray-500 italic">
                                        <c:choose>
                                            <c:when test="${not empty requestScope.searchTerm}">No se encontraron asistentes para "${requestScope.searchTerm}".</c:when>
                                            <c:otherwise>No hay asistentes registrados.</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="a" items="${asistentes}">
                                    <tr>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${a.idAsistente}</td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${a.nombre}</td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${a.email}</td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${not empty a.telefono ? a.telefono : '-'}</td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <c:catch var="formatError">
                                                <fmt:formatDate value="${a.fechaCreacion}" pattern="dd/MM/yyyy HH:mm"/>
                                            </c:catch>
                                            <c:if test="${not empty formatError}">
                                                ${a.fechaCreacion} <%-- Fallback a toString() --%>
                                            </c:if>
                                        </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-center text-sm space-x-2">
                                            <%-- Enlace Ver --%>
                                            <a href="${pageContext.request.contextPath}/api/admin/asistentes/${a.idAsistente}" class="text-indigo-600 hover:text-indigo-900 underline font-medium p-0 bg-transparent shadow-none text-xs">Ver</a>
                                            <%-- *** CAMBIO: Enlace Editar funcional *** --%>
                                            <a href="${pageContext.request.contextPath}/api/admin/asistentes/${a.idAsistente}/editar" class="text-yellow-600 hover:text-yellow-900 underline font-semibold p-0 bg-transparent shadow-none text-xs">Editar</a>
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
