const { Schema, model } = require('mongoose');

/**
 * Festivo embebido dentro del vector "festivos" de cada tipo.
 *  - Tipos 1 y 2: usan dia y mes (diasPascua = 0).
 *  - Tipos 3 y 4: dia y mes en 0 (o null), usan diasPascua (días sumados al domingo de pascua).
 * Compatible con el script BDFestivos.mjs del profesor.
 */
const festivoEsquema = new Schema(
    {
        dia: { type: Number, min: 0, max: 31, default: 0 },
        mes: { type: Number, min: 0, max: 12, default: 0 },
        nombre: { type: String, required: true, trim: true },
        diasPascua: { type: Number, default: 0 }
    },
    { _id: false }
);

const tipoEsquema = new Schema(
    {
        id: { type: Number, required: true, unique: true, min: 1, max: 4 },
        tipo: { type: String, required: true, trim: true },
        modoCalculo: { type: String, trim: true },
        festivos: { type: [festivoEsquema], default: [] }
    },
    { collection: 'tipos', versionKey: false }
);

module.exports = model('Tipo', tipoEsquema);
