# API para Sistema de Gestión de Taller Mecánico

Este repositorio contiene el backend para un sistema de gestión de taller, construido con **Node.js**, **Express** y una arquitectura de base de datos **distribuida sobre PostgreSQL**. El objetivo principal es simular un entorno empresarial real donde los datos se fragmentan geográficamente para mejorar el rendimiento y la disponibilidad local.

---

## 🏛️ Arquitectura de la Base de Datos

El sistema se basa en una base de datos distribuida fragmentada en tres nodos:

### Nodo 1 (Quito): Nodo principal

- Tablas de catálogo y centralizadas (vehículos, servicios, productos)
- Fragmentos de datos de Quito
- Vistas globales (`clientes_global`, `ordenes_trabajo_global`)
- Triggers y funciones para mantener integridad entre nodos

### Nodo 2 (Guayaquil): Nodo secundario

- Fragmentos de datos de la sucursal de Guayaquil

### Nodo 3 (Cuenca): Nodo secundario

- Fragmentos de datos de la sucursal de Cuenca

### Estrategia de Fragmentación:

- **Horizontal**: Ej. `ordenes_trabajo`, `inventario` por `ciudad_id`
- **Mixta (Vertical + Horizontal)**: Ej. `clientes`, `empleados`

### Generación de IDs Únicos:

- Prefijos como `OQ-`, `OG-`, `OC-` según la ciudad para mantener unicidad global

---

## 🚀 Tecnologías Utilizadas

- **Backend**: Node.js
- **Framework**: Express.js
- **Base de Datos**: PostgreSQL
- **ORM / Driver**: node-postgres (`pg`)
- **CORS**: cors
- **Variables de entorno**: dotenv
- **Desarrollo**: nodemon
- **Contenedores**: Docker + Docker Compose

---

## 📅 Instalación y Puesta en Marcha

### Requisitos Previos

- Node.js (v16+)
- NPM
- Git
- Docker y Docker Compose

### Paso 1: Clonar Repositorio
Clona el repositorio como prefieras. Por ejemplo con Git usa:
```bash
git clone <URL_DEL_REPOSITORIO>
cd <nombre_de_la_carpeta_del_proyecto>
```

### Paso 2: Levantar la Base de Datos con Docker

Estructura esperada:

```
/
├── backend/
├── init-db/
│   ├── quito/init.sql
│   ├── guayaquil/init.sql
│   └── cuenca/init.sql
└── docker-compose.yml
```

Levantar los contenedores:

```bash
docker-compose up -d
```

Verificar:

```bash
docker ps
```

Deberías ver: `taller_quito_db`, `taller_guayaquil_db`, `taller_cuenca_db`

### Paso 3: Configurar el Backend

```bash
cd backend
```

Crear archivo `.env` en `/backend`:

```dotenv
NODE_ENV=development
PORT=5000

DB_USER_QUITO=taller_user
DB_HOST_QUITO=localhost
DB_DATABASE_QUITO=taller_db
DB_PASSWORD_QUITO=taller_password_local
DB_PORT_QUITO=5434

DB_USER_GUAYAQUIL=taller_user
DB_HOST_GUAYAQUIL=localhost
DB_DATABASE_GUAYAQUIL=taller_db
DB_PASSWORD_GUAYAQUIL=taller_password_local
DB_PORT_GUAYAQUIL=5435

DB_USER_CUENCA=taller_user
DB_HOST_CUENCA=localhost
DB_DATABASE_CUENCA=taller_db
DB_PASSWORD_CUENCA=taller_password_local
DB_PORT_CUENCA=5436
```

### Paso 4: Instalar Dependencias y Ejecutar

```bash
npm install
npm run dev
```

Salida esperada:

```
Servidor corriendo en http://localhost:5000
```

---

## 📁 Estructura del Proyecto

```
/backend/
├── /config/           # Configuración DB
├── /controllers/      # Lógica de negocio
├── /routes/           # Endpoints
├── /utils/            # Utilidades para DB
├── .env               # Variables de entorno
├── .gitignore
├── package.json
└── server.js          # Entry point
```

---

## 🔗 Endpoints Principales de la API

| Método | Endpoint                        | Descripción                                   |
| ------ | ------------------------------- | --------------------------------------------- |
| GET    | `/api/clientes`                 | Lista global de clientes                      |
| POST   | `/api/clientes`                 | Crear cliente según `ciudad_id`               |
| GET    | `/api/clientes/{cedula}`        | Buscar cliente por cédula                     |
| GET    | `/api/vehiculos`                | Vehículos (Nodo 1)                            |
| POST   | `/api/vehiculos`                | Crear vehículo (valida existencia de cliente) |
| GET    | `/api/ordenes`                  | Lista de órdenes de todos los nodos           |
| POST   | `/api/ordenes`                  | Crear nueva orden (transacción distribuida)   |
| GET    | `/api/catalogos/servicios`      | Lista de servicios                            |
| GET    | `/api/catalogos/ciudades`       | Lista de sucursales                           |
| GET    | `/api/catalogos/tipos-vehiculo` | Tipos de vehículo                             |

---

## 💾 Colección de Postman

Puedes importar esta colección en Postman para probar los endpoints:

- [Descargar colección](https://lunar-station-853811.postman.co/workspace/epn~2b6a9937-709b-4d85-b6ce-91dfc300f794/collection/37362830-af689790-f784-4489-bb57-182208dccafa?action=share&source=copy-link&creator=37362830)

---

## 📊 Uso Desde el Frontend (React)

### Base de Axios

```js
// src/services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:5000/api',
});

export default api;
```

### Ejemplo de Componente React

```jsx
// src/pages/Clientes.jsx
import React, { useEffect, useState } from 'react';
import api from '../services/api';

function Clientes() {
  const [clientes, setClientes] = useState([]);

  useEffect(() => {
    api.get('/clientes')
      .then((res) => setClientes(res.data))
      .catch((err) => console.error(err));
  }, []);

  return (
    <div>
      <h2>Clientes</h2>
      <ul>
        {clientes.map(cliente => (
          <li key={cliente.cedula}>{cliente.nombre}</li>
        ))}
      </ul>
    </div>
  );
}

export default Clientes;
```

---

## 🎉 Listo para Usar

Si seguiste todos los pasos:

- El backend está corriendo en `http://localhost:5000`
- La base de datos distribuida está activa y conectada
- Puedes usar Postman o conectar el frontend React directamente

---

🚀 **Comienza a construir tu frontend!**

