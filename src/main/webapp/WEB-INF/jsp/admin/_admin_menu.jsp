<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- 
    Fragmento JSP para el menú de navegación del panel de Administración.
    Recibe un atributo 'activePage' en el request scope para resaltar el enlace activo.
--%>

<header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
    <h1 class="text-3xl font-bold text-purple-700 mb-4 sm:mb-0">Panel de Administración</h1>

    <nav class="flex items-center space-x-4 flex-wrap justify-center sm:justify-end">
        <c:if test="${not empty sessionScope.userName}">
            <span class="text-sm text-gray-600">Admin: ${sessionScope.userName}</span>
        </c:if>

        <a href="${pageContext.request.contextPath}/api/admin/festivales/listar-todos"
           class="text-sm hover:underline ${param.activePage == 'festivales' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Festivales
        </a>

        <%-- ENLACE MODIFICADO: Ahora apunta a la nueva página de Clientes --%>
        <a href="${pageContext.request.contextPath}/api/admin/clientes?tab=asistentes"
           class="text-sm hover:underline ${param.activePage == 'clientes' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Clientes
        </a>

        <a href="${pageContext.request.contextPath}/api/admin/promotores/listar"
           class="text-sm hover:underline ${param.activePage == 'promotores' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Promotores
        </a>
        <a href="${pageContext.request.contextPath}/api/admin/cajeros/listar"
           class="text-sm hover:underline ${param.activePage == 'cajeros' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Cajeros
        </a>
        <a href="${pageContext.request.contextPath}/api/admin/admins/listar"
           class="text-sm hover:underline ${param.activePage == 'admins' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Admins
        </a>

        <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
            <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
        </form>
    </nav>
</header>