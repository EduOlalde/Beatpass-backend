<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%-- 
    Vista unificada para listar Clientes (Compradores y Asistentes).
    Recibe los siguientes atributos del request:
    - pageTitle: "Gestionar Clientes - Compradores" o "Gestionar Clientes - Asistentes".
    - activeTab: "compradores" o "asistentes".
    - compradores: Lista de CompradorDTO (si activeTab es 'compradores').
    - asistentes: Lista de AsistenteDTO (si activeTab es 'asistentes').
    - searchTerm: El término de búsqueda actual, si existe.
--%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${pageTitle} - Beatpass Admin</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="clientes"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-2">Gestionar Clientes</h2>

            <%-- Pestañas de Navegación --%>
            <div class="mb-6 border-b border-gray-200">
                <nav class="-mb-px flex space-x-6" aria-label="Tabs">
                    <a href="${pageContext.request.contextPath}/api/admin/clientes?tab=compradores"
                       class="${activeTab == 'compradores' ? 'border-indigo-500 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'} whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm">
                        Compradores
                    </a>
                    <a href="${pageContext.request.contextPath}/api/admin/clientes?tab=asistentes"
                       class="${activeTab == 'asistentes' ? 'border-indigo-500 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'} whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm">
                        Asistentes (Nominados)
                    </a>
                </nav>
            </div>

            <jsp:include page="/WEB-INF/jsp/admin/_admin_messages.jsp"/>

            <%-- Formulario de Búsqueda --%>
            <div class="mb-4 bg-white p-4 rounded shadow-sm">
                <form action="${pageContext.request.contextPath}/api/admin/clientes" method="get" class="flex items-end space-x-3">
                    <input type="hidden" name="tab" value="${activeTab}">
                    <div>
                        <label for="searchTerm" class="block text-sm font-medium text-gray-700 mb-1">Buscar por Nombre o Email:</label>
                        <input type="search" id="searchTerm" name="buscar" value="${searchTerm}" placeholder="Nombre o email..."
                               class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md shadow-sm">
                    </div>
                    <button type="submit" class="btn btn-filter">Buscar</button>
                    <a href="${pageContext.request.contextPath}/api/admin/clientes?tab=${activeTab}" class="btn btn-secondary">Limpiar</a>
                </form>
            </div>

            <%-- Renderizado de la tabla correspondiente --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <c:choose>
                    <c:when test="${activeTab == 'compradores'}">
                        <%-- TABLA DE COMPRADORES --%>
                        <table class="min-w-full divide-y divide-gray-200">
                            <thead class="bg-gray-50">
                                <tr>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Teléfono</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha Registro</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <c:choose>
                                    <c:when test="${empty compradores}">
                                        <tr><td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 italic">No se encontraron compradores.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="c" items="${compradores}">
                                            <tr>
                                                <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${c.idComprador}</td>
                                                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${c.nombre}</td>
                                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${c.email}</td>
                                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${not empty c.telefono ? c.telefono : '-'}</td>
                                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    <fmt:parseDate value="${c.fechaCreacion}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDate" type="date"/>
                                                    <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy"/>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise>
                        <%-- TABLA DE ASISTENTES --%>
                        <table class="min-w-full divide-y divide-gray-200">
                            <thead class="bg-gray-50">
                                <tr>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Festivales / Pulseras</th>
                                    <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <c:choose>
                                    <c:when test="${empty asistentes}">
                                        <tr><td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 italic">No se encontraron asistentes.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="a" items="${asistentes}">
                                            <tr>
                                                <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${a.idAsistente}</td>
                                                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${a.nombre}</td>
                                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${a.email}</td>
                                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                    <c:forEach var="entry" items="${a.festivalPulseraInfo}">
                                                        <div class="mb-1">
                                                            <span class="font-medium text-gray-700">${entry.key}:</span>
                                                            <c:choose>
                                                                <c:when test="${not empty entry.value}"><span class="uid-code">${entry.value}</span></c:when>
                                                                <c:otherwise><span class="text-xs italic">(Sin pulsera)</span></c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </c:forEach>
                                                </td>
                                                <td class="px-4 py-4 whitespace-nowrap text-center text-sm space-x-2">
                                                    <a href="${pageContext.request.contextPath}/api/admin/asistentes/${a.idAsistente}/editar" class="action-link action-link-edit" title="Editar datos de ${a.nombre}">Editar</a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </body>
</html>