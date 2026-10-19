const { Router } = require('express');
const { validarFecha, validarAnio } = require('../validadores/festivo.validador');

/**
 * Rutas de /api/festivos.
 * @param {import('../controladores/festivo.controlador')} festivoControlador
 */
function crearFestivoRutas(festivoControlador) {
    const router = Router();

    router.get('/verificar/:anio/:mes/:dia', validarFecha, festivoControlador.verificar);
    router.get('/obtener/:anio', validarAnio, festivoControlador.obtener);

    return router;
}

module.exports = crearFestivoRutas;
