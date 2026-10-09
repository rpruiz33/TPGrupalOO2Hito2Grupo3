package com.example.hito2grupo3.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cocineros")
public class Cocinero extends Staff {

    private String especialidad;
    private boolean libretaSanitaria;
    private BigDecimal bonoPeligrosidad;

    @OneToOne
    @JoinColumn(name = "categoria_cocina_id", referencedColumnName = "id", unique = true, nullable = false)
    private CategoriaCocina categoriaCocina;
}