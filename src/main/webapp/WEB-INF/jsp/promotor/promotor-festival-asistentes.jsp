<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<c:set var="pageTitleVar" value="Asistentes - ${festival.nombre}" />

<jsp:include page="/WEB-INF/jsp/promotor/_header.jsp">
    <jsp:param name="pageTitle" value="${pageTitleVar}" />
    <jsp:param name="currentNav" value="detalleFestival" />
</jsp:include>

<div class="container mx-auto p-4 md:p-8 max-w-7xl">

    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
        <h1 class="text-2xl md:text-3xl font-bold text-gray-800 mb-4 sm:mb-0">Asistentes: <span class="text-indigo-700">${festival.nombre}</span></h1>
    </div>

    <div class="mb-6">
        <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm mb-2 inline-block">&larr; Volver a Detalles de ${festival.nombre}</a>
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

    <%-- Tabla de Asistentes del Festival --%>
    <div class="bg-white shadow-md rounded-lg overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
                <tr>
                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Teléfono</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Pulsera Asociada (UID)</th>
                </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
                <c:choose>
                    <c:when test="${empty asistentes}">
                        <tr>
                            <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 italic">
                                No hay asistentes registrados para este festival (o con entradas).
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
                                    <c:set var="pulseraUid" value="${a.festivalPulseraInfo[festival.nombre]}" />
                                    <c:choose>
                                        <c:when test="${not empty pulseraUid}">
                                            <span class="uid-code" title="${pulseraUid}">${pulseraUid}</span>
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>
