
package com.example.hito2grupo3.services;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hito2grupo3.entities.FoodTruck;
import com.example.hito2grupo3.entities.PuestoDesarmable;
import com.example.hito2grupo3.entities.Rol;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.entities.Usuario;
import com.example.hito2grupo3.repositories.RolRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.repositories.UsuarioRepository;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegistroUnidadVentaService {

    private final UsuarioRepository usuarioRepository;
    private final StaffRepository staffRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroUnidadVentaService(
            UsuarioRepository usuarioRepository,
            StaffRepository staffRepository,
            UnidadVentaRepository unidadVentaRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.staffRepository = staffRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String generarCodigo(String tipoUnidad) {

        String prefijo;

        if ("FOOD_TRUCK".equals(tipoUnidad)) {
            prefijo = "FT-";
        } else if ("PUESTO_DESARMABLE".equals(tipoUnidad)) {
            prefijo = "PD-";
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

        if (numeroMayor == 9_999_999) {
            throw new IllegalArgumentException(
                    "Se alcanzó el máximo de códigos disponibles para este tipo de unidad.");
        }

        return prefijo + String.format("%07d", numeroMayor + 1);
    }

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

        // Normalizar datos de texto
        nombreComercial = nombreComercial == null ? "" : nombreComercial.trim();
        nombre = nombre == null ? "" : nombre.trim();
        apellido = apellido == null ? "" : apellido.trim();
        dni = dni == null ? "" : dni.trim();
        email = email == null ? "" : email.trim().toLowerCase();

        // Validar campos obligatorios
        if (nombreComercial.isBlank()
                || nombre.isBlank() || apellido.isBlank()
                || dni.isBlank() || email.isBlank()
                || password == null || password.isBlank()
                || superficieM2 == null || tipoUnidad == null
                || fechaNacimiento == null || fechaIngreso == null) {
            throw new IllegalArgumentException(
                    "Completá todos los campos obligatorios.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres.");
        }

        if (superficieM2.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La superficie debe ser mayor que cero.");
        }

        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser futura.");
        }

        if (fechaIngreso.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de ingreso no puede ser futura.");
        }

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese email.");
        }

        if (staffRepository.findByDni(dni).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una persona registrada con ese DNI.");
        }

        // Generar automáticamente el código según el tipo de unidad
        String codigo = generarCodigo(tipoUnidad);

        // Validar tipo y datos específicos de la unidad
        UnidadVenta unidad;

        if ("FOOD_TRUCK".equals(tipoUnidad)) {
            if (patente == null || patente.isBlank()) {
                throw new IllegalArgumentException(
                        "La patente es obligatoria para un Food Truck.");
            }

            FoodTruck foodTruck = new FoodTruck();
            foodTruck.setPatente(patente.trim());
            foodTruck.setRequiereElectricidad(
                    Boolean.TRUE.equals(requiereElectricidad));
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

        // Buscar el rol del responsable
        Rol rolResponsable = rolRepository.findByNombre("RESPONSABLE")
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el rol RESPONSABLE en la base de datos."));

        // Crear usuario y encriptar su contraseña
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(rolResponsable);
        usuario = usuarioRepository.save(usuario);

        // Crear al responsable como Staff
        Staff responsable = new Staff();
        responsable.setNombre(nombre);
        responsable.setApellido(apellido);
        responsable.setDni(dni);
        responsable.setFechaNacimiento(fechaNacimiento);
        responsable.setFechaIngreso(fechaIngreso);
        responsable.setUsuario(usuario);
        responsable = staffRepository.save(responsable);

        // Completar los datos comunes de la unidad
        unidad.setCodigo(codigo);
        unidad.setNombreComercial(nombreComercial);
        unidad.setSuperficieM2(superficieM2);
        unidad.setResponsable(responsable);

        // Guardar la unidad de venta
        unidadVentaRepository.save(unidad);
    }
}
