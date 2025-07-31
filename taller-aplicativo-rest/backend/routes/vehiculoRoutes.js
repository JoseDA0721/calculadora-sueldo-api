const express = require('express');
const router = express.Router();
const {
    getAllVehiculos,
    getVehiculoByPlaca,
    getVehiculoByCliente,
    createVehiculo,
    updateVehiculo,
    deleteVehiculo
    // ... más funciones si las necesitas (getById, update, delete)
} = require('../controllers/vehiculoController');

router.get('/vehiculos', getAllVehiculos);
router.get('/vehiculo/:placa', getVehiculoByPlaca);
router.get('/vehiculo/cliente/:cliente_cedula', getVehiculoByCliente);
router.post('/vehiculo', createVehiculo);
router.put('/vehiculo/:placa', updateVehiculo);
router.delete('/vehiculo/:placa', deleteVehiculo);

module.exports = router;