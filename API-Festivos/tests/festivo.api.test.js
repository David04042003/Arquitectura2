const { describe, test, before, mock } = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');
const crearApp = require('../src/app');
const { cargarDocumentoSwagger } = require('../src/config/swagger');
const tipos = require('../datos/tipos.json');

// Repositorio falso: las pruebas no necesitan MongoDB
const repositorioFalso = { obtenerTodos: async () => structuredClone(tipos) };

const FESTIVOS_2023 = [
    { festivo: 'Año nuevo', fecha: '2023-01-01' },
    { festivo: 'Santos Reyes', fecha: '2023-01-09' },
    { festivo: 'San José', fecha: '2023-03-20' },
    { festivo: 'Jueves Santo', fecha: '2023-04-06' },
    { festivo: 'Viernes Santo', fecha: '2023-04-07' },
    { festivo: 'Domingo de Pascua', fecha: '2023-04-09' },
    { festivo: 'Día del Trabajo', fecha: '2023-05-01' },
    { festivo: 'Ascensión del Señor', fecha: '2023-05-22' },
    { festivo: 'Corpus Christi', fecha: '2023-06-12' },
    { festivo: 'Sagrado Corazón de Jesús', fecha: '2023-06-19' },
    { festivo: 'San Pedro y San Pablo', fecha: '2023-07-03' },
    { festivo: 'Independencia Colombia', fecha: '2023-07-20' },
    { festivo: 'Batalla de Boyacá', fecha: '2023-08-07' },
    { festivo: 'Asunción de la Virgen', fecha: '2023-08-21' },
    { festivo: 'Día de la Raza', fecha: '2023-10-16' },
    { festivo: 'Todos los santos', fecha: '2023-11-06' },
    { festivo: 'Independencia de Cartagena', fecha: '2023-11-13' },
    { festivo: 'Inmaculada Concepción', fecha: '2023-12-08' },
    { festivo: 'Navidad', fecha: '2023-12-25' }
];

describe('API Festivos', () => {
    let app;

    before(async () => {
        app = crearApp({ tipoRepositorio: repositorioFalso, documentoSwagger: await cargarDocumentoSwagger() });
    });

    describe('GET /api/festivos/verificar/:anio/:mes/:dia', () => {
        const casos = [
            ['2023/1/1', 'Es Festivo'],
            ['2023/01/09', 'Es Festivo'],
            ['2023/1/6', 'No es festivo'], // Reyes se traslada al lunes 9
            ['2023/5/22', 'Es Festivo'],
            ['2023/1/2', 'No es festivo']
        ];
        for (const [fecha, esperado] of casos) {
            test(`${fecha} -> ${esperado}`, async () => {
                const respuesta = await request(app).get(`/api/festivos/verificar/${fecha}`);
                assert.equal(respuesta.status, 200);
                assert.equal(respuesta.type, 'text/plain');
                assert.equal(respuesta.text, esperado);
            });
        }

        for (const fecha of ['2023/2/29', '2023/13/1', '2023/4/31', 'abc/1/1', '2023/1/1x', '2023/-1/1']) {
            test(`${fecha} -> Fecha No valida`, async () => {
                const respuesta = await request(app).get(`/api/festivos/verificar/${fecha}`);
                assert.equal(respuesta.status, 400);
                assert.equal(respuesta.text, 'Fecha No valida');
            });
        }
    });

    describe('GET /api/festivos/obtener/:anio', () => {
        test('retorna los festivos de 2023 ordenados por fecha', async () => {
            const respuesta = await request(app).get('/api/festivos/obtener/2023');
            assert.equal(respuesta.status, 200);
            assert.deepEqual(respuesta.body, FESTIVOS_2023);
        });

        test('año no válido responde 400', async () => {
            const respuesta = await request(app).get('/api/festivos/obtener/20x3');
            assert.equal(respuesta.status, 400);
            assert.match(respuesta.body.mensaje, /Año no válido/);
        });
    });

    test('error del repositorio responde 500', async () => {
        mock.method(console, 'error', () => {});
        const appConError = crearApp({
            tipoRepositorio: { obtenerTodos: async () => { throw new Error('Mongo caído'); } }
        });
        const respuesta = await request(appConError).get('/api/festivos/obtener/2023');
        mock.restoreAll();
        assert.equal(respuesta.status, 500);
        assert.deepEqual(respuesta.body, { mensaje: 'Error interno del servidor' });
    });

    test('ruta inexistente responde 404', async () => {
        const respuesta = await request(app).get('/api/otra');
        assert.equal(respuesta.status, 404);
    });

    test('expone la especificación OpenAPI validada y Swagger UI', async () => {
        const json = await request(app).get('/api-docs.json');
        assert.equal(json.status, 200);
        assert.ok(json.body.paths['/api/festivos/obtener/{anio}']);

        const ui = await request(app).get('/api-docs/');
        assert.equal(ui.status, 200);
        assert.match(ui.text, /swagger-ui/);
    });
});
