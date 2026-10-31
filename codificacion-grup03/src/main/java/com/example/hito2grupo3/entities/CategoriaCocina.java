package com.example.hito2grupo3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Getter
@Entity
@Table(name = "categorias_cocina")
public class CategoriaCocina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotNull(message = "El plus fijo es obligatorio")
    @PositiveOrZero(message = "El plus fijo debe ser mayor o igual a cero")
    @Digits(integer = 10, fraction = 2, message = "El plus solo puede tener hasta 2 decimales")
    private BigDecimal plusFijo;

    public CategoriaCocina() {
    }

    public CategoriaCocina(Long id, String nombre, BigDecimal plusFijo) {
        this.id = id;
        this.nombre = nombre;
        this.plusFijo = plusFijo;
    }


}