/**
 * Valida la especificación OpenAPI (docs/openapi.yaml) contra el estándar OpenAPI 3.
 *
 * Uso: npm run swagger:validar
 */
const { cargarDocumentoSwagger, RUTA_ESPECIFICACION } = require('../src/config/swagger');

cargarDocumentoSwagger()
    .then((documento) => {
        console.log(`OK: ${RUTA_ESPECIFICACION} es una especificación OpenAPI ${documento.openapi} válida`);
    })
    .catch((error) => {
        console.error(`La especificación OpenAPI no es válida:\n${error.message}`);
        process.exitCode = 1;
    });
