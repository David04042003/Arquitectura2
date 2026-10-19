const ErrorAplicacion = require('../errores/ErrorAplicacion');
const {
    crearFecha,
    obtenerDomingoPascua,
    agregarDias,
    siguienteLunes,
    sonMismaFecha
} = require('./fechas.servicio');

/**
 * Estrategia de cálculo por tipo de festivo (id del documento en la colección "tipos").
 * Cada estrategia recibe el festivo, el año y el domingo de pascua del año.
 */
const ESTRATEGIAS_CALCULO = Object.freeze({
    // 1. Fijo: se celebra en su fecha
    1: (festivo, anio) => crearFecha(anio, festivo.mes, festivo.dia),
    // 2. Ley de "Puente festivo": se traslada al siguiente lunes
    2: (festivo, anio) => siguienteLunes(crearFecha(anio, festivo.mes, festivo.dia)),
    // 3. Basado en el domingo de pascua
    3: (festivo, anio, pascua) => agregarDias(pascua, festivo.diasPascua ?? 0),
    // 4. Basado en el domingo de pascua y Ley de "Puente festivo"
    4: (festivo, anio, pascua) => siguienteLunes(agregarDias(pascua, festivo.diasPascua ?? 0))
});

/**
 * Lógica de negocio de los festivos de Colombia.
 */
class FestivoServicio {
    /**
     * @param {import('../repositorios/tipo.repositorio')} tipoRepositorio
     */
    constructor(tipoRepositorio) {
        this.tipoRepositorio = tipoRepositorio;
    }

    /**
     * Calcula todos los festivos del año ordenados por fecha.
     * @param {number} anio
     * @returns {Promise<Array<{nombre:string, fecha:Date}>>}
     */
    async calcularFestivos(anio) {
        const tipos = await this.tipoRepositorio.obtenerTodos();
        const pascua = obtenerDomingoPascua(anio);

        const festivos = tipos.flatMap((tipo) => {
            const calcular = ESTRATEGIAS_CALCULO[tipo.id];
            if (!calcular) {
                throw new ErrorAplicacion(`Tipo de festivo desconocido en la base de datos: ${tipo.id}`);
            }
            return tipo.festivos.map((festivo) => ({
                nombre: festivo.nombre,
                fecha: calcular(festivo, anio, pascua)
            }));
        });

        return festivos.sort((a, b) => a.fecha - b.fecha);
    }

    /**
     * Indica si la fecha es festivo en Colombia.
     * @param {Date} fecha fecha válida (UTC)
     * @returns {Promise<boolean>}
     */
    async esFestivo(fecha) {
        const festivos = await this.calcularFestivos(fecha.getUTCFullYear());
        return festivos.some((festivo) => sonMismaFecha(festivo.fecha, fecha));
    }
}

module.exports = FestivoServicio;
