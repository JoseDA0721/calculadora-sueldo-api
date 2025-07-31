// Usamos la librería 'pg' para conectar con PostgreSQL
const { Pool } = require('pg');
// Usamos 'dotenv' para cargar las variables de entorno desde el archivo .env
require('dotenv').config();

// Pool de conexión para el Nodo 1 (Quito)
// Este será el nodo principal para consultas globales y tablas centralizadas
const quitoPool = new Pool({
    user: process.env.DB_USER_QUITO,
    host: process.env.DB_HOST_QUITO,
    database: process.env.DB_DATABASE_QUITO,
    password: process.env.DB_PASSWORD_QUITO,
    port: process.env.DB_PORT_QUITO,
});

// Pool de conexión para el Nodo 2 (Guayaquil)
const guayaquilPool = new Pool({
    user: process.env.DB_USER_GUAYAQUIL,
    host: process.env.DB_HOST_GUAYAQUIL,
    database: process.env.DB_DATABASE_GUAYAQUIL,
    password: process.env.DB_PASSWORD_GUAYAQUIL,
    port: process.env.DB_PORT_GUAYAQUIL,
});

// Pool de conexión para el Nodo 3 (Cuenca)
const cuencaPool = new Pool({
    user: process.env.DB_USER_CUENCA,
    host: process.env.DB_HOST_CUENCA,
    database: process.env.DB_DATABASE_CUENCA,
    password: process.env.DB_PASSWORD_CUENCA,
    port: process.env.DB_PORT_CUENCA,
});

// Función para seleccionar la pool correcta según la ciudad
const getPoolByCity = (cityId) => {
    const id = parseInt(cityId, 10);
    switch (id) {
        case 1:
            return quitoPool;
        case 2:
            return guayaquilPool;
        case 3:
            return cuencaPool;
        default:
            // Por defecto, usamos la pool principal (Quito)
            return quitoPool;
    }
};

module.exports = {
    quitoPool,
    guayaquilPool,
    cuencaPool,
    getPoolByCity
};
