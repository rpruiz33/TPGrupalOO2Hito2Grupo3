package com.example.hito2grupo3.dto;

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
    private String nombre;
    private String apellido;
    private String dni;
    private String email;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaIngreso;

    private BigDecimal sueldo;
    private String tipo; // "CAJERO", "COCINERO", "GENERAL"

    // Datos específicos de Cajero
    private String turno;
    private Integer numeroCaja;
    private BigDecimal plusOadicional;

    // Datos específicos de Cocinero
    private String especialidad;
    private boolean libretaSanitaria;
    private BigDecimal bonoPeligrosidad;
    private Long categoriaCocinaId;

    private boolean activo = true;
}
