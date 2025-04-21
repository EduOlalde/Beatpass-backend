<%--
    Vista para que el Administrador gestione TODOS los festivales.
    Muestra una tabla con opción de filtrar por estado y acciones de Admin.
    ACTUALIZADO: Corregido error @apply.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Gestionar Festivales - Beatpass Admin</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            /* Estilos generales que NO usan @apply */
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
            .badge-borrador {
                background-color: #FEF3C7;
                color: #92400E;
            }
            .badge-publicado {
                background-color: #D1FAE5;
                color: #065F46;
            }
            .badge-cancelado {
                background-color: #FEE2E2;
                color: #991B1B;
            }
            .badge-finalizado {
                background-color: #E5E7EB;
                color: #374151;
            }
            /* Las clases para botones (btn, btn-*, btn-link-*) se aplicarán directamente */
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Cabecera Admin --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-purple-700 mb-4 sm:mb-0">Panel de Administración</h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Admin: ${sessionScope.userName}</span>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/api/admin/promotores/listar" class="text-sm text-indigo-600 hover:underline">Gestionar Promotores</a>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">Gestionar Todos los Festivales</h2>

            <div class="flex justify-end mb-4">
                <a href="${pageContext.request.contextPath}/api/admin/festivales/crear"
                   class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-green-600 hover:bg-green-700 text-white"> <%-- btn btn-success --%>
                    + Crear Festival (Asignar a Promotor)
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

            <%-- Filtro por estado --%>
            <div class="mb-4 bg-white p-4 rounded shadow-sm">
                <form action="${pageContext.request.contextPath}/api/admin/festivales/listar-todos" method="get" class="flex items-end space-x-3">
                    <div>
                        <%-- Aplicar clases a label --%>
                        <label for="estadoFilter" class="block text-sm font-medium text-gray-700 mb-1">Filtrar por Estado:</label>
                        <%-- Aplicar clases a select --%>
                        <select id="estadoFilter" name="estado" class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md shadow-sm">
                            <option value="">-- Todos los Estados --</option>
                            <c:forEach var="e" items="${estadosPosibles}">
                                <option value="${e}" ${requestScope.estadoFiltro == e ? 'selected' : ''}>${e}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <%-- Botón Filtrar con clases aplicadas --%>
                    <button type="submit" class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center text-sm bg-blue-600 hover:bg-blue-700 text-white"> <%-- btn btn-info btn-sm --%>
                        Filtrar
                    </button>
                    <%-- Botón Limpiar con clases aplicadas --%>
                    <a href="${pageContext.request.contextPath}/api/admin/festivales/listar-todos" class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center text-sm bg-gray-200 hover:bg-gray-300 text-gray-800">Limpiar Filtro</a> <%-- btn btn-secondary btn-sm --%>
                </form>
            </div>


            <%-- Tabla de Festivales --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre Festival</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Promotor</th>
                            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fechas</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones (Admin)</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <c:choose>
                            <c:when test="${empty festivales}">
                                <tr> <td colspan="6" class="px-6 py-4 text-center text-sm text-gray-500">
                                        <c:choose>
                                            <c:when test="${not empty requestScope.estadoFiltro}">No hay festivales con estado '${requestScope.estadoFiltro}'.</c:when>
                                            <c:otherwise>No hay festivales registrados.</c:otherwise>
                                        </c:choose>
                                    </td> </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="f" items="${festivales}">
                                    <tr>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">${f.idFestival}</td>
                                        <td class="px-6 py-4 whitespace-nowrap"> <div class="text-sm font-medium text-gray-900">${f.nombre}</div> </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <c:if test="${not empty f.nombrePromotor}">
                                                <%-- Botón estilo link --%>
                                                <a href="${pageContext.request.contextPath}/api/admin/promotores/${f.idPromotor}/festivales" class="text-indigo-600 hover:text-indigo-900 underline font-medium" title="Ver promotor y sus festivales">
                                                    ${f.nombrePromotor} (ID: ${f.idPromotor})
                                                </a>
                                            </c:if>
                                            <c:if test="${empty f.nombrePromotor}">(ID: ${f.idPromotor})</c:if>
                                            </td>
                                            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"> ${f.fechaInicio} - ${f.fechaFin} </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center">
                                            <span class="badge
                                                  <c:choose>
                                                      <c:when test="${f.estado == 'PUBLICADO'}">badge-publicado</c:when>
                                                      <c:when test="${f.estado == 'BORRADOR'}">badge-borrador</c:when>
                                                      <c:when test="${f.estado == 'CANCELADO'}">badge-cancelado</c:when>
                                                      <c:when test="${f.estado == 'FINALIZADO'}">badge-finalizado</c:when>
                                                      <c:otherwise>bg-gray-100 text-gray-800</c:otherwise>
                                                  </c:choose>
                                                  "> ${f.estado} </span>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-2">
                                            <c:if test="${f.estado == 'BORRADOR'}">
                                                <form action="${pageContext.request.contextPath}/api/admin/festivales/confirmar" method="post" class="inline" onsubmit="return confirm('Confirmar y publicar \'${f.nombre}\'?');">
                                                    <input type="hidden" name="idFestival" value="${f.idFestival}">
                                                    <%-- Botón estilo link verde --%>
                                                    <button type="submit" class="text-green-600 hover:text-green-900 underline font-semibold" title="Confirmar y Publicar">Confirmar</button>
                                                </form>
                                            </c:if>
                                            <c:if test="${f.estado == 'PUBLICADO'}">
                                                <form action="${pageContext.request.contextPath}/api/admin/festivales/cambiar-estado" method="post" class="inline" onsubmit="return confirm('¿Estás seguro de CANCELAR el festival \'${f.nombre}\'?');">
                                                    <input type="hidden" name="idFestival" value="${f.idFestival}">
                                                    <input type="hidden" name="nuevoEstado" value="CANCELADO">
                                                    <%-- Botón estilo link rojo --%>
                                                    <button type="submit" class="text-red-600 hover:text-red-900 underline font-semibold" title="Cancelar Festival">Cancelar</button>
                                                </form>
                                                <form action="${pageContext.request.contextPath}/api/admin/festivales/cambiar-estado" method="post" class="inline" onsubmit="return confirm('¿Marcar el festival \'${f.nombre}\' como FINALIZADO?');">
                                                    <input type="hidden" name="idFestival" value="${f.idFestival}">
                                                    <input type="hidden" name="nuevoEstado" value="FINALIZADO">
                                                    <%-- Botón estilo link gris --%>
                                                    <button type="submit" class="text-gray-600 hover:text-gray-900 underline font-semibold" title="Marcar como Finalizado">Finalizar</button>
                                                </form>
                                            </c:if>
                                            <%-- <a href="#" class="text-indigo-600 hover:text-indigo-900 underline font-medium ml-2" title="Ver Detalles (Admin)">Ver</a> --%>
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
