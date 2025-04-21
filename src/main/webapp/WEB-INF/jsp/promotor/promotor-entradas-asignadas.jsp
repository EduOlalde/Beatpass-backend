<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %> <%-- Mantenemos la importación por si se usa en otro sitio --%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Entradas Asignadas - ${festival.nombre} - Beatpass Promotor</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            body {
                font-family: 'Inter', sans-serif;
            }
            .badge {
                padding: 0.1em 0.6em;
                border-radius: 9999px;
                font-size: 0.75rem;
                font-weight: 600;
                display: inline-flex;
                align-items: center;
            }
            .badge-activa {
                background-color: #D1FAE5;
                color: #065F46;
            }
            .badge-usada {
                background-color: #E5E7EB;
                color: #374151;
            }
            .badge-cancelada {
                background-color: #FEE2E2;
                color: #991B1B;
            }
            .qr-code {
                font-family: monospace;
                background-color: #f3f4f6;
                padding: 0.1rem 0.3rem;
                border-radius: 0.25rem;
                font-size: 0.8rem;
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Cabecera --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">Panel de Promotor</h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-sm text-indigo-600 hover:underline">Mis Festivales</a>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <div class="mb-6">
                <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm mb-2 inline-block">&larr; Volver a Detalles de ${festival.nombre}</a>
                <h2 class="text-2xl font-semibold text-gray-700">Gestionar Entradas Asignadas: ${festival.nombre}</h2>
            </div>

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

            <%-- Tabla de Entradas Asignadas --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID Entrada</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tipo Original</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Código QR</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Asistente Nominado</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha Nom.</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty entradasAsignadas}">
                                <tr> <td colspan="7" class="px-6 py-4 text-center text-sm text-gray-500 italic"> No hay entradas generadas para este festival todavía. </td> </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="ea" items="${entradasAsignadas}">
                                    <tr>
                                        <td class="px-3 py-4 whitespace-nowrap text-sm text-gray-500">${ea.idEntradaAsignada}</td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700">${ea.tipoEntradaOriginal}</td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <c:if test="${not empty ea.codigoQr}">
                                                <span class="qr-code" title="${ea.codigoQr}"> ${ea.codigoQr.substring(0, 15)}... </span>
                                            </c:if>
                                        </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-center">
                                            <span class="badge
                                                  <c:choose>
                                                      <c:when test="${ea.estado == 'ACTIVA'}">badge-activa</c:when>
                                                      <c:when test="${ea.estado == 'USADA'}">badge-usada</c:when>
                                                      <c:when test="${ea.estado == 'CANCELADA'}">badge-cancelada</c:when>
                                                      <c:otherwise>bg-gray-100 text-gray-800</c:otherwise>
                                                  </c:choose>
                                                  "> ${ea.estado} </span>
                                        </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700">
                                            <c:choose>
                                                <c:when test="${not empty ea.idAsistente}">
                                                    ${ea.nombreAsistente} <br>
                                                    <span class="text-xs text-gray-500">${ea.emailAsistente}</span>
                                                </c:when>
                                                <c:otherwise> <span class="text-xs text-gray-500 italic">(Pendiente de nominar)</span> </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <%-- *** CORRECCIÓN: Eliminar fmt:formatDate *** --%>
                                            <c:if test="${not empty ea.fechaAsignacion}">
                                                ${ea.fechaAsignacion} <%-- Mostrar formato por defecto de LocalDateTime --%>
                                            </c:if>
                                        </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-center text-sm space-x-1">
                                            <%-- Acciones Condicionales --%>
                                            <c:if test="${empty ea.idAsistente and ea.estado == 'ACTIVA'}">
                                                <form action="${pageContext.request.contextPath}/api/promotor/entradas-asignadas/${ea.idEntradaAsignada}/nominar" method="post" class="inline-flex items-center space-x-1"
                                                      onsubmit="return document.getElementById('idAsistente_${ea.idEntradaAsignada}').value.trim() !== '';">
                                                    <input type="number" id="idAsistente_${ea.idEntradaAsignada}" name="idAsistente" required min="1" placeholder="ID Asist." title="Introduce el ID del asistente"
                                                           class="p-1 border border-gray-300 rounded-md shadow-sm text-sm w-20 focus:ring-indigo-500 focus:border-indigo-500">
                                                    <button type="submit" class="text-indigo-600 hover:text-indigo-900 underline font-medium p-0 bg-transparent shadow-none text-sm" title="Nominar esta entrada">Nominar</button>
                                                </form>
                                            </c:if>
                                            <c:if test="${not empty ea.idAsistente and ea.estado == 'ACTIVA'}">
                                                <button type="button" class="text-yellow-600 hover:text-yellow-900 underline font-semibold p-0 bg-transparent shadow-none text-sm" onclick="alert('Modificar nominación entrada ID ${ea.idEntradaAsignada} - Pendiente');">Modificar</button>
                                            </c:if>
                                            <c:if test="${ea.estado == 'ACTIVA'}">
                                                <form action="${pageContext.request.contextPath}/api/promotor/entradas-asignadas/${ea.idEntradaAsignada}/cancelar" method="post" class="inline"
                                                      onsubmit="return confirm('¿Estás seguro de CANCELAR la entrada ID ${ea.idEntradaAsignada}? Esta acción no se puede deshacer y el stock se restaurará.');">
                                                    <button type="submit" class="text-red-600 hover:text-red-900 underline font-semibold p-0 bg-transparent shadow-none text-sm" title="Cancelar Entrada">Cancelar</button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
            <p class="text-xs text-gray-500 mt-2 italic">Nota: Para nominar una entrada, introduce el ID del asistente existente y pulsa "Nominar".</p>

        </div>

    </body>
</html>
