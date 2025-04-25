<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${editMode ? 'Editar' : 'Detalle'} Asistente - Beatpass Admin</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            /* Estilos generales */
            body {
                font-family: 'Inter', sans-serif;
            }
            /* Clases base para botones (se combinarán con colores específicos) */
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
            .btn-secondary {
                background-color: #E5E7EB; /* bg-gray-200 */
                color: #1F2937; /* text-gray-800 */
            }
            .btn-secondary:hover {
                background-color: #D1D5DB; /* hover:bg-gray-300 */
            }
            .btn-edit {
                background-color: #F59E0B; /* bg-yellow-500 */
                color: white; /* text-white */
            }
            .btn-edit:hover {
                background-color: #D97706; /* hover:bg-yellow-600 */
            }
            /* Estilos para campos readonly */
            .readonly-field {
                margin-top: 0.25rem; /* mt-1 */
                display: block;
                width: 100%;
                border-radius: 0.375rem; /* rounded-md */
                border-width: 1px;
                border-color: #E5E7EB; /* border-gray-200 */
                background-color: #F9FAFB; /* bg-gray-50 */
                padding-left: 0.75rem; /* px-3 */
                padding-right: 0.75rem; /* px-3 */
                padding-top: 0.5rem; /* py-2 */
                padding-bottom: 0.5rem; /* py-2 */
                font-size: 0.875rem; /* text-sm */
                color: #374151; /* text-gray-700 */
                box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); /* shadow-sm */
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-3xl">

            <%-- Incluir Menú Admin Común --%>
            <jsp:include page="/WEB-INF/jsp/admin/_admin_menu.jsp">
                <jsp:param name="activePage" value="asistentes"/>
            </jsp:include>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">
                ${editMode ? 'Editar' : 'Detalles del'} Asistente
            </h2>

            <div class="mb-4">
                <a href="${pageContext.request.contextPath}/api/admin/asistentes" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a la lista de Asistentes</a>
            </div>

            <%-- Mensajes flash --%>
            <c:if test="${not empty requestScope.mensajeExito}">
                <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p>${requestScope.mensajeExito}</p>
                </div>
            </c:if>
            <c:if test="${not empty requestScope.error}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error:</p> <p>${requestScope.error}</p>
                </div>
            </c:if>

            <%-- Formulario (para edición) o Div (para vista) --%>
            <form action="${pageContext.request.contextPath}/api/admin/asistentes/${asistente.idAsistente}/actualizar" method="post" class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4">

                <div>
                    <label class="block text-sm font-medium text-gray-500 mb-1">ID Asistente</label>
                    <%-- Campo readonly con estilo homogeneizado --%>
                    <p class="readonly-field">${asistente.idAsistente}</p>
                </div>

                <div>
                    <label for="nombre" class="block text-sm font-medium ${editMode ? 'text-gray-700' : 'text-gray-500'} mb-1">Nombre <c:if test="${editMode}"><span class="text-red-500 ml-1">*</span></c:if></label>
                    <c:choose>
                        <c:when test="${editMode}">
                            <%-- Input editable --%>
                            <input type="text" id="nombre" name="nombre" value="${asistente.nombre}" required maxlength="100"
                                   class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                        </c:when>
                        <c:otherwise>
                            <%-- Campo readonly con estilo homogeneizado --%>
                            <p class="readonly-field">${asistente.nombre}</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div>
                    <label for="email" class="block text-sm font-medium text-gray-500 mb-1">Email</label>
                    <%-- Campo readonly con estilo homogeneizado --%>
                    <p class="readonly-field">${asistente.email}</p>
                    <c:if test="${editMode}">
                        <p class="mt-1 text-xs text-gray-500">El email no se puede modificar.</p>
                    </c:if>
                </div>

                <div>
                    <label for="telefono" class="block text-sm font-medium ${editMode ? 'text-gray-700' : 'text-gray-500'} mb-1">Teléfono</label>
                    <c:choose>
                        <c:when test="${editMode}">
                            <%-- Input editable --%>
                            <input type="tel" id="telefono" name="telefono" value="${asistente.telefono}" maxlength="20"
                                   class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="(Opcional)">
                        </c:when>
                        <c:otherwise>
                            <%-- Campo readonly con estilo homogeneizado --%>
                            <p class="readonly-field">${not empty asistente.telefono ? asistente.telefono : '-'}</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-500 mb-1">Fecha de Registro</label>
                    <%-- Campo readonly con estilo homogeneizado --%>
                    <p class="readonly-field">
                        <c:catch var="formatError">
                            <fmt:formatDate value="${asistente.fechaCreacion}" pattern="dd/MM/yyyy HH:mm:ss"/>
                        </c:catch>
                        <c:if test="${not empty formatError}"> ${asistente.fechaCreacion} </c:if>
                        </p>
                    </div>

                <%-- Botones condicionales (Estilos Homogeneizados) --%>
                <div class="mt-6 pt-4 border-t border-gray-200 flex justify-end space-x-3">
                    <c:choose>
                        <c:when test="${editMode}">
                            <%-- Botones en modo edición --%>
                            <a href="${pageContext.request.contextPath}/api/admin/asistentes/${asistente.idAsistente}" class="btn btn-secondary">Cancelar</a>
                            <button type="submit" class="btn btn-primary">Guardar Cambios</button>
                        </c:when>
                        <c:otherwise>
                            <%-- Botón en modo vista --%>
                            <a href="${pageContext.request.contextPath}/api/admin/asistentes/${asistente.idAsistente}/editar" class="btn btn-edit">Editar</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </form>

        </div>

    </body>
</html>
