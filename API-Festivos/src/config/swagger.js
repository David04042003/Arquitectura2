const fs = require('node:fs/promises');
const path = require('node:path');
const YAML = require('yaml');
const SwaggerParser = require('@apidevtools/swagger-parser');

const RUTA_ESPECIFICACION = path.join(__dirname, '..', '..', 'docs', 'openapi.yaml');

/**
 * Lee docs/openapi.yaml y lo valida contra el esquema de OpenAPI 3.
 * Lanza un error si la especificación no es válida.
 * @returns {Promise<object>} documento listo para Swagger UI
 */
async function cargarDocumentoSwagger() {
    const contenido = await fs.readFile(RUTA_ESPECIFICACION, 'utf8');
    const documento = YAML.parse(contenido);
    // validate() puede modificar el objeto (resuelve $ref), por eso se le pasa una copia
    await SwaggerParser.validate(structuredClone(documento));
    return documento;
}

module.exports = {
    RUTA_ESPECIFICACION,
    cargarDocumentoSwagger
};
