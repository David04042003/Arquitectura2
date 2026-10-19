# API Festivos (Express JS + MongoDB)

API RESTful que calcula los festivos de Colombia. Implementa el [diagrama del punto 1](../Diagramas/Punto1-API-Festivos.md).

**Integrantes:**
- Sebastian Restrepo Mira
- Vanessa Espinosa Cardona
- Eric David Florez Vasquez
- Mateo Mesa Cardona

## Endpoints

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/festivos/verificar/:anio/:mes/:dia` | Texto: `Es Festivo` / `No es festivo` (200) o `Fecha No valida` (400) |
| GET | `/api/festivos/obtener/:anio` | JSON: `[{ "festivo": "Año nuevo", "fecha": "2023-01-01" }, ...]` ordenado por fecha |
| GET | `/api-docs` | Swagger UI |
| GET | `/api-docs.json` | Especificación OpenAPI 3 |

## Ejecución

Requisitos: Node.js 20+ y MongoDB corriendo en `localhost:27017`.

```bash
npm install
cp .env.example .env      # opcional: PUERTO y MONGO_URI
npm run sembrar           # carga la colección "tipos" en la base "festivos"
npm start                 # http://localhost:8080  -  Swagger: http://localhost:8080/api-docs
```

`npm run sembrar` carga `datos/tipos.json`, que tiene los mismos festivos del script `BDFestivos.mjs` del profesor.
También se puede cargar con el script original: `mongosh < BDFestivos.mjs`. La API funciona con cualquiera de los dos.

Otros comandos:

```bash
npm run dev               # modo desarrollo con nodemon
npm test                  # pruebas (node:test + supertest, no necesitan MongoDB)
npm run swagger:validar   # valida docs/openapi.yaml contra el estándar OpenAPI 3
```

## Arquitectura (MVC + Repositorio)

```
src/
├── index.js                          # arranque: valida Swagger, conecta Mongo, levanta Express
├── app.js                            # arma las capas (inyección de dependencias)
├── config/                           # configuración, conexión a MongoDB, carga/validación de Swagger
├── rutas/festivo.rutas.js            # rutas /api/festivos
├── validadores/festivo.validador.js  # valida año, mes y día
├── controladores/festivo.controlador.js  # verificar() / obtener(): formato HTTP de la respuesta
├── servicios/
│   ├── festivo.servicio.js           # calcularFestivos(anio), esFestivo(fecha)
│   └── fechas.servicio.js            # ServicioFechas.js del profesor, mejorado
├── repositorios/tipo.repositorio.js  # único acceso a MongoDB
├── modelos/tipo.modelo.js            # esquema Mongoose de la colección "tipos"
├── middlewares/manejadorErrores.js   # 404 y errores centralizados
└── errores/ErrorAplicacion.js
docs/openapi.yaml                     # especificación Swagger (se valida al iniciar)
datos/tipos.json                      # datos iniciales
```

Flujo: `Ruta -> Validador -> Controlador -> Servicio -> Repositorio -> MongoDB`.
Cada capa recibe su dependencia por constructor, por eso las pruebas usan un repositorio falso sin base de datos.

### Cálculo por tipo de festivo

| id | Tipo | Cálculo |
|---|---|---|
| 1 | Fijo | `dia`/`mes` |
| 2 | Ley de "Puente festivo" | `siguienteLunes(dia/mes)` |
| 3 | Basado en el domingo de pascua | `pascua + diasPascua` |
| 4 | Pascua + "Puente festivo" | `siguienteLunes(pascua + diasPascua)` |

### Mejoras al `ServicioFechas.js` del profesor

- **Corrección del domingo de pascua:** la fórmula original (Gauss con constantes 24 y 5) se equivoca en
  **1954, 1981, 2049 y 2076** (da una semana tarde) y solo sirve entre 1900 y 2099. Se reemplazó por el algoritmo
  de Meeus/Jones/Butcher, válido para todo el calendario gregoriano. `obtenerDomingoRamos()` se conserva (pascua - 7).
- **Fechas en UTC:** el resultado ya no depende de la zona horaria del servidor.
- **Funciones puras:** `agregarDias()` y `siguienteLunes()` no modifican la fecha recibida.
- **Nuevas utilidades:** `crearFecha()` / `esFechaValida()` (detecta fechas como 30 de febrero), `formatearFecha()` (`YYYY-MM-DD`).
