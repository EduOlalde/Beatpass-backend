
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%--
  Fragmento JSP para el menú de navegación del panel de Administración.
  Recibe un atributo 'activePage' en el request scope para resaltar el enlace activo.
  Valores posibles: "admins", "promotores", "cajeros", "festivales", "asistentes".
--%>

<header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
    <h1 class="text-3xl font-bold text-purple-700 mb-4 sm:mb-0">Panel de Administración</h1>

    <%-- Menú de Navegación --%>
    <nav class="flex items-center space-x-4 flex-wrap justify-center sm:justify-end"> <%-- flex-wrap para móviles --%>
        <%-- Mostrar nombre de usuario si está en sesión --%>
        <c:if test="${not empty sessionScope.userName}">
            <span class="text-sm text-gray-600">Admin: ${sessionScope.userName}</span>
        </c:if>

        <%-- Enlaces de Navegación Usuarios --%>
        <a href="${pageContext.request.contextPath}/api/admin/admins/listar"
           class="text-sm hover:underline ${requestScope.activePage == 'admins' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Admins
        </a>
        <a href="${pageContext.request.contextPath}/api/admin/promotores/listar"
           class="text-sm hover:underline ${requestScope.activePage == 'promotores' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Promotores
        </a>
        <a href="${pageContext.request.contextPath}/api/admin/cajeros/listar"
           class="text-sm hover:underline ${requestScope.activePage == 'cajeros' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Cajeros
        </a>

        <%-- Enlaces de Navegación Otros --%>
        <a href="${pageContext.request.contextPath}/api/admin/festivales/listar-todos"
           class="text-sm hover:underline ${requestScope.activePage == 'festivales' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Festivales
        </a>
        <a href="${pageContext.request.contextPath}/api/admin/asistentes"
           class="text-sm hover:underline ${requestScope.activePage == 'asistentes' ? 'font-medium text-purple-700' : 'text-indigo-600'}">
            Gestionar Asistentes
        </a>

        <%-- Botón/Formulario de Logout --%>
        <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
            <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
        </form>
    </nav>
</header>