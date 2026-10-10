package com.example.hito2grupo3.entities;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cajero")
public class Cajero extends Staff {

    private String turno;
    private Integer numeroCaja;
    private BigDecimal plusOadicional;
}