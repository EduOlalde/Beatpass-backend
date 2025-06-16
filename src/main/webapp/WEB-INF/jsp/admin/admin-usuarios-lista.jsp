<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<%-- 
    Vista genérica para listar usuarios del sistema (ADMIN, PROMOTOR, CAJERO).
    Recibe los siguientes atributos del request:
    - tituloPagina: El título a mostrar en la página (ej: "Gestionar Promotores").
    - activePage: El identificador para el menú de navegación (ej: "promotores").
    - usuarios: La lista de objetos UsuarioDTO a mostrar.
    - rolListado: El nombre del rol en mayúsculas (ej: "PROMOTOR") para lógica condicional.
--%>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${tituloPagina} - Beatpass Admin</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="${activePage}"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">${tituloPagina}</h2>

            <div class="flex justify-end mb-4 space-x-3">
                <a href="${pageContext.request.contextPath}/api/admin/usuarios/crear" class="btn btn-primary">
                    + Añadir Nuevo Usuario
                </a>
            </div>

            <jsp:include page="/WEB-INF/jsp/admin/_admin_messages.jsp" />

            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>

                            <%-- Columna condicional solo para la vista de Promotores --%>
                            <c:if test="${rolListado == 'PROMOTOR'}">
                                <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Festivales</th>
                                </c:if>

                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty usuarios}">
                                <tr>
                                    <td colspan="${rolListado == 'PROMOTOR' ? 6 : 5}" class="px-6 py-4 text-center text-sm text-gray-500 italic">
                                        No hay ${fn:toLowerCase(rolListado)}s registrados.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="u" items="${usuarios}">
                                    <tr>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${u.idUsuario}</td>
                                        <td class="px-6 py-4 whitespace-nowrap">
                                            <div class="text-sm font-medium text-gray-900">${u.nombre}</div>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${u.email}</td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center">
                                            <span class="badge ${u.estado ? 'badge-activa' : 'badge-inactiva'}">${u.estado ? 'Activo' : 'Inactivo'}</span>
                                        </td>

                                        <c:if test="${rolListado == 'PROMOTOR'}">
                                            <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium">
                                                <a href="${pageContext.request.contextPath}/api/admin/promotores/${u.idUsuario}/festivales" class="action-link action-link-view" title="Ver festivales de ${u.nombre}">
                                                    Ver Festivales
                                                </a>
                                            </td>
                                        </c:if>

                                        <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-2">
                                            <a href="${pageContext.request.contextPath}/api/admin/usuarios/${u.idUsuario}/editar" class="action-link action-link-edit" title="Editar datos de ${u.nombre}">Editar</a>

                                            <c:if test="${u.idUsuario != sessionScope.userId}">
                                                <form action="${pageContext.request.contextPath}/api/admin/usuarios/cambiar-estado" method="post" class="inline">
                                                    <input type="hidden" name="idUsuario" value="${u.idUsuario}">
                                                    <input type="hidden" name="nuevoEstado" value="${!u.estado}">
                                                    <button type="submit" class="action-button ${u.estado ? 'action-button-deactivate' : 'action-button-activate'}" title="${u.estado ? 'Desactivar' : 'Activar'}">
                                                        ${u.estado ? 'Desactivar' : 'Activar'}
                                                    </button>
                                                </form>
                                            </c:if>
                                            <c:if test="${u.idUsuario == sessionScope.userId}">
                                                <span class="text-xs text-gray-400 italic">(Tú mismo)</span>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>