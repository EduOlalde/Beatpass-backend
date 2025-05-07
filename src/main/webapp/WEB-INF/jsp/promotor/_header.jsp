<%-- 
    Document   : _header
    Created on : 7 may 2025, 21:23:46
    Author     : Eduardo Olalde
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %> 



<%--
  Fragmento de cabecera común para las páginas de promotor.
  Parámetros esperados (pasados vía <jsp:param>):
  - pageTitle (String): El título para la etiqueta <title> de la página.
  - currentNav (String, opcional): Identificador de la sección actual para estilizar la navegación.
    Valores posibles: 'misFestivales', 'crearFestival', 'detalleFestival', 'editarFestival', 'gestionEntradas', etc.
--%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><c:out value="${param.pageTitle}"/> - Beatpass Promotor</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <%-- Cabecera global de la aplicación --%>
        <header class="bg-white shadow-md sticky top-0 z-50">
            <div class="container mx-auto px-4 md:px-8 py-3 flex flex-col sm:flex-row justify-between items-center">
                <%-- Logo o Nombre de la Aplicación --%>
                <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-2xl font-bold text-indigo-700 hover:text-indigo-800 transition-colors duration-150 ease-in-out">
                    Beatpass Promotor
                </a>

                <%-- Navegación y Acciones de Usuario --%>
                <nav class="flex items-center space-x-2 sm:space-x-4 mt-3 sm:mt-0">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600 hidden md:inline">Hola, <c:out value="${sessionScope.userName}"/></span>
                    </c:if>

                    <%-- Enlace a Mis Festivales --%>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales"
                       class="text-sm font-medium py-2 px-2 sm:px-3 rounded-md transition-colors duration-150 ease-in-out
                       ${param.currentNav == 'misFestivales' || fn:startsWith(param.currentNav, 'detalleFestival') || fn:startsWith(param.currentNav, 'editarFestival') || fn:startsWith(param.currentNav, 'crearFestival')
                         ? 'bg-indigo-100 text-indigo-700'
                         : 'text-gray-600 hover:bg-indigo-50 hover:text-indigo-700'}">
                        Mis Festivales
                    </a>

                    <%-- Botón Crear Nuevo Festival --%>
                    <c:if test="${param.currentNav == 'misFestivales'}">
                        <a href="${pageContext.request.contextPath}/api/promotor/festivales/crear"
                           class="btn btn-primary btn-sm text-xs sm:text-sm">
                            + Crear Festival
                        </a>
                    </c:if>

                    <%-- Formulario de Cerrar Sesión --%>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline py-2 px-2 sm:px-3 rounded-md hover:bg-red-50 transition-colors duration-150 ease-in-out">Cerrar Sesión</button>
                    </form>
                </nav>
            </div>
        </header>
