/**
 * Servicio de fechas (versión mejorada del ServicioFechas.js entregado por el profesor).
 *
 * Mejoras respecto a la versión original:
 *  - Todas las fechas se manejan en UTC: el resultado no depende de la zona horaria
 *    del servidor (antes un servidor en UTC-5 podía correr un día al serializar con toISOString()).
 *  - El domingo de pascua se calcula con el algoritmo de Meeus/Jones/Butcher, válido para
 *    cualquier año del calendario gregoriano (>= 1583). La fórmula original de Gauss con
 *    constantes fijas (24 y 5) solo es correcta entre 1900 y 2099.
 *  - Se conserva obtenerDomingoRamos() (pascua - 7 días) para mantener compatibilidad.
 *  - Funciones puras: nunca modifican la fecha recibida.
 *  - Se agregan utilidades para crear/validar fechas y formatearlas como 'YYYY-MM-DD'.
 */

const ANIO_MINIMO = 1583; // primer año completo del calendario gregoriano
const ANIO_MAXIMO = 9999;
const LUNES = 1;
const DIAS_SEMANA = 7;

/**
 * Crea una fecha UTC (a medianoche) validando que exista en el calendario.
 * @param {number} anio
 * @param {number} mes 1 a 12
 * @param {number} dia 1 a 31
 * @returns {Date|null} la fecha, o null si no es válida (ej. 30 de febrero)
 */
function crearFecha(anio, mes, dia) {
    if (![anio, mes, dia].every(Number.isInteger)) {
        return null;
    }
    if (anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
        return null;
    }
    const fecha = new Date(Date.UTC(anio, mes - 1, dia));
    // Date.UTC "desborda" fechas inválidas (31/02 -> 03/03), así se detectan
    const esValida = fecha.getUTCFullYear() === anio
        && fecha.getUTCMonth() === mes - 1
        && fecha.getUTCDate() === dia;
    return esValida ? fecha : null;
}

function esFechaValida(anio, mes, dia) {
    return crearFecha(anio, mes, dia) !== null;
}

function esAnioValido(anio) {
    return Number.isInteger(anio) && anio >= ANIO_MINIMO && anio <= ANIO_MAXIMO;
}

/**
 * Domingo de pascua (algoritmo anónimo gregoriano de Meeus/Jones/Butcher).
 * @param {number} anio
 * @returns {Date}
 */
function obtenerDomingoPascua(anio) {
    const a = anio % 19;
    const b = Math.floor(anio / 100);
    const c = anio % 100;
    const d = Math.floor(b / 4);
    const e = b % 4;
    const f = Math.floor((b + 8) / 25);
    const g = Math.floor((b - f + 1) / 3);
    const h = (19 * a + b - d - g + 15) % 30;
    const i = Math.floor(c / 4);
    const k = c % 4;
    const l = (32 + 2 * e + 2 * i - h - k) % 7;
    const m = Math.floor((a + 11 * h + 22 * l) / 451);
    const mes = Math.floor((h + l - 7 * m + 114) / 31);
    const dia = ((h + l - 7 * m + 114) % 31) + 1;

    return new Date(Date.UTC(anio, mes - 1, dia));
}

/**
 * Domingo de ramos: una semana antes del domingo de pascua.
 * @param {number} anio
 * @returns {Date}
 */
function obtenerDomingoRamos(anio) {
    return agregarDias(obtenerDomingoPascua(anio), -DIAS_SEMANA);
}

/**
 * Retorna una nueva fecha sumando (o restando si es negativo) días.
 * @param {Date} fecha
 * @param {number} dias
 * @returns {Date}
 */
function agregarDias(fecha, dias) {
    const nuevaFecha = new Date(fecha.getTime());
    nuevaFecha.setUTCDate(nuevaFecha.getUTCDate() + dias);
    return nuevaFecha;
}

/**
 * Ley de "Puente festivo": si la fecha no es lunes la traslada al lunes siguiente.
 * @param {Date} fecha
 * @returns {Date}
 */
function siguienteLunes(fecha) {
    const diaSemana = fecha.getUTCDay(); // 0 = domingo ... 6 = sábado
    const diasParaLunes = (DIAS_SEMANA + LUNES - diaSemana) % DIAS_SEMANA;
    return agregarDias(fecha, diasParaLunes);
}

/**
 * Formatea la fecha como 'YYYY-MM-DD' (formato ISO 8601 que consume la API Calendario).
 * @param {Date} fecha
 * @returns {string}
 */
function formatearFecha(fecha) {
    return fecha.toISOString().slice(0, 10);
}

function sonMismaFecha(fechaA, fechaB) {
    return formatearFecha(fechaA) === formatearFecha(fechaB);
}

module.exports = {
    ANIO_MINIMO,
    ANIO_MAXIMO,
    crearFecha,
    esFechaValida,
    esAnioValido,
    obtenerDomingoPascua,
    obtenerDomingoRamos,
    agregarDias,
    siguienteLunes,
    formatearFecha,
    sonMismaFecha
};
