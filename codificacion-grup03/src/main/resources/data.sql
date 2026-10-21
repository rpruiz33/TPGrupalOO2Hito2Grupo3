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
INSERT INTO platos (id, nombre, precio) VALUES (1, 'Sandwich de Vacio ', 9500.00);
INSERT INTO platos (id, nombre, precio) VALUES (2, 'Burger Doble Cheddar', 8800.00);
INSERT INTO platos (id, nombre, precio) VALUES (3, 'Papas con Chedas y Verdeo', 4500.00);
INSERT INTO platos (id, nombre, precio) VALUES (4, 'Empanada Cortada a Cuchillo (Unidad)', 1800.00);
INSERT INTO platos (id, nombre, precio) VALUES (5, 'Empanada Jamón y Queso (Unidad)', 1600.00);
INSERT INTO platos (id, nombre, precio) VALUES (6, 'Empanada Pollo y Verdeo (Unidad)', 1600.00);
INSERT INTO platos (id, nombre, precio) VALUES (7, 'Papas Fritas Rusticas', 4000.00);
INSERT INTO platos (id, nombre, precio) VALUES (8, 'Burger Kid', 8000.00);
INSERT INTO platos (id, nombre, precio) VALUES (9, 'Burger Triple Bacon', 10500.00);
INSERT INTO platos (id, nombre, precio) VALUES (10, 'Veggie Burger ', 8900.00);
INSERT INTO platos (id, nombre, precio) VALUES (11, 'Pizza Muzzarella Individual', 10000.00);
INSERT INTO platos (id, nombre, precio) VALUES (12, 'Pizza Fugazzeta Especial', 14000.00);
INSERT INTO platos (id, nombre, precio) VALUES (13, 'Pizza Calabrsa', 14000.00);
INSERT INTO platos (id, nombre, precio) VALUES (14, 'Pizza Jamon y Morron', 12000.00);
INSERT INTO platos (id, nombre, precio) VALUES (15, 'Pizza Napolitana', 10000.00);
INSERT INTO platos (id, nombre, precio) VALUES (16, 'Sorrentinos', 7000.00);
INSERT INTO platos (id, nombre, precio) VALUES (17, 'Ravioles', 7000.00);
INSERT INTO platos (id, nombre, precio) VALUES (18, 'Capeletis', 7000.00);
INSERT INTO platos (id, nombre, precio) VALUES (19, 'Ñoqis', 7000.00);
INSERT INTO platos (id, nombre, precio) VALUES (20, 'Fideos Frescos', 7000.00);
-- POSTRES
INSERT INTO platos (id, nombre, precio) VALUES (21, 'Frutilla con Cremas', 5000.00);
INSERT INTO platos (id, nombre, precio) VALUES (22, 'Duraznos con Crema', 5000.00);
INSERT INTO platos (id, nombre, precio) VALUES (23, 'Flan', 7000.00);
INSERT INTO platos (id, nombre, precio) VALUES (24, 'Torta (Porcion)', 8000.00);
INSERT INTO platos (id, nombre, precio) VALUES (25, 'Cañoncitos', 10000.00);

-- 7. UNIDADES DE VENTA
INSERT INTO unidades_venta (id, codigo, nombre_comercial, superficie_m2, responsable_id) VALUES 
(1, 'FT-0000001', 'El Asador Viajero', 18.00, 1),
(2, 'PD-0000001', 'La Empanada Criolla', 24.00, 5),
(3, 'FT-0000002', 'Tacos y Cia', 15.00, 9);

-- 8. FOOD TRUCKS

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


-- 11. PUESTO DESARMABLE ADICIONAL (Corregido a ID 4)
INSERT INTO unidades_venta (id, codigo, nombre_comercial, superficie_m2, responsable_id) 
VALUES (4, 'PD-0000102', 'La Rinconada Criolla', 40.00, 8);

INSERT INTO puestos_desarmables (id, cantidad_carpas, tiempo_montaje_min) 
VALUES (4, 4, 60);

INSERT INTO unidad_venta_staff (unidad_venta_id, staff_id) VALUES (4, 8), (4, 9);
INSERT INTO festival_unidad_venta (festival_id, unidad_venta_id) VALUES (1, 4);

-- 12. COSTOS
INSERT INTO costos (id, tipo_costo, monto, tipo_unidad_de_medida) VALUES
(1, 'Alquiler de Espacio', 150000.00, 'Fijo'),
(2, 'Seguridad y Limpieza', 45000.00, 'Por Dia'),
(3, 'Licencia Municipal', 30000.00, 'Evento'),
(4, 'Inspeccion Sanitaria', 12000.00, 'Unico');

-- 13. PEDIDOS 
INSERT INTO pedidos (id, fecha_transaccion, festival_id, unidad_venta_id) VALUES
(1, '2025-03-02 12:30:00', 1, 1),
(2, '2025-03-02 14:15:00', 1, 1),
(3, '2025-03-03 20:00:00', 1, 2),
(4, '2025-04-05 13:45:00', 2, 2),
(5, '2025-04-06 21:10:00', 2, 1);

-- 14. UNIDAD DE VENTA - PLATO
INSERT INTO unidad_venta_plato (unidad_venta_id, plato_id) VALUES
-- Unidad de Venta 1 (La Trinchera Gourmet)
(1, 1),
(1, 2),
(1, 3),
-- Unidad de Venta 2 (Sabores de la Tierra)
(2, 4),
(2, 5),
(2, 6);

-- 15. ITEMS DE PEDIDO 
INSERT INTO items_pedido (id, cantidad, precio_unitario, pedido_id, plato_id) VALUES
-- Pedido 1 ( Festival 1 y Unidad 1)
(1, 2, 9500.00, 1, 1), -- 2x Sandwich de Vacío Gourmet = $19.000
(2, 1, 4500.00, 1, 3), -- 1x Papas Fritas Rústicas = $4.500

-- Pedido 2 ( Festival 1 y Unidad 1)
(3, 1, 8800.00, 2, 2), -- 1x Burger Asadora Doble Cheddar = $8.800

-- Pedido 3 ( Festival 1 y Unidad 2)
(4, 3, 1800.00, 3, 4), -- 3x Empanada Cortada a Cuchillo = $5.400
(5, 1, 9200.00, 3, 7); -- 1x Combo 3 Tacos Birria = $9.200