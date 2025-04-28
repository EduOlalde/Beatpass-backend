# --- Fase 1: Definir la base ---
 # Usamos una imagen oficial de Tomcat que ya incluye Java 21 (JDK).
 # Busca la etiqueta exacta en Docker Hub (https://hub.docker.com/_/tomcat) si necesitas otra versión.
 # '10.1-jdk21' significa Tomcat 10.1 con JDK 21.
 FROM tomcat:10.1-jdk21
 
 # --- Fase 2: Preparar Tomcat (Opcional pero recomendado) ---
 # Tomcat trae algunas aplicaciones web por defecto (manager, examples, etc.).
 # Para un despliegue limpio, es buena práctica eliminarlas.
 # Esto libera algo de espacio y reduce la superficie de ataque.
 RUN rm -rf /usr/local/tomcat/webapps/*
 
 # --- Fase 3: Copiar tu aplicación ---
 # Aquí es donde copiamos el archivo .war generado por Maven.
 # Reemplaza 'target/tu-aplicacion.war' con la ruta y nombre REAL de tu archivo .war.
 # Lo copiamos como 'ROOT.war' dentro de la carpeta 'webapps' de Tomcat.
 # Al llamarlo ROOT.war, tu aplicación será accesible directamente en la raíz
 # (ej: http://localhost:8080/) en lugar de (http://localhost:8080/tu-aplicacion/).
 # ¡¡¡ IMPORTANTE: Ajusta el nombre 'tu-aplicacion.war' al nombre real de tu WAR !!!
 COPY target/BeatpassTFG.war /usr/local/tomcat/webapps/ROOT.war
 
 # --- Fase 4: Exponer el puerto ---
 # Informa a Docker que el contenedor escuchará en el puerto 8080 (el puerto por defecto de Tomcat).
 # Esto no publica el puerto al exterior, solo lo documenta. La publicación se hace al ejecutar el contenedor.
 EXPOSE 8080
 
 # --- Fase 5: Comando de ejecución (Opcional si la base ya lo tiene) ---
 # La imagen base 'tomcat' ya tiene configurado el comando para iniciar el servidor.
 # Por lo tanto, esta línea CMD no es estrictamente necesaria, pero no hace daño ponerla
 # para ser explícitos o si usaras una imagen base diferente.
 # CMD ["catalina.sh", "run"]