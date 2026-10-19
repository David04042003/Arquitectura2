const ErrorAplicacion = require('../errores/ErrorAplicacion');

/**
 * 404 para cualquier ruta que no exista.
 */
function rutaNoEncontrada(req, res) {
    res.status(404).json({ mensaje: `Ruta no encontrada: ${req.method} ${req.originalUrl}` });
}

/**
 * Manejador central de errores. Express 5 envía aquí también los errores de funciones async.
 */
// eslint-disable-next-line no-unused-vars
function manejadorErrores(error, req, res, next) {
    const codigoEstado = error instanceof ErrorAplicacion ? error.codigoEstado : 500;
    if (codigoEstado >= 500) {
        console.error(error);
    }
    res.status(codigoEstado).json({
        mensaje: codigoEstado >= 500 ? 'Error interno del servidor' : error.message
    });
}

module.exports = {
    rutaNoEncontrada,
    manejadorErrores
};
