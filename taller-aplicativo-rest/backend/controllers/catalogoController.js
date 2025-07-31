const { quitoPool: catalogPool } = require('../config/db');

exports.getServicios = async (req, res) => {
    try {
        const result = await catalogPool.query('SELECT * FROM servicios ORDER BY nombre');
        res.status(200).json(result.rows);
    } catch (err) { res.status(500).json({ error: err.message }); }
};

exports.getCiudades = async (req, res) => {
     try {
        const result = await catalogPool.query('SELECT * FROM ciudades ORDER BY nombre');
        res.status(200).json(result.rows);
    } catch (err) { res.status(500).json({ error: err.message }); }
};

exports.getTiposVehiculo = async (req, res) => {
     try {
        const result = await catalogPool.query('SELECT * FROM tipos_vehiculos ORDER BY nombre');
        res.status(200).json(result.rows);
    } catch (err) { 
        res.status(500).json({ error: err.message }); 
    }
};

exports.getProductos = async (req, res) => {
    try {
        const result = await catalogPool.query('SELECT * FROM productos ORDER BY nombre');
        res.status(200).json(result.rows);
    } catch (err) {
        res.status(500).json({error: err.message});    
    }    
};

