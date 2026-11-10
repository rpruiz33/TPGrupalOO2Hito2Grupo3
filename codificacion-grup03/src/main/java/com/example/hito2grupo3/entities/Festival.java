package com.example.hito2grupo3.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "La temporada es obligatoria")
    @Pattern(regexp = "Invierno|Primavera|Verano|Otoño", message = "La temporada debe ser Invierno, Primavera, Verano u Otoño")
    @Column(nullable = false)
    private String temporada;

    @ManyToMany
    @JoinTable(
        name = "festival_staff",
        joinColumns = @JoinColumn(name = "festival_id"),
        inverseJoinColumns = @JoinColumn(name = "staff_id"))
    private Set<Staff> staffGeneral = new HashSet<>();

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

    public Festival(Long id, String nombre, String temporada, Set<Staff> staffGeneral,
            Set<UnidadVenta> unidadesVentaHabilitadas, LocalDate fechaInicio,
            LocalDate fechaFin, Set<Costos> costos) {
        this.id = id;
        this.nombre = nombre;
        this.temporada = temporada;
        this.staffGeneral = staffGeneral;
        this.unidadesVentaHabilitadas = unidadesVentaHabilitadas;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.costos = costos;
    }

    public Festival(Long id, String nombre, Set<Staff> staffGeneral,
            Set<UnidadVenta> unidadesVentaHabilitadas, LocalDate fechaInicio,
            LocalDate fechaFin, Set<Costos> costos) {
        this(id, nombre, null, staffGeneral, unidadesVentaHabilitadas, fechaInicio, fechaFin, costos);
    }

    @PrePersist
    @PreUpdate
    private void asegurarTemporada() {
        if ((temporada == null || temporada.isBlank()) && fechaInicio != null) {
            temporada = determinarTemporada(fechaInicio);
        }
    }

    @PostLoad
    private void completarTemporadaAlCargar() {
        asegurarTemporada();
    }

    public static String determinarTemporada(LocalDate fecha) {
        int mes = fecha.getMonthValue();
        int dia = fecha.getDayOfMonth();

        if ((mes == 12 && dia >= 21) || mes == 1 || mes == 2 || (mes == 3 && dia <= 20)) {
            return "Verano";
        }
        if ((mes == 3 && dia >= 21) || mes == 4 || mes == 5 || (mes == 6 && dia <= 20)) {
            return "Otoño";
        }
        if ((mes == 6 && dia >= 21) || mes == 7 || mes == 8 || (mes == 9 && dia <= 20)) {
            return "Invierno";
        }
        return "Primavera";
    }

}
