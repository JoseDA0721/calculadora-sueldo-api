const express = require('express');
const router = express.Router();

// Importar las funciones del controlador de clientes
const {
    getAllClientes,
    getClienteByCedula,
    createCliente,
    updateCliente,
    deleteCliente
} = require('../controllers/clienteController');

// Definir las rutas del CRUD
// GET /api/clientes -> Obtener todos los clientes (consulta distribuida)
router.get('/clientes', getAllClientes);

// GET /api/clientes/:cedula -> Obtener un cliente específico
router.get('/cliente/:cedula', getClienteByCedula);

// POST /api/clientes -> Crear un nuevo cliente (en el nodo correcto)
router.post('/cliente', createCliente);

// PUT /api/clientes/:cedula -> Actualizar un cliente
router.put('/cliente/:cedula', updateCliente);

// DELETE /api/clientes/:cedula -> Eliminar un cliente
router.delete('/cliente/:cedula', deleteCliente);

module.exports = router;
