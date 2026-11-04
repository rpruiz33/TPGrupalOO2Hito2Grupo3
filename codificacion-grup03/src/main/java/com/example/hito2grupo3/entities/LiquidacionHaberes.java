
package com.example.hito2grupo3.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "liquidacion_haberes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_liquidacion_periodo",
            columnNames = {
                "staff_id",
                "unidad_venta_id",
                "festival_id",
                "periodo_inicio",
                "periodo_fin"
            }
        )
    }
)
public class LiquidacionHaberes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto_calculado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoCalculado;

    @Column(name = "fecha_liquidacion", nullable = false)
    private LocalDate fechaLiquidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidadVenta;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "dias_trabajados", nullable = false)
    private Integer diasTrabajados;

    @Column(name = "sueldo_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal sueldoBase;

    @Column(name = "monto_antiguedad", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAntiguedad = BigDecimal.ZERO;

    @Column(name = "plus_categoria", nullable = false, precision = 12, scale = 2)
    private BigDecimal plusCategoria = BigDecimal.ZERO;

    @Column(name = "factor_aplicado", nullable = false, precision = 8, scale = 4)
    private BigDecimal factorAplicado;
}
