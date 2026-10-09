package com.example.hito2grupo3.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "unidades_venta")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UnidadVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nombreComercial;
    private BigDecimal superficieM2;

    // Relación ManyToMany con Plato
    @ManyToMany
    @JoinTable(
        name = "unidad_venta_plato",
        joinColumns = @JoinColumn(name = "unidad_venta_id"),
        inverseJoinColumns = @JoinColumn(name = "plato_id")
    )
    private List<Plato> platosOfrecidos = new ArrayList<>();

    // 1. Staff Asignado: Muchos a Muchos (1..* asigna 0..*)
    @ManyToMany
    @JoinTable(
        name = "unidad_venta_staff",
        joinColumns = @JoinColumn(name = "unidad_venta_id"),
        inverseJoinColumns = @JoinColumn(name = "staff_id")
    )
    private Set<Staff> staffAsignado = new HashSet<>();

    // 2. Responsable: Muchos a Uno (Un staff puede ser responsable de varias unidades, pero la unidad tiene 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Staff responsable;

    @ManyToMany(mappedBy = "unidadesVentaHabilitadas")
    private Set<Festival> festivales = new HashSet<>();

  
    @OneToMany(mappedBy = "unidadVenta")
    private List<Pedido> pedidos = new ArrayList<>();
    
}
