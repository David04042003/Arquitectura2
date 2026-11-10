# API Calendario (Spring Boot + PostgreSQL + Arquitectura Onion + Token JWT)

API RESTful que clasifica cada día del año como **Día laboral**, **Fin de Semana** o **Día festivo**,
consumiendo la [API Festivos](../API-Festivos). Implementa el [diagrama del punto 2](../Diagramas/Punto2-API-Calendario.md).

**Integrantes:**
- Sebastian Restrepo Mira
- Vanessa Espinosa Cardona
- Eric David Florez Vasquez
- Mateo Mesa Cardona

## Endpoints

| Método | Ruta | Token | Respuesta |
|---|---|---|---|
| GET | `/api/usuarios/validar/{usuario}/{clave}` | No | `{ "usuario": {...}, "token": "eyJ..." }` (401 si las credenciales no son válidas) |
| POST | `/api/usuarios/login` | No | Igual al anterior, con cuerpo `{ "usuario": "admin", "clave": "admin123" }` |
| GET | `/api/calendario/generar/{anio}` | Sí | `true` si se clasificaron y guardaron todos los días del año |
| GET | `/api/calendario/listar/{anio}` | Sí | `[{ "id": 1, "fecha": "2023-01-01", "tipo": { "id": 3, "tipo": "Día festivo" }, "descripcion": "Año nuevo" }, ...]` |
| GET | `/swagger-ui.html` | No | Documentación Swagger (botón **Authorize** para pegar el token) |

- Sin token, o con un token inválido/expirado, las rutas protegidas responden **403 Forbidden**.
- El token se envía en el header `Authorization: Bearer {token}` y dura 30 minutos.
- Volver a generar un año reemplaza sus días (no se duplican).
- Si la API Festivos no responde, `generar` responde **503** y no modifica lo que ya estaba guardado.
- Años válidos: 1583 a 9999 (igual que la API Festivos); fuera de ese rango responde **400**.

## Ejecución

Requisitos: Java 17+, Maven, PostgreSQL y la API Festivos corriendo en `http://localhost:8080`.

```bash
# 1. Crear la base de datos (las tablas y los tipos se crean solos al iniciar)
createdb calendario

# 2. Levantar la API Festivos (puerto 8080)
cd ../API-Festivos && npm start

# 3. Levantar esta API (puerto 8081)
cd ../API-Calendario
DB_USUARIO=postgres DB_CLAVE=postgres mvn spring-boot:run
```

Al iniciar se crea el usuario **admin / admin123** (clave cifrada con BCrypt).

Variables de entorno (todas opcionales, ver `application.properties`):

| Variable | Valor por defecto |
|---|---|
| `PUERTO` | `8081` |
| `DB_URL` | `jdbc:postgresql://localhost:5432/calendario` |
| `DB_USUARIO` / `DB_CLAVE` | `postgres` / `postgres` |
| `FESTIVOS_API_URL` | `http://localhost:8080` |
| `JWT_SECRETO` | clave de desarrollo (en producción se debe cambiar) |
| `USUARIO_INICIAL` / `USUARIO_INICIAL_CLAVE` | `admin` / `admin123` |

Pruebas (no necesitan base de datos ni la API Festivos):

```bash
mvn test
```

### Ejemplo con curl

```bash
TOKEN=$(curl -s http://localhost:8081/api/usuarios/validar/admin/admin123 | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/calendario/generar/2023
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/calendario/listar/2023
```

## Arquitectura Onion

Todas las dependencias apuntan hacia el centro: el **core** no conoce Spring Data, HTTP ni la base de datos;
solo define entidades e interfaces (puertos). Las capas externas implementan esas interfaces y Spring las
inyecta por constructor (inversión de dependencias).

```
src/main/java/apicalendario/
├── core/                                   # NÚCLEO (no depende de ninguna otra capa)
│   ├── entidades/      Calendario, Tipo, Usuario
│   ├── dtos/           FestivoDto, UsuarioDto, UsuarioLoginDto, CredencialesDto
│   ├── excepciones/    SolicitudInvalida, CredencialesInvalidas, ServicioExterno
│   └── interfaces/
│       ├── repositorios/  ICalendarioRepositorio, ITipoRepositorio, IUsuarioRepositorio
│       ├── servicios/     ICalendarioServicio, IUsuarioServicio, ITokenServicio
│       └── clientes/      IFestivoCliente
├── aplicacion/                             # LÓGICA DE NEGOCIO (depende solo del core)
│   ├── CalendarioServicio      generar(anio): clasifica cada día / listar(anio)
│   ├── UsuarioServicio         login: valida credenciales y emite el token
│   └── seguridad/              ConfiguracionSeguridad, FiltroSeguridad, SeguridadServicio,
│                               UsuarioDetalles, UsuarioDetalleServicio, PropiedadesJwt
├── infraestructura/                        # DETALLES (implementan las interfaces del core)
│   ├── persistencia/           CalendarioRepositorio, TipoRepositorio, UsuarioRepositorio
│   │   └── jpa/                CalendarioJpa, TipoJpa, UsuarioJpa (Spring Data JPA)
│   ├── clientes/               FestivoCliente (RestClient -> API Festivos)
│   └── configuracion/          InicializadorUsuario
└── presentacion/                           # API REST
    ├── CalendarioControlador, UsuarioControlador
    ├── ManejadorExcepciones    excepciones -> 400 / 401 / 503 con { "mensaje": "..." }
    └── ConfiguracionOpenApi    Swagger con seguridad Bearer
src/main/resources/
├── application.properties
├── schema.sql                  tablas tipo, calendario, usuario
└── data.sql                    tipos: 1 Día laboral, 2 Fin de Semana, 3 Día festivo
```

Flujo de `generar`: `CalendarioControlador -> ICalendarioServicio (CalendarioServicio) -> IFestivoCliente (FestivoCliente -> API Festivos)
-> ICalendarioRepositorio (CalendarioRepositorio -> PostgreSQL)`.

### Regla de clasificación

1. Si la fecha está en la lista de la API Festivos → **Día festivo** (descripción: nombre del festivo). Prevalece sobre el fin de semana.
2. Si es sábado o domingo → **Fin de Semana** (descripción: nombre del día).
3. En otro caso → **Día laboral** (descripción: nombre del día).

### Mejoras frente al ejemplo de la guía

- Los repositorios del core son interfaces Java puras; Spring Data JPA queda en infraestructura (adaptadores).
- Claves cifradas con BCrypt y la respuesta del login nunca devuelve la clave.
- Secreto y vigencia del token configurables; JJWT 0.12 (sin APIs obsoletas); sesiones `STATELESS`.
- Errores con código HTTP correcto y mensaje JSON; timeout al llamar la API Festivos.
- Pruebas unitarias del servicio con repositorios en memoria (posible gracias a Onion) y pruebas de seguridad de los endpoints.
