const mongoose = require('mongoose');

/**
 * Abre la conexión con MongoDB (base de datos "festivos").
 * @param {string} uri cadena de conexión
 */
async function conectarBaseDatos(uri) {
    await mongoose.connect(uri, { serverSelectionTimeoutMS: 5000 });
    console.log(`Conectado a MongoDB: ${mongoose.connection.name}`);
}

async function desconectarBaseDatos() {
    await mongoose.disconnect();
}

module.exports = {
    conectarBaseDatos,
    desconectarBaseDatos
};
