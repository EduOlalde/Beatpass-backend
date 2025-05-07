<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Iniciar Sesión - Beatpass</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gradient-to-r from-blue-500 to-purple-600 min-h-screen flex items-center justify-center">

        <div class="bg-white p-8 md:p-12 rounded-xl shadow-2xl w-full max-w-md">
            <h1 class="text-3xl font-bold mb-8 text-center text-gray-800">Beatpass</h1>
            <h2 class="text-xl font-semibold mb-6 text-center text-gray-600">Iniciar Sesión</h2>

            <%-- Mostrar mensaje de error si existe --%>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4 text-sm" role="alert">
                    <span class="block sm:inline">${requestScope.error}</span>
                </div>
            </c:if>
            <%-- Mostrar mensaje de logout si existe --%>
            <c:if test="${not empty requestScope.mensajeLogout}">
                <div class="bg-blue-100 border border-blue-400 text-blue-700 px-4 py-3 rounded relative mb-4 text-sm" role="alert">
                    <span class="block sm:inline">${requestScope.mensajeLogout}</span>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="post">
                <div class="mb-4">
                    <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                    <input type="email" id="email" name="email" required autofocus
                           class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                           placeholder="tu@email.com">
                </div>

                <div class="mb-6">
                    <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
                    <input type="password" id="password" name="password" required
                           class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                           placeholder="********">
                </div>

                <div>
                    <button type="submit"
                            class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-gradient-to-r from-blue-600 to-purple-700 hover:from-blue-700 hover:to-purple-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-150 ease-in-out">
                        Entrar
                    </button>
                </div>
            </form>
            <p class="mt-4 text-center text-xs text-gray-500">
                ¿Aún no tienes cuenta de promotor? Contacta con el administrador.
            </p>
        </div>

    </body>
</html>
