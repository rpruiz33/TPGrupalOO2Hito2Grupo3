package com.example.hito2grupo3.entities;

import java.math.BigDecimal;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Cajero")
public class Cajero extends Staff {

    private String turno;
    private Integer NumeroCaja;
    private BigDecimal plusOadicional;
}
