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
            body {
                font-family: 'Inter', sans-serif;
            }
            .badge {
                padding: 0.1em 0.6em;
                border-radius: 9999px;
                font-size: 0.75rem;
                font-weight: 600;
                display: inline-flex;
                align-items: center;
            }
            .badge-activo {
                background-color: #D1FAE5;
                color: #065F46;
            }
            .badge-inactivo {
                background-color: #FEE2E2;
                color: #991B1B;
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

            <div class="flex justify-end mb-4 space-x-3">
                <a href="${pageContext.request.contextPath}/api/admin/promotores/crear"
                   class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out bg-purple-600 hover:bg-purple-700 text-white">
                    + Añadir Nuevo Promotor
                </a>
                <a href="${pageContext.request.contextPath}/api/admin/festivales/crear"
                   class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out bg-green-600 hover:bg-green-700 text-white">
                    + Crear Festival (Asignar)
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
                <%-- ... (contenido de la tabla sin cambios) ... --%>
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
                                <tr> <td colspan="6" class="px-6 py-4 text-center text-sm text-gray-500">No hay promotores registrados.</td> </tr>
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
                                            <a href="${pageContext.request.contextPath}/api/admin/promotores/${p.idUsuario}/festivales" class="text-indigo-600 hover:text-indigo-900 underline font-medium" title="Ver festivales de ${p.nombre}">
                                                Ver Festivales
                                            </a>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-2">
                                            <form action="${pageContext.request.contextPath}/api/admin/promotores/cambiar-estado" method="post" class="inline">
                                                <input type="hidden" name="idPromotor" value="${p.idUsuario}">
                                                <input type="hidden" name="nuevoEstado" value="${!p.estado}">
                                                <button type="submit" class="${p.estado ? 'text-yellow-600 hover:text-yellow-900' : 'text-green-600 hover:text-green-900'} underline font-semibold" title="${p.estado ? 'Desactivar cuenta' : 'Activar cuenta'}">
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
