/**
 * Error controlado de la aplicación con su código de estado HTTP.
 */
class ErrorAplicacion extends Error {
    constructor(mensaje, codigoEstado = 500) {
        super(mensaje);
        this.name = 'ErrorAplicacion';
        this.codigoEstado = codigoEstado;
    }
}

module.exports = ErrorAplicacion;
