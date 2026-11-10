package com.example.hito2grupo3.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.configuration.SueldoProperties;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.Rol;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.RolRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;
import com.example.hito2grupo3.services.ICanonService;
import com.example.hito2grupo3.services.IRegistroUnidadVentaService;

@Service
public class RegistroUnidadVentaServiceImpl implements IRegistroUnidadVentaService {

    private final UsuarioRepository usuarioRepository;
    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final FestivalRepository festivalRepository;
    private final ICanonService canonService;
    private final SueldoProperties sueldoProperties;

    @Autowired
    public RegistroUnidadVentaServiceImpl(
            UsuarioRepository usuarioRepository,
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository,
            RolRepository rolRepository,
            FestivalRepository festivalRepository,
            PasswordEncoder passwordEncoder,
            ICanonService canonService,
            SueldoProperties sueldoProperties) {
        this.usuarioRepository = usuarioRepository;
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.rolRepository = rolRepository;
        this.festivalRepository = festivalRepository;
        this.passwordEncoder = passwordEncoder;
        this.canonService = canonService;
        this.sueldoProperties = sueldoProperties;
    }

    private String generarCodigo(String tipoUnidad) {
        String prefijo;

        if ("FOOD_TRUCK".equals(tipoUnidad)) {
            prefijo = FoodTruck.PREFIJO_CODIGO;
        } else if ("PUESTO_DESARMABLE".equals(tipoUnidad)) {
            prefijo = PuestoDesarmable.PREFIJO_CODIGO;
        } else {
            throw new IllegalArgumentException(
                    "Seleccioná un tipo de unidad de venta válido.");
        }

        List<UnidadVenta> unidades = unidadVentaRepository.findByCodigoStartingWith(prefijo);
        int numeroMayor = 0;
        Pattern patron = Pattern.compile("^" + prefijo + "([0-9]+)$");

        for (UnidadVenta unidad : unidades) {
            Matcher matcher = patron.matcher(unidad.getCodigo());
            if (matcher.matches()) {
                int numero = Integer.parseInt(matcher.group(1));
                if (numero > numeroMayor) {
                    numeroMayor = numero;
                }
            }
        }

        if (numeroMayor >= 9_999_999) {
            throw new IllegalArgumentException(
                    "Se alcanzó el máximo de códigos disponibles para este tipo de unidad.");
        }

        return prefijo + String.format("%07d", numeroMayor + 1);
    }

    @Override
    @Transactional
    public void registrar(
            String nombreComercial,
            BigDecimal superficieM2,
            String tipoUnidad,
            String patente,
            Boolean requiereElectricidad,
            Integer cantidadCarpas,
            Integer tiempoMontajeMin,
            String nombre,
            String apellido,
            String dni,
            LocalDate fechaNacimiento,
            LocalDate fechaIngreso,
            String email,
            String password) {

        // Normalizar datos de texto.
        nombreComercial = nombreComercial == null ? "" : nombreComercial.trim();
        nombre = nombre == null ? "" : nombre.trim();
        apellido = apellido == null ? "" : apellido.trim();
        dni = dni == null ? "" : dni.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        patente = patente == null ? "" : patente.trim().toUpperCase();
        final String patenteNormalizada = patente;

        // Validar campos obligatorios generales. La contraseña solo es obligatoria
        // cuando se registra un responsable nuevo; si ya existe el mismo DNI/email,
        // se reutiliza su cuenta y no se modifica su contraseña.
        if (nombreComercial.isBlank()
                || nombre.isBlank()
                || apellido.isBlank()
                || dni.isBlank()
                || email.isBlank()
                || superficieM2 == null
                || tipoUnidad == null
                || fechaNacimiento == null
                || fechaIngreso == null) {
            throw new IllegalArgumentException(
                    "Completá todos los campos obligatorios.");
        }

        if (nombre.length() < 2 || nombre.length() > 50
                || !nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            throw new IllegalArgumentException(
                    "El nombre solo debe contener letras y espacios (entre 2 y 50 caracteres).");
        }

        if (apellido.length() < 2 || apellido.length() > 50
                || !apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            throw new IllegalArgumentException(
                    "El apellido solo debe contener letras y espacios (entre 2 y 50 caracteres).");
        }

        if (nombreComercial.length() < 2 || nombreComercial.length() > 50) {
            throw new IllegalArgumentException(
                    "El nombre comercial debe tener entre 2 y 50 caracteres.");
        }

        // DNI: exclusivamente 7 u 8 dígitos.
        if (!dni.matches("^[0-9]{7,8}$")) {
            throw new IllegalArgumentException(
                    "El DNI debe contener 7 u 8 dígitos numéricos, sin puntos ni letras.");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException(
                    "Ingresá un email con un formato válido.");
        }

        if (superficieM2.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La superficie debe ser mayor que cero.");
        }

        LocalDate hoy = LocalDate.now();

        if (fechaNacimiento.isAfter(hoy)) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser futura.");
        }
        if (fechaNacimiento.plusYears(18).isAfter(hoy)) {
            throw new IllegalArgumentException(
                    "El responsable debe tener al menos 18 años.");
        }
        if (fechaNacimiento.isBefore(hoy.minusYears(100))) {
            throw new IllegalArgumentException(
                    "La edad del responsable no puede superar los 100 años.");
        }

        if (fechaIngreso.isAfter(hoy)) {
            throw new IllegalArgumentException(
                    "La fecha de ingreso no puede ser futura.");
        }
        if (fechaIngreso.isBefore(fechaNacimiento)) {
            throw new IllegalArgumentException(
                    "La fecha de ingreso no puede ser anterior a la fecha de nacimiento.");
        }

        // Validar datos específicos según el tipo de unidad.
        UnidadVenta unidad;

        if ("FOOD_TRUCK".equals(tipoUnidad)) {
            if (patente.isBlank()) {
                throw new IllegalArgumentException(
                        "La patente es obligatoria para un Food Truck.");
            }
            if (!patente.matches("^[A-Z0-9\\s-]{6,10}$")) {
                throw new IllegalArgumentException(
                        "La patente debe tener un formato válido (entre 6 y 10 caracteres alfanuméricos).");
            }

            boolean patenteRegistrada = unidadVentaRepository.findAll().stream()
                    .filter(FoodTruck.class::isInstance)
                    .map(FoodTruck.class::cast)
                    .anyMatch(ft -> ft.getPatente() != null
                            && ft.getPatente().trim().equalsIgnoreCase(patenteNormalizada));
            if (patenteRegistrada) {
                throw new IllegalArgumentException(
                        "Ya existe un Food Truck registrado con esa patente.");
            }

            FoodTruck foodTruck = new FoodTruck();
            foodTruck.setPatente(patente);
            foodTruck.setRequiereElectricidad(Boolean.TRUE.equals(requiereElectricidad));
            unidad = foodTruck;

        } else if ("PUESTO_DESARMABLE".equals(tipoUnidad)) {
            if (cantidadCarpas == null || cantidadCarpas <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad de carpas debe ser mayor que cero.");
            }
            if (tiempoMontajeMin == null || tiempoMontajeMin <= 0) {
                throw new IllegalArgumentException(
                        "El tiempo de montaje debe ser mayor que cero.");
            }

            PuestoDesarmable puesto = new PuestoDesarmable();
            puesto.setCantidadCarpas(cantidadCarpas);
            puesto.setTiempoMontajeMin(tiempoMontajeMin);
            unidad = puesto;

        } else {
            throw new IllegalArgumentException(
                    "Seleccioná un tipo de unidad de venta válido.");
        }

        // DNI y email pueden repetirse únicamente cuando ambos identifican
        // al mismo Staff y al mismo Usuario.
        Optional<Staff> staffPorDni = staffRepository.findByDni(dni);
        Optional<Usuario> usuarioPorEmail = usuarioRepository.findByEmail(email);
        Optional<Staff> staffPorEmail = staffRepository.findByUsuario_Email(email);

        Staff responsable;
        if (staffPorDni.isPresent() || usuarioPorEmail.isPresent() || staffPorEmail.isPresent()) {
            boolean coincidenMismaPersona = staffPorDni.isPresent()
                    && usuarioPorEmail.isPresent()
                    && staffPorEmail.isPresent()
                    && staffPorDni.get().getId() == staffPorEmail.get().getId()
                    && staffPorEmail.get().getUsuario() != null
                    && staffPorEmail.get().getUsuario().getId().equals(usuarioPorEmail.get().getId());

            if (!coincidenMismaPersona) {
                throw new IllegalArgumentException(
                        "El DNI y el correo ya están registrados. Solo se pueden reutilizar cuando ambos pertenecen al mismo responsable.");
            }

            // Se conserva la cuenta existente: no se crea otro Usuario ni otro Staff,
            // y la contraseña ingresada no modifica la contraseña actual.
            responsable = staffPorDni.get();

        } else {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException(
                        "La contraseña es obligatoria para registrar un responsable nuevo.");
            }
            if (password.length() < 8) {
                throw new IllegalArgumentException(
                        "La contraseña debe tener al menos 8 caracteres.");
            }

            Rol rolResponsable = rolRepository.findByNombre("RESPONSABLE")
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró el rol RESPONSABLE en la base de datos."));

            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setPasswordHash(passwordEncoder.encode(password));
            usuario.setRol(rolResponsable);
            usuario = usuarioRepository.save(usuario);

            responsable = new Staff();
            responsable.setNombre(nombre);
            responsable.setApellido(apellido);
            responsable.setDni(dni);
            responsable.setFechaNacimiento(fechaNacimiento);
            responsable.setFechaIngreso(fechaIngreso);
            responsable.setUsuario(usuario);
            responsable.setSueldo(sueldoProperties.baseOrDefault());
            responsable = staffRepository.save(responsable);
        }

        // Completar datos comunes de la unidad.
        String codigo = generarCodigo(tipoUnidad);
        unidad.setCodigo(codigo);
        unidad.setNombreComercial(nombreComercial);
        unidad.setSuperficieM2(superficieM2);
        unidad.setResponsable(responsable);

        BigDecimal canonCalculado = canonService.calcularCanon(unidad);
        if (canonCalculado == null || canonCalculado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "No se puede registrar la unidad de venta porque el canon debe ser mayor que cero.");
        }

        // Buscar el festival activo antes de guardar la unidad.
        Festival festivalActivo = festivalRepository.findAll().stream()
                .filter(f -> f.getFechaInicio() != null
                        && f.getFechaFin() != null
                        && !f.getFechaInicio().isAfter(hoy)
                        && !f.getFechaFin().isBefore(hoy))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay un festival activo para asociar la unidad de venta."));

        unidadVentaRepository.save(unidad);
        festivalActivo.getUnidadesVentaHabilitadas().add(unidad);
        festivalRepository.save(festivalActivo);
    }
}
