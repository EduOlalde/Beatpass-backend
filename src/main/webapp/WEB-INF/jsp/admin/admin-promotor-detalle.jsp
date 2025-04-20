<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <c:set var="esNuevo" value="${requestScope.esNuevo}"/> <%-- Leer el indicador pasado por el Resource --%>
    <title>${esNuevo ? 'Crear Nuevo Promotor' : 'Editar Promotor'} - Beatpass Admin</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style> body {
        font-family: 'Inter', sans-serif;
    } </style>
</head>
<body class="bg-gray-100 text-gray-800">

    <div class="container mx-auto p-4 md:p-8">
        <%-- <jsp:include page="../_admin_menu.jsp" /> --%>

        <h1 class="text-3xl font-bold mb-6 text-purple-700">
            ${esNuevo ? 'Crear Nuevo Promotor' : 'Editar Promotor'} <%-- TODO: Añadir nombre si es edición --%>
        </h1>

        <%-- Mostrar mensajes de error si existen --%>
        <c:if test="${not empty requestScope.error}">
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                <span class="block sm:inline">${requestScope.error}</span>
            </div>
        </c:if>

        <%-- El action apunta al endpoint de guardar del AdminResource --%>
        <form action="${pageContext.request.contextPath}/api/admin/promotores/guardar" method="post" class="bg-white p-6 rounded-lg shadow-md max-w-lg mx-auto">
            <%-- TODO: Añadir campo oculto ID si es edición --%>
            <%-- <input type="hidden" name="idPromotor" value="${promotor.idUsuario}"> --%>

            <div>
                <label for="nombre" class="block text-sm font-medium text-gray-700">Nombre del Promotor <span class="text-red-500">*</span></label>
                <input type="text" id="nombre" name="nombre" value="${promotor.nombre}" required <%-- Muestra valor anterior si hay error --%>
                       class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
            </div>

            <div class="mt-4">
                <label for="email" class="block text-sm font-medium text-gray-700">Email de Contacto <span class="text-red-500">*</span></label>
                <input type="email" id="email" name="email" value="${promotor.email}" required <%-- Muestra valor anterior si hay error --%>
                       class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                       placeholder="contacto@promotora.com">
            </div>

            <c:if test="${esNuevo}"> <%-- Solo pedir contraseña al crear --%>
                <div class="mt-4">
                    <label for="password" class="block text-sm font-medium text-gray-700">Contraseña Inicial <span class="text-red-500">*</span></label>
                    <input type="password" id="password" name="password" required minlength="8"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                    <p class="mt-1 text-xs text-gray-500">Mínimo 8 caracteres. El promotor deberá cambiarla.</p>
                </div>
            </c:if>
            <%-- TODO: Añadir lógica para editar (cambiar estado, etc.) si es necesario --%>

            <div class="mt-6 flex justify-end space-x-3">
                <%-- Enlace Cancelar apunta a la lista de promotores --%>
                <a href="${pageContext.request.contextPath}/api/admin/promotores/listar"
                   class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-2 px-4 rounded shadow">
                    Cancelar
                </a>
                <button type="submit"
                        class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded shadow">
                    ${esNuevo ? 'Crear Promotor' : 'Guardar Cambios'}
                </button>
            </div>

        </form>
    </div>

</body>
</html>
