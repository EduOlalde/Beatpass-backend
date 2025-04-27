<%--  
    Document   : promotor-festival-compras
    Created on : 26 abr 2025, 15:54:09
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
        <title>Compras - ${festival.nombre} - Beatpass Promotor</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Cabecera Promotor (se mantienen clases Tailwind) --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">Panel de Promotor</h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-sm text-indigo-600 hover:underline font-medium">Mis Festivales</a>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <%-- Título y enlace volver --%>
            <div class="mb-6">
                <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm mb-2 inline-block">&larr; Volver a Detalles de ${festival.nombre}</a>
                <h2 class="text-2xl font-semibold text-gray-700">Historial de Compras: ${festival.nombre}</h2>
            </div>

            <%-- Mensajes flash (se mantienen clases Tailwind) --%>
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

            <%-- Tabla de Compras --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID Compra</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Comprador</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Entradas</th>
                            <th scope="col" class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Total (€)</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                    <c:choose>
                        <c:when test="${empty compras}">
                            <tr>
                                <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 italic">
                                    No se han registrado compras para este festival todavía.
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="compra" items="${compras}">
                                <tr>
                                    <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${compra.idCompra}</td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        <fmt:parseDate value="${compra.fechaCompra}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDateTime" type="both" />
                                        <fmt:formatDate value="${parsedDateTime}" pattern="dd/MM/yyyy HH:mm:ss" />
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                        ${compra.nombreAsistente}<br>
                                        <span class="text-xs text-gray-500">${compra.emailAsistente}</span>
                                    </td>
                                    <td class="px-6 py-4 text-sm text-gray-500">
                                        <%-- Mostrar el resumen de entradas --%>
                                        <c:forEach var="resumen" items="${compra.resumenEntradas}" varStatus="loop">
                                            ${resumen}<c:if test="${not loop.last}"><br></c:if>
                                        </c:forEach>
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900 text-right font-semibold">
                                        <fmt:formatNumber value="${compra.total}" type="currency" currencySymbol="€"/>
                                    </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

        </div> <%-- Fin container --%>

    </body>
</html>
