package com.example.hito2grupo3.configuration;

import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Configuration
public class DataSeeder {

    // ── repos ────────────────────────────────────────────────────────────────
    private final RolRepository rolRepo;
    private final CategoriaCocinaRepository categoriaCocinaRepo;
    private final StaffRepository staffRepo;
    private final UnidadVentaRepository unidadVentaRepo;
    private final FestivalRepository festivalRepo;
    private final TransactionTemplate tx;

    public DataSeeder(RolRepository rolRepo,
                      CategoriaCocinaRepository categoriaCocinaRepo,
                      StaffRepository staffRepo,
                      UnidadVentaRepository unidadVentaRepo,
                      FestivalRepository festivalRepo,
                      TransactionTemplate tx) {
        this.rolRepo             = rolRepo;
        this.categoriaCocinaRepo = categoriaCocinaRepo;
        this.staffRepo           = staffRepo;
        this.unidadVentaRepo     = unidadVentaRepo;
        this.festivalRepo        = festivalRepo;
        this.tx                  = tx;
    }

    // =========================================================================
    // DATOS ESTÁTICOS
    // =========================================================================

    /** [nombre, fechaInicio, fechaFin] */
    private static final String[][] FESTIVALES = {
        {"Sabores del Norte",   "2025-03-14", "2025-03-16"},
        {"Festival Gourmet BA", "2025-07-04", "2025-07-07"},
    };

    /**
     * [codigo, nombreComercial, superficieM2, tipo(FT|PD), extra1, extra2]
     *  FT -> extra1=patente, extra2=requiereElectricidad (true/false)
     *  PD -> extra1=cantidadCarpas, extra2=tiempoMontajeMin
     */
    private static final String[][] UNIDADES = {
        {"FT-001", "El Asador Viajero",   "18", "FT", "AA123BB", "true"},
        {"PD-001", "La Empanada Criolla", "24", "PD", "3",       "45"},
        {"FT-002", "Tacos y Cia",         "15", "FT", "CC456DD", "false"},
    };

    /**
     * Por unidad: 4 empleados (2 cajeros + 2 cocineros).
     * Cajero  (C): [tipo, nombre, apellido, dni, fechaNac, sueldo, turno, numeroCaja, plusOadicional, ""]
     * Cocinero(K): [tipo, nombre, apellido, dni, fechaNac, sueldo, especialidad, libretaSan, bonoPelig, catIdx]
     */
    private static final String[][][] EMPLEADOS_POR_UNIDAD = {
        // Unidad 0 -> FT-001
        {
            {"C","Lucia",    "Fernandez","32541897","1990-04-12","115000","Manana",    "1","5000.00",""},
            {"C","Martin",   "Gonzalez", "28763412","1988-09-23","110000","Tarde",     "2","4500.00",""},
            {"K","Romina",   "Soria",    "35129876","1993-11-05","130000","Parrilla",  "true","8000.00","0"},
            {"K","Fernando", "Aguirre",  "26874531","1985-06-30","145000","Reposteria","true","9000.00","1"},
        },
        // Unidad 1 -> PD-001
        {
            {"C","Valentina","Rios",    "40123654","1998-02-17","95000", "Manana",   "3","3500.00",""},
            {"C","Santiago", "Mendez",  "33987214","1991-07-08","100000","Tarde",    "4","4000.00",""},
            {"K","Camila",   "Herrera", "38456201","1996-12-22","125000","Italiana", "true","7500.00","2"},
            {"K","Diego",    "Ramos",   "29345678","1987-03-14","155000","Alta Cocina","false","12000.00","3"},
        },
        // Unidad 2 -> FT-002
        {
            {"C","Agustina", "Lopez",   "41250369","1999-08-03","93000", "Noche",    "5","3000.00",""},
            {"C","Nicolas",  "Castro",  "30874156","1989-05-19","108000","Manana",   "6","4200.00",""},
            {"K","Florencia","Vargas",  "36012487","1994-10-11","135000","Mexicana", "true","8500.00","4"},
            {"K","Ezequiel", "Morales", "27654321","1984-01-28","160000","Fusion",   "true","13000.00","5"},
        },
    };

    /** [nombre, plusFijo]  — una categoria por cocinero (UNIQUE FK en Cocinero) */
    private static final String[][] CATEGORIAS_COCINA = {
        {"Chef Ejecutivo",          "25000.00"},
        {"Pastelero Repostero",     "18000.00"},
        {"Cocinero Internacional",  "20000.00"},
        {"Chef de Autor",           "30000.00"},
        {"Cocinero Etnico",         "17000.00"},
        {"Sous-Chef",               "22000.00"},
    };

    // =========================================================================
    // COMMAND LINE RUNNER
    // =========================================================================

    @Bean
    public CommandLineRunner seedData() {
        return args -> tx.executeWithoutResult(status -> {

            // 1. Categorias de cocina
            List<CategoriaCocina> categorias = new ArrayList<>();
            for (String[] cat : CATEGORIAS_COCINA) {
                CategoriaCocina cc = categoriaCocinaRepo
                        .findByNombre(cat[0])
                        .orElseGet(() -> {
                            CategoriaCocina nuevo = new CategoriaCocina();
                            nuevo.setNombre(cat[0]);
                            nuevo.setPlusFijo(new BigDecimal(cat[1]));
                            return categoriaCocinaRepo.save(nuevo);
                        });
                categorias.add(cc);
            }

            // 2. Unidades de venta (compartidas entre festivales)
            List<UnidadVenta> todasLasUnidades = new ArrayList<>();
            for (int u = 0; u < UNIDADES.length; u++) {
                String[] uData   = UNIDADES[u];
                String[][] empRows = EMPLEADOS_POR_UNIDAD[u];

                // Crear empleados
                List<Staff> staffDeUnidad = new ArrayList<>();
                for (int e = 0; e < empRows.length; e++) {
                    String[] emp = empRows[e];
                    Staff s = "C".equals(emp[0])
                            ? crearCajero(emp)
                            : crearCocinero(emp, categorias);
                    staffDeUnidad.add(s);
                }

                // El primer empleado es responsable
                Staff responsable = staffDeUnidad.get(0);
                UnidadVenta uv = crearUnidad(uData, responsable, staffDeUnidad);
                todasLasUnidades.add(uv);
            }

            // 3. Festivales
            for (int f = 0; f < FESTIVALES.length; f++) {
                final String[] fd = FESTIVALES[f];
                final Set<UnidadVenta> uvSet = new HashSet<>(todasLasUnidades);

                festivalRepo.findAll().stream()
                        .filter(fx -> fx.getNombre().equals(fd[0]))
                        .findFirst()
                        .orElseGet(() -> {
                            Festival fest = new Festival();
                            fest.setNombre(fd[0]);
                            fest.setFechaInicio(LocalDate.parse(fd[1]));
                            fest.setFechaFin(LocalDate.parse(fd[2]));
                            fest.setUnidadesVentaHabilitadas(uvSet);
                            fest.setStaffGeneral(new HashSet<>());
                            fest.setCostos(new HashSet<>());
                            return festivalRepo.save(fest);
                        });
            }
        });
    }

    // =========================================================================
    // HELPERS PRIVADOS
    // =========================================================================

    private Cajero crearCajero(String[] emp) {
        return (Cajero) staffRepo.findByDni(emp[3]).orElseGet(() -> {
            Cajero c = new Cajero();
            c.setNombre(emp[1]);
            c.setApellido(emp[2]);
            c.setDni(emp[3]);
            c.setFechaNacimiento(LocalDate.parse(emp[4]));
            c.setSueldo(new BigDecimal(emp[5]));
            c.setFechaIngreso(LocalDate.parse(emp[4]).plusYears(18).plusMonths(6));
            c.setTurno(emp[6]);
            c.setNumeroCaja(Integer.parseInt(emp[7]));
            c.setPlusOadicional(new BigDecimal(emp[8]));
            c.setUsuario(null);
            return staffRepo.save(c);
        });
    }

    private Cocinero crearCocinero(String[] emp, List<CategoriaCocina> categorias) {
        return (Cocinero) staffRepo.findByDni(emp[3]).orElseGet(() -> {
            Cocinero co = new Cocinero();
            co.setNombre(emp[1]);
            co.setApellido(emp[2]);
            co.setDni(emp[3]);
            co.setFechaNacimiento(LocalDate.parse(emp[4]));
            co.setSueldo(new BigDecimal(emp[5]));
            co.setFechaIngreso(LocalDate.parse(emp[4]).plusYears(18).plusMonths(3));
            co.setEspecialidad(emp[6]);
            co.setLibretaSanitaria(Boolean.parseBoolean(emp[7]));
            co.setBonoPeligrosidad(new BigDecimal(emp[8]));
            co.setCategoriaCocina(categorias.get(Integer.parseInt(emp[9])));
            co.setUsuario(null);
            return staffRepo.save(co);
        });
    }

    private UnidadVenta crearUnidad(String[] uData, Staff responsable, List<Staff> staffList) {
        Optional<UnidadVenta> existing = unidadVentaRepo.findAll().stream()
                .filter(u -> Objects.equals(u.getCodigo(), uData[0]))
                .findFirst();

        return existing.orElseGet(() -> {
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
            return unidadVentaRepo.save(uv);
        });
    }
}
