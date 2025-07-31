const express = require('express');
const cors = require('cors');
require('dotenv').config();

// Importar las rutas
const clienteRoutes = require('./routes/clienteRoutes');
const vehiculoRoutes = require('./routes/vehiculoRoutes');
const ordenRoutes = require('./routes/ordenRoutes');
const catalogoRoutes = require('./routes/catalogoRoutes');


// Inicializar la aplicación de Express
const app = express();

// Middlewares
app.use(cors());
app.use(express.json());

// Mensaje de bienvenida en la ruta raíz
app.get('/', (req, res) => {
    res.send('API del Sistema de Taller Mecánico funcionando correctamente.');
});

// Rutas de la API
app.use('/api', clienteRoutes);
app.use('/api', vehiculoRoutes);
app.use('/api', ordenRoutes);
app.use('/api', catalogoRoutes);


// Definir el puerto y arrancar el servidor
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`Servidor corriendo en http://localhost:${PORT}`);
});
