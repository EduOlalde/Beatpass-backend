<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %> <%-- Aunque no usemos formatDate para LocalDate, lo dejamos por si se usa para otros tipos --%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Festivales de ${promotor.nombre} - Beatpass Admin</title> <%-- Título dinámico --%>
        <script src="https://cdn.tailwindcss.com"></script>
        <style> body {
            font-family: 'Inter', sans-serif;
        } </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8">
            <%-- Incluir menú admin si existe --%>
            <%-- <jsp:include page="../_admin_menu.jsp" /> --%>

            <div class="mb-4">
                <%-- Enlace para volver a la lista de promotores --%>
                <a href="${pageContext.request.contextPath}/api/admin/promotores/listar" class="text-indigo-600 hover:text-indigo-800">&larr; Volver a Promotores</a>
            </div>

            <h1 class="text-3xl font-bold mb-6 text-purple-700">
                Festivales de: <span class="text-gray-800">${promotor.nombre}</span> (ID: ${promotor.idUsuario})
            </h1>

            <%-- Mostrar mensajes flash (si aplicara alguna acción desde esta página) --%>
            <%-- ... (código de mensajes flash similar a otras páginas) ... --%>

            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID Festival</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fechas</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ubicación</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones (Admin)</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty festivales}">
                                <tr>
                                    <td colspan="6" class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">Este promotor no tiene festivales asignados.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="f" items="${festivales}">
                                    <tr>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${f.idFestival}</td>
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
                                            <%-- TODO: Añadir acciones específicas que el Admin pueda hacer sobre festivales de otros? --%>
                                            <%-- Ej: Ver detalles completos, cambiar estado (con cuidado), etc. --%>
                                            <%-- <a href="${pageContext.request.contextPath}/api/admin/festivales/ver/${f.idFestival}" class="text-indigo-600 hover:text-indigo-900 mr-3">Ver Detalles</a> --%>
                                            <span class="text-gray-400 italic"> (Ver detalles...) </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
            <%-- Paginación si fuera necesaria --%>
        </div>

    </body>
</html>
