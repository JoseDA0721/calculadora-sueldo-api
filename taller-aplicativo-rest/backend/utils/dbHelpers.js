const { quitoPool } = require('../config/db');

// Esta función ahora es independiente y puede ser importada en cualquier lugar.
const findClientNode = async (cedula) => {
    const result = await quitoPool.query('SELECT ciudad_id FROM clientes_global WHERE cedula = $1', [cedula]);
    if (result.rows.length === 0) {
        const error = new Error('Cliente no encontrado');
        error.statusCode = 404;
        throw error;
    }
    return result.rows[0].ciudad_id;
};

// Exportamos la función para que otros archivos puedan usarla.
module.exports = {
    findClientNode
};