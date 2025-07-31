const { quitoPool, getPoolByCity } = require('../config/db');
const { findClientNode } = require('../utils/dbHelpers');

exports.getAllOrdenes = async (req, res) => {
    const { ciudad } = req.query; 
    try {
        let result;
        let query  = 'SELECT * FROM ordenes_trabajo_global';
        const params = [];
        if(ciudad){
            const ciudadNormalizada = ciudad.toLowerCase();
            const ciudadesValidas = ['quito', 'guayaquil', 'cuenca'];
            if (!ciudadesValidas.includes(ciudadNormalizada)) {
                return res.status(400).json({ message: 'Ciudad inválida' });
            }
            const ciudad_id = ciudadNormalizada === 'quito' ? 1 : ciudadNormalizada === 'guayaquil' ? 2 : 3;
            query += ' WHERE ciudad_id = $1';
            params.push(ciudad_id);
        }
        result = await quitoPool.query(query,params);
        res.status(200).json(result.rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.createOrden = async (req, res) => {
    const { cliente_cedula, placa, fecha, estado, ciudad_id, empleado_cedula, form_pago_id, detalles } = req.body;

    if (!cliente_cedula || !placa || !ciudad_id || !detalles || !detalles.length) {
        return res.status(400).json({ message: 'Faltan datos críticos para crear la orden (cliente, placa, ciudad o detalles).' });
    }

    const ordenPool = getPoolByCity(ciudad_id);
    const centralPool = quitoPool;
    if (!ordenPool) {
        return res.status(400).json({ message: 'ID de ciudad no válido.' });
    }
    
    const finalDetails = [];
    try {
        for (const item of detalles) {
            if (item.servicio_id) {

                const recetaQuery = `
                    SELECT sp.producto_id, p.precio, sp.cantidad 
                    FROM servicio_productos sp
                    JOIN productos p ON sp.producto_id = p.producto_id
                    WHERE sp.servicio_id = $1`;
                const recetaResult = await centralPool.query(recetaQuery, [item.servicio_id]);

                if (recetaResult.rows.length > 0) {

                    recetaResult.rows.forEach(producto => {
                        finalDetails.push({
                            servicio_id: null,
                            producto_id: producto.producto_id,
                            cantidad: producto.cantidad,
                            precio: producto.precio * producto.cantidad
                        });
                    });
                }
                finalDetails.push({
                    servicio_id: item.servicio_id,
                    producto_id: null,
                    cantidad: 1,
                    precio: item.precio
                });

            } else if (item.producto_id) {
                finalDetails.push(item);
            }
        }
    } catch (err) {
        return res.status(500).json({ error: 'Error al procesar los servicios: ' + err.message });
    }

    const client = await ordenPool.connect();
    const centralClient = await centralPool.connect();
    
    try {
        await client.query('BEGIN');
        await centralClient.query('BEGIN');

        const suffix = ciudad_id === 1 ? 'quito' : ciudad_id === 2 ? 'guayaquil' : 'cuenca';
        const ordenQuery = `INSERT INTO ordenes_trabajo_${suffix} (cliente_cedula, placa, fecha, estado, ciudad_id, empleado_cedula, form_pago_id) VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING orden_id`;
        const ordenResult = await client.query(ordenQuery, [cliente_cedula, placa, fecha, estado, ciudad_id, empleado_cedula, form_pago_id]);
        const newOrdenId = ordenResult.rows[0].orden_id;

        await centralClient.query('ALTER TABLE detalles_orden DISABLE TRIGGER trg_check_orden_before_insert;');

        for (const detalle of finalDetails) {
            const detalleQuery = 'INSERT INTO detalles_orden (orden_id, servicio_id, producto_id, cantidad, precio) VALUES ($1, $2, $3, $4, $5)';
            await centralClient.query(detalleQuery, [newOrdenId, detalle.servicio_id, detalle.producto_id, detalle.cantidad, detalle.precio]);
        }
        
        await centralClient.query('ALTER TABLE detalles_orden ENABLE TRIGGER trg_check_orden_before_insert;');
        
        await client.query('COMMIT');
        await centralClient.query('COMMIT');
        
        res.status(201).json({ message: `Orden ${newOrdenId} creada exitosamente con todos sus productos.` });
    } catch (err) {

        await client.query('ROLLBACK');
        await centralClient.query('ROLLBACK');

        try {
            await centralClient.query('ALTER TABLE detalles_orden ENABLE TRIGGER trg_check_orden_before_insert;');
        } catch (enableErr) {
            console.error('Error al reactivar el trigger después de un fallo:', enableErr);
        }

        res.status(500).json({ error: 'Error en la transacción de la orden: ' + err.message });
    } finally {
        client.release();
        centralClient.release();
    }
};

exports.getOrden = async (req, res) => {
    const { orden_id } = req.params;
    console.log('orden_id:', orden_id);
    try {
        const query = `SELECT * FROM ordenes_trabajo_global WHERE orden_id = $1`
        const result = await quitoPool.query(query, [orden_id]);
        if(result.rows.length === 0){
            return res.status(400).json({ message: 'ID de ciudad no válido.' });
        }
        res.status(200).json(result.rows);
    } catch (error) {
        res.status(500).json({error: error.message});
    }  
};

exports.getOrdenesByCliente = async (req, res) => {
    try {
        const { cedula } = req.params;
        await findClientNode(cedula);
        const query = `SELECT * FROM ordenes_trabajo_global WHERE cliente_cedula = $1`;
        const result = await quitoPool.query(query, [cedula]);
        res.status(200).json(result.rows);
    } catch (error) {
        res.status(500).json({error: error.message});
    }
};

exports.updateOrden = async (req, res) => {
    const { orden_id } = req.params; 
    const { estado } = req.body;

    let pool;
    let client;
    try {
        const result = await quitoPool.query('SELECT ciudad_id FROM ordenes_trabajo_global WHERE orden_id = $1', [orden_id]);
        if(result.rows.length === 0){
            return res.status(404).json({message: 'Orden no encontrada'});
        }
        const ciudad_id = result.rows[0].ciudad_id;
        console.log('Ciudad: ', ciudad_id);
        const suffix = ciudad_id === 1 ? 'quito': ciudad_id === 2 ? 'guayaquil': 'cuenca';
        
        pool = getPoolByCity(ciudad_id);
        client = await pool.connect();

        await client.query('BEGIN');

        //Actualizar estado
        const queryEstado = `UPDATE ordenes_trabajo_${suffix}  SET estado = $1 WHERE orden_id = $2`;
        await client.query(queryEstado, [estado, orden_id]);

        await client.query('COMMIT');
        res.status(201).json({ message: `Orden actualizada exitosamente en ${suffix}.`, data: req.body });
    } catch (error) {
        await client.query('ROLLBACK');
        res.status(500).json({ error: error.message });
    } finally {
        client.release();
    }
};
