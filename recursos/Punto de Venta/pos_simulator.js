// Definir la URL base de la API (¡Ajusta si es necesario!)
const API_BASE_URL = 'http://localhost:8080/BeatpassTFG/api';
let jwtToken = null; // Variable para almacenar el token JWT

// --- Obtener Elementos del DOM ---
const loginForm = document.getElementById('loginForm');
const loginStatus = document.getElementById('loginStatus');
const logoutButton = document.getElementById('logoutButton');
const posOperationsDiv = document.getElementById('posOperations');
const resultArea = document.getElementById('resultArea'); // Área general
const checkBalanceForm = document.getElementById('checkBalanceForm');
const rechargeForm = document.getElementById('rechargeForm');
const consumeForm = document.getElementById('consumeForm');

// *** NUEVO: Referencias a los divs de resultados específicos ***
const consultaResultDiv = document.getElementById('consultaResult');
const recargaResultDiv = document.getElementById('recargaResult');
const consumoResultDiv = document.getElementById('consumoResult');

// --- Funciones Auxiliares ---

/**
 * Almacena el token JWT en memoria y localStorage.
 * Actualiza la interfaz de usuario.
 * @param {string} token - El token JWT recibido.
 */
function storeToken(token) {
    jwtToken = token;
    localStorage.setItem('jwtToken', token);
    updateLoginStatus(true);
    console.log("Token JWT almacenado.");
}

/**
 * Limpia el token JWT de memoria y localStorage.
 * Actualiza la interfaz de usuario.
 */
function clearToken() {
    jwtToken = null;
    localStorage.removeItem('jwtToken');
    updateLoginStatus(false);
    console.log("Token JWT eliminado.");
}

/**
 * Intenta cargar el token JWT desde localStorage al iniciar la página.
 */
function loadToken() {
    const storedToken = localStorage.getItem('jwtToken');
    if (storedToken) {
        jwtToken = storedToken;
        updateLoginStatus(true);
        console.log("Token JWT cargado desde localStorage.");
    } else {
        updateLoginStatus(false);
    }
}

/**
 * Actualiza los elementos de la interfaz (estado de login, visibilidad de secciones).
 * @param {boolean} isLoggedIn - Indica si el usuario está autenticado.
 */
function updateLoginStatus(isLoggedIn) {
    if (isLoggedIn) {
        loginStatus.textContent = 'Autenticado';
        loginStatus.className = 'success';
        posOperationsDiv.style.display = 'block';
        logoutButton.style.display = 'inline-block';
        loginForm.style.display = 'none';
    } else {
        loginStatus.textContent = 'No autenticado';
        loginStatus.className = 'error';
        posOperationsDiv.style.display = 'none';
        logoutButton.style.display = 'none';
        loginForm.style.display = 'block';
        // Limpiar resultados al hacer logout
        clearSpecificResults();
        resultArea.innerHTML = 'Esperando acciones...';
        resultArea.className = ''; // Resetear clase
    }
}

/**
 * Muestra resultados o mensajes de error en el área GENERAL designada del HTML.
 * @param {any} data - Los datos a mostrar (objeto, string).
 * @param {boolean} [isError=false] - Indica si es un mensaje de error.
 */
function displayResult(data, isError = false) {
    resultArea.innerHTML = ''; // Limpiar contenido previo
    const content = document.createElement('div');
    content.textContent = typeof data === 'object' ? JSON.stringify(data, null, 2) : data;
    if (isError) {
        resultArea.className = 'error'; // Aplicar clase CSS de error al contenedor general
        console.error("API Error:", data);
    } else {
        resultArea.className = 'success'; // Aplicar clase CSS de éxito al contenedor general
        console.log("API Success:", data);
    }
    resultArea.appendChild(content);
}

/**
 * Muestra resultados o mensajes de error en un DIV ESPECÍFICO de sección.
 * @param {HTMLElement} targetDiv - El elemento div donde mostrar el resultado.
 * @param {string} message - El mensaje a mostrar.
 * @param {boolean} isError - Indica si es un mensaje de error.
 */
function displaySpecificResult(targetDiv, message, isError) {
    if (targetDiv) {
        targetDiv.textContent = message;
        targetDiv.className = `section-result ${isError ? 'error' : 'success'}`;
    }
}

/**
 * Limpia el contenido y las clases de los divs de resultados específicos.
 */
function clearSpecificResults() {
    if (consultaResultDiv) {
        consultaResultDiv.textContent = '';
        consultaResultDiv.className = 'section-result';
    }
    if (recargaResultDiv) {
        recargaResultDiv.textContent = '';
        recargaResultDiv.className = 'section-result';
    }
    if (consumoResultDiv) {
        consumoResultDiv.textContent = '';
        consumoResultDiv.className = 'section-result';
    }
}


/**
 * Realiza una llamada fetch a la API, añadiendo el token JWT si está disponible.
 * Maneja la respuesta y posibles errores.
 * @param {string} url - La URL completa del endpoint de la API.
 * @param {object} [options={}] - Opciones para la función fetch (method, body, etc.).
 * @returns {Promise<any>} - Promesa que resuelve con los datos de la respuesta o rechaza con error.
 */
async function fetchWithAuth(url, options = {}) {
    const headers = { ...(options.headers || {}) };
    if (jwtToken) {
        headers['Authorization'] = `Bearer ${jwtToken}`;
    }
    if (options.body) {
        if (options.body instanceof URLSearchParams) {
            if (!headers['Content-Type']) headers['Content-Type'] = 'application/x-www-form-urlencoded';
        } else if (typeof options.body === 'object' && !(options.body instanceof FormData)) {
            if (!headers['Content-Type']) headers['Content-Type'] = 'application/json';
            if (typeof options.body !== 'string') options.body = JSON.stringify(options.body);
        }
    }
    const fetchOptions = { ...options, headers: headers };
    console.log(`Fetching: ${url}`, fetchOptions);

    try {
        const response = await fetch(url, fetchOptions);
        let responseData = null;
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            try { responseData = await response.json(); }
            catch (jsonError) {
                console.warn("Fallo al parsear JSON, obteniendo texto.", jsonError);
                try { responseData = await response.text(); }
                catch (textError) { console.error("Fallo al obtener texto.", textError); responseData = `Error ${response.status}: Respuesta no procesable.`; }
            }
        } else {
            try { responseData = await response.text(); }
            catch (textError) { console.error("Fallo al obtener texto no JSON.", textError); responseData = `Error ${response.status}: Respuesta no procesable.`; }
        }

        if (!response.ok) {
            console.error(`HTTP Error: ${response.status} ${response.statusText}`, responseData);
            const errorMessage = (responseData && typeof responseData === 'object' && responseData.error)
                ? responseData.error
                : (responseData && typeof responseData === 'string' && responseData.length < 200 ? responseData : `Error ${response.status}: ${response.statusText}`);
            // Mostrar error en el área GENERAL
            displayResult(errorMessage, true);
            // Lanzar error para detener la ejecución y ser capturado por el handler
            throw new Error(errorMessage);
        }
        console.log("Fetch successful", responseData);
        // Mostrar respuesta completa en el área GENERAL
        displayResult(responseData, false);
        return responseData; // Devolver los datos para uso específico

    } catch (error) {
        console.error('Error en fetchWithAuth:', error);
        // El error HTTP ya se mostró en displayResult. Si es otro tipo de error, mostrarlo.
        if (!error.message.includes("HTTP Error") && !error.message.startsWith("Error ")) {
            displayResult(`Error de red o fetch: ${error.message}`, true);
        }
        throw error; // Re-lanzar para que la función llamante sepa que falló
    }
}

// --- Manejadores de Eventos ---

/** Maneja el envío del formulario de login */
async function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(loginForm);
    const credentials = { email: formData.get('email'), password: formData.get('password') };
    displayResult("Intentando iniciar sesión...");
    clearSpecificResults(); // Limpiar resultados específicos al intentar login
    try {
        const data = await fetchWithAuth(`${API_BASE_URL}/auth/login`, { method: 'POST', body: credentials });
        if (data && data.token) {
            storeToken(data.token);
            // No mostramos nada en específico aquí, displayResult ya mostró el token
        } else {
            console.error("Respuesta de login inesperada (sin token):", data);
            displayResult("Error: Respuesta de login inesperada del servidor.", true);
            clearToken();
        }
    } catch (error) {
        clearToken();
        // El error ya fue mostrado por fetchWithAuth
    }
}

/** Maneja el clic en el botón de logout */
function handleLogout() {
    clearToken();
    displayResult("Sesión cerrada.");
    clearSpecificResults(); // Limpiar también los específicos
}

/** Maneja la consulta de datos de una pulsera */
async function handleCheckBalance(event) {
    event.preventDefault();
    const uid = document.getElementById('checkUid').value;
    // Limpiar resultados previos de esta sección y el general
    clearSpecificResults();
    resultArea.innerHTML = ''; resultArea.className = '';

    if (!uid) {
        displaySpecificResult(consultaResultDiv, "Por favor, introduce un UID de pulsera.", true);
        return;
    }
    displaySpecificResult(consultaResultDiv, `Consultando pulsera UID: ${uid}...`, false); // Mensaje inicial
    resultArea.innerHTML = `Consultando pulsera UID: ${uid}...`; // También en general

    try {
        const data = await fetchWithAuth(`${API_BASE_URL}/pos/pulseras/${uid}`, { method: 'GET' });
        // *** NUEVO: Mostrar resultado específico ***
        if (data && data.saldo !== undefined && data.activa !== undefined) {
            const estado = data.activa ? 'Activa' : 'Inactiva';
            // Usamos toLocaleString para formatear el número como moneda local
            const saldoFormateado = data.saldo.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
            displaySpecificResult(consultaResultDiv, `Saldo: ${saldoFormateado} | Estado: ${estado}`, false);
        } else {
            displaySpecificResult(consultaResultDiv, "Respuesta recibida, pero faltan datos de saldo o estado.", true);
        }
        // displayResult(data, false) // Ya se llama dentro de fetchWithAuth para el área general
    } catch (error) {
        // El error ya fue mostrado por fetchWithAuth en el área general
        // Mostramos un error específico en la sección
        displaySpecificResult(consultaResultDiv, `Error al consultar: ${error.message}`, true);
    }
}

/** Maneja el formulario de recarga de pulsera */
async function handleRecharge(event) {
    event.preventDefault();
    const formData = new FormData(rechargeForm);
    const uid = formData.get('codigoUid');
    const body = new URLSearchParams(formData);
    // Limpiar resultados previos de esta sección y el general
    clearSpecificResults();
    resultArea.innerHTML = ''; resultArea.className = '';

    displaySpecificResult(recargaResultDiv, `Intentando recargar pulsera UID: ${uid}...`, false);
    resultArea.innerHTML = `Intentando recargar pulsera UID: ${uid}...`;

    try {
        const data = await fetchWithAuth(`${API_BASE_URL}/pos/pulseras/${uid}/recargar`, { method: 'POST', body: body });
        // *** NUEVO: Mostrar resultado específico ***
        if (data && data.saldo !== undefined) {
            const nuevoSaldoFormateado = data.saldo.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
            displaySpecificResult(recargaResultDiv, `Recarga exitosa. Nuevo Saldo: ${nuevoSaldoFormateado}`, false);
        } else {
            displaySpecificResult(recargaResultDiv, "Recarga procesada, pero no se recibió el nuevo saldo.", true);
        }
        // displayResult(data, false); // Ya se llama dentro de fetchWithAuth
        rechargeForm.reset(); // Limpiar formulario tras éxito
    } catch (error) {
        displaySpecificResult(recargaResultDiv, `Error al recargar: ${error.message}`, true);
    }
}

/** Maneja el formulario de registro de consumo */
async function handleConsume(event) {
    event.preventDefault();
    const formData = new FormData(consumeForm);
    const uid = formData.get('codigoUid');
    const body = new URLSearchParams(formData);
    // Limpiar resultados previos de esta sección y el general
    clearSpecificResults();
    resultArea.innerHTML = ''; resultArea.className = '';

    if (!formData.get('idFestival')) {
        displaySpecificResult(consumoResultDiv, "El campo 'ID Festival' es obligatorio.", true);
        return;
    }

    displaySpecificResult(consumoResultDiv, `Intentando registrar consumo en pulsera UID: ${uid}...`, false);
    resultArea.innerHTML = `Intentando registrar consumo en pulsera UID: ${uid}...`;

    try {
        const data = await fetchWithAuth(`${API_BASE_URL}/pos/pulseras/${uid}/consumir`, { method: 'POST', body: body });
        // *** NUEVO: Mostrar resultado específico ***
        if (data && data.saldo !== undefined) {
            const nuevoSaldoFormateado = data.saldo.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });
            displaySpecificResult(consumoResultDiv, `Consumo registrado. Nuevo Saldo: ${nuevoSaldoFormateado}`, false);
        } else {
            displaySpecificResult(consumoResultDiv, "Consumo procesado, pero no se recibió el nuevo saldo.", true);
        }
        // displayResult(data, false); // Ya se llama dentro de fetchWithAuth
        consumeForm.reset(); // Limpiar formulario tras éxito
    } catch (error) {
        displaySpecificResult(consumoResultDiv, `Error al consumir: ${error.message}`, true);
    }
}

// --- Inicialización ---
document.addEventListener('DOMContentLoaded', () => {
    loadToken(); // Intentar cargar token al inicio

    // Asignar manejadores a los formularios y botones
    loginForm.addEventListener('submit', handleLogin);
    logoutButton.addEventListener('click', handleLogout);
    checkBalanceForm.addEventListener('submit', handleCheckBalance);
    rechargeForm.addEventListener('submit', handleRecharge);
    consumeForm.addEventListener('submit', handleConsume);
});
