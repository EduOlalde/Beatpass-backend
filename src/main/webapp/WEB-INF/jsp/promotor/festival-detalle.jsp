<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %> 

<c:set var="pageTitleVar" value="Detalles: ${festival.nombre}" />

<jsp:include page="/WEB-INF/jsp/promotor/_header.jsp">
    <jsp:param name="pageTitle" value="${pageTitleVar}" />
    <jsp:param name="currentNav" value="detalleFestival" />
</jsp:include>

<div class="container mx-auto p-4 md:p-8 max-w-4xl">

    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
        <h1 class="text-2xl md:text-3xl font-bold text-gray-800 mb-4 sm:mb-0 text-center sm:text-left">
            Detalles: <span class="text-indigo-700">${festival.nombre}</span>
        </h1>
        <div class="flex items-center space-x-3 mt-3 sm:mt-0">
            <a href="${pageContext.request.contextPath}/api/promotor/festivales/editar/${festival.idFestival}"
               class="btn btn-edit btn-sm">
                Editar Datos
            </a>
        </div>
    </div>

    <div class="mb-6">
        <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Mis Festivales</a>
    </div>

    <%-- Mensajes Flash --%>
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

    <%-- Mostrar Datos Generales como solo lectura --%>
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
            <p class="readonly-value">
                <span class="badge
                      <c:choose>
                          <c:when test="${festival.estado == 'PUBLICADO'}">badge-publicado</c:when>
                          <c:when test="${festival.estado == 'BORRADOR'}">badge-borrador</c:when>
                          <c:when test="${festival.estado == 'CANCELADO'}">badge-cancelado</c:when>
                          <c:when test="${festival.estado == 'FINALIZADO'}">badge-finalizado</c:when>
                          <c:otherwise>bg-gray-100 text-gray-800</c:otherwise>
                      </c:choose>
                      "> ${festival.estado} </span>
                <span class="text-xs text-gray-500 ml-2"> (Solo modificable por administrador)</span>
            </p>
        </div>
    </div>

    <%-- Sección Tipos de Entrada --%>
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

                            <%-- NUEVA COLUMNA --%>
                            <th class="px-4 py-2 text-center text-xs font-medium text-gray-500 uppercase">Modalidad</th>

                            <th class="px-4 py-2 text-center text-xs font-medium text-gray-500 uppercase">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty tiposEntrada}">
                                <tr><td colspan="6" class="px-4 py-3 text-center text-sm text-gray-500 italic">Aún no hay tipos de entrada definidos para este festival.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="tipoEntrada" items="${tiposEntrada}">
                                    <tr>
                                        <td class="px-4 py-2 whitespace-nowrap text-sm font-medium text-gray-900">${tipoEntrada.tipo}</td>
                                        <td class="px-4 py-2 text-sm text-gray-600">${tipoEntrada.descripcion}</td>
                                        <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-600 text-right"><fmt:formatNumber value="${tipoEntrada.precio}" type="currency" currencySymbol="€" minFractionDigits="2" maxFractionDigits="2"/></td>
                                        <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-600 text-right">${tipoEntrada.stock}</td>

                                        <td class="px-4 py-2 whitespace-nowrap text-center text-sm">
                                            <c:choose>
                                                <c:when test="${tipoEntrada.requiereNominacion}">
                                                    <span class="badge badge-activo" title="Esta entrada debe ser asignada a un asistente.">Nominativa</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-usada" title="Esta entrada no necesita ser asignada a un asistente.">Al Portador</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <td class="px-4 py-2 whitespace-nowrap text-center text-sm space-x-2">
                                            <a href="${pageContext.request.contextPath}/api/promotor/tiposEntrada/${tipoEntrada.idTipoEntrada}/editar" class="action-link-edit">Editar</a>
                                            <form action="${pageContext.request.contextPath}/api/promotor/tiposEntrada/${tipoEntrada.idTipoEntrada}/eliminar" method="POST" style="display:inline;" onsubmit="return confirm('¿Estás seguro de que quieres eliminar el tipo de entrada \'${fn:escapeXml(tipoEntrada.tipo)}\'? Esta acción no se puede deshacer.');">
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

    <%-- Gestión de Entradas, Asistentes y Compras --%>
    <div class="mt-10 pt-6 border-t border-gray-300">
        <h3 class="text-xl font-semibold mb-4 text-gray-700">Gestión del Festival</h3>
        <div class="bg-white p-6 rounded-lg shadow-md grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div>
                <p class="text-gray-600 mb-2 text-sm">Ver/gestionar entradas individuales y nominarlas.</p>
                <a href="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/entradas"
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

</div>
