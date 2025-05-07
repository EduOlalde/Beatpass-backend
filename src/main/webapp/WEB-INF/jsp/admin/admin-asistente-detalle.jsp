<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${editMode ? 'Editar' : 'Detalle'} Asistente - Beatpass Admin</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css">
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-3xl">

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

            <form action="${pageContext.request.contextPath}/api/admin/asistentes/${asistente.idAsistente}/actualizar" method="post" class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4">

                <div>
                    <label class="block text-sm font-medium text-gray-500 mb-1">ID Asistente</label>
                    <%-- Campo readonly con clase CSS externa --%>
                    <p class="readonly-field">${asistente.idAsistente}</p>
                </div>

                <div>
                    <label for="nombre" class="block text-sm font-medium ${editMode ? 'text-gray-700' : 'text-gray-500'} mb-1">Nombre <c:if test="${editMode}"><span class="text-red-500 ml-1">*</span></c:if></label>
                    <c:choose>
                        <c:when test="${editMode}">
                            <%-- Input editable (se mantienen clases de Tailwind para inputs) --%>
                            <input type="text" id="nombre" name="nombre" value="${asistente.nombre}" required maxlength="100"
                                   class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                        </c:when>
                        <c:otherwise>
                            <p class="readonly-field">${asistente.nombre}</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div>
                    <label for="email" class="block text-sm font-medium text-gray-500 mb-1">Email</label>
                    <p class="readonly-field">${asistente.email}</p>
                    <c:if test="${editMode}">
                        <p class="mt-1 text-xs text-gray-500">El email no se puede modificar.</p>
                    </c:if>
                </div>

                <div>
                    <label for="telefono" class="block text-sm font-medium ${editMode ? 'text-gray-700' : 'text-gray-500'} mb-1">Teléfono</label>
                    <c:choose>
                        <c:when test="${editMode}">
                            <%-- Input editable (se mantienen clases de Tailwind) --%>
                            <input type="tel" id="telefono" name="telefono" value="${asistente.telefono}" maxlength="20"
                                   class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="(Opcional)">
                        </c:when>
                        <c:otherwise>
                            <p class="readonly-field">${not empty asistente.telefono ? asistente.telefono : '-'}</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-500 mb-1">Fecha de Registro</label>
                    <p class="readonly-field">
                        <c:catch var="formatError">
                            <fmt:formatDate value="${asistente.fechaCreacion}" pattern="dd/MM/yyyy HH:mm:ss"/>
                        </c:catch>
                        <c:if test="${not empty formatError}"> ${asistente.fechaCreacion} </c:if>
                        </p>
                    </div>

                <div class="mt-6 pt-4 border-t border-gray-200 flex justify-end space-x-3">
                    <c:choose>
                        <c:when test="${editMode}">
                     
                            <a href="${pageContext.request.contextPath}/api/admin/asistentes/${asistente.idAsistente}" class="btn btn-secondary">Cancelar</a>
                            <button type="submit" class="btn btn-primary">Guardar Cambios</button>
                        </c:when>
                        <c:otherwise>
                   
                            <a href="${pageContext.request.contextPath}/api/admin/asistentes/${asistente.idAsistente}/editar" class="btn btn-edit">Editar</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </form>

        </div>

    </body>
</html>
