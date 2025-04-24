<%-- 
    Document   : promotor-festival-editar
    Created on : 24 abr 2025, 6:54:51
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
        <c:set var="esNuevo" value="${empty festival.idFestival or festival.idFestival == 0}"/>
        <title>${esNuevo ? 'Crear Nuevo' : 'Editar'} Festival - Beatpass Promotor</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            body { font-family: 'Inter', sans-serif; }
            textarea { min-height: 8rem; }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-4xl">

            <%-- Cabecera --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">
                    ${esNuevo ? 'Crear Nuevo Festival' : 'Editar Festival'}
                </h1>
                 <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">
                ${esNuevo ? 'Introduce los datos del nuevo festival' : 'Modifica los datos de:'}
                <c:if test="${not esNuevo}"><span class="font-bold text-gray-800"> ${festival.nombre}</span></c:if>
            </h2>

            <div class="mb-4">
                <%-- El enlace de volver ahora apunta a la vista de detalles si estamos editando, o a la lista si estamos creando --%>
                <c:choose>
                    <c:when test="${esNuevo}">
                         <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Mis Festivales</a>
                    </c:when>
                    <c:otherwise>
                         <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Detalles del Festival</a>
                    </c:otherwise>
                </c:choose>
            </div>

            <%-- Mensajes de error (si guardar falla) --%>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error al guardar festival:</p>
                    <p>${requestScope.error}</p>
                </div>
            </c:if>


            <%-- Formulario Datos Generales Festival --%>
            <form action="${pageContext.request.contextPath}/api/promotor/festivales/guardar" method="post" class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4 mb-10">
                <input type="hidden" name="idFestival" value="${festival.idFestival}"> <%-- Se envía 0 o vacío si es nuevo --%>
                <h3 class="text-lg font-semibold text-gray-600 border-b pb-2 mb-4">Datos Generales del Festival</h3>
                <div>
                    <label for="nombre" class="block text-sm font-medium text-gray-700 mb-1">
                        Nombre del Festival <span class="text-red-500 ml-1">*</span>
                    </label>
                    <input type="text" id="nombre" name="nombre" value="${festival.nombre}" required maxlength="100"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                </div>
                <div>
                    <label for="descripcion" class="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                    <textarea id="descripcion" name="descripcion" rows="4"
                              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">${festival.descripcion}</textarea>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label for="fechaInicio" class="block text-sm font-medium text-gray-700 mb-1">
                            Fecha de Inicio <span class="text-red-500 ml-1">*</span>
                        </label>
                        <input type="date" id="fechaInicio" name="fechaInicio" value="${festival.fechaInicio}" required
                               class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                    </div>
                    <div>
                        <label for="fechaFin" class="block text-sm font-medium text-gray-700 mb-1">
                            Fecha de Fin <span class="text-red-500 ml-1">*</span>
                        </label>
                        <input type="date" id="fechaFin" name="fechaFin" value="${festival.fechaFin}" required
                               class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                    </div>
                </div>
                <div>
                    <label for="ubicacion" class="block text-sm font-medium text-gray-700 mb-1">Ubicación</label>
                    <input type="text" id="ubicacion" name="ubicacion" value="${festival.ubicacion}" maxlength="255"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label for="aforo" class="block text-sm font-medium text-gray-700 mb-1">Aforo (Opcional)</label>
                        <input type="number" id="aforo" name="aforo" value="${festival.aforo}" min="1" placeholder="Ej: 5000"
                               class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                    </div>
                    <div>
                        <label for="imagenUrl" class="block text-sm font-medium text-gray-700 mb-1">URL de la Imagen (Opcional)</label>
                        <input type="text" id="imagenUrl" name="imagenUrl" value="${festival.imagenUrl}" placeholder="https://ejemplo.com/imagen.jpg" maxlength="255"
                               class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                    </div>
                </div>
                <%-- El estado no se edita aquí --%>
                <div class="mt-6 flex justify-end space-x-3 pt-4 border-t border-gray-200">
                     <%-- Botón Cancelar: vuelve a la vista de detalles si edita, o a la lista si crea --%>
                     <c:choose>
                         <c:when test="${esNuevo}">
                              <a href="${pageContext.request.contextPath}/api/promotor/festivales"
                                 class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-gray-200 hover:bg-gray-300 text-gray-800"> Cancelar </a>
                         </c:when>
                         <c:otherwise>
                              <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}"
                                 class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-gray-200 hover:bg-gray-300 text-gray-800"> Cancelar </a>
                         </c:otherwise>
                     </c:choose>
                    <button type="submit" class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-indigo-600 hover:bg-indigo-700 text-white">
                        ${esNuevo ? 'Crear Solicitud de Festival' : 'Guardar Cambios Festival'}
                    </button>
                </div>
            </form>

        </div> <%-- Fin container --%>

    </body>
</html>
