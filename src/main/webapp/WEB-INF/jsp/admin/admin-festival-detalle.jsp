<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <c:set var="esNuevo" value="${requestScope.esNuevo}"/>
        <title>${esNuevo ? 'Crear Nuevo' : 'Editar'} Festival - Beatpass Admin</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-4xl">

            <%-- Incluir Menú Admin Común --%>
            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="festivales"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">
                ${esNuevo ? 'Crear Nuevo Festival (Admin)' : 'Editar Festival (Admin): '}
                <c:if test="${not esNuevo}"><span class="font-bold text-gray-800"> ${festival.nombre}</span></c:if>
                </h2>

                <div class="mb-4">
                    <a href="${pageContext.request.contextPath}/api/admin/festivales/listar-todos" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Gestionar Festivales</a>
            </div>

            <%-- Mensajes de error (se mantienen clases de Tailwind) --%>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error</p>
                    <p>${requestScope.error}</p>
                </div>
            </c:if>

            <%-- Formulario (botones con clases CSS externas) --%>
            <form action="${pageContext.request.contextPath}/api/admin/festivales/guardar" method="post" class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4">
                <input type="hidden" name="idFestival" value="${festival.idFestival}">
                <c:if test="${esNuevo}">
                    <div>
                        <label for="idPromotorSeleccionado" class="block text-sm font-medium text-gray-700 mb-1">
                            Asignar a Promotor <span class="text-red-500 ml-1">*</span>
                        </label>
                        <select id="idPromotorSeleccionado" name="idPromotorSeleccionado" required
                                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                            <option value="">-- Selecciona un Promotor --</option>
                            <c:forEach var="p" items="${promotores}">
                                <option value="${p.idUsuario}">${p.nombre} (ID: ${p.idUsuario})</option>
                            </c:forEach>
                        </select>
                        <c:if test="${empty promotores}">
                            <p class="text-xs text-red-600 mt-1">No hay promotores activos disponibles.</p>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${not esNuevo}">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Promotor Asignado</label>
                        <%-- Usamos readonly-field para consistencia, aunque no estaba definido en el style original de este JSP --%>
                        <p class="readonly-field">
                            ${festival.nombrePromotor} (ID: ${festival.idPromotor})
                        </p>
                    </div>
                </c:if>
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
                <c:if test="${not esNuevo}">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Estado Actual</label>
                        <%-- Usamos readonly-field para consistencia --%>
                        <p class="readonly-field">
                            ${festival.estado}
                        </p>
                        <p class="text-xs text-gray-500 mt-1">El estado se cambia desde la lista de festivales.</p>
                    </div>
                </c:if>
                <div class="mt-6 flex justify-end space-x-3 pt-4 border-t border-gray-200">
                    <a href="${pageContext.request.contextPath}/api/admin/festivales/listar-todos"
                       class="btn btn-secondary"> <%-- Clase CSS externa --%>
                        Cancelar
                    </a>
                    <button type="submit"
                            class="btn btn-primary"> <%-- Clase CSS externa --%>
                        ${esNuevo ? 'Crear Festival' : 'Guardar Cambios'}
                    </button>
                </div>
            </form>

            <c:if test="${not esNuevo}">
                <div class="mt-10 pt-6 border-t border-gray-300">
                    <h3 class="text-xl font-semibold mb-4 text-gray-700">Tipos de Entrada (Admin View)</h3>
                    <div class="bg-white p-6 rounded-lg shadow-md">
                        <p class="text-gray-600 mb-4">Vista de administrador para los tipos de entrada de este festival.</p>
                        <p class="text-sm text-gray-500 italic">(Funcionalidad pendiente)</p>
                        <%-- Aquí iría la tabla o gestión de tipos de entrada si se implementa --%>
                    </div>
                </div>
            </c:if>
        </div>
    </body>
</html>
