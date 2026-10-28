package com.example.hito2grupo3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "cocineros")
public class Cocinero extends Staff {

    private String especialidad;
    private boolean libretaSanitaria;
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