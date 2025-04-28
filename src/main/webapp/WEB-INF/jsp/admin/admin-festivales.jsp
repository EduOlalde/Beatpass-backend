<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestionar Festivales - Beatpass Admin</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Incluir Menú Admin Común --%>
            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="festivales"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">Gestionar Todos los Festivales</h2>             

            <%-- Mensajes flash (se mantienen clases de Tailwind) --%>
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

            <%-- Filtro por estado (botones con clases CSS externas) --%>
            <div class="mb-4 bg-white p-4 rounded shadow-sm">
                <form action="${pageContext.request.contextPath}/api/admin/festivales/listar-todos" method="get" class="flex items-end space-x-3">
                    <div>
                        <label for="estadoFilter" class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Estado:</label>
                        <select id="estadoFilter" name="estado" class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md shadow-sm">
                            <option value="">-- Todos los Estados --</option>
                            <c:forEach var="e" items="${estadosPosibles}">
                                <option value="${e}" ${requestScope.estadoFiltro == e ? 'selected' : ''}>${e}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <%-- Usamos btn-filter o btn-primary según preferencia --%>
                    <button type="submit" class="btn btn-filter"> 
                        Filtrar
                    </button>
                    <a href="${pageContext.request.contextPath}/api/admin/festivales/listar-todos" class="btn btn-secondary">Limpiar Filtro</a>
                </form>
            </div>


            <%-- Tabla de Festivales --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre Festival</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Promotor</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fechas</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones (Admin)</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty festivales}">
                                <tr> <td colspan="6" class="px-6 py-4 text-center text-sm text-gray-500 italic">
                                        <c:choose>
                                            <c:when test="${not empty requestScope.estadoFiltro}">No hay festivales con estado '${requestScope.estadoFiltro}'.</c:when>
                                            <c:otherwise>No hay festivales registrados.</c:otherwise>
                                        </c:choose>
                                    </td> </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="f" items="${festivales}">
                                    <tr>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${f.idFestival}</td>
                                        <td class="px-6 py-4 whitespace-nowrap"> <div class="text-sm font-medium text-gray-900">${f.nombre}</div> </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <c:if test="${not empty f.nombrePromotor}">
                                                <%-- Enlace con clases Tailwind, no requiere clase CSS específica --%>
                                                <a href="${pageContext.request.contextPath}/api/admin/promotores/${f.idPromotor}/festivales" class="text-indigo-600 hover:underline" title="Ver promotor y sus festivales">
                                                    ${f.nombrePromotor} (ID: ${f.idPromotor})
                                                </a>
                                            </c:if>
                                            <c:if test="${empty f.nombrePromotor}">(ID: ${f.idPromotor})</c:if>
                                            </td>
                                            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"> ${f.fechaInicio} - ${f.fechaFin} </td>
                                        <%-- Badge con clases CSS externas --%>
                                        <td class="px-6 py-4 whitespace-nowrap text-center">
                                            <span class="badge
                                                  <c:choose>
                                                      <c:when test="${f.estado == 'PUBLICADO'}">badge-publicado</c:when>
                                                      <c:when test="${f.estado == 'BORRADOR'}">badge-borrador</c:when>
                                                      <c:when test="${f.estado == 'CANCELADO'}">badge-cancelado</c:when>
                                                      <c:when test="${f.estado == 'FINALIZADO'}">badge-finalizado</c:when>
                                                      <c:otherwise>bg-gray-100 text-gray-800</c:otherwise> <%-- Fallback --%>
                                                  </c:choose>
                                                  "> ${f.estado} </span>
                                        </td>
                                        <%-- Acciones con clases CSS externas --%>
                                        <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-2">
                                            <%-- Acciones de cambio de estado --%>
                                            <c:if test="${f.estado == 'BORRADOR'}">
                                                <form action="${pageContext.request.contextPath}/api/admin/festivales/confirmar" method="post" class="inline" onsubmit="return confirm('Confirmar y publicar \'${f.nombre}\'?');">
                                                    <input type="hidden" name="idFestival" value="${f.idFestival}">
                                                    <button type="submit" class="action-button action-button-confirm" title="Confirmar y Publicar">Confirmar</button>
                                                </form>
                                            </c:if>
                                            <c:if test="${f.estado == 'PUBLICADO'}">
                                                <form action="${pageContext.request.contextPath}/api/admin/festivales/cambiar-estado" method="post" class="inline" onsubmit="return confirm('¿Estás seguro de CANCELAR el festival \'${f.nombre}\'?');">
                                                    <input type="hidden" name="idFestival" value="${f.idFestival}">
                                                    <input type="hidden" name="nuevoEstado" value="CANCELADO">
                                                    <button type="submit" class="action-button action-button-cancel" title="Cancelar Festival">Cancelar</button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/api/admin/festivales/cambiar-estado" method="post" class="inline" onsubmit="return confirm('¿Marcar el festival \'${f.nombre}\' como FINALIZADO?');">
                                                    <input type="hidden" name="idFestival" value="${f.idFestival}">
                                                    <input type="hidden" name="nuevoEstado" value="FINALIZADO">
                                                    <button type="submit" class="action-button action-button-finalize" title="Marcar como Finalizado">Finalizar</button>
                                                </form>
                                            </c:if>
                                            <%-- Enlace a pulseras --%>
                                            <a href="${pageContext.request.contextPath}/api/admin/festivales/${f.idFestival}/pulseras" class="action-link action-link-pulseras" title="Ver Pulseras NFC Asociadas">Pulseras</a>
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
