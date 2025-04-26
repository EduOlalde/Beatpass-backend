# Beatpass TFG

## Descripción

**Beatpass** es un proyecto de Fin de Grado (TFG) que consiste en una plataforma web desarrollada con Jakarta EE para la gestión y venta de entradas para festivales de música. El sistema ofrece un servicio especializado para promotores de eventos, permitiéndoles crear y administrar sus festivales, definir tipos de entrada, gestionar ventas y asistentes.

Una característica clave es la integración con un sistema simulado de pulseras NFC para pagos *cashless* dentro del festival, gestionado a través de una API de Punto de Venta (POS).

El proyecto incluye:

* **Paneles Web de Gestión:** Interfaces separadas para Administradores (gestión global) y Promotores (gestión de sus propios festivales).
* **API RESTful:** Endpoints para autenticación, operaciones públicas de venta y nominación, y operaciones del POS (consulta, recarga y consumo con pulseras).
* **Seguridad:** Autenticación basada en sesión HTTP para los paneles web y autenticación basada en JWT (JSON Web Tokens) para la API REST del POS. Contraseñas hasheadas con bcrypt.
* **Funcionalidades Adicionales:** Generación de códigos QR únicos para entradas, gestión de stock, asociación de pulseras NFC a entradas.

## Características Principales

* **Gestión de Usuarios:** Roles diferenciados (ADMIN, PROMOTOR, CAJERO) con permisos específicos. Creación y activación/desactivación de cuentas. Cambio de contraseña obligatorio inicial para promotores.
* **Gestión de Festivales:**
    * Creación y edición de festivales por promotores y administradores.
    * Gestión de estados del festival (BORRADOR, PUBLICADO, CANCELADO, FINALIZADO) por administradores.
    * Listado de festivales propios para promotores y listado global/filtrado para administradores.
* **Gestión de Entradas:**
    * Creación, edición y eliminación de tipos de entrada por festival (precio, stock, descripción).
    * Generación automática de entradas individuales (`EntradaAsignada`) con código QR único al realizar una compra.
    * Gestión de stock de entradas.
* **Proceso de Venta y Nominación:**
    * API pública para consultar detalles de festivales y tipos de entrada disponibles.
    * API pública para realizar compras, registrando al asistente (o creándolo si no existe).
    * API pública para nominar una entrada comprada a un asistente específico usando su código QR.
* **Gestión de Asistentes:**
    * Registro automático al comprar o nominar.
    * Listado y búsqueda de asistentes (panel de admin).
    * Edición de datos básicos de asistentes (panel de admin).
    * Listado de asistentes por festival para promotores.
* **Sistema Cashless (Pulseras NFC):**
    * Asociación de pulseras NFC a entradas asignadas y nominadas.
    * API POS (protegida por JWT) para:
        * Consultar datos y saldo de una pulsera por su UID.
        * Registrar recargas de saldo.
        * Registrar consumos, validando saldo y pertenencia al festival.
* **Seguridad:**
    * Hashing de contraseñas (bcrypt).
    * Autenticación por sesión HTTP (paneles web).
    * Autenticación por JWT (API POS).
    * Filtro CORS configurable (`web.xml`).
    * Autorización basada en roles en los endpoints.

## Tecnologías Utilizadas

* **Backend:**
    * Lenguaje: Java 21
    * Plataforma: Jakarta EE 9.1
    * API Web: JAX-RS 3.0 (Implementación: Jersey 3.1.5)
    * Framework Web (Paneles): Jakarta Server Pages (JSP) 3.0, JSTL 3.0
    * Persistencia: Jakarta Persistence API (JPA) 3.0 (Proveedor: Hibernate 6.4.4.Final)
    * Base de Datos: MySQL 8 / MariaDB 10+
    * Servidor de Aplicaciones: Apache Tomcat 10.1 (o compatible)
    * Autenticación JWT: JJWT 0.11.5
    * Hashing Contraseñas: jBCrypt 0.4
    * Códigos QR: ZXing (Core, JavaSE) 3.5.3
    * Logging: SLF4J API 2.0.7 + Logback Classic 1.4.7
    * Filtro CORS: Jetty CrossOriginFilter 11.0.20
    * Construcción/Dependencias: Apache Maven
* **Frontend (Simuladores):**
    * HTML5
    * CSS3 (Tailwind CSS vía CDN y estilos personalizados)
    * JavaScript (Vanilla JS para llamadas API y lógica de UI)
* **Base de Datos:**
    * SQL (Script `beatpasstfg_db.sql` para MariaDB/MySQL)

## Configuración y Puesta en Marcha Local

**Prerrequisitos:**

* JDK 21 o superior.
* Apache Maven 3.x.
* Servidor de Base de Datos MySQL (8.x) o MariaDB (10.4+).
* Servidor de Aplicaciones compatible con Jakarta EE 9.1 Web Profile (ej: Apache Tomcat 10.1).

**Pasos:**

1.  **Clonar el Repositorio:**
    ```bash
    git clone [https://github.com/tu-usuario/BeatpassTFG.git](https://github.com/tu-usuario/BeatpassTFG.git)
    cd BeatpassTFG
    ```
2.  **Configurar Base de Datos:**
    * Crea una base de datos (ej: `beatpasstfg_db`).
    * Ejecuta el script `beatpasstfg_db.sql` para crear las tablas y cargar datos iniciales.
    * Configura las siguientes **variables de entorno** para que la aplicación se conecte a tu base de datos:
        * `TFG_DB_URL`: La URL JDBC (ej: `jdbc:mysql://localhost:3306/beatpasstfg_db?useSSL=false&serverTimezone=UTC`)
        * `TFG_DB_USER`: Tu usuario de base de datos.
        * `TFG_DB_PASSWORD`: La contraseña de tu usuario de base de datos.
3.  **Configurar Clave Secreta JWT:**
    * Define la siguiente **variable de entorno** con una clave secreta segura (mínimo 32 caracteres):
        * `TFG_TOKEN_KEY`: TuClaveSecretaLargaYComplejaParaJWT
4.  **Construir el Proyecto:**
    * Navega al directorio raíz del proyecto (donde está `pom.xml`).
    * Ejecuta Maven para compilar y empaquetar la aplicación:
        ```bash
        mvn clean package
        ```
    * Esto generará un archivo `BeatpassTFG.war` en el directorio `target/`.
5.  **Desplegar en Tomcat:**
    * Copia el archivo `BeatpassTFG.war` al directorio `webapps` de tu instalación de Tomcat.
    * Inicia el servidor Tomcat. La aplicación debería desplegarse automáticamente.
6.  **Acceder a la Aplicación:**
    * **Paneles Web:** Abre tu navegador y ve a `http://localhost:8080/BeatpassTFG/` (o el puerto y contexto que use tu Tomcat). Deberías ver la página de login (`login.jsp`).
        * Admin: `admin@beatpass.com` / `password` (según datos iniciales)
        * Promotor: `info@eventoslunallena.es` / `pass` (requiere cambio)
    * **API:** Los endpoints de la API estarán disponibles bajo `http://localhost:8080/BeatpassTFG/api/`.
7.  **Simuladores Frontend:**
    * Los archivos `index.html`, `festival.html`, `pos.html` y sus JS/CSS asociados se pueden abrir directamente en el navegador o, preferiblemente, servirse con una extensión como "Live Server" en VS Code.
    * **Importante:** Asegúrate de que la constante `API_BASE_URL` en `festival_simulator.js` y `pos_simulator.js` apunte a la URL correcta de tu backend local (`http://localhost:8080/BeatpassTFG/api`).
    * Ajusta el `FESTIVAL_ID` en `festival_simulator.js` y el `posFestivalId` en `pos.html` para que coincidan con IDs válidos en tu base de datos.
    * Configura CORS en `web.xml` para permitir el origen desde donde sirves los simuladores (ej: `http://localhost:5500` si usas Live Server).

## Despliegue

Según la memoria del proyecto:

* **Backend (BeatpassTFG.war):** Desplegado en [Render](https://render.com/) en la URL (https://daw2-tfg-beatpass.onrender.com)
* **Base de Datos:** Alojada en [Alwaysdata](https://www.alwaysdata.com/).
* **Frontend (Simuladores):** Desplegado en [GitHub Pages](https://pages.github.com/).

**Nota:** Para el despliegue en producción, es crucial configurar las variables de entorno (`TFG_DB_URL`, `TFG_DB_USER`, `TFG_DB_PASSWORD`, `TFG_TOKEN_KEY`) en el proveedor de hosting (Render) y ajustar la `API_BASE_URL` en los archivos JavaScript del frontend para que apunten a la URL pública del backend en Render. También se debe actualizar la configuración CORS en `web.xml` para permitir solicitudes desde el dominio de GitHub Pages.

## Autor

* **Eduardo Olalde Cruz**

## Licencia

MIT License
