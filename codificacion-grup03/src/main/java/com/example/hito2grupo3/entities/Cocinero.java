package com.example.hito2grupo3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Setter
@Getter
@Entity
@Table(name = "cocineros")
public class Cocinero extends Staff {

    @Size(max = 50, message = "La especialidad no debe superar los 50 caracteres")
    private String especialidad;
    private boolean libretaSanitaria;

    @PositiveOrZero(message = "El bono por peligrosidad debe ser mayor o igual a cero")
    @Digits(integer = 10, fraction = 2, message = "El bono solo puede tener hasta 2 decimales")
    private BigDecimal bonoPeligrosidad;

    @ManyToOne
    @JoinColumn(name = "categoria_cocina_id", referencedColumnName = "id", nullable = true)
    private CategoriaCocina categoriaCocina;

    public Cocinero() {
        super();
    }

    public Cocinero(long id, String nombre, String apellido, String dni,
            LocalDate fechaNacimiento, LocalDate fechaIngreso,
            BigDecimal sueldo, Usuario usuario,
            String especialidad, boolean libretaSanitaria,
            BigDecimal bonoPeligrosidad, CategoriaCocina categoriaCocina) {
        super(id, nombre, apellido, dni, fechaNacimiento, fechaIngreso, sueldo, usuario);
        this.especialidad = especialidad;
        this.libretaSanitaria = libretaSanitaria;
        this.bonoPeligrosidad = bonoPeligrosidad;
        this.categoriaCocina = categoriaCocina;
    }

    

   
}