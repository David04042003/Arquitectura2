# Repositorio de GitHub donde está el código fuente de la API
# (se puede cambiar con --build-arg REPO=... y --build-arg RAMA=...)
ARG REPO=https://github.com/David04042003/Arquitectura2.git
ARG RAMA=main

# ---------- Etapa 1: descargar el código fuente desde GitHub ----------
FROM scratch AS fuente
ARG REPO
ARG RAMA
ADD ${REPO}#${RAMA} /repo

# ---------- Etapa 2: compilar el .jar con Maven ----------
# Equivale a ejecutar "mvn clean package -DskipTests" sin tener Maven instalado
FROM maven:3.9-eclipse-temurin-17 AS compilacion
WORKDIR /compilacion
COPY --from=fuente /repo/API-Calendario/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY --from=fuente /repo/API-Calendario/src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 3: imagen final solo con Java 17 y el .jar ----------
# (imagen multiplataforma: funciona en Windows/Linux x86 y en Mac con chip Apple)
FROM eclipse-temurin:17-jre

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo JAR de la aplicación Spring Boot al contenedor
COPY --from=compilacion /compilacion/target/apicalendario-1.0.0.jar /app/apicalendario.jar

# Configuración por defecto (se puede sobrescribir con -e al crear el contenedor)
ENV PUERTO=8081 \
    DB_URL=jdbc:postgresql://dockerbdcalendario:5432/calendario \
    DB_USUARIO=postgres \
    DB_CLAVE=postgres \
    FESTIVOS_API_URL=http://dockerapifestivos:8080

# Exponer el puerto 8081 en el contenedor
EXPOSE 8081

# Comando que se ejecuta cuando se inicia el contenedor
CMD ["java", "-jar", "apicalendario.jar"]
