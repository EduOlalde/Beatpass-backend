<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <%-- Título ahora siempre es "Detalles" --%>
        <title>Detalles Festival: ${festival.nombre} - Beatpass Promotor</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&display=swap" rel="stylesheet">
        <style>
            body {
                font-family: 'Inter', sans-serif;
            }
            textarea {
                min-height: 8rem;
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
            /* Estilos para campos de solo lectura */
            /* *** CORRECCIÓN: Eliminada la regla .readonly-label errónea *** */
            .readonly-value {
                margin-top: 0.25rem;
                display: block;
                width: 100%;
                border-radius: 0.375rem;
                border: 1px solid #e5e7eb;
                background-color: #f9fafb;
                padding: 0.5rem 0.75rem;
                font-size: 0.875rem;
                color: #374151;
                box-shadow: inset 0 1px 2px 0 rgb(0 0 0 / 0.05);
                min-height: 38px; /* Alineación con inputs */
            }
        </style>
    </head>
    <body class="bg-gray-100 text-gray-800">

        <div class="container mx-auto p-4 md:p-8 max-w-4xl">

            <%-- Cabecera --%>
            <header class="flex flex-col sm:flex-row justify-between items-center mb-6 pb-4 border-b border-gray-300">
                <h1 class="text-3xl font-bold text-indigo-700 mb-4 sm:mb-0">
                    Detalles del Festival <%-- Título fijo --%>
                </h1>
                <div class="flex items-center space-x-4">
                    <c:if test="${not empty sessionScope.userName}">
                        <span class="text-sm text-gray-600">Hola, ${sessionScope.userName}</span>
                    </c:if>
                    <form action="${pageContext.request.contextPath}/logout" method="post" class="inline">
                        <button type="submit" class="text-sm text-gray-500 hover:text-red-600 underline">Cerrar Sesión</button>
                    </form>
                </div>
            </header>

            <h2 class="text-2xl font-semibold text-gray-700 mb-5">
                ${festival.nombre} <%-- Mostrar nombre directamente --%>
            </h2>

            <div class="mb-4 flex justify-between items-center">
                <a href="${pageContext.request.contextPath}/api/promotor/festivales" class="text-indigo-600 hover:text-indigo-800 text-sm">&larr; Volver a Mis Festivales</a>
                <%-- Botón Editar --%>
                <a href="${pageContext.request.contextPath}/api/promotor/festivales/editar/${festival.idFestival}"
                   class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-yellow-500 hover:bg-yellow-600 text-white">
                    Editar Datos Festival
                </a>
            </div>

            <%-- Mensajes --%>
            <c:if test="${not empty requestScope.error and empty requestScope.errorEntrada}">
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p class="font-bold">Error:</p> <p>${requestScope.error}</p> <%-- Mostrar error genérico si viene --%>
                </div>
            </c:if>
            <c:if test="${not empty requestScope.mensajeExito}">
                <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                    <p>${requestScope.mensajeExito}</p>
                </div>
            </c:if>


            <%-- Mostrar Datos Generales como solo lectura --%>
            <div class="bg-white p-6 md:p-8 rounded-lg shadow-md space-y-4 mb-10">
                <h3 class="text-lg font-semibold text-gray-600 border-b pb-2 mb-4">Datos Generales del Festival</h3>
                <div>
                    <%-- *** CORRECCIÓN: Clases Tailwind aplicadas directamente *** --%>
                    <span class="block text-sm font-medium text-gray-500 mb-1">Nombre del Festival</span>
                    <p class="readonly-value">${festival.nombre}</p>
                </div>
                <div>
                    <%-- *** CORRECCIÓN: Clases Tailwind aplicadas directamente *** --%>
                    <span class="block text-sm font-medium text-gray-500 mb-1">Descripción</span>
                    <p class="readonly-value">${not empty festival.descripcion ? festival.descripcion : '-'}</p>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <%-- *** CORRECCIÓN: Clases Tailwind aplicadas directamente *** --%>
                        <span class="block text-sm font-medium text-gray-500 mb-1">Fecha de Inicio</span>
                        <p class="readonly-value">${festival.fechaInicio}</p>
                    </div>
                    <div>
                        <%-- *** CORRECCIÓN: Clases Tailwind aplicadas directamente *** --%>
                        <span class="block text-sm font-medium text-gray-500 mb-1">Fecha de Fin</span>
                        <p class="readonly-value">${festival.fechaFin}</p>
                    </div>
                </div>
                <div>
                    <%-- *** CORRECCIÓN: Clases Tailwind aplicadas directamente *** --%>
                    <span class="block text-sm font-medium text-gray-500 mb-1">Ubicación</span>
                    <p class="readonly-value">${not empty festival.ubicacion ? festival.ubicacion : '-'}</p>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <%-- *** CORRECCIÓN: Clases Tailwind aplicadas directamente *** --%>
                        <span class="block text-sm font-medium text-gray-500 mb-1">Aforo</span>
                        <p class="readonly-value">${not empty festival.aforo ? festival.aforo : '-'}</p>
                    </div>
                    <div>
                        <%-- *** CORRECCIÓN: Clases Tailwind aplicadas directamente *** --%>
                        <span class="block text-sm font-medium text-gray-500 mb-1">URL de la Imagen</span>
                        <p class="readonly-value break-all">${not empty festival.imagenUrl ? festival.imagenUrl : '-'}</p>
                        <c:if test="${not empty festival.imagenUrl}">
                            <img src="${festival.imagenUrl}" alt="Imagen del festival ${festival.nombre}" class="mt-2 max-h-40 rounded shadow"
                                 onerror="this.style.display='none'; console.error('Error cargando imagen: ${festival.imagenUrl}')">
                        </c:if>
                    </div>
                </div>
                <div>
                    <%-- *** CORRECCIÓN: Clases Tailwind aplicadas directamente *** --%>
                    <span class="block text-sm font-medium text-gray-500 mb-1">Estado Actual</span>
                    <p class="readonly-value">
                        <span class="badge
                              <c:choose>
                                  <c:when test="${festival.estado == 'PUBLICADO'}">badge-publicado</c:when>
                                  <c:when test="${festival.estado == 'BORRADOR'}">badge-borrador</c:when>
                                  <c:when test="${festival.estado == 'CANCELADO'}">badge-cancelado</c:when>
                                  <c:when test="${festival.estado == 'FINALIZADO'}">badge-finalizado</c:when>
                                  <c:otherwise>bg-gray-100 text-gray-800</c:otherwise>
                              </c:choose>
                              "> ${festival.estado} </span>
                        <span class="text-xs text-gray-500 ml-2"> (Solo modificable por administrador)</span>
                    </p>
                </div>
                <%-- Botones de guardar/cancelar eliminados de esta sección --%>
            </div>

            <%-- Sección Tipos de Entrada (sin cambios estructurales, sigue aquí) --%>
            <div class="mt-10 pt-6 border-t border-gray-300">
                <h3 class="text-xl font-semibold mb-4 text-gray-700">Tipos de Entrada</h3>
                <%-- Mensaje de error específico para añadir entrada --%>
                <c:if test="${not empty requestScope.error and not empty requestScope.errorEntrada}">
                    <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-md shadow-sm" role="alert">
                        <p class="font-bold">Error al añadir tipo de entrada:</p>
                        <p>${requestScope.error}</p>
                    </div>
                </c:if>
                <%-- Tabla de Tipos de Entrada Existentes --%>
                <div class="bg-white p-4 md:p-6 rounded-lg shadow-md mb-6">
                    <h4 class="text-lg font-medium text-gray-800 mb-3">Entradas Definidas</h4>
                    <div class="overflow-x-auto">
                        <table class="min-w-full divide-y divide-gray-200">
                            <thead class="bg-gray-50">
                                <tr>
                                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Tipo</th>
                                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Descripción</th>
                                    <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Precio (€)</th>
                                    <th class="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">Stock</th>
                                    <th class="px-4 py-2 text-center text-xs font-medium text-gray-500 uppercase">Acciones</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <c:choose>
                                    <c:when test="${empty tiposEntrada}">
                                        <tr><td colspan="5" class="px-4 py-3 text-center text-sm text-gray-500 italic">Aún no has añadido ningún tipo de entrada.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="entrada" items="${tiposEntrada}">
                                            <tr>
                                                <td class="px-4 py-2 whitespace-nowrap text-sm font-medium text-gray-900">${entrada.tipo}</td>
                                                <td class="px-4 py-2 text-sm text-gray-600">${entrada.descripcion}</td>
                                                <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-600 text-right"><fmt:formatNumber value="${entrada.precio}" type="currency" currencySymbol="€" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                <td class="px-4 py-2 whitespace-nowrap text-sm text-gray-600 text-right">${entrada.stock}</td>
                                                <td class="px-4 py-2 whitespace-nowrap text-center text-sm space-x-2">
                                                    <a href="${pageContext.request.contextPath}/api/promotor/entradas/${entrada.idEntrada}/editar" class="text-indigo-600 hover:text-indigo-900 underline font-medium p-0 bg-transparent shadow-none text-xs">Editar</a>
                                                    <form action="${pageContext.request.contextPath}/api/promotor/entradas/${entrada.idEntrada}/eliminar" method="post" class="inline" onsubmit="return confirm('¿Eliminar tipo entrada \'${entrada.tipo}\'?');">
                                                        <button type="submit" class="text-red-600 hover:text-red-900 underline font-semibold p-0 bg-transparent shadow-none text-xs">Eliminar</button>
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
                <%-- Formulario para Añadir Nuevo Tipo de Entrada --%>
                <div class="bg-white p-4 md:p-6 rounded-lg shadow-md">
                    <h4 class="text-lg font-medium text-gray-800 mb-3 border-b pb-2">Añadir Nuevo Tipo de Entrada</h4>
                    <form action="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/entradas" method="post" class="space-y-3">
                        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label for="tipoEntrada" class="block text-sm font-medium text-gray-700 mb-1">Tipo <span class="text-red-500 ml-1">*</span></label>
                                <input type="text" id="tipoEntrada" name="tipo" value="${nuevaEntrada.tipo}" required maxlength="50" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="Ej: General">
                            </div>
                            <div>
                                <label for="precioEntrada" class="block text-sm font-medium text-gray-700 mb-1">Precio (€) <span class="text-red-500 ml-1">*</span></label>
                                <input type="number" id="precioEntrada" name="precio" value="${nuevaEntrada.precio}" required min="0" step="0.01" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="Ej: 25.50">
                            </div>
                            <div>
                                <label for="stockEntrada" class="block text-sm font-medium text-gray-700 mb-1">Stock <span class="text-red-500 ml-1">*</span></label>
                                <input type="number" id="stockEntrada" name="stock" value="${nuevaEntrada.stock}" required min="0" step="1" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="Ej: 1000">
                            </div>
                        </div>
                        <div>
                            <label for="descEntrada" class="block text-sm font-medium text-gray-700 mb-1">Descripción (Opcional)</label>
                            <textarea id="descEntrada" name="descripcion" rows="2" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="Detalles sobre este tipo de entrada...">${nuevaEntrada.descripcion}</textarea>
                        </div>
                        <div class="flex justify-end pt-2">
                            <button type="submit" class="font-bold py-1 px-3 text-sm rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-green-500 hover:bg-green-600 text-white"> Añadir Tipo de Entrada </button>
                        </div>
                    </form>
                </div>
            </div>

            <%-- Gestión de Entradas Vendidas/Asignadas y Asistentes (sin cambios) --%>
            <div class="mt-10 pt-6 border-t border-gray-300">
                <h3 class="text-xl font-semibold mb-4 text-gray-700">Gestión de Entradas y Asistentes</h3>
                <div class="bg-white p-6 rounded-lg shadow-md grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div class="md:col-span-1">
                        <p class="text-gray-600 mb-2 text-sm">Ver/gestionar entradas individuales generadas y nominarlas.</p>
                        <a href="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/entradas-asignadas"
                           class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-blue-600 hover:bg-blue-700 text-white text-sm w-full justify-center">
                            Gestionar Entradas
                        </a>
                    </div>
                    <div class="md:col-span-1">
                        <p class="text-gray-600 mb-2 text-sm">Consultar asistentes con entradas para este festival.</p>
                        <a href="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/asistentes"
                           class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-teal-600 hover:bg-teal-700 text-white text-sm w-full justify-center">
                            Ver Asistentes
                        </a>
                    </div>
                    <div class="md:col-span-1">
                        <p class="text-gray-600 mb-2 text-sm">Consultar pulseras NFC asociadas a entradas de este festival.</p>
                        <a href="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/pulseras"
                           class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-orange-600 hover:bg-orange-700 text-white text-sm w-full justify-center">
                            Ver Pulseras NFC
                        </a>
                    </div>
                </div>
            </div>

            <%-- Simular Venta (Pruebas) (sin cambios) --%>
            <div class="mt-10 pt-6 border-t border-gray-300">
                <h3 class="text-xl font-semibold mb-4 text-yellow-700">Simular Venta (SOLO PRUEBAS)</h3>
                <div class="bg-yellow-50 p-6 rounded-lg shadow-md border border-yellow-300">
                    <p class="text-sm text-yellow-800 mb-4">Utiliza este formulario para simular la venta de entradas y probar la generación de Entradas Asignadas y QRs. Necesitas un ID de Asistente válido existente en la base de datos.</p>
                    <form action="${pageContext.request.contextPath}/api/promotor/festivales/${festival.idFestival}/simular-venta" method="post" class="space-y-3">
                        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label for="simIdEntrada" class="block text-sm font-medium text-gray-700 mb-1">ID Tipo Entrada <span class="text-red-500 ml-1">*</span></label>
                                <select id="simIdEntrada" name="idEntrada" required class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                                    <option value="">-- Selecciona Tipo --</option>
                                    <c:forEach var="t" items="${tiposEntrada}">
                                        <option value="${t.idEntrada}">${t.tipo} (Stock: ${t.stock})</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label for="simIdAsistente" class="block text-sm font-medium text-gray-700 mb-1">ID Asistente <span class="text-red-500 ml-1">*</span></label>
                                <input type="number" id="simIdAsistente" name="idAsistente" required min="1"
                                       class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm" placeholder="ID Asistente existente">
                            </div>
                            <div>
                                <label for="simCantidad" class="block text-sm font-medium text-gray-700 mb-1">Cantidad <span class="text-red-500 ml-1">*</span></label>
                                <input type="number" id="simCantidad" name="cantidad" required min="1" step="1" value="1"
                                       class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 sm:text-sm">
                            </div>
                        </div>
                        <div class="flex justify-end pt-2">
                            <button type="submit" class="font-bold py-2 px-4 rounded shadow transition duration-150 ease-in-out inline-flex items-center bg-yellow-500 hover:bg-yellow-600 text-white">
                                Simular Venta
                            </button>
                        </div>
                    </form>
                </div>
            </div>

        </div> <%-- Fin container --%>

    </body>
</html>
