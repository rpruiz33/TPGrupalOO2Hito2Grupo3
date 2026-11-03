package com.example.hito2grupo3.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoDTO {

    private Long id;
    private Long usuarioId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras y espacios")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener 7 u 8 dígitos numéricos sin puntos ni espacios")
    private String dni;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe ingresar un formato de correo electrónico válido")
    private String email;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @PastOrPresent(message = "La fecha de ingreso no puede ser una fecha futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaIngreso;

    @NotNull(message = "El sueldo es obligatorio")
    @DecimalMin(value = "0.01", message = "El sueldo debe ser estrictamente mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "El sueldo no puede tener más de 2 decimales")
    private BigDecimal sueldo;

    private String tipo; // "CAJERO", "COCINERO", "GENERAL"

    // Datos específicos de Cajero
    @Pattern(regexp = "^$|^(?i)(MAÑANA|MANANA|TARDE|NOCHE)$", message = "El turno debe ser MAÑANA, TARDE o NOCHE")
    private String turno;

    @Min(value = 1, message = "El número de caja debe ser mayor o igual a 1")
    private Integer numeroCaja;

    @DecimalMin(value = "0.00", message = "El plus o adicional no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El plus no puede tener más de 2 decimales")
    private BigDecimal plusOadicional;

    // Datos específicos de Cocinero
    @Size(max = 50, message = "La especialidad no puede superar los 50 caracteres")
    private String especialidad;

    private boolean libretaSanitaria;

    @DecimalMin(value = "0.00", message = "El bono de peligrosidad no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El bono no puede tener más de 2 decimales")
    private BigDecimal bonoPeligrosidad;

    private Long categoriaCocinaId;

    private boolean activo = true;

    // Validaciones de dominio cruzadas
    @AssertTrue(message = "El empleado debe tener al menos 18 años de edad")
    public boolean isMayorDeEdad() {
        if (fechaNacimiento == null) {
            return true;
        }
        return !fechaNacimiento.plusYears(18).isAfter(LocalDate.now());
    }

    @AssertTrue(message = "La fecha de ingreso no puede ser anterior a la fecha de nacimiento")
    public boolean isFechaIngresoValida() {
        if (fechaNacimiento == null || fechaIngreso == null) {
            return true;
        }
        return !fechaIngreso.isBefore(fechaNacimiento);
    }

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido != null ? apellido.trim() : null;
    }

    public void setDni(String dni) {
        this.dni = dni != null ? dni.trim() : null;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }
}
