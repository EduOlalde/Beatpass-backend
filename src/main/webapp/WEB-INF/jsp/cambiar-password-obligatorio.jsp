<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Cambiar Contraseña - Beatpass</title>
        <%-- Fuente Inter --%>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 min-h-screen flex items-center justify-center p-4">

        <div class="bg-white p-8 md:p-10 rounded-xl shadow-xl w-full max-w-md">
            <h1 class="text-2xl font-bold mb-2 text-center text-gray-800">Cambio de Contraseña Requerido</h1>
            <p class="text-sm text-gray-600 text-center mb-6">
                Por seguridad, debes establecer una nueva contraseña para tu cuenta.
            </p>

            <%-- Mostrar mensaje de error si existe (enviado desde PromotorResource en la sesión) --%>
            <c:if test="${not empty sessionScope.passwordChangeError}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm text-sm" role="alert">
                    <p><strong class="font-bold">Error:</strong> ${sessionScope.passwordChangeError}</p>
                </div>
                <%-- Limpiar el error de la sesión después de mostrarlo --%>
                <% session.removeAttribute("passwordChangeError");%>
            </c:if>

            <%-- Formulario apunta al endpoint POST de PromotorResource --%>
            <form action="${pageContext.request.contextPath}/api/promotor/cambiar-password-obligatorio" method="post" class="space-y-4">
                <%-- No necesitamos enviar el ID, ya está en la sesión --%>

                <div>
                    <%-- Aplicar clases de Tailwind directamente a la etiqueta label --%>
                    <label for="newPassword" class="block text-sm font-medium text-gray-700 mb-1">
                        Nueva Contraseña <span class="text-red-500 ml-1">*</span> <%-- Clase 'required-star' aplicada directamente --%>
                    </label>
                    <%-- Aplicar clases de Tailwind directamente a la etiqueta input --%>
                    <input type="password" id="newPassword" name="newPassword" required minlength="8"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm placeholder-gray-400"
                           placeholder="Mínimo 8 caracteres">
                    <p class="mt-1 text-xs text-gray-500">Debe tener al menos 8 caracteres.</p>
                </div>

                <div>
                    <%-- Aplicar clases de Tailwind directamente a la etiqueta label --%>
                    <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-1">
                        Confirmar Nueva Contraseña <span class="text-red-500 ml-1">*</span> <%-- Clase 'required-star' aplicada directamente --%>
                    </label>
                    <%-- Aplicar clases de Tailwind directamente a la etiqueta input --%>
                    <input type="password" id="confirmPassword" name="confirmPassword" required minlength="8"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm placeholder-gray-400"
                           placeholder="Repite la contraseña">
                </div>

                <div class="pt-2">
                    <button type="submit"
                            class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-150 ease-in-out">
                        Establecer Nueva Contraseña
                    </button>
                </div>
            </form>

            <%-- Opción de Logout si el usuario no quiere cambiarla ahora (lo sacará del sistema) --%>
            <div class="mt-4 text-center">
                <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                    <button type="submit" class="text-xs text-gray-500 hover:text-red-600 underline">Cerrar sesión</button>
                </form>
            </div>

        </div>

    </body>
</html>
