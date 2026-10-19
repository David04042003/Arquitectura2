/**
 * Carga (o recarga) la colección "tipos" de la base de datos "festivos"
 * con el contenido de datos/tipos.json.
 *
 * Uso: npm run sembrar
 */
const configuracion = require('../src/config/configuracion');
const { conectarBaseDatos, desconectarBaseDatos } = require('../src/config/baseDatos');
const TipoRepositorio = require('../src/repositorios/tipo.repositorio');
const tipos = require('../datos/tipos.json');

async function sembrar() {
    await conectarBaseDatos(configuracion.mongoUri);
    const insertados = await new TipoRepositorio().reemplazarTodos(tipos);
    const totalFestivos = insertados.reduce((total, tipo) => total + tipo.festivos.length, 0);
    console.log(`Se cargaron ${insertados.length} tipos con ${totalFestivos} festivos en la colección "tipos"`);
}

sembrar()
    .catch((error) => {
        console.error('Error cargando los datos:', error.message);
        process.exitCode = 1;
    })
    .finally(desconectarBaseDatos);
