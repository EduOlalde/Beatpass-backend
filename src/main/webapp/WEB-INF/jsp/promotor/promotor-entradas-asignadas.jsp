<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Entradas Asignadas - ${festival.nombre} - Beatpass Promotor</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            /* Estilos generales */
            body {
                font-family: 'Inter', sans-serif;
            }
            /* Estilos para badges de estado */
            .badge {
                padding: 0.1em 0.6em;
                border-radius: 9999px;
                font-size: 0.75rem;
                font-weight: 600;
                display: inline-flex;
                align-items: center;
            }
            .badge-activa {
                background-color: #D1FAE5;
                color: #065F46;
            }
            .badge-usada {
                background-color: #E5E7EB;
                color: #374151;
            }
            .badge-cancelada {
                background-color: #FEE2E2;
                color: #991B1B;
            }
            /* Estilo para imagen QR */
            .qr-image {
                border: 1px solid #e5e7eb;
                display: block;
            }
            /* Estilos para acciones en tablas */
            .action-link {
                text-decoration: underline;
                font-size: 0.75rem; /* text-xs */
            }
            .action-link-edit {
                color: #D97706; /* text-yellow-600 */
                font-weight: 600; /* font-semibold */
            }
            .action-link-edit:hover {
                color: #92400E; /* hover:text-yellow-900 */
            }
            .action-button {
                background: none;
                border: none;
                padding: 0;
                cursor: pointer;
                text-decoration: underline;
                font-size: 0.75rem; /* text-xs */
                font-weight: 600; /* font-semibold */
            }
            .action-button-nominate {
                color: #4F46E5; /* text-indigo-600 */
            }
            .action-button-nominate:hover {
                color: #3730A3; /* hover:text-indigo-900 */
            }
            .action-button-associate {
                color: #059669; /* text-green-600 */
            }
            .action-button-associate:hover {
                color: #047857; /* hover:text-green-900 */
            }
            .action-button-cancel {
                color: #DC2626; /* text-red-600 */
            }
            .action-button-cancel:hover {
                color: #991B1B; /* hover:text-red-900 */
            }
            /* Estilos para inputs pequeños en acciones */
            .action-input {
                padding: 0.25rem 0.5rem; /* p-1 */
                border: 1px solid #D1D5DB; /* border-gray-300 */
                border-radius: 0.375rem; /* rounded-md */
                box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); /* shadow-sm */
                font-size: 0.75rem; /* text-xs */
                width: 9rem; /* w-36 */
            }
            .action-input:focus {
                --tw-ring-color: #6366F1; /* focus:ring-indigo-500 */
                border-color: #6366F1; /* focus:border-indigo-500 */
                box-shadow: var(--tw-ring-inset) 0 0 0 calc(1px + var(--tw-ring-offset-width)) var(--tw-ring-color);
                outline: 2px solid transparent;
                outline-offset: 2px;
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Cabecera --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">Panel de Promotor</h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-sm text-indigo-600 hover:underline font-medium">Mis Festivales</a>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <%-- Título y enlace volver --%>
            <div class="mb-6">
                <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm mb-2 inline-block">&larr; Volver a Detalles de ${festival.nombre}</a>
                <h2 class="text-2xl font-semibold text-gray-700">Gestionar Entradas Asignadas: ${festival.nombre}</h2>
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

            <%-- Tabla de Entradas Asignadas --%>
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
                            <c:when test="${empty entradasAsignadas}">
                                <tr> <td colspan="7" class="px-6 py-4 text-center text-sm text-gray-500 italic"> No hay entradas generadas para este festival todavía. </td> </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="ea" items="${entradasAsignadas}">
                                    <tr>
                                        <td class="px-3 py-4 whitespace-nowrap text-sm text-gray-500">${ea.idEntradaAsignada}</td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700">${ea.tipoEntradaOriginal}</td>
                                        <td class="px-4 py-4 whitespace-nowrap text-center">
                                            <c:if test="${not empty ea.qrCodeImageDataUrl}">
                                                <img src="${ea.qrCodeImageDataUrl}" alt="QR Entrada ${ea.idEntradaAsignada}"
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
                                                <c:when test="${not empty ea.idAsistente}">
                                                    ${ea.nombreAsistente} <br>
                                                    <span class="text-xs text-gray-500">${ea.emailAsistente}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-xs text-gray-500 italic">(Pendiente)</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <c:if test="${not empty ea.fechaAsignacion}">
                                                <fmt:parseDate value="${ea.fechaAsignacion}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDateTime" type="both" />
                                                <fmt:formatDate value="${parsedDateTime}" pattern="dd/MM/yyyy HH:mm" />
                                            </c:if>
                                        </td>
                                        <td class="px-4 py-4 whitespace-nowrap text-center text-sm">
                                            <div class="flex flex-col items-center space-y-1 md:space-y-2">
                                                <%-- Formulario Nominar (Estilos Homogeneizados) --%>
                                                <c:if test="${empty ea.idAsistente and ea.estado == 'ACTIVA'}">
                                                    <form action="${pageContext.request.contextPath}/api/promotor/entradas-asignadas/${ea.idEntradaAsignada}/nominar" method="post" class="inline-block"
                                                          onsubmit="return confirm('Nominar entrada ID ${ea.idEntradaAsignada} a ' + document.getElementById('emailAsistente_${ea.idEntradaAsignada}').value + '?');">
                                                        <div class="flex flex-col space-y-1 text-left">
                                                            <input type="email" id="emailAsistente_${ea.idEntradaAsignada}" name="emailAsistente" required placeholder="Email Asistente" title="Email del asistente" class="action-input">
                                                            <input type="text" name="nombreAsistente" required placeholder="Nombre Asistente" title="Nombre (obligatorio si es nuevo)" class="action-input">
                                                            <input type="tel" name="telefonoAsistente" placeholder="Teléfono (Opcional)" title="Teléfono (opcional)" class="action-input">
                                                            <button type="submit" class="action-button action-button-nominate w-full text-center" title="Nominar esta entrada">Confirmar Nominación</button>
                                                        </div>
                                                    </form>
                                                </c:if>

                                                <%-- Formulario Asociar Pulsera (Estilos Homogeneizados) --%>
                                                <c:if test="${not empty ea.idAsistente and ea.estado == 'ACTIVA' and empty ea.idPulseraAsociada}">
                                                    <form action="${pageContext.request.contextPath}/api/promotor/entradas-asignadas/${ea.idEntradaAsignada}/asociar-pulsera" method="post" class="inline-block mt-1"
                                                          onsubmit="return confirm('Asociar pulsera con UID ' + this.codigoUid.value + ' a entrada ID ${ea.idEntradaAsignada}?');">
                                                        <div class="flex items-center space-x-1">
                                                            <input type="text" name="codigoUid" required placeholder="UID Pulsera" title="Introduce el UID de la pulsera NFC" class="action-input w-28">
                                                            <button type="submit" class="action-button action-button-associate" title="Asociar Pulsera">Asociar</button>
                                                        </div>
                                                    </form>
                                                </c:if>
                                                <%-- Mostrar UID si ya está asociada --%>
                                                <c:if test="${not empty ea.idPulseraAsociada}">
                                                    <span class="text-xs text-green-700 font-medium block mt-1" title="Pulsera asociada">
                                                        Pulsera: <span class="text-xs font-mono bg-gray-100 px-1 rounded">${ea.codigoUidPulsera}</span>
                                                    </span>
                                                </c:if>

                                                <%-- Otros botones (Modificar, Cancelar - Estilos Homogeneizados) --%>
                                                <div class="flex justify-center space-x-2 mt-1">
                                                    <c:if test="${not empty ea.idAsistente and ea.estado == 'ACTIVA'}">
                                                        <%-- TODO: Implementar funcionalidad Modificar Nominación --%>
                                                        <a href="#" class="action-link action-link-edit" onclick="alert('Modificar nominación entrada ID ${ea.idEntradaAsignada} - Pendiente'); return false;">Modificar</a>
                                                    </c:if>
                                                    <c:if test="${ea.estado == 'ACTIVA'}">
                                                        <form action="${pageContext.request.contextPath}/api/promotor/entradas-asignadas/${ea.idEntradaAsignada}/cancelar" method="post" class="inline"
                                                              onsubmit="return confirm('¿Cancelar entrada ID ${ea.idEntradaAsignada}?');">
                                                            <button type="submit" class="action-button action-button-cancel" title="Cancelar Entrada">Cancelar</button>
                                                        </form>
                                                    </c:if>
                                                </div>
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

        </div> <%-- Fin container --%>

    </body>
</html>
