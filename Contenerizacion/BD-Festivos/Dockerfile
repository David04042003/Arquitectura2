# Imagen oficial de MongoDB desde Docker Hub
FROM mongo:8.0

# Base de datos que se crea al iniciar
ENV MONGO_INITDB_DATABASE=festivos

# Script de creación y población de la base de datos "festivos"
COPY iniciarbdfestivos.js /docker-entrypoint-initdb.d/

EXPOSE 27017
