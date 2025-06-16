<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<div class="bg-white shadow-md rounded-lg overflow-x-auto">
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
</div>