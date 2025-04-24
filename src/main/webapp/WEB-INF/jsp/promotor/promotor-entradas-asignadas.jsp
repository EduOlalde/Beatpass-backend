<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Entradas Asignadas - ${festival.nombre} - Beatpass Promotor</title>
        <%-- Incluir Tailwind CSS desde CDN --%>
        <script src="https://cdn.tailwindcss.com"></script>
        <%-- Incluir Google Fonts (Inter) --%>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <%-- Estilos CSS personalizados --%>
        <style>
            body {
                font-family: 'Inter', sans-serif; /* Aplicar fuente Inter */
            }
            /* Estilos para los badges de estado */
            .badge {
                padding: 0.1em 0.6em; /* Relleno interno */
                border-radius: 9999px; /* Bordes redondeados */
                font-size: 0.75rem; /* Tamaño de fuente pequeño */
                font-weight: 600; /* Peso de fuente semibold */
                display: inline-flex; /* Para alinear contenido */
                align-items: center; /* Centrar verticalmente */
            }
            .badge-activa {
                background-color: #D1FAE5; /* Tailwind green-100 */
                color: #065F46; /* Tailwind green-800 */
            }
            .badge-usada {
                background-color: #E5E7EB; /* Tailwind gray-200 */
                color: #374151; /* Tailwind gray-700 */
            }
            .badge-cancelada {
                background-color: #FEE2E2; /* Tailwind red-100 */
                color: #991B1B; /* Tailwind red-800 */
            }
            /* Estilo opcional para la imagen QR */
            .qr-image {
                border: 1px solid #e5e7eb; /* Borde gris claro (Tailwind gray-200) */
                display: block; /* Asegurar que sea un bloque para centrar con mx-auto */
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <%-- Contenedor principal con padding y ancho máximo --%>
        <div class="container mx-auto p-4 md:p-8 max-w-7xl">

            <%-- Cabecera con título, saludo y enlaces --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">Panel de Promotor</h1>
                <div class="flex items-center space-x-4">
                    <%-- Mostrar nombre de usuario si está en sesión --%>
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <%-- Enlace a la lista de festivales del promotor --%>
                    <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-sm text-indigo-600 hover:underline font-medium">Mis Festivales</a>
                    <%-- Formulario para cerrar sesión --%>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <%-- Título de la sección y enlace para volver --%>
            <div class="mb-6">
                <a href="${pageContext.request.contextPath}/api/promotor/festivales/ver/${festival.idFestival}" class="text-indigo-600 hover:text-indigo-800 text-sm mb-2 inline-block">&larr; Volver a Detalles de ${festival.nombre}</a>
                <h2 class="text-2xl font-semibold text-gray-700">Gestionar Entradas Asignadas: ${festival.nombre}</h2>
            </div>

            <%-- Sección para mostrar mensajes flash (éxito o error) --%>
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

            <%-- Tabla para mostrar las entradas asignadas --%>
            <div class="bg-white shadow-md rounded-lg overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tipo</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">QR</th> <%-- Columna QR --%>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Asistente</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha Nom.</th>
                            <th scope="col" class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <%-- Comprobar si la lista de entradas está vacía --%>
                        <c:choose>
                            <c:when test="${empty entradasAsignadas}">
                                <tr> <td colspan="7" class="px-6 py-4 text-center text-sm text-gray-500 italic"> No hay entradas generadas para este festival todavía. </td> </tr>
                            </c:when>
                            <c:otherwise>
                                <%-- Iterar sobre cada entrada asignada (ea) --%>
                                <c:forEach var="ea" items="${entradasAsignadas}">
                                    <tr>
                                        <%-- ID Entrada Asignada --%>
                                        <td class="px-3 py-4 whitespace-nowrap text-sm text-gray-500">${ea.idEntradaAsignada}</td>
                                        <%-- Tipo de Entrada Original --%>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-700">${ea.tipoEntradaOriginal}</td>

                                        <%-- Código QR (Imagen) --%>
                                        <td class="px-4 py-4 whitespace-nowrap text-center">
                                            <%-- Mostrar imagen si la URL de datos existe en el DTO --%>
                                            <c:if test="${not empty ea.qrCodeImageDataUrl}">
                                                <img src="${ea.qrCodeImageDataUrl}" alt="QR Entrada ${ea.idEntradaAsignada}"
                                                     title="QR: ${ea.codigoQr}" <%-- Mostrar contenido textual en tooltip --%>
                                                     class="w-16 h-16 mx-auto qr-image" <%-- Tamaño 64x64px, centrado y con borde --%>
                                                     width="64" height="64"> <%-- Atributos width/height para rendimiento --%>
                                            </c:if>
                                            <%-- Mostrar mensaje si no se pudo generar la imagen QR --%>
                                            <c:if test="${empty ea.qrCodeImageDataUrl}">
                                                <span class="text-xs text-gray-400 italic">(QR no disponible)</span>
                                            </c:if>
                                        </td>

                                        <%-- Estado de la Entrada (con badge de color) --%>
                                        <td class="px-4 py-4 whitespace-nowrap text-center">
                                            <span class="badge
                                                  <c:choose>
                                                      <c:when test="${ea.estado == 'ACTIVA'}">badge-activa</c:when>
                                                      <c:when test="${ea.estado == 'USADA'}">badge-usada</c:when>
                                                      <c:when test="${ea.estado == 'CANCELADA'}">badge-cancelada</c:when>
                                                      <c:otherwise>bg-gray-100 text-gray-800</c:otherwise> <%-- Estilo por defecto --%>
                                                  </c:choose>
                                                  "> ${ea.estado} </span>
                                        </td>

                                        <%-- Información del Asistente (si está nominada) --%>
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

                                        <%-- Fecha de Nominación (formateada) --%>
                                        <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500">
                                            <c:if test="${not empty ea.fechaAsignacion}">
                                                <%-- Parsear LocalDateTime de ISO a objeto Date --%>
                                                <fmt:parseDate value="${ea.fechaAsignacion}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDateTime" type="both" />
                                                <%-- Formatear objeto Date a dd/MM/yyyy HH:mm --%>
                                                <fmt:formatDate value="${parsedDateTime}" pattern="dd/MM/yyyy HH:mm" />
                                            </c:if>
                                        </td>

                                        <%-- Columna de Acciones --%>
                                        <td class="px-4 py-4 whitespace-nowrap text-center text-sm">
                                            <div class="flex flex-col items-center space-y-1 md:space-y-2">
                                                <%-- Formulario para Nominar (si está ACTIVA y no nominada) --%>
                                                <c:if test="${empty ea.idAsistente and ea.estado == 'ACTIVA'}">
                                                    <form action="${pageContext.request.contextPath}/api/promotor/entradas-asignadas/${ea.idEntradaAsignada}/nominar" method="post" class="inline-block"
                                                          onsubmit="return confirm('Nominar entrada ID ${ea.idEntradaAsignada} a ' + document.getElementById('emailAsistente_${ea.idEntradaAsignada}').value + '?');">
                                                        <div class="flex flex-col space-y-1 text-left">
                                                            <input type="email" id="emailAsistente_${ea.idEntradaAsignada}" name="emailAsistente" required placeholder="Email Asistente" title="Email del asistente"
                                                                   class="p-1 border border-gray-300 rounded-md shadow-sm text-xs focus:ring-indigo-500 focus:border-indigo-500 w-36">
                                                            <input type="text" name="nombreAsistente" required placeholder="Nombre Asistente" title="Nombre (obligatorio si es nuevo)"
                                                                   class="p-1 border border-gray-300 rounded-md shadow-sm text-xs focus:ring-indigo-500 focus:border-indigo-500 w-36">
                                                            <input type="tel" name="telefonoAsistente" placeholder="Teléfono (Opcional)" title="Teléfono (opcional)"
                                                                   class="p-1 border border-gray-300 rounded-md shadow-sm text-xs focus:ring-indigo-500 focus:border-indigo-500 w-36">
                                                            <button type="submit" class="w-full text-indigo-600 hover:text-indigo-900 underline font-medium p-0 bg-transparent shadow-none text-xs text-center" title="Nominar esta entrada">Confirmar Nominación</button>
                                                        </div>
                                                    </form>
                                                </c:if>

                                                <%-- Formulario para Asociar Pulsera (si está ACTIVA, nominada y sin pulsera) --%>
                                                <c:if test="${not empty ea.idAsistente and ea.estado == 'ACTIVA' and empty ea.idPulseraAsociada}">
                                                    <form action="${pageContext.request.contextPath}/api/promotor/entradas-asignadas/${ea.idEntradaAsignada}/asociar-pulsera" method="post" class="inline-block mt-1"
                                                          onsubmit="return confirm('Asociar pulsera con UID ' + this.codigoUid.value + ' a entrada ID ${ea.idEntradaAsignada}?');">
                                                        <div class="flex items-center space-x-1">
                                                            <input type="text" name="codigoUid" required placeholder="UID Pulsera" title="Introduce el UID de la pulsera NFC"
                                                                   class="p-1 border border-gray-300 rounded-md shadow-sm text-xs focus:ring-indigo-500 focus:border-indigo-500 w-28">
                                                            <button type="submit" class="text-green-600 hover:text-green-900 underline font-medium p-0 bg-transparent shadow-none text-xs" title="Asociar Pulsera">Asociar</button>
                                                        </div>
                                                    </form>
                                                </c:if>
                                                <%-- Mostrar UID si ya está asociada --%>
                                                <c:if test="${not empty ea.idPulseraAsociada}">
                                                    <span class="text-xs text-green-700 font-medium block mt-1" title="Pulsera asociada">
                                                        Pulsera: <span class="text-xs font-mono bg-gray-100 px-1 rounded">${ea.codigoUidPulsera}</span>
                                                    </span>
                                                </c:if>

                                                <%-- Otros botones (Modificar, Cancelar) --%>
                                                <div class="flex justify-center space-x-2 mt-1">
                                                    <%-- Botón Modificar (si está ACTIVA y nominada) - Funcionalidad pendiente --%>
                                                    <c:if test="${not empty ea.idAsistente and ea.estado == 'ACTIVA'}">
                                                        <button type="button" class="text-yellow-600 hover:text-yellow-900 underline font-semibold p-0 bg-transparent shadow-none text-xs" onclick="alert('Modificar nominación entrada ID ${ea.idEntradaAsignada} - Pendiente');">Modificar</button>
                                                    </c:if>
                                                    <%-- Botón Cancelar (si está ACTIVA) --%>
                                                    <c:if test="${ea.estado == 'ACTIVA'}">
                                                        <form action="${pageContext.request.contextPath}/api/promotor/entradas-asignadas/${ea.idEntradaAsignada}/cancelar" method="post" class="inline"
                                                              onsubmit="return confirm('¿Cancelar entrada ID ${ea.idEntradaAsignada}?');">
                                                            <button type="submit" class="text-red-600 hover:text-red-900 underline font-semibold p-0 bg-transparent shadow-none text-xs" title="Cancelar Entrada">Cancelar</button>
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
