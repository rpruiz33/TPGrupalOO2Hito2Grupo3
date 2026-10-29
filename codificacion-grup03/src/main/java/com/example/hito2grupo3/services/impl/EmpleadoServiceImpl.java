package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.EmpleadoDTO;
import com.example.hito2grupo3.entities.*;
import com.example.hito2grupo3.repositories.*;
import com.example.hito2grupo3.services.IEmpleadoService;
import com.example.hito2grupo3.services.IUsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmpleadoServiceImpl implements IEmpleadoService {

    private final StaffRepository staffRepository;
    private final CajeroRepository cajeroRepository;
    private final CocineroRepository cocineroRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CategoriaCocinaRepository categoriaCocinaRepository;
    private final IUsuarioService usuarioService;

    public EmpleadoServiceImpl(StaffRepository staffRepository,
                               CajeroRepository cajeroRepository,
                               CocineroRepository cocineroRepository,
                               UsuarioRepository usuarioRepository,
                               RolRepository rolRepository,
                               CategoriaCocinaRepository categoriaCocinaRepository,
                               IUsuarioService usuarioService) {
        this.staffRepository = staffRepository;
        this.cajeroRepository = cajeroRepository;
        this.cocineroRepository = cocineroRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.categoriaCocinaRepository = categoriaCocinaRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Staff> listarTodos() {
        return staffRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Staff buscarPorId(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoDTO obtenerDtoPorId(Long id) {
        Staff staff = buscarPorId(id);
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setId(staff.getId());
        dto.setNombre(staff.getNombre());
        dto.setApellido(staff.getApellido());
        dto.setDni(staff.getDni());
        dto.setFechaNacimiento(staff.getFechaNacimiento());
        dto.setFechaIngreso(staff.getFechaIngreso());
        dto.setSueldo(staff.getSueldo());
        dto.setActivo(staff.isActivo());

        if (staff.getUsuario() != null) {
            dto.setEmail(staff.getUsuario().getEmail());
            dto.setUsuarioId(staff.getUsuario().getId());
        }

        if (staff instanceof Cajero c) {
            dto.setTipo("CAJERO");
            dto.setTurno(c.getTurno());
            dto.setNumeroCaja(c.getNumeroCaja());
            dto.setPlusOadicional(c.getPlusOadicional());
        } else if (staff instanceof Cocinero k) {
            dto.setTipo("COCINERO");
            dto.setEspecialidad(k.getEspecialidad());
            dto.setLibretaSanitaria(k.isLibretaSanitaria());
            dto.setBonoPeligrosidad(k.getBonoPeligrosidad());
            if (k.getCategoriaCocina() != null) {
                dto.setCategoriaCocinaId(k.getCategoriaCocina().getId());
            }
        } else {
            dto.setTipo("GENERAL");
        }

        return dto;
    }

    @Override
    @Transactional
    public Staff crearEmpleado(EmpleadoDTO dto) {
        // Validar DNI único
        if (staffRepository.findByDni(dto.getDni().trim()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un empleado con el DNI: " + dto.getDni());
        }

        // Validar Email único
        String email = dto.getEmail().trim().toLowerCase();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + email);
        }

        // Determinar rol del usuario
        String nombreRol = switch (dto.getTipo() != null ? dto.getTipo().toUpperCase() : "GENERAL") {
            case "CAJERO" -> "CAJERO";
            case "COCINERO" -> "COCINERO";
            default -> "EMPLEADO";
        };

        Rol rol = rolRepository.findByNombre(nombreRol)
                .or(() -> rolRepository.findByNombre("ROLE_" + nombreRol))
                .orElseGet(() -> rolRepository.save(new Rol(0, nombreRol)));

        // Crear usuario y enviar credenciales automáticas
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setActivo(true);
        Usuario usuarioGuardado = usuarioService.crearUsuario(usuario);

        LocalDate fechaIngreso = dto.getFechaIngreso() != null ? dto.getFechaIngreso() : LocalDate.now();
        BigDecimal sueldo = dto.getSueldo() != null ? dto.getSueldo() : BigDecimal.ZERO;

        if ("CAJERO".equalsIgnoreCase(dto.getTipo())) {
            Cajero cajero = new Cajero();
            cajero.setNombre(dto.getNombre().trim());
            cajero.setApellido(dto.getApellido().trim());
            cajero.setDni(dto.getDni().trim());
            cajero.setFechaNacimiento(dto.getFechaNacimiento());
            cajero.setFechaIngreso(fechaIngreso);
            cajero.setSueldo(sueldo);
            cajero.setActivo(true);
            cajero.setUsuario(usuarioGuardado);
            cajero.setTurno(dto.getTurno());
            cajero.setNumeroCaja(dto.getNumeroCaja());
            cajero.setPlusOadicional(dto.getPlusOadicional() != null ? dto.getPlusOadicional() : BigDecimal.ZERO);
            return cajeroRepository.save(cajero);
        } else if ("COCINERO".equalsIgnoreCase(dto.getTipo())) {
            Cocinero cocinero = new Cocinero();
            cocinero.setNombre(dto.getNombre().trim());
            cocinero.setApellido(dto.getApellido().trim());
            cocinero.setDni(dto.getDni().trim());
            cocinero.setFechaNacimiento(dto.getFechaNacimiento());
            cocinero.setFechaIngreso(fechaIngreso);
            cocinero.setSueldo(sueldo);
            cocinero.setActivo(true);
            cocinero.setUsuario(usuarioGuardado);
            cocinero.setEspecialidad(dto.getEspecialidad());
            cocinero.setLibretaSanitaria(dto.isLibretaSanitaria());
            cocinero.setBonoPeligrosidad(dto.getBonoPeligrosidad() != null ? dto.getBonoPeligrosidad() : BigDecimal.ZERO);
            if (dto.getCategoriaCocinaId() != null) {
                categoriaCocinaRepository.findById(dto.getCategoriaCocinaId())
                        .ifPresent(cocinero::setCategoriaCocina);
            }
            return cocineroRepository.save(cocinero);
        } else {
            Staff staff = new Staff();
            staff.setNombre(dto.getNombre().trim());
            staff.setApellido(dto.getApellido().trim());
            staff.setDni(dto.getDni().trim());
            staff.setFechaNacimiento(dto.getFechaNacimiento());
            staff.setFechaIngreso(fechaIngreso);
            staff.setSueldo(sueldo);
            staff.setActivo(true);
            staff.setUsuario(usuarioGuardado);
            return staffRepository.save(staff);
        }
    }

    @Override
    @Transactional
    public Staff actualizarEmpleado(Long id, EmpleadoDTO dto) {
        Staff staff = buscarPorId(id);

        // Validar DNI si cambió
        if (dto.getDni() != null && !dto.getDni().trim().isEmpty()) {
            String nuevoDni = dto.getDni().trim();
            if (staff.getDni() == null || !nuevoDni.equalsIgnoreCase(staff.getDni().trim())) {
                staffRepository.findByDni(nuevoDni).ifPresent(s -> {
                    if (s.getId() != id.longValue()) {
                        throw new IllegalArgumentException("Ya existe otro empleado registrado con el DNI: " + nuevoDni);
                    }
                });
                staff.setDni(nuevoDni);
            }
        }

        // Validar y actualizar email en el usuario vinculado
        if (staff.getUsuario() != null && dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String nuevoEmail = dto.getEmail().trim().toLowerCase();
            if (!staff.getUsuario().getEmail().equalsIgnoreCase(nuevoEmail)) {
                usuarioRepository.findByEmail(nuevoEmail).ifPresent(u -> {
                    if (!u.getId().equals(staff.getUsuario().getId())) {
                        throw new IllegalArgumentException("Ya existe otro usuario con el correo: " + nuevoEmail);
                    }
                });
                staff.getUsuario().setEmail(nuevoEmail);
                usuarioRepository.save(staff.getUsuario());
            }
        } else if (staff.getUsuario() == null && dto.getEmail() != null && !dto.getEmail().isBlank()) {
            usuarioRepository.findByEmail(dto.getEmail().trim().toLowerCase()).ifPresent(staff::setUsuario);
        }

        if (dto.getNombre() != null) {
            staff.setNombre(dto.getNombre().trim());
        }
        if (dto.getApellido() != null) {
            staff.setApellido(dto.getApellido().trim());
        }
        staff.setFechaNacimiento(dto.getFechaNacimiento());
        if (dto.getFechaIngreso() != null) {
            staff.setFechaIngreso(dto.getFechaIngreso());
        }
        if (dto.getSueldo() != null) {
            staff.setSueldo(dto.getSueldo());
        }

        if (staff instanceof Cajero c) {
            c.setTurno(dto.getTurno());
            c.setNumeroCaja(dto.getNumeroCaja());
            if (dto.getPlusOadicional() != null) {
                c.setPlusOadicional(dto.getPlusOadicional());
            }
            return cajeroRepository.save(c);
        } else if (staff instanceof Cocinero k) {
            k.setEspecialidad(dto.getEspecialidad());
            k.setLibretaSanitaria(dto.isLibretaSanitaria());
            if (dto.getBonoPeligrosidad() != null) {
                k.setBonoPeligrosidad(dto.getBonoPeligrosidad());
            }
            if (dto.getCategoriaCocinaId() != null) {
                k.setCategoriaCocina(categoriaCocinaRepository.findById(dto.getCategoriaCocinaId()).orElse(null));
            } else {
                k.setCategoriaCocina(null);
            }
            return cocineroRepository.save(k);
        }

        return staffRepository.save(staff);
    }

    @Override
    @Transactional
    public void darDeBaja(Long id) {
        // Usar UPDATE directo para evitar problemas con InheritanceType.JOINED
        int rows = staffRepository.actualizarActivo(id, false);
        if (rows == 0) {
            throw new IllegalArgumentException("No se encontró empleado con ID: " + id);
        }

        // Desactivar también el usuario vinculado
        staffRepository.findById(id).ifPresent(staff -> {
            if (staff.getUsuario() != null) {
                usuarioRepository.actualizarActivo(staff.getUsuario().getId(), false);
            }
        });
    }

    @Override
    @Transactional
    public void reactivar(Long id) {
        // Usar UPDATE directo para evitar problemas con InheritanceType.JOINED
        int rows = staffRepository.actualizarActivo(id, true);
        if (rows == 0) {
            throw new IllegalArgumentException("No se encontró empleado con ID: " + id);
        }

        // Reactivar el usuario vinculado sin cambiar la contraseña ni enviar mail
        staffRepository.findById(id).ifPresent(staff -> {
            if (staff.getUsuario() != null) {
                usuarioRepository.actualizarActivo(staff.getUsuario().getId(), true);
            }
        });
    }

    @Override
    @Transactional
    public void reenviarCredenciales(Long id) {
        Staff staff = buscarPorId(id);
        if (staff.getUsuario() == null) {
            throw new IllegalStateException("El empleado no tiene un usuario de acceso vinculado.");
        }
        usuarioService.reenviarCredenciales(staff.getUsuario().getId());
    }
}
