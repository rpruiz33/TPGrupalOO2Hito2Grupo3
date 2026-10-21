-- 1. CATEGORÍAS DE COCINA
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (1, 'Chef Ejecutivo', 25000.00);
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (2, 'Pastelero Repostero', 18000.00);
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (3, 'Cocinero Internacional', 20000.00);
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (4, 'Chef de Autor', 30000.00);
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (5, 'Cocinero Etnico', 17000.00);
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (6, 'Sous-Chef', 22000.00);
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (7, 'Parrillero Especialista', 15000.00);
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (8, 'Maestro Pizzero', 12000.00);
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (9, 'Especialista en Comida Rápida', 10000.00);
INSERT INTO categorias_cocina (id, nombre, plus_fijo) VALUES (10, 'Barista Gourmet', 9000.00);

-- 2. STAFF BASE
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (1, 'Lucia', 'Fernandez', '32541897', '1990-04-12', '2021-03-01', 115000.00);
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (2, 'Martin', 'Gonzalez', '28763412', '1988-09-23', '2020-05-15', 110000.00);
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (3, 'Romina', 'Soria', '35129876', '1993-11-05', '2022-01-10', 130000.00);
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (4, 'Fernando', 'Aguirre', '26874531', '1985-06-30', '2019-11-20', 145000.00);
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (5, 'Valentina', 'Rios', '40123654', '1998-02-17', '2023-04-01', 95000.00);
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (6, 'Santiago', 'Mendez', '33987214', '1991-07-08', '2021-08-12', 100000.00);
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (7, 'Camila', 'Herrera', '38456201', '1996-12-22', '2022-06-05', 125000.00);
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (8, 'Mateo', 'Benitez', '39123888', '1997-03-14', '2023-01-15', 98000.00);
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (9, 'Sofia', 'Castro', '36554112', '1994-08-19', '2021-10-01', 112000.00);
INSERT INTO staff (id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES (10, 'Joaquin', 'Perez', '31445900', '1989-12-01', '2018-04-10', 150000.00);

-- 3. CAJEROS
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES (1, 'Manana', 1, 5000.00);
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES (2, 'Tarde', 2, 4500.00);
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES (5, 'Manana', 3, 3500.00);
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES (6, 'Tarde', 4, 4000.00);
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES (8, 'Noche', 5, 6000.00);

-- 4. COCINEROS
INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES (3, 'Parrilla', 1, 8000.00, 1);
INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES (4, 'Reposteria', 1, 9000.00, 2);
INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES (7, 'Italiana', 1, 7500.00, 8);
INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES (9, 'Hamburguesas & Minutas', 1, 5000.00, 9);
INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES (10, 'Gourmet de Autor', 1, 11000.00, 4);

-- 5. FESTIVALES
INSERT INTO festivales (id, nombre, fecha_inicio, fecha_fin) VALUES (1, 'Sabores del Norte', '2026-10-15', '2026-10-25');
INSERT INTO festivales (id, nombre, fecha_inicio, fecha_fin) VALUES (2, 'Festival Gourmet BA', '2025-07-04', '2025-07-07');

-- 6. PLATOS
INSERT INTO platos (id, nombre, precio) VALUES (1, 'Sandwich de Vacio Gourmet', 9500.00);
INSERT INTO platos (id, nombre, precio) VALUES (2, 'Burger Asadora Doble Cheddar', 8800.00);
INSERT INTO platos (id, nombre, precio) VALUES (3, 'Papas Fritas Rústicas', 4500.00);
INSERT INTO platos (id, nombre, precio) VALUES (4, 'Empanada Cortada a Cuchillo (Unidad)', 1800.00);
INSERT INTO platos (id, nombre, precio) VALUES (5, 'Empanada Jamón y Queso (Unidad)', 1600.00);
INSERT INTO platos (id, nombre, precio) VALUES (6, 'Locro Tradicional (Porción)', 8500.00);
INSERT INTO platos (id, nombre, precio) VALUES (7, 'Combo 3 Tacos Birria', 9200.00);

-- 7. UNIDADES DE VENTA
INSERT INTO unidades_venta (id, codigo, nombre_comercial, superficie_m2, responsable_id) VALUES 
(1, 'FT-0000001', 'El Asador Viajero', 18.00, 1),
(2, 'PD-0000001', 'La Empanada Criolla', 24.00, 5),
(3, 'FT-0000002', 'Tacos y Cia', 15.00, 9);

-- 8. FOOD TRUCKS
-- Debe enlazar directamente con los IDs 1 y 3 creados en unidades_venta
INSERT INTO food_trucks (id, patente, requiere_electricidad) VALUES
(1, 'AA123BB', 1),
(3, 'CC456DD', 0);

-- 9. UNIDAD VENTA STAFF
INSERT INTO unidad_venta_staff (unidad_venta_id, staff_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(2, 5), (2, 6), (2, 7), (2, 8),
(3, 9), (3, 10);

-- 10. FESTIVAL UNIDAD VENTA
INSERT INTO festival_unidad_venta (festival_id, unidad_venta_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2), (2, 3);