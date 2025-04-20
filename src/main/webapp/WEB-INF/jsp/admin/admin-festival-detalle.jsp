<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %> <%-- Usar jakarta.tags.core para JSTL 3.0 --%>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %> <%-- Usar jakarta.tags.fmt para JSTL 3.0 --%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Crear Nuevo Festival - Beatpass Admin</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <style> body {
            font-family: 'Inter', sans-serif;
        } </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8">
            <%-- Incluir menú admin si existe --%>
            <%-- <jsp:include page="../_admin_menu.jsp" /> --%>

            <h1 class="text-3xl font-bold mb-6 text-purple-700">Crear Nuevo Festival</h1>

            <%-- Mostrar mensajes de error si existen --%>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span class="block sm:inline">${requestScope.error}</span>
                </div>
            </c:if>

            <%-- El action apunta al endpoint de guardar festival del AdminResource --%>
            <form action="${pageContext.request.contextPath}/api/admin/festivales/guardar" method="post" class="bg-white p-6 rounded-lg shadow-md max-w-2xl mx-auto">
                <%-- No necesitamos ID aquí porque es solo para crear --%>

                <%-- Selector de Promotor --%>
                <div class="mb-4">
                    <label for="idPromotorSeleccionado" class="block text-sm font-medium text-gray-700">Asignar a Promotor <span class="text-red-500">*</span></label>
                    <select id="idPromotorSeleccionado" name="idPromotorSeleccionado" required
                            class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                        <option value="">-- Selecciona un Promotor --</option>
                        <c:forEach var="promotor" items="${promotores}"> <%-- Usar la lista pasada desde AdminResource --%>
                            <option value="${promotor.idUsuario}" ${promotor.idUsuario == festival.idPromotor ? 'selected' : ''}>
                                ${promotor.nombre} (ID: ${promotor.idUsuario})
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty promotores}">
                        <p class="text-red-600 text-xs mt-1">No hay promotores activos disponibles para asignar.</p>
                    </c:if>
                </div>

                <%-- Campos del Festival --%>
                <div class="mb-4">
                    <label for="nombre" class="block text-sm font-medium text-gray-700">Nombre del Festival <span class="text-red-500">*</span></label>
                    <input type="text" id="nombre" name="nombre" value="${festival.nombre}" required
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                </div>

                <div class="mb-4">
                    <label for="descripcion" class="block text-sm font-medium text-gray-700">Descripción</label>
                    <textarea id="descripcion" name="descripcion" rows="4"
                              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">${festival.descripcion}</textarea>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
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

                <div class="mb-4">
                    <label for="ubicacion" class="block text-sm font-medium text-gray-700">Ubicación</label>
                    <input type="text" id="ubicacion" name="ubicacion" value="${festival.ubicacion}"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm">
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
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

                <%-- El estado se asigna por defecto en el backend al crear --%>

                <div class="mt-6 flex justify-end space-x-3">
                    <%-- Enlace Cancelar apunta a la lista de promotores --%>
                    <a href="${pageContext.request.contextPath}/api/admin/promotores/listar"
                       class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-2 px-4 rounded shadow">
                        Cancelar
                    </a>
                    <button type="submit"
                            class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded shadow">
                        Crear Festival
                    </button>
                </div>

            </form>
        </div>

    </body>
</html>
