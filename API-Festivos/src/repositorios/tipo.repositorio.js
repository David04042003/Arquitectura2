const Tipo = require('../modelos/tipo.modelo');

/**
 * Repositorio de la colección "tipos". Es la única capa que conoce Mongoose,
 * así los servicios no dependen de la tecnología de persistencia.
 */
class TipoRepositorio {
    /**
     * @returns {Promise<Array<{id:number, tipo:string, modoCalculo:string, festivos:Array}>>}
     */
    async obtenerTodos() {
        return Tipo.find({}, { _id: 0 }).sort({ id: 1 }).lean();
    }

    async obtenerPorId(id) {
        return Tipo.findOne({ id }, { _id: 0 }).lean();
    }

    /**
     * Reemplaza todos los tipos (usado por el script de carga inicial).
     */
    async reemplazarTodos(tipos) {
        await Tipo.deleteMany({});
        return Tipo.insertMany(tipos);
    }
}

module.exports = TipoRepositorio;
