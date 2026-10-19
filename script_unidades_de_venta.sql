SET SQL_SAFE_UPDATES = 0;
START TRANSACTION;

-- =============================================================================
-- 1. FESTIVAL ÚNICO
-- =============================================================================
INSERT INTO festivales (nombre, fecha_inicio, fecha_fin)
SELECT 'Epicentro Gourmet UNLa 2026', '2026-10-01', '2026-10-20'
WHERE NOT EXISTS (
    SELECT 1 FROM festivales WHERE nombre = 'Epicentro Gourmet UNLa 2026'
);

SET @festival_id = (SELECT id FROM festivales WHERE nombre = 'Epicentro Gourmet UNLa 2026' LIMIT 1);
SET @user_empleado_id = (SELECT id FROM usuarios WHERE email = 'empleado@epicentro.com' LIMIT 1);

-- =============================================================================
-- 2. TABLAS TEMPORALES DE CARGA
-- =============================================================================
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
('FT-101', 'La Trinchera Gourmet',    22.50, 'FT', 'AF101CD', 1, NULL, NULL, '34125890'),
('PD-101', 'Sabores Criollos',        30.00, 'PD', NULL,     NULL, 3,    45,   '33412876'),
('FT-102', 'El Buen Sabor Rodante',   20.00, 'FT', 'AF102CD', 1, NULL, NULL, '32145698'),
('PD-102', 'La Esquina Dulce',        25.00, 'PD', NULL,     NULL, 2,    30,   '35478912'),
('FT-103', 'Burger Master Truck',     24.00, 'FT', 'AF103CD', 1, NULL, NULL, '31258741'),
('PD-103', 'Brasas Serranas',         35.00, 'PD', NULL,     NULL, 4,    60,   '36985214'),
('FT-104', 'Tacos El Guero',          18.00, 'FT', 'AF104CD', 0, NULL, NULL, '29874512'),
('PD-104', 'Pastas de la Nonna',      28.00, 'PD', NULL,     NULL, 3,    50,   '37894561'),
('FT-105', 'Ahumados Patagonicos',    22.00, 'FT', 'AF105CD', 1, NULL, NULL, '30147852'),
('PD-105', 'Empanadas Saltenas',      32.00, 'PD', NULL,     NULL, 3,    40,   '38521479');

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
-- U1: FT-101 (Principal, responsable empleado@epicentro.com)
('C', 'Carlos',    'Benitez',   '34125890', '1988-04-12', '2024-05-01', 180000.00, 'Manana', 1,  12000.00, NULL, NULL, NULL, NULL, 'FT-101'),
('C', 'Mariana',   'Gomez',     '36781245', '1992-08-19', '2022-03-15', 145000.00, 'Tarde',  2,   8500.00, NULL, NULL, NULL, NULL, 'FT-101'),
('C', 'Lucas',     'Fernandez', '30456123', '1984-11-05', '2017-06-10', 165000.00, 'Noche',  3,  15000.00, NULL, NULL, NULL, NULL, 'FT-101'),
('K', 'Gonzalo',   'Perez',     '32987410', '1987-01-22', '2023-09-01', 175000.00, NULL, NULL, NULL, 'Carnes Rojas', 1, 16000.00, 'Parrillero Especialista U1', 'FT-101'),
('K', 'Sofia',     'Navarro',   '38123654', '1994-06-30', '2020-02-15', 130000.00, NULL, NULL, NULL, 'Minutas',      1,  9500.00, 'Minutero U1',               'FT-101'),
('K', 'Esteban',   'Morales',   '28541236', '1981-03-14', '2016-01-10', 195000.00, NULL, NULL, NULL, 'Pizzas',       1, 22000.00, 'Pizzero U1',                'FT-101'),

-- U2: PD-101
('C', 'Javier',    'Martinez',  '33412876', '1988-10-09', '2024-08-01', 150000.00, 'Manana', 4,   9000.00, NULL, NULL, NULL, NULL, 'PD-101'),
('C', 'Camila',    'Alvarez',   '39542109', '1996-05-18', '2023-02-01', 120000.00, 'Tarde',  5,   6500.00, NULL, NULL, NULL, NULL, 'PD-101'),
('C', 'Martin',    'Roldan',    '31876543', '1985-12-03', '2019-04-12', 160000.00, 'Noche',  6,  13500.00, NULL, NULL, NULL, NULL, 'PD-101'),
('K', 'Valentina', 'Romero',    '37456981', '1993-07-25', '2021-11-20', 140000.00, NULL, NULL, NULL, 'Parrilla',     1, 11000.00, 'Parrillero Especialista U2', 'PD-101'),
('K', 'Nicolas',   'Gutierrez', '35678432', '1990-09-14', '2017-08-05', 170000.00, NULL, NULL, NULL, 'Gourmet',      1, 18000.00, 'Minutero U2',               'PD-101'),
('K', 'Andrea',    'Dominguez', '27981452', '1980-02-28', '2016-03-01', 190000.00, NULL, NULL, NULL, 'Pasteleria',   1, 24000.00, 'Pizzero U2',                'PD-101'),

-- U3: FT-102
('C', 'Marcos',    'Diaz',      '32145698', '1986-07-15', '2018-05-10', 165000.00, 'Manana', 7,  14000.00, NULL, NULL, NULL, NULL, 'FT-102'),
('C', 'Florencia', 'Acosta',    '40125896', '1997-03-22', '2025-01-15', 110000.00, 'Tarde',  8,   5000.00, NULL, NULL, NULL, NULL, 'FT-102'),
('C', 'Ignacio',   'Paz',       '36258974', '1991-11-04', '2022-07-01', 135000.00, 'Noche',  9,   8000.00, NULL, NULL, NULL, NULL, 'FT-102'),
('K', 'Ramiro',    'Silva',     '29365412', '1982-09-18', '2017-04-12', 185000.00, NULL, NULL, NULL, 'Brasas',       1, 20000.00, 'Parrillero Especialista U3', 'FT-102'),
('K', 'Carla',     'Mendoza',   '38452147', '1994-12-08', '2020-09-10', 145000.00, NULL, NULL, NULL, 'Minutas',      1, 12000.00, 'Minutero U3',               'FT-102'),
('K', 'Gaston',    'Herrera',   '34789512', '1989-06-30', '2019-02-20', 160000.00, NULL, NULL, NULL, 'Pizzas',       1, 15000.00, 'Pizzero U3',                'FT-102'),

-- U4: PD-102
('C', 'Laura',     'Blanco',    '35478912', '1990-05-14', '2019-10-01', 155000.00, 'Manana', 10, 11000.00, NULL, NULL, NULL, NULL, 'PD-102'),
('C', 'Tomas',     'Ibarra',    '41258963', '1998-08-25', '2024-09-01', 105000.00, 'Tarde',  11,  4500.00, NULL, NULL, NULL, NULL, 'PD-102'),
('C', 'Belen',     'Suarez',    '33698521', '1988-02-17', '2018-03-15', 160000.00, 'Noche',  12, 13000.00, NULL, NULL, NULL, NULL, 'PD-102'),
('K', 'Federico',  'Quiroga',   '30874521', '1984-10-11', '2016-08-20', 190000.00, NULL, NULL, NULL, 'Pastas',       1, 21000.00, 'Parrillero Especialista U4', 'PD-102'),
('K', 'Agustina',  'Vidal',     '37145289', '1993-01-09', '2021-04-05', 135000.00, NULL, NULL, NULL, 'Dulces',       1,  9000.00, 'Minutero U4',               'PD-102'),
('K', 'Emiliano',  'Sosa',      '27541289', '1979-11-29', '2016-02-10', 200000.00, NULL, NULL, NULL, 'Panaderia',    1, 23000.00, 'Pizzero U4',                'PD-102'),

-- U5: FT-103
('C', 'Sergio',    'Ramos',     '31258741', '1985-04-03', '2017-09-12', 170000.00, 'Manana', 13, 16000.00, NULL, NULL, NULL, NULL, 'FT-103'),
('C', 'Paula',     'Cardozo',   '39874563', '1996-12-14', '2023-05-18', 125000.00, 'Tarde',  14,  7000.00, NULL, NULL, NULL, NULL, 'FT-103'),
('C', 'Joaquin',   'Medina',    '34896523', '1989-10-21', '2020-01-10', 140000.00, 'Noche',  15,  9500.00, NULL, NULL, NULL, NULL, 'FT-103'),
('K', 'Claudio',   'Cabrera',   '28745698', '1981-08-16', '2016-05-01', 195000.00, NULL, NULL, NULL, 'Hamburguesas', 1, 22000.00, 'Parrillero Especialista U5', 'FT-103'),
('K', 'Romina',    'Flores',    '36541278', '1992-03-05', '2022-08-14', 130000.00, NULL, NULL, NULL, 'Frituras',     1, 10000.00, 'Minutero U5',               'FT-103'),
('K', 'Hernan',    'Castillo',  '33125874', '1987-07-28', '2019-11-03', 165000.00, NULL, NULL, NULL, 'Salsas',       1, 14000.00, 'Pizzero U5',                'FT-103'),

-- U6: PD-103
('C', 'Pablo',     'Castro',    '36985214', '1992-06-18', '2021-03-10', 145000.00, 'Manana', 16,  8500.00, NULL, NULL, NULL, NULL, 'PD-103'),
('C', 'Valeria',   'Gimenez',   '42145896', '2000-01-20', '2025-06-01',  95000.00, 'Tarde',  17,  3500.00, NULL, NULL, NULL, NULL, 'PD-103'),
('C', 'Esteban',   'Molina',    '30985214', '1984-05-12', '2017-02-14', 170000.00, 'Noche',  18, 15000.00, NULL, NULL, NULL, NULL, 'PD-103'),
('K', 'Mauricio',  'Duarte',    '32456987', '1986-11-23', '2018-09-01', 180000.00, NULL, NULL, NULL, 'Cordero',      1, 19000.00, 'Parrillero Especialista U6', 'PD-103'),
('K', 'Luciana',   'Godoy',     '38965412', '1995-04-17', '2023-10-15', 120000.00, NULL, NULL, NULL, 'Guarniciones', 1,  8000.00, 'Minutero U6',               'PD-103'),
('K', 'Julian',    'Ferreyra',  '26985412', '1978-08-30', '2016-04-01', 200000.00, NULL, NULL, NULL, 'Ahumados',     1, 25000.00, 'Pizzero U6',                'PD-103'),

-- U7: FT-104
('C', 'Daniel',    'Rios',      '29874512', '1983-02-27', '2016-07-10', 175000.00, 'Manana', 19, 17000.00, NULL, NULL, NULL, NULL, 'FT-104'),
('C', 'Micaela',   'Ponce',     '41789654', '1999-09-08', '2024-11-01', 100000.00, 'Tarde',  20,  4000.00, NULL, NULL, NULL, NULL, 'FT-104'),
('C', 'Adrian',    'Vargas',    '35124578', '1990-03-19', '2020-06-12', 150000.00, 'Noche',  21, 10500.00, NULL, NULL, NULL, NULL, 'FT-104'),
('K', 'Jorge',     'Ortega',    '31896547', '1985-12-11', '2017-08-15', 185000.00, NULL, NULL, NULL, 'Tacos',        1, 17500.00, 'Parrillero Especialista U7', 'FT-104'),
('K', 'Gisela',    'Aguirre',   '37854123', '1993-10-02', '2022-01-20', 135000.00, NULL, NULL, NULL, 'Salsas Mex',   1, 11000.00, 'Minutero U7',               'FT-104'),
('K', 'Rodrigo',   'Benitez',   '34258963', '1989-01-14', '2019-05-18', 160000.00, NULL, NULL, NULL, 'Tortillas',    1, 13000.00, 'Pizzero U7',                'FT-104'),

-- U8: PD-104
('C', 'Lucas',     'Ortiz',     '37894561', '1993-08-04', '2021-09-01', 140000.00, 'Manana', 22,  8000.00, NULL, NULL, NULL, NULL, 'PD-104'),
('C', 'Brenda',    'Peralta',   '40896523', '1998-04-19', '2023-04-15', 115000.00, 'Tarde',  23,  6000.00, NULL, NULL, NULL, NULL, 'PD-104'),
('C', 'Gustavo',   'Franco',    '28965412', '1982-01-07', '2016-09-20', 180000.00, 'Noche',  24, 18000.00, NULL, NULL, NULL, NULL, 'PD-104'),
('K', 'Mariano',   'Nieto',     '33852147', '1988-06-25', '2018-07-12', 170000.00, NULL, NULL, NULL, 'Pastas Caseras',1, 16000.00, 'Parrillero Especialista U8', 'PD-104'),
('K', 'Natalia',   'Campos',    '36412589', '1991-12-18', '2020-10-05', 145000.00, NULL, NULL, NULL, 'Rellenos',     1, 12500.00, 'Minutero U8',               'PD-104'),
('K', 'Cristian',  'Barrios',   '30258963', '1983-10-15', '2017-01-10', 190000.00, NULL, NULL, NULL, 'Salsas Ita',   1, 20500.00, 'Pizzero U8',                'PD-104'),

-- U9: FT-105
('C', 'Matias',    'Luna',      '30147852', '1983-07-22', '2017-03-01', 175000.00, 'Manana', 25, 16500.00, NULL, NULL, NULL, NULL, 'FT-105'),
('C', 'Daniela',   'Rojas',     '42896541', '2001-02-14', '2025-08-01',  92000.00, 'Tarde',  26,  3000.00, NULL, NULL, NULL, NULL, 'FT-105'),
('C', 'Federico',  'Maldonado', '35896541', '1991-05-09', '2020-04-18', 150000.00, 'Noche',  27, 10000.00, NULL, NULL, NULL, NULL, 'FT-105'),
('K', 'Leandro',   'Santillan', '32896541', '1987-09-30', '2018-06-25', 180000.00, NULL, NULL, NULL, 'Trucha Ahumada',1, 18500.00, 'Parrillero Especialista U9', 'FT-105'),
('K', 'Sabrina',   'Ibanez',    '37965412', '1994-01-26', '2022-09-10', 135000.00, NULL, NULL, NULL, 'Ciervo',       1, 11500.00, 'Minutero U9',               'FT-105'),
('K', 'Guillermo', 'Paz',       '27896541', '1980-04-12', '2016-01-15', 200000.00, NULL, NULL, NULL, 'Quesos',       1, 24500.00, 'Pizzero U9',                'FT-105'),

-- U10: PD-105
('C', 'Damian',    'Vega',      '38521479', '1994-11-16', '2021-02-01', 145000.00, 'Manana', 28,  9000.00, NULL, NULL, NULL, NULL, 'PD-105'),
('C', 'Florencia', 'Lucero',    '43125896', '2002-06-03', '2025-05-10',  90000.00, 'Tarde',  29,  3000.00, NULL, NULL, NULL, NULL, 'PD-105'),
('C', 'Manuel',    'Bustos',    '31524789', '1985-08-28', '2018-11-20', 165000.00, 'Noche',  30, 14000.00, NULL, NULL, NULL, NULL, 'PD-105'),
('K', 'Oscar',     'Gallardo',  '29521478', '1982-10-05', '2016-10-01', 195000.00, NULL, NULL, NULL, 'Empanada Salteña',1, 22500.00, 'Parrillero Especialista U10','PD-105'),
('K', 'Melisa',    'Carrizo',   '36987451', '1992-04-11', '2020-07-15', 140000.00, NULL, NULL, NULL, 'Locro',          1, 13500.00, 'Minutero U10',             'PD-105'),
('K', 'Walter',    'Farías',    '33521478', '1988-03-02', '2019-01-22', 170000.00, NULL, NULL, NULL, 'Tamales',        1, 16500.00, 'Pizzero U10',              'PD-105');

-- =============================================================================
-- 3. INSERCIÓN DE CATEGORÍAS DE COCINA
-- =============================================================================
INSERT INTO categorias_cocina (nombre, plus_fijo)
SELECT t.nombre, t.plus_fijo
FROM tmp_categorias t
WHERE NOT EXISTS (
    SELECT 1 FROM categorias_cocina c WHERE c.nombre = t.nombre
);

-- =============================================================================
-- 4. INSERCIÓN DE STAFF
-- =============================================================================
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo, usuario_id)
SELECT t.nombre, t.apellido, t.dni, t.fecha_nac, t.fecha_ingreso, t.sueldo,
       CASE WHEN t.dni = '34125890' THEN @user_empleado_id ELSE NULL END
FROM tmp_empleados t
WHERE NOT EXISTS (
    SELECT 1 FROM staff s WHERE s.dni = t.dni
);

UPDATE staff 
SET usuario_id = @user_empleado_id
WHERE id = (SELECT id FROM (SELECT id FROM staff WHERE dni = '34125890' LIMIT 1) sub)
  AND usuario_id IS NULL 
  AND @user_empleado_id IS NOT NULL;

-- =============================================================================
-- 5. INSERCIÓN DE CAJEROS Y COCINEROS
-- =============================================================================
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional)
SELECT s.id, t.turno, t.num_caja, t.plus_adicional
FROM tmp_empleados t
JOIN staff s ON s.dni = t.dni
WHERE t.tipo = 'C'
  AND NOT EXISTS (SELECT 1 FROM cajero c WHERE c.id = s.id);

INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id)
SELECT s.id, t.especialidad, t.libreta, t.bono_pelig, c.id
FROM tmp_empleados t
JOIN staff s ON s.dni = t.dni
JOIN categorias_cocina c ON c.nombre = t.cat_nombre
WHERE t.tipo = 'K'
  AND NOT EXISTS (SELECT 1 FROM cocineros k WHERE k.id = s.id);

-- =============================================================================
-- 6. INSERCIÓN DE UNIDADES DE VENTA
-- =============================================================================
INSERT INTO unidades_venta (codigo, nombre_comercial, superficie_m2, responsable_id)
SELECT u.codigo, u.nombre_comercial, u.superficie, s.id
FROM tmp_unidades u
JOIN staff s ON s.dni = u.dni_responsable
WHERE NOT EXISTS (
    SELECT 1 FROM unidades_venta uv WHERE uv.codigo = u.codigo
);

-- =============================================================================
-- 7. INSERCIÓN DE FOOD TRUCKS Y PUESTOS DESARMABLES
-- =============================================================================
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

-- =============================================================================
-- 8. RELACIONES: UNIDAD_VENTA_STAFF Y FESTIVAL_UNIDAD_VENTA
-- =============================================================================
INSERT INTO unidad_venta_staff (unidad_venta_id, staff_id)
SELECT uv.id, s.id
FROM tmp_empleados t
JOIN staff s ON s.dni = t.dni
JOIN unidades_venta uv ON uv.codigo = t.cod_unidad
WHERE NOT EXISTS (
    SELECT 1 FROM unidad_venta_staff uvs 
    WHERE uvs.unidad_venta_id = uv.id AND uvs.staff_id = s.id
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

COMMIT;
SET SQL_SAFE_UPDATES = 1;