package com.example.hito2grupo3.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
@Entity
@Table(name = "staff")
@Inheritance(strategy = InheritanceType.JOINED)
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @jakarta.validation.constraints.NotBlank(message = "El nombre es obligatorio")
    @jakarta.validation.constraints.Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @jakarta.validation.constraints.NotBlank(message = "El apellido es obligatorio")
    @jakarta.validation.constraints.Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras y espacios")
    private String apellido;

    @jakarta.validation.constraints.NotBlank(message = "El DNI es obligatorio")
    @jakarta.validation.constraints.Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener 7 u 8 dígitos numéricos")
    private String dni;

    @jakarta.validation.constraints.NotNull(message = "La fecha de nacimiento es obligatoria")
    @jakarta.validation.constraints.Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate fechaNacimiento;

    @jakarta.validation.constraints.NotNull(message = "La fecha de ingreso es obligatoria")
    @jakarta.validation.constraints.PastOrPresent(message = "La fecha de ingreso no puede ser una fecha futura")
    private LocalDate fechaIngreso;

    @jakarta.validation.constraints.NotNull(message = "El sueldo es obligatorio")
    @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "El sueldo debe ser mayor a cero")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2, message = "El sueldo no puede tener más de 2 decimales")
    private BigDecimal sueldo;

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido != null ? apellido.trim() : null;
    }

    public void setDni(String dni) {
        this.dni = dni != null ? dni.trim() : null;
    }

    @Column(name = "activo", nullable = false, columnDefinition = "boolean default true")
    private boolean activo = true;

    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", unique = true)
    private Usuario usuario;

    public Staff() {
    }

    public Staff(long id, String nombre, String apellido, String dni,
            LocalDate fechaNacimiento, LocalDate fechaIngreso,
            BigDecimal sueldo, Usuario usuario) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaIngreso = fechaIngreso;
        this.sueldo = sueldo;
        this.usuario = usuario;
    }

}