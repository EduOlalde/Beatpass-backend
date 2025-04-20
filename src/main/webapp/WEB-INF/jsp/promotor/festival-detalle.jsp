<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <%-- Determinar si es creación o edición para el título --%>
    <c:set var="esNuevo" value="${empty festival.idFestival or festival.idFestival == 0}"/>
    <title>${esNuevo ? 'Crear Nuevo' : 'Editar'} Festival - Beatpass Promotor</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        body {
            font-family: 'Inter', sans-serif;
        }
        label {
            margin-top: 0.5rem;
            display: block;
        }
        input[type=text], input[type=date], input[type=number], textarea, select {
            width: 100%;
            padding: 0.5rem;
            border: 1px solid #ccc;
            border-radius: 0.25rem;
            margin-top: 0.25rem;
        }
        .error-message {
            color: red;
            font-size: 0.875rem;
            margin-top: 0.25rem;
        }
    </style>
</head>
<body class="bg-gray-100 text-gray-800">

    <div class="container mx-auto p-4 md:p-8">
        <h1 class="text-3xl font-bold mb-6 text-blue-700">
            ${esNuevo ? 'Crear Nuevo Festival' : 'Editar Festival: '}
            <c:if test="${not esNuevo}"><span class="text-gray-700">${festival.nombre}</span></c:if>
        </h1>

        <%-- Mostrar mensajes de error si existen --%>
        <c:if test="${not empty requestScope.error}">
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                <span class="block sm:inline">${requestScope.error}</span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/promotor/festivales/" method="post" class="bg-white p-6 rounded-lg shadow-md">
            <%-- Campo oculto para indicar la acción al servlet --%>
            <input type="hidden" name="action" value="guardar">
            <%-- Campo oculto para el ID si estamos editando --%>
            <input type="hidden" name="idFestival" value="${festival.idFestival}">

            <div>
                <label for="nombre" class="block text-sm font-medium text-gray-700">Nombre del Festival <span class="text-red-500">*</span></label>
                <input type="text" id="nombre" name="nombre" value="${festival.nombre}" required
                       class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
            </div>

            <div class="mt-4">
                <label for="descripcion" class="block text-sm font-medium text-gray-700">Descripción</label>
                <textarea id="descripcion" name="descripcion" rows="4"
                          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">${festival.descripcion}</textarea>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                <div>
                    <label for="fechaInicio" class="block text-sm font-medium text-gray-700">Fecha de Inicio <span class="text-red-500">*</span></label>
                    <input type="date" id="fechaInicio" name="fechaInicio" value="${festival.fechaInicio}" required
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                </div>
                <div>
                    <label for="fechaFin" class="block text-sm font-medium text-gray-700">Fecha de Fin <span class="text-red-500">*</span></label>
                    <input type="date" id="fechaFin" name="fechaFin" value="${festival.fechaFin}" required
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                </div>
            </div>

            <div class="mt-4">
                <label for="ubicacion" class="block text-sm font-medium text-gray-700">Ubicación</label>
                <input type="text" id="ubicacion" name="ubicacion" value="${festival.ubicacion}"
                       class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                <div>
                    <label for="aforo" class="block text-sm font-medium text-gray-700">Aforo (Opcional)</label>
                    <input type="number" id="aforo" name="aforo" value="${festival.aforo}" min="0"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                </div>
                <div>
                    <label for="imagenUrl" class="block text-sm font-medium text-gray-700">URL de la Imagen (Opcional)</label>
                    <input type="text" id="imagenUrl" name="imagenUrl" value="${festival.imagenUrl}" placeholder="https://ejemplo.com/imagen.jpg"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                </div>
            </div>

            <c:if test="${not esNuevo}"> <%-- Solo mostrar opción de estado si se está editando --%>
                <div class="mt-4">
                    <label for="estado" class="block text-sm font-medium text-gray-700">Estado</label>
                    <select id="estado" name="estado"
                            class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                        <c:forEach var="e" items="${estadosPosibles}">
                            <option value="${e}" ${festival.estado == e ? 'selected' : ''}>${e}</option>
                        </c:forEach>
                    </select>
                </div>
            </c:if>

            <div class="mt-6 flex justify-end space-x-3">
                <a href="${pageContext.request.contextPath}/promotor/festivales/listar"
                   class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-2 px-4 rounded shadow">
                    Cancelar
                </a>
                <button type="submit"
                        class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded shadow">
                    ${esNuevo ? 'Crear Festival' : 'Guardar Cambios'}
                </button>
            </div>

        </form>

        <%-- Sección para gestionar Tipos de Entrada (a implementar) --%>
        <c:if test="${not esNuevo}">
            <div class="mt-10 pt-6 border-t border-gray-300">
                <h2 class="text-2xl font-bold mb-4 text-gray-700">Tipos de Entrada</h2>
                <p class="text-gray-500">Aquí se mostraría la lista de tipos de entrada para este festival y opciones para añadir/editar/eliminar.</p>
                <%-- TODO: Implementar lógica para mostrar y gestionar tipos de entrada --%>
                <%-- <a href="#" class="bg-green-500 hover:bg-green-600 text-white font-bold py-1 px-3 rounded text-sm shadow">Añadir Tipo Entrada</a> --%>
                <%-- <table class="min-w-full ..."> ... </table> --%>
            </div>
        </c:if>

    </div>

</body>
</html>
