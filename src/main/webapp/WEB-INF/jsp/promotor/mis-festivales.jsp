<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %> <%-- Usar jakarta.tags.core para JSTL 3.0 --%>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %> <%-- Usar jakarta.tags.fmt para JSTL 3.0 --%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Mis Festivales - Beatpass Promotor</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <style>
            body {
                font-family: 'Inter', sans-serif;
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8">
            <%-- Aquí podrías incluir un menú específico para promotor --%>
            <div class="flex justify-between items-center mb-6">
                <h1 class="text-3xl font-bold text-blue-700">Mis Festivales</h1>
                <div>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales/crear" <%-- Enlace para que el promotor cree --%>
                       class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded shadow">
                        + Crear Nuevo Festival
                    </a>
                    <%-- Enlace de Logout --%>
                    <a href="${pageContext.request.contextPath}/logout" class="ml-4 text-sm text-gray-600 hover:text-red-700">Cerrar Sesión</a>
                </div>
            </div>


            <%-- Mostrar mensajes flash (éxito/error) desde la sesión --%>
            <c:if test="${not empty sessionScope.mensaje}">
                <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${sessionScope.mensaje}</span>
                </div>
                <% session.removeAttribute("mensaje"); %>
            </c:if>
            <c:if test="${not empty sessionScope.error}">
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${sessionScope.error}</span>
                </div>
                <% session.removeAttribute("error");%>
            </c:if>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${requestScope.error}</span>
                </div>
            </c:if>


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
                                            <%-- CORRECCIÓN: Mostrar LocalDate directamente --%>
                                            ${f.fechaInicio} - ${f.fechaFin}
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
                                            <%-- Enlace para que el promotor edite SU festival --%>
                                            <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${f.idFestival}" class="text-indigo-600 hover:text-indigo-900 mr-3">Ver/Editar</a>
                                            <%-- TODO: Añadir enlaces a gestión de entradas, estadísticas, etc. --%>
                                            <%-- <a href="#" class="text-gray-600 hover:text-gray-900 ml-3">Entradas</a> --%>
                                            <%-- <a href="#" class="text-gray-600 hover:text-gray-900 ml-3">Estadísticas</a> --%>
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
