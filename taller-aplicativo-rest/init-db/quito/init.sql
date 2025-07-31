CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- Parte 1: Creación de Tablas
-- TABLAS CENTRALIZADAS
CREATE TABLE ciudades (id_ciudad INT PRIMARY KEY, nombre VARCHAR);
CREATE TABLE formas_pago (forma_pago_id INT PRIMARY KEY, metodo VARCHAR);
CREATE TABLE tipos_vehiculos (tipo_id INT PRIMARY KEY, nombre VARCHAR);
CREATE TABLE productos (producto_id INT PRIMARY KEY, nombre VARCHAR, precio INT);
CREATE TABLE servicios (servicio_id INT PRIMARY KEY, nombre VARCHAR, precio NUMERIC);
CREATE TABLE servicio_productos (id SERIAL PRIMARY KEY, servicio_id INT REFERENCES servicios(servicio_id), producto_id INT REFERENCES productos(producto_id), cantidad INT);
CREATE TABLE vehiculos (placa VARCHAR PRIMARY KEY, tipo_id INT REFERENCES tipos_vehiculos(tipo_id), marca VARCHAR, modelo VARCHAR, cliente_cedula VARCHAR);
CREATE TABLE detalles_orden (detalle_id SERIAL PRIMARY KEY, orden_id VARCHAR(20), servicio_id INT, producto_id INT, cantidad INT, precio NUMERIC);

-- FRAGMENTOS DE QUITO
CREATE TABLE info_empleados_quito (empleado_cedula VARCHAR PRIMARY KEY, nombre VARCHAR, ciudad_id INT);
CREATE TABLE rol_empleados_quito (empleado_cedula VARCHAR PRIMARY KEY REFERENCES info_empleados_quito(empleado_cedula), rol VARCHAR);
CREATE TABLE info_clientes_quito (cedula VARCHAR PRIMARY KEY, nombre VARCHAR, ciudad_id INT);
CREATE TABLE telefono_clientes_quito (cedula VARCHAR PRIMARY KEY REFERENCES info_clientes_quito(cedula), telefono VARCHAR);
CREATE TABLE correo_clientes_quito (cedula VARCHAR PRIMARY KEY REFERENCES info_clientes_quito(cedula), correo VARCHAR);
CREATE TABLE inventario_quito (inventario_id INT PRIMARY KEY, producto_id INT, ciudad_id INT, stock INT);

-- LÓGICA PARA IDs DE ORDEN PERSONALIZADOS EN QUITO
CREATE SEQUENCE orden_quito_seq START 1;
CREATE OR REPLACE FUNCTION generar_id_orden_quito() RETURNS VARCHAR AS $$
BEGIN RETURN 'OQ-' || nextval('orden_quito_seq'); END;
$$ LANGUAGE plpgsql;

CREATE TABLE ordenes_trabajo_quito (
    orden_id VARCHAR(20) PRIMARY KEY DEFAULT generar_id_orden_quito(),
    cliente_cedula VARCHAR, placa VARCHAR, fecha DATE, estado VARCHAR, ciudad_id INT,
    empleado_cedula VARCHAR, form_pago_id INT
);

-- Parte 2: Inserción de Datos
-- DATOS DE CATÁLOGO Y VEHÍCULOS (Centralizados)
INSERT INTO ciudades VALUES (1, 'Quito'), (2, 'Guayaquil'), (3, 'Cuenca');
INSERT INTO formas_pago VALUES (1, 'Efectivo'), (2, 'Tarjeta de Crédito'), (3, 'Transferencia Bancaria');
INSERT INTO tipos_vehiculos VALUES (1, 'Sedán'), (2, 'SUV'), (3, 'Camioneta');
INSERT INTO productos (producto_id, nombre, precio) VALUES (1,'Aceite de Motor 10W-30',25),(2,'Filtro de Aceite',8),(3,'Filtro de Aire',15),(4,'Bujía de Iridio',12),(5,'Pastillas de Freno Delanteras',45),(6,'Pastillas de Freno Traseras',40),(7,'Disco de Freno',60),(8,'Batería 12V',110),(9,'Líquido de Frenos DOT4',9),(10,'Refrigerante 50/50',20),(11,'Amortiguador Delantero',80),(12,'Amortiguador Trasero',75),(13,'Neumático 205/55 R16',95),(14,'Kit de Embrague',250),(15,'Correa de Distribución',55),(16,'Bomba de Agua',70),(17,'Termostato',22),(18,'Radiador',180),(19,'Alternador',220),(20,'Motor de Arranque',190),(21,'Faro Delantero Halógeno',65),(22,'Luz Trasera LED',50),(23,'Plumillas Limpiaparabrisas (par)',18),(24,'Sensor de Oxígeno',48),(25,'Válvula EGR',90),(26,'Inyector de Combustible',130),(27,'Bomba de Combustible',150),(28,'Rodamiento de Rueda',42),(29,'Junta Homocinética',85),(30,'Soporte de Motor',60);
INSERT INTO servicios (servicio_id, nombre, precio) VALUES (1,'Cambio de Aceite y Filtro',35.00),(2,'Alineación y Balanceo',40.00),(3,'Revisión de Frenos',25.00),(4,'Cambio de Pastillas de Freno',50.00),(5,'Rectificación de Discos de Freno',60.00),(6,'Sincronización de Motor',80.00),(7,'Limpieza de Inyectores',70.00),(8,'Cambio de Batería',20.00),(9,'Revisión Sistema Eléctrico',45.00),(10,'Cambio de Amortiguadores (par)',90.00),(11,'Cambio de Llantas (por llanta)',15.00),(12,'Reparación de Fuga de Refrigerante',100.00),(13,'Cambio de Kit de Embrague',300.00),(14,'Cambio de Correa de Distribución',250.00),(15,'Diagnóstico con Escáner',30.00),(16,'Mantenimiento Preventivo 10.000km',120.00),(17,'Mantenimiento Preventivo 50.000km',280.00),(18,'Reparación de Aire Acondicionado',150.00),(19,'Carga de Gas de Aire Acondicionado',65.00),(20,'Cambio de Radiador',130.00),(21,'Reparación de Alternador',110.00),(22,'Cambio de Motor de Arranque',100.00),(23,'Lavado de Chasis y Motor',25.00),(24,'Revisión Pre-Viaje',35.00),(25,'Cambio de Bomba de Combustible',180.00),(26,'Reparación de Dirección',220.00),(27,'Cambio de Soportes de Motor',95.00),(28,'Reparación de Caja de Cambios',500.00),(29,'Pulida de Faros',30.00),(30,'Revisión de Gases',20.00);
INSERT INTO servicio_productos (servicio_id, producto_id, cantidad) VALUES (1,1,1),(1,2,1),(4,5,1),(4,9,1),(5,7,2),(5,9,1),(6,4,4),(6,3,1),(8,8,1),(10,11,2),(11,13,1),(12,10,1),(13,14,1),(14,15,1),(14,16,1),(20,18,1),(20,10,1),(22,20,1),(25,27,1),(27,30,1);
INSERT INTO vehiculos (placa, tipo_id, marca, modelo, cliente_cedula) VALUES ('PBA-0101',1,'Chevrolet','Sail','1712345678'),('PBA-0102',2,'Kia','Sportage','1723456789'),('PBA-0103',3,'Ford','Ranger','1734567890'),('PBA-0104',1,'Hyundai','Accent','1745678901'),('PBA-0105',2,'Mazda','CX-5','1756789012'),('PBA-0106',3,'Toyota','Hilux','1767890123'),('PBA-0107',1,'Nissan','Versa','1778901234'),('PBA-0108',2,'Renault','Duster','1789012345'),('PBA-0109',1,'Volkswagen','Virtus','1790123456'),('PBA-0110',3,'Chevrolet','D-Max','1701234567'),('PBA-0111',2,'Great Wall','Haval H6','1711223344'),('PBA-0112',1,'Suzuki','Swift','1722334455'),('PBA-0113',2,'Chery','Tiggo 7 Pro','1733445566'),('PBA-0114',3,'JAC','T8','1744556677'),('PBA-0115',1,'Kia','Soluto','1755667788'),('PBA-0116',2,'Hyundai','Tucson','1766778899'),('PBA-0117',1,'Toyota','Yaris','1777889900'),('PBA-0118',3,'Nissan','Frontier','1788990011'),('PBA-0119',2,'Chevrolet','Tracker','1799001122'),('PBA-0120',1,'Renault','Logan','1700112233'),('PBA-0121',3,'Mitsubishi','L200','1712131415'),('PBA-0122',2,'Kia','Sonet','1723242526'),('PBA-0123',1,'Hyundai','Grand i10','1734353637'),('PBA-0124',2,'Suzuki','Grand Vitara','1745464748'),('PBA-0125',1,'Changan','Alsvin','1756575859'),('PBA-0126',2,'JAC','JS4','1767686970'),('PBA-0127',3,'Great Wall','Wingle 7','1778798081'),('PBA-0128',1,'Chevrolet','Onix','1789809192'),('PBA-0129',2,'Toyota','RAV4','1790910203'),('PBA-0130',1,'Mazda','Mazda 2','1701021314'),('GBA-0201',1,'Chevrolet','Spark','0912345678'),('GBA-0202',2,'Kia','Seltos','0923456789'),('GBA-0203',3,'Ford','F-150','0934567890'),('GBA-0204',1,'Hyundai','i10','0945678901'),('GBA-0205',2,'Mazda','CX-30','0956789012'),('GBA-0206',3,'Toyota','Tacoma','0967890123'),('GBA-0207',1,'Nissan','Sentra','0978901234'),('GBA-0208',2,'Renault','Captur','0989012345'),('GBA-0209',1,'Volkswagen','Jetta','0990123456'),('GBA-0210',3,'Chevrolet','Silverado','0901234567'),('GBA-0211',2,'Great Wall','Poer','0911223344'),('GBA-0212',1,'Suzuki','Baleno','0922334455'),('GBA-0213',2,'Chery','Tiggo 2 Pro','0933445566'),('GBA-0214',3,'JAC','T6','0944556677'),('GBA-0215',1,'Kia','Rio','0955667788'),('GBA-0216',2,'Hyundai','Creta','0966778899'),('GBA-0217',1,'Toyota','Corolla','0977889900'),('GBA-0218',3,'Nissan','Titan','0988990011'),('GBA-0219',2,'Chevrolet','Groove','0999001122'),('GBA-0220',1,'Renault','Sandero','0900112233'),('GBA-0221',3,'Mitsubishi','Triton','0912131415'),('GBA-0222',2,'Kia','Stonic','0923242526'),('GBA-0223',1,'Hyundai','Verna','0934353637'),('GBA-0224',2,'Suzuki','S-Cross','0945464748'),('GBA-0225',1,'Changan','Eado','0956575859'),('GBA-0226',2,'JAC','JS3','0967686970'),('GBA-0227',3,'Great Wall','Wingle 5','0978798081'),('GBA-0228',1,'Chevrolet','Aveo','0989809192'),('GBA-0229',2,'Toyota','Corolla Cross','0990910203'),('GBA-0230',1,'Mazda','Mazda 3','0901021314'),('ABA-0301',1,'Chevrolet','Beat','0101234567'),('ABA-0302',2,'Kia','Niro','0102345678'),('ABA-0303',3,'Ford','Ranger Raptor','0103456789'),('ABA-0304',1,'Hyundai','Elantra','0104567890'),('ABA-0305',2,'Mazda','CX-9','0105678901'),('ABA-0306',3,'Toyota','Tundra','0106789012'),('ABA-0307',1,'Nissan','Altima','0107890123'),('ABA-0308',2,'Renault','Koleos','0108901234'),('ABA-0309',1,'Volkswagen','Polo','0109012345'),('ABA-0310',3,'Chevrolet','Colorado','0110123456'),('ABA-0311',2,'Great Wall','Jolion','0111234567'),('ABA-0312',1,'Suzuki','Ciaz','0112345678'),('ABA-0313',2,'Chery','Tiggo 8 Pro','0113456789'),('ABA-0314',3,'JAC','Hunter','0114567890'),('ABA-0315',1,'Kia','Picanto','0115678901'),('ABA-0316',2,'Hyundai','Santa Fe','0116789012'),('ABA-0317',1,'Toyota','Prius','0117890123'),('ABA-0318',3,'Nissan','Navara','0118901234'),('ABA-0319',2,'Chevrolet','Captiva','0119012345'),('ABA-0320',1,'Renault','Kwid','0120123456'),('ABA-0321',3,'Mitsubishi','Montero Sport','0121234567'),('ABA-0322',2,'Kia','Carnival','0122345678'),('ABA-0323',1,'Hyundai','Sonata','0123456789'),('ABA-0324',2,'Suzuki','Jimny','0124567890'),('ABA-0325',1,'Changan','CS35 Plus','0125678901'),('ABA-0326',2,'JAC','JS8','0126789012'),('ABA-0327',3,'Great Wall','Cannon','0127890123'),('ABA-0328',1,'Chevrolet','Joy','0128901234'),('ABA-0329',2,'Toyota','Land Cruiser','0129012345'),('ABA-0330',1,'Mazda','Mazda 6','0130123456');

-- DATOS PARA FRAGMENTOS DE QUITO
INSERT INTO info_empleados_quito (empleado_cedula, nombre, ciudad_id) VALUES ('1700000001','Carlos Andrade',1),('1700000002','Lucía Benítez',1),('1700000003','Miguel Castro',1),('1700000004','Verónica Durán',1),('1700000005','Esteban Flores',1),('1700000006','Gabriela Gómez',1),('1700000007','Ricardo Herrera',1),('1700000008','Isabel Jiménez',1),('1700000009','Fernando Larrea',1),('1700000010','Mónica Moreno',1),('1700000011','Patricio Núñez',1),('1700000012','Olivia Paredes',1),('1700000013','Santiago Quizhpe',1),('1700000014','Raquel Ramírez',1),('1700000015','Tomás Salazar',1),('1700000016','Úrsula Torres',1),('1700000017','Vicente Ulloa',1),('1700000018','Wendy Vargas',1),('1700000019','Xavier Yánez',1),('1700000020','Yolanda Zambrano',1),('1700000021','Andrés Acosta',1),('1700000022','Beatriz Bravo',1),('1700000023','César Cárdenas',1),('1700000024','Daniela Delgado',1),('1700000025','Eduardo Espinosa',1),('1700000026','Fernanda Falconí',1),('1700000027','Gregorio Guerrero',1),('1700000028','Hilda Hidalgo',1),('1700000029','Iván Ibarra',1),('1700000030','Jessica Jaramillo',1);
INSERT INTO rol_empleados_quito (empleado_cedula, rol) VALUES ('1700000001','Gerente de Taller'),('1700000002','Asesora de Servicio'),('1700000003','Mecánico Jefe'),('1700000004','Asesora de Servicio'),('1700000005','Mecánico A'),('1700000006','Recepcionista'),('1700000007','Mecánico B'),('1700000008','Contadora'),('1700000009','Mecánico C'),('1700000010','Jefa de Repuestos'),('1700000011','Mecánico A'),('1700000012','Asistente Administrativa'),('1700000013','Mecánico B'),('1700000014','Cajera'),('1700000015','Mecánico A'),('1700000016','Asesora de Servicio'),('1700000017','Mecánico C'),('1700000018','Pasante Mecánica'),('1700000019','Bodeguero'),('1700000020','Personal de Limpieza'),('1700000021','Mecánico B'),('1700000022','Asesora de Servicio'),('1700000023','Mecánico A'),('1700000024','Recepcionista'),('1700000025','Mecánico C'),('1700000026','Cajera'),('1700000027','Mecánico B'),('1700000028','Asistente Contable'),('1700000029','Bodeguero'),('1700000030','Mecánico A');
INSERT INTO info_clientes_quito (cedula, nombre, ciudad_id) VALUES ('1712345678','Ana Paredes',1),('1723456789','Bruno Saltos',1),('1734567890','Carla Trujillo',1),('1745678901','David Villacís',1),('1756789012','Elena Yánez',1),('1767890123','Fabián Zambrano',1),('1778901234','Gloria Acosta',1),('1789012345','Hugo Bravo',1),('1790123456','Inés Cárdenas',1),('1701234567','Jorge Delgado',1),('1711223344','Karina Espinosa',1),('1722334455','Luis Falconí',1),('1733445566','María Guerrero',1),('1744556677','Nancy Hidalgo',1),('1755667788','Oscar Ibarra',1),('1766778899','Paola Jaramillo',1),('1777889900','Quintin King',1),('1788990011','Rosa López',1),('1799001122','Saúl Mendoza',1),('1700112233','Tania Navarro',1),('1712131415','Ulises Orellana',1),('1723242526','Violeta Ponce',1),('1734353637','Walter Quezada',1),('1745464748','Ximena Ríos',1),('1756575859','Yadira Suárez',1),('1767686970','Zacarías Tufiño',1),('1778798081','Amelia Valdivieso',1),('1789809192','Benito zurita',1),('1790910203','Catalina Armijos',1),('1701021314','Darío Bustamante',1);
INSERT INTO telefono_clientes_quito (cedula, telefono) VALUES ('1712345678','0991234567'),('1723456789','0987654321'),('1734567890','0998765432'),('1745678901','0976543210'),('1756789012','0965432109'),('1767890123','0954321098'),('1778901234','0943210987'),('1789012345','0932109876'),('1790123456','0921098765'),('1701234567','0910987654'),('1711223344','0991122334'),('1722334455','0982233445'),('1733445566','0973344556'),('1744556677','0964455667'),('1755667788','0955667788'),('1766778899','0946677889'),('1777889900','0937788990'),('1788990011','0928899001'),('1799001122','0919900112'),('1700112233','0900112233'),('1712131415','0912131415'),('1723242526','0923242526'),('1734353637','0934353637'),('1745464748','0945464748'),('1756575859','0956575859'),('1767686970','0967686970'),('1778798081','0978798081'),('1789809192','0989809192'),('1790910203','0990910203'),('1701021314','0901021314');
INSERT INTO correo_clientes_quito (cedula, correo) VALUES ('1712345678','ana.p@mail.com'),('1723456789','bruno.s@mail.com'),('1734567890','carla.t@mail.com'),('1745678901','david.v@mail.com'),('1756789012','elena.y@mail.com'),('1767890123','fabian.z@mail.com'),('1778901234','gloria.a@mail.com'),('1789012345','hugo.b@mail.com'),('1790123456','ines.c@mail.com'),('1701234567','jorge.d@mail.com'),('1711223344','karina.e@mail.com'),('1722334455','luis.f@mail.com'),('1733445566','maria.g@mail.com'),('1744556677','nancy.h@mail.com'),('1755667788','oscar.i@mail.com'),('1766778899','paola.j@mail.com'),('1777889900','quintin.k@mail.com'),('1788990011','rosa.l@mail.com'),('1799001122','saul.m@mail.com'),('1700112233','tania.n@mail.com'),('1712131415','ulises.o@mail.com'),('1723242526','violeta.p@mail.com'),('1734353637','walter.q@mail.com'),('1745464748','ximena.r@mail.com'),('1756575859','yadira.s@mail.com'),('1767686970','zacarias.t@mail.com'),('1778798081','amelia.v@mail.com'),('1789809192','benito.z@mail.com'),('1790910203','catalina.a@mail.com'),('1701021314','dario.b@mail.com');
INSERT INTO inventario_quito (inventario_id, producto_id, ciudad_id, stock) VALUES (1,1,1,50),(2,2,1,120),(3,3,1,80),(4,4,1,200),(5,5,1,40),(6,6,1,40),(7,7,1,30),(8,8,1,15),(9,9,1,60),(10,10,1,70),(11,11,1,25),(12,12,1,25),(13,13,1,60),(14,14,1,10),(15,15,1,18),(16,16,1,22),(17,17,1,35),(18,18,1,8),(19,19,1,7),(20,20,1,9),(21,21,1,30),(22,22,1,28),(23,23,1,150),(24,24,1,45),(25,25,1,12),(26,26,1,16),(27,27,1,11),(28,28,1,55),(29,29,1,24),(30,30,1,14);
INSERT INTO ordenes_trabajo_quito (cliente_cedula, placa, fecha, estado, ciudad_id, empleado_cedula, form_pago_id) VALUES ('1712345678','PBA-0101','2024-05-01','Finalizada',1,'1700000002',1),('1723456789','PBA-0102','2024-05-01','Finalizada',1,'1700000004',2),('1734567890','PBA-0103','2024-05-02','En Proceso',1,'1700000002',3),('1745678901','PBA-0104','2024-05-03','Finalizada',1,'1700000004',1),('1756789012','PBA-0105','2024-05-04','Recibida',1,'1700000016',2),('1767890123','PBA-0106','2024-05-04','Finalizada',1,'1700000002',1),('1778901234','PBA-0107','2024-05-05','Finalizada',1,'1700000004',3),('1789012345','PBA-0108','2024-05-06','En Proceso',1,'1700000016',1),('1790123456','PBA-0109','2024-05-06','Finalizada',1,'1700000002',2),('1701234567','PBA-0110','2024-05-07','Recibida',1,'1700000004',3),('1711223344','PBA-0111','2024-05-08','Finalizada',1,'1700000016',1),('1722334455','PBA-0112','2024-05-08','En Proceso',1,'1700000002',2),('1733445566','PBA-0113','2024-05-09','Finalizada',1,'1700000004',1),('1744556677','PBA-0114','2024-05-10','Finalizada',1,'1700000016',3),('1755667788','PBA-0115','2024-05-11','Recibida',1,'1700000002',2),('1766778899','PBA-0116','2024-05-12','En Proceso',1,'1700000004',1),('1777889900','PBA-0117','2024-05-13','Finalizada',1,'1700000016',1),('1788990011','PBA-0118','2024-05-13','Finalizada',1,'1700000002',2),('1799001122','PBA-0119','2024-05-14','Recibida',1,'1700000004',3),('1700112233','PBA-0120','2024-05-15','En Proceso',1,'1700000016',1),('1712131415','PBA-0121','2024-05-16','Finalizada',1,'1700000002',2),('1723242526','PBA-0122','2024-05-17','Finalizada',1,'1700000004',1),('1734353637','PBA-0123','2024-05-18','Recibida',1,'1700000016',3),('1745464748','PBA-0124','2024-05-19','Finalizada',1,'1700000002',1),('1756575859','PBA-0125','2024-05-20','En Proceso',1,'1700000004',2),('1767686970','PBA-0126','2024-05-21','Finalizada',1,'1700000016',1),('1778798081','PBA-0127','2024-05-22','Recibida',1,'1700000002',3),('1789809192','PBA-0128','2024-05-23','Finalizada',1,'1700000004',2),('1790910203','PBA-0129','2024-05-24','En Proceso',1,'1700000016',1),('1701021314','PBA-0130','2024-05-25','Finalizada',1,'1700000002',1);
INSERT INTO detalles_orden (orden_id, servicio_id, producto_id, cantidad, precio) VALUES ('OQ-1',1,1,1,60.00),('OQ-2',2,13,4,420.00),('OQ-3',14,15,1,305.00),('OQ-4',3,5,2,115.00),('OQ-5',15,NULL,0,30.00),('OQ-6',1,2,1,43.00),('OQ-7',7,NULL,0,70.00),('OQ-8',10,11,2,250.00),('OQ-9',6,4,4,128.00),('OQ-10',24,NULL,0,35.00),('OQ-11',8,8,1,130.00),('OQ-12',1,1,1,60.00),('OQ-13',2,NULL,0,40.00),('OQ-14',11,13,2,220.00),('OQ-15',16,NULL,0,120.00),('OQ-16',1,1,1,60.00),('OQ-17',5,7,2,180.00),('OQ-18',12,10,1,120.00),('OQ-19',9,NULL,0,45.00),('OQ-20',19,NULL,0,65.00),('OQ-21',1,1,1,60.00),('OQ-22',4,6,2,130.00),('OQ-23',15,NULL,0,30.00),('OQ-24',29,NULL,0,30.00),('OQ-25',21,19,1,330.00),('OQ-26',1,1,1,60.00),('OQ-27',17,NULL,0,280.00),('OQ-28',22,20,1,290.00),('OQ-29',1,2,1,43.00),('OQ-30',2,13,2,230.00);

-- 3. Crear vista local para clientes completos
CREATE OR REPLACE VIEW v_clientes_completos_quito AS
SELECT i.cedula, i.nombre, t.telefono, c.correo, i.ciudad_id
FROM info_clientes_quito i
JOIN telefono_clientes_quito t ON i.cedula = t.cedula
JOIN correo_clientes_quito c ON i.cedula = c.cedula;

-- Parte 4: Configuración de Servidores Remotos
CREATE SERVER nodo_guayaquil_fdw FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (host 'nodo-guayaquil', dbname 'taller_db', port '5432');

CREATE SERVER nodo_cuenca_fdw FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (host 'nodo-cuenca', dbname 'taller_db', port '5432');

CREATE USER MAPPING FOR taller_user SERVER nodo_guayaquil_fdw
    OPTIONS (user 'taller_user', password 'taller_password_local');

CREATE USER MAPPING FOR taller_user SERVER nodo_cuenca_fdw
    OPTIONS (user 'taller_user', password 'taller_password_local');


-- Parte 2: Creación de Tablas Foráneas que apuntan a VISTAS Remotas
-- En lugar de importar cada fragmento, importamos la vista pre-unida
-- de cada nodo. Esto es la clave de la optimización.

CREATE FOREIGN TABLE v_clientes_completos_guayaquil (
    cedula VARCHAR,
    nombre VARCHAR,
    telefono VARCHAR,
    correo VARCHAR,
    ciudad_id INT
) SERVER nodo_guayaquil_fdw OPTIONS (table_name 'v_clientes_completos_guayaquil');

CREATE FOREIGN TABLE v_clientes_completos_cuenca (
    cedula VARCHAR,
    nombre VARCHAR,
    telefono VARCHAR,
    correo VARCHAR,
    ciudad_id INT
) SERVER nodo_cuenca_fdw OPTIONS (table_name 'v_clientes_completos_cuenca');

-- También importamos las órdenes de trabajo para la vista global de órdenes.
CREATE FOREIGN TABLE ordenes_trabajo_guayaquil (
    orden_id VARCHAR(20), cliente_cedula VARCHAR, placa VARCHAR, fecha DATE, estado VARCHAR, ciudad_id INT, empleado_cedula VARCHAR, form_pago_id INT
) SERVER nodo_guayaquil_fdw OPTIONS (table_name 'ordenes_trabajo_guayaquil');

CREATE FOREIGN TABLE ordenes_trabajo_cuenca (
    orden_id VARCHAR(20), cliente_cedula VARCHAR, placa VARCHAR, fecha DATE, estado VARCHAR, ciudad_id INT, empleado_cedula VARCHAR, form_pago_id INT
) SERVER nodo_cuenca_fdw OPTIONS (table_name 'ordenes_trabajo_cuenca');


-- Parte 3: Creación de Vistas Globales Optimizadas

-- VISTA GLOBAL DE CLIENTES
-- Esta vista ahora es mucho más simple. Une los datos locales y luego
-- simplemente selecciona los datos de las tablas foráneas que apuntan a las vistas.
CREATE OR REPLACE VIEW clientes_global AS
-- Datos locales de Quito (el JOIN se hace aquí porque es local y rápido)
SELECT
    i.cedula,
    i.nombre,
    t.telefono,
    c.correo,
    i.ciudad_id
FROM
    info_clientes_quito i
JOIN telefono_clientes_quito t ON i.cedula = t.cedula
JOIN correo_clientes_quito c ON i.cedula = c.cedula

UNION ALL

-- Datos remotos de Guayaquil (ya vienen unidos desde la vista remota)
SELECT cedula, nombre, telefono, correo, ciudad_id
FROM v_clientes_completos_guayaquil

UNION ALL

-- Datos remotos de Cuenca (ya vienen unidos desde la vista remota)
SELECT cedula, nombre, telefono, correo, ciudad_id
FROM v_clientes_completos_cuenca;

-- VISTA GLOBAL DE ÓRDENES DE TRABAJO
CREATE OR REPLACE VIEW ordenes_trabajo_global AS
SELECT orden_id, cliente_cedula, placa, fecha, estado, ciudad_id, empleado_cedula, form_pago_id
FROM ordenes_trabajo_quito
UNION ALL
SELECT orden_id, cliente_cedula, placa, fecha, estado, ciudad_id, empleado_cedula, form_pago_id
FROM ordenes_trabajo_guayaquil
UNION ALL
SELECT orden_id, cliente_cedula, placa, fecha, estado, ciudad_id, empleado_cedula, form_pago_id
FROM ordenes_trabajo_cuenca;

-- Trigger para validar FK en 'vehiculos'
CREATE OR REPLACE FUNCTION check_cliente_exists() RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM clientes_global WHERE cedula = NEW.cliente_cedula) THEN
        RAISE EXCEPTION 'La cédula % no existe en la vista global de clientes', NEW.cliente_cedula;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_cliente_before_insert BEFORE INSERT OR UPDATE ON vehiculos
FOR EACH ROW EXECUTE FUNCTION check_cliente_exists();

-- Trigger para validar FK en 'detalles_orden'
CREATE OR REPLACE FUNCTION check_orden_exists() RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ordenes_trabajo_global WHERE orden_id = NEW.orden_id) THEN
        RAISE EXCEPTION 'La orden_id % no existe en la vista global de órdenes de trabajo', NEW.orden_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_orden_before_insert BEFORE INSERT OR UPDATE ON detalles_orden
FOR EACH ROW EXECUTE FUNCTION check_orden_exists();