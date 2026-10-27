package com.example.hito2grupo3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
@Entity
@Table(name = "categorias_cocina")
public class CategoriaCocina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private BigDecimal plusFijo;

    public CategoriaCocina() {
    }

    public CategoriaCocina(Long id, String nombre, BigDecimal plusFijo) {
        this.id = id;
        this.nombre = nombre;
        this.plusFijo = plusFijo;
    }


}