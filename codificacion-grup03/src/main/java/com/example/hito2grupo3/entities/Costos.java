package com.example.hito2grupo3.entities;

import java.math.BigDecimal;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;


@Data
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "costos")
public class Costos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String tipoCosto;
    private BigDecimal monto;
    private String tipoUnidadDeMedida;

    public Costos(long id, String tipoCosto, BigDecimal monto, String tipoUnidadDeMedida) {
        this.id = id;
        this.tipoCosto = tipoCosto;
        this.monto = monto;
        this.tipoUnidadDeMedida = tipoUnidadDeMedida;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}
