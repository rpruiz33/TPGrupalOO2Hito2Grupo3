package com.example.hito2grupo3.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "Cajero")
public class Cajero extends Staff {

    private String turno;
    private Integer NumeroCaja;
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
