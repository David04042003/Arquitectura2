# Repositorio de GitHub donde está el código fuente de la API
# (se puede cambiar con --build-arg REPO=... y --build-arg RAMA=...)
ARG REPO=https://github.com/David04042003/Arquitectura2.git
ARG RAMA=main

# ---------- Etapa 1: descargar el código fuente desde GitHub ----------
FROM scratch AS fuente
ARG REPO
ARG RAMA
ADD ${REPO}#${RAMA} /repo

# ---------- Etapa 2: imagen de la API ----------
# Imagen de Node.js como base
FROM node:22-alpine

# Carpeta de la aplicación
WORKDIR /usr/src/app

# Instalar dependencias (se copian package.json y package-lock.json primero
# para aprovechar la caché de capas de Docker)
COPY --from=fuente /repo/API-Festivos/package*.json ./
RUN npm ci --omit=dev

# Copiar el código fuente de la aplicación
COPY --from=fuente /repo/API-Festivos/src ./src
COPY --from=fuente /repo/API-Festivos/docs ./docs
COPY --from=fuente /repo/API-Festivos/datos ./datos
COPY --from=fuente /repo/API-Festivos/scripts ./scripts

# Configuración por defecto (se puede sobrescribir con -e al crear el contenedor)
ENV NODE_ENV=production \
    PUERTO=8080 \
    MONGO_URI=mongodb://dockerbdfestivos:27017/festivos

# Exponer el puerto donde corre la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["node", "src/index.js"]
