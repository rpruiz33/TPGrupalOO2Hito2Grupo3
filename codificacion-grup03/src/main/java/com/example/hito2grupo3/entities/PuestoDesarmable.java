package com.example.hito2grupo3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter 
@Getter 

@Table(name = "puestos_desarmables")
public class PuestoDesarmable extends UnidadVenta {
    private Integer cantidadCarpas;
    private Integer tiempoMontajeMin;
}
