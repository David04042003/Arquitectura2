const { describe, test } = require('node:test');
const assert = require('node:assert/strict');
const {
    crearFecha,
    obtenerDomingoPascua,
    obtenerDomingoRamos,
    agregarDias,
    siguienteLunes,
    formatearFecha
} = require('../src/servicios/fechas.servicio');

// Fórmula original del ServicioFechas.js del profesor (Gauss simplificado) para comparar
function domingoRamosOriginal(anio) {
    const a = anio % 19;
    const b = anio % 4;
    const c = anio % 7;
    const d = (19 * a + 24) % 30;
    const dias = d + (2 * b + 4 * c + 6 * d + 5) % 7;
    let dia = 15 + dias;
    let mes = 3;
    if (dia > 31) {
        mes = 4;
        dia = dia - 31;
    }
    return new Date(Date.UTC(anio, mes - 1, dia));
}

describe('fechas.servicio', () => {
    const pascuasConocidas = [
        [2023, '2023-04-09'],
        [2024, '2024-03-31'],
        [2025, '2025-04-20'],
        [2026, '2026-04-05'],
        [1954, '1954-04-18'],
        [1981, '1981-04-19'],
        [1818, '1818-03-22'], // pascua más temprana posible
        [1943, '1943-04-25'], // pascua más tardía posible
        [2285, '2285-03-22']
    ];
    for (const [anio, esperado] of pascuasConocidas) {
        test(`domingo de pascua ${anio} es ${esperado}`, () => {
            assert.equal(formatearFecha(obtenerDomingoPascua(anio)), esperado);
        });
    }

    test('la fórmula original falla en 1954, 1981, 2049 y 2076; la nueva coincide en los demás años 1900-2099', () => {
        const diferencias = [];
        for (let anio = 1900; anio <= 2099; anio++) {
            if (formatearFecha(obtenerDomingoRamos(anio)) !== formatearFecha(domingoRamosOriginal(anio))) {
                diferencias.push(anio);
            }
        }
        // Son las excepciones conocidas del método de Gauss (pascua 25/26 de abril -> 18/19 de abril)
        assert.deepEqual(diferencias, [1954, 1981, 2049, 2076]);
    });

    test('siguienteLunes deja el lunes igual y traslada los demás días', () => {
        assert.equal(formatearFecha(siguienteLunes(crearFecha(2023, 1, 9))), '2023-01-09'); // lunes
        assert.equal(formatearFecha(siguienteLunes(crearFecha(2023, 1, 8))), '2023-01-09'); // domingo
        assert.equal(formatearFecha(siguienteLunes(crearFecha(2023, 1, 10))), '2023-01-16'); // martes
        assert.equal(formatearFecha(siguienteLunes(crearFecha(2023, 12, 26))), '2024-01-01'); // cambio de año
    });

    test('agregarDias no modifica la fecha original', () => {
        const fecha = crearFecha(2024, 2, 28);
        assert.equal(formatearFecha(agregarDias(fecha, 1)), '2024-02-29');
        assert.equal(formatearFecha(agregarDias(fecha, -59)), '2023-12-31');
        assert.equal(formatearFecha(fecha), '2024-02-28');
    });

    const fechasInvalidas = [
        [2023, 2, 29],
        [2023, 2, 30],
        [2023, 4, 31],
        [2023, 13, 1],
        [2023, 0, 10],
        [1500, 1, 1],
        [2023.5, 1, 1],
        [NaN, 1, 1]
    ];
    for (const [anio, mes, dia] of fechasInvalidas) {
        test(`crearFecha(${anio}, ${mes}, ${dia}) es null`, () => {
            assert.equal(crearFecha(anio, mes, dia), null);
        });
    }

    test('crearFecha acepta el 29 de febrero en año bisiesto', () => {
        assert.equal(formatearFecha(crearFecha(2024, 2, 29)), '2024-02-29');
    });
});
