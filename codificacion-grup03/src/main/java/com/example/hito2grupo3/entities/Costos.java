package com.example.hito2grupo3.entities;

import java.math.BigDecimal;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;




@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Costos")
public class Costos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String tipoCosto;
    private BigDecimal monto;
    private String tipoUnidadDeMedida;


}
