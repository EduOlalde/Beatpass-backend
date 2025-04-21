<%-- 
    Document   : pos-index
    Created on : 21 abr 2025, 21:18:26
    Author     : Eduardo Olalde
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>Punto de Venta/Acceso - Beatpass</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <style> body {
            font-family: 'Inter', sans-serif;
        } </style>
    </head>
    <body class="bg-gray-200 p-10">
        <div class="max-w-md mx-auto bg-white p-8 rounded-lg shadow-lg">
            <h1 class="text-2xl font-bold text-center text-indigo-700 mb-6">Punto de Venta / Acceso</h1>

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

            <%-- Formulario para buscar pulsera por UID --%>
            <form action="${pageContext.request.contextPath}/api/pos/buscar-pulsera" method="get" class="mb-6">
                <label for="codigoUid" class="block text-sm font-medium text-gray-700 mb-1">Buscar Pulsera por UID:</label>
                <div class="flex">
                    <input type="text" id="codigoUid" name="codigoUid" required
                           class="flex-grow mt-1 block w-full rounded-l-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm" placeholder="Leer UID...">
                    <button type="submit" class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded-r-md">Buscar</button>
                </div>
            </form>

            <p class="text-center text-gray-500 text-xs">
                Otras acciones como asociar pulsera, recargar o consumir se realizarían desde la vista de detalle de la pulsera o entrada.
            </p>

            <%-- Enlace Logout (si se usa sesión) --%>
            <div class="text-center mt-6">
                <form action="${pageContext.request.contextPath}/logout" method="post">
                    <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión (Cajero)</button>
                </form>
            </div>

        </div>
    </body>
</html>
