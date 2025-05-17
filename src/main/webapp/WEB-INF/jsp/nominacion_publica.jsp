<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Nominar Entrada - <c:out value="${nombreFestival}" default="Beatpass"/></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/output.css"> 
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nominar.css">  
    </head>
    <body>
        <div class="nomination-container">
            <div class="logo-container">
                <img src="${pageContext.request.contextPath}/img/logo.png" alt="Beatpass Logo">
            </div>
            <h1>Nominar Entrada</h1>

            <c:if test="${not empty requestScope.successMessage}">
                <div class="message success">
                    <p><c:out value="${requestScope.successMessage}"/></p>
                    <c:if test="${not empty requestScope.nominatedEmail}">
                        <p>Se ha enviado un email de confirmación a <strong><c:out value="${requestScope.nominatedEmail}"/></strong>.</p>
                    </c:if>
                </div>
            </c:if>

            <c:if test="${not empty requestScope.error}">
                <div class="message error">
                    <p><c:out value="${requestScope.error}"/></p>
                </div>
            </c:if>

            <c:if test="${empty requestScope.successMessage and not empty ticketCode and empty requestScope.hideForm}">
                <p class="festival-name">Estás nominando una entrada para: <strong><c:out value="${nombreFestival}" default="el festival"/></strong>.</p>

                <div class="form-group">
                    <span class="ticket-code-display-label">Código de Entrada:</span>
                    <div class="ticket-code-display"><c:out value="${ticketCode}"/></div>
                </div>

                <form id="nominationForm" method="POST" action="${pageContext.request.contextPath}/api/public/venta/nominar">
                    <input type="hidden" name="codigoQr" value="<c:out value='${ticketCode}'/>">

                    <div class="form-group">
                        <label for="nombreNominado">Nombre completo del Asistente: <span class="required-indicator">*</span></label>
                        <input type="text" id="nombreNominado" name="nombreNominado" required value="<c:out value='${requestScope.nombreNominadoParam}'/>" placeholder="Ej: Ada Lovelace">
                    </div>
                    <div class="form-group">
                        <label for="emailNominado">Email del Asistente: <span class="required-indicator">*</span></label>
                        <input type="email" id="emailNominado" name="emailNominado" required value="<c:out value='${requestScope.emailNominadoParam}'/>" placeholder="Ej: ada.lovelace@example.com">
                    </div>
                    <div class="form-group">
                        <label for="confirmEmailNominado">Confirmar Email del Asistente: <span class="required-indicator">*</span></label>
                        <input type="email" id="confirmEmailNominado" name="confirmEmailNominado" required value="<c:out value='${requestScope.confirmEmailNominadoParam}'/>" placeholder="Repite el email">
                        <%-- El div para el error de JS se insertará aquí --%>
                    </div>
                    <div class="form-group">
                        <label for="telefonoNominado">Teléfono del Asistente (Opcional):</label>
                        <input type="tel" id="telefonoNominado" name="telefonoNominado" value="<c:out value='${requestScope.telefonoNominadoParam}'/>" placeholder="Ej: 600123456">
                    </div>
                    <button type="submit" class="submit-button">Nominar Entrada</button>
                </form>
            </c:if>

            <%-- Mensaje si no hay ticketCode Y no hay un mensaje de éxito ya mostrado Y no hay un error de validación ya mostrado --%>
            <c:if test="${empty ticketCode and empty requestScope.successMessage and empty requestScope.error and empty requestScope.hideForm}">
                <div class="message error">
                    <p>No se ha especificado un código de entrada válido para nominar.</p>
                </div>
            </c:if>
        </div>

        <script>
            const form = document.getElementById('nominationForm');
            if (form) {
                form.addEventListener('submit', function (event) {
                    const emailInput = document.getElementById('emailNominado');
                    const confirmEmailInput = document.getElementById('confirmEmailNominado');

                    // Intentar remover el mensaje de error previo si existe
                    let jsEmailErrorDiv = document.getElementById('jsEmailError');
                    if (jsEmailErrorDiv) {
                        jsEmailErrorDiv.remove();
                    }

                    if (emailInput.value !== confirmEmailInput.value) {
                        event.preventDefault(); // Prevenir el envío del formulario

                        // Crear el div para el mensaje de error
                        jsEmailErrorDiv = document.createElement('div');
                        jsEmailErrorDiv.id = 'jsEmailError'; // ID para poder encontrarlo y quitarlo después
                        jsEmailErrorDiv.textContent = 'Los emails no coinciden. Por favor, verifica.';

                        // Añadir la clase CSS en lugar de estilos en línea
                        jsEmailErrorDiv.classList.add('js-email-error-message');

                        // Insertar el mensaje de error después del campo de confirmación de email
                        confirmEmailInput.parentNode.insertBefore(jsEmailErrorDiv, confirmEmailInput.nextSibling);

                        confirmEmailInput.focus();
                    }
                });
            }
        </script>
    </body>
</html>
