use ghsicilianotfi;
SET FOREIGN_KEY_CHECKS = 0;

SET SQL_SAFE_UPDATES = 0;
START TRANSACTION;

-- #############################################################################
-- PARTE 1: ROLES Y USUARIOS
-- #############################################################################
INSERT INTO roles (id, nombre) VALUES (1, 'ADMIN'), (2, 'RESPONSABLE'), (3, 'EMPLEADO'), (4, 'CAJERO'), (5, 'COCINERO');
 --- Cambiar el hash de la Clave que te genera DataCarga (Pase la clave por el grupo) 
SET @hash_pw = '$2a$10$hcci4nXZCsU/oO2nIHxgvObLc5ludU8h8IUeD2TbpravRjRmzjg0u';

INSERT INTO usuarios (id, debe_cambiar_password, email, password_hash, rol_id) VALUES
(1, 0, 'rpruiz33@gmail.com',      @hash_pw, 1),
(2, 0, 'empleado@epicentro.com',  @hash_pw, 3),
(3, 0, 'admin@epicentro.com',  @hash_pw, 1),
(4, 0, 'cajero@epicentro.com',  @hash_pw, 4),
(5, 0, 'responsable@epicentro.com',  @hash_pw, 2);
-- #############################################################################
-- PARTE 2: CARGA DE FESTIVAL, CATEGORÍAS, STAFF, UNIDADES Y RELACIONES
-- #############################################################################

-- 2.1 FESTIVALES DINÁMICOS SEGÚN LA FECHA DE EJECUCIÓN
INSERT INTO festivales (
    nombre,
    fecha_inicio,
    fecha_fin,
    activo
) VALUES

-- Festival actual
(
    'Epicentro Gourmet UNLa (Edicion Actual)',
    DATE_SUB(CURRENT_DATE(), INTERVAL 10 DAY),
    DATE_ADD(CURRENT_DATE(), INTERVAL 15 DAY),
    TRUE
),

-- Festival anterior
(
    'Epicentro Gourmet UNLa (Edicion Anterior)',
    DATE_SUB(CURRENT_DATE(), INTERVAL 60 DAY),
    DATE_SUB(CURRENT_DATE(), INTERVAL 40 DAY),
    TRUE
),

-- Festival anual
(
    'Epicentro Gourmet UNLa (Edicion Anual)',
    DATE_SUB(CURRENT_DATE(), INTERVAL 365 DAY),
    DATE_SUB(CURRENT_DATE(), INTERVAL 345 DAY),
    TRUE
);
-- Asignamos los IDs de los festivales
SET @festival_id = (SELECT id FROM festivales WHERE nombre = 'Epicentro Gourmet UNLa (Edicion Actual)' LIMIT 1);
SET @festival_anterior_id = (SELECT id FROM festivales WHERE nombre = 'Epicentro Gourmet UNLa (Edicion Anterior)' LIMIT 1);
SET @festival_anual_id = (SELECT id FROM festivales WHERE nombre = 'Epicentro Gourmet UNLa (Edicion Anual)' LIMIT 1);
SET @user_empleado_id = (SELECT id FROM usuarios WHERE email = 'empleado@epicentro.com' LIMIT 1);

-- 2.1.1 COSTOS DEL FESTIVAL ANUAL (HACE 1 AÑO - INICIALES: TOTAL $550.000)
INSERT INTO costos (tipo_costo, monto, tipo_unidad_de_medida, festival_id) VALUES
('Alquiler de Espacio Ferial Básico',           200000.00, 'Global',   @festival_anual_id),
('Vigilancia y Seguridad Inicial',              120000.00, 'Servicio', @festival_anual_id),
('Sonido y Luces Edición Inicial',             100000.00, 'Global',   @festival_anual_id),
('Servicio Básico de Limpieza',                  70000.00, 'Jornada',  @festival_anual_id),
('Generador Eléctrico Portátil',                 60000.00, 'Litros',   @festival_anual_id);

-- 2.1.2 COSTOS DEL FESTIVAL ANTERIOR (HACE 2 MESES - INTERMEDIOS: TOTAL $980.000)
INSERT INTO costos (tipo_costo, monto, tipo_unidad_de_medida, festival_id) VALUES
('Alquiler de Espacio Ferial Intermedio',       320000.00, 'Global',   @festival_anterior_id),
('Vigilancia y Seguridad Nocturna',             210000.00, 'Servicio', @festival_anterior_id),
('Equipamiento de Audio y Escenario',           170000.00, 'Global',   @festival_anterior_id),
('Mantenimiento y Sanitización',                110000.00, 'Jornada',  @festival_anterior_id),
('Alquiler de Baños Químicos',                   90000.00, 'Unidad',   @festival_anterior_id),
('Grupo Electrógeno de Respaldo',                80000.00, 'Servicio', @festival_anterior_id);

-- 2.1.3 COSTOS DEL FESTIVAL ACTUAL (EN CURSO - MÁS ALTOS: TOTAL $1.650.000)
INSERT INTO costos (tipo_costo, monto, tipo_unidad_de_medida, festival_id) VALUES
('Alquiler de Predio Mayor y Espacios',         550000.00, 'Global',   @festival_id),
('Seguridad Privada y Control de Accesos',      380000.00, 'Servicio', @festival_id),
('Escenario Principal, Sonido Pro e Iluminación', 350000.00, 'Global', @festival_id),
('Gestión Integral de Residuos y Limpieza',     200000.00, 'Jornada',  @festival_id),
('Red de Generadores Eléctricos y Combustible', 170000.00, 'Litros',   @festival_id);

-- 2.2 TABLAS TEMPORALES DE CARGA
DROP TEMPORARY TABLE IF EXISTS tmp_categorias;
CREATE TEMPORARY TABLE tmp_categorias (
    nombre VARCHAR(100),
    plus_fijo DECIMAL(10,2)
);

INSERT INTO tmp_categorias (nombre, plus_fijo) VALUES
('Parrillero Especialista U1', 15000.00), ('Minutero U1', 10000.00), ('Pizzero U1', 12000.00),
('Parrillero Especialista U2', 14000.00), ('Minutero U2', 11000.00), ('Pizzero U2', 13000.00),
('Parrillero Especialista U3', 16000.00), ('Minutero U3', 10500.00), ('Pizzero U3', 12500.00),
('Parrillero Especialista U4', 15500.00), ('Minutero U4', 9500.00),  ('Pizzero U4', 14000.00),
('Parrillero Especialista U5', 17000.00), ('Minutero U5', 12000.00), ('Pizzero U5', 13500.00),
('Parrillero Especialista U6', 16500.00), ('Minutero U6', 11500.00), ('Pizzero U6', 11000.00),
('Parrillero Especialista U7', 14500.00), ('Minutero U7', 10000.00), ('Pizzero U7', 15000.00),
('Parrillero Especialista U8', 18000.00), ('Minutero U8', 13000.00), ('Pizzero U8', 14500.00),
('Parrillero Especialista U9', 17500.00), ('Minutero U9', 12500.00), ('Pizzero U9', 13000.00),
('Parrillero Especialista U10', 19000.00), ('Minutero U10', 14000.00), ('Pizzero U10', 16000.00);

DROP TEMPORARY TABLE IF EXISTS tmp_unidades;
CREATE TEMPORARY TABLE tmp_unidades (
    codigo VARCHAR(10),
    nombre_comercial VARCHAR(100),
    superficie DECIMAL(6,2),
    tipo VARCHAR(2),
    patente VARCHAR(20),
    electrico BIT,
    carpas INT,
    montaje INT,
    dni_responsable VARCHAR(8)
);

INSERT INTO tmp_unidades VALUES
('FT-0000101', 'La Trinchera Gourmet',    22.50, 'FT', 'AF101CD', 1, NULL, NULL, '34125890'),
('PD-0000101', 'Sabores Criollos',        30.00, 'PD', NULL,     NULL, 3,    45,   '33412876'),
('FT-0000102', 'El Buen Sabor Rodante',   20.00, 'FT', 'AF102CD', 1, NULL, NULL, '32145698'),
('PD-0000102', 'La Esquina Dulce',        25.00, 'PD', NULL,     NULL, 2,    30,   '35478912'),
('FT-0000103', 'Burger Master Truck',     24.00, 'FT', 'AF103CD', 1, NULL, NULL, '31258741'),
('PD-0000103', 'Brasas Serranas',         35.00, 'PD', NULL,     NULL, 4,    60,   '36985214'),
('FT-0000104', 'Tacos El Guero',          18.00, 'FT', 'AF104CD', 0, NULL, NULL, '29874512'),
('PD-0000104', 'Pastas de la Nonna',      28.00, 'PD', NULL,     NULL, 3,    50,   '37894561'),
('FT-0000105', 'Ahumados Patagonicos',    22.00, 'FT', 'AF105CD', 1, NULL, NULL, '30147852'),
('PD-0000105', 'Empanadas Saltenas',      32.00, 'PD', NULL,     NULL, 3,    40,   '38521479');

DROP TEMPORARY TABLE IF EXISTS tmp_empleados;
CREATE TEMPORARY TABLE tmp_empleados (
    tipo CHAR(1),
    nombre VARCHAR(50),
    apellido VARCHAR(50),
    dni VARCHAR(8),
    fecha_nac DATE,
    fecha_ingreso DATE,
    sueldo DECIMAL(10,2),
    turno VARCHAR(20),
    num_caja INT,
    plus_adicional DECIMAL(10,2),
    especialidad VARCHAR(50),
    libreta BIT,
    bono_pelig DECIMAL(10,2),
    cat_nombre VARCHAR(100),
    cod_unidad VARCHAR(10)
);

INSERT INTO tmp_empleados VALUES
-- U1: FT-0000101 (responsable empleado@epicentro.com)
('C', 'Carlos',    'Benitez',   '34125890', '1988-04-12', '2024-05-01', 180000.00, 'Manana', 1,  12000.00, NULL, NULL, NULL, NULL, 'FT-0000101'),
('C', 'Mariana',   'Gomez',     '36781245', '1992-08-19', '2022-03-15', 145000.00, 'Tarde',  2,   8500.00, NULL, NULL, NULL, NULL, 'FT-0000101'),
('C', 'Lucas',     'Fernandez', '30456123', '1984-11-05', '2017-06-10', 165000.00, 'Noche',  3,  15000.00, NULL, NULL, NULL, NULL, 'FT-0000101'),
('K', 'Gonzalo',   'Perez',     '32987410', '1987-01-22', '2023-09-01', 175000.00, NULL, NULL, NULL, 'Carnes Rojas', 1, 16000.00, 'Parrillero Especialista U1', 'FT-0000101'),
('K', 'Sofia',     'Navarro',   '38123654', '1994-06-30', '2020-02-15', 130000.00, NULL, NULL, NULL, 'Minutas',      1,  9500.00, 'Minutero U1',               'FT-0000101'),
('K', 'Esteban',   'Morales',   '28541236', '1981-03-14', '2016-01-10', 195000.00, NULL, NULL, NULL, 'Pizzas',       1, 22000.00, 'Pizzero U1',                'FT-0000101'),

-- U2: PD-0000101
('C', 'Javier',    'Martinez',  '33412876', '1988-10-09', '2024-08-01', 150000.00, 'Manana', 4,   9000.00, NULL, NULL, NULL, NULL, 'PD-0000101'),
('C', 'Camila',    'Alvarez',   '39542109', '1996-05-18', '2023-02-01', 120000.00, 'Tarde',  5,   6500.00, NULL, NULL, NULL, NULL, 'PD-0000101'),
('C', 'Martin',    'Roldan',    '31876543', '1985-12-03', '2019-04-12', 160000.00, 'Noche',  6,  13500.00, NULL, NULL, NULL, NULL, 'PD-0000101'),
('K', 'Valentina', 'Romero',    '37456981', '1993-07-25', '2021-11-20', 140000.00, NULL, NULL, NULL, 'Parrilla',     1, 11000.00, 'Parrillero Especialista U2', 'PD-0000101'),
('K', 'Nicolas',   'Gutierrez', '35678432', '1990-09-14', '2017-08-05', 170000.00, NULL, NULL, NULL, 'Gourmet',      1, 18000.00, 'Minutero U2',               'PD-0000101'),
('K', 'Andrea',    'Dominguez', '27981452', '1980-02-28', '2016-03-01', 190000.00, NULL, NULL, NULL, 'Pasteleria',   1, 24000.00, 'Pizzero U2',                'PD-0000101'),

-- U3: FT-0000102
('C', 'Marcos',    'Diaz',      '32145698', '1986-07-15', '2018-05-10', 165000.00, 'Manana', 7,  14000.00, NULL, NULL, NULL, NULL, 'FT-0000102'),
('C', 'Florencia', 'Acosta',    '40125896', '1997-03-22', '2025-01-15', 110000.00, 'Tarde',  8,   5000.00, NULL, NULL, NULL, NULL, 'FT-0000102'),
('C', 'Ignacio',   'Paz',       '36258974', '1991-11-04', '2022-07-01', 135000.00, 'Noche',  9,   8000.00, NULL, NULL, NULL, NULL, 'FT-0000102'),
('K', 'Ramiro',    'Silva',     '29365412', '1982-09-18', '2017-04-12', 185000.00, NULL, NULL, NULL, 'Brasas',       1, 20000.00, 'Parrillero Especialista U3', 'FT-0000102'),
('K', 'Carla',     'Mendoza',   '38452147', '1994-12-08', '2020-09-10', 145000.00, NULL, NULL, NULL, 'Minutas',      1, 12000.00, 'Minutero U3',               'FT-0000102'),
('K', 'Gaston',    'Herrera',   '34789512', '1989-06-30', '2019-02-20', 160000.00, NULL, NULL, NULL, 'Pizzas',       1, 15000.00, 'Pizzero U3',                'FT-0000102'),

-- U4: PD-0000102
('C', 'Laura',     'Blanco',    '35478912', '1990-05-14', '2019-10-01', 155000.00, 'Manana', 10, 11000.00, NULL, NULL, NULL, NULL, 'PD-0000102'),
('C', 'Tomas',     'Ibarra',    '41258963', '1998-08-25', '2024-09-01', 105000.00, 'Tarde',  11,  4500.00, NULL, NULL, NULL, NULL, 'PD-0000102'),
('C', 'Belen',     'Suarez',    '33698521', '1988-02-17', '2018-03-15', 160000.00, 'Noche',  12, 13000.00, NULL, NULL, NULL, NULL, 'PD-0000102'),
('K', 'Federico',  'Quiroga',   '30874521', '1984-10-11', '2016-08-20', 190000.00, NULL, NULL, NULL, 'Pastas',       1, 21000.00, 'Parrillero Especialista U4', 'PD-0000102'),
('K', 'Agustina',  'Vidal',     '37145289', '1993-01-09', '2021-04-05', 135000.00, NULL, NULL, NULL, 'Dulces',       1,  9000.00, 'Minutero U4',               'PD-0000102'),
('K', 'Emiliano',  'Sosa',      '27541289', '1979-11-29', '2016-02-10', 200000.00, NULL, NULL, NULL, 'Panaderia',    1, 23000.00, 'Pizzero U4',                'PD-0000102'),

-- U5: FT-0000103
('C', 'Sergio',    'Ramos',     '31258741', '1985-04-03', '2017-09-12', 170000.00, 'Manana', 13, 16000.00, NULL, NULL, NULL, NULL, 'FT-0000103'),
('C', 'Paula',     'Cardozo',   '39874563', '1996-12-14', '2023-05-18', 125000.00, 'Tarde',  14,  7000.00, NULL, NULL, NULL, NULL, 'FT-0000103'),
('C', 'Joaquin',   'Medina',    '34896523', '1989-10-21', '2020-01-10', 140000.00, 'Noche',  15,  9500.00, NULL, NULL, NULL, NULL, 'FT-0000103'),
('K', 'Claudio',   'Cabrera',   '28745698', '1981-08-16', '2016-05-01', 195000.00, NULL, NULL, NULL, 'Hamburguesas', 1, 22000.00, 'Parrillero Especialista U5', 'FT-0000103'),
('K', 'Romina',    'Flores',    '36541278', '1992-03-05', '2022-08-14', 130000.00, NULL, NULL, NULL, 'Frituras',     1, 10000.00, 'Minutero U5',               'FT-0000103'),
('K', 'Hernan',    'Castillo',  '33125874', '1987-07-28', '2019-11-03', 165000.00, NULL, NULL, NULL, 'Salsas',       1, 14000.00, 'Pizzero U5',                'FT-0000103'),

-- U6: PD-0000103
('C', 'Pablo',     'Castro',    '36985214', '1992-06-18', '2021-03-10', 145000.00, 'Manana', 16,  8500.00, NULL, NULL, NULL, NULL, 'PD-0000103'),
('C', 'Valeria',   'Gimenez',   '42145896', '2000-01-20', '2025-06-01',  95000.00, 'Tarde',  17,  3500.00, NULL, NULL, NULL, NULL, 'PD-0000103'),
('C', 'Esteban',   'Molina',    '30985214', '1984-05-12', '2017-02-14', 170000.00, 'Noche',  18, 15000.00, NULL, NULL, NULL, NULL, 'PD-0000103'),
('K', 'Mauricio',  'Duarte',    '32456987', '1986-11-23', '2018-09-01', 180000.00, NULL, NULL, NULL, 'Cordero',      1, 19000.00, 'Parrillero Especialista U6', 'PD-0000103'),
('K', 'Luciana',   'Godoy',     '38965412', '1995-04-17', '2023-10-15', 120000.00, NULL, NULL, NULL, 'Guarniciones', 1,  8000.00, 'Minutero U6',               'PD-0000103'),
('K', 'Julian',    'Ferreyra',  '26985412', '1978-08-30', '2016-04-01', 200000.00, NULL, NULL, NULL, 'Ahumados',     1, 25000.00, 'Pizzero U6',                'PD-0000103'),

-- U7: FT-0000104
('C', 'Daniel',    'Rios',      '29874512', '1983-02-27', '2016-07-10', 175000.00, 'Manana', 19, 17000.00, NULL, NULL, NULL, NULL, 'FT-0000104'),
('C', 'Micaela',   'Ponce',     '41789654', '1999-09-08', '2024-11-01', 100000.00, 'Tarde',  20,  4000.00, NULL, NULL, NULL, NULL, 'FT-0000104'),
('C', 'Adrian',    'Vargas',    '35124578', '1990-03-19', '2020-06-12', 150000.00, 'Noche',  21, 10500.00, NULL, NULL, NULL, NULL, 'FT-0000104'),
('K', 'Jorge',     'Ortega',    '31896547', '1985-12-11', '2017-08-15', 185000.00, NULL, NULL, NULL, 'Tacos',        1, 17500.00, 'Parrillero Especialista U7', 'FT-0000104'),
('K', 'Gisela',    'Aguirre',   '37854123', '1993-10-02', '2022-01-20', 135000.00, NULL, NULL, NULL, 'Salsas Mex',   1, 11000.00, 'Minutero U7',               'FT-0000104'),
('K', 'Rodrigo',   'Benitez',   '34258963', '1989-01-14', '2019-05-18', 160000.00, NULL, NULL, NULL, 'Tortillas',    1, 13000.00, 'Pizzero U7',                'FT-0000104'),

-- U8: PD-0000104
('C', 'Lucas',     'Ortiz',     '37894561', '1993-08-04', '2021-09-01', 140000.00, 'Manana', 22,  8000.00, NULL, NULL, NULL, NULL, 'PD-0000104'),
('C', 'Brenda',    'Peralta',   '40896523', '1998-04-19', '2023-04-15', 115000.00, 'Tarde',  23,  6000.00, NULL, NULL, NULL, NULL, 'PD-0000104'),
('C', 'Gustavo',   'Franco',    '28965412', '1982-01-07', '2016-09-20', 180000.00, 'Noche',  24, 18000.00, NULL, NULL, NULL, NULL, 'PD-0000104'),
('K', 'Mariano',   'Nieto',     '33852147', '1988-06-25', '2018-07-12', 170000.00, NULL, NULL, NULL, 'Pastas Caseras',1, 16000.00, 'Parrillero Especialista U8', 'PD-0000104'),
('K', 'Natalia',   'Campos',    '36412589', '1991-12-18', '2020-10-05', 145000.00, NULL, NULL, NULL, 'Rellenos',     1, 12500.00, 'Minutero U8',               'PD-0000104'),
('K', 'Cristian',  'Barrios',   '30258963', '1983-10-15', '2017-01-10', 190000.00, NULL, NULL, NULL, 'Salsas Ita',   1, 20500.00, 'Pizzero U8',                'PD-0000104'),

-- U9: FT-0000105
('C', 'Matias',    'Luna',      '30147852', '1983-07-22', '2017-03-01', 175000.00, 'Manana', 25, 16500.00, NULL, NULL, NULL, NULL, 'FT-0000105'),
('C', 'Daniela',   'Rojas',     '42896541', '2001-02-14', '2025-08-01',  92000.00, 'Tarde',  26,  3000.00, NULL, NULL, NULL, NULL, 'FT-0000105'),
('C', 'Federico',  'Maldonado', '35896541', '1991-05-09', '2020-04-18', 150000.00, 'Noche',  27, 10000.00, NULL, NULL, NULL, NULL, 'FT-0000105'),
('K', 'Leandro',   'Santillan', '32896541', '1987-09-30', '2018-06-25', 180000.00, NULL, NULL, NULL, 'Trucha Ahumada',1, 18500.00, 'Parrillero Especialista U9', 'FT-0000105'),
('K', 'Sabrina',   'Ibanez',    '37965412', '1994-01-26', '2022-09-10', 135000.00, NULL, NULL, NULL, 'Ciervo',       1, 11500.00, 'Minutero U9',               'FT-0000105'),
('K', 'Guillermo', 'Paz',       '27896541', '1980-04-12', '2016-01-15', 200000.00, NULL, NULL, NULL, 'Quesos',       1, 24500.00, 'Pizzero U9',                'FT-0000105'),

-- U10: PD-0000105
('C', 'Damian',    'Vega',      '38521479', '1994-11-16', '2021-02-01', 145000.00, 'Manana', 28,  9000.00, NULL, NULL, NULL, NULL, 'PD-0000105'),
('C', 'Florencia', 'Lucero',    '43125896', '2002-06-03', '2025-05-10',  90000.00, 'Tarde',  29,  3000.00, NULL, NULL, NULL, NULL, 'PD-0000105'),
('C', 'Manuel',    'Bustos',    '31524789', '1985-08-28', '2018-11-20', 165000.00, 'Noche',  30, 14000.00, NULL, NULL, NULL, NULL, 'PD-0000105'),
('K', 'Oscar',     'Gallardo',  '29521478', '1982-10-05', '2016-10-01', 195000.00, NULL, NULL, NULL, 'Empanada Salteña',1, 22500.00, 'Parrillero Especialista U10','PD-0000105'),
('K', 'Melisa',    'Carrizo',   '36987451', '1992-04-11', '2020-07-15', 140000.00, NULL, NULL, NULL, 'Locro',          1, 13500.00, 'Minutero U10',             'PD-0000105'),
('K', 'Walter',    'Farías',    '33521478', '1988-03-02', '2019-01-22', 170000.00, NULL, NULL, NULL, 'Tamales',        1, 16500.00, 'Pizzero U10',              'PD-0000105');

-- 2.3 CATEGORÍAS DE COCINA
INSERT INTO categorias_cocina (nombre, plus_fijo)
SELECT t.nombre, t.plus_fijo
FROM tmp_categorias t
WHERE NOT EXISTS (SELECT 1 FROM categorias_cocina c WHERE c.nombre = t.nombre);

-- 2.3.1 CREACIÓN DE USUARIOS PARA CADA EMPLEADO DEL STAFF
INSERT INTO usuarios (email, password_hash, rol_id, debe_cambiar_password, activo)
SELECT 
    LOWER(CONCAT(t.nombre, '.', t.apellido, '@epicentro.com')),
    @hash_pw,
    CASE 
        WHEN t.dni = '33412876' THEN 2 -- Responsable
        WHEN t.tipo = 'C' THEN 4       -- Cajero
        WHEN t.tipo = 'K' THEN 5       -- Cocinero
        ELSE 3                         -- Empleado
    END,
    0,
    1
FROM tmp_empleados t
WHERE t.dni NOT IN ('34125890', '33412876')
  AND NOT EXISTS (
      SELECT 1 FROM usuarios u 
      WHERE u.email = LOWER(CONCAT(t.nombre, '.', t.apellido, '@epicentro.com'))
  );

-- 2.4 STAFF (vinculado a sus respectivos usuarios)
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo, usuario_id)
SELECT t.nombre, t.apellido, t.dni, t.fecha_nac, t.fecha_ingreso, t.sueldo, u.id
FROM tmp_empleados t
LEFT JOIN usuarios u ON u.email = CASE 
    WHEN t.dni = '34125890' THEN 'empleado@epicentro.com'
    WHEN t.dni = '33412876' THEN 'responsable@epicentro.com'
    ELSE LOWER(CONCAT(t.nombre, '.', t.apellido, '@epicentro.com'))
END
WHERE NOT EXISTS (SELECT 1 FROM staff s WHERE s.dni = t.dni);

-- Asegurar vinculación de usuario_id para todos los empleados de staff
UPDATE staff s
JOIN tmp_empleados t ON s.dni = t.dni
JOIN usuarios u ON u.email = CASE 
    WHEN t.dni = '34125890' THEN 'empleado@epicentro.com'
    WHEN t.dni = '33412876' THEN 'responsable@epicentro.com'
    ELSE LOWER(CONCAT(t.nombre, '.', t.apellido, '@epicentro.com'))
END
SET s.usuario_id = u.id
WHERE s.usuario_id IS NULL;

-- Asociar cajero@epicentro.com a un empleado
SET @user_cajero_id = (
    SELECT id
    FROM usuarios
    WHERE email = 'cajero@epicentro.com'
    LIMIT 1
);
-- Asociar el usuario Responsable con Javier Martinez
UPDATE staff s
JOIN usuarios u
    ON u.email = 'responsable@epicentro.com'
SET s.usuario_id = u.id
WHERE s.dni = '33412876'
  AND (s.usuario_id IS NULL OR s.usuario_id = u.id);
INSERT INTO staff (
    nombre,
    apellido,
    dni,
    fecha_nacimiento,
    fecha_ingreso,
    sueldo,
    usuario_id
)
SELECT
    'Alejandro',
    'Ramirez',
    '43987651',
    '1998-05-12',
    '2026-01-15',
    150000.00,
    @user_cajero_id
WHERE NOT EXISTS (
    SELECT 1
    FROM staff
    WHERE dni = '43987651'
);

-- 2.5 CAJEROS Y COCINEROS
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional)
SELECT s.id, t.turno, t.num_caja, t.plus_adicional
FROM tmp_empleados t
JOIN staff s ON s.dni = t.dni
WHERE t.tipo = 'C'
  AND NOT EXISTS (SELECT 1 FROM cajero c WHERE c.id = s.id);

-- Registrar a Alejandro como cajero, vinculado a cajero@epicentro.com
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional)
SELECT s.id, 'Tarde', 31, 0.00
FROM staff s
JOIN usuarios u ON u.id = s.usuario_id
WHERE s.dni = '43987651'
  AND u.email = 'cajero@epicentro.com'
  AND NOT EXISTS (
      SELECT 1
      FROM cajero c
      WHERE c.id = s.id
  );

INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id)
SELECT s.id, t.especialidad, t.libreta, t.bono_pelig, c.id
FROM tmp_empleados t
JOIN staff s ON s.dni = t.dni
JOIN categorias_cocina c ON c.nombre = t.cat_nombre
WHERE t.tipo = 'K'
  AND NOT EXISTS (SELECT 1 FROM cocineros k WHERE k.id = s.id);

-- 2.6 UNIDADES DE VENTA
INSERT INTO unidades_venta (codigo, nombre_comercial, superficie_m2, responsable_id)
SELECT u.codigo, u.nombre_comercial, u.superficie, s.id
FROM tmp_unidades u
JOIN staff s ON s.dni = u.dni_responsable
WHERE NOT EXISTS (SELECT 1 FROM unidades_venta uv WHERE uv.codigo = u.codigo);

-- 2.7 FOOD TRUCKS Y PUESTOS DESARMABLES
INSERT INTO food_trucks (id, patente, requiere_electricidad)
SELECT uv.id, u.patente, u.electrico
FROM tmp_unidades u
JOIN unidades_venta uv ON uv.codigo = u.codigo
WHERE u.tipo = 'FT'
  AND NOT EXISTS (SELECT 1 FROM food_trucks ft WHERE ft.id = uv.id);

INSERT INTO puestos_desarmables (id, cantidad_carpas, tiempo_montaje_min)
SELECT uv.id, u.carpas, u.montaje
FROM tmp_unidades u
JOIN unidades_venta uv ON uv.codigo = u.codigo
WHERE u.tipo = 'PD'
  AND NOT EXISTS (SELECT 1 FROM puestos_desarmables pd WHERE pd.id = uv.id);

-- 2.8 RELACIONES
INSERT INTO unidad_venta_staff (unidad_venta_id, staff_id)
SELECT uv.id, s.id
FROM tmp_empleados t
JOIN staff s ON s.dni = t.dni
JOIN unidades_venta uv ON uv.codigo = t.cod_unidad
WHERE NOT EXISTS (
    SELECT 1 FROM unidad_venta_staff uvs
    WHERE uvs.unidad_venta_id = uv.id AND uvs.staff_id = s.id
);

-- Asignar a Alejandro Ramirez a la unidad La Trinchera Gourmet
INSERT INTO unidad_venta_staff (unidad_venta_id, staff_id)
SELECT uv.id, s.id
FROM unidades_venta uv
JOIN staff s ON s.dni = '43987651'
JOIN usuarios u ON u.id = s.usuario_id
WHERE uv.codigo = 'FT-0000101'
  AND u.email = 'cajero@epicentro.com'
  AND NOT EXISTS (
      SELECT 1
      FROM unidad_venta_staff uvs
      WHERE uvs.unidad_venta_id = uv.id
        AND uvs.staff_id = s.id
  );

INSERT INTO festival_unidad_venta (festival_id, unidad_venta_id)
SELECT @festival_id, uv.id
FROM unidades_venta uv
JOIN tmp_unidades tu ON tu.codigo = uv.codigo
WHERE @festival_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM festival_unidad_venta fuv
    WHERE fuv.festival_id = @festival_id AND fuv.unidad_venta_id = uv.id
);

DROP TEMPORARY TABLE IF EXISTS tmp_empleados;
DROP TEMPORARY TABLE IF EXISTS tmp_unidades;
DROP TEMPORARY TABLE IF EXISTS tmp_categorias;

-- #############################################################################
-- PARTE 3: PLATOS + MENÚS + PEDIDOS (REPORTE DE RECAUDACIÓN)
-- #############################################################################

-- 3.1 Catálogo base de platos (temporal).
-- Cada unidad recibirá su propio registro de cada plato de su menú.
DROP TEMPORARY TABLE IF EXISTS tmp_catalogo_platos;
CREATE TEMPORARY TABLE tmp_catalogo_platos (
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    costo_produccion DECIMAL(10,2) NOT NULL
);

INSERT INTO tmp_catalogo_platos (nombre, precio, costo_produccion) VALUES
('Vacío al Libro con Papas Rústicas', 8500.00, 4500.00),
('Choripán Clásico con Chimichurri', 4500.00, 2200.00),
('Sándwich de Bondiola a la Mostaza', 7200.00, 3800.00),
('Matambre a la Pizza al Plato', 9000.00, 5000.00),
('Empanada de Carne Cortada a Cuchillo', 2200.00, 1100.00),
('Empanada de Jamón y Queso', 1800.00, 900.00),
('Empanada Salteña Picante', 2200.00, 1100.00),
('Docena de Empanadas Variadas', 24000.00, 12000.00),
('Tamal Criollo Tradicional', 3500.00, 1800.00),
('Tacos al Pastor (3 unidades)', 6500.00, 3200.00),
('Tacos de Ternera Mechada (3 unidades)', 7000.00, 3500.00),
('Quesadillas con Guacamole', 5800.00, 2800.00),
('Nachos con Cheddar y Jalapeños', 4800.00, 2300.00),
('Gaseosa Línea Cola 500ml', 2000.00, 1000.00),
('Cerveza Artesanal Tirada 500ml', 4000.00, 2000.00),
('Agua Mineral 500ml', 1500.00, 700.00);

-- 3.2 Menús: A = asador, E = empanadas/criollo, T = tacos/mexicano
DROP TEMPORARY TABLE IF EXISTS tmp_menu_unidad;
CREATE TEMPORARY TABLE tmp_menu_unidad (codigo VARCHAR(10), menu CHAR(1));
INSERT INTO tmp_menu_unidad VALUES
('FT-0000101','A'), ('PD-0000101','E'),
('FT-0000102','T'), ('PD-0000102','E'),
('FT-0000103','A'), ('PD-0000103','A'),
('FT-0000104','T'), ('PD-0000104','E'),
('FT-0000105','A'), ('PD-0000105','E');

DROP TEMPORARY TABLE IF EXISTS tmp_menu_plato;
CREATE TEMPORARY TABLE tmp_menu_plato (menu CHAR(1), idx INT, cnt INT, plato VARCHAR(100));
INSERT INTO tmp_menu_plato VALUES
('A',1,7,'Vacío al Libro con Papas Rústicas'),
('A',2,7,'Choripán Clásico con Chimichurri'),
('A',3,7,'Sándwich de Bondiola a la Mostaza'),
('A',4,7,'Matambre a la Pizza al Plato'),
('A',5,7,'Gaseosa Línea Cola 500ml'),
('A',6,7,'Cerveza Artesanal Tirada 500ml'),
('A',7,7,'Agua Mineral 500ml'),
('E',1,8,'Empanada de Carne Cortada a Cuchillo'),
('E',2,8,'Empanada de Jamón y Queso'),
('E',3,8,'Empanada Salteña Picante'),
('E',4,8,'Docena de Empanadas Variadas'),
('E',5,8,'Tamal Criollo Tradicional'),
('E',6,8,'Gaseosa Línea Cola 500ml'),
('E',7,8,'Cerveza Artesanal Tirada 500ml'),
('E',8,8,'Agua Mineral 500ml'),
('T',1,7,'Tacos al Pastor (3 unidades)'),
('T',2,7,'Tacos de Ternera Mechada (3 unidades)'),
('T',3,7,'Quesadillas con Guacamole'),
('T',4,7,'Nachos con Cheddar y Jalapeños'),
('T',5,7,'Gaseosa Línea Cola 500ml'),
('T',6,7,'Cerveza Artesanal Tirada 500ml'),
('T',7,7,'Agua Mineral 500ml');

-- 3.3 Crear platos independientes por unidad de venta.
-- Los nombres pueden repetirse entre unidades, pero cada registro tiene su propio id.
-- Todos los platos se crean activos inicialmente.

INSERT INTO platos (
    nombre,
    precio,
    costo_produccion,
    unidad_venta_id,
    activo
)
SELECT
    cp.nombre,
    cp.precio,
    cp.costo_produccion,
    uv.id,
    TRUE
FROM tmp_menu_unidad mu
JOIN unidades_venta uv
    ON uv.codigo = mu.codigo
JOIN tmp_menu_plato mp
    ON mp.menu = mu.menu
JOIN tmp_catalogo_platos cp
    ON cp.nombre = mp.plato;



-- 3.4 Pedidos: 1 por unidad y por día (desde hace 10 días hasta hoy CURRENT_DATE())
DROP TEMPORARY TABLE IF EXISTS tmp_dias;
CREATE TEMPORARY TABLE tmp_dias (d INT);
INSERT INTO tmp_dias VALUES
(0),(1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

INSERT INTO pedidos (
    fecha_transaccion, festival_id, unidad_venta_id,
    estado, fecha_registro, registrado_por_id
)
SELECT
    DATE_ADD(DATE_SUB(CURRENT_DATE(), INTERVAL 10 DAY), INTERVAL d.d DAY),
    @festival_id,
    uv.id,
    'A',
    TIMESTAMP(
        DATE_ADD(DATE_SUB(CURRENT_DATE(), INTERVAL 10 DAY), INTERVAL d.d DAY),
        '12:00:00'
    ),
    (
        SELECT uvs.staff_id
        FROM unidad_venta_staff uvs
        JOIN staff s ON s.id = uvs.staff_id
        WHERE uvs.unidad_venta_id = uv.id
        ORDER BY s.id
        LIMIT 1
    )
FROM festival_unidad_venta fuv
JOIN unidades_venta uv ON uv.id = fuv.unidad_venta_id
JOIN tmp_menu_unidad mu ON mu.codigo = uv.codigo
CROSS JOIN tmp_dias d
WHERE fuv.festival_id = @festival_id;

-- 3.5 Items: 2 platos distintos por pedido, cantidad 1 a 4
INSERT INTO items_pedido (pedido_id, plato_id, cantidad, precio_unitario)
SELECT p.id,
       pl.id,
       1 + ((DATEDIFF(p.fecha_transaccion, DATE_SUB(CURRENT_DATE(), INTERVAL 10 DAY)) + mp.idx) % 4),
       pl.precio
FROM pedidos p
JOIN unidades_venta uv ON uv.id = p.unidad_venta_id
JOIN tmp_menu_unidad mu ON mu.codigo = uv.codigo
JOIN tmp_menu_plato mp ON mp.menu = mu.menu
JOIN platos pl
    ON pl.nombre = mp.plato
   AND pl.unidad_venta_id = uv.id
WHERE p.festival_id = @festival_id
  AND (   mp.idx = (DATEDIFF(p.fecha_transaccion, DATE_SUB(CURRENT_DATE(), INTERVAL 10 DAY)) % mp.cnt) + 1
       OR mp.idx = ((DATEDIFF(p.fecha_transaccion, DATE_SUB(CURRENT_DATE(), INTERVAL 10 DAY)) + 3) % mp.cnt) + 1);

DROP TEMPORARY TABLE IF EXISTS tmp_dias;
DROP TEMPORARY TABLE IF EXISTS tmp_menu_plato;
DROP TEMPORARY TABLE IF EXISTS tmp_menu_unidad;
-- #############################################################################
-- 3.6 AJUSTE: ASEGURAR QUE EL FESTIVAL ACTUAL NO TENGA PÉRDIDA
-- @incluir_sueldos = 1 -> el punto de equilibrio incluye sueldos + plus del staff
-- @incluir_sueldos = 0 -> solo se consideran los costos de la tabla `costos`
-- #############################################################################
SET @incluir_sueldos = 1;
SET @margen = 1.20;   -- 20% de ganancia sobre los costos

SET @costos_fest = (
    SELECT COALESCE(SUM(monto), 0) FROM costos WHERE festival_id = @festival_id
);

SET @costo_staff = (
    SELECT COALESCE(SUM(
             s.sueldo
           + COALESCE(ca.plus_oadicional, 0)
           + COALESCE(co.bono_peligrosidad, 0)
           + COALESCE(cc.plus_fijo, 0)
           ), 0)
    FROM staff s
    LEFT JOIN cajero ca ON ca.id = s.id
    LEFT JOIN cocineros co ON co.id = s.id
    LEFT JOIN categorias_cocina cc ON cc.id = co.categoria_cocina_id
);

SET @recaudado = (
    SELECT COALESCE(SUM(ip.cantidad * ip.precio_unitario), 0)
    FROM items_pedido ip
    JOIN pedidos p ON p.id = ip.pedido_id
    WHERE p.festival_id = @festival_id
      AND p.estado = 'A'
);

SET @objetivo = (@costos_fest + IF(@incluir_sueldos = 1, @costo_staff, 0)) * @margen;

SET @factor = IF(@recaudado = 0 OR @recaudado >= @objetivo, 1, CEIL(@objetivo / @recaudado));

UPDATE items_pedido ip
JOIN pedidos p ON p.id = ip.pedido_id
SET ip.cantidad = ip.cantidad * @factor
WHERE p.festival_id = @festival_id
  AND @factor > 1;

-- Control rápido (podés borrarlo)
SELECT @costos_fest AS costos_festival,
       @costo_staff AS costo_staff,
       @recaudado   AS recaudado_antes,
       @factor      AS factor_aplicado,
       (SELECT SUM(ip.cantidad * ip.precio_unitario)
          FROM items_pedido ip JOIN pedidos p ON p.id = ip.pedido_id
         WHERE p.festival_id = @festival_id
            AND p.estado = 'A') AS recaudado_despues;
-- #############################################################################
-- PARTE 4: PEDIDOS E ITEMS PARA LOS FESTIVALES PASADOS
-- Anual (hace ~1 año): recaudacion media-baja (15 dias, cantidad 1-2)
-- Anterior (hace ~2 meses): recaudacion media (20 dias, cantidad 1-3)
-- Ambos usan las mismas 10 unidades de venta del festival actual
-- #############################################################################

-- 4.1 Vincular unidades de venta a los festivales pasados
INSERT INTO festival_unidad_venta (festival_id, unidad_venta_id)
SELECT @festival_anterior_id, uv.id
FROM festival_unidad_venta fuv
JOIN unidades_venta uv ON uv.id = fuv.unidad_venta_id
WHERE fuv.festival_id = @festival_id
  AND @festival_anterior_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM festival_unidad_venta fuv2
      WHERE fuv2.festival_id = @festival_anterior_id AND fuv2.unidad_venta_id = uv.id
  );

INSERT INTO festival_unidad_venta (festival_id, unidad_venta_id)
SELECT @festival_anual_id, uv.id
FROM festival_unidad_venta fuv
JOIN unidades_venta uv ON uv.id = fuv.unidad_venta_id
WHERE fuv.festival_id = @festival_id
  AND @festival_anual_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM festival_unidad_venta fuv2
      WHERE fuv2.festival_id = @festival_anual_id AND fuv2.unidad_venta_id = uv.id
  );

-- 4.2 Pedidos festival anterior (20 dias desde hace 60 dias)
DROP TEMPORARY TABLE IF EXISTS tmp_dias_ant;
CREATE TEMPORARY TABLE tmp_dias_ant (d INT);
INSERT INTO tmp_dias_ant VALUES
(0),(1),(2),(3),(4),(5),(6),(7),(8),(9),
(10),(11),(12),(13),(14),(15),(16),(17),(18),(19);

DROP TEMPORARY TABLE IF EXISTS tmp_mu_ant;
CREATE TEMPORARY TABLE tmp_mu_ant (codigo VARCHAR(10), menu CHAR(1));
INSERT INTO tmp_mu_ant VALUES
('FT-0000101','A'), ('PD-0000101','E'),
('FT-0000102','T'), ('PD-0000102','E'),
('FT-0000103','A'), ('PD-0000103','A'),
('FT-0000104','T'), ('PD-0000104','E'),
('FT-0000105','A'), ('PD-0000105','E');

DROP TEMPORARY TABLE IF EXISTS tmp_mp_ant;
CREATE TEMPORARY TABLE tmp_mp_ant (menu CHAR(1), idx INT, cnt INT, plato VARCHAR(100));
INSERT INTO tmp_mp_ant VALUES
('A',1,7,'Vacío al Libro con Papas Rústicas'),
('A',2,7,'Choripán Clásico con Chimichurri'),
('A',3,7,'Sándwich de Bondiola a la Mostaza'),
('A',4,7,'Matambre a la Pizza al Plato'),
('A',5,7,'Gaseosa Línea Cola 500ml'),
('A',6,7,'Cerveza Artesanal Tirada 500ml'),
('A',7,7,'Agua Mineral 500ml'),
('E',1,8,'Empanada de Carne Cortada a Cuchillo'),
('E',2,8,'Empanada de Jamón y Queso'),
('E',3,8,'Empanada Salteña Picante'),
('E',4,8,'Docena de Empanadas Variadas'),
('E',5,8,'Tamal Criollo Tradicional'),
('E',6,8,'Gaseosa Línea Cola 500ml'),
('E',7,8,'Cerveza Artesanal Tirada 500ml'),
('E',8,8,'Agua Mineral 500ml'),
('T',1,7,'Tacos al Pastor (3 unidades)'),
('T',2,7,'Tacos de Ternera Mechada (3 unidades)'),
('T',3,7,'Quesadillas con Guacamole'),
('T',4,7,'Nachos con Cheddar y Jalapeños'),
('T',5,7,'Gaseosa Línea Cola 500ml'),
('T',6,7,'Cerveza Artesanal Tirada 500ml'),
('T',7,7,'Agua Mineral 500ml');

INSERT INTO pedidos (
    fecha_transaccion, festival_id, unidad_venta_id,
    estado, fecha_registro, registrado_por_id
)
SELECT
    DATE_SUB(CURRENT_DATE(), INTERVAL (60 - d.d) DAY),
    @festival_anterior_id,
    uv.id,
    'A',
    TIMESTAMP(
        DATE_SUB(CURRENT_DATE(), INTERVAL (60 - d.d) DAY),
        '12:00:00'
    ),
    (
        SELECT uvs.staff_id
        FROM unidad_venta_staff uvs
        JOIN staff s ON s.id = uvs.staff_id
        WHERE uvs.unidad_venta_id = uv.id
        ORDER BY s.id
        LIMIT 1
    )
FROM festival_unidad_venta fuv
JOIN unidades_venta uv ON uv.id = fuv.unidad_venta_id
CROSS JOIN tmp_dias_ant d
WHERE fuv.festival_id = @festival_anterior_id
  AND @festival_anterior_id IS NOT NULL;

INSERT INTO items_pedido (pedido_id, plato_id, cantidad, precio_unitario)
SELECT p.id,
       pl.id,
       1 + ((DATEDIFF(p.fecha_transaccion, DATE_SUB(CURRENT_DATE(), INTERVAL 60 DAY)) + mp.idx) % 3),
       pl.precio
FROM pedidos p
JOIN unidades_venta uv ON uv.id = p.unidad_venta_id
JOIN tmp_mu_ant mu ON mu.codigo = uv.codigo
JOIN tmp_mp_ant mp ON mp.menu = mu.menu
JOIN platos pl
    ON pl.nombre = mp.plato
   AND pl.unidad_venta_id = uv.id
WHERE p.festival_id = @festival_anterior_id
  AND (   mp.idx = (DATEDIFF(p.fecha_transaccion, DATE_SUB(CURRENT_DATE(), INTERVAL 60 DAY)) % mp.cnt) + 1
       OR mp.idx = ((DATEDIFF(p.fecha_transaccion, DATE_SUB(CURRENT_DATE(), INTERVAL 60 DAY)) + 3) % mp.cnt) + 1);

DROP TEMPORARY TABLE IF EXISTS tmp_dias_ant;
DROP TEMPORARY TABLE IF EXISTS tmp_mu_ant;
DROP TEMPORARY TABLE IF EXISTS tmp_mp_ant;

-- 4.3 Pedidos festival anual (15 dias desde hace 365 dias, cantidades menores)
DROP TEMPORARY TABLE IF EXISTS tmp_dias_anu;
CREATE TEMPORARY TABLE tmp_dias_anu (d INT);
INSERT INTO tmp_dias_anu VALUES
(0),(1),(2),(3),(4),(5),(6),(7),(8),(9),
(10),(11),(12),(13),(14);

DROP TEMPORARY TABLE IF EXISTS tmp_mu_anu;
CREATE TEMPORARY TABLE tmp_mu_anu (codigo VARCHAR(10), menu CHAR(1));
INSERT INTO tmp_mu_anu VALUES
('FT-0000101','A'), ('PD-0000101','E'),
('FT-0000102','T'), ('PD-0000102','E'),
('FT-0000103','A'), ('PD-0000103','A'),
('FT-0000104','T'), ('PD-0000104','E'),
('FT-0000105','A'), ('PD-0000105','E');

DROP TEMPORARY TABLE IF EXISTS tmp_mp_anu;
CREATE TEMPORARY TABLE tmp_mp_anu (menu CHAR(1), idx INT, cnt INT, plato VARCHAR(100));
INSERT INTO tmp_mp_anu VALUES
('A',1,7,'Vacío al Libro con Papas Rústicas'),
('A',2,7,'Choripán Clásico con Chimichurri'),
('A',3,7,'Sándwich de Bondiola a la Mostaza'),
('A',4,7,'Matambre a la Pizza al Plato'),
('A',5,7,'Gaseosa Línea Cola 500ml'),
('A',6,7,'Cerveza Artesanal Tirada 500ml'),
('A',7,7,'Agua Mineral 500ml'),
('E',1,8,'Empanada de Carne Cortada a Cuchillo'),
('E',2,8,'Empanada de Jamón y Queso'),
('E',3,8,'Empanada Salteña Picante'),
('E',4,8,'Docena de Empanadas Variadas'),
('E',5,8,'Tamal Criollo Tradicional'),
('E',6,8,'Gaseosa Línea Cola 500ml'),
('E',7,8,'Cerveza Artesanal Tirada 500ml'),
('E',8,8,'Agua Mineral 500ml'),
('T',1,7,'Tacos al Pastor (3 unidades)'),
('T',2,7,'Tacos de Ternera Mechada (3 unidades)'),
('T',3,7,'Quesadillas con Guacamole'),
('T',4,7,'Nachos con Cheddar y Jalapeños'),
('T',5,7,'Gaseosa Línea Cola 500ml'),
('T',6,7,'Cerveza Artesanal Tirada 500ml'),
('T',7,7,'Agua Mineral 500ml');

INSERT INTO pedidos (
    fecha_transaccion, festival_id, unidad_venta_id,
    estado, fecha_registro, registrado_por_id
)
SELECT
    DATE_SUB(CURRENT_DATE(), INTERVAL (365 - d.d) DAY),
    @festival_anual_id,
    uv.id,
    'A',
    TIMESTAMP(
        DATE_SUB(CURRENT_DATE(), INTERVAL (365 - d.d) DAY),
        '12:00:00'
    ),
    (
        SELECT uvs.staff_id
        FROM unidad_venta_staff uvs
        JOIN staff s ON s.id = uvs.staff_id
        WHERE uvs.unidad_venta_id = uv.id
        ORDER BY s.id
        LIMIT 1
    )
FROM festival_unidad_venta fuv
JOIN unidades_venta uv ON uv.id = fuv.unidad_venta_id
CROSS JOIN tmp_dias_anu d
WHERE fuv.festival_id = @festival_anual_id
  AND @festival_anual_id IS NOT NULL;

INSERT INTO items_pedido (pedido_id, plato_id, cantidad, precio_unitario)
SELECT p.id,
       pl.id,
       1 + ((DATEDIFF(p.fecha_transaccion, DATE_SUB(CURRENT_DATE(), INTERVAL 365 DAY)) + mp.idx) % 2),
       pl.precio
FROM pedidos p
JOIN unidades_venta uv ON uv.id = p.unidad_venta_id
JOIN tmp_mu_anu mu ON mu.codigo = uv.codigo
JOIN tmp_mp_anu mp ON mp.menu = mu.menu
JOIN platos pl
    ON pl.nombre = mp.plato
   AND pl.unidad_venta_id = uv.id
WHERE p.festival_id = @festival_anual_id
  AND (   mp.idx = (DATEDIFF(p.fecha_transaccion, DATE_SUB(CURRENT_DATE(), INTERVAL 365 DAY)) % mp.cnt) + 1
       OR mp.idx = ((DATEDIFF(p.fecha_transaccion, DATE_SUB(CURRENT_DATE(), INTERVAL 365 DAY)) + 3) % mp.cnt) + 1);

DROP TEMPORARY TABLE IF EXISTS tmp_dias_anu;
DROP TEMPORARY TABLE IF EXISTS tmp_mu_anu;
DROP TEMPORARY TABLE IF EXISTS tmp_mp_anu;

-- #############################################################################
-- VERIFICACIÓN FINAL
-- #############################################################################
SELECT u.id, u.email, r.nombre AS rol FROM usuarios u JOIN roles r ON r.id = u.rol_id;

SELECT uv.id, uv.codigo, uv.nombre_comercial,
       CASE WHEN ft.id IS NOT NULL THEN 'FOOD_TRUCK'
            WHEN pd.id IS NOT NULL THEN 'PUESTO_DESARMABLE'
            ELSE 'DESCONOCIDO' END AS tipo
FROM unidades_venta uv
LEFT JOIN food_trucks ft ON ft.id = uv.id
LEFT JOIN puestos_desarmables pd ON pd.id = uv.id
ORDER BY uv.codigo;

SELECT f.nombre AS festival, uv.codigo, uv.nombre_comercial,
       COUNT(DISTINCT p.id) AS pedidos,
       COALESCE(SUM(ip.cantidad * ip.precio_unitario), 0) AS recaudado
FROM festivales f
JOIN festival_unidad_venta fuv ON fuv.festival_id = f.id
JOIN unidades_venta uv ON uv.id = fuv.unidad_venta_id
LEFT JOIN pedidos p ON p.unidad_venta_id = uv.id AND p.festival_id = f.id AND p.estado = 'A'
LEFT JOIN items_pedido ip ON ip.pedido_id = p.id
GROUP BY f.nombre, uv.codigo, uv.nombre_comercial
ORDER BY uv.codigo;

COMMIT;
SET SQL_SAFE_UPDATES = 1;