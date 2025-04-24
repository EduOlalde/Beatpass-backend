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
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            body {
                font-family: 'Inter', sans-serif;
            }
            /* Clases para badges (copiadas de festival-detalle.jsp para consistencia) */
            .badge {
                padding: 0.1em 0.6em;
                border-radius: 9999px;
                font-size: 0.75rem;
                font-weight: 600;
                display: inline-flex;
                align-items: center;
            }
            .badge-borrador {
                background-color: #FEF3C7;
                color: #92400E;
            } /* yellow */
            .badge-publicado {
                background-color: #D1FAE5;
                color: #065F46;
            } /* green */
            .badge-cancelado {
                background-color: #FEE2E2;
                color: #991B1B;
            } /* red */
            .badge-finalizado {
                background-color: #E5E7EB;
                color: #374151;
            } /* gray */
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">
            <%-- Cabecera Promotor --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">Mis Festivales</h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales/crear"
                       class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out bg-indigo-600 hover:bg-indigo-700 text-white text-sm">
                        + Crear Nuevo Festival
                    </a>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>


            <%-- Mostrar mensajes flash (éxito/error) desde la sesión --%>
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
                                            <%-- LocalDate se muestra bien por defecto, opcionalmente formatear --%>
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
                                                      <c:otherwise>bg-gray-100 text-gray-800</c:otherwise> <%-- Fallback --%>
                                                  </c:choose>
                                                  ">
                                                ${f.estado}
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-3">
                                            <%-- *** CAMBIO: Separar enlaces Ver y Editar *** --%>
                                            <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${f.idFestival}" class="text-indigo-600 hover:text-indigo-900 underline font-medium" title="Ver detalles de ${f.nombre}">Ver</a>
                                            <a href="${pageContext.request.contextPath}/api/promotor/festivales/editar/${f.idFestival}" class="text-yellow-600 hover:text-yellow-900 underline font-semibold" title="Editar datos de ${f.nombre}">Editar</a>
                                            <%-- Otros enlaces (Entradas, Asistentes, Pulseras) podrían ir aquí o en la página de Ver --%>
                                            <%-- Ejemplo: <a href="${pageContext.request.contextPath}/api/promotor/festivales/${f.idFestival}/entradas-asignadas" class="text-blue-600 hover:text-blue-900">Entradas</a> --%>
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
