<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<c:set var="esNuevo" value="${empty festival.idFestival or festival.idFestival == 0}"/>
<c:set var="pageTitleVar" value="${esNuevo ? 'Crear Nuevo Festival' : 'Editar Festival'}" />

<jsp:include page="/WEB-INF/jsp/promotor/_header.jsp">
    <jsp:param name="pageTitle" value="${pageTitleVar}" />
    <jsp:param name="currentNav" value="${esNuevo ? 'crearFestival' : 'editarFestival'}" />
</jsp:include>

<div class="container mx-auto p-4 md:p-8 max-w-4xl">

    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
        <h1 class="text-2xl md:text-3xl font-bold text-gray-800 mb-4 sm:mb-0">
            ${esNuevo ? 'Crear Nuevo Festival' : 'Editar Festival'}
        </h1>
    </div>

    <h2 class="text-xl font-semibold text-gray-700 mb-5">
        ${esNuevo ? 'Introduce los datos del nuevo festival' : 'Modifica los datos de:'}
        <c:if test="${not esNuevo}"><span class="font-bold text-indigo-700"> ${festival.nombre}</span></c:if>
        </h2>

        <div class="mb-4">
        <c:choose>
            <c:when test="${esNuevo}">
                <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Mis Festivales</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Detalles del Festival</a>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- Mensajes de error --%>
    <c:if test="${not empty requestScope.error and empty requestScope.errorEntrada}">
        <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
            <p class="font-bold">Error al guardar festival:</p>
            <p>${requestScope.error}</p>
        </div>
    </c:if>
    <c:if test="${not empty requestScope.error and not empty requestScope.errorEntrada}">
        <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
            <p class="font-bold">Error al añadir tipo de entrada:</p>
            <p>${requestScope.error}</p>
        </div>
    </c:if>

    <%-- Formulario Datos Generales Festival --%>
    <form action="${pageContext.request.contextPath}/api/promotor/festivales/guardar" method="post" class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4 mb-10">
        <input type="hidden" name="idFestival" value="${festival.idFestival}">
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
        <div class="mt-6 flex justify-end space-x-3 pt-4 border-t border-gray-200">
            <c:choose>
                <c:when test="${esNuevo}">
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="btn btn-secondary"> Cancelar </a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="btn btn-secondary"> Cancelar </a>
                </c:otherwise>
            </c:choose>
            <button type="submit" class="btn btn-primary">
                ${esNuevo ? 'Crear Solicitud de Festival' : 'Guardar Cambios Festival'}
            </button>
        </div>
    </form>

    <%-- Sección Añadir Nuevo Tipo de Entrada --%>
    <c:if test="${not esNuevo}">
        <div class="mt-10 pt-6 border-t border-gray-300">
            <h3 class="text-xl font-semibold mb-4 text-gray-700">Añadir Nuevo Tipo de Entrada</h3>
            <c:if test="${not empty requestScope.error and not empty requestScope.errorEntrada}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error al añadir tipo de entrada:</p>
                    <p>${requestScope.error}</p>
                </div>
            </c:if>
            <div class="bg-white p-4 md:p-6 rounded-lg shadow-md">
                <form action="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/tiposEntrada" method="post" class="space-y-3">
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div>
                            <label for="tipoEntrada" class="block text-sm font-medium text-gray-700 mb-1">Tipo <span class="text-red-500 ml-1">*</span></label>
                            <input type="text" id="tipoEntrada" name="tipo" value="${nuevaEntrada.tipo}" required maxlength="50" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="Ej: General">
                        </div>
                        <div>
                            <label for="precioEntrada" class="block text-sm font-medium text-gray-700 mb-1">Precio (€) <span class="text-red-500 ml-1">*</span></label>
                            <input type="number" id="precioEntrada" name="precio" value="${nuevaEntrada.precio}" required min="0" step="0.01" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="Ej: 25.50">
                        </div>
                        <div>
                            <label for="stockEntrada" class="block text-sm font-medium text-gray-700 mb-1">Stock <span class="text-red-500 ml-1">*</span></label>
                            <input type="number" id="stockEntrada" name="stock" value="${nuevaEntrada.stock}" required min="0" step="1" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="Ej: 1000">
                        </div>
                    </div>
                    <div>
                        <label for="descEntrada" class="block text-sm font-medium text-gray-700 mb-1">Descripción (Opcional)</label>
                        <textarea id="descEntrada" name="descripcion" rows="2" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="Detalles sobre este tipo de entrada...">${nuevaEntrada.descripcion}</textarea>
                    </div>
                    <div class="flex justify-end pt-2">
                        <button type="submit" class="btn btn-success text-sm"> Añadir Tipo de Entrada </button>
                    </div>
                </form>
            </div>
        </div>
    </c:if>
</div>
