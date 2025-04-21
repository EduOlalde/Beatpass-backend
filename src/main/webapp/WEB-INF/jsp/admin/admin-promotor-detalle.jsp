<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <c:set var="esNuevo" value="${requestScope.esNuevo}"/>
        <title>${esNuevo ? 'Crear Nuevo Promotor' : 'Editar Promotor'} - Beatpass Admin</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            /* Estilos generales que NO usan @apply */
            body {
                font-family: 'Inter', sans-serif;
            }
            /* Clases para label, input, required-star y botones se aplicarán directamente */
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-2xl">

            <%-- Cabecera Admin (simplificada) --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-purple-700 mb-4 sm:mb-0">Panel de Administración</h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Admin: ${sessionScope.userName}</span>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/api/admin/promotores/listar" class="text-sm text-indigo-600 hover:underline">Gestionar Promotores</a>
                    <a href="${pageContext.request.contextPath}/api/admin/festivales/listar-todos" class="text-sm text-indigo-600 hover:underline">Gestionar Festivales</a>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">
                ${esNuevo ? 'Crear Nuevo Promotor' : 'Editar Promotor'}
                <c:if test="${not esNuevo and not empty promotor.nombre}">: <span class="font-bold text-gray-800">${promotor.nombre}</span></c:if>
                </h2>

                <div class="mb-4">
                    <a href="${pageContext.request.contextPath}/api/admin/promotores/listar" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Gestionar Promotores</a>
            </div>

            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error</p>
                    <p>${requestScope.error}</p>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/api/admin/promotores/guardar" method="post" class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4">
                <c:if test="${not esNuevo}">
                    <input type="hidden" name="idPromotor" value="${promotor.idUsuario}">
                </c:if>

                <div>
                    <%-- Aplicar clases directamente a label --%>
                    <label for="nombre" class="block text-sm font-medium text-gray-700 mb-1">
                        Nombre del Promotor <span class="text-red-500 ml-1">*</span> <%-- Clase para asterisco --%>
                    </label>
                    <%-- Aplicar clases directamente a input --%>
                    <input type="text" id="nombre" name="nombre" value="${promotor.nombre}" required maxlength="100"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                </div>

                <div class="mt-4">
                    <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
                        Email de Contacto <span class="text-red-500 ml-1">*</span>
                    </label>
                    <input type="email" id="email" name="email" value="${promotor.email}" required maxlength="100"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm placeholder-gray-400"
                           placeholder="contacto@promotora.com" ${not esNuevo ? 'readonly' : ''} >
                    <c:if test="${not esNuevo}">
                        <p class="mt-1 text-xs text-gray-500">El email no se puede modificar una vez creado.</p>
                    </c:if>
                </div>

                <c:if test="${esNuevo}">
                    <div class="mt-4">
                        <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
                            Contraseña Inicial <span class="text-red-500 ml-1">*</span>
                        </label>
                        <input type="password" id="password" name="password" required minlength="8"
                               class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                        <p class="mt-1 text-xs text-gray-500">Mínimo 8 caracteres. El promotor deberá cambiarla en su primer inicio de sesión.</p>
                    </div>
                </c:if>

                <c:if test="${not esNuevo}">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Estado Actual</label>
                        <p class="mt-1 text-sm font-semibold p-2 rounded border border-gray-200 ${promotor.estado ? 'text-green-700 bg-green-50' : 'text-red-700 bg-red-50'}">
                            ${promotor.estado ? 'Activo' : 'Inactivo'}
                        </p>
                        <p class="mt-1 text-xs text-gray-500">El estado se activa/desactiva desde la lista de promotores.</p>
                    </div>
                </c:if>

                <div class="mt-6 flex justify-end space-x-3 pt-4 border-t border-gray-200">
                    <%-- Botón Cancelar con clases aplicadas --%>
                    <a href="${pageContext.request.contextPath}/api/admin/promotores/listar"
                       class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out bg-gray-200 hover:bg-gray-300 text-gray-800">
                        Cancelar
                    </a>
                    <%-- Botón Guardar/Crear con clases aplicadas --%>
                    <button type="submit"
                            class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out bg-purple-600 hover:bg-purple-700 text-white">
                        ${esNuevo ? 'Crear Promotor' : 'Guardar Cambios'}
                    </button>
                </div>

            </form>
        </div>

    </body>
</html>
