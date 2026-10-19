const { formatearFecha } = require('../servicios/fechas.servicio');

const MENSAJE_ES_FESTIVO = 'Es Festivo';
const MENSAJE_NO_ES_FESTIVO = 'No es festivo';

/**
 * Controlador HTTP: traduce la petición a llamadas del servicio y da formato a la respuesta.
 * Los parámetros ya llegan validados en res.locals (ver festivo.validador.js).
 */
class FestivoControlador {
    /**
     * @param {import('../servicios/festivo.servicio')} festivoServicio
     */
    constructor(festivoServicio) {
        this.festivoServicio = festivoServicio;
    }

    // GET /api/festivos/verificar/:anio/:mes/:dia
    verificar = async (req, res) => {
        const esFestivo = await this.festivoServicio.esFestivo(res.locals.fecha);
        res.type('text/plain').send(esFestivo ? MENSAJE_ES_FESTIVO : MENSAJE_NO_ES_FESTIVO);
    };

    // GET /api/festivos/obtener/:anio
    obtener = async (req, res) => {
        const festivos = await this.festivoServicio.calcularFestivos(res.locals.anio);
        res.json(festivos.map(({ nombre, fecha }) => ({
            festivo: nombre,
            fecha: formatearFecha(fecha)
        })));
    };
}

module.exports = FestivoControlador;
