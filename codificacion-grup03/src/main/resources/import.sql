--  CATEGORÍAS DE COCINA

INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Chef Ejecutivo', 25000.00);
INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Pastelero Repostero', 18000.00);
INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Cocinero Internacional', 20000.00);
INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Chef de Autor', 30000.00);
INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Cocinero Etnico', 17000.00);
INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Sous-Chef', 22000.00);
INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Parrillero Especialista', 15000.00);
INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Maestro Pizzero', 12000.00);
INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Especialista en Comida Rápida', 10000.00);
INSERT INTO categorias_cocina (nombre, plus_fijo) VALUES ('Barista Gourmet', 9000.00);


-- STAFF BASE

INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Lucia', 'Fernandez', '32541897', '1990-04-12', '2021-03-01', 115000.00);
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Martin', 'Gonzalez', '28763412', '1988-09-23', '2020-05-15', 110000.00);
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Romina', 'Soria', '35129876', '1993-11-05', '2022-01-10', 130000.00);
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Fernando', 'Aguirre', '26874531', '1985-06-30', '2019-11-20', 145000.00);
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Valentina', 'Rios', '40123654', '1998-02-17', '2023-04-01', 95000.00);
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Santiago', 'Mendez', '33987214', '1991-07-08', '2021-08-12', 100000.00);
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Camila', 'Herrera', '38456201', '1996-12-22', '2022-06-05', 125000.00);
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Mateo', 'Benitez', '39123888', '1997-03-14', '2023-01-15', 98000.00);
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Sofia', 'Castro', '36554112', '1994-08-19', '2021-10-01', 112000.00);
INSERT INTO staff (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, sueldo) VALUES ('Joaquin', 'Perez', '31445900', '1989-12-01', '2018-04-10', 150000.00);


--  Cajeros 

INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES ((SELECT id FROM staff WHERE dni = '32541897'), 'Manana', 1, 5000.00);
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES ((SELECT id FROM staff WHERE dni = '28763412'), 'Tarde', 2, 4500.00);
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES ((SELECT id FROM staff WHERE dni = '40123654'), 'Manana', 3, 3500.00);
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES ((SELECT id FROM staff WHERE dni = '33987214'), 'Tarde', 4, 4000.00);
INSERT INTO cajero (id, turno, numero_caja, plus_oadicional) VALUES ((SELECT id FROM staff WHERE dni = '39123888'), 'Noche', 5, 6000.00);

-- Cocineros

INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES ((SELECT id FROM staff WHERE dni = '35129876'), 'Parrilla', true, 8000.00, (SELECT id FROM categorias_cocina WHERE nombre = 'Chef Ejecutivo'));
INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES ((SELECT id FROM staff WHERE dni = '26874531'), 'Reposteria', true, 9000.00, (SELECT id FROM categorias_cocina WHERE nombre = 'Pastelero Repostero'));
INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES ((SELECT id FROM staff WHERE dni = '38456201'), 'Italiana', true, 7500.00, (SELECT id FROM categorias_cocina WHERE nombre = 'Maestro Pizzero'));
INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES ((SELECT id FROM staff WHERE dni = '36554112'), 'Hamburguesas & Minutas', true, 5000.00, (SELECT id FROM categorias_cocina WHERE nombre = 'Especialista en Comida Rápida'));
INSERT INTO cocineros (id, especialidad, libreta_sanitaria, bono_peligrosidad, categoria_cocina_id) VALUES ((SELECT id FROM staff WHERE dni = '31445900'), 'Gourmet de Autor', true, 11000.00, (SELECT id FROM categorias_cocina WHERE nombre = 'Chef de Autor'));

-- Festivales

INSERT INTO festivales (nombre, fecha_inicio, fecha_fin) VALUES ('Festival Gourmet BA 2026', '2026-10-15', '2026-10-25');

-- Platos

INSERT INTO platos (nombre, precio) VALUES ('Sandwich de Vacipán Gourmet', 9500.00);
INSERT INTO platos (nombre, precio) VALUES ('Burger Asadora Doble Cheddar', 8800.00);
INSERT INTO platos (nombre, precio) VALUES ('Papas Fritas Rústicas', 4500.00);
INSERT INTO platos (nombre, precio) VALUES ('Empanada Cortada a Cuchillo (Unidad)', 1800.00);
INSERT INTO platos (nombre, precio) VALUES ('Empanada Jamón y Queso (Unidad)', 1600.00);
INSERT INTO platos (nombre, precio) VALUES ('Locro Tradicional (Porción)', 8500.00);
INSERT INTO platos (nombre, precio) VALUES ('Combo 3 Tacos Birria', 9200.00);
INSERT INTO platos (nombre, precio) VALUES ('Nachos con Queso y Guacamole', 5500.00);
INSERT INTO platos (nombre, precio) VALUES ('Smash Burger Triple Bacon', 10500.00);
INSERT INTO platos (nombre, precio) VALUES ('Veggie Burger Crispy', 8900.00);
INSERT INTO platos (nombre, precio) VALUES ('Pizza Muzzarella Individual', 6500.00);
INSERT INTO platos (nombre, precio) VALUES ('Pizza Fugazzeta Especial', 7800.00);
INSERT INTO platos (nombre, precio) VALUES ('Waffle con Nutella y Frutillas', 6200.00);
INSERT INTO platos (nombre, precio) VALUES ('Churros Rellenos (3 Unidades)', 3800.00);

