<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<c:set var="pageTitleVar" value="Entradas - ${festival.nombre}" />

<jsp:include page="/WEB-INF/jsp/promotor/_header.jsp">
    <jsp:param name="pageTitle" value="${pageTitleVar}" />
    <jsp:param name="currentNav" value="detalleFestival" /> 
</jsp:include>

<div class="container mx-auto p-4 md:p-8 max-w-7xl">

    <div class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
        <h1 class="text-2xl md:text-3xl font-bold text-gray-800 mb-4 sm:mb-0">Gestionar Entradas: <span class="text-indigo-700">${festival.nombre}</span></h1>
    </div>

    <div class="mb-6">
        <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm mb-2 inline-block">&larr; Volver a Detalles de ${festival.nombre}</a>
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

    <%-- Tabla de Entradas --%>
    <div class="bg-white shadow-md rounded-lg overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
                <tr>
                    <th scope="col" class="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tipo</th>
                    <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">QR</th>
                    <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Asistente</th>
                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha Nom.</th>
                    <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
                <c:choose>
                    <c:when test="${empty entradas}">
                        <tr> <td colspan="7" class="px-6 py-4 text-center text-sm text-gray-500 italic"> No hay entradas generadas para este festival todavía. </td> </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="ea" items="${entradas}">
                            <tr>
                                <td class="px-3 py-4 whitespace-nowrap text-sm text-gray-500">${ea.idEntrada}</td>
                                <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700">${ea.tipoEntradaOriginal}</td>
                                <td class="px-4 py-4 whitespace-nowrap text-center">
                                    <c:if test="${not empty ea.qrCodeImageDataUrl}">
                                        <img src="${ea.qrCodeImageDataUrl}" alt="QR Entrada ${ea.idEntrada}"
                                             title="QR: ${ea.codigoQr}"
                                             class="w-16 h-16 mx-auto qr-image"
                                             width="64" height="64">
                                    </c:if>
                                    <c:if test="${empty ea.qrCodeImageDataUrl}">
                                        <span class="text-xs text-gray-400 italic">(QR no disponible)</span>
                                    </c:if>
                                </td>
                                <td class="px-4 py-4 whitespace-nowrap text-center">
                                    <span class="badge
                                          <c:choose>
                                              <c:when test="${ea.estado == 'ACTIVA'}">badge-activa</c:when>
                                              <c:when test="${ea.estado == 'USADA'}">badge-usada</c:when>
                                              <c:when test="${ea.estado == 'CANCELADA'}">badge-cancelada</c:when>
                                              <c:otherwise>bg-gray-100 text-gray-800</c:otherwise>
                                          </c:choose>
                                          "> ${ea.estado} </span>
                                </td>
                                <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700">
                                    <c:choose>
                                        <%-- Si ya está nominada, muestra los datos --%>
                                        <c:when test="${not empty ea.idAsistente}">
                                            <div class="font-medium text-gray-900">${ea.nombreAsistente}</div>
                                            <div class="text-xs text-gray-500">${ea.emailAsistente}</div>
                                        </c:when>
                                        <%-- Si requiere nominación y está pendiente --%>
                                        <c:when test="${ea.requiereNominacion}">
                                            <span class="text-xs text-gray-500 italic">(Pendiente de nominación)</span>
                                        </c:when>
                                        <%-- Si no requiere nominación --%>
                                        <c:otherwise>
                                            <span class="badge badge-usada">Al Portador</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">
                                    <c:if test="${not empty ea.fechaAsignacion}">
                                        <fmt:formatDate value="${ea.fechaAsignacion}" pattern="dd/MM/yyyy HH:mm" />
                                    </c:if>
                                </td>
                                <td class="px-4 py-4 whitespace-nowrap text-center text-sm">
                                    <div class="flex flex-col items-center space-y-1 md:space-y-2">

                                        <%-- Formulario Nominar: Solo si la entrada lo requiere y está sin nominar --%>
                                        <c:if test="${ea.requiereNominacion and empty ea.idAsistente and ea.estado == 'ACTIVA'}">
                                            <form action="${pageContext.request.contextPath}/api/promotor/entradas/${ea.idEntrada}/nominar" method="post" class="inline-block"
                                                  onsubmit="return confirm('Nominar entrada ID ${ea.idEntrada} a ' + this.emailAsistente.value + '?');">
                                                <div class="flex flex-col space-y-1 text-left">
                                                    <input type="email" name="emailAsistente" required placeholder="Email Asistente" title="Email del asistente" class="action-input">
                                                    <input type="text" name="nombreAsistente" required placeholder="Nombre Asistente" title="Nombre (obligatorio si es nuevo)" class="action-input">
                                                    <input type="tel" name="telefonoAsistente" placeholder="Teléfono (Opcional)" title="Teléfono (opcional)" class="action-input">
                                                    <button type="submit" class="action-button action-button-nominate w-full text-center" title="Nominar esta entrada">Confirmar Nominación</button>
                                                </div>
                                            </form>
                                        </c:if>

                                        <%-- Formulario Asociar Pulsera: Si la entrada está activa, no tiene pulsera y (es al portador O ya está nominada) --%>
                                        <c:if test="${ea.estado == 'ACTIVA' and empty ea.idPulseraAsociada and (not ea.requiereNominacion or not empty ea.idAsistente)}">
                                            <form action="${pageContext.request.contextPath}/api/promotor/entradas/${ea.idEntrada}/asociar-pulsera" method="post" class="inline-block mt-1"
                                                  onsubmit="return confirm('Asociar pulsera con UID ' + this.codigoUid.value + ' a entrada ID ${ea.idEntrada}?');">
                                                <div class="flex items-center space-x-1">
                                                    <input type="text" name="codigoUid" required placeholder="UID Pulsera" title="Introduce el UID de la pulsera NFC" class="action-input w-28">
                                                    <button type="submit" class="action-button action-button-associate" title="Asociar Pulsera">Asociar</button>
                                                </div>
                                            </form>
                                        </c:if>

                                        <c:if test="${not empty ea.idPulseraAsociada}">
                                            <span class="text-xs text-green-700 font-medium block mt-1" title="Pulsera asociada">
                                                Pulsera: <span class="uid-code">${ea.codigoUidPulsera}</span>
                                            </span>
                                        </c:if>

                                        <%-- Botón Cancelar --%>
                                        <c:if test="${ea.estado == 'ACTIVA'}">
                                            <form action="${pageContext.request.contextPath}/api/promotor/entradas/${ea.idEntrada}/cancelar" method="post" class="inline mt-2"
                                                  onsubmit="return confirm('¿Cancelar entrada ID ${ea.idEntrada}?');">
                                                <button type="submit" class="action-button action-button-cancel" title="Cancelar Entrada">Cancelar</button>
                                            </form>
                                        </c:if>

                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
    <p class="text-xs text-gray-500 mt-2 italic">Nota: Para nominar, introduce email y nombre. Para asociar pulsera, introduce su UID (la entrada debe estar nominada y activa).</p>
</div>
