<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<c:set var="pageTitleVar" value="Mis Festivales" />

<jsp:include page="/WEB-INF/jsp/promotor/_header.jsp">
    <jsp:param name="pageTitle" value="${pageTitleVar}" />
    <jsp:param name="currentNav" value="misFestivales" />
</jsp:include>

<div class="container mx-auto p-4 md:p-8 max-w-7xl">

    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
        <h1 class="text-2xl md:text-3xl font-bold text-gray-800 mb-4 sm:mb-0">Mis Festivales</h1>
    </div>

    <%-- Mensajes flash --%>
    <c:if test="${not empty sessionScope.mensaje}">
        <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
            <span class="block sm:inline">${sessionScope.mensaje}</span>
        </div>
        <% session.removeAttribute("mensaje"); %>
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
            <p class="font-bold">Error:</p> <p>${sessionScope.error}</p>
        </div>
        <% session.removeAttribute("error");%>
    </c:if>
    <c:if test="${not empty requestScope.error}">
        <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
            <p class="font-bold">Error:</p> <p>${requestScope.error}</p>
        </div>
    </c:if>

    <%-- Tabla de Festivales --%>
    <div class="bg-white shadow-md rounded-lg overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
                <tr>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fechas</th>
                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ubicación</th>
                    <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                    <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
                <c:choose>
                    <c:when test="${empty festivales}">
                        <tr>
                            <td colspan="5" class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center italic">No tienes festivales creados todavía.</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="f" items="${festivales}">
                            <tr>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <div class="text-sm font-medium text-gray-900">${f.nombre}</div>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                    ${f.fechaInicio} - ${f.fechaFin}
                                </td>
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
                                          ">
                                        ${f.estado}
                                    </span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-3">
                                    <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${f.idFestival}" class="action-link action-link-view" title="Ver detalles de ${fn:escapeXml(f.nombre)}">Ver</a>
                                    <a href="${pageContext.request.contextPath}/api/promotor/festivales/editar/${f.idFestival}" class="action-link action-link-edit" title="Editar datos de ${fn:escapeXml(f.nombre)}">Editar</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

