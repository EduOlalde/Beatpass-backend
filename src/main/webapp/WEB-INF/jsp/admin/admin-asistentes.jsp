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
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Incluir Menú Admin Común --%>
            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="asistentes"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">Gestionar Asistentes</h2>

            <%-- Mensajes flash (se mantienen clases de Tailwind) --%>
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

            <%-- Formulario de Búsqueda (botones con clases CSS externas) --%>
            <div class="mb-4 bg-white p-4 rounded shadow-sm">
                <form action="${pageContext.request.contextPath}/api/admin/asistentes" method="get" class="flex items-end space-x-3">
                    <div>
                        <label for="searchTerm" class="block text-sm font-medium text-gray-700 mb-1">Buscar por Nombre o Email:</label>
                        <input type="search" id="searchTerm" name="buscar" value="${requestScope.searchTerm}" placeholder="Nombre o email..."
                               class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md shadow-sm">
                    </div>
                    <button type="submit" class="btn btn-filter">
                        Buscar
                    </button>
                    <a href="${pageContext.request.contextPath}/api/admin/asistentes" class="btn btn-secondary">Limpiar</a>
                </form>
            </div>

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
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Festivales / Pulseras</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty asistentes}">
                                <tr>
                                    <td colspan="7" class="px-6 py-4 text-center text-sm text-gray-500 italic">
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
                                                ${a.fechaCreacion}
                                            </c:if>
                                        </td>
                                        <%-- Celda con UID de pulsera usando clase CSS externa --%>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <c:choose>
                                                <c:when test="${not empty a.festivalPulseraInfo}">
                                                    <c:forEach var="entry" items="${a.festivalPulseraInfo}">
                                                        <div class="mb-1">
                                                            <span class="font-medium text-gray-700">${entry.key}:</span>
                                                            <c:choose>
                                                                <c:when test="${not empty entry.value}">
                                                                    <span class="uid-code" title="${entry.value}">${entry.value}</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="text-xs italic">(Sin pulsera)</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    -
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <%-- Acciones con clases CSS externas --%>
                                        <td class="px-4 py-4 whitespace-nowrap text-center text-sm space-x-2">
                                            <a href="${pageContext.request.contextPath}/api/admin/asistentes/${a.idAsistente}" class="action-link action-link-view" title="Ver detalles de ${a.nombre}">Ver</a>
                                            <a href="${pageContext.request.contextPath}/api/admin/asistentes/${a.idAsistente}/editar" class="action-link action-link-edit" title="Editar datos de ${a.nombre}">Editar</a>
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
