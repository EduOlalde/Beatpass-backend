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
            /* Estilos generales */
            body {
                font-family: 'Inter', sans-serif;
            }
            /* Clases base para botones */
            .btn {
                font-weight: bold; /* font-bold */
                padding-top: 0.5rem; /* py-2 */
                padding-bottom: 0.5rem; /* py-2 */
                padding-left: 1rem; /* px-4 */
                padding-right: 1rem; /* px-4 */
                border-radius: 0.375rem; /* rounded */
                box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); /* shadow */
                transition-property: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform; /* transition */
                transition-duration: 150ms; /* duration-150 */
                transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); /* ease-in-out */
                display: inline-flex; /* inline-flex */
                align-items: center; /* items-center */
                font-size: 0.875rem; /* text-sm */
            }
            .btn-primary {
                background-color: #8B5CF6; /* bg-purple-600 */
                color: white; /* text-white */
            }
            .btn-primary:hover {
                background-color: #7C3AED; /* hover:bg-purple-700 */
            }
            .btn-secondary {
                background-color: #E5E7EB; /* bg-gray-200 */
                color: #1F2937; /* text-gray-800 */
            }
            .btn-secondary:hover {
                background-color: #D1D5DB; /* hover:bg-gray-300 */
            }
            /* Estilos para campos readonly */
            .readonly-field {
                margin-top: 0.25rem; /* mt-1 */
                display: block;
                width: 100%;
                border-radius: 0.375rem; /* rounded-md */
                border-width: 1px;
                border-color: #E5E7EB; /* border-gray-200 */
                background-color: #F9FAFB; /* bg-gray-50 */
                padding-left: 0.75rem; /* px-3 */
                padding-right: 0.75rem; /* px-3 */
                padding-top: 0.5rem; /* py-2 */
                padding-bottom: 0.5rem; /* py-2 */
                font-size: 0.875rem; /* text-sm */
                color: #374151; /* text-gray-700 */
                box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); /* shadow-sm */
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-2xl">

            <%-- Incluir Menú Admin Común --%>
            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="promotores"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">
                ${esNuevo ? 'Crear Nuevo Promotor' : 'Editar Promotor'}
                <c:if test="${not esNuevo and not empty promotor.nombre}">: <span class="font-bold text-gray-800">${promotor.nombre}</span></c:if>
                </h2>

                <div class="mb-4">
                    <a href="${pageContext.request.contextPath}/api/admin/promotores/listar" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Gestionar Promotores</a>
            </div>

            <%-- Mensajes de error --%>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error</p>
                    <p>${requestScope.error}</p>
                </div>
            </c:if>

            <%-- Formulario --%>
            <form action="${pageContext.request.contextPath}/api/admin/promotores/guardar" method="post" class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4">
                <%-- Campo oculto para ID en modo edición (no necesario en creación) --%>
                <c:if test="${not esNuevo}">
                    <input type="hidden" name="idPromotor" value="${promotor.idUsuario}">
                </c:if>
                <div>
                    <label for="nombre" class="block text-sm font-medium text-gray-700 mb-1">
                        Nombre del Promotor <span class="text-red-500 ml-1">*</span>
                    </label>
                    <input type="text" id="nombre" name="nombre" value="${promotor.nombre}" required maxlength="100"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                </div>
                <div class="mt-4">
                    <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
                        Email de Contacto <span class="text-red-500 ml-1">*</span>
                    </label>
                    <input type="email" id="email" name="email" value="${promotor.email}" required maxlength="100"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm placeholder-gray-400 ${not esNuevo ? 'bg-gray-100 cursor-not-allowed' : ''}"
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
                        <%-- Campo readonly con estilo homogeneizado --%>
                        <p class="readonly-field ${promotor.estado ? 'text-green-700 bg-green-50 border-green-200' : 'text-red-700 bg-red-50 border-red-200'}">
                            ${promotor.estado ? 'Activo' : 'Inactivo'}
                        </p>
                        <p class="mt-1 text-xs text-gray-500">El estado se activa/desactiva desde la lista de promotores.</p>
                    </div>
                </c:if>
                <div class="mt-6 flex justify-end space-x-3 pt-4 border-t border-gray-200">
                    <%-- Botones con estilos homogeneizados --%>
                    <a href="${pageContext.request.contextPath}/api/admin/promotores/listar"
                       class="btn btn-secondary">
                        Cancelar
                    </a>
                    <button type="submit"
                            class="btn btn-primary">
                        ${esNuevo ? 'Crear Promotor' : 'Guardar Cambios'}
                    </button>
                </div>
            </form>
        </div>
    </body>
</html>
