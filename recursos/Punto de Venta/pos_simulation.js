// URL base de tu API backend
const API_BASE_URL = '/BeatpassTFG/api'; // Ajusta si tu contexto es diferente

// Elementos del DOM
const loginSection = document.getElementById('login-section');
const posOperationsSection = document.getElementById('pos-operations');
const messageArea = document.getElementById('message-area');
const loginForm = document.getElementById('login-form');
const searchWristbandForm = document.getElementById('search-wristband-form');
const wristbandDetailsSection = document.getElementById('wristband-details');
const operationFormsSection = document.getElementById('operation-forms');
const associateForm = document.getElementById('associate-form');
const topupForm = document.getElementById('topup-form');
const consumeForm = document.getElementById('consume-form');
const logoutButton = document.getElementById('logout-button');

// Estado de la aplicación
let jwtToken = localStorage.getItem('posToken'); // Intentar recuperar token al cargar
let currentWristband = null; // Guardará los datos de la pulsera buscada

// --- Funciones de Utilidad ---

/** Muestra un mensaje en el área designada */
function showMessage(message, isError = false) {
    messageArea.textContent = message;
    messageArea.className = `message ${isError ? 'message-error' : 'message-success'}`;
    messageArea.style.display = 'block';
    // Ocultar mensaje después de unos segundos
    setTimeout(() => { messageArea.style.display = 'none'; }, 5000);
}

/** Realiza una petición fetch a la API con manejo de token y errores */
async function apiFetch(endpoint, options = {}) {
    const headers = {
        'Accept': 'application/json',
        ...options.headers, // Permite pasar otras cabeceras
    };
    // Añadir token si lo tenemos y no es la petición de login
    if (jwtToken && endpoint !== '/auth/login') {
        headers['Authorization'] = `Bearer ${jwtToken}`;
    }
     // Si enviamos datos de formulario, Content-Type lo establece el navegador
     // Si enviamos JSON, lo especificamos
     if (options.body && !(options.body instanceof FormData)) {
         headers['Content-Type'] = 'application/json';
         options.body = JSON.stringify(options.body); // Convertir objeto a JSON
     }

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, { ...options, headers });

        if (!response.ok) {
            let errorData;
            try {
                errorData = await response.json(); // Intenta leer el cuerpo del error JSON
            } catch (e) {
                errorData = { error: `HTTP ${response.status}: ${response.statusText}` }; // Error genérico si no hay JSON
            }
            throw new Error(errorData.error || `Error ${response.status}`);
        }

        // Si la respuesta no tiene contenido (ej: 204 No Content), devolver null
        if (response.status === 204) {
            return null;
        }
        // Si esperamos JSON, lo parseamos
        if (options.responseType !== 'text') {
             return await response.json();
        }
        // Si esperamos texto plano
        return await response.text();

    } catch (error) {
        console.error(`Error en API fetch a ${endpoint}:`, error);
        throw error; // Relanzar para que el llamador lo maneje
    }
}

/** Actualiza la interfaz según el estado de autenticación */
function updateUIAuthState() {
    if (jwtToken) {
        loginSection.style.display = 'none';
        posOperationsSection.style.display = 'block';
        wristbandDetailsSection.style.display = 'none'; // Ocultar detalles al inicio/logout
        operationFormsSection.style.display = 'none'; // Ocultar forms al inicio/logout
    } else {
        loginSection.style.display = 'block';
        posOperationsSection.style.display = 'none';
    }
}

/** Actualiza la sección de detalles de la pulsera */
function displayWristbandDetails(wristbandData) {
    if (!wristbandData) {
        wristbandDetailsSection.style.display = 'none';
        operationFormsSection.style.display = 'none';
        return;
    }
    currentWristband = wristbandData; // Guardar datos actuales

    document.getElementById('detail-uid').textContent = wristbandData.codigoUid || 'N/A';
    document.getElementById('detail-saldo').textContent = wristbandData.saldo?.toFixed(2) || '0.00'; // Formatear saldo
    document.getElementById('detail-status').textContent = wristbandData.activa ? 'Activa' : 'Inactiva';
    document.getElementById('detail-entrada-id').textContent = wristbandData.idEntradaAsignada || 'No asociada';
    document.getElementById('detail-asistente').textContent = wristbandData.nombreAsistente ? `${wristbandData.nombreAsistente} (${wristbandData.emailAsistente})` : 'No nominado';
    document.getElementById('detail-festival').textContent = wristbandData.nombreFestival ? `${wristbandData.nombreFestival} (ID: ${wristbandData.idFestival})` : 'N/A';

    // Rellenar UIDs/IDs en formularios de operación
    document.getElementById('associate-uid').value = wristbandData.codigoUid;
    document.getElementById('topup-uid').value = wristbandData.codigoUid;
    document.getElementById('consume-uid').value = wristbandData.codigoUid;
    document.getElementById('consume-festival-id').value = wristbandData.idFestival || ''; // Necesario para consumo

    wristbandDetailsSection.style.display = 'block';
    operationFormsSection.style.display = 'block'; // Mostrar formularios de operación
}

// --- Manejadores de Eventos ---

/** Maneja el envío del formulario de login */
async function handleLogin(event) {
    event.preventDefault();
    const email = loginForm.email.value;
    const password = loginForm.password.value;
    messageArea.style.display = 'none'; // Ocultar mensajes previos

    try {
        const data = await apiFetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }, // AuthResource espera JSON
            body: JSON.stringify({ email, password })
        });
        jwtToken = data.token;
        localStorage.setItem('posToken', jwtToken); // Guardar token
        updateUIAuthState();
        showMessage('Login exitoso.');
        loginForm.reset();
    } catch (error) {
        jwtToken = null;
        localStorage.removeItem('posToken');
        updateUIAuthState();
        showMessage(`Error de login: ${error.message}`, true);
    }
}

/** Maneja el envío del formulario de búsqueda de pulsera */
async function handleSearchWristband(event) {
    event.preventDefault();
    const codigoUid = searchWristbandForm.codigoUid.value;
    messageArea.style.display = 'none';
    wristbandDetailsSection.style.display = 'none'; // Ocultar detalles previos
    operationFormsSection.style.display = 'none';
    currentWristband = null;

    if (!codigoUid) {
        showMessage('Por favor, introduce un UID.', true);
        return;
    }

    try {
        // Llamamos al nuevo endpoint que devuelve el DTO completo
        const wristbandData = await apiFetch(`/pos/pulseras/${codigoUid}`);
        displayWristbandDetails(wristbandData);
        showMessage(`Pulsera ${codigoUid} encontrada.`);
    } catch (error) {
        showMessage(`Error buscando pulsera ${codigoUid}: ${error.message}`, true);
    }
}

/** Maneja el envío del formulario de asociación */
async function handleAssociate(event) {
     event.preventDefault();
     messageArea.style.display = 'none';
     const formData = new FormData(associateForm);
     const codigoUid = formData.get('codigoUid');
     const idEntradaAsignada = formData.get('idEntradaAsignada');

     try {
         const result = await apiFetch('/pos/asociar-pulsera', {
             method: 'POST',
             body: formData // Enviar como form data porque el endpoint espera eso
         });
         showMessage(`Pulsera ${codigoUid} asociada a entrada ${idEntradaAsignada} con éxito.`, false);
         // Opcional: Refrescar datos de la pulsera actual si es la misma
         if (currentWristband && currentWristband.codigoUid === codigoUid) {
             handleSearchWristband({ preventDefault: () => {}, target: { codigoUid: { value: codigoUid } } }); // Simular evento
         }
         associateForm.reset(); // Limpiar campo ID entrada
         document.getElementById('associate-uid').value = codigoUid; // Reponer UID
     } catch (error) {
          showMessage(`Error al asociar: ${error.message}`, true);
     }
}


/** Maneja el envío del formulario de recarga */
async function handleTopup(event) {
    event.preventDefault();
    messageArea.style.display = 'none';
    const formData = new FormData(topupForm);
    const codigoUid = formData.get('codigoUid');

    try {
        const updatedWristband = await apiFetch(`/pos/pulseras/${codigoUid}/recargar`, {
            method: 'POST',
            body: formData // Enviar como form data
        });
        showMessage(`Recarga exitosa para ${codigoUid}. Nuevo saldo: ${updatedWristband.saldo.toFixed(2)}€`, false);
        displayWristbandDetails(updatedWristband); // Actualizar UI con nuevos datos
        topupForm.reset(); // Limpiar formulario
        document.getElementById('topup-uid').value = codigoUid; // Reponer UID
    } catch (error) {
        showMessage(`Error en recarga: ${error.message}`, true);
    }
}

/** Maneja el envío del formulario de consumo */
async function handleConsume(event) {
    event.preventDefault();
    messageArea.style.display = 'none';
    const formData = new FormData(consumeForm);
    const codigoUid = formData.get('codigoUid');

     if (!formData.get('idFestival')) {
         showMessage('Error: No se pudo determinar el festival para el consumo.', true);
         return;
     }

    try {
        const updatedWristband = await apiFetch(`/pos/pulseras/${codigoUid}/consumir`, {
            method: 'POST',
            body: formData // Enviar como form data
        });
        showMessage(`Consumo exitoso para ${codigoUid}. Nuevo saldo: ${updatedWristband.saldo.toFixed(2)}€`, false);
        displayWristbandDetails(updatedWristband); // Actualizar UI
        consumeForm.reset(); // Limpiar formulario
        document.getElementById('consume-uid').value = codigoUid; // Reponer UID
        document.getElementById('consume-festival-id').value = updatedWristband.idFestival || ''; // Reponer ID festival
    } catch (error) {
        showMessage(`Error en consumo: ${error.message}`, true);
    }
}

/** Maneja el logout */
function handleLogout() {
    jwtToken = null;
    localStorage.removeItem('posToken');
    currentWristband = null;
    updateUIAuthState();
    searchWristbandForm.reset();
    associateForm.reset();
    topupForm.reset();
    consumeForm.reset();
    showMessage('Sesión cerrada.');
}

// --- Inicialización y Event Listeners ---

// Actualizar UI al cargar la página basado en si hay token guardado
updateUIAuthState();

// Asignar manejadores
loginForm.addEventListener('submit', handleLogin);
searchWristbandForm.addEventListener('submit', handleSearchWristband);
associateForm.addEventListener('submit', handleAssociate);
topupForm.addEventListener('submit', handleTopup);
consumeForm.addEventListener('submit', handleConsume);
logoutButton.addEventListener('click', handleLogout);

