const { crearFecha, esAnioValido, ANIO_MINIMO, ANIO_MAXIMO } = require('../servicios/fechas.servicio');

const MENSAJE_FECHA_NO_VALIDA = 'Fecha No valida';
const SOLO_DIGITOS = /^\d{1,4}$/;

/**
 * Convierte un parámetro de ruta a entero; solo acepta dígitos (rechaza "2023abc", "1.5", "-3").
 */
function aEntero(valor) {
    return SOLO_DIGITOS.test(valor) ? Number(valor) : NaN;
}

/**
 * Valida :anio/:mes/:dia. Si la fecha no existe responde texto "Fecha No valida".
 * Si es válida deja la fecha (Date UTC) en res.locals.fecha para el controlador.
 */
function validarFecha(req, res, next) {
    const { anio, mes, dia } = req.params;
    const fecha = crearFecha(aEntero(anio), aEntero(mes), aEntero(dia));

    if (!fecha) {
        return res.status(400).type('text/plain').send(MENSAJE_FECHA_NO_VALIDA);
    }

    res.locals.fecha = fecha;
    return next();
}

/**
 * Valida :anio. Si es válido lo deja en res.locals.anio.
 */
function validarAnio(req, res, next) {
    const anio = aEntero(req.params.anio);

    if (!esAnioValido(anio)) {
        return res.status(400).json({
            mensaje: `Año no válido. Debe ser un entero entre ${ANIO_MINIMO} y ${ANIO_MAXIMO}`
        });
    }

    res.locals.anio = anio;
    return next();
}

module.exports = {
    MENSAJE_FECHA_NO_VALIDA,
    validarFecha,
    validarAnio
};
