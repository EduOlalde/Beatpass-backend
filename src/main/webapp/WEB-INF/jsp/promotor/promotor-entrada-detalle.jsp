<%-- 
    Document   : promotor-entradas-detalle
    Created on : 21 abr 2025, 17:03:20
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
        <title>Editar Tipo de Entrada - Beatpass Promotor</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            /* Estilos generales que NO usan @apply */
            body {
                font-family: 'Inter', sans-serif;
            }
            textarea {
                min-height: 6rem;
            }
            /* Las clases para label, input, textarea, required-star y botones se aplicarán directamente */
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-2xl">

            <%-- Cabecera --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">Panel de Promotor</h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-sm text-indigo-600 hover:underline">Mis Festivales</a>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">Editar Tipo de Entrada</h2>

            <div class="mb-4">
                <c:if test="${not empty entrada.idFestival}">
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${entrada.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Detalles del Festival</a>
                </c:if>
            </div>

            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error:</p> <p>${requestScope.error}</p>
                </div>
            </c:if>

            <%-- Formulario de Edición --%>
            <form action="${pageContext.request.contextPath}/api/promotor/entradas/${entrada.idEntrada}/actualizar" method="post" class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4">
                <input type="hidden" name="idEntrada" value="${entrada.idEntrada}">
                <p class="text-sm text-gray-500">Editando entrada para Festival ID: ${entrada.idFestival}</p>

                <div>
                    <%-- Aplicar clases directamente a label --%>
                    <label for="tipo" class="block text-sm font-medium text-gray-700 mb-1">
                        Tipo <span class="text-red-500 ml-1">*</span> <%-- Clase para asterisco --%>
                    </label>
                    <%-- Aplicar clases directamente a input --%>
                    <input type="text" id="tipo" name="tipo" value="${entrada.tipo}" required maxlength="50"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label for="precio" class="block text-sm font-medium text-gray-700 mb-1">
                            Precio (€) <span class="text-red-500 ml-1">*</span>
                        </label>
                        <input type="number" id="precio" name="precio" value="${entrada.precio}" required min="0" step="0.01"
                               class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                    </div>
                    <div>
                        <label for="stock" class="block text-sm font-medium text-gray-700 mb-1">
                            Stock <span class="text-red-500 ml-1">*</span>
                        </label>
                        <input type="number" id="stock" name="stock" value="${entrada.stock}" required min="0" step="1"
                               class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                    </div>
                </div>

                <div>
                    <label for="descripcion" class="block text-sm font-medium text-gray-700 mb-1">Descripción (Opcional)</label>
                    <%-- Aplicar clases directamente a textarea --%>
                    <textarea id="descripcion" name="descripcion" rows="3"
                              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">${entrada.descripcion}</textarea>
                </div>

                <%-- Botones --%>
                <div class="mt-6 flex justify-end space-x-3 pt-4 border-t border-gray-200">
                    <%-- Botón Cancelar con clases aplicadas directamente --%>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${entrada.idFestival}"
                       class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-gray-200 hover:bg-gray-300 text-gray-800">
                        Cancelar
                    </a>
                    <%-- Botón Guardar Cambios con clases aplicadas directamente --%>
                    <button type="submit"
                            class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-indigo-600 hover:bg-indigo-700 text-white">
                        Guardar Cambios
                    </button>
                </div>

            </form>

        </div>

    </body>
</html>
