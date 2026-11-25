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

Este repositorio contiene **solo la contenerización**. El código fuente de las APIs está en el repositorio del
proyecto, [David04042003/Arquitectura2](https://github.com/David04042003/Arquitectura2), y los Dockerfile de
las APIs lo descargan desde GitHub (rama `main`) al construir la imagen. Por eso no hace falta clonar el proyecto.

```
Contenerizacion/
├── docker-compose.yml
├── BD-Festivos/
│   ├── Dockerfile               FROM mongo + script de inicio
│   └── iniciarbdfestivos.js     crea y puebla la colección "tipos"
├── API-Festivos/
│   └── Dockerfile               descarga API-Festivos de GitHub, npm ci y node src/index.js
├── BD-Calendario/
│   ├── Dockerfile               FROM postgres + scripts de inicio
│   ├── DDL_Calendario.sql       crea la BD "calendario" y sus tablas
│   └── DML_Calendario.sql       carga el catálogo de tipos de día
├── API-Calendario/
│   └── Dockerfile               descarga API-Calendario de GitHub, compila el .jar y lo ejecuta con Java 17
└── evidencias/                  salidas de la ejecución (ver sección Evidencias)
```

Los scripts de las bases de datos se copian a `/docker-entrypoint-initdb.d/`, así que Mongo y Postgres los
ejecutan automáticamente la primera vez que arranca el contenedor (no hace falta `docker cp` ni poblarlas a mano).

Los Dockerfile de las APIs tienen varias etapas (multi-stage):

1. **fuente**: `ADD https://github.com/David04042003/Arquitectura2.git#main /repo` descarga el repositorio.
2. **compilación** (solo API Calendario): ejecuta `mvn clean package -DskipTests` para generar el `.jar`.
3. **imagen final**: solo contiene Node.js o Java 17 y la aplicación, sin Maven ni el resto del repositorio.

Se puede construir desde otro repositorio o rama con `--build-arg REPO=<url-git>` y `--build-arg RAMA=<rama>`.

## Requisitos

- Docker Desktop en ejecución (`docker --version`, `docker run hello-world`).
- Conexión a internet (para descargar las imágenes base y el código de GitHub).
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
docker build -t apifestivos API-Festivos
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
docker build -t apicalendario API-Calendario
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

## Evidencias

La carpeta [evidencias](evidencias) tiene la salida real de la ejecución, tomada después de construir todo desde
cero con `docker compose up -d --build` en una carpeta que solo tenía este repositorio:

| Archivo | Qué demuestra |
|---|---|
| [01-version-e-imagenes.txt](evidencias/01-version-e-imagenes.txt) | Versión de Docker y las 4 imágenes construidas |
| [02-contenedores-en-ejecucion.txt](evidencias/02-contenedores-en-ejecucion.txt) | Los 4 contenedores en ejecución (`docker compose ps`) con sus puertos |
| [03-red-redcalendario.txt](evidencias/03-red-redcalendario.txt) | La red `redcalendario` y la IP de cada contenedor |
| [04-bd-festivos-mongodb.txt](evidencias/04-bd-festivos-mongodb.txt) | La base `festivos` poblada en MongoDB (4 tipos de festivo) |
| [05-bd-calendario-postgresql.txt](evidencias/05-bd-calendario-postgresql.txt) | La base `calendario` en PostgreSQL con sus tablas, el catálogo y el usuario inicial |
| [06-api-festivos.txt](evidencias/06-api-festivos.txt) | La API Festivos respondiendo (`verificar` y `obtener`) |
| [07-api-calendario.txt](evidencias/07-api-calendario.txt) | Login con token, `generar/2023` → `true` y `listar/2023` con los 365 días (19 festivos) |
| [08-logs-contenedores.txt](evidencias/08-logs-contenedores.txt) | Logs de arranque: ejecución de los scripts de las BD y de las dos APIs |

La prueba del archivo 07 recorre toda la red: `dockerapicalendario` → `dockerapifestivos` → `dockerbdfestivos`,
y el resultado queda guardado en `dockerbdcalendario`.

Las capturas de pantalla (Docker Desktop y Postman) van en [evidencias/capturas](evidencias/capturas).

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
