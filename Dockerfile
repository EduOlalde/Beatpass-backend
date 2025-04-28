# --- Etapa 1: Build Frontend Assets (Tailwind CSS) ---
# Usamos una imagen de Node.js para instalar Tailwind y generar el CSS
FROM node:18-alpine as frontend-builder
WORKDIR /app-frontend

# Copiar archivos de configuración de Node y Tailwind
# Asegúrate de que estos archivos existan en la raíz de tu proyecto o ajusta las rutas
COPY package*.json ./
COPY tailwind.config.js ./
# Copiar el archivo CSS de entrada para Tailwind
# Asume que está en src/main/webapp/css/input.css
COPY src/main/webapp/css/input.css ./src/main/webapp/css/input.css

# Copiar los archivos fuente que Tailwind necesita escanear (JSPs, HTML, etc.)
# Ajusta estas rutas según la estructura de tu proyecto
COPY src/main/webapp/WEB-INF/jsp ./src/main/webapp/WEB-INF/jsp
COPY src/main/webapp/*.jsp ./src/main/webapp/ # Si tienes JSPs directamente en webapp
COPY src/main/webapp/*.html ./src/main/webapp/ # Si tienes HTMLs

# Instalar dependencias (Tailwind CSS)
RUN npm install

# Generar el archivo CSS optimizado y minificado para producción
# El archivo de salida se guardará en /app-frontend/dist/css/output.css
RUN mkdir -p /app-frontend/dist/css
RUN npx tailwindcss -i ./src/main/webapp/css/input.css -o /app-frontend/dist/css/output.css --minify


# --- Etapa 2: Build Java Application (Maven) ---
# Usamos una imagen de Maven con JDK 17 (ajusta si usas otra versión)
FROM maven:3.8-openjdk-17 as java-builder
WORKDIR /app-java

# Copiar todo el código fuente del proyecto Java/Maven
COPY . .

# Construir el archivo WAR usando Maven
# -DskipTests acelera el build si no necesitas correr tests en Docker
RUN mvn package -DskipTests


# --- Etapa 3: Final Runtime Image (Tomcat) ---
# Usamos la imagen de Tomcat que especificaste
FROM tomcat:10.1-jdk21

# Eliminar aplicaciones web por defecto de Tomcat (recomendado)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el archivo .war generado desde la etapa de build Java
# Asegúrate que 'target/BeatpassTFG.war' es la ruta correcta
COPY --from=java-builder /app-java/target/BeatpassTFG.war /usr/local/tomcat/webapps/ROOT.war

# Copiar el archivo CSS generado desde la etapa de build frontend
# Lo copiamos a la carpeta 'css' dentro de la aplicación desplegada (ROOT)
COPY --from=frontend-builder /app-frontend/dist/css/output.css /usr/local/tomcat/webapps/ROOT/css/output.css

# Exponer el puerto 8080 (ya definido en la imagen base, pero es bueno documentarlo)
EXPOSE 8080

# Comando de inicio (ya definido en la imagen base de Tomcat)
# CMD ["catalina.sh", "run"]
