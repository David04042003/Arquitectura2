const configuracion = require('./config/configuracion');
const { conectarBaseDatos, desconectarBaseDatos } = require('./config/baseDatos');
const { cargarDocumentoSwagger } = require('./config/swagger');
const crearApp = require('./app');

async function iniciar() {
    // Si la especificación OpenAPI no es válida la API no arranca
    const documentoSwagger = await cargarDocumentoSwagger();
    await conectarBaseDatos(configuracion.mongoUri);

    const app = crearApp({ documentoSwagger });
    const servidor = app.listen(configuracion.puerto, () => {
        console.log(`API Festivos escuchando en http://localhost:${configuracion.puerto}`);
        console.log(`Swagger UI: http://localhost:${configuracion.puerto}/api-docs`);
    });

    const apagar = () => {
        servidor.close(async () => {
            await desconectarBaseDatos();
            process.exit(0);
        });
    };
    process.on('SIGINT', apagar);
    process.on('SIGTERM', apagar);
}

iniciar().catch((error) => {
    console.error('No fue posible iniciar la API:', error.message);
    process.exit(1);
});
