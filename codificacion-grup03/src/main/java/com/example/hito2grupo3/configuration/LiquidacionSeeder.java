package com.example.hito2grupo3.configuration;
import org.hibernate.Hibernate;
import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Configuration
public class LiquidacionSeeder {

    private final RolRepository rolRepo;
    private final UsuarioRepository usuarioRepo;
    private final CategoriaCocinaRepository catRepo;
    private final StaffRepository staffRepo;
    private final UnidadVentaRepository unidadRepo;
    private final FestivalRepository festivalRepo;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate tx;

    public LiquidacionSeeder(RolRepository rolRepo,
                             UsuarioRepository usuarioRepo,
                             CategoriaCocinaRepository catRepo,
                             StaffRepository staffRepo,
                             UnidadVentaRepository unidadRepo,
                             FestivalRepository festivalRepo,
                             PasswordEncoder passwordEncoder,
                             TransactionTemplate tx) {
        this.rolRepo = rolRepo;
        this.usuarioRepo = usuarioRepo;
        this.catRepo = catRepo;
        this.staffRepo = staffRepo;
        this.unidadRepo = unidadRepo;
        this.festivalRepo = festivalRepo;
        this.passwordEncoder = passwordEncoder;
        this.tx = tx;
    }

    private static final String[][] FESTIVALES = {
        {"Festival de la Cerveza y Food Trucks", "2025-03-01", "2025-03-10"},
        {"Fiesta Gastronomica del Rio",           "2025-04-01", "2025-04-20"},
        {"Festival Tradicion y Brasas",          "2025-05-01", "2025-06-04"}
    };

    private static final String[][] UNIDADES = {
        {"FT-001", "La Trinchera Gourmet", "22.50", "FT", "AF123CD", "true"},
        {"PD-001", "Sabores de la Tierra", "35.00", "PD", "3",       "45"}
    };

    private static final String[][] CATEGORIAS = {
        {"Parrillero Especialista", "15000.00"},
        {"Cocinero Minutero",       "10000.00"},
        {"Maestro Pizzero",         "12000.00"},
        {"Pastelero Artesanal",     "14000.00"},
        {"Cocinero Fueguino",       "18000.00"},
        {"Chef Asador Criollo",     "20000.00"}
    };

    private static final String[][][] EMPLEADOS = {
        {
            {"C", "Carlos",   "Benitez",   "34125890", "1988-04-12", "180000.00", "Manana", "1", "12000.00", "2024-05-01", "EMP_USER"},
            {"C", "Mariana",  "Gomez",     "36781245", "1992-08-19", "145000.00", "Tarde",  "2", "8500.00",  "2022-03-15", ""},
            {"C", "Lucas",    "Fernandez", "30456123", "1984-11-05", "165000.00", "Noche",  "3", "15000.00", "2017-06-10", ""},
            {"K", "Gonzalo",  "Perez",     "32987410", "1987-01-22", "175000.00", "Carnes Rojas",        "true", "16000.00", "0", "2023-09-01"},
            {"K", "Sofia",    "Navarro",   "38123654", "1994-06-30", "130000.00", "Minutas",             "true", "9500.00",  "1", "2020-02-15"},
            {"K", "Esteban",  "Morales",   "28541236", "1981-03-14", "195000.00", "Pizzas y Empanadas", "true", "22000.00", "2", "2015-01-10"}
        },
        {
            {"C", "Javier",   "Martinez",  "33412876", "1988-10-09", "150000.00", "Manana", "4", "9000.00",  "2024-08-01", ""},
            {"C", "Camila",   "Alvarez",   "39542109", "1996-05-18", "120000.00", "Tarde",  "5", "6500.00",  "2023-02-01", ""},
            {"C", "Martin",   "Roldan",    "31876543", "1985-12-03", "160000.00", "Noche",  "6", "13500.00", "2019-04-12", ""},
            {"K", "Valentina","Romero",    "37456981", "1993-07-25", "140000.00", "Pasteleria",          "true", "11000.00", "3", "2021-11-20"},
            {"K", "Nicolas",  "Gutierrez", "35678432", "1990-09-14", "170000.00", "Fuegueros",           "true", "18000.00", "4", "2017-08-05"},
            {"K", "Andrea",   "Dominguez", "27981452", "1980-02-28", "190000.00", "Brasas y Ahumados",   "true", "24000.00", "5", "2015-03-01"}
        }
    };

    @Bean
    public CommandLineRunner seedLiquidacionData() {
        return args -> tx.executeWithoutResult(status -> {
            Rol rolAdmin = rolRepo.findByNombre("ADMIN").orElseGet(() -> {
                Rol r = new Rol();
                r.setNombre("ADMIN");
                return rolRepo.save(r);
            });

            Rol rolEmpleado = rolRepo.findByNombre("EMPLEADO").orElseGet(() -> {
                Rol r = new Rol();
                r.setNombre("EMPLEADO");
                return rolRepo.save(r);
            });

            usuarioRepo.findByEmail("admin@epicentro.com").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setEmail("admin@epicentro.com");
                u.setPasswordHash(passwordEncoder.encode("admin123"));
                u.setRol(rolAdmin);
                return usuarioRepo.save(u);
            });

            Usuario usuarioEmpleado = usuarioRepo.findByEmail("empleado@epicentro.com").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setEmail("empleado@epicentro.com");
                u.setPasswordHash(passwordEncoder.encode("empleado123"));
                u.setRol(rolEmpleado);
                return usuarioRepo.save(u);
            });

            List<CategoriaCocina> categorias = new ArrayList<>();
            for (String[] cat : CATEGORIAS) {
                CategoriaCocina cc = catRepo.findByNombre(cat[0]).orElseGet(() -> {
                    CategoriaCocina n = new CategoriaCocina();
                    n.setNombre(cat[0]);
                    n.setPlusFijo(new BigDecimal(cat[1]));
                    return catRepo.save(n);
                });
                categorias.add(cc);
            }

            List<UnidadVenta> unidades = new ArrayList<>();
            for (int u = 0; u < UNIDADES.length; u++) {
                String[] uData = UNIDADES[u];
                String[][] empRows = EMPLEADOS[u];

                List<Staff> staffList = new ArrayList<>();
                Staff responsable = null;

                for (String[] emp : empRows) {
                    boolean esRespLogin = "EMP_USER".equals(emp[emp.length - 1]);
                    Staff s;
                    if ("C".equals(emp[0])) {
                        s = crearCajero(emp, esRespLogin ? usuarioEmpleado : null);
                    } else {
                        s = crearCocinero(emp, categorias);
                    }
                    staffList.add(s);
                    if (esRespLogin) {
                        responsable = s;
                    }
                }

                if (responsable == null) {
                    responsable = staffList.get(0);
                }

                UnidadVenta uv = crearUnidad(uData, responsable, staffList);
                unidades.add(uv);
            }

            Set<UnidadVenta> uvSet = new HashSet<>(unidades);
            for (String[] fd : FESTIVALES) {
                crearFestival(fd, uvSet);
            }
        });
    }

  private Cajero crearCajero(String[] emp, Usuario usuario) {
    if (usuario != null) {
        Optional<Staff> staffConUsuario = staffRepo.findByUsuario_Email(usuario.getEmail());
        if (staffConUsuario.isPresent()
                && Hibernate.unproxy(staffConUsuario.get()) instanceof Cajero c) {
            return c;
        }
    }

    Optional<Staff> existente = staffRepo.findByDni(emp[3]);
    if (existente.isPresent()) {
        return (Cajero) Hibernate.unproxy(existente.get());
    }

    Cajero c = new Cajero();
    c.setNombre(emp[1]);
    c.setApellido(emp[2]);
    c.setDni(emp[3]);
    c.setFechaNacimiento(LocalDate.parse(emp[4]));
    c.setSueldo(new BigDecimal(emp[5]));
    c.setTurno(emp[6]);
    c.setNumeroCaja(Integer.parseInt(emp[7]));
    c.setPlusOadicional(new BigDecimal(emp[8]));
    c.setFechaIngreso(LocalDate.parse(emp[9]));
    c.setUsuario(usuario);
    return staffRepo.save(c);
}

private Cocinero crearCocinero(String[] emp, List<CategoriaCocina> categorias) {
    Optional<Staff> existente = staffRepo.findByDni(emp[3]);
    if (existente.isPresent()) {
        return (Cocinero) Hibernate.unproxy(existente.get());
    }

    Cocinero co = new Cocinero();
    co.setNombre(emp[1]);
    co.setApellido(emp[2]);
    co.setDni(emp[3]);
    co.setFechaNacimiento(LocalDate.parse(emp[4]));
    co.setSueldo(new BigDecimal(emp[5]));
    co.setEspecialidad(emp[6]);
    co.setLibretaSanitaria(Boolean.parseBoolean(emp[7]));
    co.setBonoPeligrosidad(new BigDecimal(emp[8]));
    co.setCategoriaCocina(categorias.get(Integer.parseInt(emp[9])));
    co.setFechaIngreso(LocalDate.parse(emp[10]));
    co.setUsuario(null);
    return staffRepo.save(co);
}

    private UnidadVenta crearUnidad(String[] uData, Staff responsable, List<Staff> staffList) {
        Optional<UnidadVenta> existing = unidadRepo.findAll().stream()
                .filter(u -> Objects.equals(u.getCodigo(), uData[0]))
                .findFirst();

        if (existing.isPresent()) {
            UnidadVenta uv = existing.get();
            boolean changed = false;

            if (responsable.getUsuario() != null
                    && (uv.getResponsable() == null || uv.getResponsable().getUsuario() == null)) {
                uv.setResponsable(responsable);
                changed = true;
            }

            if (uv.getStaffAsignado() == null) {
                uv.setStaffAsignado(new HashSet<>());
            }

            for (Staff s : staffList) {
                boolean yaAsignado = uv.getStaffAsignado().stream()
                        .anyMatch(a -> Objects.equals(a.getId(), s.getId()));
                if (!yaAsignado) {
                    uv.getStaffAsignado().add(s);
                    changed = true;
                }
            }

            if (changed) {
                uv = unidadRepo.save(uv);
            }
            return uv;
        }

        UnidadVenta uv;
        if ("FT".equals(uData[3])) {
            FoodTruck ft = new FoodTruck();
            ft.setPatente(uData[4]);
            ft.setRequiereElectricidad(Boolean.parseBoolean(uData[5]));
            uv = ft;
        } else {
            PuestoDesarmable pd = new PuestoDesarmable();
            pd.setCantidadCarpas(Integer.parseInt(uData[4]));
            pd.setTiempoMontajeMin(Integer.parseInt(uData[5]));
            uv = pd;
        }

        uv.setCodigo(uData[0]);
        uv.setNombreComercial(uData[1]);
        uv.setSuperficieM2(new BigDecimal(uData[2]));
        uv.setResponsable(responsable);
        uv.setStaffAsignado(new HashSet<>(staffList));
        uv.setPlatosOfrecidos(new ArrayList<>());
        uv.setFestivales(new HashSet<>());
        uv.setPedidos(new ArrayList<>());
        return unidadRepo.save(uv);
    }

    private Festival crearFestival(String[] fd, Set<UnidadVenta> uvSet) {
        Optional<Festival> existing = festivalRepo.findAll().stream()
                .filter(f -> f.getNombre() != null && f.getNombre().equalsIgnoreCase(fd[0]))
                .findFirst();

        if (existing.isPresent()) {
            Festival f = existing.get();
            boolean changed = false;
            if (f.getUnidadesVentaHabilitadas() == null) {
                f.setUnidadesVentaHabilitadas(new HashSet<>());
            }
            for (UnidadVenta uv : uvSet) {
                boolean yaHabilitada = f.getUnidadesVentaHabilitadas().stream()
                        .anyMatch(u -> Objects.equals(u.getId(), uv.getId()));
                if (!yaHabilitada) {
                    f.getUnidadesVentaHabilitadas().add(uv);
                    changed = true;
                }
            }
            if (changed) {
                festivalRepo.save(f);
            }
            return f;
        }

        Festival fest = new Festival();
        fest.setNombre(fd[0]);
        fest.setFechaInicio(LocalDate.parse(fd[1]));
        fest.setFechaFin(LocalDate.parse(fd[2]));
        fest.setUnidadesVentaHabilitadas(new HashSet<>(uvSet));
        fest.setStaffGeneral(new HashSet<>());
        fest.setCostos(new HashSet<>());
        return festivalRepo.save(fest);
    }
}
