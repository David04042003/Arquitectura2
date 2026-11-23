# Imagen oficial de PostgreSQL desde Docker Hub
FROM postgres:17

# Copia los scripts de DDL y DML al directorio de inicialización
# (se ejecutan en orden alfabético solo la primera vez que arranca el contenedor)
COPY DDL_Calendario.sql /docker-entrypoint-initdb.d/01_DDL_Calendario.sql
COPY DML_Calendario.sql /docker-entrypoint-initdb.d/02_DML_Calendario.sql

EXPOSE 5432
