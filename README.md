
# Beatpass

## Descripción

**Beatpass** nació como un proyecto de Fin de Grado (TFG), y ha continuado siendo desarrollado para convertirse en una plataforma web para la gestión y venta de entradas para festivales de música. El sistema ofrece un servicio especializado para promotores de eventos, permitiéndoles crear y administrar sus festivales, definir tipos de entrada, gestionar ventas y asistentes.

Actualmente, el proyecto se encuentra en proceso de migración de sus vistas de gestión, de Jakarta Server Pages (JSP) a una SPA en React. Esto desacopla la lógica de presentación del backend, que ahora funciona como una API RESTful.

Una característica clave es la integración con un sistema simulado de pulseras NFC para pagos cashless dentro del festival, gestionado a través de una API POS. También se incluye el envío automático de entradas en PDF por correo electrónico tras la compra o nominación.

## El proyecto incluye

- **API RESTful (Backend)**: Jakarta EE + JAX-RS, lógica de negocio, persistencia, JWT, roles, endpoints de gestión y venta.
- **Vistas de Gestión (SPA en React)**: Interfaz para administración y promotores.
- **Vistas Públicas (SPA en React)**: Nominación pública de entradas.
- **Simuladores (Frontend Web Estático)**: Compra pública y simulación de POS.
- **Seguridad**: JWT, bcrypt, autorización por roles.
- **Funcionalidades adicionales**: QR, stock, pulseras NFC, PDFs por email.

## Características Principales

### Gestión de Usuarios
- Roles: ADMIN, PROMOTOR, CAJERO.
- Alta, baja, edición y listado de usuarios.
- Cambio de contraseña inicial obligatorio.
- Gestión por AdminResource.

### Gestión de Festivales
- CRUD de festivales.
- Estados: BORRADOR, PUBLICADO, CANCELADO, FINALIZADO.
- Acceso público a festivales PUBLICADO.

### Tipos de Entrada
- CRUD de tipos por festival.
- Acceso público a tipos de entrada.

### Entradas Individuales
- Generación con QR al comprar.
- Asociación con pulsera NFC (marca como usada).
- Cancelación con reversión de stock.
- Nominación pública o por promotor.

### Proceso de Venta y Nominación
- API pública con Stripe para pagos.
- Confirmación de compra y generación de entradas.
- Envío de entradas y nominaciones por email (PDF).

### Gestión de Compradores y Asistentes
- Registro automático.
- Edición y consulta desde panel.
- Listado por festival para promotores.

### Sistema Cashless (Pulseras NFC)
- API POS protegida por JWT.
- Asociación de UID a entrada.
- Consulta de saldo, recargas y consumos.

### Seguridad
- Hashing con jBCrypt.
- JWT.
- Filtro CORS (CorsFilter.java).
- Autorización por roles.
- Excepciones globales (GenericExceptionMapper).

## Tecnologías Utilizadas

### Backend
- Java 21, Jakarta EE 9.1
- JAX-RS 3.0, JPA 3.0 (Hibernate 6.4.4.Final)
- MariaDB, WildFly Bootable JAR
- JWT: JJWT 0.11.5
- Hashing: jBCrypt 0.4
- QR: ZXing 3.5.3
- PDF: Apache PDFBox 3.0.2
- Email: Angus Mail 2.0.2
- Logging: SLF4J + Logback
- DTO Mapper: MapStruct 1.5.5.Final
- Pagos: Stripe SDK Java 25.11.0
- Build: Apache Maven

### Frontend (SPA React)
- React.js, Tailwind CSS

### Base de Datos
- MariaDB (script: `recursos/beatpasstfg_db.sql`)

## Configuración y Puesta en Marcha Local

### Prerrequisitos
- JDK 21+
- Apache Maven 3.x
- Docker

### Backend

1. **Clonar el repositorio**:

```bash
git clone https://github.com/EduOlalde/DAW2-TFG-Beatpass.git
cd DAW2-TFG-Beatpass
```

2. **Configurar variables de entorno**:

Crear archivo `docker.env` en la raíz con:

- `TFG_DB_URL`, `TFG_DB_USER`, `TFG_DB_PASSWORD`
- `TFG_TOKEN_KEY` (mín. 32 caracteres)
- `STRIPE_SECRET_KEY`
- Variables de email (`MAIL_...`)
- `APP_BASE_URL`

3. **Construcción y ejecución**:

```bash
docker build -t beatpass .
docker run -p 8080:8080 --env-file docker.env beatpass
```

4. **Acceder a la API**:  
`http://localhost:8080/api/`

## Despliegue

- **Backend**: Render (Dockerfile).
- **Base de Datos**: Alwaysdata.

**Nota**: Configurar `APP_BASE_URL` en Render. Ajustar `CorsFilter.java` con origen del frontend.

## Autor
Eduardo Olalde Cruz

## Licencia
MIT License
