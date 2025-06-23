-- Tabla de roles
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       nombre VARCHAR(50) UNIQUE ,
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
                          user_name VARCHAR(100) UNIQUE,
                          password VARCHAR(100),
                          creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de trabajadores
CREATE TABLE trabajadores (
                              id VARCHAR(10) PRIMARY KEY,
                              nombre VARCHAR(255),
                              apellido VARCHAR(255),
                              user_id INT UNIQUE REFERENCES usuarios(id),
                              actividad VARCHAR(100),
                              metodo VARCHAR(20) CHECK (metodo IN ('EFECTIVO', 'TRANSFERENCIA')) DEFAULT 'TRANSFERENCIA',
                              email VARCHAR(255) UNIQUE,
                              telefono VARCHAR(10) UNIQUE,
                              telegram_chat_id BIGINT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de contratos
CREATE TABLE contratos (
                           id SERIAL PRIMARY KEY,
                           trabajador_id VARCHAR(10) REFERENCES trabajadores(id),
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
                                 horas_trabajadas DECIMAL(10,2),
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

-- Tabla de solicitudes de pago
CREATE TABLE solicitudes_pago (
                                  id SERIAL PRIMARY KEY,
                                  contrato_id INT REFERENCES contratos(id),
                                  periodo VARCHAR(7),
                                  estado VARCHAR(20) CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')) DEFAULT 'PENDIENTE',
                                  fecha_solicitud DATE,
                                  revisado_por INT REFERENCES usuarios(id),
                                  observacion TEXT,
                                  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de pagos
CREATE TABLE pagos (
                       id SERIAL PRIMARY KEY,
                       solicitud_id INT REFERENCES solicitudes_pago(id) UNIQUE,
                       sueldo_base DECIMAL(10,2),
                       discuentos DECIMAL(10,2),
                       total DECIMAL(10,2),
                       estado VARCHAR(20) CHECK (estado IN ('EN_PROCESO', 'PAGADO')) DEFAULT 'EN_PROCESO',
                       fecha_pago DATE,
                       creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de notificaciones
CREATE TABLE notificaciones (
                                id SERIAL PRIMARY KEY,
                                user_id INT REFERENCES usuarios(id),
                                mensaje TEXT,
                                leido BOOLEAN DEFAULT FALSE,
                                fecha DATE
);

-- Tabla de métricas mensuales
CREATE TABLE metricas_mensuales (
                                    id SERIAL PRIMARY KEY,
                                    contrato_id INT REFERENCES contratos(id),
                                    periodo VARCHAR(7),
                                    total_horas DECIMAL(10,2),
                                    sueldo_neto DECIMAL(10,2),
                                    total_descuento DECIMAL(10,2),
                                    generado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
