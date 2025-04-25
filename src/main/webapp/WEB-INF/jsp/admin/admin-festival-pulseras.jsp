<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Pulseras NFC - ${festival.nombre} - Admin</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            /* Estilos generales */
            body {
                font-family: 'Inter', sans-serif;
            }
            /* Estilos para badges de estado */
            .badge {
                padding: 0.1em 0.6em;
                border-radius: 9999px;
                font-size: 0.75rem; /* text-xs */
                font-weight: 600; /* font-semibold */
                display: inline-flex;
                align-items: center;
            }
            .badge-activa {
                background-color: #D1FAE5; /* green-100 */
                color: #065F46; /* green-800 */
            }
            .badge-inactiva {
                background-color: #FEE2E2; /* red-100 */
                color: #991B1B; /* red-800 */
            }
            /* Estilo para código UID */
            .uid-code {
                font-family: monospace;
                background-color: #f3f4f6; /* gray-100 */
                padding: 0.1rem 0.3rem;
                border-radius: 0.25rem;
                font-size: 0.8rem;
            }
            /* Estilos para acciones en tablas */
            .action-link {
                text-decoration: underline;
                font-size: 0.75rem; /* text-xs */
            }
            .action-link-view {
                color: #4F46E5; /* text-indigo-600 */
                font-weight: 500; /* font-medium */
            }
            .action-link-view:hover {
                color: #3730A3; /* hover:text-indigo-900 */
            }
            .action-button-danger {
                color: #DC2626; /* text-red-600 */
                font-weight: 600; /* font-semibold */
            }
            .action-button-danger:hover {
                color: #991B1B; /* hover:text-red-900 */
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Incluir Menú Admin Común --%>
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
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700"> <span class="uid-code" title="${p.codigoUid}">${p.codigoUid}</span> </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900 text-right font-semibold"> <fmt:formatNumber value="${p.saldo}" type="currency" currencySymbol="€"/> </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-center">
                                            <span class="badge ${p.activa ? 'badge-activa' : 'badge-inactiva'}"> ${p.activa ? 'Activa' : 'Inactiva'} </span>
                                        </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${not empty p.idEntradaAsignada ? p.idEntradaAsignada : '-'}</td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700">
                                            <c:if test="${not empty p.idAsistente}">
                                                ${p.nombreAsistente} <br> <span class="text-xs text-gray-500">${p.emailAsistente}</span>
                                            </c:if>
                                            <c:if test="${empty p.idAsistente}"> - </c:if>
                                            </td>
                                            <td class="px-4 py-4 whitespace-nowrap text-center text-sm space-x-2">
                                            <%-- Acción Ver Detalles (Estilo Homogeneizado) --%>
                                            <%-- TODO: Implementar vista real de detalles (recargas/consumos) --%>
                                            <a href="#" class="action-link action-link-view" title="Ver detalles de la pulsera (Pendiente)">Ver Detalles</a>
                                            <c:if test="${p.activa}">
                                                <%-- TODO: Implementar acción de desactivación --%>
                                                <%-- <form action="..." method="post" class="inline"> ... <button type="submit" class="action-link action-button-danger">Desactivar</button> </form> --%>
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
