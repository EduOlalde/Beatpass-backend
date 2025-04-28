<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Detalles Festival: ${festival.nombre} - Beatpass Promotor</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-4xl">

            <%-- Cabecera (se mantienen clases Tailwind) --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">
                    Detalles del Festival
                </h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">
                ${festival.nombre}
            </h2>

            <div class="mb-4 flex justify-between items-center">
                <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Mis Festivales</a>
                <%-- Botón Editar con clase CSS externa --%>
                <a href="${pageContext.request.contextPath}/api/promotor/festivales/editar/${festival.idFestival}"
                   class="btn btn-edit">
                    Editar Datos Festival
                </a>
            </div>

            <%-- Mensajes (se mantienen clases Tailwind) --%>
            <c:if test="${not empty requestScope.error and empty requestScope.errorEntrada}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error:</p> <p>${requestScope.error}</p>
                </div>
            </c:if>
            <c:if test="${not empty requestScope.mensajeExito}">
                <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p>${requestScope.mensajeExito}</p>
                </div>
            </c:if>


            <%-- Mostrar Datos Generales como solo lectura (usando clase CSS externa) --%>
            <div class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4 mb-10">
                <h3 class="text-lg font-semibold text-gray-600 border-b pb-2 mb-4">Datos Generales del Festival</h3>
                <div>
                    <span class="block text-sm font-medium text-gray-500 mb-1">Nombre del Festival</span>
                    <p class="readonly-value">${festival.nombre}</p>
                </div>
                <div>
                    <span class="block text-sm font-medium text-gray-500 mb-1">Descripción</span>
                    <p class="readonly-value">${not empty festival.descripcion ? festival.descripcion : '-'}</p>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <span class="block text-sm font-medium text-gray-500 mb-1">Fecha de Inicio</span>
                        <p class="readonly-value">${festival.fechaInicio}</p>
                    </div>
                    <div>
                        <span class="block text-sm font-medium text-gray-500 mb-1">Fecha de Fin</span>
                        <p class="readonly-value">${festival.fechaFin}</p>
                    </div>
                </div>
                <div>
                    <span class="block text-sm font-medium text-gray-500 mb-1">Ubicación</span>
                    <p class="readonly-value">${not empty festival.ubicacion ? festival.ubicacion : '-'}</p>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <span class="block text-sm font-medium text-gray-500 mb-1">Aforo</span>
                        <p class="readonly-value">${not empty festival.aforo ? festival.aforo : '-'}</p>
                    </div>
                    <div>
                        <span class="block text-sm font-medium text-gray-500 mb-1">URL de la Imagen</span>
                        <p class="readonly-value break-all">${not empty festival.imagenUrl ? festival.imagenUrl : '-'}</p>
                        <c:if test="${not empty festival.imagenUrl}">
                            <img src="${festival.imagenUrl}" alt="Imagen del festival ${festival.nombre}" class="mt-2 max-h-40 rounded shadow"
                                 onerror="this.style.display='none'; console.error('Error cargando imagen: ${festival.imagenUrl}')">
                        </c:if>
                    </div>
                </div>
                <div>
                    <span class="block text-sm font-medium text-gray-500 mb-1">Estado Actual</span>
                    <%-- Badge con clase CSS externa --%>
                    <p class="readonly-value">
                        <span class="badge
                              <c:choose>
                                  <c:when test="${festival.estado == 'PUBLICADO'}">badge-publicado</c:when>
                                  <c:when test="${festival.estado == 'BORRADOR'}">badge-borrador</c:when>
                                  <c:when test="${festival.estado == 'CANCELADO'}">badge-cancelado</c:when>
                                  <c:when test="${festival.estado == 'FINALIZADO'}">badge-finalizado</c:when>
                                  <c:otherwise>bg-gray-100 text-gray-800</c:otherwise> <%-- Fallback --%>
                              </c:choose>
                              "> ${festival.estado} </span>
                        <span class="text-xs text-gray-500 ml-2"> (Solo modificable por administrador)</span>
                    </p>
                </div>
            </div>

            <%-- Sección Tipos de Entrada (acciones con clases CSS externas) --%>
            <div class="mt-10 pt-6 border-t border-gray-300">
                <h3 class="text-xl font-semibold mb-4 text-gray-700">Tipos de Entrada</h3>
                <div class="bg-white p-4 md:p-6 rounded-lg shadow-md mb-6">
                    <h4 class="text-lg font-medium text-gray-800 mb-3">Entradas Definidas</h4>
                    <div class="overflow-x-auto">
                        <table class="min-w-full divide-y divide-gray-200">
                            <thead class="bg-gray-50">
                                <tr>
                                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Tipo</th>
                                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Descripción</th>
                                    <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Precio (€)</th>
                                    <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Stock</th>
                                    <th class="px-4 py-2 text-center text-xs font-medium text-gray-500 uppercase">Acciones</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <c:choose>
                                    <c:when test="${empty tiposEntrada}">
                                        <tr><td colspan="5" class="px-4 py-3 text-center text-sm text-gray-500 italic">Aún no hay tipos de entrada definidos para este festival.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="entrada" items="${tiposEntrada}">
                                            <tr>
                                                <td class="px-4 py-2 whitespace-nowrap text-sm font-medium text-gray-900">${entrada.tipo}</td>
                                                <td class="px-4 py-2 text-sm text-gray-600">${entrada.descripcion}</td>
                                                <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-600 text-right"><fmt:formatNumber value="${entrada.precio}" type="currency" currencySymbol="€" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-600 text-right">${entrada.stock}</td>
                                                <td class="px-4 py-2 whitespace-nowrap text-center text-sm space-x-2">
                                                    <a href="${pageContext.request.contextPath}/api/promotor/entradas/${entrada.idEntrada}/editar" class="action-link action-link-edit">Editar</a>
                                                    <form action="${pageContext.request.contextPath}/api/promotor/entradas/${entrada.idEntrada}/eliminar" method="post" class="inline" onsubmit="return confirm('¿Eliminar tipo entrada \'${entrada.tipo}\'?');">
                                                        <button type="submit" class="action-button action-button-danger">Eliminar</button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <%-- Gestión de Entradas, Asistentes y Compras (botones con clases CSS externas) --%>
            <div class="mt-10 pt-6 border-t border-gray-300">
                <h3 class="text-xl font-semibold mb-4 text-gray-700">Gestión del Festival</h3>
                <div class="bg-white p-6 rounded-lg shadow-md grid grid-cols-1 md:grid-cols-4 gap-4">
                    <div>
                        <p class="text-gray-600 mb-2 text-sm">Ver/gestionar entradas individuales y nominarlas.</p>
                        <a href="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/entradas-asignadas"
                           class="btn btn-info w-full">
                            Gestionar Entradas
                        </a>
                    </div>
                    <div>
                        <p class="text-gray-600 mb-2 text-sm">Consultar asistentes con entradas para este festival.</p>
                        <a href="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/asistentes"
                           class="btn btn-teal w-full">
                            Ver Asistentes
                        </a>
                    </div>
                    <div>
                        <p class="text-gray-600 mb-2 text-sm">Consultar pulseras NFC asociadas a entradas.</p>
                        <a href="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/pulseras"
                           class="btn btn-orange w-full">
                            Ver Pulseras NFC
                        </a>
                    </div>
                    <div>
                        <p class="text-gray-600 mb-2 text-sm">Consultar el historial de compras realizadas.</p>
                        <a href="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/compras"
                           class="btn btn-purple w-full">
                            Ver Compras
                        </a>
                    </div>
                </div>
            </div>

        </div> <%-- Fin container --%>

    </body>
</html>
