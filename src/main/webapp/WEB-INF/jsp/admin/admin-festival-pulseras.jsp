<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Pulseras NFC - ${festival.nombre} - Admin</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="festivales"/>
            </jsp:include>

            <div class="mb-6">
                <a href="${pageContext.request.contextPath}/api/admin/festivales/listar-todos" class="text-indigo-600 hover:text-indigo-800 text-sm mb-2 inline-block">&larr; Volver a Gestionar Festivales</a>
                <h2 class="text-2xl font-semibold text-gray-700">Pulseras NFC Asociadas: ${festival.nombre}</h2>
            </div>

            <%-- Mensajes flash --%>
            <c:if test="${not empty requestScope.mensajeExito}">
                <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded-md shadow-sm" role="alert"> <p>${requestScope.mensajeExito}</p> </div>
            </c:if>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert"> <p class="font-bold">Error:</p> <p>${requestScope.error}</p> </div>
            </c:if>

            <%-- Tabla de Pulseras NFC --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID Pulsera</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Código UID</th>
                            <th scope="col" class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Saldo (€)</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Entrada Asociada (ID)</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Asistente Asociado</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty pulseras}">
                                <tr> <td colspan="7" class="px-6 py-4 text-center text-sm text-gray-500 italic"> No hay pulseras asociadas a este festival todavía. </td> </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="p" items="${pulseras}">
                                    <tr>
                                        <td class="px-3 py-4 whitespace-nowrap text-sm text-gray-500">${p.idPulsera}</td>
                                        <%-- UID con clase CSS externa --%>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700"> <span class="uid-code" title="${p.codigoUid}">${p.codigoUid}</span> </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900 text-right font-semibold"> <fmt:formatNumber value="${p.saldo}" type="currency" currencySymbol="€"/> </td>
                                        <%-- Badge con clase CSS externa (unificada) --%>
                                        <td class="px-4 py-4 whitespace-nowrap text-center">
                                            <span class="badge ${p.activa ? 'badge-activa' : 'badge-inactiva'}"> ${p.activa ? 'Activa' : 'Inactiva'} </span>
                                        </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${not empty p.idEntrada ? p.idEntrada : '-'}</td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700">
                                            <c:if test="${not empty p.idAsistente}">
                                                ${p.nombreAsistente} <br> <span class="text-xs text-gray-500">${p.emailAsistente}</span>
                                            </c:if>
                                            <c:if test="${empty p.idAsistente}"> - </c:if>
                                            </td>
                                        <%-- Acciones --%>
                                        <td class="px-4 py-4 whitespace-nowrap text-center text-sm space-x-2">
                                            <a href="#" class="action-link action-link-view" title="Ver detalles de la pulsera (Pendiente)">Ver Detalles</a>
                                            <c:if test="${p.activa}">
                                                <%-- TODO: Implementar acción de desactivación --%>                                              
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
