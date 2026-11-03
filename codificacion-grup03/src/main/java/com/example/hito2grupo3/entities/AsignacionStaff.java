package com.example.hito2grupo3.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
    name = "asignaciones_staff",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "staff_id",
                "unidad_venta_id",
                "festival_id"
            }
        )
    }
)
public class AsignacionStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidadVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    
}