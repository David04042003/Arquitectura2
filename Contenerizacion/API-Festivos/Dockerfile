# Imagen de Node.js como base
FROM node:22-alpine

# Carpeta de la aplicación
WORKDIR /usr/src/app

# Instalar dependencias (se copian package.json y package-lock.json primero
# para aprovechar la caché de capas de Docker)
COPY package*.json ./
RUN npm ci --omit=dev

# Copiar el código fuente de la aplicación
COPY src ./src
COPY docs ./docs
COPY datos ./datos
COPY scripts ./scripts

# Configuración por defecto (se puede sobrescribir con -e al crear el contenedor)
ENV NODE_ENV=production \
    PUERTO=8080 \
    MONGO_URI=mongodb://dockerbdfestivos:27017/festivos

# Exponer el puerto donde corre la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["node", "src/index.js"]
