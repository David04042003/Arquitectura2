# Taller 4 – Contenerización con Docker

Contenerización de los dos microservicios del proyecto y sus bases de datos. Todo queda en una red de
contenedores llamada **`redcalendario`**:

```
                         Red de contenedores: redcalendario
 ┌──────────────────────┐   ┌──────────────────────┐   ┌──────────────────────┐   ┌──────────────────────┐
 │ dockerbdfestivos     │◄──│ dockerapifestivos    │◄──│ dockerapicalendario  │──►│ dockerbdcalendario   │
 │ MongoDB (festivos)   │   │ Express JS           │   │ Spring Boot          │   │ PostgreSQL           │
 │ 27018 → 27017        │   │ 8080 → 8080          │   │ 8081 → 8081          │   │ 5433 → 5432          │
 └──────────────────────┘   └──────────────────────┘   └──────────────────────┘   └──────────────────────┘
```

| Contenedor | Imagen | Puerto externo → interno | Qué contiene |
|---|---|---|---|
| `dockerbdfestivos` | `bdfestivos` (mongo:8.0) | 27018 → 27017 | Base de datos `festivos`, colección `tipos` |
| `dockerapifestivos` | `apifestivos` (node:22) | 8080 → 8080 | API Festivos (punto 1) |
| `dockerbdcalendario` | `bdcalendario` (postgres:17) | 5433 → 5432 | Base de datos `calendario` (tablas `tipo`, `calendario`, `usuario`) |
| `dockerapicalendario` | `apicalendario` (Java 17) | 8081 → 8081 | API Calendario (punto 2) |

> Postgres se publica en el **5433** para no chocar con un PostgreSQL instalado localmente en el 5432.
> Dentro de la red los contenedores se comunican por nombre (`dockerbdcalendario:5432`, `dockerapifestivos:8080`...).

## Estructura

Esta carpeta **no duplica el código**: los Dockerfile de las APIs usan como contexto de construcción las carpetas
del proyecto, por eso debe quedar al lado de `API-Festivos` y `API-Calendario`:

```
Arquitectura2/
├── API-Festivos/            código de la API Express (punto 1)
├── API-Calendario/          código de la API Spring Boot (punto 2)
└── Contenerizacion/         <- esta carpeta (entrega del Taller 4)
    ├── docker-compose.yml
    ├── BD-Festivos/
    │   ├── Dockerfile               FROM mongo + script de inicio
    │   └── iniciarbdfestivos.js     crea y puebla la colección "tipos"
    ├── API-Festivos/
    │   ├── Dockerfile               FROM node, npm ci y node src/index.js
    │   └── Dockerfile.dockerignore
    ├── BD-Calendario/
    │   ├── Dockerfile               FROM postgres + scripts de inicio
    │   ├── DDL_Calendario.sql       crea la BD "calendario" y sus tablas
    │   └── DML_Calendario.sql       carga el catálogo de tipos de día
    └── API-Calendario/
        ├── Dockerfile               compila el .jar con Maven y lo ejecuta con Java 17
        └── Dockerfile.dockerignore
```

Los scripts de las bases de datos se copian a `/docker-entrypoint-initdb.d/`, así que Mongo y Postgres los
ejecutan automáticamente la primera vez que arranca el contenedor (no hace falta `docker cp` ni poblarlas a mano).

El Dockerfile de la API Calendario hace la compilación dentro de Docker (multi-stage): la primera etapa ejecuta
`mvn clean package -DskipTests` y la segunda solo copia el `.jar`, por lo que no se necesita Maven ni Java instalados.

## Requisitos

- Docker Desktop en ejecución (`docker --version`, `docker run hello-world`).
- Puertos libres: 8080, 8081, 5433 y 27018.

## Opción 1: todo con Docker Compose (recomendado)

Desde esta carpeta:

```bash
docker compose up -d --build
```

Crea la red, construye las 4 imágenes y levanta los contenedores en orden (cada API espera a que su base de datos
esté lista). Para ver el estado y los logs:

```bash
docker compose ps
```

```bash
docker compose logs -f dockerapicalendario
```

Para detener todo (agregar `-v` para borrar también los datos de las bases de datos):

```bash
docker compose down
```

## Opción 2: paso a paso, como en la guía

Todos los comandos se ejecutan desde esta carpeta (`Contenerizacion`).

**1. Crear la red**

```bash
docker network create redcalendario
```

**2. Base de datos MongoDB de la API Festivos**

```bash
docker build -t bdfestivos BD-Festivos
```

```bash
docker container run --name dockerbdfestivos --network redcalendario -p 27018:27017 -d bdfestivos
```

**3. API Festivos (Express JS)**

```bash
docker build -t apifestivos -f API-Festivos/Dockerfile ../API-Festivos
```

```bash
docker container run --name dockerapifestivos --network redcalendario -p 8080:8080 -e MONGO_URI=mongodb://dockerbdfestivos:27017/festivos -d apifestivos
```

**4. Base de datos PostgreSQL de la API Calendario**

```bash
docker build -t bdcalendario BD-Calendario
```

```bash
docker container run --name dockerbdcalendario --network redcalendario -e POSTGRES_PASSWORD=postgres -p 5433:5432 -d bdcalendario
```

**5. API Calendario (Spring Boot)**

```bash
docker build -t apicalendario -f API-Calendario/Dockerfile ../API-Calendario
```

```bash
docker container run --name dockerapicalendario --network redcalendario -p 8081:8081 -e DB_URL=jdbc:postgresql://dockerbdcalendario:5432/calendario -e DB_CLAVE=postgres -e FESTIVOS_API_URL=http://dockerapifestivos:8080 -d apicalendario
```

**Verificar los contenedores y la red**

```bash
docker ps
```

```bash
docker network inspect redcalendario
```

## Verificación de las bases de datos

MongoDB (dentro del contenedor):

```bash
docker container exec -it dockerbdfestivos mongosh festivos --eval "db.tipos.find({}, {_id:0, id:1, tipo:1})"
```

También se puede conectar un cliente externo (Robo 3T, Compass) a `localhost:27018`.

PostgreSQL (dentro del contenedor):

```bash
docker exec -it dockerbdcalendario psql -U postgres -d calendario -c "SELECT * FROM tipo;"
```

Cliente externo (pgAdmin, DBeaver): `localhost:5433`, usuario `postgres`, clave `postgres`, base `calendario`.

## Prueba de la red de contenedores (Postman o curl)

| Método | URL | Resultado esperado |
|---|---|---|
| GET | `http://localhost:8080/api/festivos/verificar/2023/12/25` | `Es Festivo` |
| GET | `http://localhost:8080/api-docs` | Swagger de la API Festivos |
| GET | `http://localhost:8081/api/usuarios/validar/admin/admin123` | `{ "usuario": {...}, "token": "eyJ..." }` |
| GET | `http://localhost:8081/api/calendario/generar/2023` (Bearer token) | `true` — la API Calendario consulta internamente a `dockerapifestivos` |
| GET | `http://localhost:8081/api/calendario/listar/2023` (Bearer token) | 365 días clasificados |
| GET | `http://localhost:8081/swagger-ui.html` | Swagger de la API Calendario |

Con curl:

```bash
TOKEN=$(curl -s http://localhost:8081/api/usuarios/validar/admin/admin123 | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/calendario/generar/2023
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/calendario/listar/2023
```

La llamada a `generar` demuestra el funcionamiento completo de la red: `dockerapicalendario` → `dockerapifestivos`
→ `dockerbdfestivos`, y el resultado se guarda en `dockerbdcalendario`.

## Variables de entorno

| Contenedor | Variable | Valor en Docker |
|---|---|---|
| `dockerapifestivos` | `PUERTO` | `8080` |
| `dockerapifestivos` | `MONGO_URI` | `mongodb://dockerbdfestivos:27017/festivos` |
| `dockerbdcalendario` | `POSTGRES_PASSWORD` | `postgres` |
| `dockerapicalendario` | `PUERTO` | `8081` |
| `dockerapicalendario` | `DB_URL` | `jdbc:postgresql://dockerbdcalendario:5432/calendario` |
| `dockerapicalendario` | `DB_USUARIO` / `DB_CLAVE` | `postgres` / `postgres` |
| `dockerapicalendario` | `FESTIVOS_API_URL` | `http://dockerapifestivos:8080` |
| `dockerapicalendario` | `JWT_SECRETO`, `USUARIO_INICIAL_CLAVE` | opcionales (por defecto los de `application.properties`) |
