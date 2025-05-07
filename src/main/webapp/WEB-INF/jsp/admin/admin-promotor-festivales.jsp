<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Festivales de ${promotor.nombre} - Beatpass Admin</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="promotores"/>
            </jsp:include>

            <div class="mb-4">
                <a href="${pageContext.request.contextPath}/api/admin/promotores/listar" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a la lista de Promotores</a>
            </div>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">
                Festivales gestionados por: <span class="font-bold text-gray-800">${promotor.nombre}</span> (ID: ${promotor.idUsuario})
            </h2>

            <%-- Tabla de Festivales del Promotor --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID Fest.</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fechas</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ubicación</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones (Admin)</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty festivales}">
                                <tr> <td colspan="6" class="px-6 py-4 text-center text-sm text-gray-500 italic">Este promotor no tiene ningún festival asignado.</td> </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="f" items="${festivales}">
                                    <tr>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${f.idFestival}</td>
                                        <td class="px-6 py-4 whitespace-nowrap"> <div class="text-sm font-medium text-gray-900">${f.nombre}</div> </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"> ${f.fechaInicio} - ${f.fechaFin} </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${f.ubicacion}</td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center">
                                            <span class="badge
                                                  <c:choose>
                                                      <c:when test="${f.estado == 'PUBLICADO'}">badge-publicado</c:when>
                                                      <c:when test="${f.estado == 'BORRADOR'}">badge-borrador</c:when>
                                                      <c:when test="${f.estado == 'CANCELADO'}">badge-cancelado</c:when>
                                                      <c:when test="${f.estado == 'FINALIZADO'}">badge-finalizado</c:when>
                                                      <c:otherwise>bg-gray-100 text-gray-800</c:otherwise> 
                                                  </c:choose>
                                                  "> ${f.estado} </span>
                                        </td>
                                        <%-- Acciones --%>
                                        <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-2">
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
