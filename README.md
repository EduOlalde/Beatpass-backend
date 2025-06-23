# Beatpass TFG

## Descripción

**Beatpass** nació como un proyecto de Fin de Grado (TFG), y ha continuado siendo desarrollado para convertirse en una plataforma web para la gestión y venta de entradas para festivales de música. El sistema ofrece un servicio especializado para promotores de eventos, permitiéndoles crear y administrar sus festivales, definir tipos de entrada, gestionar ventas y asistentes.

Actualmente, el proyecto se encuentra en un proceso de migración de sus vistas de gestión. Originalmente implementadas con Jakarta Server Pages (JSP), estas vistas están siendo reemplazadas por una **Aplicación de Página Única (SPA) desarrollada en React**. Esto implica que la lógica de presentación se ha desacoplado del backend, que ahora funciona principalmente como una **API RESTful**.

Una característica clave es la integración con un sistema simulado de pulseras NFC para pagos *cashless* dentro del festival, gestionado a través de una API de Punto de Venta (POS). El sistema también incluye el **envío automático de entradas en formato PDF por correo electrónico** tras la compra o nominación.

El proyecto incluye:

* **API RESTful (Backend):** Implementado con Jakarta EE y JAX-RS, sirviendo como el cerebro de la aplicación. Gestiona la lógica de negocio, persistencia de datos, autenticación (JWT) y autorización, y proporciona todos los endpoints necesarios para las operaciones de gestión y venta.
* **Vistas de Gestión (Frontend SPA en React - en desarrollo):** Una nueva interfaz de usuario moderna y reactiva para los paneles de administración y promotores, que consume la API RESTful.
* **Vistas Públicas (Frontend SPA en React - en desarrollo):** Interfaces para la nominación pública de entradas, consumiendo la API pública.
* **Simuladores (Frontend Web Estático):** Interfaces simplificadas para simular el proceso de compra pública y operaciones de punto de venta (POS) con pulseras NFC.
* **Seguridad:** Autenticación basada en JWT (JSON Web Tokens) para la API REST, con contraseñas hasheadas con bcrypt. Autorización basada en roles.
* **Funcionalidades Adicionales:** Generación de códigos QR únicos para entradas, gestión de stock, asociación de pulseras NFC a entradas (marcando la entrada como usada), y envío de entradas en PDF por email.

## Características Principales

* **Gestión de Usuarios:**
    * Roles diferenciados: `ADMIN`, `PROMOTOR`, `CAJERO` con permisos específicos.
    * Creación, activación/desactivación de cuentas (gestionado por `AdminResource`).
    * Cambio de contraseña inicial obligatorio para promotores y cajeros.
    * Listado de usuarios por rol (ADMIN, PROMOTOR, CAJERO).
    * Actualización de datos de usuario (nombre, estado).
    * Eliminación de usuarios.
* **Gestión de Festivales:**
    * Creación y edición de festivales por promotores y administradores.
    * Gestión de estados del festival (`BORRADOR`, `PUBLICADO`, `CANCELADO`, `FINALIZADO`) por administradores, con transiciones válidas.
    * Listado de festivales propios para promotores.
    * Listado global/filtrado por estado para administradores.
    * Acceso público a festivales `PUBLICADO` por rango de fechas y detalle.
* **Gestión de Tipos de Entrada:**
    * Creación, edición y eliminación de tipos de entrada por festival (precio, stock, descripción, si requiere nominación).
    * Las operaciones están protegidas, requiriendo que el promotor sea dueño del festival o que el usuario sea un administrador.
    * Consulta de tipos de entrada para festivales públicos.
* **Gestión de Entradas Individuales:**
    * Generación automática de entradas individuales (`Entrada`) con código QR único al realizar una compra.
    * Las entradas se marcan como "USADA" y se registra la fecha de uso cuando se asocia una pulsera NFC.
    * Cancelación de entradas con reversión de stock.
    * Nominación de entradas a asistentes específicos (pública o por promotor).
* **Proceso de Venta y Nominación (Público):**
    * API pública para iniciar el proceso de pago (integración con Stripe para `PaymentIntent`).
    * API pública para confirmar la compra después de un pago exitoso, creando al comprador si es nuevo y generando las entradas.
    * **Envío automático de un correo electrónico al comprador con las entradas adquiridas en formato PDF**.
    * API pública para nominar una entrada a un asistente específico usando su código QR, accesible desde enlaces enviados por email.
    * **Envío automático de un correo electrónico al asistente nominado con su entrada personalizada en formato PDF**.
* **Gestión de Compradores y Asistentes:**
    * Registro automático de compradores al realizar una compra.
    * Registro automático de asistentes al nominar una entrada.
    * Listado y búsqueda de compradores y asistentes desde el panel de administración.
    * Edición de datos básicos de asistentes (nombre, teléfono).
    * Listado de asistentes y compras por festival para promotores.
* **Sistema Cashless (Pulseras NFC):**
    * API POS (protegida por JWT con roles `CAJERO`, `ADMIN`, `PROMOTOR`).
    * Asociación de pulseras NFC a entradas nominadas y activas (esto cambia el estado de la entrada a "USADA").
    * Consulta de datos y saldo de una pulsera por su UID.
    * Registro de recargas de saldo.
    * Registro de consumos, validando saldo y pertenencia al festival.
* **Seguridad:**
    * Hashing de contraseñas con jBCrypt.
    * Autenticación por JWT para la API REST.
    * Filtro CORS configurable (`web.xml` y `CorsFilter.java`).
    * Autorización basada en roles en los endpoints de los recursos JAX-RS (utilizando `SecurityContext`).
    * Manejo global de excepciones para respuestas RESTful (`GenericExceptionMapper`).

## Tecnologías Utilizadas

* **Backend:**
    * Lenguaje: Java 21
    * Plataforma: Jakarta EE 9.1
    * API Web: JAX-RS 3.0 (Implementación: Jersey 3.1.5)
    * Persistencia: Jakarta Persistence API (JPA) 3.0 (Proveedor: Hibernate 6.4.4.Final)
    * Base de Datos: MySQL 8 / MariaDB 10+
    * Servidor de Aplicaciones: Apache Tomcat 10.1 (o compatible)
    * Autenticación JWT: JJWT 0.11.5
    * Hashing Contraseñas: jBCrypt 0.4
    * Códigos QR: ZXing (Core, JavaSE) 3.5.3
    * Generación de PDF: Apache PDFBox 3.0.2
    * Envío de Email: Jakarta Mail API 2.1.2 (Implementación: Angus Mail 2.0.2)
    * Logging: SLF4J API 2.0.7 + Logback Classic 1.4.7
    * Pool de Conexiones: HikariCP 5.1.0
    * Mapper (DTO-Entity): MapStruct 1.5.5.Final
    * Pasarela de pago: Stripe (SDK Java 25.11.0)
    * Build Tool: Apache Maven
* **Frontend (SPA React - en desarrollo):**
    * Framework: React.js
    * Gestión de estado: Context API
    * Routing: React Router
    * Estilos: Tailwind CSS
* **Base de Datos:**
    * SQL (Script `recursos/beatpasstfg_db.sql` para MariaDB/MySQL)

## Configuración y Puesta en Marcha Local

**Prerrequisitos:**

* JDK 21 o superior.
* Apache Maven 3.x.
* Servidor de Base de Datos MySQL (8.x) o MariaDB (10.4+).
* Servidor de Aplicaciones compatible con Jakarta EE 9.1 Web Profile (ej: Apache Tomcat 10.1).
* Un entorno para desarrollar la SPA en React (Node.js, npm/yarn).

**Pasos para el Backend (Jakarta EE):**

1.  **Clonar el Repositorio:**
    ```bash
    git clone [https://github.com/EduOlalde/DAW2-TFG-Beatpass.git](https://github.com/EduOlalde/DAW2-TFG-Beatpass.git)
    cd DAW2-TFG-Beatpass
    ```
2.  **Configurar Base de Datos:**
    * Crea una base de datos (ej: `beatpasstfg_db`).
    * Ejecuta el script `recursos/beatpasstfg_db.sql` para crear las tablas y cargar datos iniciales.
    * Configura las siguientes **variables de entorno** para que la aplicación se conecte a tu base de datos:
        * `TFG_DB_URL`: La URL JDBC (ej: `jdbc:mysql://localhost:3306/beatpasstfg_db?useSSL=false&serverTimezone=UTC`)
        * `TFG_DB_USER`: Tu usuario de base de datos.
        * `TFG_DB_PASSWORD`: La contraseña de tu usuario de base de datos.
3.  **Configurar Clave Secreta JWT:**
    * Define la siguiente **variable de entorno** con una clave secreta segura (mínimo 32 caracteres):
        * `TFG_TOKEN_KEY`: `TuClaveSecretaLargaYComplejaParaJWT`
4.  **Configurar Clave Secreta de Stripe:**
    * Define la siguiente **variable de entorno**:
        * `STRIPE_SECRET_KEY`: `sk_test_XXXXXXXXXXXXXXXXXXXXXXXXX` (Tu clave secreta de Stripe)
5.  **Configurar Variables de Entorno para Email:**
    * `MAIL_SMTP_HOST`: Host del servidor SMTP (ej: `smtp.gmail.com`)
    * `MAIL_SMTP_PORT`: Puerto del servidor SMTP (ej: `587`)
    * `MAIL_SMTP_AUTH`: `true` o `false`
    * `MAIL_SMTP_STARTTLS_ENABLE`: `true` o `false`
    * `MAIL_SMTP_USER`: Usuario para la autenticación SMTP (ej: `tu_correo@gmail.com`)
    * `MAIL_SMTP_PASSWORD`: Contraseña para la autenticación SMTP (¡usa una contraseña de aplicación si es Gmail!)
    * `MAIL_FROM_ADDRESS`: Dirección de email remitente (ej: `noreply@beatpass.com`)
    * `APP_BASE_URL`: URL base de tu aplicación para los enlaces en los correos (ej: `http://localhost:5173/` si tu frontend React corre en ese puerto).

6.  **Construir el Proyecto Backend:**
    * Navega al directorio raíz del proyecto (donde está `pom.xml`).
    * Ejecuta Maven para compilar y empaquetar la aplicación:
        ```bash
        mvn clean package
        ```
    * Esto generará un archivo `BeatpassTFG.war` en el directorio `target/`.
7.  **Desplegar en Tomcat:**
    * Copia el archivo `BeatpassTFG.war` al directorio `webapps` de tu instalación de Tomcat.
    * Inicia el servidor Tomcat. La aplicación debería desplegarse automáticamente.
8.  **Acceder a la API (Backend):**
    * Los endpoints de la API estarán disponibles bajo `http://localhost:8080/BeatpassTFG/api/` (o el puerto y contexto que use tu Tomcat).


## Despliegue

* **Backend (BeatpassTFG.war):** Desplegado en [Render](https://render.com/) en la URL `https://beatpass.onrender.com`.
* **Base de Datos:** Alojada en [Alwaysdata](https://www.alwaysdata.com/).
* **Frontend (Simuladores):** Desplegado en [GitHub Pages](https://pages.github.com/).

**Nota para despliegue en producción:** Es crucial configurar todas las variables de entorno (`TFG_DB_URL`, `TFG_DB_USER`, `TFG_DB_PASSWORD`, `TFG_TOKEN_KEY`, `STRIPE_SECRET_KEY` y las variables de email) en el proveedor de hosting (Render para el backend). Además, la `APP_BASE_URL` en las variables de entorno del backend debe apuntar a la URL pública donde se despliegue el frontend SPA/simuladores, y la configuración CORS en `src/main/java/com/beatpass/security/CorsFilter.java` (y `web.xml`) debe permitir solicitudes desde los dominios de producción del frontend (GitHub Pages, etc.).

## Autor

* **Eduardo Olalde Cruz**

## Licencia

MIT License