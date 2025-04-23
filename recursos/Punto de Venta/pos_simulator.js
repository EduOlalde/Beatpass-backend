// Definir la URL base de la API (¡Ajusta si es necesario!)
const API_BASE_URL = 'http://localhost:8080/BeatpassTFG/api';
let jwtToken = null; // Variable para almacenar el token JWT

// --- Obtener Elementos del DOM ---
const loginForm = document.getElementById('loginForm');
const loginStatus = document.getElementById('loginStatus');
const logoutButton = document.getElementById('logoutButton');
const posOperationsDiv = document.getElementById('posOperations');
const resultArea = document.getElementById('resultArea');
const checkBalanceForm = document.getElementById('checkBalanceForm');
const rechargeForm = document.getElementById('rechargeForm');
const consumeForm = document.getElementById('consumeForm');
const associateForm = document.getElementById('associateForm');

// --- Funciones Auxiliares ---

/**
 * Almacena el token JWT en memoria y localStorage.
 * Actualiza la interfaz de usuario.
 * @param {string} token - El token JWT recibido.
 */
function storeToken(token) {
    jwtToken = token;
    localStorage.setItem('jwtToken', token); // Guardar en localStorage para persistencia
    updateLoginStatus(true);
    console.log("Token JWT almacenado.");
}

/**
 * Limpia el token JWT de memoria y localStorage.
 * Actualiza la interfaz de usuario.
 */
function clearToken() {
    jwtToken = null;
    localStorage.removeItem('jwtToken'); // Limpiar de localStorage
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
        loginStatus.className = 'success'; // Clase CSS para éxito
        posOperationsDiv.style.display = 'block'; // Mostrar operaciones POS
        logoutButton.style.display = 'inline-block'; // Mostrar botón logout
        loginForm.style.display = 'none'; // Ocultar formulario de login
    } else {
        loginStatus.textContent = 'No autenticado';
        loginStatus.className = 'error'; // Clase CSS para error/no autenticado
        posOperationsDiv.style.display = 'none'; // Ocultar operaciones POS
        logoutButton.style.display = 'none'; // Ocultar botón logout
        loginForm.style.display = 'block'; // Mostrar formulario de login
    }
}

/**
 * Muestra resultados o mensajes de error en el área designada del HTML.
 * @param {any} data - Los datos a mostrar (objeto, string).
 * @param {boolean} [isError=false] - Indica si es un mensaje de error.
 */
function displayResult(data, isError = false) {
    resultArea.innerHTML = ''; // Limpiar contenido previo
    const content = document.createElement('div');
    // Formatear objeto como JSON para legibilidad
    content.textContent = typeof data === 'object' ? JSON.stringify(data, null, 2) : data;
    if (isError) {
        content.className = 'error'; // Aplicar clase CSS de error
        console.error("API Error:", data); // Loguear error en consola
    } else {
        content.className = 'success'; // Aplicar clase CSS de éxito
        console.log("API Success:", data); // Loguear éxito en consola
    }
    resultArea.appendChild(content);
}

/**
 * Realiza una llamada fetch a la API, añadiendo el token JWT si está disponible.
 * Maneja la respuesta y posibles errores.
 * @param {string} url - La URL completa del endpoint de la API.
 * @param {object} [options={}] - Opciones para la función fetch (method, body, etc.).
 * @returns {Promise<any>} - Promesa que resuelve con los datos de la respuesta o rechaza con error.
 */
async function fetchWithAuth(url, options = {}) {
    // Clonar cabeceras existentes o crear un objeto vacío
    const headers = {
        ...(options.headers || {}),
    };

    // Añadir token de autorización si existe
    if (jwtToken) {
        headers['Authorization'] = `Bearer ${jwtToken}`;
    }

    // Determinar Content-Type basado en el tipo de body
    if (options.body) {
        if (options.body instanceof URLSearchParams) {
            // Si es form-urlencoded, asegurarse que la cabecera esté (o añadirla)
            if (!headers['Content-Type']) {
                headers['Content-Type'] = 'application/x-www-form-urlencoded';
            }
        } else if (typeof options.body === 'object') {
            // **NUEVO**: Si es un objeto JS (no FormData/URLSearchParams), asumir JSON
            if (!headers['Content-Type']) {
                headers['Content-Type'] = 'application/json';
            }
            // Convertir el objeto a string JSON solo si no es ya un string
            if (typeof options.body !== 'string') {
                options.body = JSON.stringify(options.body);
            }
        }
        // Podrías añadir más casos para otros tipos de body si fuera necesario
    }


    // Configurar opciones finales para fetch
    const fetchOptions = {
        ...options,
        headers: headers
    };

    console.log(`Fetching: ${url}`, fetchOptions); // Loguear petición para depuración

    try {
        const response = await fetch(url, fetchOptions);

        // Intentar obtener datos de la respuesta (JSON o texto)
        let responseData = null;
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            try {
                responseData = await response.json(); // Intentar parsear como JSON
            } catch (jsonError) {
                // Si falla el parseo JSON (ej. respuesta vacía o mal formada)
                console.warn("Fallo al parsear JSON de la respuesta, obteniendo texto.", jsonError);
                try {
                    responseData = await response.text(); // Intentar obtener como texto
                } catch (textError) {
                    console.error("Fallo también al obtener texto de la respuesta.", textError);
                    responseData = `Error ${response.status}: Respuesta no procesable.`; // Mensaje genérico
                }
            }
        } else {
            try {
                responseData = await response.text(); // Obtener como texto si no es JSON
            } catch (textError) {
                console.error("Fallo al obtener texto de la respuesta no JSON.", textError);
                responseData = `Error ${response.status}: Respuesta no procesable.`;
            }
        }

        // Verificar si la respuesta HTTP fue exitosa (status 2xx)
        if (!response.ok) {
            console.error(`HTTP Error: ${response.status} ${response.statusText}`, responseData);
            // Extraer mensaje de error del cuerpo JSON si existe, si no usar statusText
            const errorMessage = (responseData && typeof responseData === 'object' && responseData.error)
                ? responseData.error // Mensaje de error específico del backend
                : (responseData && typeof responseData === 'string' && responseData.length < 200 ? responseData : `Error ${response.status}: ${response.statusText}`); // Mensaje genérico o texto corto

            displayResult(errorMessage, true); // Mostrar error en la interfaz
            throw new Error(errorMessage); // Lanzar error para detener la ejecución
        }

        console.log("Fetch successful", responseData); // Loguear respuesta exitosa
        return responseData; // Devolver los datos

    } catch (error) {
        console.error('Error en fetchWithAuth:', error);
        // Mostrar error de red o el error ya lanzado por !response.ok
        if (!error.message.startsWith("Error ")) { // Evitar duplicar mensajes de error HTTP
            displayResult(`Error de red o fetch: ${error.message}`, true);
        }
        throw error; // Re-lanzar para que la función llamante sepa que falló
    }
}

// --- Manejadores de Eventos ---

/** Maneja el envío del formulario de login */
async function handleLogin(event) {
    event.preventDefault(); // Evitar recarga de página
    const formData = new FormData(loginForm);
    const email = formData.get('email');
    const password = formData.get('password');

    // **CAMBIO**: Crear objeto para enviar como JSON
    const credentials = {
        email: email,
        password: password
    };

    displayResult("Intentando iniciar sesión..."); // Mensaje inicial

    try {
        // Llamar a fetchWithAuth, pasando el objeto 'credentials' en el body
        // fetchWithAuth lo convertirá a JSON y establecerá Content-Type: application/json
        const data = await fetchWithAuth(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            body: credentials // Enviar el objeto directamente
        });

        // Verificar si la respuesta contiene el token esperado
        if (data && data.token) {
            storeToken(data.token); // Almacenar el token
            displayResult("Login exitoso.", false); // Mostrar éxito
        } else {
            // Caso raro: respuesta 200 OK pero sin token
            console.error("Respuesta de login inesperada (sin token):", data);
            displayResult("Error: Respuesta de login inesperada del servidor.", true);
            clearToken(); // Asegurarse de limpiar cualquier token previo
        }
    } catch (error) {
        // El error ya se mostró en displayResult dentro de fetchWithAuth
        clearToken(); // Limpiar token en caso de error de login
    }
}

/** Maneja el clic en el botón de logout */
function handleLogout() {
    clearToken(); // Limpiar el token
    displayResult("Sesión cerrada."); // Mostrar mensaje
}

/** Maneja la consulta de datos de una pulsera */
async function handleCheckBalance(event) {
    event.preventDefault();
    const uid = document.getElementById('checkUid').value;
    if (!uid) {
        displayResult("Por favor, introduce un UID de pulsera.", true);
        return;
    }
    displayResult(`Consultando pulsera UID: ${uid}...`);
    try {
        // Llamada GET al endpoint específico de la pulsera
        const data = await fetchWithAuth(`${API_BASE_URL}/pos/pulseras/${uid}`, {
            method: 'GET'
        });
        displayResult(data, false); // Mostrar datos recibidos
    } catch (error) {
        // El error ya fue mostrado por fetchWithAuth
    }
}

/** Maneja el formulario de recarga de pulsera */
async function handleRecharge(event) {
    event.preventDefault();
    const formData = new FormData(rechargeForm);
    const uid = formData.get('codigoUid');
    // Crear cuerpo como URLSearchParams ya que el endpoint espera form-urlencoded
    const body = new URLSearchParams(formData);

    displayResult(`Intentando recargar pulsera UID: ${uid}...`);
    try {
        // Llamada POST al endpoint de recarga
        const data = await fetchWithAuth(`${API_BASE_URL}/pos/pulseras/${uid}/recargar`, {
            method: 'POST',
            body: body // Enviar como form-urlencoded
        });
        displayResult(data, false); // Mostrar respuesta (DTO de pulsera actualizada)
        // Podrías limpiar el formulario aquí si quieres: rechargeForm.reset();
    } catch (error) {
        // El error ya fue mostrado
    }
}

/** Maneja el formulario de registro de consumo */
async function handleConsume(event) {
    event.preventDefault();
    const formData = new FormData(consumeForm);
    const uid = formData.get('codigoUid');
    // Crear cuerpo como URLSearchParams
    const body = new URLSearchParams(formData);

    // Validar que idFestival no esté vacío (importante para el backend)
    if (!formData.get('idFestival')) {
        displayResult("El campo 'ID Festival' es obligatorio para registrar un consumo.", true);
        return;
    }

    displayResult(`Intentando registrar consumo en pulsera UID: ${uid}...`);
    try {
        // Llamada POST al endpoint de consumo
        const data = await fetchWithAuth(`${API_BASE_URL}/pos/pulseras/${uid}/consumir`, {
            method: 'POST',
            body: body // Enviar como form-urlencoded
        });
        displayResult(data, false); // Mostrar respuesta (DTO de pulsera actualizada)
        // Podrías limpiar el formulario aquí: consumeForm.reset();
    } catch (error) {
        // El error ya fue mostrado
    }
}

/** Maneja el formulario de asociación de pulsera a entrada */
async function handleAssociate(event) {
    event.preventDefault();
    const formData = new FormData(associateForm);
    // Crear cuerpo como URLSearchParams
    const body = new URLSearchParams(formData);

    displayResult(`Intentando asociar pulsera UID: ${formData.get('codigoUid')}...`);
    try {
        // Llamada POST al endpoint de asociación
        const data = await fetchWithAuth(`${API_BASE_URL}/pos/asociar-pulsera`, {
            method: 'POST',
            body: body // Enviar como form-urlencoded
        });
        displayResult(data, false); // Mostrar respuesta (DTO de pulsera asociada)
        // Podrías limpiar el formulario aquí: associateForm.reset();
    } catch (error) {
        // El error ya fue mostrado
    }
}


// --- Inicialización ---
// Añadir listeners a los eventos cuando el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', () => {
    loadToken(); // Intentar cargar token al inicio

    // Asignar manejadores a los formularios y botones
    loginForm.addEventListener('submit', handleLogin);
    logoutButton.addEventListener('click', handleLogout);
    checkBalanceForm.addEventListener('submit', handleCheckBalance);
    rechargeForm.addEventListener('submit', handleRecharge);
    consumeForm.addEventListener('submit', handleConsume);
    associateForm.addEventListener('submit', handleAssociate);
});