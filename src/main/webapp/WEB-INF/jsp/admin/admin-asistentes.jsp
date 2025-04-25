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
            /* Estilos generales */
            body {
                font-family: 'Inter', sans-serif;
            }
            /* Clases base para botones */
            .btn {
                font-weight: bold;
                padding: 0.5rem 1rem;
                border-radius: 0.375rem;
                box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
                transition: all 150ms ease-in-out;
                display: inline-flex;
                align-items: center;
                font-size: 0.875rem;
            }
            .btn-primary {
                background-color: #8B5CF6;
                color: white;
            }
            .btn-primary:hover {
                background-color: #7C3AED;
            }
            .btn-secondary {
                background-color: #E5E7EB;
                color: #1F2937;
            }
            .btn-secondary:hover {
                background-color: #D1D5DB;
            }
            .btn-filter {
                background-color: #2563EB;
                color: white;
            }
            .btn-filter:hover {
                background-color: #1D4ED8;
            }
            /* Estilos para acciones en tablas */
            .action-link {
                text-decoration: underline;
                font-size: 0.75rem;
            }
            .action-link-view {
                color: #4F46E5;
                font-weight: 500;
            }
            .action-link-view:hover {
                color: #3730A3;
            }
            .action-link-edit {
                color: #D97706;
                font-weight: 600;
            }
            .action-link-edit:hover {
                color: #92400E;
            }
            /* Estilo para código UID */
            .uid-code {
                font-family: monospace;
                background-color: #f3f4f6;
                padding: 0.1rem 0.3rem;
                border-radius: 0.25rem;
                font-size: 0.8rem;
                display: inline-block;
                margin-left: 0.25rem;
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
                                <%-- *** COLUMNA ACTUALIZADA *** --%>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Festivales / Pulseras</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty asistentes}">
                                <tr>
                                    <%-- *** Colspan ajustado *** --%>
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
                                        <%-- *** CELDA ACTUALIZADA PARA MAPA FESTIVAL/PULSERA *** --%>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <c:choose>
                                                <c:when test="${not empty a.festivalPulseraInfo}">
                                                    <%-- Itera sobre el mapa (clave=festival, valor=pulseraUID) --%>
                                                    <c:forEach var="entry" items="${a.festivalPulseraInfo}">
                                                        <div class="mb-1"> <%-- Div para cada par --%>
                                                            <span class="font-medium text-gray-700">${entry.key}:</span> <%-- Nombre Festival --%>
                                                            <c:choose>
                                                                <c:when test="${not empty entry.value}">
                                                                    <span class="uid-code" title="${entry.value}">${entry.value}</span> <%-- UID Pulsera --%>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="text-xs italic">(Sin pulsera)</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    - <%-- Mostrar guión si no hay mapa o está vacío --%>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
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
