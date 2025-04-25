<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestionar Promotores - Beatpass Admin</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            /* Estilos generales */
            body {
                font-family: 'Inter', sans-serif;
            }
            /* Clases base para botones */
            .btn {
                font-weight: bold; /* font-bold */
                padding-top: 0.5rem; /* py-2 */
                padding-bottom: 0.5rem; /* py-2 */
                padding-left: 1rem; /* px-4 */
                padding-right: 1rem; /* px-4 */
                border-radius: 0.375rem; /* rounded */
                box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); /* shadow */
                transition-property: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform; /* transition */
                transition-duration: 150ms; /* duration-150 */
                transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1); /* ease-in-out */
                display: inline-flex; /* inline-flex */
                align-items: center; /* items-center */
                font-size: 0.875rem; /* text-sm */
            }
            .btn-primary {
                background-color: #8B5CF6; /* bg-purple-600 */
                color: white; /* text-white */
            }
            .btn-primary:hover {
                background-color: #7C3AED; /* hover:bg-purple-700 */
            }
            /* Estilos para badges de estado */
            .badge {
                padding: 0.1em 0.6em;
                border-radius: 9999px;
                font-size: 0.75rem; /* text-xs */
                font-weight: 600; /* font-semibold */
                display: inline-flex;
                align-items: center;
            }
            .badge-activo {
                background-color: #D1FAE5; /* Tailwind green-100 */
                color: #065F46; /* Tailwind green-800 */
            }
            .badge-inactivo {
                background-color: #FEE2E2; /* Tailwind red-100 */
                color: #991B1B; /* Tailwind red-800 */
            }
            /* Estilos para acciones en tablas */
            .action-link {
                text-decoration: underline;
                font-size: 0.75rem; /* text-xs */
            }
            .action-link-view {
                color: #4F46E5; /* text-indigo-600 */
                font-weight: 500; /* font-medium */
            }
            .action-link-view:hover {
                color: #3730A3; /* hover:text-indigo-900 */
            }
            .action-link-edit {
                color: #D97706; /* text-yellow-600 */
                font-weight: 600; /* font-semibold */
            }
            .action-link-edit:hover {
                color: #92400E; /* hover:text-yellow-900 */
            }
            .action-button { /* Para botones dentro de formularios en la tabla */
                background: none;
                border: none;
                padding: 0;
                cursor: pointer;
                text-decoration: underline;
                font-size: 0.75rem; /* text-xs */
                font-weight: 600; /* font-semibold */
            }
            .action-button-activate {
                color: #059669; /* text-green-600 */
            }
            .action-button-activate:hover {
                color: #047857; /* hover:text-green-900 */
            }
            .action-button-deactivate {
                color: #DC2626; /* text-red-600 */
            }
            .action-button-deactivate:hover {
                color: #991B1B; /* hover:text-red-900 */
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Incluir Menú Admin Común --%>
            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="promotores"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">Gestionar Promotores</h2>

            <%-- Botón Añadir Promotor (Estilo Homogeneizado) --%>
            <div class="flex justify-end mb-4 space-x-3">
                <a href="${pageContext.request.contextPath}/api/admin/promotores/crear"
                   class="btn btn-primary">
                    + Añadir Nuevo Promotor
                </a>
            </div>

            <%-- Mensajes flash --%>
            <c:if test="${not empty requestScope.mensajeExito}">
                <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Éxito</p> <p>${requestScope.mensajeExito}</p>
                </div>
            </c:if>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error</p> <p>${requestScope.error}</p>
                </div>
            </c:if>

            <%-- Tabla de Promotores --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Festivales</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty promotores}">
                                <tr> <td colspan="6" class="px-6 py-4 text-center text-sm text-gray-500 italic">No hay promotores registrados.</td> </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="p" items="${promotores}">
                                    <tr>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${p.idUsuario}</td>
                                        <td class="px-6 py-4 whitespace-nowrap">
                                            <div class="text-sm font-medium text-gray-900">${p.nombre}</div>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${p.email}</td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center">
                                            <span class="badge ${p.estado ? 'badge-activo' : 'badge-inactivo'}">
                                                ${p.estado ? 'Activo' : 'Inactivo'}
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium">
                                            <%-- Enlace Ver Festivales (Estilo Homogeneizado) --%>
                                            <a href="${pageContext.request.contextPath}/api/admin/promotores/${p.idUsuario}/festivales" class="action-link action-link-view" title="Ver festivales de ${p.nombre}">
                                                Ver Festivales
                                            </a>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-2">
                                            <%-- Enlace Editar (Estilo Homogeneizado) --%>
                                            <a href="${pageContext.request.contextPath}/api/admin/promotores/${p.idUsuario}/editar" class="action-link action-link-edit" title="Editar datos de ${p.nombre}">Editar</a>
                                            <%-- Formulario para activar/desactivar (Estilo Homogeneizado) --%>
                                            <form action="${pageContext.request.contextPath}/api/admin/promotores/cambiar-estado" method="post" class="inline">
                                                <input type="hidden" name="idPromotor" value="${p.idUsuario}">
                                                <input type="hidden" name="nuevoEstado" value="${!p.estado}">
                                                <button type="submit" class="action-button ${p.estado ? 'action-button-deactivate' : 'action-button-activate'}" title="${p.estado ? 'Desactivar cuenta' : 'Activar cuenta'}">
                                                    ${p.estado ? 'Desactivar' : 'Activar'}
                                                </button>
                                            </form>
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
