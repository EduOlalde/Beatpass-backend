<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- Este fragmento se encarga de mostrar los mensajes flash de éxito o error --%>
<c:if test="${not empty requestScope.mensajeExito}">
    <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
        <p class="font-bold">Éxito</p>
        <p>${requestScope.mensajeExito}</p>
    </div>
</c:if>

<c:if test="${not empty requestScope.error}">
    <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
        <p class="font-bold">Error</p>
        <p>${requestScope.error}</p>
    </div>
</c:if>