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
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Teléfono</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha Registro</th>
            </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
        <c:choose>
            <c:when test="${empty compradores}">
                <tr><td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 italic">No se encontraron compradores.</td></tr>
            </c:when>
            <c:otherwise>
                <c:forEach var="c" items="${compradores}">
                    <tr>
                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${c.idComprador}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${c.nombre}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${c.email}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${not empty c.telefono ? c.telefono : '-'}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <fmt:parseDate value="${c.fechaCreacion}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDate" type="date"/>
                    <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy"/>
                    </td>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
</div>