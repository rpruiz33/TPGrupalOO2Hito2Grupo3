package com.example.hito2grupo3.configuration;

import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataCarga {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            StaffRepository staffRepository,
            CategoriaCocinaRepository categoriaCocinaRepository,
            FestivalRepository festivalRepository,
            UnidadVentaRepository unidadVentaRepository,
            PasswordEncoder passwordEncoder,
            TransactionTemplate transactionTemplate) {

        // Todo el seeder corre en UNA transacción: las entidades quedan managed
        // y no se produce "Detached entity passed to persist".
        return args -> transactionTemplate.executeWithoutResult(status -> {

            // 1. Roles del sistema
            Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseGet(() -> {
                Rol r = new Rol();
                r.setNombre("ADMIN");
                return rolRepository.save(r);
            });
            Rol rolEmpleado = rolRepository.findByNombre("EMPLEADO").orElseGet(() -> {
                Rol r = new Rol();
                r.setNombre("EMPLEADO");
                return rolRepository.save(r);
            });

            // 2. Usuarios base
            Usuario admin = usuarioRepository.findByEmail("admin@epicentro.com").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setEmail("admin@epicentro.com");
                u.setPasswordHash(passwordEncoder.encode("admin123"));
                u.setRol(rolAdmin);
                return usuarioRepository.save(u);
            });

            Usuario empleado = usuarioRepository.findByEmail("empleado@epicentro.com").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setEmail("empleado@epicentro.com");
                u.setPasswordHash(passwordEncoder.encode("empleado123"));
                u.setRol(rolEmpleado);
                return usuarioRepository.save(u);
            });

            // 3. Categorías de cocina
            CategoriaCocina catChef = categoriaCocinaRepository.findByNombre("Chef Ejecutivo").orElseGet(() -> {
                CategoriaCocina c = new CategoriaCocina();
                c.setNombre("Chef Ejecutivo");
                c.setPlusFijo(new BigDecimal("150000"));
                return categoriaCocinaRepository.save(c);
            });
            CategoriaCocina catAyudante = categoriaCocinaRepository.findByNombre("Ayudante de Cocina").orElseGet(() -> {
                CategoriaCocina c = new CategoriaCocina();
                c.setNombre("Ayudante de Cocina");
                c.setPlusFijo(new BigDecimal("50000"));
                return categoriaCocinaRepository.save(c);
            });

            // 4. Staff con cuenta de usuario vinculada
            Staff staffResponsable = staffRepository.findByDni("30111222").orElseGet(() -> {
                Staff s = new Staff();
                s.setNombre("Carlos");
                s.setApellido("Rodríguez");
                s.setDni("30111222");
                s.setFechaNacimiento(LocalDate.of(1985, 4, 12));
                s.setFechaIngreso(LocalDate.of(2018, 3, 1));
                s.setSueldo(new BigDecimal("120000"));
                s.setUsuario(empleado);
                return staffRepository.save(s);
            });

            staffRepository.findByDni("20000001").orElseGet(() -> {
                Staff s = new Staff();
                s.setNombre("Administrador");
                s.setApellido("General");
                s.setDni("20000001");
                s.setFechaNacimiento(LocalDate.of(1980, 1, 1));
                s.setFechaIngreso(LocalDate.of(2015, 1, 1));
                s.setSueldo(new BigDecimal("200000"));
                s.setUsuario(admin);
                return staffRepository.save(s);
            });

            // 5. Personal operativo (sin usuario propio)
            Cocinero cocinero1 = (Cocinero) staffRepository.findByDni("35444555").orElseGet(() -> {
                Cocinero c = new Cocinero();
                c.setNombre("Martín");
                c.setApellido("Gómez");
                c.setDni("35444555");
                c.setFechaNacimiento(LocalDate.of(1990, 6, 15));
                c.setFechaIngreso(LocalDate.of(2021, 2, 10));
                c.setSueldo(new BigDecimal("100000"));
                c.setEspecialidad("Parrilla");
                c.setLibretaSanitaria(true);
                c.setBonoPeligrosidad(new BigDecimal("20000"));
                c.setCategoriaCocina(catChef);
                return staffRepository.save(c);
            });

            Cocinero cocinero2 = (Cocinero) staffRepository.findByDni("38999888").orElseGet(() -> {
                Cocinero c = new Cocinero();
                c.setNombre("Lucía");
                c.setApellido("Fernández");
                c.setDni("38999888");
                c.setFechaNacimiento(LocalDate.of(1995, 11, 20));
                c.setFechaIngreso(LocalDate.of(2023, 5, 1));
                c.setSueldo(new BigDecimal("100000"));
                c.setEspecialidad("Pastas");
                c.setLibretaSanitaria(true);
                c.setBonoPeligrosidad(new BigDecimal("15000"));
                c.setCategoriaCocina(catAyudante);
                return staffRepository.save(c);
            });

            Cajero cajero1 = (Cajero) staffRepository.findByDni("41222333").orElseGet(() -> {
                Cajero c = new Cajero();
                c.setNombre("Esteban");
                c.setApellido("Quispe");
                c.setDni("41222333");
                c.setFechaNacimiento(LocalDate.of(1998, 8, 5));
                c.setFechaIngreso(LocalDate.of(2022, 1, 15));
                c.setSueldo(new BigDecimal("100000"));
                c.setTurno("Tarde/Noche");
                c.setNumeroCaja(1);
                c.setPlusOadicional(new BigDecimal("10000"));
                return staffRepository.save(c);
            });

            Cajero cajero2 = (Cajero) staffRepository.findByDni("43777666").orElseGet(() -> {
                Cajero c = new Cajero();
                c.setNombre("Valeria");
                c.setApellido("Paz");
                c.setDni("43777666");
                c.setFechaNacimiento(LocalDate.of(2001, 3, 25));
                c.setFechaIngreso(LocalDate.of(2024, 2, 1));
                c.setSueldo(new BigDecimal("100000"));
                c.setTurno("Mañana/Tarde");
                c.setNumeroCaja(2);
                c.setPlusOadicional(new BigDecimal("5000"));
                return staffRepository.save(c);
            });

            // 6. Festivales y unidades de venta
            Festival festival1 = festivalRepository.findAll().stream()
                    .filter(f -> "Epicentro Gourmet UNLa 2026".equals(f.getNombre()))
                    .findFirst().orElseGet(() -> {
                        Festival f = new Festival();
                        f.setNombre("Epicentro Gourmet UNLa 2026");
                        f.setFechaInicio(LocalDate.of(2026, 10, 1));
                        f.setFechaFin(LocalDate.of(2026, 10, 20));
                        return festivalRepository.save(f);
                    });

            if (unidadVentaRepository.count() == 0) {
                FoodTruck ft = new FoodTruck();
                ft.setCodigo("FT-001");
                ft.setNombreComercial("Puesto 1 - Hamburguesas");
                ft.setSuperficieM2(new BigDecimal("25.50"));
                ft.setPatente("AF123CD");
                ft.setRequiereElectricidad(true);
                ft.setResponsable(staffResponsable);

                Set<Staff> staffUnidad = new HashSet<>();
                staffUnidad.add(cocinero1);
                staffUnidad.add(cocinero2);
                staffUnidad.add(cajero1);
                staffUnidad.add(cajero2);
                ft.setStaffAsignado(staffUnidad);

                UnidadVenta uvGuardada = unidadVentaRepository.save(ft);
                festival1.getUnidadesVentaHabilitadas().add(uvGuardada);
                festivalRepository.save(festival1);
            }

            System.out.println(">>> ¡Base de datos inicializada con éxito! <<<");
        });
    }
}