const { quitoPool: pool } = require('../config/db'); // Los vehículos solo están en Quito

exports.getAllVehiculos = async (req, res) => {
    try {
        const result = await pool.query('SELECT * FROM vehiculos');
        res.status(200).json(result.rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getVehiculoByPlaca = async (req, res) =>{
    const { placa } = req.params;
    try {
        const result = await pool.query('SELECT * FROM vehiculos WHERE placa = $1',[placa]);
        if (result.rows.length === 0) {
            return res.status(404).json({ message: 'Vehiculo no encontrado' });
        }
        res.status(200).json(result.rows);
    } catch (error) {
        res.status(500).json({ error: err.message });
    }
};

exports.getVehiculoByCliente = async (req, res) =>{
    const { cliente_cedula } = req.params;
    console.log('cedula_cliente:', cliente_cedula);
    try {
        const result = await pool.query('SELECT * FROM vehiculos WHERE cliente_cedula = $1',[cliente_cedula]);
        if (result.rows.length === 0) {
            return res.status(404).json({ message: 'Vehiculo no encontrado' });
        }
        res.status(200).json(result.rows);
    } catch (error) {
        res.status(500).json({ error: err.message });
    }
};


exports.createVehiculo = async (req, res) => {
    const { placa, marca, modelo, tipo_id, cliente_cedula } = req.body;
    if (!placa || !marca || !modelo || !tipo_id || !cliente_cedula) {
        return res.status(400).json({ message: 'Todos los campos son requeridos.' });
    }
    try {
        const query = 'INSERT INTO vehiculos (placa, marca, modelo, tipo_id, cliente_cedula) VALUES ($1, $2, $3, $4, $5) RETURNING *';
        const result = await pool.query(query, [placa, marca, modelo, tipo_id, cliente_cedula]);
        res.status(201).json({ message: 'Vehículo creado exitosamente.', data: result.rows[0] });
    } catch (err) {
        // El trigger en la BD se encargará de validar si el cliente_cedula existe
        res.status(500).json({ error: err.message });
    }
};

exports.updateVehiculo = async(req, res) =>{res.status(400).json("No implementado");};

exports.deleteVehiculo = async (req, res) => {
    const { placa } = req.params;
    console.log('Placa: ',placa);
    try {
        const query = 'DELETE FROM vehiculos WHERE placa = $1 RETURNING *';
        const result = await pool.query(query, [placa]);

        // Verificamos si la consulta devolvió la fila que eliminó
        if (result.rowCount === 0) {
            // Si rowCount es 0, no se encontró el vehículo y no se borró nada
            return res.status(404).json({ message: 'Vehículo no encontrado.' });
        }
        
        // Si rowCount es > 0, el vehículo fue eliminado
        res.status(200).json({ message: `Vehículo con placa ${placa} eliminado exitosamente.` });

    } catch (error) {
        res.status(500).json({ error: err.message });
    }  
};

