const express = require('express');
const router = express.Router();
const {
    getAllOrdenes,
    createOrden,
    getOrden,
    getOrdenesByCliente,
    updateOrden
} = require('../controllers/ordenController');

router.get('/ordenes', getAllOrdenes);
router.post('/orden', createOrden);
router.get('/orden/id/:orden_id', getOrden);                // ✅ Buscar por ID
router.get('/orden/cliente/:cedula', getOrdenesByCliente); // ✅ Buscar por cédula
router.put('/orden/:orden_id', updateOrden);


module.exports = router;