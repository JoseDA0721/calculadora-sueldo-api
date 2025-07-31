const express = require('express');
const router = express.Router();
const {
    getServicios,
    getCiudades,
    getTiposVehiculo,
    getProductos
} = require('../controllers/catalogoController');

router.get('/servicios', getServicios);
router.get('/ciudades', getCiudades);
router.get('/tipos-vehiculo', getTiposVehiculo);
router.get('/productos', getProductos);

module.exports = router;