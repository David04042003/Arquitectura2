require('dotenv').config({ quiet: true });

/**
 * Configuración centralizada de la aplicación (leída de variables de entorno).
 */
const configuracion = Object.freeze({
    puerto: Number(process.env.PUERTO) || 8080,
    mongoUri: process.env.MONGO_URI || 'mongodb://localhost:27017/festivos'
});

module.exports = configuracion;
