package com.example.hito2grupo3.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "food_trucks")
public class FoodTruck extends UnidadVenta {
    private String patente;
    private boolean requiereElectricidad;

    public FoodTruck(Long id, String codigo, String nombreComercial, BigDecimal superficieM2,
                     List<Plato> platosOfrecidos, Set<Staff> staffAsignado,
                     Staff responsable, Set<Festival> festivales, List<Pedido> pedidos,
                     String patente, boolean requiereElectricidad) {
        super();
        setId(id);
        setCodigo(codigo);
        setNombreComercial(nombreComercial);
        setSuperficieM2(superficieM2);
        setPlatosOfrecidos(platosOfrecidos != null ? platosOfrecidos : new ArrayList<>());
        setStaffAsignado(staffAsignado != null ? staffAsignado : new HashSet<>());
        setResponsable(responsable);
        setFestivales(festivales != null ? festivales : new HashSet<>());
        setPedidos(pedidos != null ? pedidos : new ArrayList<>());
        this.patente = patente;
        this.requiereElectricidad = requiereElectricidad;
    }
}
