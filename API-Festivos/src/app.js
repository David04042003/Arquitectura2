const express = require('express');
const swaggerUi = require('swagger-ui-express');

const TipoRepositorio = require('./repositorios/tipo.repositorio');
const FestivoServicio = require('./servicios/festivo.servicio');
const FestivoControlador = require('./controladores/festivo.controlador');
const crearFestivoRutas = require('./rutas/festivo.rutas');
const { rutaNoEncontrada, manejadorErrores } = require('./middlewares/manejadorErrores');

/**
 * Crea la aplicación Express armando las capas (inyección de dependencias):
 * Repositorio -> Servicio -> Controlador -> Rutas.
 *
 * @param {object} opciones
 * @param {object} [opciones.tipoRepositorio] permite inyectar un repositorio falso en las pruebas
 * @param {object} [opciones.documentoSwagger] especificación OpenAPI ya validada
 */
function crearApp({ tipoRepositorio = new TipoRepositorio(), documentoSwagger } = {}) {
    const festivoServicio = new FestivoServicio(tipoRepositorio);
    const festivoControlador = new FestivoControlador(festivoServicio);

    const app = express();
    app.disable('x-powered-by');
    app.use(express.json());

    if (documentoSwagger) {
        app.get('/api-docs.json', (req, res) => res.json(documentoSwagger));
        app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(documentoSwagger));
    }

    app.use('/api/festivos', crearFestivoRutas(festivoControlador));

    app.use(rutaNoEncontrada);
    app.use(manejadorErrores);

    return app;
}

module.exports = crearApp;
