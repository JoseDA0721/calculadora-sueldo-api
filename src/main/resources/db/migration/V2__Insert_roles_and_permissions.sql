-- ===============================
-- V2 - Seed Data: Roles & Permisos
-- ===============================

-- Insertar roles base del sistema
INSERT INTO roles (nombre, descripcion) VALUES
                                            ('Administrador', 'Acceso completo al sistema'),
                                            ('Empleado', 'Acceso limitado a su propia información');

-- Insertar permisos base
INSERT INTO permisos (nombre, descripcion) VALUES
                                               ('ver_trabajadores', 'Puede ver la lista de trabajadores'),
                                               ('editar_trabajadores', 'Puede editar información de los trabajadores'),
                                               ('ver_solicitudes_pago', 'Puede ver solicitudes de pago'),
                                               ('aprobar_solicitudes_pago', 'Puede aprobar o rechazar solicitudes de pago'),
                                               ('ver_mis_registros', 'Empleado puede ver sus propios registros de horas');

-- Asignar permisos al rol: Administrador (id = 1)
INSERT INTO roles_permisos (rol_id, permiso_id) VALUES
                                                    (1, 1), -- ver_trabajadores
                                                    (1, 2), -- editar_trabajadores
                                                    (1, 3), -- ver_solicitudes_pago
                                                    (1, 4), -- aprobar_solicitudes_pago
                                                    (1, 5); -- ver_mis_registros

-- Asignar permisos al rol: Empleado (id = 2)
INSERT INTO roles_permisos (rol_id, permiso_id) VALUES
                                                    (2, 3), -- ver_solicitudes_pago
                                                    (2, 5); -- ver_mis_registros
