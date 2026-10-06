# Punto 2 – API RESTful Calendario (Spring Boot + PostgreSQL)

**Integrantes:**
- Sebastian Restrepo Mira
- Vanessa Espinosa Cardona
- Eric David Florez Vasquez
- Mateo Mesa Cardona

## Endpoints que expone la API

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/calendario/generar/{anio}` | `true` si se clasificaron y guardaron todos los días del año |
| GET | `/api/calendario/listar/{anio}` | JSON: `[{ "id": 1, "fecha": "2023-01-01", "tipo": { "id": 3, "tipo": "Día festivo" }, "descripcion": "Domingo" }, ...]` |

## Endpoint que consume de la API Festivos (punto 1)

| Método | Ruta | Uso |
|---|---|---|
| GET | `/api/festivos/obtener/{anio}` | Obtener la lista de festivos del año para clasificar los días |

---

## 1. Diagrama de Arquitectura Onion

En la arquitectura Onion las capas son anillos concéntricos y **todas las dependencias apuntan hacia el centro**. El dominio no depende de nada. El núcleo define interfaces (puertos) y la infraestructura las implementa (inversión de dependencias). La base de datos y la API Festivos quedan afuera, como detalles reemplazables.

```mermaid
graph TD
    %% Actores y sistemas externos
    Client["Cliente Web / Móvil / Postman / Swagger UI"]
    DB[("Base de Datos - PostgreSQL<br/><i>tablas tipo y calendario</i>")]
    FestivosAPI["API Festivos - Express JS + MongoDB<br/><i>GET /api/festivos/obtener/{anio}</i>"]

    subgraph Onion [Arquitectura Onion - API Calendario]

        %% Anillo externo: Presentación e Infraestructura
        subgraph OuterLayer [Anillo Externo: Presentación e Infraestructura]
            Controllers["Presentación<br/><i>CalendarioControlador.java (@RestController)</i><br/>GET /api/calendario/generar/{anio}<br/>GET /api/calendario/listar/{anio}"]
            RepoImpl["Infraestructura - Persistencia<br/><i>CalendarioRepositorio.java</i><br/><i>TipoRepositorio.java</i><br/><i>(Spring Data JPA)</i>"]
            ClientImpl["Infraestructura - Cliente HTTP<br/><i>FestivoCliente.java</i><br/><i>(RestTemplate / WebClient)</i>"]

            %% Anillo de Aplicación
            subgraph ApplicationLayer [Anillo de Aplicación]
                Services["Servicios de Aplicación<br/><i>CalendarioServicio.java (@Service)</i><br/>generar(anio): clasifica cada día como<br/>Día laboral / Fin de Semana / Día festivo<br/>listar(anio)"]

                %% Anillo de Servicios de Dominio (Core)
                subgraph CoreLayer [Anillo de Servicios de Dominio - Core]
                    Interfaces["Interfaces / Puertos<br/><i>ICalendarioServicio.java</i><br/><i>ICalendarioRepositorio.java</i><br/><i>ITipoRepositorio.java</i><br/><i>IFestivoCliente.java</i>"]

                    %% Centro: Dominio
                    subgraph DomainLayer [Núcleo: Dominio]
                        Entities["Entidades<br/><i>Calendario.java / Tipo.java</i><br/>DTO<br/><i>FestivoDto.java</i>"]
                    end
                end
            end
        end
    end

    %% Flujo de la Petición (Request)
    Client -->|1. Petición HTTP GET| Controllers
    Controllers -->|2. Usa ICalendarioServicio| Interfaces
    Services -->|3. Usa IFestivoCliente e ICalendarioRepositorio| Interfaces
    ClientImpl -->|4. Petición HTTP GET| FestivosAPI
    RepoImpl -->|5. INSERT / SELECT| DB

    %% Implementaciones (inversión de dependencias)
    Services -.->|implementa ICalendarioServicio| Interfaces
    ClientImpl -.->|implementa IFestivoCliente| Interfaces
    RepoImpl -.->|implementa ICalendarioRepositorio / ITipoRepositorio| Interfaces

    %% Dependencia hacia el núcleo
    Interfaces -->|usa| Entities

    %% Flujo de la Respuesta (Response)
    Controllers -.->|6. Respuesta JSON: true / calendario| Client

    %% Estilos de Nodos
    style Onion fill:#ffffff,stroke:#9e9e9e,stroke-width:1px,color:#000
    style OuterLayer fill:#fff3e0,stroke:#f57c00,stroke-width:2px,color:#000
    style ApplicationLayer fill:#e8f5e9,stroke:#388e3c,stroke-width:2px,color:#000
    style CoreLayer fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px,color:#000
    style DomainLayer fill:#ffebee,stroke:#d32f2f,stroke-width:2px,color:#000
```

> El paso 4 (llamada a la API Festivos) solo ocurre en `generar`. En `listar` el servicio solo consulta el repositorio.

### Regla de dependencias

| Anillo | Contiene | Depende de |
|---|---|---|
| Núcleo: Dominio | Entidades `Calendario`, `Tipo` y DTO `FestivoDto` | Nada |
| Servicios de Dominio (Core) | Interfaces `ICalendarioServicio`, `ICalendarioRepositorio`, `ITipoRepositorio`, `IFestivoCliente` | Dominio |
| Aplicación | `CalendarioServicio` (lógica de clasificación de días) | Core y Dominio |
| Externo: Presentación e Infraestructura | `CalendarioControlador`, repositorios JPA, `FestivoCliente` | Anillos internos (implementa sus interfaces) |

---

## 2. Diagrama de la Base de Datos (PostgreSQL)

Modelo relacional del taller: cada registro de `calendario` es un día del año y está clasificado por un `tipo`.

```mermaid
erDiagram
    TIPO ||--o{ CALENDARIO : "clasifica"

    TIPO {
        int id PK
        varchar tipo "Día laboral, Fin de Semana, Día festivo"
    }

    CALENDARIO {
        int id PK
        date fecha "día del año"
        int idTipo FK "referencia a TIPO.id"
        varchar descripcion "nombre del día o del festivo"
    }
```

### Datos de la tabla `tipo`

| id | tipo |
|---|---|
| 1 | Día laboral |
| 2 | Fin de Semana |
| 3 | Día festivo |
