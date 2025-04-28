<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %> <%-- Importar fmt por si se usa en el futuro --%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <c:set var="esNuevo" value="${requestScope.esNuevo}"/>
        <title>${esNuevo ? 'Crear Nuevo Usuario' : 'Editar Usuario'} - Beatpass Admin</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <%-- Enlace al CSS centralizado --%>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-2xl">

            <%-- Incluir Menú Admin Común (activePage dependerá de dónde venga) --%>
            <%-- Podríamos pasar el rol como parámetro para determinar la página activa --%>
            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="${not empty usuario.rol ? (usuario.rol == 'ADMIN' ? 'admins' : (usuario.rol == 'PROMOTOR' ? 'promotores' : 'cajeros')) : 'promotores'}"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">
                ${esNuevo ? 'Crear Nuevo Usuario' : 'Editar Usuario'}
                <c:if test="${not esNuevo and not empty usuario.nombre}">: <span class="font-bold text-gray-800">${usuario.nombre}</span></c:if>
                </h2>

                <div class="mb-4">
                <%-- Enlace Volver dinámico según el rol (si estamos editando) o a promotores por defecto --%>
                <c:set var="listaUrl">
                    <c:choose>
                        <c:when test="${not esNuevo and usuario.rol == 'ADMIN'}">${pageContext.request.contextPath}/api/admin/admins/listar</c:when>
                        <c:when test="${not esNuevo and usuario.rol == 'PROMOTOR'}">${pageContext.request.contextPath}/api/admin/promotores/listar</c:when>
                        <c:when test="${not esNuevo and usuario.rol == 'CAJERO'}">${pageContext.request.contextPath}/api/admin/cajeros/listar</c:when>
                        <c:otherwise>${pageContext.request.contextPath}/api/admin/promotores/listar</c:otherwise> 
                    </c:choose>
                </c:set>
                <a href="${listaUrl}" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a la lista</a>
            </div>

            <%-- Mensajes de error --%>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error</p>
                    <p>${requestScope.error}</p>
                </div>
            </c:if>

            <%-- Formulario Generalizado --%>
            <form action="${pageContext.request.contextPath}/api/admin/usuarios/guardar" method="post" class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4">
                <%-- Campo oculto para ID en modo edición --%>
                <c:if test="${not esNuevo}">
                    <input type="hidden" name="idUsuario" value="${usuario.idUsuario}">
                </c:if>

                <div>
                    <label for="nombre" class="block text-sm font-medium text-gray-700 mb-1">
                        Nombre <span class="text-red-500 ml-1">*</span>
                    </label>
                    <input type="text" id="nombre" name="nombre" value="${usuario.nombre}" required maxlength="100"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                </div>

                <div class="mt-4">
                    <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
                        Email <span class="text-red-500 ml-1">*</span>
                    </label>
                    <input type="email" id="email" name="email" value="${usuario.email}" required maxlength="100"
                           class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm placeholder-gray-400 ${not esNuevo ? 'bg-gray-100 cursor-not-allowed' : ''}"
                           placeholder="usuario@ejemplo.com" ${not esNuevo ? 'readonly' : ''} >
                    <c:if test="${not esNuevo}">
                        <p class="mt-1 text-xs text-gray-500">El email no se puede modificar una vez creado.</p>
                    </c:if>
                </div>

                <%-- Campos solo para creación --%>
                <c:if test="${esNuevo}">
                    <div class="mt-4">
                        <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
                            Contraseña Inicial <span class="text-red-500 ml-1">*</span>
                        </label>
                        <input type="password" id="password" name="password" required minlength="8"
                               class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                        <p class="mt-1 text-xs text-gray-500">Mínimo 8 caracteres. El usuario deberá cambiarla en su primer inicio de sesión.</p>
                    </div>
                    <div class="mt-4">
                        <label for="rol" class="block text-sm font-medium text-gray-700 mb-1">
                            Rol del Usuario <span class="text-red-500 ml-1">*</span>
                        </label>
                        <select id="rol" name="rol" required
                                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                            <option value="">-- Selecciona un Rol --</option>
                            <c:forEach var="rol" items="${rolesPosibles}">
                                <option value="${rol.name()}" ${usuario.rol == rol.name() ? 'selected' : ''}>${rol.name()}</option>
                            </c:forEach>
                        </select>
                    </div>
                </c:if>

                <%-- Campos solo para edición --%>
                <c:if test="${not esNuevo}">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Rol Actual</label>
                        <p class="readonly-field">${usuario.rol}</p>
                        <p class="mt-1 text-xs text-gray-500">El rol no se puede modificar una vez creado.</p>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Estado Actual</label>
                        <p class="readonly-field ${usuario.estado ? 'text-green-700 bg-green-50 border-green-200' : 'text-red-700 bg-red-50 border-red-200'}">
                            ${usuario.estado ? 'Activo' : 'Inactivo'}
                        </p>
                        <p class="mt-1 text-xs text-gray-500">El estado se activa/desactiva desde la lista de usuarios.</p>
                    </div>
                </c:if>

                <div class="mt-6 flex justify-end space-x-3 pt-4 border-t border-gray-200">
                    <a href="${listaUrl}" class="btn btn-secondary">
                        Cancelar
                    </a>
                    <button type="submit" class="btn btn-primary">
                        ${esNuevo ? 'Crear Usuario' : 'Guardar Cambios'}
                    </button>
                </div>
            </form>
        </div>
    </body>
</html>
