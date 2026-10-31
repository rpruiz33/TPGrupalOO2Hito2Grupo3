package com.example.hito2grupo3.entities;

import java.time.LocalDate;

import java.util.HashSet;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "festivales")
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @ManyToMany
    @JoinTable(
        name = "festival_staff",
        joinColumns = @JoinColumn(name = "festival_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id"))
        private Set<Usuario> staffGeneral = new HashSet<>();
    @ManyToMany
    @JoinTable(
        name = "festival_unidad_venta", 
        joinColumns = @JoinColumn(name = "festival_id"), 
        inverseJoinColumns = @JoinColumn(name = "unidad_venta_id")) 
    private Set<UnidadVenta> unidadesVentaHabilitadas = new HashSet<>();
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @jakarta.persistence.Column(nullable = false)
    private boolean activo = true;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "festival_id") // Crea la columna 'festival_id' en la tabla 'costos'
    private Set<Costos> costos = new HashSet<>();

    public Festival(Long id, String nombre, Set<Usuario> staffGeneral,
            Set<UnidadVenta> unidadesVentaHabilitadas, LocalDate fechaInicio,
            LocalDate fechaFin, Set<Costos> costos) {
        this.id = id;
        this.nombre = nombre;
        this.staffGeneral = staffGeneral;
        this.unidadesVentaHabilitadas = unidadesVentaHabilitadas;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.costos = costos;
    }

}
