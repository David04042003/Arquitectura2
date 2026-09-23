# Punto 1 – API RESTful Festivos (Express JS + MongoDB)

## Endpoints que expone la API

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/festivos/verificar/:anio/:mes/:dia` | Texto: `Es Festivo` / `No es festivo` / `Fecha No valida` |
| GET | `/api/festivos/obtener/:anio` | JSON: `[{ "festivo": "Año nuevo", "fecha": "2023-01-01" }, ...]` (lo consume la API del punto 2) |

---

## 1. Diagrama de Arquitectura por Capas

```mermaid
graph TD
    %% Cliente / Capa de Presentación Externa
    subgraph ClientLayer [Capa de Cliente]
        Client["Cliente Web / Postman<br/>API Calendario - Spring Boot (punto 2)"]
    end

    %% Capa de Entrada y Enrutamiento
    subgraph PresentationLayer [Capa de Presentación / API]
        Index["index.js / app.js<br/><i>Express - puerto 8080</i>"]
        Routes["Rutas Express<br/><i>festivo.rutas.js</i><br/>GET /api/festivos/verificar/:anio/:mes/:dia<br/>GET /api/festivos/obtener/:anio"]
        Validators["Middlewares / Validadores<br/><i>festivo.validador.js</i><br/>valida año, mes y día (fecha válida)"]
    end

    %% Capa de Lógica de Negocio
    subgraph BusinessLayer [Capa de Lógica de Negocio]
        Controllers["Controladores<br/><i>festivo.controlador.js</i><br/>verificar() / obtener()"]
        Services["Servicios<br/><i>festivo.servicio.js</i><br/>calcularDomingoPascua()<br/>trasladarASiguienteLunes()<br/>calcularFestivos(anio)"]
    end

    %% Capa de Acceso a Datos
    subgraph DataAccessLayer [Capa de Acceso a Datos]
        Repositories["Repositorios / Modelos<br/><i>tipo.repositorio.js</i><br/><i>tipo.modelo.js (Mongoose)</i>"]
    end

    %% Capa de Persistencia
    subgraph PersistenceLayer [Capa de Persistencia]
        DB[("Base de Datos - MongoDB<br/><i>festivos.tipos</i>")]
    end

    %% Flujo de la Petición (Request)
    Client -->|1. Petición HTTP GET| Index
    Index -->|2. Delega a| Routes
    Routes -->|3. Valida datos| Validators
    Validators -->|4. Pasa filtro| Controllers
    Validators -.->|4b. Fecha No valida| Client
    Controllers -->|5. Solicita cálculo| Services
    Services -->|6. Solicita tipos de festivo| Repositories
    Repositories -->|7. Consulta| DB

    %% Flujo de la Respuesta (Response)
    DB -.->|8. Retorna documentos| Repositories
    Repositories -.->|9. Lista de tipos y festivos| Services
    Services -.->|10. Festivos calculados del año| Controllers
    Controllers -.->|11. Respuesta Texto / JSON| Client

    %% Estilos de Nodos
    style ClientLayer fill:#e1f5fe,stroke:#0288d1,stroke-width:2px
    style PresentationLayer fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    style BusinessLayer fill:#e8f5e9,stroke:#388e3c,stroke-width:2px
    style DataAccessLayer fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    style PersistenceLayer fill:#ffebee,stroke:#d32f2f,stroke-width:2px
```

---

## 2. Diagrama de la Base de Datos (MongoDB)

Colección `tipos`: cada documento es un **tipo de festivo** y contiene el vector `festivos`.

```mermaid
erDiagram
    TIPOS ||--|{ FESTIVO : "contiene (vector festivos)"

    TIPOS {
        ObjectId _id PK "generado por MongoDB"
        int id "1 a 4"
        string tipo "Fijo, Ley Puente festivo, Basado en pascua, Pascua + Puente"
        string modoCalculo "descripción de cómo se calcula"
        array festivos "vector de objetos FESTIVO"
    }

    FESTIVO {
        int dia "null en tipos 3 y 4"
        int mes "null en tipos 3 y 4"
        string nombre "ej. Año nuevo, Corpus Christi"
        int diasPascua "días sumados al domingo de pascua (tipos 3 y 4)"
    }
```

### Documento de ejemplo

```json
{
  "id": 4,
  "tipo": "Basado en el domingo de pascua y Ley de \"Puente festivo\"",
  "modoCalculo": "Se suma diasPascua al domingo de pascua y se traslada al siguiente lunes",
  "festivos": [
    { "dia": null, "mes": null, "nombre": "Ascensión del Señor",      "diasPascua": 40 },
    { "dia": null, "mes": null, "nombre": "Corpus Christi",           "diasPascua": 61 },
    { "dia": null, "mes": null, "nombre": "Sagrado Corazón de Jesús", "diasPascua": 68 }
  ]
}
```
