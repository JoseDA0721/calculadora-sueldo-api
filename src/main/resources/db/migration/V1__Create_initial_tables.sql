
-- Crear tipo ENUM para estados de solicitud de pago
CREATE TYPE estado_solicitud_pago AS ENUM ('pendiente', 'aprobada', 'rechazada');

-- Crear tipo ENUM para estados de pago
CREATE TYPE estado_pago AS ENUM ('en_proceso', 'pagado');

-- Crear tipo ENUM para estados de solicitud de pago
CREATE TYPE metodo AS ENUM ('efectivo', 'transferencia');

-- Tabla de roles
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50),
    descripcion TEXT
);

-- Tabla de permisos
CREATE TABLE permisos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    descripcion TEXT
);

-- Tabla puente entre roles y permisos
CREATE TABLE roles_permisos (
    id SERIAL PRIMARY KEY,
    rol_id INT REFERENCES roles(id),
    permiso_id INT REFERENCES permisos(id)
);

-- Tabla de usuarios
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    rol_id INT REFERENCES roles(id),
    user_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de trabajadores
CREATE TABLE trabajadores (
    id VARCHAR(10) PRIMARY KEY,
    nombre VARCHAR(255),
    apellido VARCHAR(255),
    user_id INT,
    actividad VARCHAR(100),
    metodo_pago metodo,
    telegramChatId BIGINT,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    FOREIGN KEY (user_id) REFERENCES usuarios(id)
);

-- Tabla de contratos
CREATE TABLE contratos (
    id SERIAL PRIMARY KEY,
    trabajador_id VARCHAR(20) REFERENCES trabajadores(id),
    tarifa_hora DECIMAL(10,2),
    fecha_inicio DATE,
    fecha_fin DATE,
    activo BOOLEAN DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de registros de horas
CREATE TABLE registros_horas (
    id SERIAL PRIMARY KEY,
    contrato_id INT REFERENCES contratos(id),
    fecha DATE,
    horas_trabajadas DECIMAL(5,2),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de descuentos
CREATE TABLE descuentos (
    id SERIAL PRIMARY KEY,
    contrato_id INT REFERENCES contratos(id),
    descripcion TEXT,
    monto DECIMAL(10,2),
    fecha DATE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de solicitudes de pago (actualizada con ENUM)
CREATE TABLE solicitudes_pago (
    id SERIAL PRIMARY KEY,
    contrato_id INT REFERENCES contratos(id),
    periodo VARCHAR(7), -- YYYY-MM
    estado estado_solicitud_pago, -- ENUM
    fecha_solicitud DATE,
    revisado_por INT REFERENCES users(id),
    observacion TEXT,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de pagos (actualizada con ENUM)
CREATE TABLE pagos (
    id SERIAL PRIMARY KEY,
    solicitud_id INT REFERENCES solicitudes_pago(id),
    sueldo_base DECIMAL(10,2),
    descuentos DECIMAL(10,2),
    total DECIMAL(10,2),
    estado estado_pago, -- ENUM
    fecha_pago DATE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de notificaciones
CREATE TABLE notificaciones (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    mensaje TEXT,
    leido BOOLEAN DEFAULT FALSE,
    tipo VARCHAR(50), -- info, alerta, sistema
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de métricas mensuales
CREATE TABLE metricas_mensuales (
    id SERIAL PRIMARY KEY,
    contrato_id INT REFERENCES contratos(id),
    periodo VARCHAR(7), -- YYYY-MM
    total_horas DECIMAL(10,2),
    total_descuentos DECIMAL(10,2),
    sueldo_neto DECIMAL(10,2),
    generado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
