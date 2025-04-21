<%-- 
    Document   : pos-operacion-pulsera
    Created on : 21 abr 2025, 21:18:42
    Author     : Eduardo Olalde
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>Operar Pulsera ${pulsera.codigoUid} - Beatpass POS</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <style> body {
            font-family: 'Inter', sans-serif;
        } </style>
    </head>
    <body class="bg-gray-200 p-10">
        <div class="max-w-lg mx-auto bg-white p-8 rounded-lg shadow-lg">
            <a href="${pageContext.request.contextPath}/pos-index.jsp" class="text-sm text-indigo-600 hover:underline mb-4 inline-block">&larr; Volver a Buscar</a>
            <h1 class="text-2xl font-bold text-center text-indigo-700 mb-4">Pulsera: <span class="font-mono bg-gray-100 px-2 py-1 rounded">${pulsera.codigoUid}</span></h1>

            <%-- Mensajes --%>
            <c:if test="${not empty requestScope.mensaje}">
                <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${requestScope.mensaje}</span>
                </div>
            </c:if>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <strong class="font-bold">Error:</strong>
                    <span class="block sm:inline">${requestScope.error}</span>
                </div>
            </c:if>

            <%-- Mostrar Saldo Actual --%>
            <div class="mb-6 p-4 bg-blue-50 border border-blue-200 rounded text-center">
                <p class="text-sm font-medium text-blue-700">Saldo Actual:</p>
                <p class="text-3xl font-bold text-blue-800">
                <fmt:formatNumber value="${pulsera.saldo}" type="currency" currencySymbol="€"/>
                </p>
            </div>

            <%-- Información Asociada (si existe) --%>
            <c:if test="${not empty pulsera.idEntradaAsignada}">
                <div class="mb-6 p-4 bg-gray-50 border rounded text-sm">
                    <h3 class="font-semibold mb-1">Información Asociada:</h3>
                    <p><strong>Entrada ID:</strong> ${pulsera.idEntradaAsignada}</p>
                    <c:if test="${not empty pulsera.idAsistente}">
                        <p><strong>Asistente:</strong> ${pulsera.nombreAsistente} (${pulsera.emailAsistente})</p>
                    </c:if>
                    <c:if test="${not empty pulsera.idFestival}">
                        <p><strong>Festival:</strong> ${pulsera.nombreFestival} (ID: ${pulsera.idFestival})</p>
                    </c:if>
                </div>
            </c:if>

            <%-- Formulario Recarga --%>
            <div class="mb-6 border-t pt-4">
                <h3 class="text-lg font-semibold mb-2">Registrar Recarga</h3>
                <form action="${pageContext.request.contextPath}/api/pos/pulseras/${pulsera.codigoUid}/recargar" method="post">
                    <div class="flex items-end space-x-2">
                        <div class="flex-grow">
                            <label for="montoRecarga" class="block text-sm font-medium text-gray-700">Monto (€):</label>
                            <input type="number" id="montoRecarga" name="monto" required min="0.01" step="0.01"
                                   class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                        </div>
                        <div>
                            <label for="metodoPago" class="block text-sm font-medium text-gray-700">Método:</label>
                            <select id="metodoPago" name="metodoPago" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                                <option>Efectivo</option>
                                <option>Tarjeta</option>
                                <option>Otro</option>
                            </select>
                        </div>
                        <button type="submit" class="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded shadow">Recargar</button>
                    </div>
                </form>
            </div>

            <%-- Formulario Consumo --%>
            <div class="border-t pt-4">
                <h3 class="text-lg font-semibold mb-2">Registrar Consumo</h3>
                <form action="${pageContext.request.contextPath}/api/pos/pulseras/${pulsera.codigoUid}/consumir" method="post">
                    <%-- Necesitamos saber en qué festival estamos para registrar el consumo --%>
                    <input type="hidden" name="idFestival" value="${pulsera.idFestival}"> <%-- Asume que el DTO tiene idFestival --%>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-2 items-end">
                        <div class="md:col-span-1">
                            <label for="montoConsumo" class="block text-sm font-medium text-gray-700">Monto (€):</label>
                            <input type="number" id="montoConsumo" name="monto" required min="0.01" step="0.01"
                                   class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                        </div>
                        <div class="md:col-span-1">
                            <label for="descConsumo" class="block text-sm font-medium text-gray-700">Descripción:</label>
                            <input type="text" id="descConsumo" name="descripcion" required maxlength="100"
                                   class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm" placeholder="Ej: Bebida">
                        </div>
                        <div class="md:col-span-1">
                            <button type="submit" class="w-full bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded shadow">Consumir</button>
                        </div>
                    </div>
                </form>
            </div>

            <%-- TODO: Mostrar Historial de Recargas y Consumos --%>

        </div>
    </body>
</html>
