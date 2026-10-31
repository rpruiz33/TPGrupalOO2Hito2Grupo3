package com.example.hito2grupo3.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "Cajero")
public class Cajero extends Staff {

    @Pattern(regexp = "^(?i)(MAÑANA|MANANA|TARDE|NOCHE)$", message = "El turno debe ser Mañana, Tarde o Noche")
    private String turno;

    @Min(value = 1, message = "El número de caja debe ser al menos 1")
    private Integer NumeroCaja;

    @PositiveOrZero(message = "El plus o adicional debe ser mayor o igual a cero")
    @Digits(integer = 10, fraction = 2, message = "El plus solo puede tener hasta 2 decimales")
    private BigDecimal plusOadicional;

    public Cajero(long id, String nombre, String apellido, String dni,
                  LocalDate fechaNacimiento, LocalDate fechaIngreso,
                  java.math.BigDecimal sueldo,
                  com.example.hito2grupo3.entities.Usuario usuario,
                  String turno, Integer NumeroCaja, BigDecimal plusOadicional) {
        super(id, nombre, apellido, dni, fechaNacimiento, fechaIngreso, sueldo, usuario);
        this.turno = turno;
        this.NumeroCaja = NumeroCaja;
        this.plusOadicional = plusOadicional;
    }
}
