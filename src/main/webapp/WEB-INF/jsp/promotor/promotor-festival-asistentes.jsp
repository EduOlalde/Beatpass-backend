<%-- 
    Document   : promotor-festival-asistentes
    Created on : 21 abr 2025, 19:52:48
    Author     : Eduardo Olalde
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Asistentes - ${festival.nombre} - Beatpass Promotor</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            body {
                font-family: 'Inter', sans-serif;
            }
            /* Estilos aplicados directamente */
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Incluir Menú Promotor (si existiera) o cabecera básica --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">Panel de Promotor</h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-sm text-indigo-600 hover:underline font-medium">Mis Festivales</a>
                    <%-- Podríamos añadir más enlaces si el promotor tuviera más secciones --%>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <div class="mb-6">
                <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm mb-2 inline-block">&larr; Volver a Detalles de ${festival.nombre}</a>
                <h2 class="text-2xl font-semibold text-gray-700">Asistentes Registrados para: ${festival.nombre}</h2>
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
                                <%-- Podríamos añadir columnas como "Nº Entradas" si el DTO lo incluyera --%>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                    <c:choose>
                        <c:when test="${empty asistentes}">
                            <tr>
                                <td colspan="4" class="px-6 py-4 text-center text-sm text-gray-500 italic">
                                    No hay asistentes registrados para este festival (o con entradas asignadas).
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
