<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> <%-- Para formatear fechas --%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Mis Festivales - Beatpass Promotor</title>
        <%-- Incluir Tailwind CSS desde CDN --%>
        <script src="https://cdn.tailwindcss.com"></script>
        <style>
            /* Estilos adicionales si son necesarios */
            body {
                font-family: 'Inter', sans-serif;
            } /* Asegura la fuente de RIU6 */
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8">
            <h1 class="text-3xl font-bold mb-6 text-blue-700">Mis Festivales</h1>

            <%-- Mostrar mensajes flash (éxito/error) desde la sesión --%>
            <c:if test="${not empty sessionScope.mensaje}">
                <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${sessionScope.mensaje}</span>
                </div>
                <% session.removeAttribute("mensaje"); %> <%-- Limpiar mensaje después de mostrarlo --%>
            </c:if>
            <c:if test="${not empty sessionScope.error}">
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${sessionScope.error}</span>
                </div>
                <% session.removeAttribute("error");%> <%-- Limpiar mensaje después de mostrarlo --%>
            </c:if>
            <c:if test="${not empty requestScope.error}"> <%-- Errores directos de la request --%>
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${requestScope.error}</span>
                </div>
            </c:if>


            <div class="mb-4 text-right">
                <a href="${pageContext.request.contextPath}/promotor/festivales/crear"
                   class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded shadow">
                    + Crear Nuevo Festival
                </a>
            </div>

            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fechas</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ubicación</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                    <c:choose>
                        <c:when test="${empty festivales}">
                            <tr>
                                <td colspan="5" class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">No tienes festivales creados todavía.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="f" items="${festivales}">
                                <tr>
                                    <td class="px-6 py-4 whitespace-nowrap">
                                        <div class="text-sm font-medium text-gray-900">${f.nombre}</div>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                <fmt:formatDate value="${f.fechaInicio}" pattern="dd/MM/yyyy"/> -
                                <fmt:formatDate value="${f.fechaFin}" pattern="dd/MM/yyyy"/>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${f.ubicacion}</td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full
                                          <c:choose>
                                          <c:when test="${f.estado == 'PUBLICADO'}">bg-green-100 text-green-800</c:when>
                                        <c:when test="${f.estado == 'BORRADOR'}">bg-yellow-100 text-yellow-800</c:when>
                                        <c:when test="${f.estado == 'CANCELADO'}">bg-red-100 text-red-800</c:when>
                                        <c:when test="${f.estado == 'FINALIZADO'}">bg-gray-100 text-gray-800</c:when>
                                        <c:otherwise>bg-gray-100 text-gray-800</c:otherwise>
                                        </c:choose>
                                        ">
                                        ${f.estado}
                                    </span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                    <a href="${pageContext.request.contextPath}/promotor/festivales/ver?id=${f.idFestival}" class="text-indigo-600 hover:text-indigo-900 mr-3">Ver/Editar</a>
                                    <%-- Formulario para eliminar (más seguro que un link GET) --%>
                                    <form action="${pageContext.request.contextPath}/promotor/festivales/" method="post" class="inline" onsubmit="return confirm('¿Estás seguro de que quieres eliminar el festival ${f.nombre}? Esta acción no se puede deshacer.');">
                                        <input type="hidden" name="action" value="eliminar">
                                        <input type="hidden" name="idFestival" value="${f.idFestival}">
                                        <button type="submit" class="text-red-600 hover:text-red-900">Eliminar</button>
                                    </form>
                                    <%-- Añadir enlace a estadísticas, entradas, etc. aquí --%>
                                    <%-- <a href="#" class="text-gray-600 hover:text-gray-900 ml-3">Estadísticas</a> --%>
                                    <%-- <a href="#" class="text-gray-600 hover:text-gray-900 ml-3">Entradas</a> --%>
                                </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
            <%-- Aquí iría la lógica de paginación si fuera necesaria --%>
        </div>

    </body>
</html>

